#!/usr/bin/env python3
"""Convierte una ejecución de TLC en resultados CSV y JSON verificables."""

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


def readInvariants(config_path: Path) -> list[str]:
    invariants: list[str] = []
    for line in config_path.read_text(encoding="utf-8").splitlines():
        match = re.match(r"\s*INVARIANT\s+([A-Za-z_][A-Za-z0-9_]*)", line)
        if match:
            invariants.append(match.group(1))
    return invariants


def findFirst(patterns: list[str], text: str) -> re.Match[str] | None:
    for pattern in patterns:
        match = re.search(pattern, text, re.IGNORECASE | re.MULTILINE)
        if match:
            return match
    return None


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--kind", required=True)
    parser.add_argument("--spec", required=True, type=Path)
    parser.add_argument("--config", required=True, type=Path)
    parser.add_argument("--stdout", required=True, type=Path)
    parser.add_argument("--stderr", required=True, type=Path)
    parser.add_argument("--time-log", required=True, type=Path)
    parser.add_argument("--exit-code", required=True, type=int)
    parser.add_argument("--expect", choices=("success", "failure"), required=True)
    parser.add_argument("--rows", required=True, type=Path)
    parser.add_argument("--summary", required=True, type=Path)
    args = parser.parse_args()

    stdout = args.stdout.read_text(encoding="utf-8", errors="replace") if args.stdout.exists() else ""
    stderr = args.stderr.read_text(encoding="utf-8", errors="replace") if args.stderr.exists() else ""
    time_text = args.time_log.read_text(encoding="utf-8", errors="replace") if args.time_log.exists() else ""
    combined = stdout + "\n" + stderr

    states_match = findFirst(
        [r"([\d,]+)\s+states generated,\s+([\d,]+)\s+distinct states found"],
        combined,
    )
    depth_match = findFirst(
        [r"depth of the complete state graph search is\s+([\d,]+)"],
        combined,
    )
    elapsed_match = findFirst(
        [r"Elapsed \(wall clock\) time \(h:mm:ss or m:ss\):\s*(\S+)"],
        time_text,
    )
    memory_match = findFirst(
        [r"Maximum resident set size \(kbytes\):\s*([\d,]+)"],
        time_text,
    )
    version_match = findFirst([r"^(TLC2 Version[^\r\n]+)"], combined)
    violation_match = findFirst(
        [
            r"Invariant\s+([A-Za-z_][A-Za-z0-9_]*)\s+is violated",
            r"Invariant\s+([A-Za-z_][A-Za-z0-9_]*)\s+is violated\.",
            r"The invariant of\s+([A-Za-z_][A-Za-z0-9_]*)\s+is equal to FALSE\.?",
        ],
        combined,
    )

    states_generated = parseInteger(states_match.group(1)) if states_match else None
    distinct_states = parseInteger(states_match.group(2)) if states_match else None
    depth = parseInteger(depth_match.group(1)) if depth_match else None
    elapsed_seconds = parseElapsed(elapsed_match.group(1)) if elapsed_match else None
    max_memory_kb = parseInteger(memory_match.group(1)) if memory_match else None
    tool_version = version_match.group(1).strip() if version_match else None
    violated_property = violation_match.group(1) if violation_match else None
    no_error = "Model checking completed. No error has been found." in combined
    invariants = readInvariants(args.config)

    report_present = bool(tool_version and invariants and (states_match or violation_match))
    actual_success = args.exit_code == 0 and no_error and violated_property is None
    expectation_met = actual_success if args.expect == "success" else not actual_success

    if args.expect == "success":
        metrics_complete = all(
            value is not None
            for value in (states_generated, distinct_states, depth, elapsed_seconds, max_memory_kb)
        )
    else:
        metrics_complete = elapsed_seconds is not None and max_memory_kb is not None

    rows: list[dict[str, object]] = []
    for invariant in invariants:
        if actual_success:
            result = "PASS"
        elif invariant == violated_property:
            result = "FAIL"
        else:
            result = "NOT_EVALUATED"
        rows.append(
            {
                "run_id": args.run_id,
                "kind": args.kind,
                "model": args.spec.name,
                "property": invariant,
                "result": result,
                "expected_outcome": args.expect,
                "tool_exit_code": args.exit_code,
                "states_generated": states_generated if states_generated is not None else "",
                "distinct_states": distinct_states if distinct_states is not None else "",
                "depth": depth if depth is not None else "",
                "elapsed_seconds": elapsed_seconds if elapsed_seconds is not None else "",
                "max_memory_kb": max_memory_kb if max_memory_kb is not None else "",
                "tool_version": tool_version or "",
                "stdout_path": str(args.stdout),
                "stderr_path": str(args.stderr),
            }
        )

    fieldnames = list(rows[0].keys()) if rows else []
    args.rows.parent.mkdir(parents=True, exist_ok=True)
    with args.rows.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    summary = {
        "run_id": args.run_id,
        "kind": args.kind,
        "tool": "TLC",
        "tool_version": tool_version,
        "model": str(args.spec),
        "config": str(args.config),
        "expected_outcome": args.expect,
        "actual_outcome": "success" if actual_success else "failure",
        "expectation_met": expectation_met,
        "report_present": report_present,
        "metrics_complete": metrics_complete,
        "tool_exit_code": args.exit_code,
        "states_generated": states_generated,
        "distinct_states": distinct_states,
        "depth": depth,
        "elapsed_seconds": elapsed_seconds,
        "max_memory_kb": max_memory_kb,
        "violated_property": violated_property,
        "properties": rows,
    }
    args.summary.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    if not report_present:
        print("TLC no genero un reporte reconocible.", file=sys.stderr)
        return 1
    if not metrics_complete:
        print("El reporte de TLC no contiene las metricas obligatorias.", file=sys.stderr)
        return 1
    if not expectation_met:
        print("El resultado de TLC no coincide con el resultado esperado.", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
