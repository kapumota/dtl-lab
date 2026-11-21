#!/usr/bin/env bash
set -euo pipefail

# G2-P3: testigos Java reales + verificacion formal finita del no-retorno tras debito.
repo="$(git rev-parse --show-toplevel)"
out="$(realpath -m "${1:-$HOME/dtl-g2-p3-evidencia}")"
self="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
mkdir -p "$out"
printf 'INCOMPLETO: G2-P3 no ha terminado.\n' > "$out/ESTADO.txt"

cd "$repo"
if ! git merge-base --is-ancestor 8ced907 HEAD; then
  echo 'ERROR: falta el commit G1 de referencia.' >&2
  exit 3
fi
if [[ ! -f src/test/java/dltlab/conformance/v2/V2DecisionWitnessTest.java ]]; then
  echo 'ERROR: no se aplico el parche G2-P3.' >&2
  exit 4
fi
if ! git diff --check; then exit 5; fi
(
 cd specs/tla/research/paper1_v2_2
 sha256sum -c SHA256SUMS.txt
) > "$out/integridad_v22.log"
(
 cd docs/research/paper1/evidencia-gates-2026-11-20
 sha256sum -c SHA256SUMS.txt
) > "$out/integridad_evidencia.log"
{
  echo "revision=$(git rev-parse HEAD)"
  echo "rama=$(git branch --show-current)"
  date --iso-8601=seconds
  java -version 2>&1
  javac -version 2>&1
  git status --short
} > "$out/entorno.txt"

# Compilar y ejecutar el nuevo test sobre las clases REALES, no sobre fixtures.
mkdir -p build/classes build/test-classes
javac --release 17 -d build/classes $(find src/main/java -name '*.java')
javac --release 17 -cp build/classes -d build/test-classes \
  $(find src/test/java -name '*.java')
java -cp build/classes:build/test-classes \
  dltlab.conformance.v2.V2IntermediateAuditTest > "$out/g2_p2_control.log" 2>&1
java -cp build/classes:build/test-classes \
  dltlab.conformance.v2.V2DecisionWitnessTest "$out" > "$out/testigos_java.log" 2>&1

if [[ -n "${TLA2TOOLS_JAR:-}" ]]; then
  jar="$TLA2TOOLS_JAR"
elif [[ -f "$HOME/dtl-g0-v22-paquete/DTL_Lab_V2_Artifact_v2.2/tools/tla2tools.jar" ]]; then
  jar="$HOME/dtl-g0-v22-paquete/DTL_Lab_V2_Artifact_v2.2/tools/tla2tools.jar"
elif [[ -f "$repo/.formal-tools/tla/1.7.4/tla2tools.jar" ]]; then
  jar="$repo/.formal-tools/tla/1.7.4/tla2tools.jar"
else
  echo 'NO GO: ubica TLC 1.7.4 e indica TLA2TOOLS_JAR=/ruta/tla2tools.jar' >&2
  exit 7
fi

python3 "$self/verificar_decision_g2_p3.py" \
  --modelo "$repo/specs/tla/research/paper1_v2_2" \
  --testigos "$out/G2P3_TESTIGOS_JAVA.tsv" \
  --jar "$jar" --salida "$out"
(
  cd "$out"
  find . -type f ! -name EVIDENCIA_SHA256SUMS.txt -print0 | \
    sort -z | xargs -0 -r sha256sum > EVIDENCIA_SHA256SUMS.txt
)
cat "$out/ESTADO.txt"
