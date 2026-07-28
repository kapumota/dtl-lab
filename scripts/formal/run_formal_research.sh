#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# shellcheck source=tool_versions.env
source "$ROOT_DIR/scripts/formal/tool_versions.env"

TOOLS_ROOT="${FORMAL_TOOLS_ROOT:-$ROOT_DIR/$FORMAL_TOOLS_DIR}"
TLA_JAR="${TLA_TOOLS_JAR:-$TOOLS_ROOT/tla/$TLA_TOOLS_VERSION/tla2tools.jar}"
ALLOY_JAR="${ALLOY_JAR:-$TOOLS_ROOT/alloy/$ALLOY_VERSION/org.alloytools.alloy.dist-$ALLOY_VERSION.jar}"
RESULT_DIR="${FORMAL_RESULTS_DIR:-$ROOT_DIR/results/formal}"
LOG_DIR="$RESULT_DIR/logs"
COUNTEREXAMPLE_DIR="$RESULT_DIR/counterexamples"

calculateSha256() {
  local path="$1"
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$path" | awk '{print $1}'
  elif command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$path" | awk '{print $1}'
  else
    echo "No se encontro una herramienta para calcular SHA-256." >&2
    exit 1
  fi
}

requireCommand() {
  local command_name="$1"
  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "Falta el comando obligatorio: $command_name" >&2
    exit 1
  fi
}

runTla() {
  local run_id="$1"
  local kind="$2"
  local spec_path="$3"
  local config_path="$4"
  local expectation="$5"

  bash "$ROOT_DIR/scripts/formal/run_tlc.sh" \
    "$run_id" "$kind" "$spec_path" "$config_path" "$expectation"
}

runAlloy() {
  local run_id="$1"
  local kind="$2"
  local spec_path="$3"
  local expectation="$4"

  bash "$ROOT_DIR/scripts/formal/run_alloy.sh" \
    "$run_id" "$kind" "$spec_path" "$expectation"
}

requireCommand java
requireCommand python3
requireCommand git

bash "$ROOT_DIR/scripts/formal/check_structure.sh"

if [[ ! -f "$TLA_JAR" ]]; then
  echo "El perfil cientifico requiere TLC instalado en $TLA_JAR." >&2
  exit 1
fi
if [[ ! -f "$ALLOY_JAR" ]]; then
  echo "El perfil cientifico requiere Alloy instalado en $ALLOY_JAR." >&2
  exit 1
fi

java_major="$(java -version 2>&1 | awk -F'[".]' '/version/ {print $2; exit}')"
if [[ -z "$java_major" || "$java_major" -lt "$JAVA_REQUIRED_MAJOR" ]]; then
  echo "Java $JAVA_REQUIRED_MAJOR o superior es obligatorio." >&2
  exit 1
fi

tlc_probe_output="$(java -cp "$TLA_JAR" tlc2.TLC -version 2>&1 || true)"
tlc_reported_version="$(grep -m1 'TLC2 Version' <<<"$tlc_probe_output" || true)"
if [[ -z "$tlc_reported_version" ]]; then
  echo "No se pudo identificar la version de TLC." >&2
  exit 1
fi

alloy_reported_version="$(java -jar "$ALLOY_JAR" version --full 2>&1 | head -n 1)"
if [[ -z "$alloy_reported_version" ]] || ! grep -Eq "^${ALLOY_VERSION}([.-]|$)" <<<"$alloy_reported_version"; then
  echo "No se pudo identificar la version de Alloy." >&2
  exit 1
fi

mkdir -p "$RESULT_DIR"
find "$RESULT_DIR" -mindepth 1 -maxdepth 1 ! -name README.md -exec rm -rf {} +
mkdir -p "$LOG_DIR" "$COUNTEREXAMPLE_DIR"

cat > "$RESULT_DIR/tla_runs.csv" <<'CSV'
run_id,kind,model,property,result,expected_outcome,tool_exit_code,states_generated,distinct_states,depth,elapsed_seconds,max_memory_kb,tool_version,stdout_path,stderr_path
CSV

cat > "$RESULT_DIR/alloy_runs.csv" <<'CSV'
run_id,kind,model,property,command_type,result,expected_outcome,tool_exit_code,solver,scope,counterexamples,solve_duration_ms,elapsed_seconds,max_memory_kb,states_generated,distinct_states,depth,receipt_path
CSV

cat > "$RESULT_DIR/mutant_matrix.csv" <<'CSV'
tool,run_id,mutant,property,spec,config
TLC,tla-mutant-no-replay,NoReplayProtection,NoReceiptReplay,specs/tla/mutants/NoReplayProtection.tla,specs/tla/configs/mutants/no-replay-protection.cfg
TLC,tla-mutant-credit-before-receipt,CreditBeforeReceipt,DestinationCreditRequiresValidReceipt,specs/tla/mutants/CreditBeforeReceipt.tla,specs/tla/configs/mutants/credit-before-receipt.cfg
TLC,tla-mutant-commit-after-abort,CommitAfterAbort,DecisionConsistency,specs/tla/mutants/CommitAfterAbort.tla,specs/tla/configs/mutants/commit-after-abort.cfg
TLC,tla-mutant-timeout-without-release,TimeoutWithoutRelease,EventuallyReleasedAfterTimeout,specs/tla/mutants/TimeoutWithoutRelease.tla,specs/tla/configs/mutants/timeout-without-release.cfg
TLC,tla-mutant-quorum-bypass,QuorumBypass,QuorumRequired,specs/tla/mutants/QuorumBypass.tla,specs/tla/configs/mutants/quorum-bypass.cfg
Alloy,alloy-mutant-no-replay,NoReplayProtection,NoReceiptReplay,specs/alloy/mutants/NoReplayProtection.als,
Alloy,alloy-mutant-credit-before-receipt,CreditBeforeReceipt,DestinationCreditRequiresValidReceipt,specs/alloy/mutants/CreditBeforeReceipt.als,
Alloy,alloy-mutant-commit-after-abort,CommitAfterAbort,DecisionConsistency,specs/alloy/mutants/CommitAfterAbort.als,
Alloy,alloy-mutant-timeout-without-release,TimeoutWithoutRelease,EventuallyReleasedAfterTimeout,specs/alloy/mutants/TimeoutWithoutRelease.als,
Alloy,alloy-mutant-quorum-bypass,QuorumBypass,QuorumRequired,specs/alloy/mutants/QuorumBypass.als,
CSV

java_version_full="$(java -version 2>&1 | tr '\n' ' ' | sed 's/[[:space:]]\+/ /g')"
python_version="$(python3 --version 2>&1)"
git_commit="$(git -C "$ROOT_DIR" rev-parse HEAD 2>/dev/null || printf 'desconocido')"

cat > "$RESULT_DIR/tool_versions.txt" <<EOF_VERSIONS
TLA_TOOLS_DISTRIBUTION=$TLA_TOOLS_VERSION
TLC_REPORTED_VERSION=$tlc_reported_version
TLA_TOOLS_SHA256=$(calculateSha256 "$TLA_JAR")
ALLOY_DISTRIBUTION=$ALLOY_VERSION
ALLOY_REPORTED_VERSION=$alloy_reported_version
ALLOY_SHA256=$(calculateSha256 "$ALLOY_JAR")
JAVA=$java_version_full
PYTHON=$python_version
EOF_VERSIONS

python3 - "$RESULT_DIR/environment.json" "$git_commit" "$java_version_full" "$python_version" <<'PY'
import json
import os
import platform
import sys
from datetime import datetime, timezone
from pathlib import Path

output = Path(sys.argv[1])
data = {
    "schema_version": 1,
    "generated_at_utc": datetime.now(timezone.utc).isoformat(),
    "git_commit": sys.argv[2],
    "java": sys.argv[3],
    "python": sys.argv[4],
    "operating_system": platform.platform(),
    "architecture": platform.machine(),
    "processor": platform.processor(),
    "cpu_count": os.cpu_count(),
    "github_actions": os.environ.get("GITHUB_ACTIONS", "false"),
    "github_run_id": os.environ.get("GITHUB_RUN_ID"),
    "github_sha": os.environ.get("GITHUB_SHA"),
}
output.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
PY

TLA_SPEC="$ROOT_DIR/specs/tla/CrossShardCommit.tla"
TLA_CONFIG_DIR="$ROOT_DIR/specs/tla/configs"
TLA_MUTANT_DIR="$ROOT_DIR/specs/tla/mutants"
ALLOY_SPEC="$ROOT_DIR/specs/alloy/CrossShardCommit.als"
ALLOY_MUTANT_DIR="$ROOT_DIR/specs/alloy/mutants"

runTla "tla-valid-2shards-1transfer" "valid" "$TLA_SPEC" \
  "$TLA_CONFIG_DIR/2shards-1transfer.cfg" "success"
runTla "tla-valid-2shards-2transfers" "valid" "$TLA_SPEC" \
  "$TLA_CONFIG_DIR/2shards-2transfers.cfg" "success"
runTla "tla-valid-3shards-3transfers" "valid" "$TLA_SPEC" \
  "$TLA_CONFIG_DIR/3shards-3transfers.cfg" "success"
runTla "tla-valid-duplicate-receipt" "valid" "$TLA_SPEC" \
  "$TLA_CONFIG_DIR/duplicate-receipt.cfg" "success"
runTla "tla-valid-delayed-message" "valid" "$TLA_SPEC" \
  "$TLA_CONFIG_DIR/delayed-message.cfg" "success"
runTla "tla-valid-timeout-race" "valid" "$TLA_SPEC" \
  "$TLA_CONFIG_DIR/timeout-race.cfg" "success"

runTla "tla-mutant-no-replay" "mutant" \
  "$TLA_MUTANT_DIR/NoReplayProtection.tla" \
  "$TLA_CONFIG_DIR/mutants/no-replay-protection.cfg" "failure"
runTla "tla-mutant-credit-before-receipt" "mutant" \
  "$TLA_MUTANT_DIR/CreditBeforeReceipt.tla" \
  "$TLA_CONFIG_DIR/mutants/credit-before-receipt.cfg" "failure"
runTla "tla-mutant-commit-after-abort" "mutant" \
  "$TLA_MUTANT_DIR/CommitAfterAbort.tla" \
  "$TLA_CONFIG_DIR/mutants/commit-after-abort.cfg" "failure"
runTla "tla-mutant-timeout-without-release" "mutant" \
  "$TLA_MUTANT_DIR/TimeoutWithoutRelease.tla" \
  "$TLA_CONFIG_DIR/mutants/timeout-without-release.cfg" "failure"
runTla "tla-mutant-quorum-bypass" "mutant" \
  "$TLA_MUTANT_DIR/QuorumBypass.tla" \
  "$TLA_CONFIG_DIR/mutants/quorum-bypass.cfg" "failure"

runAlloy "alloy-valid-multisession" "valid" "$ALLOY_SPEC" "success"
runAlloy "alloy-mutant-no-replay" "mutant" \
  "$ALLOY_MUTANT_DIR/NoReplayProtection.als" "failure"
runAlloy "alloy-mutant-credit-before-receipt" "mutant" \
  "$ALLOY_MUTANT_DIR/CreditBeforeReceipt.als" "failure"
runAlloy "alloy-mutant-commit-after-abort" "mutant" \
  "$ALLOY_MUTANT_DIR/CommitAfterAbort.als" "failure"
runAlloy "alloy-mutant-timeout-without-release" "mutant" \
  "$ALLOY_MUTANT_DIR/TimeoutWithoutRelease.als" "failure"
runAlloy "alloy-mutant-quorum-bypass" "mutant" \
  "$ALLOY_MUTANT_DIR/QuorumBypass.als" "failure"

python3 - "$RESULT_DIR" "$git_commit" <<'PY'
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

result_dir = Path(sys.argv[1])
summaries = [
    json.loads(path.read_text(encoding="utf-8"))
    for path in sorted((result_dir / "logs").glob("*.summary.json"))
]

expected_reports = 17
if len(summaries) != expected_reports:
    raise SystemExit(
        f"Se esperaban {expected_reports} reportes formales y se generaron {len(summaries)}."
    )
if not all(item.get("expectation_met") for item in summaries):
    raise SystemExit("Al menos una ejecucion formal no cumplio su expectativa.")
if not all(item.get("report_present") for item in summaries):
    raise SystemExit("Al menos una herramienta no genero reporte.")
if not all(item.get("metrics_complete") for item in summaries):
    raise SystemExit("Al menos una ejecucion formal carece de metricas obligatorias.")

mutant_runs = [item for item in summaries if item.get("kind") == "mutant"]
if len(mutant_runs) != 10:
    raise SystemExit("No se generaron los diez reportes de mutantes esperados.")
if not all(item.get("actual_outcome") == "failure" for item in mutant_runs):
    raise SystemExit("Al menos un mutante no produjo una violacion detectable.")

counterexamples = sorted(
    str(path.relative_to(result_dir))
    for path in (result_dir / "counterexamples").glob("*")
    if path.is_file()
)
if len(counterexamples) < 10:
    raise SystemExit("No se almacenaron los diez contraejemplos esperados.")

manifest = {
    "schema_version": 2,
    "profile": "formal-research",
    "phase": 6,
    "status": "passed",
    "generated_at_utc": datetime.now(timezone.utc).isoformat(),
    "git_commit": sys.argv[2],
    "bounded_verification": True,
    "valid_configurations": 7,
    "scientific_mutants": {
        "total": 10,
        "tla": 5,
        "alloy": 5,
        "counterexamples": counterexamples,
    },
    "runs": summaries,
    "outputs": [
        "tool_versions.txt",
        "environment.json",
        "tla_runs.csv",
        "alloy_runs.csv",
        "mutant_matrix.csv",
        "execution_manifest.json",
        "logs/",
        "counterexamples/",
    ],
}
(result_dir / "execution_manifest.json").write_text(
    json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
    encoding="utf-8",
)
PY

for output in \
  tool_versions.txt \
  environment.json \
  tla_runs.csv \
  alloy_runs.csv \
  mutant_matrix.csv \
  execution_manifest.json; do
  if [[ ! -s "$RESULT_DIR/$output" ]]; then
    echo "No se genero el resultado obligatorio: $RESULT_DIR/$output" >&2
    exit 1
  fi
done

if ! grep -q ",mutant," "$RESULT_DIR/tla_runs.csv"; then
  echo "No se registraron mutantes TLC." >&2
  exit 1
fi
if ! grep -q ",mutant," "$RESULT_DIR/alloy_runs.csv"; then
  echo "No se registraron mutantes Alloy." >&2
  exit 1
fi

echo "Model checking multisesion y mutantes completado correctamente."
