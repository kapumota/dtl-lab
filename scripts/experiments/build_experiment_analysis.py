#!/usr/bin/env python3
"""Genera resultados derivados, tablas y figuras para la Fase 8D."""

from __future__ import annotations

import argparse
import csv
import json
import math
import shutil
import sys
import tempfile
from collections import defaultdict
from collections.abc import Iterable
from pathlib import Path
from typing import Any

SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

from analysis_statistics import (  # noqa: E402
    spearman_correlation,
    summarize_numeric,
    wilson_interval,
)
from analysis_svg import (  # noqa: E402
    render_architecture,
    render_bar_chart,
    render_flow,
    render_line_chart,
)
from experiment_io import (  # noqa: E402
    calculate_sha256_file,
    load_json,
    load_jsonl,
    require_relative_path,
    write_json_atomic,
    write_text_atomic,
)

PROTOCOL_ID = "paper1-q3-v1"
TERMINAL_STATUSES = {
    "completed",
    "timeout",
    "out_of_memory",
    "tool_error",
}
PROFILE_ORDER = {
    "small": 1,
    "medium": 2,
    "large": 3,
}
NUMERIC_METRICS = (
    "elapsed_seconds",
    "max_rss_kb",
    "states_generated",
    "distinct_states",
    "depth",
    "solve_duration_ms",
)
DATASET_FIELDS = (
    "task_id",
    "planned_order",
    "configuration_id",
    "rq_ids",
    "tool",
    "model_kind",
    "case_type",
    "case_id",
    "property",
    "bound_profile",
    "fault_profile",
    "seed",
    "repetition_kind",
    "repetition_index",
    "status",
    "expectation_met",
    "logical_outcome",
    "elapsed_seconds",
    "max_rss_kb",
    "exit_code",
    "tool_elapsed_seconds",
    "tool_max_memory_kb",
    "states_generated",
    "distinct_states",
    "depth",
    "scope",
    "counterexamples",
    "solve_duration_ms",
    "accepted",
    "diagnostic_matches",
    "checked_abstract_steps",
    "rejected_abstract_step",
    "rejected_concrete_step",
    "rejected_action",
    "transfer_id",
    "is_measured",
    "is_censored",
    "included_main",
    "numeric_observation",
    "exclusion_reason",
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Genera el análisis reproducible de la Fase 8D."
    )
    parser.add_argument("--run-dir", required=True)
    parser.add_argument("--experiment-spec", required=True)
    parser.add_argument("--analysis-spec", required=True)
    parser.add_argument("--derived-dir", required=True)
    parser.add_argument("--tables-dir", required=True)
    parser.add_argument("--figures-dir", required=True)
    parser.add_argument("--expect-tasks", type=int, default=1272)
    return parser.parse_args()


def require_file(path: Path) -> None:
    if not path.is_file():
        raise SystemExit(f"Falta el archivo obligatorio: {path}.")


def require_outside_raw(run_dir: Path, output_dir: Path, field_name: str) -> None:
    try:
        output_dir.relative_to(run_dir)
    except ValueError:
        return
    raise SystemExit(f"{field_name} no puede estar dentro de resultados raw.")


def json_scalar(value: Any) -> str | int | float | bool | None:
    if value is None or isinstance(value, (str, int, float, bool)):
        return value
    return json.dumps(value, ensure_ascii=False, sort_keys=True)


def optional_number(value: Any) -> int | float | None:
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        return None
    number = float(value)
    if not math.isfinite(number):
        return None
    if isinstance(value, int):
        return value
    return number


def write_csv_atomic(
    path: Path,
    rows: Iterable[dict[str, Any]],
    fieldnames: Iterable[str],
) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    descriptor, temporary_name = tempfile.mkstemp(
        prefix=f".{path.name}.",
        suffix=".tmp",
        dir=path.parent,
        text=True,
    )
    temporary_path = Path(temporary_name)
    try:
        with open(descriptor, "w", encoding="utf-8", newline="") as stream:
            writer = csv.DictWriter(
                stream,
                fieldnames=list(fieldnames),
                extrasaction="ignore",
                lineterminator="\n",
            )
            writer.writeheader()
            for row in rows:
                writer.writerow(
                    {
                        key: json_scalar(row.get(key))
                        for key in writer.fieldnames
                    }
                )
        temporary_path.replace(path)
    except Exception:
        temporary_path.unlink(missing_ok=True)
        raise


def markdown_value(value: Any) -> str:
    if value is None:
        return ""
    if isinstance(value, float):
        if math.isnan(value) or math.isinf(value):
            return ""
        return f"{value:.6g}"
    return str(value).replace("|", "\\|").replace("\n", " ")


def write_markdown_table(
    path: Path,
    title: str,
    rows: list[dict[str, Any]],
    fields: list[tuple[str, str]],
) -> None:
    headers = [label for _, label in fields]
    lines = [
        f"### {title}",
        "",
        "| " + " | ".join(headers) + " |",
        "| " + " | ".join("---" for _ in headers) + " |",
    ]
    if rows:
        for row in rows:
            lines.append(
                "| "
                + " | ".join(
                    markdown_value(row.get(field_name))
                    for field_name, _ in fields
                )
                + " |"
            )
    else:
        lines.append(
            "| " + " | ".join("Sin datos" if index == 0 else "" for index in range(len(headers))) + " |"
        )
    lines.append("")
    write_text_atomic(path, "\n".join(lines))


def flatten_mapping(
    value: Any,
    *,
    prefix: str = "",
) -> list[tuple[str, Any]]:
    rows: list[tuple[str, Any]] = []
    if isinstance(value, dict):
        for key in sorted(value):
            child_prefix = f"{prefix}.{key}" if prefix else str(key)
            rows.extend(flatten_mapping(value[key], prefix=child_prefix))
        return rows
    if isinstance(value, list):
        rows.append((prefix, json.dumps(value, ensure_ascii=False, sort_keys=True)))
        return rows
    rows.append((prefix, value))
    return rows


def load_raw_context(
    run_dir: Path,
    expected_tasks: int,
) -> tuple[
    dict[str, Any],
    dict[str, Any],
    dict[str, Any],
    list[dict[str, Any]],
    list[dict[str, Any]],
]:
    manifest_path = run_dir / "raw-manifest.json"
    plan_path = run_dir / "snapshots/plan.jsonl"
    plan_manifest_path = run_dir / "snapshots/plan-manifest.json"
    environment_path = run_dir / "environment.json"
    provenance_path = run_dir / "provenance.json"
    state_path = run_dir / "state.json"
    for path in (
        manifest_path,
        plan_path,
        plan_manifest_path,
        environment_path,
        provenance_path,
        state_path,
    ):
        require_file(path)

    raw_manifest = load_json(manifest_path)
    plan_manifest = load_json(plan_manifest_path)
    environment = load_json(environment_path)
    tasks = load_jsonl(plan_path)
    provenance = load_json(provenance_path)

    if raw_manifest.get("schema_version") != 1:
        raise SystemExit("El manifiesto raw debe usar schema_version 1.")
    if raw_manifest.get("phase") != "8C":
        raise SystemExit("El manifiesto raw debe declarar Fase 8C.")
    if raw_manifest.get("protocol_id") != PROTOCOL_ID:
        raise SystemExit("El manifiesto raw no coincide con el protocolo.")
    if raw_manifest.get("run_kind") != "definitive":
        raise SystemExit("El análisis principal exige una ejecución definitiva.")
    if raw_manifest.get("status") != "complete":
        raise SystemExit("La ejecución raw debe estar completa.")
    if raw_manifest.get("derived_results_generated") is not False:
        raise SystemExit("El manifiesto raw no debe declarar derivados previos.")
    if raw_manifest.get("tables_generated") is not False:
        raise SystemExit("El manifiesto raw no debe declarar tablas previas.")
    if raw_manifest.get("figures_generated") is not False:
        raise SystemExit("El manifiesto raw no debe declarar figuras previas.")
    if provenance.get("results_are_raw") is not True:
        raise SystemExit("La procedencia debe declarar resultados raw.")
    if provenance.get("derived_results_generated") is not False:
        raise SystemExit("La procedencia de 8C no debe declarar derivados.")
    if provenance.get("run_kind") != "definitive":
        raise SystemExit("La procedencia no corresponde a la ejecución definitiva.")

    counts = raw_manifest.get("counts", {})
    tasks_total = counts.get("tasks_total")
    results_total = counts.get("results_total")
    if tasks_total != expected_tasks or results_total != expected_tasks:
        raise SystemExit(
            f"Se esperaban {expected_tasks} tareas y resultados completos."
        )
    if len(tasks) != expected_tasks:
        raise SystemExit("El snapshot del plan no tiene la cantidad esperada.")
    if plan_manifest.get("counts", {}).get("tasks_total") != expected_tasks:
        raise SystemExit("El manifiesto del plan no coincide con la ejecución.")
    if calculate_sha256_file(plan_path) != raw_manifest.get("plan_sha256"):
        raise SystemExit("El hash del plan no coincide con el manifiesto raw.")
    if calculate_sha256_file(plan_manifest_path) != raw_manifest.get(
        "plan_manifest_sha256"
    ):
        raise SystemExit("El hash del manifiesto del plan no coincide.")
    if calculate_sha256_file(environment_path) != raw_manifest.get(
        "environment_sha256"
    ):
        raise SystemExit("El hash del ambiente no coincide.")
    if calculate_sha256_file(provenance_path) != raw_manifest.get(
        "provenance_sha256"
    ):
        raise SystemExit("El hash de procedencia no coincide.")

    task_by_id = {str(task.get("task_id")): task for task in tasks}
    if len(task_by_id) != len(tasks):
        raise SystemExit("El plan contiene task_id duplicados.")

    results: list[dict[str, Any]] = []
    manifest_entries = raw_manifest.get("results")
    if not isinstance(manifest_entries, list) or len(manifest_entries) != expected_tasks:
        raise SystemExit("El manifiesto raw no enumera todos los resultados.")
    seen_tasks: set[str] = set()
    for entry in manifest_entries:
        if not isinstance(entry, dict):
            raise SystemExit("Existe una entrada inválida en el manifiesto raw.")
        task_id = str(entry.get("task_id"))
        if task_id not in task_by_id or task_id in seen_tasks:
            raise SystemExit("El manifiesto raw contiene una tarea ajena o duplicada.")
        seen_tasks.add(task_id)
        relative_path = require_relative_path(str(entry.get("path")), "path")
        result_path = run_dir / relative_path
        require_file(result_path)
        actual_hash = calculate_sha256_file(result_path)
        if actual_hash != entry.get("sha256"):
            raise SystemExit(f"El hash de {task_id} no coincide.")
        result = load_json(result_path)
        if result.get("task_id") != task_id:
            raise SystemExit(f"El resultado {task_id} declara otro task_id.")
        if result.get("task_sha256") != task_by_id[task_id].get("task_sha256"):
            raise SystemExit(f"El resultado {task_id} usa otra definición.")
        if result.get("status") not in TERMINAL_STATUSES:
            raise SystemExit(f"El resultado {task_id} no es terminal.")
        executor_relative = str(result.get("executor_result_path", ""))
        if executor_relative:
            executor_path = run_dir / require_relative_path(
                executor_relative,
                "executor_result_path",
            )
            require_file(executor_path)
            if load_json(executor_path) != result.get("executor_payload"):
                raise SystemExit(
                    f"El payload científico de {task_id} no coincide con su archivo."
                )
        results.append(result)

    actual_by_status = {
        status: sum(result.get("status") == status for result in results)
        for status in sorted(TERMINAL_STATUSES)
        if any(result.get("status") == status for result in results)
    }
    declared_by_status = counts.get("by_status", {})
    if actual_by_status != declared_by_status:
        raise SystemExit("Los conteos por estado no coinciden con el manifiesto raw.")
    actual_expectation_met = sum(
        result.get("expectation_met") is True for result in results
    )
    if counts.get("expectation_met") != actual_expectation_met:
        raise SystemExit(
            "El conteo de expectativas satisfechas no coincide con raw."
        )

    results.sort(key=lambda item: task_by_id[str(item["task_id"])]["planned_order"])
    return raw_manifest, plan_manifest, environment, tasks, results


def normalize_result(
    task: dict[str, Any],
    result: dict[str, Any],
) -> dict[str, Any]:
    payload = result.get("executor_payload")
    if not isinstance(payload, dict):
        payload = {}
    metrics = payload.get("metrics")
    if not isinstance(metrics, dict):
        metrics = {}

    repetition_kind = str(task.get("repetition_kind"))
    status = str(result.get("status"))
    is_measured = repetition_kind == "measured"
    is_censored = status in {"timeout", "out_of_memory"}
    numeric_observation = status == "completed"
    exclusion_reason = ""
    if not is_measured:
        exclusion_reason = "calentamiento"
    elif status == "tool_error" and not payload:
        exclusion_reason = "error de herramienta sin métricas científicas"

    row = {
        "task_id": task.get("task_id"),
        "planned_order": task.get("planned_order"),
        "configuration_id": task.get("configuration_id"),
        "rq_ids": ";".join(str(value) for value in task.get("rq_ids", [])),
        "tool": str(task.get("tool", "")).lower(),
        "model_kind": task.get("model_kind"),
        "case_type": task.get("case_type"),
        "case_id": task.get("case_id"),
        "property": payload.get("property") or task.get("expected_property"),
        "bound_profile": task.get("bound_profile"),
        "fault_profile": task.get("fault_profile"),
        "seed": task.get("seed"),
        "repetition_kind": repetition_kind,
        "repetition_index": task.get("repetition_index"),
        "status": status,
        "expectation_met": result.get("expectation_met"),
        "logical_outcome": payload.get("logical_outcome"),
        "elapsed_seconds": optional_number(result.get("elapsed_seconds")),
        "max_rss_kb": optional_number(result.get("max_rss_kb")),
        "exit_code": result.get("exit_code"),
        "tool_elapsed_seconds": optional_number(
            metrics.get("tool_elapsed_seconds")
        ),
        "tool_max_memory_kb": optional_number(
            metrics.get("tool_max_memory_kb")
        ),
        "states_generated": optional_number(metrics.get("states_generated")),
        "distinct_states": optional_number(metrics.get("distinct_states")),
        "depth": optional_number(metrics.get("depth")),
        "scope": json_scalar(metrics.get("scope")),
        "counterexamples": optional_number(metrics.get("counterexamples")),
        "solve_duration_ms": optional_number(metrics.get("solve_duration_ms")),
        "accepted": metrics.get("accepted"),
        "diagnostic_matches": metrics.get("diagnostic_matches"),
        "checked_abstract_steps": optional_number(
            metrics.get("checked_abstract_steps")
        ),
        "rejected_abstract_step": optional_number(
            metrics.get("rejected_abstract_step")
        ),
        "rejected_concrete_step": optional_number(
            metrics.get("rejected_concrete_step")
        ),
        "rejected_action": metrics.get("rejected_action"),
        "transfer_id": metrics.get("transfer_id"),
        "is_measured": is_measured,
        "is_censored": is_censored,
        "included_main": is_measured,
        "numeric_observation": numeric_observation,
        "exclusion_reason": exclusion_reason,
    }
    return row


def group_rows(
    rows: Iterable[dict[str, Any]],
    fields: tuple[str, ...],
) -> dict[tuple[Any, ...], list[dict[str, Any]]]:
    groups: dict[tuple[Any, ...], list[dict[str, Any]]] = defaultdict(list)
    for row in rows:
        groups[tuple(row.get(field) for field in fields)].append(row)
    return groups


def build_statistics(
    rows: list[dict[str, Any]],
    bootstrap_resamples: int,
) -> list[dict[str, Any]]:
    measured_rows = [row for row in rows if row["is_measured"]]
    group_fields = (
        "configuration_id",
        "rq_ids",
        "tool",
        "model_kind",
        "case_type",
        "case_id",
        "property",
        "bound_profile",
        "fault_profile",
    )
    statistics: list[dict[str, Any]] = []
    for key, group in sorted(group_rows(measured_rows, group_fields).items()):
        base = dict(zip(group_fields, key, strict=True))
        censored = sum(bool(row["is_censored"]) for row in group)
        errors = sum(row["status"] == "tool_error" for row in group)
        for metric in NUMERIC_METRICS:
            values = [
                row.get(metric)
                for row in group
                if row["numeric_observation"]
            ]
            summary = summarize_numeric(
                values,
                resamples=bootstrap_resamples,
                key="|".join(str(value) for value in (*key, metric)),
            )
            statistics.append(
                {
                    **base,
                    "metric": metric,
                    "n_total": len(group),
                    "n_observed": summary.pop("n"),
                    "n_censored": censored,
                    "n_tool_error": errors,
                    **summary,
                }
            )
    return statistics


def status_counts(group: list[dict[str, Any]]) -> dict[str, int]:
    return {
        status: sum(row["status"] == status for row in group)
        for status in sorted(TERMINAL_STATUSES)
    }


def build_table_01(
    run_dir: Path,
    environment: dict[str, Any],
) -> list[dict[str, Any]]:
    sources: list[tuple[str, dict[str, Any]]] = [("environment.json", environment)]
    for name in ("runtime.json", "timing-host.json", "provenance.json"):
        path = run_dir / name
        if path.is_file():
            sources.append((name, load_json(path)))
    rows: list[dict[str, Any]] = []
    for source, mapping in sources:
        for field, value in flatten_mapping(mapping):
            rows.append(
                {
                    "source": source,
                    "field": field,
                    "value": json_scalar(value),
                }
            )
    return rows


def build_table_02(rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    selected = [
        row
        for row in rows
        if row["is_measured"] and "RQ1" in row["rq_ids"].split(";")
    ]
    fields = ("tool", "property", "bound_profile")
    output: list[dict[str, Any]] = []
    for key, group in sorted(group_rows(selected, fields).items()):
        counts = status_counts(group)
        output.append(
            {
                **dict(zip(fields, key, strict=True)),
                "measured_runs": len(group),
                "completed": counts["completed"],
                "passed": sum(row["logical_outcome"] == "passed" for row in group),
                "counterexample": sum(
                    row["logical_outcome"] == "counterexample" for row in group
                ),
                "timeout": counts["timeout"],
                "out_of_memory": counts["out_of_memory"],
                "tool_error": counts["tool_error"],
                "expectation_met": sum(row["expectation_met"] is True for row in group),
            }
        )
    return output


def build_table_03(rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    selected = [
        row
        for row in rows
        if row["is_measured"] and "RQ2" in row["rq_ids"].split(";")
    ]
    fields = ("tool", "case_id", "property")
    output: list[dict[str, Any]] = []
    for key, group in sorted(group_rows(selected, fields).items()):
        detected_runs = sum(
            row["logical_outcome"] == "counterexample" for row in group
        )
        completed = sum(row["status"] == "completed" for row in group)
        output.append(
            {
                **dict(zip(fields, key, strict=True)),
                "measured_runs": len(group),
                "completed_runs": completed,
                "detected_runs": detected_runs,
                "detected": detected_runs > 0,
                "detection_consistent": completed > 0 and detected_runs == completed,
                "timeouts": sum(row["status"] == "timeout" for row in group),
                "out_of_memory": sum(
                    row["status"] == "out_of_memory" for row in group
                ),
            }
        )
    return output


def build_table_04(rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    selected = [
        row
        for row in rows
        if row["is_measured"] and "RQ3" in row["rq_ids"].split(";")
    ]
    fields = ("case_type", "case_id")
    output: list[dict[str, Any]] = []
    for key, group in sorted(group_rows(selected, fields).items()):
        case_type = str(key[0])
        successful = sum(
            row["accepted"] is True
            if case_type == "valid"
            else row["accepted"] is False
            for row in group
        )
        diagnostic_matches = sum(
            row["diagnostic_matches"] is True for row in group
        )
        lower, upper = wilson_interval(successful, len(group))
        output.append(
            {
                **dict(zip(fields, key, strict=True)),
                "runs": len(group),
                "successful_classification": successful,
                "proportion": successful / len(group) if group else None,
                "wilson_95_lower": lower,
                "wilson_95_upper": upper,
                "diagnostic_matches": diagnostic_matches,
                "completed": sum(row["status"] == "completed" for row in group),
            }
        )
    return output


def profile_metrics(
    rows: list[dict[str, Any]],
    experiment_spec: dict[str, Any],
    bootstrap_resamples: int,
) -> list[dict[str, Any]]:
    selected = [
        row
        for row in rows
        if row["is_measured"]
        and row["bound_profile"] in PROFILE_ORDER
        and row["model_kind"] == "valid"
        and row["fault_profile"] == "normal"
        and str(row["configuration_id"]).startswith("RQ1R4-")
        and "RQ4" in row["rq_ids"].split(";")
    ]
    profiles = experiment_spec.get("bound_profiles", {})
    output: list[dict[str, Any]] = []
    for key, group in sorted(group_rows(selected, ("tool", "bound_profile")).items()):
        tool, profile = key
        profile_values = profiles.get(profile, {})
        row: dict[str, Any] = {
            "tool": tool,
            "bound_profile": profile,
            "profile_order": PROFILE_ORDER.get(str(profile)),
            "shards": profile_values.get("shards"),
            "concurrent_transfers": profile_values.get("concurrent_transfers"),
            "validators": profile_values.get("validators"),
            "measured_runs": len(group),
            "timeouts": sum(item["status"] == "timeout" for item in group),
            "out_of_memory": sum(
                item["status"] == "out_of_memory" for item in group
            ),
        }
        for metric in (
            "elapsed_seconds",
            "max_rss_kb",
            "states_generated",
            "distinct_states",
        ):
            summary = summarize_numeric(
                [
                    item.get(metric)
                    for item in group
                    if item["numeric_observation"]
                ],
                resamples=bootstrap_resamples,
                key=f"profile|{tool}|{profile}|{metric}",
            )
            row[f"{metric}_n"] = summary["n"]
            row[f"{metric}_median"] = summary["median"]
            row[f"{metric}_iqr"] = summary["iqr"]
            row[f"{metric}_bootstrap_95_lower"] = summary[
                "bootstrap_95_lower"
            ]
            row[f"{metric}_bootstrap_95_upper"] = summary[
                "bootstrap_95_upper"
            ]
        output.append(row)
    return output


def build_table_06(
    rows: list[dict[str, Any]],
    bootstrap_resamples: int,
) -> list[dict[str, Any]]:
    selected = [
        row
        for row in rows
        if row["is_measured"]
        and str(row["configuration_id"]).startswith("RQ4-TLC-FAULT-")
    ]
    fields = ("fault_profile", "case_id")
    output: list[dict[str, Any]] = []
    for key, group in sorted(group_rows(selected, fields).items()):
        elapsed = summarize_numeric(
            [
                row["elapsed_seconds"]
                for row in group
                if row["numeric_observation"]
            ],
            resamples=bootstrap_resamples,
            key=f"fault|{'|'.join(str(value) for value in key)}|elapsed",
        )
        memory = summarize_numeric(
            [row["max_rss_kb"] for row in group if row["numeric_observation"]],
            resamples=bootstrap_resamples,
            key=f"fault|{'|'.join(str(value) for value in key)}|memory",
        )
        counts = status_counts(group)
        output.append(
            {
                **dict(zip(fields, key, strict=True)),
                "measured_runs": len(group),
                "elapsed_median": elapsed["median"],
                "elapsed_iqr": elapsed["iqr"],
                "memory_median_kb": memory["median"],
                "memory_iqr_kb": memory["iqr"],
                "completed": counts["completed"],
                "timeout": counts["timeout"],
                "out_of_memory": counts["out_of_memory"],
                "tool_error": counts["tool_error"],
            }
        )
    return output


def build_table_07(rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    selected = [
        row
        for row in rows
        if row["is_measured"] and row["status"] != "completed"
    ]
    fields = ("tool", "configuration_id", "status")
    output: list[dict[str, Any]] = []
    for key, group in sorted(group_rows(selected, fields).items()):
        output.append(
            {
                **dict(zip(fields, key, strict=True)),
                "runs": len(group),
                "case_ids": ";".join(
                    sorted({str(row["case_id"]) for row in group})
                ),
            }
        )
    return output


def build_table_08(analysis_spec: dict[str, Any]) -> list[dict[str, Any]]:
    threats = analysis_spec.get("validity_threats")
    if not isinstance(threats, list):
        raise SystemExit("analysis-spec.json no declara amenazas a la validez.")
    rows: list[dict[str, Any]] = []
    for item in threats:
        if not isinstance(item, dict):
            raise SystemExit("Existe una amenaza a la validez inválida.")
        rows.append(
            {
                "category": item.get("category"),
                "threat": item.get("threat"),
                "mitigation": item.get("mitigation"),
                "residual_risk": item.get("residual_risk"),
            }
        )
    return rows


def build_associations(
    profile_rows: list[dict[str, Any]],
) -> list[dict[str, Any]]:
    output: list[dict[str, Any]] = []
    for tool, group in sorted(group_rows(profile_rows, ("tool",)).items()):
        ordered = sorted(group, key=lambda row: row["profile_order"])
        levels = [row["profile_order"] for row in ordered]
        for metric in (
            "elapsed_seconds_median",
            "max_rss_kb_median",
            "states_generated_median",
            "distinct_states_median",
        ):
            pairs = [
                (level, row.get(metric))
                for level, row in zip(levels, ordered, strict=True)
                if row.get(metric) is not None
            ]
            coefficient = None
            if len(pairs) >= 2:
                coefficient = spearman_correlation(
                    [pair[0] for pair in pairs],
                    [pair[1] for pair in pairs],
                )
            growth_small_large = None
            available = {
                str(row["bound_profile"]): row.get(metric)
                for row in ordered
                if row.get(metric) is not None
            }
            small = available.get("small")
            large = available.get("large")
            if isinstance(small, (int, float)) and small != 0 and isinstance(
                large, (int, float)
            ):
                growth_small_large = large / small
            output.append(
                {
                    "tool": tool[0],
                    "metric": metric,
                    "profiles_observed": len(pairs),
                    "spearman": coefficient,
                    "large_to_small_ratio": growth_small_large,
                }
            )
    return output


def build_findings(
    rows: list[dict[str, Any]],
    table_03: list[dict[str, Any]],
    table_04: list[dict[str, Any]],
    associations: list[dict[str, Any]],
) -> dict[str, Any]:
    measured = [row for row in rows if row["is_measured"]]
    rq1 = [row for row in measured if "RQ1" in row["rq_ids"].split(";")]
    rq2 = [row for row in measured if "RQ2" in row["rq_ids"].split(";")]
    rq3 = [row for row in measured if "RQ3" in row["rq_ids"].split(";")]
    rq4 = [row for row in measured if "RQ4" in row["rq_ids"].split(";")]

    rq1_violations = sum(
        row["logical_outcome"] == "counterexample" for row in rq1
    )
    rq1_incomplete = sum(row["status"] != "completed" for row in rq1)
    rq1_unclassified = sum(
        row["status"] == "completed"
        and row["logical_outcome"] not in {"passed", "counterexample"}
        for row in rq1
    )
    rq1_incomplete += rq1_unclassified
    h1_status = "respaldada_en_bounds_completados"
    if rq1_violations:
        h1_status = "no_respaldada"
    elif rq1_incomplete:
        h1_status = "respaldada_parcialmente_con_censura"

    mutant_total = len(table_03)
    mutant_detected = sum(bool(row["detected"]) for row in table_03)
    mutant_consistent = sum(
        bool(row["detection_consistent"]) for row in table_03
    )
    mutation_score = mutant_detected / mutant_total if mutant_total else None
    h2_status = (
        "respaldada"
        if mutant_total > 0
        and mutant_detected == mutant_total
        and mutant_consistent == mutant_total
        else "no_respaldada"
        if mutant_total > 0
        else "datos_insuficientes"
    )

    valid_rows = [row for row in rq3 if row["case_type"] == "valid"]
    negative_rows = [row for row in rq3 if row["case_type"] == "negative"]
    valid_accepted = sum(row["accepted"] is True for row in valid_rows)
    negative_rejected = sum(row["accepted"] is False for row in negative_rows)
    diagnostics_matching = sum(
        row["diagnostic_matches"] is True for row in negative_rows
    )
    h3_status = (
        "respaldada"
        if valid_rows
        and negative_rows
        and valid_accepted == len(valid_rows)
        and negative_rejected == len(negative_rows)
        and diagnostics_matching == len(negative_rows)
        else "no_respaldada"
        if valid_rows or negative_rows
        else "datos_insuficientes"
    )

    positive_time_associations = [
        row
        for row in associations
        if row.get("metric") == "elapsed_seconds_median"
        and row.get("profiles_observed") == 3
        and isinstance(row.get("spearman"), (int, float))
        and row["spearman"] > 0
    ]
    h4_status = (
        "evidencia_de_crecimiento_sin_prueba_de_no_linealidad"
        if positive_time_associations
        else "crecimiento_no_establecido"
    )

    return {
        "schema_version": 1,
        "phase": "8D",
        "protocol_id": PROTOCOL_ID,
        "research_questions": {
            "RQ1": {
                "hypothesis": "H1",
                "status": h1_status,
                "measured_runs": len(rq1),
                "completed_runs": sum(row["status"] == "completed" for row in rq1),
                "property_violations": rq1_violations,
                "censored_or_error_runs": rq1_incomplete,
                "interpretation": (
                    "La afirmación se limita a las configuraciones completadas y a los bounds documentados."
                ),
            },
            "RQ2": {
                "hypothesis": "H2",
                "status": h2_status,
                "measured_runs": len(rq2),
                "mutants_total": mutant_total,
                "mutants_detected": mutant_detected,
                "mutants_with_consistent_detection": mutant_consistent,
                "mutation_score": mutation_score,
                "interpretation": (
                    "La detección se evalúa por mutante y propiedad objetivo, sin comparar tiempos absolutos entre herramientas."
                ),
            },
            "RQ3": {
                "hypothesis": "H3",
                "status": h3_status,
                "measured_runs": len(rq3),
                "valid_total": len(valid_rows),
                "valid_accepted": valid_accepted,
                "negative_total": len(negative_rows),
                "negative_rejected": negative_rejected,
                "diagnostics_matching": diagnostics_matching,
                "interpretation": (
                    "La conclusión corresponde a conformidad acotada de trazas entre implementación y modelo."
                ),
            },
            "RQ4": {
                "hypothesis": "H4",
                "status": h4_status,
                "measured_runs": len(rq4),
                "associations": associations,
                "interpretation": (
                    "Las tendencias se analizan dentro de cada herramienta y no establecen superioridad absoluta entre TLC y Alloy."
                ),
            },
        },
        "conformance_by_case": table_04,
    }


def build_findings_markdown(findings: dict[str, Any]) -> str:
    questions = findings["research_questions"]
    lines = [
        "### Respuestas reproducibles a RQ1, RQ2, RQ3 y RQ4",
        "",
        "#### Alcance",
        "",
        "Las respuestas se derivan exclusivamente de la ejecución raw validada. No constituyen una prueba fuera de los bounds, configuraciones y herramientas documentadas.",
        "",
    ]
    for rq_id in ("RQ1", "RQ2", "RQ3", "RQ4"):
        value = questions[rq_id]
        lines.extend(
            [
                f"#### {rq_id}",
                "",
                f"Estado de {value['hypothesis']}: `{value['status']}`.",
                "",
                value["interpretation"],
                "",
                "```json",
                json.dumps(value, ensure_ascii=False, indent=2, sort_keys=True),
                "```",
                "",
            ]
        )
    return "\n".join(lines)


def median_by_profile(
    profile_rows: list[dict[str, Any]],
    metric: str,
) -> dict[str, list[tuple[float, float]]]:
    output: dict[str, list[tuple[float, float]]] = defaultdict(list)
    for row in profile_rows:
        x_value = row.get("concurrent_transfers")
        y_value = row.get(metric)
        if isinstance(x_value, (int, float)) and isinstance(y_value, (int, float)):
            output[str(row["tool"]).upper()].append((float(x_value), float(y_value)))
    return dict(output)


def memory_by_shards(
    profile_rows: list[dict[str, Any]],
) -> dict[str, list[tuple[float, float]]]:
    output: dict[str, list[tuple[float, float]]] = defaultdict(list)
    for row in profile_rows:
        x_value = row.get("shards")
        y_value = row.get("max_rss_kb_median")
        if isinstance(x_value, (int, float)) and isinstance(y_value, (int, float)):
            output[str(row["tool"]).upper()].append(
                (float(x_value), float(y_value) / 1024.0)
            )
    return dict(output)


def relative_cost_values(profile_rows: list[dict[str, Any]]) -> list[tuple[str, float]]:
    grouped = group_rows(profile_rows, ("tool",))
    values: list[tuple[str, float]] = []
    for tool_key, rows in sorted(grouped.items()):
        by_profile = {
            str(row["bound_profile"]): row.get("elapsed_seconds_median")
            for row in rows
        }
        baseline = by_profile.get("small")
        if not isinstance(baseline, (int, float)) or baseline == 0:
            continue
        for profile in ("small", "medium", "large"):
            value = by_profile.get(profile)
            if isinstance(value, (int, float)):
                values.append((f"{tool_key[0].upper()} {profile}", value / baseline))
    return values


def mutant_time_values(
    rows: list[dict[str, Any]],
    bootstrap_resamples: int,
) -> list[tuple[str, float]]:
    selected = [
        row
        for row in rows
        if row["is_measured"]
        and "RQ2" in row["rq_ids"].split(";")
        and row["logical_outcome"] == "counterexample"
    ]
    output: list[tuple[str, float]] = []
    for key, group in sorted(group_rows(selected, ("tool", "case_id")).items()):
        summary = summarize_numeric(
            [row["elapsed_seconds"] for row in group],
            resamples=bootstrap_resamples,
            key=f"mutant-time|{key[0]}|{key[1]}",
        )
        if isinstance(summary["median"], (int, float)):
            output.append((f"{str(key[0]).upper()} {key[1]}", summary["median"]))
    return output


def conformance_values(table_04: list[dict[str, Any]]) -> list[tuple[str, float]]:
    valid = [row for row in table_04 if row["case_type"] == "valid"]
    negative = [row for row in table_04 if row["case_type"] == "negative"]
    valid_total = sum(int(row["runs"]) for row in valid)
    valid_success = sum(int(row["successful_classification"]) for row in valid)
    negative_total = sum(int(row["runs"]) for row in negative)
    negative_success = sum(int(row["successful_classification"]) for row in negative)
    diagnostics = sum(int(row["diagnostic_matches"]) for row in negative)
    values: list[tuple[str, float]] = []
    if valid_total:
        values.append(("Trazas válidas aceptadas", valid_success / valid_total))
    if negative_total:
        values.append(("Trazas negativas rechazadas", negative_success / negative_total))
        values.append(("Diagnósticos coincidentes", diagnostics / negative_total))
    return values


def write_tables(
    tables_dir: Path,
    tables: dict[str, tuple[str, list[dict[str, Any]], list[tuple[str, str]]]],
) -> None:
    for identifier, (title, rows, fields) in tables.items():
        write_csv_atomic(
            tables_dir / f"{identifier}.csv",
            rows,
            [field for field, _ in fields],
        )
        write_markdown_table(
            tables_dir / f"{identifier}.md",
            title,
            rows,
            fields,
        )


def write_figures(
    figures_dir: Path,
    rows: list[dict[str, Any]],
    profile_rows: list[dict[str, Any]],
    table_04: list[dict[str, Any]],
    bootstrap_resamples: int,
) -> None:
    render_architecture(figures_dir / "figure-01-evidence-architecture.svg")
    render_flow(figures_dir / "figure-02-experimental-flow.svg")
    render_line_chart(
        figures_dir / "figure-03-distinct-states-vs-transfers.svg",
        "Estados distintos frente a transferencias concurrentes",
        median_by_profile(profile_rows, "distinct_states_median"),
        x_label="Transferencias concurrentes",
        y_label="Mediana de estados distintos",
    )
    render_line_chart(
        figures_dir / "figure-04-time-vs-transfers.svg",
        "Tiempo frente a transferencias concurrentes",
        median_by_profile(profile_rows, "elapsed_seconds_median"),
        x_label="Transferencias concurrentes",
        y_label="Mediana de tiempo en segundos",
    )
    render_line_chart(
        figures_dir / "figure-05-memory-vs-shards.svg",
        "Memoria frente a shards",
        memory_by_shards(profile_rows),
        x_label="Shards",
        y_label="Mediana de memoria en MiB",
    )
    render_bar_chart(
        figures_dir / "figure-06-relative-cost-by-profile.svg",
        "Costo relativo por perfil",
        relative_cost_values(profile_rows),
        y_label="Razón respecto al perfil pequeño",
    )
    render_bar_chart(
        figures_dir / "figure-07-counterexample-time-by-mutant.svg",
        "Tiempo hasta contraejemplo por mutante",
        mutant_time_values(rows, bootstrap_resamples),
        y_label="Mediana de tiempo en segundos",
    )
    render_bar_chart(
        figures_dir / "figure-08-multiseed-conformance.svg",
        "Aceptación y rechazo multiseed",
        conformance_values(table_04),
        y_label="Proporción",
    )


def promote_directory(staging: Path, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    backup = destination.parent / f".{destination.name}.previous"
    if backup.exists():
        shutil.rmtree(backup)
    if destination.exists():
        destination.replace(backup)
    try:
        staging.replace(destination)
    except Exception:
        if backup.exists() and not destination.exists():
            backup.replace(destination)
        raise
    if backup.exists():
        shutil.rmtree(backup)


def list_output_files(root: Path) -> list[Path]:
    return sorted(path for path in root.rglob("*") if path.is_file())


def build_output_manifest(
    *,
    run_dir: Path,
    raw_manifest: dict[str, Any],
    experiment_spec_path: Path,
    analysis_spec_path: Path,
    derived_stage: Path,
    tables_stage: Path,
    figures_stage: Path,
    row_count: int,
    measured_count: int,
    statistics_count: int,
) -> dict[str, Any]:
    outputs: list[dict[str, Any]] = []
    for category, root in (
        ("derived", derived_stage),
        ("tables", tables_stage),
        ("figures", figures_stage),
    ):
        for path in list_output_files(root):
            if category == "derived" and path.name == "derived-manifest.json":
                continue
            outputs.append(
                {
                    "category": category,
                    "path": str(path.relative_to(root)),
                    "sha256": calculate_sha256_file(path),
                }
            )
    return {
        "schema_version": 1,
        "phase": "8D",
        "protocol_id": PROTOCOL_ID,
        "status": "complete",
        "source_run_id": run_dir.name,
        "source_completed_at_utc": raw_manifest.get("completed_at_utc"),
        "input_sha256": {
            "raw_manifest": calculate_sha256_file(run_dir / "raw-manifest.json"),
            "plan": calculate_sha256_file(run_dir / "snapshots/plan.jsonl"),
            "plan_manifest": calculate_sha256_file(
                run_dir / "snapshots/plan-manifest.json"
            ),
            "environment": calculate_sha256_file(run_dir / "environment.json"),
            "provenance": calculate_sha256_file(run_dir / "provenance.json"),
            "experiment_spec": calculate_sha256_file(experiment_spec_path),
            "analysis_spec": calculate_sha256_file(analysis_spec_path),
            "analysis_script": calculate_sha256_file(Path(__file__)),
            "statistics_module": calculate_sha256_file(
                SCRIPT_DIR / "analysis_statistics.py"
            ),
            "svg_module": calculate_sha256_file(SCRIPT_DIR / "analysis_svg.py"),
        },
        "counts": {
            "task_rows": row_count,
            "measured_rows": measured_count,
            "statistics_rows": statistics_count,
            "tables": 8,
            "figures": 8,
            "output_files": len(outputs),
        },
        "raw_results_modified": False,
        "outputs": outputs,
    }


def main() -> None:
    args = parse_args()
    run_dir = Path(args.run_dir).resolve()
    experiment_spec_path = Path(args.experiment_spec).resolve()
    analysis_spec_path = Path(args.analysis_spec).resolve()
    derived_dir = Path(args.derived_dir).resolve()
    tables_dir = Path(args.tables_dir).resolve()
    figures_dir = Path(args.figures_dir).resolve()

    for output_dir, field_name in (
        (derived_dir, "derived-dir"),
        (tables_dir, "tables-dir"),
        (figures_dir, "figures-dir"),
    ):
        require_outside_raw(run_dir, output_dir, field_name)
    output_dirs = (derived_dir, tables_dir, figures_dir)
    if len(set(output_dirs)) != 3:
        raise SystemExit("Los directorios de salida deben ser diferentes.")
    for index, left in enumerate(output_dirs):
        for right in output_dirs[index + 1 :]:
            try:
                left.relative_to(right)
                raise SystemExit("Los directorios de salida no pueden anidarse.")
            except ValueError:
                pass
            try:
                right.relative_to(left)
                raise SystemExit("Los directorios de salida no pueden anidarse.")
            except ValueError:
                pass

    require_file(experiment_spec_path)
    require_file(analysis_spec_path)
    experiment_spec = load_json(experiment_spec_path)
    analysis_spec = load_json(analysis_spec_path)
    if experiment_spec.get("protocol_id") != PROTOCOL_ID:
        raise SystemExit("experiment-spec.json no coincide con el protocolo.")
    if experiment_spec.get("status") != "frozen":
        raise SystemExit("El protocolo experimental debe permanecer congelado.")
    if analysis_spec.get("phase") != "8D":
        raise SystemExit("analysis-spec.json debe declarar Fase 8D.")
    if analysis_spec.get("protocol_id") != PROTOCOL_ID:
        raise SystemExit("analysis-spec.json no coincide con el protocolo.")
    bootstrap_resamples = analysis_spec.get("bootstrap_resamples")
    if not isinstance(bootstrap_resamples, int) or bootstrap_resamples <= 0:
        raise SystemExit("analysis-spec.json declara remuestras inválidas.")

    raw_manifest, _, environment, tasks, results = load_raw_context(
        run_dir,
        args.expect_tasks,
    )
    task_by_id = {str(task["task_id"]): task for task in tasks}
    rows = [
        normalize_result(task_by_id[str(result["task_id"])], result)
        for result in results
    ]
    measured_rows = [row for row in rows if row["is_measured"]]
    exclusions = [row for row in rows if row["exclusion_reason"]]
    statistics = build_statistics(rows, bootstrap_resamples)

    table_01 = build_table_01(run_dir, environment)
    table_02 = build_table_02(rows)
    table_03 = build_table_03(rows)
    table_04 = build_table_04(rows)
    table_05 = profile_metrics(rows, experiment_spec, bootstrap_resamples)
    table_06 = build_table_06(rows, bootstrap_resamples)
    table_07 = build_table_07(rows)
    table_08 = build_table_08(analysis_spec)
    associations = build_associations(table_05)
    findings = build_findings(rows, table_03, table_04, associations)

    staging_parent = derived_dir.parent
    staging_parent.mkdir(parents=True, exist_ok=True)
    staging_root = Path(
        tempfile.mkdtemp(prefix=".paper1-analysis-", dir=staging_parent)
    )
    derived_stage = staging_root / "derived"
    tables_stage = staging_root / "tables"
    figures_stage = staging_root / "figures"
    derived_stage.mkdir()
    tables_stage.mkdir()
    figures_stage.mkdir()

    try:
        write_csv_atomic(
            derived_stage / "task-results.csv",
            rows,
            DATASET_FIELDS,
        )
        write_csv_atomic(
            derived_stage / "measured-results.csv",
            measured_rows,
            DATASET_FIELDS,
        )
        write_csv_atomic(
            derived_stage / "exclusions.csv",
            exclusions,
            DATASET_FIELDS,
        )
        statistics_fields = [
            "configuration_id",
            "rq_ids",
            "tool",
            "model_kind",
            "case_type",
            "case_id",
            "property",
            "bound_profile",
            "fault_profile",
            "metric",
            "n_total",
            "n_observed",
            "n_censored",
            "n_tool_error",
            "median",
            "q1",
            "q3",
            "iqr",
            "minimum",
            "maximum",
            "mean",
            "coefficient_of_variation",
            "bootstrap_95_lower",
            "bootstrap_95_upper",
        ]
        write_csv_atomic(
            derived_stage / "statistics.csv",
            statistics,
            statistics_fields,
        )
        write_json_atomic(derived_stage / "rq-findings.json", findings)
        write_text_atomic(
            derived_stage / "rq-findings.md",
            build_findings_markdown(findings),
        )
        write_json_atomic(
            derived_stage / "analysis-summary.json",
            {
                "schema_version": 1,
                "phase": "8D",
                "protocol_id": PROTOCOL_ID,
                "tasks_total": len(rows),
                "measured_total": len(measured_rows),
                "warmup_total": sum(not row["is_measured"] for row in rows),
                "censored_total": sum(row["is_censored"] for row in rows),
                "tool_error_total": sum(
                    row["status"] == "tool_error" for row in rows
                ),
                "by_status": {
                    status: sum(row["status"] == status for row in rows)
                    for status in sorted(TERMINAL_STATUSES)
                },
                "bootstrap_resamples": bootstrap_resamples,
                "rq_findings_path": "rq-findings.json",
            },
        )

        tables = {
            "table-01-tools-hardware": (
                "Tabla 1. Herramientas, versiones y hardware",
                table_01,
                [("source", "Fuente"), ("field", "Campo"), ("value", "Valor")],
            ),
            "table-02-properties-results": (
                "Tabla 2. Propiedades y resultados por herramienta",
                table_02,
                [
                    ("tool", "Herramienta"),
                    ("property", "Propiedad"),
                    ("bound_profile", "Perfil"),
                    ("measured_runs", "Mediciones"),
                    ("completed", "Completadas"),
                    ("passed", "Sin contraejemplo"),
                    ("counterexample", "Contraejemplo"),
                    ("timeout", "Timeout"),
                    ("out_of_memory", "Sin memoria"),
                    ("tool_error", "Error"),
                ],
            ),
            "table-03-mutants-detection": (
                "Tabla 3. Mutantes, propiedad objetivo y detección",
                table_03,
                [
                    ("tool", "Herramienta"),
                    ("case_id", "Mutante"),
                    ("property", "Propiedad objetivo"),
                    ("measured_runs", "Mediciones"),
                    ("detected_runs", "Detecciones"),
                    ("detected", "Detectado"),
                    ("detection_consistent", "Consistencia"),
                ],
            ),
            "table-04-multiseed-conformance": (
                "Tabla 4. Conformidad multiseed",
                table_04,
                [
                    ("case_type", "Tipo"),
                    ("case_id", "Caso"),
                    ("runs", "Seeds"),
                    ("successful_classification", "Clasificaciones correctas"),
                    ("proportion", "Proporción"),
                    ("wilson_95_lower", "Wilson inferior"),
                    ("wilson_95_upper", "Wilson superior"),
                    ("diagnostic_matches", "Diagnósticos coincidentes"),
                ],
            ),
            "table-05-cost-by-bound-profile": (
                "Tabla 5. Costo por perfil de bound",
                table_05,
                [
                    ("tool", "Herramienta"),
                    ("bound_profile", "Perfil"),
                    ("shards", "Shards"),
                    ("concurrent_transfers", "Transferencias"),
                    ("measured_runs", "Mediciones"),
                    ("elapsed_seconds_median", "Tiempo mediano"),
                    ("max_rss_kb_median", "Memoria mediana KiB"),
                    ("distinct_states_median", "Estados distintos medianos"),
                    ("timeouts", "Timeouts"),
                    ("out_of_memory", "Sin memoria"),
                ],
            ),
            "table-06-tlc-fault-cost": (
                "Tabla 6. Fallos habilitados y costo TLC",
                table_06,
                [
                    ("fault_profile", "Perfil de fallo"),
                    ("case_id", "Caso"),
                    ("measured_runs", "Mediciones"),
                    ("elapsed_median", "Tiempo mediano"),
                    ("memory_median_kb", "Memoria mediana KiB"),
                    ("completed", "Completadas"),
                    ("timeout", "Timeout"),
                    ("out_of_memory", "Sin memoria"),
                    ("tool_error", "Error"),
                ],
            ),
            "table-07-incomplete-runs": (
                "Tabla 7. Ejecuciones incompletas",
                table_07,
                [
                    ("tool", "Herramienta"),
                    ("configuration_id", "Configuración"),
                    ("status", "Estado"),
                    ("runs", "Ejecuciones"),
                    ("case_ids", "Casos"),
                ],
            ),
            "table-08-validity-threats": (
                "Tabla 8. Amenazas a la validez y mitigaciones",
                table_08,
                [
                    ("category", "Categoría"),
                    ("threat", "Amenaza"),
                    ("mitigation", "Mitigación"),
                    ("residual_risk", "Riesgo residual"),
                ],
            ),
        }
        write_tables(tables_stage, tables)
        write_figures(
            figures_stage,
            rows,
            table_05,
            table_04,
            bootstrap_resamples,
        )

        manifest = build_output_manifest(
            run_dir=run_dir,
            raw_manifest=raw_manifest,
            experiment_spec_path=experiment_spec_path,
            analysis_spec_path=analysis_spec_path,
            derived_stage=derived_stage,
            tables_stage=tables_stage,
            figures_stage=figures_stage,
            row_count=len(rows),
            measured_count=len(measured_rows),
            statistics_count=len(statistics),
        )
        write_json_atomic(derived_stage / "derived-manifest.json", manifest)

        promote_directory(derived_stage, derived_dir)
        promote_directory(tables_stage, tables_dir)
        promote_directory(figures_stage, figures_dir)
    finally:
        if staging_root.exists():
            shutil.rmtree(staging_root)

    print("El análisis de Fase 8D terminó correctamente.")
    print(f"Resultados derivados: {derived_dir}.")
    print(f"Tablas: {tables_dir}.")
    print(f"Figuras: {figures_dir}.")


if __name__ == "__main__":
    main()
