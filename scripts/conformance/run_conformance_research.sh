#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# shellcheck source=../formal/tool_versions.env
source "$ROOT_DIR/scripts/formal/tool_versions.env"

TOOLS_ROOT="${FORMAL_TOOLS_ROOT:-$ROOT_DIR/$FORMAL_TOOLS_DIR}"
TLA_JAR="${TLA_TOOLS_JAR:-$TOOLS_ROOT/tla/$TLA_TOOLS_VERSION/tla2tools.jar}"
OUTPUT_DIR="${1:-$ROOT_DIR/results/conformance/research-v1}"
SEED="${2:-2026}"
BASE_SPEC="$ROOT_DIR/specs/tla/CrossShardCommit.tla"
VALID_DIR="$OUTPUT_DIR/valid"
NEGATIVE_DIR="$OUTPUT_DIR/negative"

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

actual_tla_sha1="$(sha1sum "$TLA_JAR" | awk '{print $1}')"
if [[ "$actual_tla_sha1" != "$TLA_TOOLS_SHA1" ]]; then
  echo "El SHA-1 de TLC no coincide con la version fijada." >&2
  exit 1
fi

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

bash "$ROOT_DIR/scripts/conformance/run_trace_replay.sh" \
  "$VALID_DIR" \
  "$SEED"

bash "$ROOT_DIR/scripts/conformance/run_negative_trace_corpus.sh" \
  "$NEGATIVE_DIR" \
  "$SEED"

checked_out_commit="$(git -C "$ROOT_DIR" rev-parse HEAD)"
source_commit="${CONFORMANCE_SOURCE_COMMIT:-${FORMAL_SOURCE_COMMIT:-$checked_out_commit}}"
source_ref="${CONFORMANCE_SOURCE_REF:-${FORMAL_SOURCE_REF:-}}"
if [[ -z "$source_ref" ]]; then
  source_ref="$(git -C "$ROOT_DIR" branch --show-current)"
fi
if [[ -z "$source_ref" ]]; then
  source_ref="detached-$checked_out_commit"
fi

python3 -B "$ROOT_DIR/scripts/conformance/build_conformance_manifest.py" \
  --valid-manifest "$VALID_DIR/manifest.csv" \
  --negative-manifest "$NEGATIVE_DIR/manifest.csv" \
  --output-dir "$OUTPUT_DIR" \
  --seed "$SEED" \
  --release "v1.1.0-rc.1" \
  --tla-version "$TLA_TOOLS_VERSION" \
  --tla-sha1 "$actual_tla_sha1" \
  --source-commit "$source_commit" \
  --checked-out-commit "$checked_out_commit" \
  --source-ref "$source_ref" \
  --event-name "${GITHUB_EVENT_NAME:-local}" \
  --run-id "${GITHUB_RUN_ID:-}" \
  --run-attempt "${GITHUB_RUN_ATTEMPT:-}" \
  --repository "${GITHUB_REPOSITORY:-kapumota/dtl-lab}"

python3 -m json.tool "$OUTPUT_DIR/manifest.json" >/dev/null
python3 -m json.tool "$OUTPUT_DIR/summary.json" >/dev/null
test -s "$OUTPUT_DIR/conformance_matrix.csv"
test -s "$OUTPUT_DIR/summary.md"

echo "Conformidad cientifica completada correctamente."
echo "Artefacto: $OUTPUT_DIR."
