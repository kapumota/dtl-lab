#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_DIR="${1:-$ROOT_DIR/results/traces/catalog-v1}"
SEED="${2:-2026}"

mkdir -p "$ROOT_DIR/build/classes"

javac -d "$ROOT_DIR/build/classes" \
  $(find "$ROOT_DIR/src/main/java" -name "*.java")

java -cp "$ROOT_DIR/build/classes" \
  dltlab.trace.TraceCatalogExporter \
  "$OUTPUT_DIR" \
  "$SEED"
