#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

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

require_file "$ROOT_DIR/docs/research/paper1/PROTOCOLO_EXPERIMENTAL_Q3.md"
require_file "$ROOT_DIR/experiments/paper1/README.md"
require_file "$ROOT_DIR/experiments/paper1/experiment-spec.json"
require_file "$ROOT_DIR/experiments/paper1/configurations.csv"
require_file "$ROOT_DIR/experiments/paper1/seeds.txt"
require_file "$ROOT_DIR/scripts/experiments/check_experimental_protocol.py"
require_file "$ROOT_DIR/docs/research/paper1/CIERRE_CONFORMIDAD_FASE_7.md"
require_file "$ROOT_DIR/scripts/conformance/run_conformance_research.sh"
require_file "$ROOT_DIR/specs/tla/CrossShardCommit.tla"
require_file "$ROOT_DIR/specs/alloy/CrossShardCommit.als"

require_text "$ROOT_DIR/Makefile" "experiment-protocol:"
require_text "$ROOT_DIR/scripts/run_tests.sh" "check_experimental_structure.sh"
require_text "$ROOT_DIR/docs/research/paper1/PREGUNTAS_DE_INVESTIGACION.md" "#### RQ4: costo de verificación"
require_text "$ROOT_DIR/docs/research/paper1/PROTOCOLO_EXPERIMENTAL_Q3.md" "H4 permanece pendiente de evaluación."
require_text "$ROOT_DIR/docs/research/paper1/PROTOCOLO_EXPERIMENTAL_Q3.md" "dos repeticiones de calentamiento"
require_text "$ROOT_DIR/docs/research/paper1/PROTOCOLO_EXPERIMENTAL_Q3.md" "diez repeticiones medidas"
require_text "$ROOT_DIR/docs/research/paper1/PROTOCOLO_EXPERIMENTAL_Q3.md" "timeout de 1800 segundos"
require_text "$ROOT_DIR/docs/research/paper1/PROTOCOLO_EXPERIMENTAL_Q3.md" "límite de memoria residente de 12288 MiB"
require_text "$ROOT_DIR/docs/research/paper1/PROTOCOLO_EXPERIMENTAL_Q3.md" "no se han generado resultados definitivos"

python3 -B "$ROOT_DIR/scripts/experiments/check_experimental_protocol.py" \
  --spec "$ROOT_DIR/experiments/paper1/experiment-spec.json" \
  --configurations "$ROOT_DIR/experiments/paper1/configurations.csv" \
  --seeds "$ROOT_DIR/experiments/paper1/seeds.txt"

python3 -B - "$ROOT_DIR" <<'__CHECK_STYLE__'
import sys
from pathlib import Path

root = Path(sys.argv[1])
paths = [
    root / "docs/research/paper1/PROTOCOLO_EXPERIMENTAL_Q3.md",
    root / "experiments/paper1/README.md",
]
errors = []

for path in paths:
    text = path.read_text(encoding="utf-8")
    if "\u2013" in text or "\u2014" in text:
        errors.append(f"{path}: contiene guiones tipograficos.")
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
                    f"{path}:{line_number}: titulo de nivel {level}."
                )

if errors:
    raise SystemExit("\n".join(errors))
__CHECK_STYLE__

echo "La validacion estructural del protocolo experimental Q3 paso correctamente."
