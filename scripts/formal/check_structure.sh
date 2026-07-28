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
requireFile "$ROOT_DIR/docs/research/paper1/MODELO_MULTISESION_MUTANTES.md"
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

for config in \
  2shards-1transfer.cfg \
  2shards-2transfers.cfg \
  3shards-3transfers.cfg \
  duplicate-receipt.cfg \
  delayed-message.cfg \
  timeout-race.cfg; do
  requireFile "$ROOT_DIR/specs/tla/configs/$config"
done

for config in \
  no-replay-protection.cfg \
  credit-before-receipt.cfg \
  commit-after-abort.cfg \
  timeout-without-release.cfg \
  quorum-bypass.cfg; do
  requireFile "$ROOT_DIR/specs/tla/configs/mutants/$config"
done

for mutant in \
  NoReplayProtection \
  CreditBeforeReceipt \
  CommitAfterAbort \
  TimeoutWithoutRelease \
  QuorumBypass; do
  requireFile "$ROOT_DIR/specs/tla/mutants/$mutant.tla"
  requireFile "$ROOT_DIR/specs/alloy/mutants/$mutant.als"
done

requireFile "$ROOT_DIR/specs/alloy/scopes/bounds.json"
requireFile "$ROOT_DIR/specs/alloy/scopes/README.md"

for property in \
  NoReceiptReplay \
  DestinationCreditRequiresValidReceipt \
  DecisionConsistency \
  EventuallyReleasedAfterTimeout \
  QuorumRequired; do
  requireText "$TLA_SPEC" "$property"
  requireText "$TLA_CFG" "$property"
  requireText "$ALLOY_SPEC" "$property"
done

for variable in \
  status \
  sourceShard \
  targetShard \
  locked \
  receiptOwner \
  receiptUseCount \
  destinationCredit \
  fundsReleased \
  messages \
  votes; do
  requireText "$TLA_SPEC" "$variable"
done

for signature in State Transfer Receipt Shard Validator Message; do
  requireText "$ALLOY_SPEC" "sig $signature"
done

requireText "$TLA_SPEC" "Stutter =="
requireText "$TLA_SPEC" "\\/ Stutter"
requireText "$TLA_SPEC" "Spec =="
requireText "$TLA_CFG" "SPECIFICATION Spec"
requireText "$ALLOY_SPEC" "open util/ordering[State]"
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
