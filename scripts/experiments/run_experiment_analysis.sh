#!/usr/bin/env bash
set -euo pipefail
export PYTHONDONTWRITEBYTECODE=1

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
RUN_DIR="${1:-$ROOT_DIR/results/experiments/raw/paper1-q3-v1}"
DERIVED_DIR="${2:-$ROOT_DIR/results/experiments/derived/paper1-q3-v1}"
TABLES_DIR="${3:-$ROOT_DIR/results/experiments/tables/paper1-q3-v1}"
FIGURES_DIR="${4:-$ROOT_DIR/results/experiments/figures/paper1-q3-v1}"

python3 -B "$ROOT_DIR/scripts/experiments/validate_experiment_run.py" \
  --run-dir "$RUN_DIR" \
  --expect-run-kind definitive

python3 -B "$ROOT_DIR/scripts/experiments/build_experiment_analysis.py" \
  --run-dir "$RUN_DIR" \
  --experiment-spec "$ROOT_DIR/experiments/paper1/experiment-spec.json" \
  --analysis-spec "$ROOT_DIR/experiments/paper1/analysis-spec.json" \
  --derived-dir "$DERIVED_DIR" \
  --tables-dir "$TABLES_DIR" \
  --figures-dir "$FIGURES_DIR" \
  --expect-tasks 1272

python3 -B - "$DERIVED_DIR/derived-manifest.json" <<'PY'
import json
import sys
from pathlib import Path

path = Path(sys.argv[1])
manifest = json.loads(path.read_text(encoding="utf-8"))
if manifest.get("phase") != "8D":
    raise SystemExit("El manifiesto derivado no declara Fase 8D.")
if manifest.get("status") != "complete":
    raise SystemExit("El análisis derivado no terminó correctamente.")
if manifest.get("raw_results_modified") is not False:
    raise SystemExit("El manifiesto no conserva la inmutabilidad de raw.")
counts = manifest.get("counts", {})
if counts.get("task_rows") != 1272:
    raise SystemExit("El análisis no contiene las 1272 tareas.")
if counts.get("tables") != 8 or counts.get("figures") != 8:
    raise SystemExit("Faltan tablas o figuras previstas.")
PY

echo "La Fase 8D terminó correctamente."
