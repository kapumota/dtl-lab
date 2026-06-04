#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TLA_SPEC="$ROOT_DIR/specs/tla/CrossShardCommit.tla"
TLA_CFG="$ROOT_DIR/specs/tla/CrossShardCommit.cfg"
ALLOY_SPEC="$ROOT_DIR/specs/alloy/CrossShardCommit.als"

checkFile() {
  local path="$1"
  if [[ ! -f "$path" ]]; then
    echo "Falta el archivo requerido: $path" >&2
    exit 1
  fi
}

checkText() {
  local path="$1"
  local text="$2"
  if ! grep -q "$text" "$path"; then
    echo "No se encontro '$text' en $path" >&2
    exit 1
  fi
}

echo "Verificacion formal estructural de DLT-Lab"
echo "-------------------------------------------"

checkFile "$TLA_SPEC"
checkFile "$TLA_CFG"
checkFile "$ALLOY_SPEC"

for invariant in NoDoubleMint NoValueLoss NoReceiptReplay AtomicCommit TimeoutReleasesFunds; do
  checkText "$TLA_SPEC" "$invariant"
  checkText "$TLA_CFG" "$invariant"
  checkText "$ALLOY_SPEC" "$invariant"
done

checkText "$TLA_SPEC" "Spec =="
checkText "$TLA_CFG" "SPECIFICATION Spec"
checkText "$ALLOY_SPEC" "module CrossShardCommit"

echo "Especificaciones encontradas y nombres de invariantes validados."

TLA_JAR="${TLA_TOOLS_JAR:-}"
if [[ -z "$TLA_JAR" && -f "$ROOT_DIR/tools/tla2tools.jar" ]]; then
  TLA_JAR="$ROOT_DIR/tools/tla2tools.jar"
fi

if [[ -n "$TLA_JAR" && -f "$TLA_JAR" ]]; then
  echo "Ejecutando TLC con $TLA_JAR"
  java -cp "$TLA_JAR" tlc2.TLC -config "$TLA_CFG" "$TLA_SPEC"
elif command -v tlc >/dev/null 2>&1; then
  echo "Ejecutando TLC desde el comando tlc"
  tlc -config "$TLA_CFG" "$TLA_SPEC"
else
  echo "TLC no fue encontrado. Se omite model checking ejecutable y se conserva la validacion estructural."
  echo "Para ejecutar TLC, define TLA_TOOLS_JAR o coloca tools/tla2tools.jar."
fi

if [[ -n "${ALLOY_JAR:-}" && -f "${ALLOY_JAR:-}" ]]; then
  echo "Alloy detectado en ALLOY_JAR. Abre $ALLOY_SPEC con Alloy Analyzer para revisar los checks."
else
  echo "Alloy Analyzer no se ejecuta automaticamente desde este script. El modelo esta en $ALLOY_SPEC."
fi

echo "Verificacion formal finalizada."
