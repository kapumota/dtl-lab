#!/usr/bin/env python3
"""Integra los manifiestos valido y negativo de conformidad."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
from pathlib import Path
from typing import Any

VALID_EXPECTED = 10
NEGATIVE_EXPECTED = 10
COMMIT_PATTERN = re.compile(r"^[0-9a-f]{40}$")


def require(condition: bool, message: str) -> None:
    """Interrumpe la integracion cuando un resultado no es valido."""
    if not condition:
        raise SystemExit(message)


def requireText(value: str, message: str) -> str:
    """Valida una cadena no vacia."""
    require(bool(value and value.strip()), message)
    return value.strip()


def readRows(path: Path) -> list[dict[str, str]]:
    """Lee un manifiesto CSV con encabezado."""
    require(path.is_file(), f"No existe el manifiesto: {path}.")
    with path.open(encoding="utf-8", newline="") as stream:
        reader = csv.DictReader(stream)
        require(reader.fieldnames is not None, f"El manifiesto carece de encabezado: {path}.")
        return list(reader)


def sha256File(path: Path) -> str:
    """Calcula el hash SHA-256 de un archivo."""
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(65536), b""):
            digest.update(block)
    return "sha256:" + digest.hexdigest()


def parseBoolean(value: str, field: str, case_id: str) -> bool:
    """Convierte un booleano CSV estricto."""
    normalized = value.strip().lower()
    require(
        normalized in {"true", "false"},
        f"{case_id}: el campo {field} debe ser true o false.",
    )
    return normalized == "true"


def parseInteger(value: str, field: str, case_id: str) -> int:
    """Convierte un entero CSV estricto."""
    try:
        return int(value)
    except ValueError as error:
        raise SystemExit(f"{case_id}: el campo {field} no es entero.") from error


def validateValidRows(rows: list[dict[str, str]], seed: int) -> list[dict[str, Any]]:
    """Valida el catalogo de escenarios aceptados."""
    require(
        len(rows) == VALID_EXPECTED,
        f"Se esperaban {VALID_EXPECTED} escenarios validos y se encontraron {len(rows)}.",
    )
    identifiers: set[str] = set()
    normalized: list[dict[str, Any]] = []
    for row in rows:
        case_id = requireText(row.get("scenario_id", ""), "Falta scenario_id.")
        require(case_id not in identifiers, f"Escenario valido duplicado: {case_id}.")
        identifiers.add(case_id)
        row_seed = parseInteger(row.get("seed", ""), "seed", case_id)
        accepted = parseBoolean(row.get("accepted", ""), "accepted", case_id)
        exit_code = parseInteger(row.get("exit_code", ""), "exit_code", case_id)
        require(row_seed == seed, f"{case_id}: la seed no coincide con {seed}.")
        require(accepted, f"TLC rechazo indebidamente el escenario valido {case_id}.")
        require(exit_code == 0, f"{case_id}: un escenario aceptado debe terminar con codigo cero.")
        require(not row.get("rejected_abstract_step", ""), f"{case_id}: un escenario valido declara rechazo.")
        normalized.append(
            {
                "case_id": case_id,
                "category": "valid",
                "accepted": True,
                "exit_code": exit_code,
                "checked_abstract_steps": parseInteger(
                    row.get("checked_abstract_steps", ""),
                    "checked_abstract_steps",
                    case_id,
                ),
                "diagnostic_matches": None,
                "expected_property": None,
                "rejected_abstract_step": None,
                "rejected_concrete_step": None,
                "rejected_action": None,
                "transfer_id": None,
            }
        )
    return sorted(normalized, key=lambda item: item["case_id"])


def validateNegativeRows(rows: list[dict[str, str]], seed: int) -> list[dict[str, Any]]:
    """Valida el corpus de mutaciones rechazadas."""
    require(
        len(rows) == NEGATIVE_EXPECTED,
        f"Se esperaban {NEGATIVE_EXPECTED} mutaciones y se encontraron {len(rows)}.",
    )
    identifiers: set[str] = set()
    normalized: list[dict[str, Any]] = []
    for row in rows:
        case_id = requireText(row.get("mutation_id", ""), "Falta mutation_id.")
        require(case_id not in identifiers, f"Mutacion duplicada: {case_id}.")
        identifiers.add(case_id)
        row_seed = parseInteger(row.get("seed", ""), "seed", case_id)
        accepted = parseBoolean(row.get("accepted", ""), "accepted", case_id)
        matched = parseBoolean(row.get("diagnostic_matches", ""), "diagnostic_matches", case_id)
        exit_code = parseInteger(row.get("exit_code", ""), "exit_code", case_id)
        require(row_seed == seed, f"{case_id}: la seed no coincide con {seed}.")
        require(not accepted, f"TLC acepto indebidamente la mutacion {case_id}.")
        require(matched, f"El diagnostico no coincide para {case_id}.")
        require(exit_code != 0, f"{case_id}: una mutacion rechazada no debe terminar con codigo cero.")
        for expected_field, observed_field in (
            ("expected_abstract_step", "rejected_abstract_step"),
            ("expected_concrete_step", "rejected_concrete_step"),
            ("expected_action", "rejected_action"),
            ("expected_transfer_id", "transfer_id"),
        ):
            require(
                row.get(expected_field, "") == row.get(observed_field, ""),
                f"{case_id}: {observed_field} no coincide con {expected_field}.",
            )
        normalized.append(
            {
                "case_id": case_id,
                "category": "negative",
                "accepted": False,
                "exit_code": exit_code,
                "checked_abstract_steps": parseInteger(
                    row.get("checked_abstract_steps", ""),
                    "checked_abstract_steps",
                    case_id,
                ),
                "diagnostic_matches": True,
                "expected_property": requireText(
                    row.get("expected_property", ""),
                    f"{case_id}: falta expected_property.",
                ),
                "rejected_abstract_step": parseInteger(
                    row.get("rejected_abstract_step", ""),
                    "rejected_abstract_step",
                    case_id,
                ),
                "rejected_concrete_step": parseInteger(
                    row.get("rejected_concrete_step", ""),
                    "rejected_concrete_step",
                    case_id,
                ),
                "rejected_action": requireText(
                    row.get("rejected_action", ""),
                    f"{case_id}: falta rejected_action.",
                ),
                "transfer_id": requireText(
                    row.get("transfer_id", ""),
                    f"{case_id}: falta transfer_id.",
                ),
            }
        )
    return sorted(normalized, key=lambda item: item["case_id"])


def writeJson(path: Path, value: Any) -> None:
    """Escribe JSON estable con UTF-8 y salto final."""
    path.write_text(
        json.dumps(value, ensure_ascii=False, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def writeMatrix(path: Path, rows: list[dict[str, Any]]) -> None:
    """Escribe una matriz CSV estable de aceptacion y rechazo."""
    fieldnames = [
        "category",
        "case_id",
        "expected_result",
        "observed_result",
        "diagnostic_matches",
        "expected_property",
        "checked_abstract_steps",
        "rejected_abstract_step",
        "rejected_concrete_step",
        "rejected_action",
        "transfer_id",
        "exit_code",
    ]
    with path.open("w", encoding="utf-8", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=fieldnames, lineterminator="\n")
        writer.writeheader()
        for row in rows:
            negative = row["category"] == "negative"
            writer.writerow(
                {
                    "category": row["category"],
                    "case_id": row["case_id"],
                    "expected_result": "rejected" if negative else "accepted",
                    "observed_result": "accepted" if row["accepted"] else "rejected",
                    "diagnostic_matches": "" if row["diagnostic_matches"] is None else str(row["diagnostic_matches"]).lower(),
                    "expected_property": row["expected_property"] or "",
                    "checked_abstract_steps": row["checked_abstract_steps"],
                    "rejected_abstract_step": "" if row["rejected_abstract_step"] is None else row["rejected_abstract_step"],
                    "rejected_concrete_step": "" if row["rejected_concrete_step"] is None else row["rejected_concrete_step"],
                    "rejected_action": row["rejected_action"] or "",
                    "transfer_id": row["transfer_id"] or "",
                    "exit_code": row["exit_code"],
                }
            )


def writeSummary(path: Path, release: str, seed: int) -> None:
    """Escribe un resumen Markdown compatible con las reglas documentales."""
    path.write_text(
        "### Resumen de conformidad acotada basada en trazas\n\n"
        "#### Resultado\n\n"
        f"- release: `{release}`;\n"
        f"- seed: `{seed}`;\n"
        f"- escenarios validos aceptados: `{VALID_EXPECTED}/{VALID_EXPECTED}`;\n"
        f"- trazas corruptas rechazadas: `{NEGATIVE_EXPECTED}/{NEGATIVE_EXPECTED}`;\n"
        f"- diagnosticos negativos coincidentes: `{NEGATIVE_EXPECTED}/{NEGATIVE_EXPECTED}`;\n"
        "- estado: `passed`.\n\n"
        "#### Alcance\n\n"
        "El resultado demuestra conformidad acotada para los escenarios, la seed y las mutaciones declaradas. "
        "No constituye una prueba general de refinamiento, equivalencia total ni verificacion completa de la implementacion.\n",
        encoding="utf-8",
    )


def parseArguments() -> argparse.Namespace:
    """Define la interfaz de linea de comandos."""
    parser = argparse.ArgumentParser(
        description="Integra resultados validos y negativos de conformidad."
    )
    parser.add_argument("--valid-manifest", type=Path, required=True)
    parser.add_argument("--negative-manifest", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--seed", type=int, required=True)
    parser.add_argument("--release", required=True)
    parser.add_argument("--tla-version", required=True)
    parser.add_argument("--tla-sha1", required=True)
    parser.add_argument("--source-commit", required=True)
    parser.add_argument("--checked-out-commit", required=True)
    parser.add_argument("--source-ref", required=True)
    parser.add_argument("--event-name", required=True)
    parser.add_argument("--run-id", default="")
    parser.add_argument("--run-attempt", default="")
    parser.add_argument("--repository", required=True)
    return parser.parse_args()


def main() -> int:
    """Valida, integra y publica el manifiesto cientifico de Fase 7E."""
    args = parseArguments()
    valid_manifest = args.valid_manifest.resolve()
    negative_manifest = args.negative_manifest.resolve()
    output_dir = args.output_dir.resolve()
    release = requireText(args.release, "La release es obligatoria.")
    source_commit = requireText(args.source_commit, "El commit fuente es obligatorio.")
    checked_out_commit = requireText(args.checked_out_commit, "El commit ejecutado es obligatorio.")
    require(COMMIT_PATTERN.fullmatch(source_commit) is not None, "El commit fuente debe ser SHA-1 completo.")
    require(COMMIT_PATTERN.fullmatch(checked_out_commit) is not None, "El commit ejecutado debe ser SHA-1 completo.")
    require(re.fullmatch(r"[0-9a-f]{40}", args.tla_sha1) is not None, "El SHA-1 de TLC es invalido.")

    valid_rows = validateValidRows(readRows(valid_manifest), args.seed)
    negative_rows = validateNegativeRows(readRows(negative_manifest), args.seed)
    combined = valid_rows + negative_rows
    output_dir.mkdir(parents=True, exist_ok=True)

    counts = {
        "diagnostics_matching": sum(1 for row in negative_rows if row["diagnostic_matches"]),
        "negative_rejected": sum(1 for row in negative_rows if not row["accepted"]),
        "negative_total": len(negative_rows),
        "total_cases": len(combined),
        "valid_accepted": sum(1 for row in valid_rows if row["accepted"]),
        "valid_total": len(valid_rows),
    }
    manifest = {
        "checked_out_commit": checked_out_commit,
        "claims": {
            "allowed": "bounded implementation-model trace conformance",
            "excluded": [
                "equivalencia total Java-TLA+",
                "prueba general de refinamiento",
                "verificacion completa de la implementacion",
            ],
        },
        "counts": counts,
        "event_name": requireText(args.event_name, "El evento de ejecucion es obligatorio."),
        "github_run_attempt": args.run_attempt,
        "github_run_id": args.run_id,
        "phase": "7E",
        "release": release,
        "repository": requireText(args.repository, "El repositorio es obligatorio."),
        "runs": combined,
        "schema_version": 1,
        "seed": args.seed,
        "source_commit": source_commit,
        "source_manifests": {
            "negative": {"path": "negative/manifest.csv", "sha256": sha256File(negative_manifest)},
            "valid": {"path": "valid/manifest.csv", "sha256": sha256File(valid_manifest)},
        },
        "source_ref": requireText(args.source_ref, "La referencia fuente es obligatoria."),
        "status": "passed",
        "toolchain": {
            "tla_tools_sha1": args.tla_sha1,
            "tla_tools_version": requireText(args.tla_version, "La version de TLC es obligatoria."),
        },
        "verification_scope": {
            "bounded": True,
            "negative_mutations": NEGATIVE_EXPECTED,
            "valid_scenarios": VALID_EXPECTED,
        },
    }
    summary = {
        "bounded": True,
        "counts": counts,
        "phase": "7E",
        "release": release,
        "seed": args.seed,
        "source_commit": source_commit,
        "status": "passed",
    }

    writeJson(output_dir / "manifest.json", manifest)
    writeJson(output_dir / "summary.json", summary)
    writeMatrix(output_dir / "conformance_matrix.csv", combined)
    writeSummary(output_dir / "summary.md", release, args.seed)
    print("El manifiesto cientifico de conformidad fue generado correctamente.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
