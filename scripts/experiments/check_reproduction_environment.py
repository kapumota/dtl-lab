#!/usr/bin/env python3
"""Valida y registra el ambiente usado para la reproducción independiente."""

from __future__ import annotations

import argparse
import json
import os
import platform
import re
import shutil
import socket
import subprocess
from pathlib import Path
from typing import Any

REQUIRED_COMMANDS = (
    "git",
    "make",
    "java",
    "javac",
    "python3",
    "curl",
    "tar",
    "sha256sum",
    "readlink",
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Valida el ambiente de reproducción de Fase 8E."
    )
    parser.add_argument("--repository-root", required=True)
    parser.add_argument("--output")
    parser.add_argument("--allow-dirty", action="store_true")
    return parser.parse_args()


def run_text(command: list[str], cwd: Path | None = None) -> str:
    completed = subprocess.run(
        command,
        cwd=cwd,
        check=False,
        capture_output=True,
        text=True,
    )
    return (completed.stdout + completed.stderr).strip()


def detect_wsl() -> bool:
    release = platform.release().lower()
    version = platform.version().lower()
    proc_version = ""
    path = Path("/proc/version")
    if path.is_file():
        proc_version = path.read_text(encoding="utf-8", errors="replace").lower()
    return "microsoft" in release or "microsoft" in version or "microsoft" in proc_version


def detect_virtualization() -> str:
    command = shutil.which("systemd-detect-virt")
    if command is None:
        return "desconocida"
    completed = subprocess.run(
        [command],
        check=False,
        capture_output=True,
        text=True,
    )
    value = completed.stdout.strip()
    if completed.returncode == 0 and value:
        return value
    return "ninguna"


def parse_java_major(text: str) -> int | None:
    match = re.search(r'version "([0-9]+)', text)
    if match:
        return int(match.group(1))
    return None


def parse_python_version(text: str) -> tuple[int, int] | None:
    match = re.search(r"Python\s+([0-9]+)\.([0-9]+)", text)
    if match:
        return int(match.group(1)), int(match.group(2))
    return None


def git_status(repository_root: Path) -> tuple[str, bool]:
    commit = run_text(["git", "rev-parse", "HEAD"], cwd=repository_root)
    status = run_text(
        ["git", "status", "--porcelain", "--untracked-files=normal"],
        cwd=repository_root,
    )
    return commit, not bool(status.strip())


def write_json(path: Path, value: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(value, ensure_ascii=False, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
        newline="\n",
    )


def main() -> None:
    args = parse_args()
    repository_root = Path(args.repository_root).resolve()
    if not repository_root.is_dir():
        raise SystemExit(f"No existe el repositorio: {repository_root}.")

    commands = {name: shutil.which(name) for name in REQUIRED_COMMANDS}
    commands["time"] = "/usr/bin/time" if Path("/usr/bin/time").is_file() else None
    missing = sorted(name for name, path in commands.items() if path is None)

    java_text = run_text(["java", "-version"]) if commands["java"] else ""
    javac_text = run_text(["javac", "-version"]) if commands["javac"] else ""
    python_text = run_text(["python3", "--version"]) if commands["python3"] else ""
    java_major = parse_java_major(java_text)
    python_version = parse_python_version(python_text)
    commit, clean = git_status(repository_root)

    warnings: list[str] = []
    errors: list[str] = []
    if platform.system().lower() != "linux":
        errors.append("La reproducción automatizada requiere Linux.")
    if missing:
        errors.append("Faltan comandos obligatorios: " + ", ".join(missing) + ".")
    if java_major != 17:
        errors.append("La reproducción requiere Java 17.")
    if python_version != (3, 12):
        errors.append("La reproducción requiere Python 3.12.")
    if not clean and not args.allow_dirty:
        errors.append("El repositorio contiene cambios rastreados.")

    wsl = detect_wsl()
    virtualization = detect_virtualization()
    if wsl:
        warnings.append(
            "WSL permite reproducción funcional y comparación de hashes, pero no mediciones de rendimiento."
        )
    if virtualization not in {"ninguna", "desconocida", "wsl"}:
        warnings.append(
            "La virtualización se registra como una limitación para cualquier interpretación temporal."
        )

    report = {
        "schema_version": 1,
        "phase": "8E",
        "status": "incompatible" if errors else "compatible_con_advertencias" if warnings else "compatible",
        "hostname": socket.gethostname(),
        "user": os.environ.get("USER") or os.environ.get("USERNAME") or "desconocido",
        "platform": platform.platform(),
        "architecture": platform.machine(),
        "kernel": platform.release(),
        "is_wsl": wsl,
        "virtualization": virtualization,
        "repository_root": str(repository_root),
        "source_commit": commit,
        "tracked_worktree_clean": clean,
        "commands": commands,
        "java": java_text,
        "javac": javac_text,
        "python": python_text,
        "warnings": warnings,
        "errors": errors,
    }

    if args.output:
        write_json(Path(args.output).resolve(), report)
    print(json.dumps(report, ensure_ascii=False, indent=2, sort_keys=True))

    if errors:
        raise SystemExit("El ambiente no cumple los requisitos de reproducción.")


if __name__ == "__main__":
    main()
