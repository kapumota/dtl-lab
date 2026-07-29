#!/usr/bin/env python3
"""Construye un subconjunto científico representativo de seis tareas."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any

SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

from experiment_io import (  # noqa: E402
    calculate_sha256_bytes,
    calculate_sha256_file,
    load_json,
    load_jsonl,
    write_json_atomic,
    write_jsonl_atomic,
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Construye el plan de smoke científico de Fase 8C."
    )
    parser.add_argument("--plan", required=True)
    parser.add_argument("--manifest", required=True)
    parser.add_argument("--profiles", required=True)
    parser.add_argument("--output-plan", required=True)
    parser.add_argument("--output-manifest", required=True)
    return parser.parse_args()


def count_by_key(
    tasks: list[dict[str, Any]],
    key: str,
) -> dict[str, int]:
    counts: dict[str, int] = {}
    for task in tasks:
        value = str(task[key])
        counts[value] = counts.get(value, 0) + 1
    return counts


def main() -> None:
    args = parse_args()
    plan_path = Path(args.plan)
    manifest_path = Path(args.manifest)
    profiles_path = Path(args.profiles)
    tasks = load_jsonl(plan_path)
    manifest = load_json(manifest_path)
    profiles = load_json(profiles_path)

    selected_ids = profiles.get("smoke_task_ids")
    if not isinstance(selected_ids, list) or len(selected_ids) != 6:
        raise SystemExit("El perfil smoke debe declarar seis task_id.")

    by_id = {task["task_id"]: task for task in tasks}
    selected: list[dict[str, Any]] = []
    for task_id in selected_ids:
        if task_id not in by_id:
            raise SystemExit(f"Falta la tarea smoke: {task_id}.")
        selected.append(by_id[task_id])

    if len({task["task_id"] for task in selected}) != 6:
        raise SystemExit("El plan smoke contiene identificadores repetidos.")

    plan_bytes = b"".join(
        json.dumps(task, ensure_ascii=False, sort_keys=True).encode("utf-8")
        + b"\n"
        for task in selected
    )
    by_rq: dict[str, int] = {}
    for task in selected:
        for rq_id in task["rq_ids"]:
            by_rq[rq_id] = by_rq.get(rq_id, 0) + 1

    output_manifest = {
        "schema_version": 1,
        "phase": "8C",
        "protocol_id": "paper1-q3-v1",
        "status": "planned",
        "run_kind": "scientific_smoke",
        "definitive_results_generated": False,
        "source_plan_sha256": calculate_sha256_file(plan_path),
        "source_manifest_sha256": calculate_sha256_file(manifest_path),
        "plan_sha256": calculate_sha256_bytes(plan_bytes),
        "input_sha256": manifest.get("input_sha256", {}),
        "counts": {
            "tasks_total": 6,
            "warmup_tasks": 0,
            "measured_tasks": 6,
            "by_configuration": count_by_key(
                selected,
                "configuration_id",
            ),
            "by_rq": by_rq,
        },
    }

    write_jsonl_atomic(Path(args.output_plan), selected)
    write_json_atomic(Path(args.output_manifest), output_manifest)
    print("Plan científico smoke construido con seis tareas.")


if __name__ == "__main__":
    main()
