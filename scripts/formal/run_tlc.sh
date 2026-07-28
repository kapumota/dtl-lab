#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 5 ]]; then
  echo "Uso: run_tlc.sh <run-id> <tipo> <especificacion> <configuracion> <success|failure>" >&2
  exit 2
fi

RUN_ID="$1"
KIND="$2"
SPEC_PATH="$3"
CFG_PATH="$4"
EXPECTATION="$5"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# shellcheck source=tool_versions.env
source "$ROOT_DIR/scripts/formal/tool_versions.env"

TOOLS_ROOT="${FORMAL_TOOLS_ROOT:-$ROOT_DIR/$FORMAL_TOOLS_DIR}"
TLA_JAR="${TLA_TOOLS_JAR:-$TOOLS_ROOT/tla/$TLA_TOOLS_VERSION/tla2tools.jar}"
RESULT_DIR="${FORMAL_RESULTS_DIR:-$ROOT_DIR/results/formal}"
LOG_DIR="$RESULT_DIR/logs"
STDOUT_PATH="$LOG_DIR/$RUN_ID.tlc.stdout.txt"
STDERR_PATH="$LOG_DIR/$RUN_ID.tlc.stderr.txt"
TIME_PATH="$LOG_DIR/$RUN_ID.tlc.time.txt"
ROWS_PATH="$LOG_DIR/$RUN_ID.tlc.rows.csv"
SUMMARY_PATH="$LOG_DIR/$RUN_ID.tlc.summary.json"
META_DIR="$LOG_DIR/$RUN_ID.tlc-meta"

if [[ ! -f "$TLA_JAR" ]]; then
  echo "Falta TLC en $TLA_JAR." >&2
  exit 1
fi
if [[ ! -f "$SPEC_PATH" || ! -f "$CFG_PATH" ]]; then
  echo "Falta la especificacion o configuracion de TLC." >&2
  exit 1
fi
if [[ ! -x /usr/bin/time ]]; then
  echo "/usr/bin/time es obligatorio para registrar tiempo y memoria." >&2
  exit 1
fi

mkdir -p "$LOG_DIR" "$META_DIR"
SPEC_DIR="$(cd "$(dirname "$SPEC_PATH")" && pwd)"
SPEC_NAME="$(basename "$SPEC_PATH")"
CFG_ABS="$(cd "$(dirname "$CFG_PATH")" && pwd)/$(basename "$CFG_PATH")"

set +e
(
  cd "$SPEC_DIR"
  /usr/bin/time -v -o "$TIME_PATH" \
    java -XX:+UseParallelGC -cp "$TLA_JAR" tlc2.TLC \
      -workers 1 \
      -metadir "$META_DIR" \
      -config "$CFG_ABS" \
      "$SPEC_NAME"
) >"$STDOUT_PATH" 2>"$STDERR_PATH"
exit_code=$?
set -e

python3 "$ROOT_DIR/scripts/formal/parse_tlc_results.py" \
  --run-id "$RUN_ID" \
  --kind "$KIND" \
  --spec "$SPEC_PATH" \
  --config "$CFG_PATH" \
  --stdout "$STDOUT_PATH" \
  --stderr "$STDERR_PATH" \
  --time-log "$TIME_PATH" \
  --exit-code "$exit_code" \
  --expect "$EXPECTATION" \
  --rows "$ROWS_PATH" \
  --summary "$SUMMARY_PATH"

tail -n +2 "$ROWS_PATH" >> "$RESULT_DIR/tla_runs.csv"
