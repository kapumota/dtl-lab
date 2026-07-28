#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# shellcheck source=../formal/tool_versions.env
source "$ROOT_DIR/scripts/formal/tool_versions.env"

TOOLS_ROOT="${FORMAL_TOOLS_ROOT:-$ROOT_DIR/$FORMAL_TOOLS_DIR}"
TLA_JAR="${TLA_TOOLS_JAR:-$TOOLS_ROOT/tla/$TLA_TOOLS_VERSION/tla2tools.jar}"
OUTPUT_DIR="${1:-$ROOT_DIR/results/conformance/replay-v1}"
SEED="${2:-2026}"
BASE_SPEC="$ROOT_DIR/specs/tla/CrossShardCommit.tla"

if [[ ! -f "$TLA_JAR" ]]; then
  echo "Falta TLC en $TLA_JAR." >&2
  exit 1
fi
if [[ ! -f "$BASE_SPEC" ]]; then
  echo "Falta la especificacion base en $BASE_SPEC." >&2
  exit 1
fi
if ! [[ "$SEED" =~ ^-?[0-9]+$ ]]; then
  echo "La seed debe ser un entero." >&2
  exit 2
fi

mkdir -p "$ROOT_DIR/build/classes" "$OUTPUT_DIR"

javac -d "$ROOT_DIR/build/classes" \
  $(find "$ROOT_DIR/src/main/java" -name "*.java")

java -cp "$ROOT_DIR/build/classes" \
  dltlab.conformance.TraceReplayCatalogRunner \
  "$OUTPUT_DIR" \
  "$TLA_JAR" \
  "$BASE_SPEC" \
  "$SEED"
