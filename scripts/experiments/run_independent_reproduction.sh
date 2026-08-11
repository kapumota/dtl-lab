#!/usr/bin/env bash
set -euo pipefail
export PYTHONDONTWRITEBYTECODE=1

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BUNDLE_PATH="${1:-}"
OUTPUT_DIR="${2:-$ROOT_DIR/results/experiments/reproduction/paper1-q3-v1}"

if [[ -z "$BUNDLE_PATH" ]]; then
  echo "Uso: run_independent_reproduction.sh <artefacto.tar.gz> [directorio-salida]." >&2
  exit 2
fi

BUNDLE_PATH="$(readlink -f "$BUNDLE_PATH")"
OUTPUT_DIR="$(readlink -m "$OUTPUT_DIR")"

if [[ ! -f "$BUNDLE_PATH" ]]; then
  echo "No existe el artefacto de reproducción: $BUNDLE_PATH." >&2
  exit 1
fi

if [[ -e "$OUTPUT_DIR" && -n "$(find "$OUTPUT_DIR" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "El directorio de salida debe estar vacío: $OUTPUT_DIR." >&2
  exit 1
fi

WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT
REPORT_DIR="$WORK_DIR/report"
mkdir -p "$REPORT_DIR/logs"
STEPS_FILE="$REPORT_DIR/steps.tsv"
printf 'step\tstatus\texit_code\n' > "$STEPS_FILE"
OVERALL_STATUS=0

runStep() {
  local step="$1"
  shift
  local stdout_path="$REPORT_DIR/logs/$step.stdout.txt"
  local stderr_path="$REPORT_DIR/logs/$step.stderr.txt"
  local exit_code

  set +e
  "$@" >"$stdout_path" 2>"$stderr_path"
  exit_code=$?
  set -e

  if [[ "$exit_code" -eq 0 ]]; then
    printf '%s\tcorrecto\t0\n' "$step" >> "$STEPS_FILE"
  else
    printf '%s\tfallo\t%s\n' "$step" "$exit_code" >> "$STEPS_FILE"
    OVERALL_STATUS=1
  fi
  return 0
}

runStep environment \
  python3 -B "$ROOT_DIR/scripts/experiments/check_reproduction_environment.py" \
    --repository-root "$ROOT_DIR" \
    --output "$REPORT_DIR/environment.json"

runStep bundle_verify \
  python3 -B "$ROOT_DIR/scripts/experiments/reproduction_bundle.py" \
    verify \
    --bundle "$BUNDLE_PATH" \
    --output-dir "$WORK_DIR/bundle"

BUNDLE_ROOT="$(
  find "$WORK_DIR/bundle" \
    -type f \
    -name bundle-manifest.json \
    -printf '%h\n' \
    2>/dev/null |
  head -1 || true
)"

if [[ -z "$BUNDLE_ROOT" ]]; then
  BUNDLE_ROOT="$WORK_DIR/bundle-invalido"
  mkdir -p \
    "$BUNDLE_ROOT/raw" \
    "$BUNDLE_ROOT/reference/derived" \
    "$BUNDLE_ROOT/reference/tables" \
    "$BUNDLE_ROOT/reference/figures"
  echo "No se pudo localizar el manifiesto del artefacto." \
    > "$REPORT_DIR/logs/source_commit.stderr.txt"
  : > "$REPORT_DIR/logs/source_commit.stdout.txt"
  printf 'source_commit\tfallo\t1\n' >> "$STEPS_FILE"
  OVERALL_STATUS=1
else
  set +e
  EXPECTED_COMMIT="$(
    python3 -B - \
      "$BUNDLE_ROOT/bundle-manifest.json" \
      2>"$REPORT_DIR/logs/source_commit.parse.stderr.txt" <<'PY'
import json
import sys
from pathlib import Path

manifest = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
print(manifest.get("source_commit", ""))
PY
  )"
  PARSE_STATUS=$?
  set -e
  ACTUAL_COMMIT="$(git -C "$ROOT_DIR" rev-parse HEAD)"

  if [[ "$PARSE_STATUS" -eq 0 && \
        -n "$EXPECTED_COMMIT" && \
        "$EXPECTED_COMMIT" == "$ACTUAL_COMMIT" ]]; then
    printf 'Commit esperado: %s\n' "$EXPECTED_COMMIT" \
      > "$REPORT_DIR/logs/source_commit.stdout.txt"
    : > "$REPORT_DIR/logs/source_commit.stderr.txt"
    printf 'source_commit\tcorrecto\t0\n' >> "$STEPS_FILE"
  else
    {
      echo "El commit del clon no coincide con el artefacto."
      echo "Esperado: $EXPECTED_COMMIT"
      echo "Actual: $ACTUAL_COMMIT"
    } > "$REPORT_DIR/logs/source_commit.stderr.txt"
    : > "$REPORT_DIR/logs/source_commit.stdout.txt"
    printf 'source_commit\tfallo\t1\n' >> "$STEPS_FILE"
    OVERALL_STATUS=1
  fi
fi

runStep install_tlc \
  bash "$ROOT_DIR/scripts/formal/install_tla_tools.sh"
runStep install_alloy \
  bash "$ROOT_DIR/scripts/formal/install_alloy.sh"
runStep validate \
  make -C "$ROOT_DIR" validate
runStep scientific_smoke \
  make -C "$ROOT_DIR" experiment-scientific-smoke

RAW_ARCHIVE="$BUNDLE_ROOT/raw/paper1-q3-v1.tar.gz"
runStep raw_extract \
  python3 -B "$ROOT_DIR/scripts/experiments/reproduction_bundle.py" \
    extract-raw \
    --archive "$RAW_ARCHIVE" \
    --output-dir "$WORK_DIR/raw"

RUN_DIR="$(
  find "$WORK_DIR/raw" \
    -type f \
    -name raw-manifest.json \
    -printf '%h\n' \
    2>/dev/null |
  head -1 || true
)"
CANDIDATE_ROOT="$WORK_DIR/candidate"
mkdir -p \
  "$CANDIDATE_ROOT/derived" \
  "$CANDIDATE_ROOT/tables" \
  "$CANDIDATE_ROOT/figures"

if [[ -n "$RUN_DIR" ]]; then
  runStep regenerate_analysis \
    bash "$ROOT_DIR/scripts/experiments/run_experiment_analysis.sh" \
      "$RUN_DIR" \
      "$CANDIDATE_ROOT/derived" \
      "$CANDIDATE_ROOT/tables" \
      "$CANDIDATE_ROOT/figures"
else
  echo "No se encontró raw-manifest.json después de extraer el respaldo." \
    > "$REPORT_DIR/logs/regenerate_analysis.stderr.txt"
  : > "$REPORT_DIR/logs/regenerate_analysis.stdout.txt"
  printf 'regenerate_analysis\tfallo\t1\n' >> "$STEPS_FILE"
  OVERALL_STATUS=1
fi

runStep compare_hashes \
  python3 -B "$ROOT_DIR/scripts/experiments/compare_reproduction_outputs.py" \
    --reference-root "$BUNDLE_ROOT/reference" \
    --candidate-root "$CANDIDATE_ROOT" \
    --output-dir "$REPORT_DIR/comparison"

cp -a "$CANDIDATE_ROOT" "$REPORT_DIR/regenerated"
if [[ -f "$BUNDLE_ROOT/bundle-manifest.json" ]]; then
  cp "$BUNDLE_ROOT/bundle-manifest.json" "$REPORT_DIR/bundle-manifest.json"
fi

set +e
python3 -B - \
  "$REPORT_DIR" \
  "$STEPS_FILE" \
  "$OVERALL_STATUS" <<'PY'
import csv
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

output_dir = Path(sys.argv[1])
steps_path = Path(sys.argv[2])
overall_status = int(sys.argv[3])

with steps_path.open(encoding="utf-8", newline="") as stream:
    steps = list(csv.DictReader(stream, delimiter="\t"))

environment_path = output_dir / "environment.json"
environment = (
    json.loads(environment_path.read_text(encoding="utf-8"))
    if environment_path.is_file()
    else {}
)
comparison_path = output_dir / "comparison/comparison.json"
comparison = (
    json.loads(comparison_path.read_text(encoding="utf-8"))
    if comparison_path.is_file()
    else {"status": "no_disponible", "different": None}
)

incidents = []
for step in steps:
    if step["status"] != "correcto":
        incidents.append(
            {
                "severity": "error",
                "category": "ejecución",
                "step": step["step"],
                "description": "El paso terminó con un código distinto de cero.",
                "resolution": "Revisar stdout y stderr del paso antes de repetir.",
            }
        )
for warning in environment.get("warnings", []):
    incidents.append(
        {
            "severity": "advertencia",
            "category": "ambiente",
            "step": "environment",
            "description": warning,
            "resolution": "Conservar la advertencia como limitación de reproducción.",
        }
    )

incidents_path = output_dir / "incidents.csv"
with incidents_path.open("w", encoding="utf-8", newline="") as stream:
    writer = csv.DictWriter(
        stream,
        fieldnames=[
            "severity",
            "category",
            "step",
            "description",
            "resolution",
        ],
        lineterminator="\n",
    )
    writer.writeheader()
    writer.writerows(incidents)

passed = all(step["status"] == "correcto" for step in steps)
status = (
    "reproducido"
    if passed and comparison.get("status") == "coincide" and overall_status == 0
    else "fallo"
)
report = {
    "schema_version": 1,
    "phase": "8E",
    "protocol_id": "paper1-q3-v1",
    "status": status,
    "completed_at_utc": datetime.now(timezone.utc).isoformat(),
    "environment_status": environment.get("status"),
    "source_commit": environment.get("source_commit"),
    "steps": steps,
    "comparison": comparison,
    "incidents_total": len(incidents),
    "full_matrix_rerun": False,
    "scientific_smoke_executed": any(
        step["step"] == "scientific_smoke" and step["status"] == "correcto"
        for step in steps
    ),
    "analysis_regenerated_from_raw": any(
        step["step"] == "regenerate_analysis" and step["status"] == "correcto"
        for step in steps
    ),
}
(output_dir / "reproduction-report.json").write_text(
    json.dumps(report, ensure_ascii=False, indent=2, sort_keys=True) + "\n",
    encoding="utf-8",
    newline="\n",
)

lines = [
    "### Informe de reproducción independiente",
    "",
    "#### Resultado",
    "",
    f"Estado: `{status}`.",
    "",
    "#### Pasos",
    "",
    "| Paso | Estado | Código |",
    "| --- | --- | --- |",
]
for step in steps:
    lines.append(
        f"| {step['step']} | {step['status']} | {step['exit_code']} |"
    )
lines.extend(
    [
        "",
        "#### Comparación",
        "",
        f"Estado: `{comparison.get('status')}`.",
        "",
        f"Archivos diferentes: {comparison.get('different')}.",
        "",
        "#### Incidencias",
        "",
        f"Incidencias registradas: {len(incidents)}.",
        "",
    ]
)
(output_dir / "reproduction-report.md").write_text(
    "\n".join(lines),
    encoding="utf-8",
    newline="\n",
)

if status != "reproducido":
    raise SystemExit("La reproducción independiente no cumplió todos los gates.")
PY
REPORT_STATUS=$?
set -e

mkdir -p "$OUTPUT_DIR"
cp -a "$REPORT_DIR/." "$OUTPUT_DIR/"

if [[ "$REPORT_STATUS" -ne 0 ]]; then
  echo "La reproducción independiente terminó con incidencias." >&2
  echo "Informe: $OUTPUT_DIR/reproduction-report.md" >&2
  exit 1
fi

echo "La reproducción independiente terminó correctamente."
echo "Informe: $OUTPUT_DIR/reproduction-report.md"
