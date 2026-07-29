#!/usr/bin/env python3
"""Construye el manifiesto de integridad de una ejecución raw."""

from __future__ import annotations

import argparse
import json
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

from experiment_io import (  # noqa: E402
    calculate_sha256_file,
    load_json,
    load_jsonl,
    write_json_atomic,
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Finaliza el manifiesto raw de la Fase 8C."
    )
    parser.add_argument("--run-dir", required=True)
    parser.add_argument("--expected-tasks", type=int, required=True)
    parser.add_argument(
        "--run-kind",
        choices=["scientific_smoke", "definitive"],
        required=True,
    )
    return parser.parse_args()


def increment(mapping: dict[str, int], key: str) -> None:
    mapping[key] = mapping.get(key, 0) + 1


def main() -> None:
    args = parse_args()
    run_dir = Path(args.run_dir).resolve()
    plan_path = run_dir / "snapshots/plan.jsonl"
    manifest_path = run_dir / "snapshots/plan-manifest.json"
    provenance_path = run_dir / "provenance.json"
    environment_path = run_dir / "environment.json"

    for path in (
        plan_path,
        manifest_path,
        provenance_path,
        environment_path,
    ):
        if not path.is_file():
            raise SystemExit(f"Falta el archivo obligatorio: {path}.")

    tasks = load_jsonl(plan_path)
    plan_manifest = load_json(manifest_path)
    provenance = load_json(provenance_path)

    if len(tasks) != args.expected_tasks:
        raise SystemExit(
            "La cantidad de tareas no coincide con el cierre solicitado."
        )
    if provenance.get("run_kind") != args.run_kind:
        raise SystemExit("El tipo de ejecución no coincide con el cierre.")

    task_by_id = {task["task_id"]: task for task in tasks}
    result_paths = sorted((run_dir / "tasks").glob("*/result.json"))
    if len(result_paths) != len(tasks):
        raise SystemExit(
            f"La ejecución está incompleta: {len(result_paths)} de {len(tasks)}."
        )

    by_status: dict[str, int] = {}
    by_tool: dict[str, int] = {}
    by_rq: dict[str, int] = {}
    logical_outcomes: dict[str, int] = {}
    expectation_met = 0
    files: list[dict[str, str]] = []

    for result_path in result_paths:
        result = load_json(result_path)
        task_id = result.get("task_id")
        if task_id not in task_by_id:
            raise SystemExit(f"Resultado ajeno al plan: {task_id}.")
        task = task_by_id[task_id]
        increment(by_status, str(result.get("status")))
        increment(by_tool, str(task.get("tool")))
        for rq_id in task.get("rq_ids", []):
            increment(by_rq, str(rq_id))
        if result.get("expectation_met") is True:
            expectation_met += 1
        payload = result.get("executor_payload", {})
        if isinstance(payload, dict):
            logical = payload.get("logical_outcome")
            if isinstance(logical, str) and logical:
                increment(logical_outcomes, logical)
        files.append(
            {
                "task_id": str(task_id),
                "path": str(result_path.relative_to(run_dir)),
                "sha256": calculate_sha256_file(result_path),
            }
        )

    raw_manifest: dict[str, Any] = {
        "schema_version": 1,
        "phase": "8C",
        "protocol_id": "paper1-q3-v1",
        "run_kind": args.run_kind,
        "status": "complete",
        "completed_at_utc": datetime.now(timezone.utc).isoformat(),
        "plan_sha256": calculate_sha256_file(plan_path),
        "plan_manifest_sha256": calculate_sha256_file(manifest_path),
        "environment_sha256": calculate_sha256_file(environment_path),
        "provenance_sha256": calculate_sha256_file(provenance_path),
        "source_plan_sha256": plan_manifest.get("source_plan_sha256"),
        "counts": {
            "tasks_total": len(tasks),
            "results_total": len(result_paths),
            "expectation_met": expectation_met,
            "by_status": by_status,
            "by_tool": by_tool,
            "by_rq": by_rq,
            "logical_outcomes": logical_outcomes,
        },
        "results": files,
        "derived_results_generated": False,
        "tables_generated": False,
        "figures_generated": False,
    }
    write_json_atomic(run_dir / "raw-manifest.json", raw_manifest)
    print(
        "El manifiesto raw de Fase 8C fue generado. "
        f"Tareas={len(tasks)}."
    )


if __name__ == "__main__":
    main()
