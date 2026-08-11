#!/usr/bin/env bash
set -euo pipefail
export PYTHONDONTWRITEBYTECODE=1

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TEMP_DIR"' EXIT

snapshotResults() {
  python3 -B - "$ROOT_DIR/results/experiments" <<'PY'
import hashlib
import sys
from pathlib import Path

root = Path(sys.argv[1])
if not root.exists():
    raise SystemExit(0)
for path in sorted(root.rglob("*")):
    if path.is_file():
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        print(f"{path.relative_to(root)}\t{digest}")
PY
}

requireFile() {
  local path="$1"
  if [[ ! -f "$path" ]]; then
    echo "Falta el archivo obligatorio: $path." >&2
    exit 1
  fi
}

requireText() {
  local path="$1"
  local text="$2"
  if ! grep -Fq -- "$text" "$path"; then
    echo "Falta el texto obligatorio '$text' en $path." >&2
    exit 1
  fi
}

requireBlob() {
  local path="$1"
  local expected="$2"
  local actual
  actual="$(git -C "$ROOT_DIR" hash-object "$ROOT_DIR/$path")"
  if [[ "$actual" != "$expected" ]]; then
    echo "El contrato congelado de Fase 8D cambió: $path." >&2
    exit 1
  fi
}

RESULTS_BEFORE="$(snapshotResults)"

requireBlob \
  "experiments/paper1/analysis-spec.json" \
  "aab56a5ed2f5e983348007b4422a86f299dce786"
requireBlob \
  "scripts/experiments/analysis_statistics.py" \
  "dcc9f1f0789da9180d6424821f42f62ae6b1d58d"
requireBlob \
  "scripts/experiments/analysis_svg.py" \
  "046ba81a166cbbcab524b85b864e1a012e83ab4a"
requireBlob \
  "scripts/experiments/build_experiment_analysis.py" \
  "089b3d786fefae84a2d4c02888d27293a7d32eff"
requireBlob \
  "scripts/experiments/check_experiment_analysis_structure.sh" \
  "5a258f74668c33c42a0dccaa8cef6385242c12aa"

required_files=(
  "docs/research/paper1/REPRODUCCION_INDEPENDIENTE.md"
  "experiments/paper1/reproduction-spec.json"
  "scripts/experiments/reproduction_bundle.py"
  "scripts/experiments/compare_reproduction_outputs.py"
  "scripts/experiments/check_reproduction_environment.py"
  "scripts/experiments/run_independent_reproduction.sh"
  "scripts/experiments/check_independent_reproduction_structure.sh"
)
for path in "${required_files[@]}"; do
  requireFile "$ROOT_DIR/$path"
done

requireText "$ROOT_DIR/Makefile" "experiment-reproduction-structure:"
requireText "$ROOT_DIR/Makefile" "experiment-reproduction-bundle:"
requireText "$ROOT_DIR/Makefile" "experiment-reproduction:"
requireText \
  "$ROOT_DIR/scripts/run_tests.sh" \
  "check_independent_reproduction_structure.sh"
requireText \
  "$ROOT_DIR/docs/research/paper1/REPRODUCCION_INDEPENDIENTE.md" \
  "La matriz definitiva de 1272 tareas no se vuelve a ejecutar."

requireText \
  "$ROOT_DIR/scripts/validate.sh" \
  'VALIDATION_TMP_DIR="$(mktemp -d)"'

if grep -Fq '/tmp/dtl_' "$ROOT_DIR/scripts/validate.sh"; then
  echo "validate.sh no debe usar rutas temporales globales fijas /tmp/dtl_*." >&2
  exit 1
fi

python3 -B - "$ROOT_DIR" <<'PY'
import ast
import json
import sys
import unicodedata
from pathlib import Path

root = Path(sys.argv[1])
python_paths = [
    root / "scripts/experiments/reproduction_bundle.py",
    root / "scripts/experiments/compare_reproduction_outputs.py",
    root / "scripts/experiments/check_reproduction_environment.py",
]
for path in python_paths:
    ast.parse(path.read_text(encoding="utf-8"), filename=str(path))

json_paths = [root / "experiments/paper1/reproduction-spec.json"]
for path in json_paths:
    value = json.loads(path.read_text(encoding="utf-8"))
    if value.get("phase") != "8E":
        raise SystemExit(f"{path}: no declara Fase 8E.")

style_paths = [
    root / "docs/research/paper1/REPRODUCCION_INDEPENDIENTE.md",
    root / "experiments/paper1/README.md",
    root / "results/experiments/README.md",
    root / "README.md",
]
errors = []
for path in style_paths:
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

bash -n "$ROOT_DIR/scripts/experiments/run_independent_reproduction.sh"
bash -n "$ROOT_DIR/scripts/experiments/check_independent_reproduction_structure.sh"

mkdir -p "$TEMP_DIR/source-repository"
git -C "$TEMP_DIR/source-repository" init -q
echo "prueba" > "$TEMP_DIR/source-repository/README.md"
git -C "$TEMP_DIR/source-repository" add README.md
git -C "$TEMP_DIR/source-repository" \
  -c user.name="Prueba" \
  -c user.email="prueba@example.invalid" \
  commit -qm "Crea repositorio de prueba"

mkdir -p "$TEMP_DIR/raw/paper1-q3-v1"
echo '{"phase":"8C"}' > "$TEMP_DIR/raw/paper1-q3-v1/raw-manifest.json"
tar -C "$TEMP_DIR/raw" -czf "$TEMP_DIR/raw.tar.gz" paper1-q3-v1
sha256sum "$TEMP_DIR/raw.tar.gz" > "$TEMP_DIR/raw.tar.gz.sha256"

mkdir -p \
  "$TEMP_DIR/reference/derived" \
  "$TEMP_DIR/reference/tables" \
  "$TEMP_DIR/reference/figures"
cat > "$TEMP_DIR/reference/derived/derived-manifest.json" <<'JSON'
{
  "schema_version": 1,
  "phase": "8D",
  "protocol_id": "paper1-q3-v1",
  "status": "complete",
  "raw_results_modified": false,
  "counts": {
    "task_rows": 1272,
    "tables": 8,
    "figures": 8
  }
}
JSON
for name in \
  task-results.csv \
  measured-results.csv \
  exclusions.csv \
  statistics.csv \
  analysis-summary.json \
  rq-findings.json \
  rq-findings.md; do
  echo "dato-$name" > "$TEMP_DIR/reference/derived/$name"
done
for index in $(seq -w 1 8); do
  echo "tabla-$index" > "$TEMP_DIR/reference/tables/table-$index.csv"
  echo "tabla-$index" > "$TEMP_DIR/reference/tables/table-$index.md"
  echo "<svg><text>$index</text></svg>" \
    > "$TEMP_DIR/reference/figures/figure-$index.svg"
done

for suffix in one two; do
  python3 -B "$ROOT_DIR/scripts/experiments/reproduction_bundle.py" \
    build \
    --repository-root "$TEMP_DIR/source-repository" \
    --raw-archive "$TEMP_DIR/raw.tar.gz" \
    --raw-checksum "$TEMP_DIR/raw.tar.gz.sha256" \
    --derived-dir "$TEMP_DIR/reference/derived" \
    --tables-dir "$TEMP_DIR/reference/tables" \
    --figures-dir "$TEMP_DIR/reference/figures" \
    --output-archive "$TEMP_DIR/bundle-$suffix.tar.gz"
done

if [[ "$(sha256sum "$TEMP_DIR/bundle-one.tar.gz" | awk '{print $1}')" != \
      "$(sha256sum "$TEMP_DIR/bundle-two.tar.gz" | awk '{print $1}')" ]]; then
  echo "El artefacto de reproducción no es determinista." >&2
  exit 1
fi

python3 -B "$ROOT_DIR/scripts/experiments/reproduction_bundle.py" \
  verify \
  --bundle "$TEMP_DIR/bundle-one.tar.gz" \
  --output-dir "$TEMP_DIR/extracted" > "$TEMP_DIR/bundle-root.txt"

BUNDLE_ROOT="$(tail -1 "$TEMP_DIR/bundle-root.txt")"
python3 -B "$ROOT_DIR/scripts/experiments/reproduction_bundle.py" \
  extract-raw \
  --archive "$BUNDLE_ROOT/raw/paper1-q3-v1.tar.gz" \
  --output-dir "$TEMP_DIR/extracted-raw" > /dev/null

mkdir -p "$TEMP_DIR/candidate"
cp -a "$BUNDLE_ROOT/reference/." "$TEMP_DIR/candidate/"
python3 -B "$ROOT_DIR/scripts/experiments/compare_reproduction_outputs.py" \
  --reference-root "$BUNDLE_ROOT/reference" \
  --candidate-root "$TEMP_DIR/candidate" \
  --output-dir "$TEMP_DIR/comparison-match"

echo "cambio" >> "$TEMP_DIR/candidate/tables/table-01.csv"
if python3 -B "$ROOT_DIR/scripts/experiments/compare_reproduction_outputs.py" \
  --reference-root "$BUNDLE_ROOT/reference" \
  --candidate-root "$TEMP_DIR/candidate" \
  --output-dir "$TEMP_DIR/comparison-mismatch"; then
  echo "La comparación no detectó una diferencia conocida." >&2
  exit 1
fi

RESULTS_AFTER="$(snapshotResults)"
if [[ "$RESULTS_BEFORE" != "$RESULTS_AFTER" ]]; then
  echo "El gate de Fase 8E modificó resultados experimentales existentes." >&2
  exit 1
fi

echo "La estructura de reproducción independiente de Fase 8E pasó correctamente."
