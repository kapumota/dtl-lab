#!/usr/bin/env python3
"""Valida que el host de medición cumpla el protocolo Q3."""

from __future__ import annotations

import argparse
import json
import os
import platform
import subprocess
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Valida el host dedicado para la matriz definitiva."
    )
    parser.add_argument("--repository-root", required=True)
    parser.add_argument("--output", required=True)
    return parser.parse_args()


def read_text(path: Path) -> str:
    return (
        path.read_text(encoding="utf-8", errors="replace")
        if path.is_file()
        else ""
    )


def run_command(command: list[str], cwd: Path) -> dict[str, Any]:
    try:
        completed = subprocess.run(
            command,
            cwd=cwd,
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            check=False,
            timeout=20,
        )
    except FileNotFoundError:
        return {
            "available": False,
            "exit_code": None,
            "stdout": "",
            "stderr": "Comando no disponible.",
        }
    return {
        "available": True,
        "exit_code": completed.returncode,
        "stdout": completed.stdout.strip(),
        "stderr": completed.stderr.strip(),
    }


def read_total_memory_kb() -> int | None:
    for line in read_text(Path("/proc/meminfo")).splitlines():
        if line.startswith("MemTotal:"):
            parts = line.split()
            if len(parts) >= 2 and parts[1].isdigit():
                return int(parts[1])
    return None


def main() -> None:
    args = parse_args()
    root = Path(args.repository_root).resolve()
    output = Path(args.output)

    errors: list[str] = []
    release_text = platform.release().lower()
    version_text = read_text(Path("/proc/version")).lower()
    if platform.system() != "Linux":
        errors.append("El host definitivo debe usar Linux.")
    if "microsoft" in release_text or "microsoft" in version_text:
        errors.append("WSL no está permitido para mediciones definitivas.")
    if os.environ.get("GITHUB_ACTIONS") == "true":
        errors.append("GitHub Actions no puede ejecutar la matriz definitiva.")

    virtualization = run_command(["systemd-detect-virt"], root)
    detected = virtualization["stdout"].strip().lower()
    if virtualization["available"] and virtualization["exit_code"] == 0:
        if detected not in {"", "none"}:
            errors.append(
                "La virtualización detectada no está permitida: "
                + detected
                + "."
            )

    git_status = run_command(
        ["git", "status", "--porcelain", "--untracked-files=no"],
        root,
    )
    if git_status["stdout"]:
        errors.append(
            "El árbol con archivos versionados debe estar limpio."
        )

    total_memory_kb = read_total_memory_kb()
    minimum_memory_kb = 12288 * 1024
    if total_memory_kb is None:
        errors.append("No se pudo identificar la memoria total.")
    elif total_memory_kb < minimum_memory_kb:
        errors.append(
            "El host no dispone de los 12288 MiB declarados."
        )

    report = {
        "schema_version": 1,
        "phase": "8C",
        "captured_at_utc": datetime.now(timezone.utc).isoformat(),
        "accepted": not errors,
        "errors": errors,
        "platform": {
            "system": platform.system(),
            "release": platform.release(),
            "machine": platform.machine(),
            "processor": platform.processor(),
            "cpu_count": os.cpu_count(),
            "total_memory_kb": total_memory_kb,
        },
        "virtualization": virtualization,
        "git_status": git_status,
        "github_actions": os.environ.get("GITHUB_ACTIONS", "false"),
    }
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )

    if errors:
        raise SystemExit("\n".join(errors))
    print("El host dedicado cumple el protocolo experimental Q3.")


if __name__ == "__main__":
    main()
