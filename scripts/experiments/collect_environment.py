#!/usr/bin/env python3
"""Captura ambiente y procedencia para una ejecución experimental."""

from __future__ import annotations

import argparse
import os
import platform
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

from experiment_io import (  # noqa: E402
    calculate_sha256_file,
    write_json_atomic,
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Captura el ambiente de una ejecución experimental."
    )
    parser.add_argument("--repository-root", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument(
        "--phase",
        choices=["8B", "8C"],
        default="8B",
    )
    return parser.parse_args()


def run_command(
    command: list[str],
    cwd: Path,
) -> dict[str, Any]:
    try:
        completed = subprocess.run(
            command,
            cwd=cwd,
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            check=False,
            timeout=30,
        )
    except FileNotFoundError:
        return {
            "available": False,
            "exit_code": None,
            "stdout": "",
            "stderr": "Comando no disponible.",
        }
    except subprocess.TimeoutExpired:
        return {
            "available": True,
            "exit_code": None,
            "stdout": "",
            "stderr": "El comando excedió el tiempo de captura.",
        }
    return {
        "available": True,
        "exit_code": completed.returncode,
        "stdout": completed.stdout.strip(),
        "stderr": completed.stderr.strip(),
    }


def read_total_memory_kb() -> int | None:
    path = Path("/proc/meminfo")
    if not path.is_file():
        return None
    for line in path.read_text(encoding="utf-8").splitlines():
        if line.startswith("MemTotal:"):
            parts = line.split()
            if len(parts) >= 2 and parts[1].isdigit():
                return int(parts[1])
    return None


def find_formal_artifact(
    root: Path,
    relative_path: str,
) -> dict[str, Any]:
    path = root / relative_path
    if not path.is_file():
        return {
            "path": relative_path,
            "available": False,
            "sha256": None,
            "size_bytes": None,
        }
    return {
        "path": relative_path,
        "available": True,
        "sha256": calculate_sha256_file(path),
        "size_bytes": path.stat().st_size,
    }


def main() -> None:
    args = parse_args()
    root = Path(args.repository_root).resolve()
    output = Path(args.output)

    git_commit = run_command(["git", "rev-parse", "HEAD"], root)
    git_branch = run_command(["git", "branch", "--show-current"], root)
    git_status = run_command(
        ["git", "status", "--porcelain", "--untracked-files=no"],
        root,
    )

    environment = {
        "schema_version": 1,
        "phase": args.phase,
        "captured_at_utc": datetime.now(timezone.utc).isoformat(),
        "repository_root": str(root),
        "git": {
            "commit": git_commit["stdout"] or "desconocido",
            "branch": git_branch["stdout"] or "desconocida",
            "tracked_worktree_clean": git_status["stdout"] == "",
        },
        "platform": {
            "operating_system": platform.platform(),
            "system": platform.system(),
            "release": platform.release(),
            "machine": platform.machine(),
            "processor": platform.processor(),
            "cpu_count": os.cpu_count(),
            "total_memory_kb": read_total_memory_kb(),
            "virtualized_timing_allowed": False,
        },
        "commands": {
            "python": run_command([sys.executable, "--version"], root),
            "java": run_command(["java", "-version"], root),
            "javac": run_command(["javac", "-version"], root),
            "git": run_command(["git", "--version"], root),
            "time": run_command(["/usr/bin/time", "--version"], root),
        },
        "formal_artifacts": {
            "tlc": find_formal_artifact(
                root,
                ".formal-tools/tla/1.7.4/tla2tools.jar",
            ),
            "alloy": find_formal_artifact(
                root,
                ".formal-tools/alloy/6.2.0/"
                "org.alloytools.alloy.dist-6.2.0.jar",
            ),
        },
        "github_actions": {
            "enabled": os.environ.get("GITHUB_ACTIONS", "false"),
            "event_name": os.environ.get("GITHUB_EVENT_NAME"),
            "run_id": os.environ.get("GITHUB_RUN_ID"),
            "run_attempt": os.environ.get("GITHUB_RUN_ATTEMPT"),
            "repository": os.environ.get("GITHUB_REPOSITORY"),
            "sha": os.environ.get("GITHUB_SHA"),
            "ref": os.environ.get("GITHUB_REF"),
        },
    }

    write_json_atomic(output, environment)
    print(f"Ambiente experimental capturado en {output}.")


if __name__ == "__main__":
    main()
