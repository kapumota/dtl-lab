#!/usr/bin/env python3
"""Valida el protocolo experimental congelado de la Fase 8A."""

from __future__ import annotations

import argparse
import csv
import json
from pathlib import Path
from typing import Any


EXPECTED_BASELINE = "45cb114d61b1df8c605c50700f3cc72d48d157fe"
EXPECTED_RELEASE = "v1.1.0-rc.1"
EXPECTED_RQS = {"RQ1", "RQ2", "RQ3", "RQ4"}
EXPECTED_HYPOTHESES = {"H1", "H2", "H3", "H4"}
EXPECTED_SEEDS = list(range(2026001, 2026031))
EXPECTED_CONFIGURATIONS = {
    "RQ1R4-TLC-SMALL",
    "RQ1R4-TLC-MEDIUM",
    "RQ1R4-TLC-LARGE",
    "RQ1R4-ALLOY-SMALL",
    "RQ1R4-ALLOY-MEDIUM",
    "RQ1R4-ALLOY-LARGE",
    "RQ2-TLC-MUTANTS",
    "RQ2-ALLOY-MUTANTS",
    "RQ3-TLC-VALID",
    "RQ3-TLC-NEGATIVE",
    "RQ4-TLC-FAULT-NORMAL",
    "RQ4-TLC-FAULT-REPLAY",
    "RQ4-TLC-FAULT-TIMEOUT",
    "RQ4-TLC-FAULT-QUORUM",
}
REQUIRED_COLUMNS = {
    "configuration_id",
    "rq_ids",
    "tool",
    "model_kind",
    "bound_profile",
    "fault_profile",
    "case_type",
    "seed_policy",
    "warmup_repetitions",
    "measured_repetitions",
    "timeout_seconds",
    "max_rss_mb",
    "notes",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Valida la especificacion experimental de la Fase 8A."
    )
    parser.add_argument("--spec", required=True)
    parser.add_argument("--configurations", required=True)
    parser.add_argument("--seeds", required=True)
    return parser.parse_args()


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


def load_seeds(path: Path) -> list[int]:
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except FileNotFoundError as error:
        raise SystemExit(f"Falta el archivo de seeds: {path}.") from error

    seeds: list[int] = []
    for line_number, raw_line in enumerate(lines, start=1):
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        try:
            seeds.append(int(line))
        except ValueError as error:
            raise SystemExit(
                f"Seed invalida en {path}:{line_number}: {line}."
            ) from error
    return seeds


def load_configurations(path: Path) -> list[dict[str, str]]:
    try:
        with path.open(encoding="utf-8", newline="") as stream:
            reader = csv.DictReader(stream)
            columns = set(reader.fieldnames or [])
            missing = REQUIRED_COLUMNS.difference(columns)
            if missing:
                raise SystemExit(
                    f"Faltan columnas en {path}: {sorted(missing)}."
                )
            return list(reader)
    except FileNotFoundError as error:
        raise SystemExit(f"Falta la matriz de configuraciones: {path}.") from error


def require_equal(actual: Any, expected: Any, message: str) -> None:
    if actual != expected:
        raise SystemExit(f"{message} Esperado={expected!r}, actual={actual!r}.")


def require_positive_integer(value: Any, message: str) -> int:
    if not isinstance(value, int) or isinstance(value, bool) or value <= 0:
        raise SystemExit(message)
    return value


def validate_research_questions(spec: dict[str, Any]) -> None:
    questions = spec.get("research_questions")
    if not isinstance(questions, list):
        raise SystemExit("research_questions debe ser una lista.")

    question_ids = {item.get("id") for item in questions if isinstance(item, dict)}
    require_equal(question_ids, EXPECTED_RQS, "Las RQ no coinciden.")

    mapping = {
        item.get("id"): item.get("hypothesis")
        for item in questions
        if isinstance(item, dict)
    }
    require_equal(
        mapping,
        {"RQ1": "H1", "RQ2": "H2", "RQ3": "H3", "RQ4": "H4"},
        "El mapeo entre RQ e hipotesis no coincide.",
    )

    hypotheses = spec.get("hypotheses")
    if not isinstance(hypotheses, list):
        raise SystemExit("hypotheses debe ser una lista.")

    hypothesis_ids = {
        item.get("id") for item in hypotheses if isinstance(item, dict)
    }
    require_equal(
        hypothesis_ids,
        EXPECTED_HYPOTHESES,
        "Las hipotesis no coinciden.",
    )
    for item in hypotheses:
        if not isinstance(item, dict):
            raise SystemExit("Cada hipotesis debe ser un objeto.")
        require_equal(
            item.get("status"),
            "to_be_tested",
            f"La hipotesis {item.get('id')} no debe estar confirmada.",
        )
        statement = item.get("statement")
        if not isinstance(statement, str) or not statement.strip():
            raise SystemExit(
                f"La hipotesis {item.get('id')} necesita una declaracion."
            )


def validate_tools(spec: dict[str, Any]) -> None:
    tools = spec.get("tools")
    if not isinstance(tools, dict):
        raise SystemExit("tools debe ser un objeto.")

    require_equal(tools.get("tlc"), {"version": "1.7.4", "workers": 1}, "TLC no coincide.")
    require_equal(
        tools.get("alloy"),
        {"version": "6.2.0", "solver": "sat4j"},
        "Alloy no coincide.",
    )
    require_equal(tools.get("java"), {"major": 17}, "Java no coincide.")
    require_equal(
        tools.get("python"),
        {"major_minor": "3.12"},
        "Python no coincide.",
    )
    require_equal(
        tools.get("time_command"),
        "/usr/bin/time -v",
        "El comando de medicion no coincide.",
    )


def validate_factors(spec: dict[str, Any]) -> None:
    factors = spec.get("factors")
    if not isinstance(factors, dict):
        raise SystemExit("factors debe ser un objeto.")

    expected = {
        "tool": ["tlc", "alloy"],
        "model_kind": ["valid", "mutant"],
        "shards": [2, 3, 4],
        "concurrent_transfers": [1, 2, 3],
        "validators": [2, 3, 4],
        "receipt_copies": [1, 2],
        "bound_profile": ["catalog", "small", "medium", "large"],
        "tlc_fault_profile": [
            "normal",
            "replay",
            "timeout",
            "insufficient_quorum",
        ],
        "trace_case_type": ["valid", "negative"],
    }
    require_equal(factors, expected, "Los factores o niveles no coinciden.")

    profiles = spec.get("bound_profiles")
    if not isinstance(profiles, dict):
        raise SystemExit("bound_profiles debe ser un objeto.")
    require_equal(set(profiles), {"catalog", "small", "medium", "large"}, "Los perfiles no coinciden.")
    require_equal(profiles["small"]["shards"], 2, "El perfil small no coincide.")
    require_equal(profiles["medium"]["concurrent_transfers"], 2, "El perfil medium no coincide.")
    require_equal(profiles["large"]["alloy_state_scope"], 10, "El perfil large no coincide.")


def validate_sampling_and_resources(spec: dict[str, Any]) -> None:
    sampling = spec.get("sampling")
    if not isinstance(sampling, dict):
        raise SystemExit("sampling debe ser un objeto.")
    require_equal(sampling.get("seed_count"), 30, "La cantidad de seeds no coincide.")
    require_equal(sampling.get("smoke_seed"), 2026, "La seed smoke no coincide.")
    require_equal(sampling.get("warmup_repetitions"), 2, "Los calentamientos no coinciden.")
    require_equal(sampling.get("measured_repetitions"), 10, "Las repeticiones no coinciden.")
    require_equal(
        sampling.get("rq3_repetitions_per_case_seed"),
        1,
        "Las repeticiones de RQ3 no coinciden.",
    )

    resources = spec.get("resources")
    if not isinstance(resources, dict):
        raise SystemExit("resources debe ser un objeto.")
    require_equal(resources.get("timeout_seconds"), 1800, "El timeout no coincide.")
    require_equal(resources.get("max_rss_mb"), 12288, "La memoria no coincide.")
    require_equal(resources.get("max_parallel_runs"), 1, "El paralelismo no coincide.")
    require_equal(resources.get("timing_host"), "dedicated_native_linux", "El host no coincide.")
    require_equal(resources.get("ci_timing_allowed"), False, "CI no debe usarse para tiempos.")
    require_equal(
        resources.get("virtualized_timing_allowed"),
        False,
        "La virtualizacion no debe usarse para tiempos.",
    )
    require_equal(resources.get("tlc_workers"), 1, "TLC debe usar un worker.")

    incomplete = spec.get("incomplete_runs")
    if not isinstance(incomplete, dict):
        raise SystemExit("incomplete_runs debe ser un objeto.")
    require_equal(
        incomplete.get("instrumentation_retry_limit"),
        1,
        "El limite de reintento no coincide.",
    )
    require_equal(
        incomplete.get("result_based_retry_allowed"),
        False,
        "No se permiten reintentos por resultado.",
    )

    statistics = spec.get("statistics")
    if not isinstance(statistics, dict):
        raise SystemExit("statistics debe ser un objeto.")
    require_equal(statistics.get("location"), "median", "La medida central no coincide.")
    require_equal(
        statistics.get("dispersion"),
        "interquartile_range",
        "La dispersion no coincide.",
    )
    require_equal(
        statistics.get("bootstrap_resamples"),
        10000,
        "Las remuestras bootstrap no coinciden.",
    )
    require_equal(
        statistics.get("cross_tool_absolute_speed_claim_allowed"),
        False,
        "No se permite reclamar velocidad absoluta entre herramientas.",
    )


def validate_configurations(
    rows: list[dict[str, str]],
    spec: dict[str, Any],
) -> None:
    require_equal(len(rows), 14, "La matriz debe contener catorce configuraciones.")
    ids = [row["configuration_id"] for row in rows]
    require_equal(set(ids), EXPECTED_CONFIGURATIONS, "Los identificadores no coinciden.")
    if len(ids) != len(set(ids)):
        raise SystemExit("Hay identificadores de configuracion duplicados.")

    resources = spec["resources"]
    covered_rqs: set[str] = set()

    for row in rows:
        config_id = row["configuration_id"]
        rq_ids = set(row["rq_ids"].split(";"))
        if not rq_ids or not rq_ids.issubset(EXPECTED_RQS):
            raise SystemExit(f"RQ invalidas en {config_id}: {sorted(rq_ids)}.")
        covered_rqs.update(rq_ids)

        if row["tool"] not in {"tlc", "alloy"}:
            raise SystemExit(f"Herramienta invalida en {config_id}.")
        if row["model_kind"] not in {"valid", "mutant"}:
            raise SystemExit(f"Tipo de modelo invalido en {config_id}.")
        if row["bound_profile"] not in {"catalog", "small", "medium", "large"}:
            raise SystemExit(f"Perfil de bound invalido en {config_id}.")
        if row["case_type"] not in {"formal", "valid", "negative"}:
            raise SystemExit(f"Tipo de caso invalido en {config_id}.")

        try:
            warmups = int(row["warmup_repetitions"])
            measured = int(row["measured_repetitions"])
            timeout = int(row["timeout_seconds"])
            memory = int(row["max_rss_mb"])
        except ValueError as error:
            raise SystemExit(f"Valor numerico invalido en {config_id}.") from error

        require_equal(timeout, resources["timeout_seconds"], f"Timeout invalido en {config_id}.")
        require_equal(memory, resources["max_rss_mb"], f"Memoria invalida en {config_id}.")

        if "RQ3" in rq_ids:
            require_equal(row["tool"], "tlc", f"RQ3 debe usar TLC en {config_id}.")
            require_equal(row["seed_policy"], "multiseed-30", f"Seeds invalidas en {config_id}.")
            require_equal(warmups, 0, f"RQ3 no usa calentamiento en {config_id}.")
            require_equal(measured, 1, f"RQ3 usa una ejecucion por caso y seed en {config_id}.")
        else:
            require_equal(row["seed_policy"], "none", f"Seed policy invalida en {config_id}.")
            require_equal(warmups, 2, f"Calentamientos invalidos en {config_id}.")
            require_equal(measured, 10, f"Repeticiones invalidas en {config_id}.")

        if row["tool"] == "alloy" and row["fault_profile"] != "normal":
            raise SystemExit(
                f"Los perfiles de fallo de esta version solo son validos para TLC: {config_id}."
            )

    require_equal(covered_rqs, EXPECTED_RQS, "La matriz no cubre todas las RQ.")


def validate_outputs(spec: dict[str, Any]) -> None:
    require_equal(
        spec.get("definitive_results_generated"),
        False,
        "La Fase 8A no debe declarar resultados definitivos.",
    )
    planned = spec.get("planned_outputs")
    if not isinstance(planned, dict):
        raise SystemExit("planned_outputs debe ser un objeto.")
    require_equal(
        planned.get("valid_conformance_runs"),
        300,
        "Las trazas validas planificadas no coinciden.",
    )
    require_equal(
        planned.get("negative_conformance_runs"),
        300,
        "Las trazas negativas planificadas no coinciden.",
    )


def main() -> int:
    args = parse_args()
    spec_path = Path(args.spec)
    configurations_path = Path(args.configurations)
    seeds_path = Path(args.seeds)

    spec = load_json(spec_path)
    seeds = load_seeds(seeds_path)
    rows = load_configurations(configurations_path)

    require_equal(spec.get("schema_version"), 1, "schema_version no coincide.")
    require_equal(spec.get("phase"), "8A", "La fase no coincide.")
    require_equal(spec.get("protocol_id"), "paper1-q3-v1", "El protocolo no coincide.")
    require_equal(spec.get("status"), "frozen", "El protocolo no esta congelado.")
    require_equal(spec.get("baseline"), {"commit": EXPECTED_BASELINE, "release": EXPECTED_RELEASE}, "El baseline no coincide.")

    require_equal(seeds, EXPECTED_SEEDS, "Las seeds no coinciden.")
    if len(seeds) != len(set(seeds)):
        raise SystemExit("Las seeds deben ser unicas.")

    validate_research_questions(spec)
    validate_tools(spec)
    validate_factors(spec)
    validate_sampling_and_resources(spec)
    validate_configurations(rows, spec)
    validate_outputs(spec)

    print("El protocolo experimental Q3 es consistente.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
