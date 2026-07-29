#!/usr/bin/env bash
set -euo pipefail
export PYTHONDONTWRITEBYTECODE=1

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TEMP_DIR"' EXIT

snapshot_results() {
  python3 -B - "$ROOT_DIR/results/experiments" <<'PY'
import hashlib
import sys
from pathlib import Path

root = Path(sys.argv[1])
if not root.exists():
    raise SystemExit(0)

for path in sorted(root.rglob("*")):
    if not path.is_file():
        continue
    digest = hashlib.sha256(path.read_bytes()).hexdigest()
    print(f"{path.relative_to(root)}\t{digest}")
PY
}

RESULTS_SNAPSHOT_BEFORE="$(snapshot_results)"

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
require_blob \
  "experiments/paper1/cases.json" \
  "c2859f39a5d81ae45992df501e2dc1dd77bc5a19"
require_blob \
  "experiments/paper1/result-schema-v1.json" \
  "b659de53254b02b7770da74da4b84f6bbb153d24"
require_blob \
  "scripts/experiments/experiment_io.py" \
  "3a7c8531e21509e97002b7efe76672017a1b4428"
require_blob \
  "scripts/experiments/build_experiment_plan.py" \
  "a06ec250d6fac116cb1ab2b36cc22a34ff3f341a"

required_files=(
  "docs/research/paper1/EJECUCION_MATRIZ_EXPERIMENTAL.md"
  "experiments/paper1/execution-profiles.json"
  "scripts/experiments/build_scientific_smoke_plan.py"
  "scripts/experiments/check_timing_host.py"
  "scripts/experiments/prepare_scientific_runtime.py"
  "scripts/experiments/scientific_executor.py"
  "scripts/experiments/finalize_experiment_run.py"
  "scripts/experiments/run_scientific_matrix.sh"
  "scripts/experiments/check_scientific_matrix_structure.sh"
  "src/main/java/dltlab/conformance/ConformanceCaseRunner.java"
)

for path in "${required_files[@]}"; do
  require_file "$ROOT_DIR/$path"
done

require_text "$ROOT_DIR/Makefile" "experiment-scientific-structure:"
require_text "$ROOT_DIR/Makefile" "experiment-scientific-smoke:"
require_text "$ROOT_DIR/Makefile" "experiment-matrix:"
require_text "$ROOT_DIR/scripts/run_tests.sh" "check_scientific_matrix_structure.sh"
require_text \
  "$ROOT_DIR/.github/workflows/formal-verification.yml" \
  "make experiment-scientific-smoke"
require_text \
  "$ROOT_DIR/docs/research/paper1/EJECUCION_MATRIZ_EXPERIMENTAL.md" \
  "1272 tareas"
require_text \
  "$ROOT_DIR/docs/research/paper1/EJECUCION_MATRIZ_EXPERIMENTAL.md" \
  "La Fase 8C no genera resultados derivados."

bash "$ROOT_DIR/scripts/experiments/check_experimental_structure.sh"

if grep -Fq -- "experiment-infrastructure"   "$ROOT_DIR/scripts/experiments/run_scientific_matrix.sh"; then
  echo "La ejecución 8C no debe invocar el gate histórico completo de 8B." >&2
  exit 1
fi

require_text   "$ROOT_DIR/scripts/experiments/run_scientific_matrix.sh"   "check_experimental_structure.sh"

python3 -B "$ROOT_DIR/scripts/experiments/build_experiment_plan.py" \
  --spec "$ROOT_DIR/experiments/paper1/experiment-spec.json" \
  --configurations "$ROOT_DIR/experiments/paper1/configurations.csv" \
  --seeds "$ROOT_DIR/experiments/paper1/seeds.txt" \
  --cases "$ROOT_DIR/experiments/paper1/cases.json" \
  --output-plan "$TEMP_DIR/full-plan.jsonl" \
  --output-manifest "$TEMP_DIR/full-plan-manifest.json"

python3 -B "$ROOT_DIR/scripts/experiments/run_experiment_matrix.py" \
  --repository-root "$ROOT_DIR" \
  --plan "$TEMP_DIR/full-plan.jsonl" \
  --manifest "$TEMP_DIR/full-plan-manifest.json" \
  --run-dir "$TEMP_DIR/dry-run" \
  --executor "$ROOT_DIR/scripts/experiments/scientific_executor.py" \
  --dry-run

python3 -B "$ROOT_DIR/scripts/experiments/build_scientific_smoke_plan.py" \
  --plan "$TEMP_DIR/full-plan.jsonl" \
  --manifest "$TEMP_DIR/full-plan-manifest.json" \
  --profiles "$ROOT_DIR/experiments/paper1/execution-profiles.json" \
  --output-plan "$TEMP_DIR/smoke-plan.jsonl" \
  --output-manifest "$TEMP_DIR/smoke-plan-manifest.json"

python3 -B - "$TEMP_DIR/smoke-plan.jsonl" "$TEMP_DIR" "$ROOT_DIR" <<'PY'
import json
import subprocess
import sys
from pathlib import Path

plan_path = Path(sys.argv[1])
temp_dir = Path(sys.argv[2])
root = Path(sys.argv[3])
tasks = [
    json.loads(line)
    for line in plan_path.read_text(encoding="utf-8").splitlines()
    if line.strip()
]
if len(tasks) != 6:
    raise SystemExit("El plan smoke debe contener seis tareas.")

for index, task in enumerate(tasks, start=1):
    task_path = temp_dir / f"task-{index}.json"
    output_dir = temp_dir / f"mapping-{index}"
    task_path.write_text(
        json.dumps(task, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    completed = subprocess.run(
        [
            sys.executable,
            str(root / "scripts/experiments/scientific_executor.py"),
            "--task",
            str(task_path),
            "--output-dir",
            str(output_dir),
            "--validate-only",
        ],
        cwd=root,
        check=False,
    )
    if completed.returncode != 0:
        raise SystemExit(
            f"Falló el mapeo científico de {task['task_id']}."
        )
PY

python3 -B - "$ROOT_DIR" <<'PY'
import ast
import sys
import unicodedata
from pathlib import Path

root = Path(sys.argv[1])
python_paths = [
    root / "scripts/experiments/build_scientific_smoke_plan.py",
    root / "scripts/experiments/check_timing_host.py",
    root / "scripts/experiments/prepare_scientific_runtime.py",
    root / "scripts/experiments/scientific_executor.py",
    root / "scripts/experiments/finalize_experiment_run.py",
]
for path in python_paths:
    ast.parse(path.read_text(encoding="utf-8"), filename=str(path))

docs = [
    root / "docs/research/paper1/EJECUCION_MATRIZ_EXPERIMENTAL.md",
    root / "experiments/paper1/README.md",
    root / "results/experiments/README.md",
]
errors = []
for path in docs:
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
        if unicodedata.category(character) == "So":
            errors.append(f"{path}: contiene un símbolo no permitido.")
            break

if errors:
    raise SystemExit("\n".join(errors))
PY

bash -n "$ROOT_DIR/scripts/experiments/run_scientific_matrix.sh"
bash -n "$ROOT_DIR/scripts/experiments/check_scientific_matrix_structure.sh"

RESULTS_SNAPSHOT_AFTER="$(snapshot_results)"
if [[ "$RESULTS_SNAPSHOT_BEFORE" != "$RESULTS_SNAPSHOT_AFTER" ]]; then
  echo "El gate estructural modificó resultados científicos existentes." >&2
  exit 1
fi

echo "La estructura científica de Fase 8C pasó correctamente."
