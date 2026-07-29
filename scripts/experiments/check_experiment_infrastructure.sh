#!/usr/bin/env bash
set -euo pipefail
export PYTHONDONTWRITEBYTECODE=1

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

require_blob() {
  local path="$1"
  local expected="$2"
  local actual
  actual="$(git -C "$ROOT_DIR" hash-object "$ROOT_DIR/$path")"
  if [[ "$actual" != "$expected" ]]; then
    echo "El archivo congelado cambió: $path." >&2
    exit 1
  fi
}

require_file "$ROOT_DIR/experiments/paper1/experiment-spec.json"
require_file "$ROOT_DIR/experiments/paper1/configurations.csv"
require_file "$ROOT_DIR/experiments/paper1/seeds.txt"
require_file "$ROOT_DIR/experiments/paper1/cases.json"
require_file "$ROOT_DIR/experiments/paper1/result-schema-v1.json"
require_file "$ROOT_DIR/docs/research/paper1/PROTOCOLO_EXPERIMENTAL_Q3.md"
require_file "$ROOT_DIR/docs/research/paper1/INFRAESTRUCTURA_EXPERIMENTAL.md"
require_file "$ROOT_DIR/scripts/experiments/experiment_io.py"
require_file "$ROOT_DIR/scripts/experiments/build_experiment_plan.py"
require_file "$ROOT_DIR/scripts/experiments/collect_environment.py"
require_file "$ROOT_DIR/scripts/experiments/run_experiment_matrix.py"
require_file "$ROOT_DIR/scripts/experiments/mock_experiment_executor.py"
require_file "$ROOT_DIR/scripts/experiments/validate_experiment_run.py"
require_file "$ROOT_DIR/results/experiments/README.md"

require_blob \
  "experiments/paper1/experiment-spec.json" \
  "e9f37bbccdfe59976340edbef52875b0da200ae2"
require_blob \
  "experiments/paper1/configurations.csv" \
  "4f89c055e14d0aa4306db42cbe5afbf493cf9bc0"
require_blob \
  "experiments/paper1/seeds.txt" \
  "3dccbd9cff45d88592766293d96b2d63b5bac1e3"
require_blob \
  "docs/research/paper1/PROTOCOLO_EXPERIMENTAL_Q3.md" \
  "aaa7931d8c3f92b51872cc3dcbcca848ced9e2f9"

require_text "$ROOT_DIR/Makefile" "experiment-infrastructure:"
require_text "$ROOT_DIR/scripts/run_tests.sh" "check_experiment_infrastructure.sh"
require_text \
  "$ROOT_DIR/docs/research/paper1/INFRAESTRUCTURA_EXPERIMENTAL.md" \
  "La Fase 8B no ejecuta la matriz definitiva."
require_text \
  "$ROOT_DIR/docs/research/paper1/INFRAESTRUCTURA_EXPERIMENTAL.md" \
  "1272 tareas"

bash "$ROOT_DIR/scripts/experiments/check_experimental_structure.sh"

python3 -B "$ROOT_DIR/scripts/experiments/build_experiment_plan.py" \
  --spec "$ROOT_DIR/experiments/paper1/experiment-spec.json" \
  --configurations "$ROOT_DIR/experiments/paper1/configurations.csv" \
  --seeds "$ROOT_DIR/experiments/paper1/seeds.txt" \
  --cases "$ROOT_DIR/experiments/paper1/cases.json" \
  --output-plan "$TEMP_DIR/plan.jsonl" \
  --output-manifest "$TEMP_DIR/plan-manifest.json"

python3 -B - "$TEMP_DIR/plan-manifest.json" <<'__CHECK_PLAN__'
import json
import sys
from pathlib import Path

manifest = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
counts = manifest.get("counts", {})
if counts.get("tasks_total") != 1272:
    raise SystemExit("El plan debe contener 1272 tareas.")
if counts.get("warmup_tasks") != 112:
    raise SystemExit("El plan debe contener 112 calentamientos.")
if counts.get("measured_tasks") != 1160:
    raise SystemExit("El plan debe contener 1160 tareas medidas.")
if manifest.get("definitive_results_generated") is not False:
    raise SystemExit("El plan no debe declarar resultados definitivos.")
__CHECK_PLAN__

python3 -B "$ROOT_DIR/scripts/experiments/run_experiment_matrix.py" \
  --repository-root "$ROOT_DIR" \
  --plan "$TEMP_DIR/plan.jsonl" \
  --manifest "$TEMP_DIR/plan-manifest.json" \
  --run-dir "$TEMP_DIR/dry-run" \
  --executor "$ROOT_DIR/scripts/experiments/mock_experiment_executor.py" \
  --dry-run

python3 -B "$ROOT_DIR/scripts/experiments/run_experiment_matrix.py" \
  --repository-root "$ROOT_DIR" \
  --plan "$TEMP_DIR/plan.jsonl" \
  --manifest "$TEMP_DIR/plan-manifest.json" \
  --run-dir "$TEMP_DIR/smoke-run" \
  --executor "$ROOT_DIR/scripts/experiments/mock_experiment_executor.py" \
  --smoke \
  --limit 2 \
  >"$TEMP_DIR/first-run.txt"

python3 -B "$ROOT_DIR/scripts/experiments/run_experiment_matrix.py" \
  --repository-root "$ROOT_DIR" \
  --plan "$TEMP_DIR/plan.jsonl" \
  --manifest "$TEMP_DIR/plan-manifest.json" \
  --run-dir "$TEMP_DIR/smoke-run" \
  --executor "$ROOT_DIR/scripts/experiments/mock_experiment_executor.py" \
  --smoke \
  --limit 2 \
  >"$TEMP_DIR/second-run.txt"

grep -Fq "Ejecutadas=2, reanudadas=0." "$TEMP_DIR/first-run.txt"
grep -Fq "Ejecutadas=0, reanudadas=2." "$TEMP_DIR/second-run.txt"

python3 -B "$ROOT_DIR/scripts/experiments/validate_experiment_run.py" \
  --run-dir "$TEMP_DIR/smoke-run" \
  --allow-partial \
  --expect-completed 2 \
  --expect-run-kind infrastructure_smoke

python3 -B - "$ROOT_DIR" <<'__CHECK_STYLE__'
import sys
import unicodedata
from pathlib import Path

root = Path(sys.argv[1])
paths = [
    root / "docs/research/paper1/INFRAESTRUCTURA_EXPERIMENTAL.md",
    root / "experiments/paper1/README.md",
    root / "results/experiments/README.md",
]
errors = []

for path in paths:
    text = path.read_text(encoding="utf-8")
    if "\u2013" in text or "\u2014" in text:
        errors.append(f"{path}: contiene guiones tipográficos.")
    if "=" * 8 in text:
        errors.append(f"{path}: contiene un separador no permitido.")

    inside_code = False
    for line_number, line in enumerate(text.splitlines(), start=1):
        if line.lstrip().startswith("```"):
            inside_code = not inside_code
            continue
        if not inside_code and line.startswith("#"):
            level = len(line) - len(line.lstrip("#"))
            if level not in (3, 4):
                errors.append(
                    f"{path}:{line_number}: título de nivel {level}."
                )

    for character in text:
        category = unicodedata.category(character)
        if category == "So":
            errors.append(
                f"{path}: contiene un símbolo no permitido."
            )
            break

if errors:
    raise SystemExit("\n".join(errors))
__CHECK_STYLE__

if find "$ROOT_DIR/results/experiments" \
  -mindepth 1 \
  -maxdepth 1 \
  ! -name README.md \
  -print -quit |
  grep -q .; then
  echo "La Fase 8B no debe almacenar resultados definitivos." >&2
  exit 1
fi

echo "La infraestructura experimental de Fase 8B pasó correctamente."
