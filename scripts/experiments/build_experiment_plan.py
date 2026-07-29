#!/usr/bin/env python3
"""Construye el plan determinista de tareas para la Fase 8B."""

from __future__ import annotations

import argparse
import csv
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
    canonical_json_bytes,
    load_json,
    write_json_atomic,
    write_jsonl_atomic,
)


EXPECTED_PROTOCOL_ID = "paper1-q3-v1"
EXPECTED_TASKS = 1272
EXPECTED_WARMUPS = 112
EXPECTED_MEASURED = 1160


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Construye un plan experimental determinista y reanudable."
    )
    parser.add_argument("--spec", required=True)
    parser.add_argument("--configurations", required=True)
    parser.add_argument("--seeds", required=True)
    parser.add_argument("--cases", required=True)
    parser.add_argument("--output-plan", required=True)
    parser.add_argument("--output-manifest", required=True)
    return parser.parse_args()


def load_configurations(path: Path) -> list[dict[str, str]]:
    try:
        with path.open(encoding="utf-8", newline="") as stream:
            return list(csv.DictReader(stream))
    except FileNotFoundError as error:
        raise SystemExit(f"Falta la matriz de configuraciones: {path}.") from error


def load_seeds(path: Path) -> list[int]:
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except FileNotFoundError as error:
        raise SystemExit(f"Falta el archivo de seeds: {path}.") from error

    seeds: list[int] = []
    for line_number, raw_line in enumerate(lines, start=1):
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        try:
            seeds.append(int(line))
        except ValueError as error:
            raise SystemExit(
                f"Seed invalida en {path}:{line_number}: {line}."
            ) from error
    return seeds


def make_task_id(
    configuration_id: str,
    case_id: str,
    seed: int | None,
    repetition_kind: str,
    repetition_index: int,
) -> str:
    parts = [
        configuration_id,
        case_id,
        f"seed-{seed}" if seed is not None else "seed-none",
        repetition_kind,
        f"{repetition_index:02d}",
    ]
    return "__".join(parts)


def attach_task_hash(task: dict[str, Any]) -> dict[str, Any]:
    task_copy = dict(task)
    task_copy["task_sha256"] = calculate_sha256_bytes(
        canonical_json_bytes(task)
    )
    return task_copy


def build_repetition_tasks(
    row: dict[str, str],
    case: dict[str, Any],
    order_start: int,
) -> list[dict[str, Any]]:
    tasks: list[dict[str, Any]] = []
    warmups = int(row["warmup_repetitions"])
    measured = int(row["measured_repetitions"])
    repetition_groups = [
        ("warmup", warmups),
        ("measured", measured),
    ]

    for repetition_kind, count in repetition_groups:
        for repetition_index in range(1, count + 1):
            task_id = make_task_id(
                row["configuration_id"],
                str(case["id"]),
                None,
                repetition_kind,
                repetition_index,
            )
            task = {
                "schema_version": 1,
                "protocol_id": EXPECTED_PROTOCOL_ID,
                "planned_order": order_start + len(tasks),
                "task_id": task_id,
                "configuration_id": row["configuration_id"],
                "rq_ids": row["rq_ids"].split(";"),
                "tool": row["tool"],
                "model_kind": row["model_kind"],
                "bound_profile": row["bound_profile"],
                "fault_profile": row["fault_profile"],
                "case_type": row["case_type"],
                "case_id": case["id"],
                "expected_property": case.get("expected_property"),
                "model": case.get("model"),
                "configuration": case.get("configuration"),
                "seed": None,
                "repetition_kind": repetition_kind,
                "repetition_index": repetition_index,
                "timeout_seconds": int(row["timeout_seconds"]),
                "max_rss_mb": int(row["max_rss_mb"]),
            }
            tasks.append(attach_task_hash(task))
    return tasks


def build_rq3_tasks(
    row: dict[str, str],
    case_ids: list[str],
    seeds: list[int],
    order_start: int,
) -> list[dict[str, Any]]:
    tasks: list[dict[str, Any]] = []
    for case_id in case_ids:
        for seed in seeds:
            task_id = make_task_id(
                row["configuration_id"],
                case_id,
                seed,
                "measured",
                1,
            )
            task = {
                "schema_version": 1,
                "protocol_id": EXPECTED_PROTOCOL_ID,
                "planned_order": order_start + len(tasks),
                "task_id": task_id,
                "configuration_id": row["configuration_id"],
                "rq_ids": ["RQ3"],
                "tool": "tlc",
                "model_kind": row["model_kind"],
                "bound_profile": "catalog",
                "fault_profile": "catalog",
                "case_type": row["case_type"],
                "case_id": case_id,
                "expected_property": None,
                "model": None,
                "configuration": None,
                "seed": seed,
                "repetition_kind": "measured",
                "repetition_index": 1,
                "timeout_seconds": int(row["timeout_seconds"]),
                "max_rss_mb": int(row["max_rss_mb"]),
            }
            tasks.append(attach_task_hash(task))
    return tasks


def select_cases(
    row: dict[str, str],
    cases: dict[str, Any],
) -> list[dict[str, Any]]:
    rq_ids = set(row["rq_ids"].split(";"))
    if "RQ2" in rq_ids:
        return [
            item
            for item in cases["formal_mutants"]
            if item["tool"] == row["tool"]
        ]
    if "RQ1" in rq_ids:
        return [
            {
                "id": item["id"],
                "expected_property": item["id"],
            }
            for item in cases["properties"]
            if row["tool"] in item["tools"]
        ]
    if row["configuration_id"].startswith("RQ4-TLC-FAULT-"):
        return [
            {
                "id": f"fault-{row['fault_profile']}",
                "expected_property": None,
            }
        ]
    raise SystemExit(
        f"No existe un inventario para {row['configuration_id']}."
    )


def validate_inputs(
    spec: dict[str, Any],
    cases: dict[str, Any],
    rows: list[dict[str, str]],
    seeds: list[int],
) -> None:
    if spec.get("protocol_id") != EXPECTED_PROTOCOL_ID:
        raise SystemExit("El protocol_id no coincide con la Fase 8A.")
    if spec.get("status") != "frozen":
        raise SystemExit("El protocolo debe permanecer congelado.")
    if spec.get("definitive_results_generated") is not False:
        raise SystemExit("La Fase 8B no acepta resultados definitivos previos.")
    if cases.get("protocol_id") != EXPECTED_PROTOCOL_ID:
        raise SystemExit("El inventario de casos no coincide con el protocolo.")
    if cases.get("phase") != "8B":
        raise SystemExit("El inventario de casos debe declarar Fase 8B.")
    if len(rows) != 14:
        raise SystemExit("Se esperaban catorce configuraciones.")
    if len(seeds) != 30 or len(set(seeds)) != 30:
        raise SystemExit("Se esperaban treinta seeds unicas.")


def build_plan(
    spec: dict[str, Any],
    cases: dict[str, Any],
    rows: list[dict[str, str]],
    seeds: list[int],
) -> list[dict[str, Any]]:
    tasks: list[dict[str, Any]] = []
    for row in rows:
        if row["case_type"] in {"valid", "negative"}:
            case_ids = (
                cases["valid_trace_cases"]
                if row["case_type"] == "valid"
                else cases["negative_trace_cases"]
            )
            tasks.extend(
                build_rq3_tasks(
                    row,
                    case_ids,
                    seeds,
                    len(tasks) + 1,
                )
            )
            continue

        selected = select_cases(row, cases)
        for case in selected:
            tasks.extend(
                build_repetition_tasks(
                    row,
                    case,
                    len(tasks) + 1,
                )
            )

    task_ids = [task["task_id"] for task in tasks]
    if len(task_ids) != len(set(task_ids)):
        raise SystemExit("El plan contiene task_id duplicados.")
    if len(tasks) != EXPECTED_TASKS:
        raise SystemExit(
            f"Se esperaban {EXPECTED_TASKS} tareas y se generaron {len(tasks)}."
        )

    warmups = sum(
        task["repetition_kind"] == "warmup"
        for task in tasks
    )
    measured = sum(
        task["repetition_kind"] == "measured"
        for task in tasks
    )
    if warmups != EXPECTED_WARMUPS or measured != EXPECTED_MEASURED:
        raise SystemExit(
            "La cantidad de calentamientos o repeticiones medidas no coincide."
        )
    return tasks


def build_manifest(
    paths: dict[str, Path],
    tasks: list[dict[str, Any]],
) -> dict[str, Any]:
    plan_bytes = b"".join(
        json.dumps(task, ensure_ascii=False, sort_keys=True).encode("utf-8")
        + b"\n"
        for task in tasks
    )
    by_configuration: dict[str, int] = {}
    by_rq: dict[str, int] = {}
    for task in tasks:
        by_configuration[task["configuration_id"]] = (
            by_configuration.get(task["configuration_id"], 0) + 1
        )
        for rq_id in task["rq_ids"]:
            by_rq[rq_id] = by_rq.get(rq_id, 0) + 1

    return {
        "schema_version": 1,
        "phase": "8B",
        "protocol_id": EXPECTED_PROTOCOL_ID,
        "status": "planned",
        "definitive_results_generated": False,
        "input_sha256": {
            name: calculate_sha256_file(path)
            for name, path in paths.items()
        },
        "plan_sha256": calculate_sha256_bytes(plan_bytes),
        "counts": {
            "tasks_total": len(tasks),
            "warmup_tasks": sum(
                task["repetition_kind"] == "warmup"
                for task in tasks
            ),
            "measured_tasks": sum(
                task["repetition_kind"] == "measured"
                for task in tasks
            ),
            "by_configuration": by_configuration,
            "by_rq": by_rq,
        },
    }


def main() -> None:
    args = parse_args()
    paths = {
        "spec": Path(args.spec),
        "configurations": Path(args.configurations),
        "seeds": Path(args.seeds),
        "cases": Path(args.cases),
    }
    spec = load_json(paths["spec"])
    cases = load_json(paths["cases"])
    rows = load_configurations(paths["configurations"])
    seeds = load_seeds(paths["seeds"])

    validate_inputs(spec, cases, rows, seeds)
    tasks = build_plan(spec, cases, rows, seeds)
    manifest = build_manifest(paths, tasks)

    write_jsonl_atomic(Path(args.output_plan), tasks)
    write_json_atomic(Path(args.output_manifest), manifest)

    print(
        "Plan experimental construido correctamente con "
        f"{len(tasks)} tareas."
    )


if __name__ == "__main__":
    main()
