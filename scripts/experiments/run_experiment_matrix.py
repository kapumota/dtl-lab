#!/usr/bin/env python3
"""Ejecuta de forma serial y reanudable un plan experimental."""

from __future__ import annotations

import argparse
import fcntl
import json
import os
import re
import shutil
import signal
import subprocess
import sys
import time
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
    require_sha256,
    write_json_atomic,
)


TERMINAL_STATUSES = {
    "completed",
    "timeout",
    "out_of_memory",
    "tool_error",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Ejecuta un plan experimental serial con reanudacion."
    )
    parser.add_argument("--repository-root", required=True)
    parser.add_argument("--plan", required=True)
    parser.add_argument("--manifest", required=True)
    parser.add_argument("--run-dir", required=True)
    parser.add_argument("--executor", required=True)
    parser.add_argument(
        "--phase",
        choices=["8B", "8C"],
        default="8B",
    )
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--smoke", action="store_true")
    parser.add_argument("--limit", type=int)
    return parser.parse_args()


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def parse_max_rss_kb(path: Path) -> int | None:
    if not path.is_file():
        return None
    pattern = re.compile(
        r"Maximum resident set size \(kbytes\):\s*(\d+)"
    )
    for line in path.read_text(
        encoding="utf-8",
        errors="replace",
    ).splitlines():
        match = pattern.search(line)
        if match:
            return int(match.group(1))
    return None


def build_executor_command(
    executor: Path,
    task_path: Path,
    output_dir: Path,
) -> list[str]:
    if executor.suffix == ".py":
        prefix = [sys.executable, str(executor)]
    else:
        prefix = [str(executor)]
    return prefix + [
        "--task",
        str(task_path),
        "--output-dir",
        str(output_dir),
    ]


def classify_status(
    exit_code: int | None,
    stderr_text: str,
    timed_out: bool,
    executor_payload: dict[str, Any] | None,
) -> str:
    if timed_out:
        return "timeout"
    lowered = stderr_text.lower()
    if exit_code in {134, 137} or any(
        marker in lowered
        for marker in (
            "outofmemoryerror",
            "cannot allocate memory",
            "killed",
        )
    ):
        return "out_of_memory"
    if exit_code == 0 and executor_payload is not None:
        if executor_payload.get("status") == "completed":
            return "completed"
    return "tool_error"


def validate_plan(
    plan_path: Path,
    manifest_path: Path,
) -> tuple[list[dict[str, Any]], dict[str, Any]]:
    tasks = load_jsonl(plan_path)
    manifest = load_json(manifest_path)
    if manifest.get("protocol_id") != "paper1-q3-v1":
        raise SystemExit("El manifiesto del plan no coincide con el protocolo.")
    expected_plan_sha = require_sha256(
        manifest.get("plan_sha256"),
        "plan_sha256",
    )
    actual_plan_sha = calculate_sha256_file(plan_path)
    if actual_plan_sha != expected_plan_sha:
        raise SystemExit("El hash del plan no coincide con su manifiesto.")
    expected_total = manifest.get("counts", {}).get("tasks_total")
    if expected_total != len(tasks):
        raise SystemExit("La cantidad de tareas no coincide con el manifiesto.")

    task_ids: set[str] = set()
    for task in tasks:
        task_id = task.get("task_id")
        if not isinstance(task_id, str) or not task_id:
            raise SystemExit("Existe una tarea sin task_id.")
        if task_id in task_ids:
            raise SystemExit(f"task_id duplicado: {task_id}.")
        task_ids.add(task_id)
        require_sha256(task.get("task_sha256"), f"task_sha256 de {task_id}")
    return tasks, manifest


def acquire_lock(run_dir: Path):
    lock_path = run_dir.parent / f".{run_dir.name}.lock"
    lock_path.parent.mkdir(parents=True, exist_ok=True)
    stream = lock_path.open("a+", encoding="utf-8")
    try:
        fcntl.flock(stream.fileno(), fcntl.LOCK_EX | fcntl.LOCK_NB)
    except BlockingIOError as error:
        stream.close()
        raise SystemExit(
            f"Ya existe una ejecución activa para {run_dir}."
        ) from error
    return stream


def initialize_run(
    root: Path,
    run_dir: Path,
    plan_path: Path,
    manifest_path: Path,
    executor: Path,
    smoke: bool,
    phase: str,
) -> None:
    run_dir.mkdir(parents=True, exist_ok=True)
    snapshots = run_dir / "snapshots"
    snapshots.mkdir(parents=True, exist_ok=True)

    destination_plan = snapshots / "plan.jsonl"
    destination_manifest = snapshots / "plan-manifest.json"
    if destination_plan.exists():
        if calculate_sha256_file(destination_plan) != calculate_sha256_file(
            plan_path
        ):
            raise SystemExit(
                "El plan local no coincide con el snapshot de la ejecución."
            )
    else:
        shutil.copy2(plan_path, destination_plan)

    if destination_manifest.exists():
        if calculate_sha256_file(
            destination_manifest
        ) != calculate_sha256_file(manifest_path):
            raise SystemExit(
                "El manifiesto local no coincide con el snapshot."
            )
    else:
        shutil.copy2(manifest_path, destination_manifest)

    environment_path = run_dir / "environment.json"
    if not environment_path.exists():
        command = [
            sys.executable,
            str(SCRIPT_DIR / "collect_environment.py"),
            "--repository-root",
            str(root),
            "--output",
            str(environment_path),
            "--phase",
            phase,
        ]
        completed = subprocess.run(command, check=False)
        if completed.returncode != 0:
            raise SystemExit("No se pudo capturar el ambiente experimental.")

    provenance_path = run_dir / "provenance.json"
    if not provenance_path.exists():
        provenance = {
            "schema_version": 1,
            "phase": phase,
            "protocol_id": "paper1-q3-v1",
            "run_kind": (
                "scientific_smoke"
                if smoke and phase == "8C"
                else "infrastructure_smoke"
                if smoke
                else "definitive"
            ),
            "created_at_utc": utc_now(),
            "plan_sha256": calculate_sha256_file(plan_path),
            "manifest_sha256": calculate_sha256_file(manifest_path),
            "executor_path": str(executor),
            "executor_sha256": calculate_sha256_file(executor),
            "max_parallel_runs": 1,
            "results_are_raw": True,
            "derived_results_generated": False,
        }
        write_json_atomic(provenance_path, provenance)


def load_existing_result(
    result_path: Path,
    task: dict[str, Any],
) -> dict[str, Any] | None:
    if not result_path.is_file():
        return None
    result = load_json(result_path)
    if result.get("task_id") != task["task_id"]:
        raise SystemExit(
            f"El resultado de {task['task_id']} tiene otro task_id."
        )
    if result.get("task_sha256") != task["task_sha256"]:
        raise SystemExit(
            f"El resultado de {task['task_id']} usa otra definición."
        )
    if result.get("status") not in TERMINAL_STATUSES:
        raise SystemExit(
            f"El resultado de {task['task_id']} no es terminal."
        )
    return result


def preserve_incomplete_attempt(task_dir: Path) -> None:
    if not task_dir.exists():
        return
    result_path = task_dir / "result.json"
    if result_path.exists():
        return
    timestamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    destination = (
        task_dir.parent.parent
        / "incomplete_attempts"
        / f"{task_dir.name}-{timestamp}"
    )
    destination.parent.mkdir(parents=True, exist_ok=True)
    if destination.exists():
        raise SystemExit(
            f"Ya existe el intento incompleto {destination}."
        )
    task_dir.replace(destination)


def execute_task(
    task: dict[str, Any],
    tasks_root: Path,
    executor: Path,
) -> dict[str, Any]:
    task_dir = tasks_root / task["task_id"]
    result_path = task_dir / "result.json"
    existing = load_existing_result(result_path, task)
    if existing is not None:
        return {
            "action": "skipped",
            "result": existing,
        }

    preserve_incomplete_attempt(task_dir)
    task_dir.mkdir(parents=True, exist_ok=False)
    task_path = task_dir / "task.json"
    write_json_atomic(task_path, task)

    stdout_path = task_dir / "stdout.txt"
    stderr_path = task_dir / "stderr.txt"
    time_path = task_dir / "time.txt"
    executor_result_path = task_dir / "executor-result.json"

    command = [
        "/usr/bin/time",
        "-v",
        "-o",
        str(time_path),
        *build_executor_command(executor, task_path, task_dir),
    ]
    started_at = utc_now()
    started_monotonic = time.monotonic()
    timed_out = False
    exit_code: int | None = None

    with stdout_path.open(
        "w",
        encoding="utf-8",
        newline="\n",
    ) as stdout_stream, stderr_path.open(
        "w",
        encoding="utf-8",
        newline="\n",
    ) as stderr_stream:
        process = subprocess.Popen(
            command,
            stdout=stdout_stream,
            stderr=stderr_stream,
            start_new_session=True,
        )
        try:
            exit_code = process.wait(
                timeout=int(task["timeout_seconds"])
            )
        except subprocess.TimeoutExpired:
            timed_out = True
            os.killpg(process.pid, signal.SIGTERM)
            try:
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                os.killpg(process.pid, signal.SIGKILL)
                process.wait()

    finished_at = utc_now()
    elapsed_seconds = time.monotonic() - started_monotonic
    stderr_text = (
        stderr_path.read_text(encoding="utf-8", errors="replace")
        if stderr_path.is_file()
        else ""
    )
    executor_payload = (
        load_json(executor_result_path)
        if executor_result_path.is_file()
        else None
    )
    status = classify_status(
        exit_code,
        stderr_text,
        timed_out,
        executor_payload,
    )
    expectation_met = (
        executor_payload.get("expectation_met")
        if executor_payload is not None
        else None
    )

    result = {
        "schema_version": 1,
        "protocol_id": "paper1-q3-v1",
        "task_id": task["task_id"],
        "task_sha256": task["task_sha256"],
        "status": status,
        "started_at_utc": started_at,
        "finished_at_utc": finished_at,
        "elapsed_seconds": round(elapsed_seconds, 6),
        "max_rss_kb": parse_max_rss_kb(time_path),
        "exit_code": exit_code,
        "expectation_met": expectation_met,
        "stdout_path": str(stdout_path.relative_to(tasks_root.parent)),
        "stderr_path": str(stderr_path.relative_to(tasks_root.parent)),
        "time_path": str(time_path.relative_to(tasks_root.parent)),
        "executor_result_path": (
            str(executor_result_path.relative_to(tasks_root.parent))
            if executor_result_path.is_file()
            else ""
        ),
        "message": (
            "La tarea terminó correctamente."
            if status == "completed"
            else "La tarea terminó con un estado incompleto o de error."
        ),
        "executor_payload": executor_payload or {},
    }
    write_json_atomic(result_path, result)
    return {
        "action": "executed",
        "result": result,
    }


def write_state(
    run_dir: Path,
    total: int,
    executed: int,
    skipped: int,
    statuses: dict[str, int],
    smoke: bool,
    phase: str,
) -> None:
    state = {
        "schema_version": 1,
        "phase": phase,
        "protocol_id": "paper1-q3-v1",
        "run_kind": (
            "scientific_smoke"
            if smoke and phase == "8C"
            else "infrastructure_smoke"
            if smoke
            else "definitive"
        ),
        "updated_at_utc": utc_now(),
        "tasks_planned": total,
        "tasks_executed_this_invocation": executed,
        "tasks_skipped_this_invocation": skipped,
        "terminal_results_by_status": statuses,
    }
    write_json_atomic(run_dir / "state.json", state)


def main() -> None:
    args = parse_args()
    root = Path(args.repository_root).resolve()
    plan_path = Path(args.plan).resolve()
    manifest_path = Path(args.manifest).resolve()
    run_dir = Path(args.run_dir).resolve()
    executor = Path(args.executor).resolve()

    if not executor.is_file():
        raise SystemExit(f"Falta el executor: {executor}.")
    if args.limit is not None and not args.smoke:
        raise SystemExit("--limit solo está permitido con --smoke.")
    if args.limit is not None and args.limit <= 0:
        raise SystemExit("--limit debe ser positivo.")
    if os.environ.get("GITHUB_ACTIONS") == "true" and not (
        args.smoke or args.dry_run
    ):
        raise SystemExit(
            "CI no puede producir mediciones experimentales definitivas."
        )

    tasks, manifest = validate_plan(plan_path, manifest_path)
    if args.limit is not None:
        tasks = tasks[: args.limit]

    if args.dry_run:
        print(
            "Plan válido. Tareas seleccionadas: "
            f"{len(tasks)} de {manifest['counts']['tasks_total']}."
        )
        return

    lock_stream = acquire_lock(run_dir)
    try:
        initialize_run(
            root,
            run_dir,
            plan_path,
            manifest_path,
            executor,
            args.smoke,
            args.phase,
        )
        tasks_root = run_dir / "tasks"
        tasks_root.mkdir(parents=True, exist_ok=True)

        executed = 0
        skipped = 0
        statuses: dict[str, int] = {}
        for task in tasks:
            outcome = execute_task(task, tasks_root, executor)
            result = outcome["result"]
            if outcome["action"] == "executed":
                executed += 1
            else:
                skipped += 1
            status = result["status"]
            statuses[status] = statuses.get(status, 0) + 1
            write_state(
                run_dir,
                len(tasks),
                executed,
                skipped,
                statuses,
                args.smoke,
                args.phase,
            )

        print(
            "Ejecución experimental completada. "
            f"Ejecutadas={executed}, reanudadas={skipped}."
        )
    finally:
        fcntl.flock(lock_stream.fileno(), fcntl.LOCK_UN)
        lock_stream.close()


if __name__ == "__main__":
    main()
