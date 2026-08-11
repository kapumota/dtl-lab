#!/usr/bin/env python3
"""Compara por SHA-256 las salidas regeneradas con la referencia de Fase 8D."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import tempfile
from pathlib import Path
from typing import Any

SECTIONS = ("derived", "tables", "figures")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Compara resultados de reproducción con una referencia."
    )
    parser.add_argument("--reference-root", required=True)
    parser.add_argument("--candidate-root", required=True)
    parser.add_argument("--output-dir", required=True)
    return parser.parse_args()


def calculate_sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def collect_files(root: Path) -> dict[str, Path]:
    if not root.is_dir():
        raise SystemExit(f"No existe el directorio para comparar: {root}.")
    return {
        path.relative_to(root).as_posix(): path
        for path in sorted(root.rglob("*"), key=lambda item: item.as_posix())
        if path.is_file()
    }


def compare_sections(reference_root: Path, candidate_root: Path) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    for section in SECTIONS:
        reference_files = collect_files(reference_root / section)
        candidate_files = collect_files(candidate_root / section)
        names = sorted(set(reference_files).union(candidate_files))
        for name in names:
            reference_path = reference_files.get(name)
            candidate_path = candidate_files.get(name)
            reference_hash = (
                calculate_sha256(reference_path) if reference_path is not None else ""
            )
            candidate_hash = (
                calculate_sha256(candidate_path) if candidate_path is not None else ""
            )
            if reference_path is None:
                status = "inesperado"
            elif candidate_path is None:
                status = "ausente"
            elif reference_hash == candidate_hash:
                status = "coincide"
            else:
                status = "hash_distinto"
            rows.append(
                {
                    "section": section,
                    "path": name,
                    "status": status,
                    "reference_sha256": reference_hash,
                    "candidate_sha256": candidate_hash,
                }
            )
    return rows


def write_csv_atomic(path: Path, rows: list[dict[str, Any]]) -> None:
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
                fieldnames=[
                    "section",
                    "path",
                    "status",
                    "reference_sha256",
                    "candidate_sha256",
                ],
                lineterminator="\n",
            )
            writer.writeheader()
            writer.writerows(rows)
        temporary_path.replace(path)
    except Exception:
        temporary_path.unlink(missing_ok=True)
        raise


def write_json_atomic(path: Path, value: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    payload = json.dumps(value, ensure_ascii=False, indent=2, sort_keys=True) + "\n"
    path.write_text(payload, encoding="utf-8", newline="\n")


def write_markdown(path: Path, summary: dict[str, Any], rows: list[dict[str, Any]]) -> None:
    lines = [
        "### Comparación de reproducción independiente",
        "",
        "#### Resumen",
        "",
        f"Archivos comparados: {summary['files_total']}.",
        "",
        f"Archivos coincidentes: {summary['matching']}.",
        "",
        f"Archivos con diferencias: {summary['different']}.",
        "",
        "#### Diferencias",
        "",
        "| Sección | Ruta | Estado |",
        "| --- | --- | --- |",
    ]
    differences = [row for row in rows if row["status"] != "coincide"]
    if differences:
        for row in differences:
            lines.append(
                f"| {row['section']} | {row['path']} | {row['status']} |"
            )
    else:
        lines.append("| todas | todas | coincide |")
    lines.append("")
    path.write_text("\n".join(lines), encoding="utf-8", newline="\n")


def main() -> None:
    args = parse_args()
    reference_root = Path(args.reference_root).resolve()
    candidate_root = Path(args.candidate_root).resolve()
    output_dir = Path(args.output_dir).resolve()
    output_dir.mkdir(parents=True, exist_ok=True)

    rows = compare_sections(reference_root, candidate_root)
    matching = sum(row["status"] == "coincide" for row in rows)
    summary = {
        "schema_version": 1,
        "phase": "8E",
        "status": "coincide" if matching == len(rows) else "no_coincide",
        "files_total": len(rows),
        "matching": matching,
        "different": len(rows) - matching,
        "by_section": {
            section: {
                "files": sum(row["section"] == section for row in rows),
                "matching": sum(
                    row["section"] == section and row["status"] == "coincide"
                    for row in rows
                ),
            }
            for section in SECTIONS
        },
    }

    write_csv_atomic(output_dir / "comparison.csv", rows)
    write_json_atomic(output_dir / "comparison.json", summary)
    write_markdown(output_dir / "comparison.md", summary, rows)

    if summary["status"] != "coincide":
        raise SystemExit("La reproducción produjo archivos diferentes a la referencia.")
    print("Todos los datasets, tablas y figuras coinciden por SHA-256.")


if __name__ == "__main__":
    main()
