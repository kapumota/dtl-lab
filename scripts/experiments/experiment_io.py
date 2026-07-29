#!/usr/bin/env python3
"""Utilidades compartidas para la infraestructura experimental de la Fase 8B."""

from __future__ import annotations

import hashlib
import json
import os
import tempfile
from pathlib import Path
from typing import Any, Iterable


def canonical_json_bytes(value: Any) -> bytes:
    return (
        json.dumps(
            value,
            ensure_ascii=False,
            sort_keys=True,
            separators=(",", ":"),
        )
        + "\n"
    ).encode("utf-8")


def calculate_sha256_bytes(value: bytes) -> str:
    return hashlib.sha256(value).hexdigest()


def calculate_sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def load_json(path: Path) -> dict[str, Any]:
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError as error:
        raise SystemExit(f"Falta el archivo JSON: {path}.") from error
    except json.JSONDecodeError as error:
        raise SystemExit(f"JSON invalido en {path}: {error}.") from error
    if not isinstance(value, dict):
        raise SystemExit(f"El archivo {path} debe contener un objeto JSON.")
    return value


def load_jsonl(path: Path) -> list[dict[str, Any]]:
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except FileNotFoundError as error:
        raise SystemExit(f"Falta el archivo JSONL: {path}.") from error

    rows: list[dict[str, Any]] = []
    for line_number, raw_line in enumerate(lines, start=1):
        line = raw_line.strip()
        if not line:
            continue
        try:
            value = json.loads(line)
        except json.JSONDecodeError as error:
            raise SystemExit(
                f"JSONL invalido en {path}:{line_number}: {error}."
            ) from error
        if not isinstance(value, dict):
            raise SystemExit(
                f"Cada linea de {path} debe contener un objeto JSON."
            )
        rows.append(value)
    return rows


def write_json_atomic(path: Path, value: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    payload = json.dumps(value, ensure_ascii=False, indent=2) + "\n"
    write_text_atomic(path, payload)


def write_jsonl_atomic(path: Path, rows: Iterable[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    payload = "".join(
        json.dumps(row, ensure_ascii=False, sort_keys=True) + "\n"
        for row in rows
    )
    write_text_atomic(path, payload)


def write_text_atomic(path: Path, value: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    file_descriptor, temporary_name = tempfile.mkstemp(
        prefix=f".{path.name}.",
        suffix=".tmp",
        dir=path.parent,
        text=True,
    )
    temporary_path = Path(temporary_name)
    try:
        with os.fdopen(file_descriptor, "w", encoding="utf-8", newline="\n") as stream:
            stream.write(value)
            stream.flush()
            os.fsync(stream.fileno())
        temporary_path.replace(path)
    except Exception:
        temporary_path.unlink(missing_ok=True)
        raise


def require_relative_path(path: str, field_name: str) -> Path:
    candidate = Path(path)
    if candidate.is_absolute() or ".." in candidate.parts:
        raise SystemExit(
            f"{field_name} debe ser una ruta relativa sin retrocesos: {path}."
        )
    return candidate


def require_sha256(value: Any, field_name: str) -> str:
    if not isinstance(value, str) or len(value) != 64:
        raise SystemExit(f"{field_name} debe ser un SHA-256 hexadecimal.")
    if any(character not in "0123456789abcdef" for character in value):
        raise SystemExit(f"{field_name} debe ser un SHA-256 hexadecimal.")
    return value
