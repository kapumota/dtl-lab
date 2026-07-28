#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TEMP_DIR"' EXIT

require_file() {
  local path="$1"
  if [[ ! -f "$path" ]]; then
    echo "Falta el archivo obligatorio: $path." >&2
    exit 1
  fi
}

require_text() {
  local path="$1"
  local text="$2"
  if ! grep -Fq -- "$text" "$path"; then
    echo "Falta el texto obligatorio '$text' en $path." >&2
    exit 1
  fi
}

require_file "$ROOT_DIR/scripts/conformance/build_conformance_manifest.py"
require_file "$ROOT_DIR/scripts/conformance/run_conformance_research.sh"
require_file "$ROOT_DIR/docs/research/paper1/CIERRE_CONFORMIDAD_FASE_7.md"
require_file "$ROOT_DIR/.github/workflows/formal-verification.yml"

require_text "$ROOT_DIR/Makefile" "conformance-research:"
require_text "$ROOT_DIR/.github/workflows/formal-verification.yml" "make conformance-research"
require_text "$ROOT_DIR/.github/workflows/formal-verification.yml" "dtl-lab-conformance-results"
require_text "$ROOT_DIR/docs/research/paper1/CIERRE_CONFORMIDAD_FASE_7.md" "bounded implementation-model trace conformance"
require_text "$ROOT_DIR/scripts/conformance/run_conformance_research.sh" "v1.1.0-rc.1"

python3 -B - "$TEMP_DIR" <<'__SAMPLE_DATA__'
import csv
import sys
from pathlib import Path

root = Path(sys.argv[1])
valid = root / "valid.csv"
negative = root / "negative.csv"

valid_fields = [
    "scenario_id", "seed", "accepted", "exit_code",
    "checked_abstract_steps", "rejected_abstract_step",
    "rejected_concrete_step", "rejected_action", "transfer_id",
    "message", "module", "config", "stdout", "stderr",
]
with valid.open("w", encoding="utf-8", newline="") as stream:
    writer = csv.DictWriter(stream, fieldnames=valid_fields, lineterminator="\n")
    writer.writeheader()
    for index in range(1, 11):
        writer.writerow(
            {
                "scenario_id": f"S{index:02d}_PRUEBA",
                "seed": 2026,
                "accepted": "true",
                "exit_code": 0,
                "checked_abstract_steps": index,
                "message": "Aceptada.",
                "module": "modulo.tla",
                "config": "modulo.cfg",
                "stdout": "stdout.txt",
                "stderr": "stderr.txt",
            }
        )

negative_fields = [
    "mutation_id", "source_scenario", "seed", "expected_property",
    "description", "expected_abstract_step", "expected_concrete_step",
    "expected_action", "expected_transfer_id", "accepted",
    "diagnostic_matches", "exit_code", "checked_abstract_steps",
    "rejected_abstract_step", "rejected_concrete_step",
    "rejected_action", "transfer_id", "message", "module", "config",
    "stdout", "stderr",
]
with negative.open("w", encoding="utf-8", newline="") as stream:
    writer = csv.DictWriter(stream, fieldnames=negative_fields, lineterminator="\n")
    writer.writeheader()
    for index in range(1, 11):
        step = index - 1
        writer.writerow(
            {
                "mutation_id": f"M{index:02d}_PRUEBA",
                "source_scenario": "S01_PRUEBA",
                "seed": 2026,
                "expected_property": "PropiedadPrueba",
                "description": "Mutacion de prueba.",
                "expected_abstract_step": step,
                "expected_concrete_step": step,
                "expected_action": "Stutter",
                "expected_transfer_id": "transferencia-prueba",
                "accepted": "false",
                "diagnostic_matches": "true",
                "exit_code": 12,
                "checked_abstract_steps": step,
                "rejected_abstract_step": step,
                "rejected_concrete_step": step,
                "rejected_action": "Stutter",
                "transfer_id": "transferencia-prueba",
                "message": "Rechazada.",
                "module": "modulo.tla",
                "config": "modulo.cfg",
                "stdout": "stdout.txt",
                "stderr": "stderr.txt",
            }
        )
__SAMPLE_DATA__

python3 -B "$ROOT_DIR/scripts/conformance/build_conformance_manifest.py" \
  --valid-manifest "$TEMP_DIR/valid.csv" \
  --negative-manifest "$TEMP_DIR/negative.csv" \
  --output-dir "$TEMP_DIR/output" \
  --seed 2026 \
  --release "v1.1.0-rc.1" \
  --tla-version "1.7.4" \
  --tla-sha1 "0000000000000000000000000000000000000000" \
  --source-commit "0000000000000000000000000000000000000000" \
  --checked-out-commit "0000000000000000000000000000000000000000" \
  --source-ref "prueba-local" \
  --event-name "local" \
  --repository "kapumota/dtl-lab" \
  >/dev/null

python3 -B - "$TEMP_DIR/output/manifest.json" <<'__VALIDATE_MANIFEST__'
import json
import sys
from pathlib import Path

manifest = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
if manifest.get("schema_version") != 1:
    raise SystemExit("El manifiesto de conformidad debe usar schema_version 1.")
if manifest.get("phase") != "7E":
    raise SystemExit("El manifiesto de conformidad debe declarar Fase 7E.")
if manifest.get("status") != "passed":
    raise SystemExit("El manifiesto estructural debe terminar en passed.")
counts = manifest.get("counts", {})
if counts.get("valid_accepted") != 10:
    raise SystemExit("El manifiesto debe registrar diez escenarios validos.")
if counts.get("negative_rejected") != 10:
    raise SystemExit("El manifiesto debe registrar diez mutaciones rechazadas.")
if counts.get("diagnostics_matching") != 10:
    raise SystemExit("El manifiesto debe registrar diez diagnosticos coincidentes.")
__VALIDATE_MANIFEST__

echo "La validacion estructural de conformidad paso correctamente."
