#!/usr/bin/env python3
"""Valida la estructura raw de una ejecución experimental."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path
from typing import Any

SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

from experiment_io import (  # noqa: E402
    calculate_sha256_file,
    load_json,
    load_jsonl,
    require_sha256,
)


TERMINAL_STATUSES = {
    "completed",
    "timeout",
    "out_of_memory",
    "tool_error",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Valida una ejecución experimental raw."
    )
    parser.add_argument("--run-dir", required=True)
    parser.add_argument("--allow-partial", action="store_true")
    parser.add_argument("--expect-completed", type=int)
    parser.add_argument(
        "--expect-run-kind",
            choices=[
                "infrastructure_smoke",
                "scientific_smoke",
                "definitive",
            ],
    )
    return parser.parse_args()


def require_file(path: Path) -> None:
    if not path.is_file():
        raise SystemExit(f"Falta el archivo obligatorio: {path}.")


def validate_result(
    result: dict[str, Any],
    task: dict[str, Any],
    run_dir: Path,
) -> None:
    required = {
        "schema_version",
        "protocol_id",
        "task_id",
        "task_sha256",
        "status",
        "started_at_utc",
        "finished_at_utc",
        "elapsed_seconds",
        "max_rss_kb",
        "exit_code",
        "expectation_met",
        "stdout_path",
        "stderr_path",
        "time_path",
        "executor_result_path",
    }
    missing = required.difference(result)
    if missing:
        raise SystemExit(
            f"Faltan campos en el resultado de {task['task_id']}: "
            f"{sorted(missing)}."
        )
    if result["schema_version"] != 1:
        raise SystemExit("El resultado debe usar schema_version 1.")
    if result["protocol_id"] != "paper1-q3-v1":
        raise SystemExit("El resultado no coincide con el protocolo.")
    if result["task_id"] != task["task_id"]:
        raise SystemExit("El resultado no coincide con su task_id.")
    if result["task_sha256"] != task["task_sha256"]:
        raise SystemExit("El resultado no coincide con la definición de tarea.")
    require_sha256(result["task_sha256"], "task_sha256")
    if result["status"] not in TERMINAL_STATUSES:
        raise SystemExit(
            f"Estado terminal inválido en {task['task_id']}."
        )
    elapsed = result["elapsed_seconds"]
    if not isinstance(elapsed, (int, float)) or elapsed < 0:
        raise SystemExit(
            f"elapsed_seconds inválido en {task['task_id']}."
        )
    max_rss = result["max_rss_kb"]
    if max_rss is not None and (
        not isinstance(max_rss, int) or max_rss < 0
    ):
        raise SystemExit(f"max_rss_kb inválido en {task['task_id']}.")

    for field_name in (
        "stdout_path",
        "stderr_path",
        "time_path",
    ):
        relative = Path(result[field_name])
        if relative.is_absolute() or ".." in relative.parts:
            raise SystemExit(
                f"Ruta insegura en {field_name} de {task['task_id']}."
            )
        require_file(run_dir / relative)

    executor_result = result["executor_result_path"]
    if executor_result:
        relative = Path(executor_result)
        if relative.is_absolute() or ".." in relative.parts:
            raise SystemExit(
                f"Ruta insegura en executor_result_path de "
                f"{task['task_id']}."
            )
        require_file(run_dir / relative)


def main() -> None:
    args = parse_args()
    run_dir = Path(args.run_dir).resolve()
    snapshots = run_dir / "snapshots"
    plan_path = snapshots / "plan.jsonl"
    manifest_path = snapshots / "plan-manifest.json"

    for path in (
        plan_path,
        manifest_path,
        run_dir / "environment.json",
        run_dir / "provenance.json",
        run_dir / "state.json",
    ):
        require_file(path)

    if (run_dir / "derived").exists():
        raise SystemExit(
            "Los resultados derivados no deben estar dentro del raw."
        )

    tasks = load_jsonl(plan_path)
    manifest = load_json(manifest_path)
    provenance = load_json(run_dir / "provenance.json")
    state = load_json(run_dir / "state.json")

    if calculate_sha256_file(plan_path) != manifest.get("plan_sha256"):
        raise SystemExit("El snapshot del plan no coincide con su manifiesto.")
    if provenance.get("plan_sha256") != calculate_sha256_file(plan_path):
        raise SystemExit("La procedencia no conserva el hash del plan.")
    if provenance.get("results_are_raw") is not True:
        raise SystemExit("La procedencia debe declarar resultados raw.")
    if provenance.get("derived_results_generated") is not False:
        raise SystemExit("La Fase 8B no debe generar resultados derivados.")
    if args.expect_run_kind is not None and (
        provenance.get("run_kind") != args.expect_run_kind
    ):
        raise SystemExit("El tipo de ejecución no coincide.")

    task_by_id = {task["task_id"]: task for task in tasks}
    if len(task_by_id) != len(tasks):
        raise SystemExit("El snapshot contiene task_id duplicados.")

    results: list[dict[str, Any]] = []
    tasks_root = run_dir / "tasks"
    if tasks_root.is_dir():
        for result_path in sorted(tasks_root.glob("*/result.json")):
            result = load_json(result_path)
            task_id = result.get("task_id")
            if task_id not in task_by_id:
                raise SystemExit(
                    f"Existe un resultado para una tarea ajena: {task_id}."
                )
            validate_result(result, task_by_id[task_id], run_dir)
            results.append(result)

    if not args.allow_partial and len(results) != len(tasks):
        raise SystemExit(
            f"La ejecución está incompleta: {len(results)} de {len(tasks)}."
        )
    if args.expect_completed is not None:
        completed = sum(
            result["status"] == "completed"
            for result in results
        )
        if completed != args.expect_completed:
            raise SystemExit(
                f"Se esperaban {args.expect_completed} resultados "
                f"completed y existen {completed}."
            )

    terminal_count = sum(
        state.get("terminal_results_by_status", {}).values()
    )
    if terminal_count > len(results):
        raise SystemExit(
            "El estado declara más resultados que los archivos raw."
        )

    print(
        "La ejecución experimental raw es consistente. "
        f"Resultados={len(results)}, tareas={len(tasks)}."
    )


if __name__ == "__main__":
    main()
