#!/usr/bin/env python3
"""Ejecuta una tarea científica real de la matriz del Paper 1."""

from __future__ import annotations

import argparse
import csv
import json
import os
import re
import subprocess
import sys
from pathlib import Path
from typing import Any

SCRIPT_DIR = Path(__file__).resolve().parent
ROOT_DIR = SCRIPT_DIR.parent.parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

from experiment_io import (  # noqa: E402
    load_json,
    require_relative_path,
    write_json_atomic,
    write_text_atomic,
)

PROTOCOL_ID = "paper1-q3-v1"
TLA_HEADER = (
    "run_id,kind,model,property,result,expected_outcome,tool_exit_code,"
    "states_generated,distinct_states,depth,elapsed_seconds,max_memory_kb,"
    "tool_version,stdout_path,stderr_path\n"
)
ALLOY_HEADER = (
    "run_id,kind,model,property,expected_property,command_type,result,"
    "expected_outcome,tool_exit_code,solver,scope,counterexamples,"
    "solve_duration_ms,elapsed_seconds,max_memory_kb,states_generated,"
    "distinct_states,depth,receipt_path\n"
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Ejecuta una tarea científica real de la Fase 8C."
    )
    parser.add_argument("--task", required=True)
    parser.add_argument("--output-dir", required=True)
    parser.add_argument("--validate-only", action="store_true")
    return parser.parse_args()


def load_profiles() -> dict[str, Any]:
    return load_json(ROOT_DIR / "experiments/paper1/execution-profiles.json")


def require_task(task: dict[str, Any]) -> None:
    if task.get("protocol_id") != PROTOCOL_ID:
        raise SystemExit("La tarea no coincide con el protocolo congelado.")
    if task.get("tool") not in {"tlc", "alloy"}:
        raise SystemExit("La tarea declara una herramienta desconocida.")
    if task.get("case_type") not in {"formal", "valid", "negative"}:
        raise SystemExit("La tarea declara un tipo de caso desconocido.")
    if task.get("repetition_kind") not in {"warmup", "measured"}:
        raise SystemExit("La tarea declara un tipo de repetición desconocido.")
    if not isinstance(task.get("timeout_seconds"), int):
        raise SystemExit("La tarea no declara un timeout entero.")
    if not isinstance(task.get("max_rss_mb"), int):
        raise SystemExit("La tarea no declara memoria máxima entera.")


def read_shell_values(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    pattern = re.compile(r'^([A-Z0-9_]+)="([^"]*)"$')
    for line in path.read_text(encoding="utf-8").splitlines():
        match = pattern.match(line.strip())
        if match:
            values[match.group(1)] = match.group(2)
    return values


def tool_paths() -> dict[str, Path]:
    values = read_shell_values(ROOT_DIR / "scripts/formal/tool_versions.env")
    tools_root = ROOT_DIR / values.get("FORMAL_TOOLS_DIR", ".formal-tools")
    tla_version = values.get("TLA_TOOLS_VERSION", "1.7.4")
    alloy_version = values.get("ALLOY_VERSION", "6.2.0")
    return {
        "tlc": tools_root / "tla" / tla_version / "tla2tools.jar",
        "alloy": (
            tools_root
            / "alloy"
            / alloy_version
            / f"org.alloytools.alloy.dist-{alloy_version}.jar"
        ),
    }


def set_java_memory(environment: dict[str, str], max_rss_mb: int) -> None:
    current = environment.get("JAVA_TOOL_OPTIONS", "").strip()
    option = f"-Xmx{max_rss_mb}m"
    environment["JAVA_TOOL_OPTIONS"] = (
        f"{current} {option}".strip()
    )


def run_checked(
    command: list[str],
    *,
    cwd: Path,
    environment: dict[str, str],
) -> None:
    completed = subprocess.run(
        command,
        cwd=cwd,
        env=environment,
        check=False,
    )
    if completed.returncode != 0:
        raise SystemExit(
            "El launcher científico no cumplió la expectativa de la tarea."
        )


def set_literal(prefix: str, count: int) -> str:
    return "{" + ", ".join(f"{prefix}{index}" for index in range(1, count + 1)) + "}"


def delayed_literal(values: list[int]) -> str:
    if not values:
        return "{}"
    return "{" + ", ".join(str(value) for value in values) + "}"


def resolve_tlc_profile(
    task: dict[str, Any],
    profiles: dict[str, Any],
) -> tuple[dict[str, Any], str]:
    bound_profile = str(task["bound_profile"])
    if task["configuration_id"].startswith("RQ4-TLC-FAULT-"):
        fault_profile = str(task["fault_profile"])
        fault = profiles["fault_profiles"].get(fault_profile)
        if not isinstance(fault, dict):
            raise SystemExit(f"Perfil de fallo desconocido: {fault_profile}.")
        base = dict(profiles["profiles"][fault["base_profile"]])
        base["receipt_copies"] = fault["receipt_copies"]
        base["delayed_copies"] = fault["delayed_copies"]
        base["enable_timeout"] = fault["enable_timeout"]
        base["quorum"] = fault["quorum"]
        return base, str(fault["property"])

    profile = profiles["profiles"].get(bound_profile)
    if not isinstance(profile, dict):
        raise SystemExit(f"Perfil de bound desconocido: {bound_profile}.")
    value = dict(profile)
    value["delayed_copies"] = []
    value["enable_timeout"] = False
    property_name = task.get("expected_property")
    if not isinstance(property_name, str) or not property_name:
        raise SystemExit("La tarea TLC válida necesita una propiedad.")
    return value, property_name


def render_tlc_config(
    task: dict[str, Any],
    profiles: dict[str, Any],
) -> tuple[str, str]:
    profile, property_name = resolve_tlc_profile(task, profiles)
    enable_timeout = "TRUE" if profile["enable_timeout"] else "FALSE"
    lines = [
        "CONSTANTS",
        f"    Shards = {set_literal('s', int(profile['shards']))}",
        (
            "    Transfers = "
            f"{set_literal('t', int(profile['concurrent_transfers']))}"
        ),
        (
            "    Validators = "
            f"{set_literal('v', int(profile['validators']))}"
        ),
        f"    Quorum = {int(profile['quorum'])}",
        f"    ReceiptCopies = {int(profile['receipt_copies'])}",
        (
            "    DelayedCopies = "
            f"{delayed_literal(list(profile['delayed_copies']))}"
        ),
        f"    EnableTimeout = {enable_timeout}",
        "",
        "SPECIFICATION Spec",
        "",
        "INVARIANT TypeOK",
        f"INVARIANT {property_name}",
        "",
    ]
    return "\n".join(lines), property_name


def initialize_formal_result_dir(
    result_dir: Path,
    tool: str,
) -> None:
    result_dir.mkdir(parents=True, exist_ok=True)
    if tool == "tlc":
        write_text_atomic(result_dir / "tla_runs.csv", TLA_HEADER)
    else:
        write_text_atomic(result_dir / "alloy_runs.csv", ALLOY_HEADER)


def load_summary(result_dir: Path, task_id: str, tool: str) -> dict[str, Any]:
    suffix = "tlc" if tool == "tlc" else "alloy"
    path = result_dir / "logs" / f"{task_id}.{suffix}.summary.json"
    return load_json(path)


def run_tlc_task(
    task: dict[str, Any],
    output_dir: Path,
    profiles: dict[str, Any],
) -> dict[str, Any]:
    result_dir = output_dir / "formal"
    initialize_formal_result_dir(result_dir, "tlc")
    environment = os.environ.copy()
    environment["FORMAL_RESULTS_DIR"] = str(result_dir)
    set_java_memory(environment, int(task["max_rss_mb"]))

    if task["model_kind"] == "mutant":
        model = ROOT_DIR / require_relative_path(
            str(task["model"]),
            "model",
        )
        configuration = ROOT_DIR / require_relative_path(
            str(task["configuration"]),
            "configuration",
        )
        expectation = "failure"
        property_name = str(task["expected_property"])
    else:
        model = ROOT_DIR / profiles["base_models"]["tlc"]
        configuration = output_dir / "generated-task.cfg"
        config_text, property_name = render_tlc_config(task, profiles)
        write_text_atomic(configuration, config_text)
        expectation = "success"

    run_checked(
        [
            "bash",
            str(ROOT_DIR / "scripts/formal/run_tlc.sh"),
            str(task["task_id"]),
            str(task["model_kind"]),
            str(model),
            str(configuration),
            expectation,
        ],
        cwd=ROOT_DIR,
        environment=environment,
    )
    summary = load_summary(result_dir, str(task["task_id"]), "tlc")
    if summary.get("expectation_met") is not True:
        raise SystemExit("TLC no cumplió la expectativa declarada.")
    return {
        "tool": "TLC",
        "property": property_name,
        "logical_outcome": (
            "counterexample"
            if summary.get("actual_outcome") == "failure"
            else "passed"
        ),
        "summary": summary,
        "metrics": {
            "states_generated": summary.get("states_generated"),
            "distinct_states": summary.get("distinct_states"),
            "depth": summary.get("depth"),
            "tool_elapsed_seconds": summary.get("elapsed_seconds"),
            "tool_max_memory_kb": summary.get("max_memory_kb"),
            "violated_property": summary.get("violated_property"),
        },
    }


def alloy_module_name(task_id: str) -> str:
    clean = re.sub(r"[^A-Za-z0-9_]", "_", task_id)
    if clean and clean[0].isdigit():
        clean = "Task_" + clean
    return "Generated_" + clean


def render_alloy_model(
    source: Path,
    task: dict[str, Any],
    profiles: dict[str, Any],
) -> tuple[str, str, str]:
    profile = profiles["profiles"].get(str(task["bound_profile"]))
    if not isinstance(profile, dict):
        raise SystemExit("El perfil Alloy no existe.")
    property_name = task.get("expected_property")
    if not isinstance(property_name, str) or not property_name:
        raise SystemExit("La tarea Alloy necesita una propiedad.")

    module_name = alloy_module_name(str(task["task_id"]))
    text = source.read_text(encoding="utf-8")
    text = re.sub(
        r"(?m)^module\s+\S+",
        f"module {module_name}",
        text,
        count=1,
    )
    text = re.sub(r"(?m)^\s*check\s+[^\n]+\n?", "", text)

    expected = 1 if task["model_kind"] == "mutant" else 0
    profile_scope = (
        f"exactly {int(profile['alloy_state_scope'])} State, "
        f"exactly {int(profile['concurrent_transfers'])} Transfer, "
        f"exactly {int(profile['shards'])} Shard, "
        f"exactly {int(profile['validators'])} Validator, "
        f"exactly {int(profile['alloy_receipts'])} Receipt, "
        f"{int(profile['alloy_messages'])} Message"
    )
    text = (
        text.rstrip()
        + "\n\n"
        + f"check {property_name} for {profile_scope} expect {expected}\n"
    )
    return text, property_name, module_name


def run_alloy_task(
    task: dict[str, Any],
    output_dir: Path,
    profiles: dict[str, Any],
) -> dict[str, Any]:
    result_dir = output_dir / "formal"
    initialize_formal_result_dir(result_dir, "alloy")
    environment = os.environ.copy()
    environment["FORMAL_RESULTS_DIR"] = str(result_dir)
    set_java_memory(environment, int(task["max_rss_mb"]))

    if task["model_kind"] == "mutant":
        source = ROOT_DIR / require_relative_path(
            str(task["model"]),
            "model",
        )
        expectation = "failure"
    else:
        source = ROOT_DIR / profiles["base_models"]["alloy"]
        expectation = "success"

    model_text, property_name, module_name = render_alloy_model(
        source,
        task,
        profiles,
    )
    generated_model = output_dir / f"{module_name}.als"
    write_text_atomic(generated_model, model_text)

    run_checked(
        [
            "bash",
            str(ROOT_DIR / "scripts/formal/run_alloy.sh"),
            str(task["task_id"]),
            str(task["model_kind"]),
            str(generated_model),
            expectation,
            property_name if expectation == "failure" else "",
        ],
        cwd=ROOT_DIR,
        environment=environment,
    )
    summary = load_summary(result_dir, str(task["task_id"]), "alloy")
    if summary.get("expectation_met") is not True:
        raise SystemExit("Alloy no cumplió la expectativa declarada.")
    properties = summary.get("properties")
    first_property = (
        properties[0]
        if isinstance(properties, list) and properties
        else {}
    )
    return {
        "tool": "Alloy",
        "property": property_name,
        "logical_outcome": (
            "counterexample"
            if summary.get("actual_outcome") == "failure"
            else "passed"
        ),
        "summary": summary,
        "metrics": {
            "scope": first_property.get("scope"),
            "counterexamples": first_property.get("counterexamples"),
            "solve_duration_ms": first_property.get("solve_duration_ms"),
            "tool_elapsed_seconds": summary.get("elapsed_seconds"),
            "tool_max_memory_kb": summary.get("max_memory_kb"),
            "violated_properties": summary.get("violated_properties"),
        },
    }


def parse_time_metrics(path: Path) -> dict[str, Any]:
    text = path.read_text(encoding="utf-8", errors="replace")
    elapsed = None
    memory = None
    elapsed_match = re.search(
        r"Elapsed \(wall clock\) time \(h:mm:ss or m:ss\):\s*(\S+)",
        text,
    )
    if elapsed_match:
        parts = elapsed_match.group(1).split(":")
        if len(parts) == 2:
            elapsed = int(parts[0]) * 60 + float(parts[1])
        elif len(parts) == 3:
            elapsed = (
                int(parts[0]) * 3600
                + int(parts[1]) * 60
                + float(parts[2])
            )
        else:
            elapsed = float(parts[0])
    memory_match = re.search(
        r"Maximum resident set size \(kbytes\):\s*(\d+)",
        text,
    )
    if memory_match:
        memory = int(memory_match.group(1))
    return {
        "tool_elapsed_seconds": elapsed,
        "tool_max_memory_kb": memory,
    }


def run_conformance_task(
    task: dict[str, Any],
    output_dir: Path,
    profiles: dict[str, Any],
) -> dict[str, Any]:
    paths = tool_paths()
    classes_dir = ROOT_DIR / profiles["conformance"]["java_classes_dir"]
    runner_class = profiles["conformance"]["runner_class"]
    base_model = ROOT_DIR / profiles["conformance"]["tla_model"]
    case_output = output_dir / "conformance"
    case_output.mkdir(parents=True, exist_ok=True)
    stdout_path = case_output / "java.stdout.txt"
    stderr_path = case_output / "java.stderr.txt"
    time_path = case_output / "java.time.txt"
    environment = os.environ.copy()
    set_java_memory(environment, int(task["max_rss_mb"]))

    command = [
        "/usr/bin/time",
        "-v",
        "-o",
        str(time_path),
        "java",
        "-cp",
        str(classes_dir),
        str(runner_class),
        str(case_output),
        str(paths["tlc"]),
        str(base_model),
        str(task["case_type"]),
        str(task["case_id"]),
        str(task["seed"]),
    ]
    with stdout_path.open("w", encoding="utf-8", newline="\n") as stdout_stream, (
        stderr_path.open("w", encoding="utf-8", newline="\n")
    ) as stderr_stream:
        completed = subprocess.run(
            command,
            cwd=ROOT_DIR,
            env=environment,
            stdout=stdout_stream,
            stderr=stderr_stream,
            check=False,
        )
    case_result = load_json(case_output / "case-result.json")
    if completed.returncode != 0 or case_result.get("expectation_met") is not True:
        raise SystemExit("La conformidad no coincidió con su expectativa.")
    metrics = parse_time_metrics(time_path)
    metrics.update(
        {
            "accepted": case_result.get("accepted"),
            "diagnostic_matches": case_result.get("diagnostic_matches"),
            "checked_abstract_steps": case_result.get(
                "checked_abstract_steps"
            ),
            "rejected_abstract_step": case_result.get(
                "rejected_abstract_step"
            ),
            "rejected_concrete_step": case_result.get(
                "rejected_concrete_step"
            ),
            "rejected_action": case_result.get("rejected_action"),
            "transfer_id": case_result.get("transfer_id"),
        }
    )
    return {
        "tool": "TLC",
        "property": task.get("expected_property"),
        "logical_outcome": (
            "passed" if task["case_type"] == "valid" else "counterexample"
        ),
        "case_result": case_result,
        "metrics": metrics,
    }


def validate_task_mapping(
    task: dict[str, Any],
    output_dir: Path,
    profiles: dict[str, Any],
) -> None:
    if task["case_type"] in {"valid", "negative"}:
        if task["tool"] != "tlc" or task.get("seed") is None:
            raise SystemExit("RQ3 debe usar TLC y una seed explícita.")
        return
    if task["tool"] == "tlc":
        if task["model_kind"] == "valid":
            config_text, _ = render_tlc_config(task, profiles)
            write_text_atomic(output_dir / "validation-task.cfg", config_text)
        else:
            ROOT_DIR.joinpath(
                require_relative_path(str(task["model"]), "model")
            ).resolve(strict=True)
            ROOT_DIR.joinpath(
                require_relative_path(
                    str(task["configuration"]),
                    "configuration",
                )
            ).resolve(strict=True)
        return
    source = (
        ROOT_DIR
        / require_relative_path(str(task["model"]), "model")
        if task["model_kind"] == "mutant"
        else ROOT_DIR / profiles["base_models"]["alloy"]
    )
    model_text, _, module_name = render_alloy_model(
        source,
        task,
        profiles,
    )
    write_text_atomic(output_dir / f"{module_name}.als", model_text)


def main() -> None:
    args = parse_args()
    task_path = Path(args.task)
    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    task = load_json(task_path)
    profiles = load_profiles()
    require_task(task)
    validate_task_mapping(task, output_dir, profiles)

    if args.validate_only:
        print(f"Mapeo científico válido para {task['task_id']}.")
        return

    if task["case_type"] in {"valid", "negative"}:
        details = run_conformance_task(task, output_dir, profiles)
    elif task["tool"] == "tlc":
        details = run_tlc_task(task, output_dir, profiles)
    else:
        details = run_alloy_task(task, output_dir, profiles)

    payload = {
        "schema_version": 1,
        "phase": "8C",
        "protocol_id": PROTOCOL_ID,
        "status": "completed",
        "expectation_met": True,
        "task_id": task["task_id"],
        "configuration_id": task["configuration_id"],
        "rq_ids": task["rq_ids"],
        "tool": details["tool"],
        "case_id": task["case_id"],
        "case_type": task["case_type"],
        "model_kind": task["model_kind"],
        "bound_profile": task["bound_profile"],
        "fault_profile": task["fault_profile"],
        "repetition_kind": task["repetition_kind"],
        "repetition_index": task["repetition_index"],
        "seed": task.get("seed"),
        "property": details.get("property"),
        "logical_outcome": details["logical_outcome"],
        "metrics": details["metrics"],
        "details": {
            key: value
            for key, value in details.items()
            if key not in {"tool", "property", "logical_outcome", "metrics"}
        },
        "message": "La tarea científica terminó con el resultado esperado.",
    }
    write_json_atomic(output_dir / "executor-result.json", payload)
    print("La tarea científica terminó con el resultado esperado.")


if __name__ == "__main__":
    main()
