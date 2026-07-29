#!/usr/bin/env python3
"""Prepara herramientas y clases para ejecutar la matriz científica."""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Prepara el runtime científico de la Fase 8C."
    )
    parser.add_argument("--repository-root", required=True)
    parser.add_argument("--mode", choices=["smoke", "definitive"], required=True)
    parser.add_argument("--output", required=True)
    return parser.parse_args()


def read_shell_values(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    pattern = re.compile(r'^([A-Z0-9_]+)="([^"]*)"$')
    for line in path.read_text(encoding="utf-8").splitlines():
        match = pattern.match(line.strip())
        if match:
            values[match.group(1)] = match.group(2)
    return values


def calculate_sha1(path: Path) -> str:
    digest = hashlib.sha1()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def run_command(
    command: list[str],
    *,
    cwd: Path,
    check: bool = True,
) -> subprocess.CompletedProcess[str]:
    completed = subprocess.run(
        command,
        cwd=cwd,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )
    if check and completed.returncode != 0:
        message = completed.stderr.strip() or completed.stdout.strip()
        raise SystemExit(
            "Falló la preparación del runtime científico. " + message
        )
    return completed


def parse_java_major(text: str) -> int:
    match = re.search(r'version "(\d+)', text)
    if not match:
        raise SystemExit("No se pudo identificar la versión de Java.")
    return int(match.group(1))


def compile_java(root: Path, destination: Path) -> int:
    sources = sorted((root / "src/main/java").rglob("*.java"))
    if not sources:
        raise SystemExit("No se encontraron fuentes Java.")
    if destination.exists():
        shutil.rmtree(destination)
    destination.mkdir(parents=True, exist_ok=True)
    run_command(
        [
            "javac",
            "-d",
            str(destination),
            *[str(path) for path in sources],
        ],
        cwd=root,
    )
    return len(sources)


def main() -> None:
    args = parse_args()
    root = Path(args.repository_root).resolve()
    output = Path(args.output)
    values = read_shell_values(root / "scripts/formal/tool_versions.env")

    tla_version = values.get("TLA_TOOLS_VERSION", "")
    alloy_version = values.get("ALLOY_VERSION", "")
    tools_dir = values.get("FORMAL_TOOLS_DIR", ".formal-tools")
    tla_jar = root / tools_dir / "tla" / tla_version / "tla2tools.jar"
    alloy_jar = (
        root
        / tools_dir
        / "alloy"
        / alloy_version
        / f"org.alloytools.alloy.dist-{alloy_version}.jar"
    )

    for path in (tla_jar, alloy_jar):
        if not path.is_file():
            raise SystemExit(f"Falta la herramienta formal: {path}.")

    expected_tla_sha1 = values.get("TLA_TOOLS_SHA1", "")
    actual_tla_sha1 = calculate_sha1(tla_jar)
    if actual_tla_sha1 != expected_tla_sha1:
        raise SystemExit("El SHA-1 de TLC no coincide con la versión fijada.")

    java_result = run_command(["java", "-version"], cwd=root, check=False)
    java_text = java_result.stderr + "\n" + java_result.stdout
    if parse_java_major(java_text) != 17:
        raise SystemExit("La matriz científica requiere Java 17.")

    python_result = run_command(
        ["python3", "--version"],
        cwd=root,
        check=False,
    )
    if not python_result.stdout.startswith("Python 3.12"):
        raise SystemExit("La matriz científica requiere Python 3.12.")

    alloy_result = run_command(
        ["java", "-jar", str(alloy_jar), "version", "--full"],
        cwd=root,
        check=False,
    )
    if not alloy_result.stdout.startswith(alloy_version):
        raise SystemExit("La versión reportada de Alloy no coincide.")

    classes_dir = root / "build/experiment-classes"
    source_count = compile_java(root, classes_dir)

    report: dict[str, Any] = {
        "schema_version": 1,
        "phase": "8C",
        "mode": args.mode,
        "prepared_at_utc": datetime.now(timezone.utc).isoformat(),
        "repository_root": str(root),
        "java_sources_compiled": source_count,
        "classes_dir": str(classes_dir),
        "tools": {
            "tlc": {
                "path": str(tla_jar),
                "version": tla_version,
                "sha1": actual_tla_sha1,
            },
            "alloy": {
                "path": str(alloy_jar),
                "version": alloy_version,
                "reported": alloy_result.stdout.strip(),
            },
            "java": java_text.strip(),
            "python": python_result.stdout.strip(),
        },
        "github_actions": os.environ.get("GITHUB_ACTIONS", "false"),
    }
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    print("El runtime científico de Fase 8C quedó preparado.")


if __name__ == "__main__":
    main()
