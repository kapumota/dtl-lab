#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 5 ]]; then
  echo "Uso: run_alloy.sh <run-id> <tipo> <modelo> <success|failure> <propiedad-objetivo>" >&2
  exit 2
fi

RUN_ID="$1"
KIND="$2"
SPEC_PATH="$3"
EXPECTATION="$4"
EXPECTED_PROPERTY="$5"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# shellcheck source=tool_versions.env
source "$ROOT_DIR/scripts/formal/tool_versions.env"

TOOLS_ROOT="${FORMAL_TOOLS_ROOT:-$ROOT_DIR/$FORMAL_TOOLS_DIR}"
ALLOY_JAR="${ALLOY_JAR:-$TOOLS_ROOT/alloy/$ALLOY_VERSION/org.alloytools.alloy.dist-$ALLOY_VERSION.jar}"
RESULT_DIR="${FORMAL_RESULTS_DIR:-$ROOT_DIR/results/formal}"
LOG_DIR="$RESULT_DIR/logs"
COUNTEREXAMPLE_DIR="$RESULT_DIR/counterexamples"
STDOUT_PATH="$LOG_DIR/$RUN_ID.alloy.stdout.txt"
STDERR_PATH="$LOG_DIR/$RUN_ID.alloy.stderr.txt"
TIME_PATH="$LOG_DIR/$RUN_ID.alloy.time.txt"
ROWS_PATH="$LOG_DIR/$RUN_ID.alloy.rows.csv"
SUMMARY_PATH="$LOG_DIR/$RUN_ID.alloy.summary.json"
OUTPUT_DIR="$LOG_DIR/$RUN_ID.alloy-output"
RECEIPT_PATH="$OUTPUT_DIR/receipt.json"

if [[ ! -f "$ALLOY_JAR" ]]; then
  echo "Falta Alloy en $ALLOY_JAR." >&2
  exit 1
fi
if [[ ! -f "$SPEC_PATH" ]]; then
  echo "Falta el modelo Alloy: $SPEC_PATH" >&2
  exit 1
fi
if [[ ! -x /usr/bin/time ]]; then
  echo "/usr/bin/time es obligatorio para registrar tiempo y memoria." >&2
  exit 1
fi

mkdir -p "$LOG_DIR" "$COUNTEREXAMPLE_DIR"
rm -rf "$OUTPUT_DIR"

set +e
/usr/bin/time -v -o "$TIME_PATH" \
  java -jar "$ALLOY_JAR" exec \
    --quiet \
    --force \
    --type json \
    --solver sat4j \
    --output "$OUTPUT_DIR" \
    "$SPEC_PATH" \
  >"$STDOUT_PATH" 2>"$STDERR_PATH"
exit_code=$?
set -e

python3 "$ROOT_DIR/scripts/formal/parse_alloy_results.py" \
  --run-id "$RUN_ID" \
  --kind "$KIND" \
  --spec "$SPEC_PATH" \
  --receipt "$RECEIPT_PATH" \
  --stdout "$STDOUT_PATH" \
  --stderr "$STDERR_PATH" \
  --time-log "$TIME_PATH" \
  --exit-code "$exit_code" \
  --expect "$EXPECTATION" \
  --expected-property "$EXPECTED_PROPERTY" \
  --rows "$ROWS_PATH" \
  --summary "$SUMMARY_PATH"

tail -n +2 "$ROWS_PATH" >> "$RESULT_DIR/alloy_runs.csv"

if [[ "$EXPECTATION" == "failure" ]]; then
  mapfile -t solution_files < <(
    find "$OUTPUT_DIR" -maxdepth 1 -type f -name '*solution-*.json' | sort
  )

  if [[ ${#solution_files[@]} -eq 0 ]]; then
    echo "Alloy no almaceno un contraejemplo para $RUN_ID." >&2
    exit 1
  fi

  for solution_path in "${solution_files[@]}"; do
    destination="$COUNTEREXAMPLE_DIR/$RUN_ID-$(basename "$solution_path")"
    cp "$solution_path" "$destination"
  done
fi
