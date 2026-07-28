#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TLA_SPEC="$ROOT_DIR/specs/tla/CrossShardCommit.tla"
TLA_CFG="$ROOT_DIR/specs/tla/CrossShardCommit.cfg"
ALLOY_SPEC="$ROOT_DIR/specs/alloy/CrossShardCommit.als"
VERSION_FILE="$ROOT_DIR/scripts/formal/tool_versions.env"

requireFile() {
  local path="$1"
  if [[ ! -f "$path" ]]; then
    echo "Falta el archivo requerido: $path" >&2
    exit 1
  fi
}

requireText() {
  local path="$1"
  local text="$2"
  if ! grep -Fq "$text" "$path"; then
    echo "No se encontro '$text' en $path" >&2
    exit 1
  fi
}

requireExecutable() {
  local path="$1"
  requireFile "$path"
  if [[ ! -x "$path" ]]; then
    echo "El script debe ser ejecutable: $path" >&2
    exit 1
  fi
}

echo "Verificacion formal estructural de DLT-Lab"

requireFile "$TLA_SPEC"
requireFile "$TLA_CFG"
requireFile "$ALLOY_SPEC"
requireFile "$VERSION_FILE"
requireFile "$ROOT_DIR/.github/workflows/formal-verification.yml"
requireFile "$ROOT_DIR/results/formal/README.md"
requireText "$ROOT_DIR/Makefile" "formal-research:"

for script in \
  check_structure.sh \
  install_tla_tools.sh \
  install_alloy.sh \
  run_tlc.sh \
  run_alloy.sh \
  run_formal_research.sh; do
  requireExecutable "$ROOT_DIR/scripts/formal/$script"
done

requireFile "$ROOT_DIR/scripts/formal/parse_tlc_results.py"
requireFile "$ROOT_DIR/scripts/formal/parse_alloy_results.py"

for invariant in NoDoubleMint NoValueLoss NoReceiptReplay AtomicCommit TimeoutReleasesFunds; do
  requireText "$TLA_SPEC" "$invariant"
  requireText "$TLA_CFG" "$invariant"
  requireText "$ALLOY_SPEC" "$invariant"
done

requireText "$TLA_SPEC" "Spec =="
requireText "$TLA_CFG" "SPECIFICATION Spec"
requireText "$ALLOY_SPEC" "module CrossShardCommit"
requireText "$VERSION_FILE" "TLA_TOOLS_VERSION="
requireText "$VERSION_FILE" "ALLOY_VERSION="
requireText "$VERSION_FILE" "JAVA_REQUIRED_MAJOR="

if LC_ALL=C grep -RInE $'\xE2\x80\x93|\xE2\x80\x94' "$ROOT_DIR/scripts/formal" >/dev/null 2>&1; then
  echo "Los scripts formales contienen guiones tipograficos no permitidos." >&2
  exit 1
fi

if grep -RInE '={8,}' "$ROOT_DIR/scripts/formal" >/dev/null 2>&1; then
  echo "Los scripts formales contienen separadores decorativos no permitidos." >&2
  exit 1
fi

echo "Estructura formal validada correctamente."
