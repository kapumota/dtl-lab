#!/usr/bin/env python3
"""Convierte el recibo JSON de Alloy en resultados CSV y JSON verificables."""

from __future__ import annotations

import argparse
import csv
import json
import re
import sys
from pathlib import Path


def parseInteger(value: str | None) -> int | None:
    if value is None:
        return None
    return int(value.replace(",", ""))


def parseElapsed(value: str | None) -> float | None:
    if value is None:
        return None
    parts = value.strip().split(":")
    try:
        if len(parts) == 3:
            hours, minutes, seconds = parts
            return int(hours) * 3600 + int(minutes) * 60 + float(seconds)
        if len(parts) == 2:
            minutes, seconds = parts
            return int(minutes) * 60 + float(seconds)
        return float(parts[0])
    except ValueError:
        return None


def findFirst(pattern: str, text: str) -> re.Match[str] | None:
    return re.search(pattern, text, re.IGNORECASE | re.MULTILINE)


def extractCheckCommands(spec_text: str) -> list[tuple[str, str]]:
    checks: list[tuple[str, str]] = []
    pattern = re.compile(
        r"^\s*check\s+([A-Za-z_][A-Za-z0-9_]*)\b(.*)$",
        re.IGNORECASE | re.MULTILINE,
    )
    for match in pattern.finditer(spec_text):
        checks.append((match.group(1), match.group(2).strip()))
    return checks


def findExpectationViolations(
    checks: list[tuple[str, str]], output: str
) -> list[str]:
    violations: list[str] = []
    for line in output.splitlines():
        if "was satisfied against expectation" not in line.lower():
            continue
        for name, _ in checks:
            if re.search(rf"\b{re.escape(name)}\b", line, re.IGNORECASE):
                violations.append(name)
    return list(dict.fromkeys(violations))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--kind", required=True)
    parser.add_argument("--spec", required=True, type=Path)
    parser.add_argument("--receipt", required=True, type=Path)
    parser.add_argument("--stdout", required=True, type=Path)
    parser.add_argument("--stderr", required=True, type=Path)
    parser.add_argument("--time-log", required=True, type=Path)
    parser.add_argument("--exit-code", required=True, type=int)
    parser.add_argument("--expect", choices=("success", "failure"), required=True)
    parser.add_argument("--expected-property", default="")
    parser.add_argument("--rows", required=True, type=Path)
    parser.add_argument("--summary", required=True, type=Path)
    args = parser.parse_args()
    expected_property = args.expected_property.strip()

    stdout = args.stdout.read_text(encoding="utf-8", errors="replace") if args.stdout.exists() else ""
    stderr = args.stderr.read_text(encoding="utf-8", errors="replace") if args.stderr.exists() else ""
    time_text = args.time_log.read_text(encoding="utf-8", errors="replace") if args.time_log.exists() else ""

    elapsed_match = findFirst(
        r"Elapsed \(wall clock\) time \(h:mm:ss or m:ss\):\s*(\S+)", time_text
    )
    memory_match = findFirst(
        r"Maximum resident set size \(kbytes\):\s*([\d,]+)", time_text
    )
    elapsed_seconds = parseElapsed(elapsed_match.group(1)) if elapsed_match else None
    max_memory_kb = parseInteger(memory_match.group(1)) if memory_match else None

    receipt_data: dict[str, object] = {}
    if args.receipt.exists():
        try:
            receipt_data = json.loads(args.receipt.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            receipt_data = {}

    spec_text = args.spec.read_text(encoding="utf-8", errors="replace")
    checks = extractCheckCommands(spec_text)
    combined_output = f"{stdout}\n{stderr}"
    expectation_violations = findExpectationViolations(checks, combined_output)

    commands = receipt_data.get("commands", {}) if isinstance(receipt_data, dict) else {}
    solver = receipt_data.get("solver", "") if isinstance(receipt_data, dict) else ""
    rows: list[dict[str, object]] = []

    if isinstance(commands, dict):
        for label, command in commands.items():
            if not isinstance(command, dict):
                continue
            command_type = str(command.get("type", ""))
            if command_type.lower() != "check":
                continue
            solutions = command.get("solution", [])
            if not isinstance(solutions, list):
                solutions = []
            counterexamples = len(solutions)
            result = "PASS" if counterexamples == 0 else "FAIL"
            solve_duration_ms = sum(
                int(solution.get("duration", 0))
                for solution in solutions
                if isinstance(solution, dict)
            )
            rows.append(
                {
                    "run_id": args.run_id,
                    "kind": args.kind,
                    "model": args.spec.name,
                    "property": label,
                    "expected_property": expected_property,
                    "command_type": command_type,
                    "result": result,
                    "expected_outcome": args.expect,
                    "tool_exit_code": args.exit_code,
                    "solver": solver,
                    "scope": command.get("scope", ""),
                    "counterexamples": counterexamples,
                    "solve_duration_ms": solve_duration_ms,
                    "elapsed_seconds": elapsed_seconds if elapsed_seconds is not None else "",
                    "max_memory_kb": max_memory_kb if max_memory_kb is not None else "",
                    "states_generated": "",
                    "distinct_states": "",
                    "depth": "",
                    "receipt_path": str(args.receipt),
                }
            )

    report_source = "receipt-json" if args.receipt.exists() and rows and solver else ""

    if not rows and expectation_violations:
        solver = solver or "sat4j"
        violated = set(expectation_violations)
        for label, scope in checks:
            is_violated = label in violated
            rows.append(
                {
                    "run_id": args.run_id,
                    "kind": args.kind,
                    "model": args.spec.name,
                    "property": label,
                    "expected_property": expected_property,
                    "command_type": "check",
                    "result": "FAIL" if is_violated else "NOT_EVALUATED",
                    "expected_outcome": args.expect,
                    "tool_exit_code": args.exit_code,
                    "solver": solver,
                    "scope": scope,
                    "counterexamples": 1 if is_violated else "",
                    "solve_duration_ms": "",
                    "elapsed_seconds": elapsed_seconds if elapsed_seconds is not None else "",
                    "max_memory_kb": max_memory_kb if max_memory_kb is not None else "",
                    "states_generated": "",
                    "distinct_states": "",
                    "depth": "",
                    "receipt_path": "",
                }
            )
        report_source = "cli-expectation-error"

    report_present = bool(
        report_source == "receipt-json"
        or (
            report_source == "cli-expectation-error"
            and args.expect == "failure"
            and args.exit_code != 0
            and expectation_violations
        )
    )
    violated_properties = list(
        dict.fromkeys(
            [
                str(row["property"])
                for row in rows
                if row.get("result") == "FAIL"
            ]
            + expectation_violations
        )
    )
    target_property_failed = bool(
        expected_property and expected_property in violated_properties
    )
    unexpected_violated_properties = [
        property_name
        for property_name in violated_properties
        if property_name != expected_property
    ]
    actual_success = bool(
        args.exit_code == 0 and rows and all(row["result"] == "PASS" for row in rows)
    )
    if args.expect == "success":
        expectation_met = actual_success
    else:
        expectation_met = bool(
            report_present
            and expected_property
            and target_property_failed
        )
    metrics_complete = elapsed_seconds is not None and max_memory_kb is not None

    fieldnames = list(rows[0].keys()) if rows else [
        "run_id",
        "kind",
        "model",
        "property",
        "expected_property",
        "command_type",
        "result",
        "expected_outcome",
        "tool_exit_code",
        "solver",
        "scope",
        "counterexamples",
        "solve_duration_ms",
        "elapsed_seconds",
        "max_memory_kb",
        "states_generated",
        "distinct_states",
        "depth",
        "receipt_path",
    ]
    args.rows.parent.mkdir(parents=True, exist_ok=True)
    with args.rows.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    summary = {
        "run_id": args.run_id,
        "kind": args.kind,
        "tool": "Alloy",
        "model": str(args.spec),
        "expected_outcome": args.expect,
        "expected_property": expected_property,
        "actual_outcome": "success" if actual_success else "failure",
        "expectation_met": expectation_met,
        "report_present": report_present,
        "report_source": report_source,
        "metrics_complete": metrics_complete,
        "tool_exit_code": args.exit_code,
        "solver": solver,
        "violated_properties": violated_properties,
        "target_property_failed": target_property_failed,
        "unexpected_violated_properties": unexpected_violated_properties,
        "elapsed_seconds": elapsed_seconds,
        "max_memory_kb": max_memory_kb,
        "stdout_path": str(args.stdout),
        "stderr_path": str(args.stderr),
        "properties": rows,
    }
    args.summary.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    if not report_present:
        print("Alloy no genero un recibo JSON reconocible.", file=sys.stderr)
        return 1
    if not metrics_complete:
        print("El reporte de Alloy no contiene tiempo y memoria.", file=sys.stderr)
        return 1
    if args.expect == "failure" and not expected_property:
        print("Falta declarar la propiedad objetivo del mutante Alloy.", file=sys.stderr)
        return 1
    if args.expect == "failure" and not target_property_failed:
        print(
            f"La propiedad objetivo {expected_property} no produjo un contraejemplo.",
            file=sys.stderr,
        )
        return 1
    if not expectation_met:
        print("El resultado de Alloy no coincide con el resultado esperado.", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
