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
TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TEMP_DIR"' EXIT

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
mkdir -p "$LOG_DIR"

cat > "$RESULT_DIR/tla_runs.csv" <<'CSV'
run_id,kind,model,property,result,expected_outcome,tool_exit_code,states_generated,distinct_states,depth,elapsed_seconds,max_memory_kb,tool_version,stdout_path,stderr_path
CSV

cat > "$RESULT_DIR/alloy_runs.csv" <<'CSV'
run_id,kind,model,property,command_type,result,expected_outcome,tool_exit_code,solver,scope,counterexamples,solve_duration_ms,elapsed_seconds,max_memory_kb,states_generated,distinct_states,depth,receipt_path
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
TLA_CFG="$ROOT_DIR/specs/tla/CrossShardCommit.cfg"
ALLOY_SPEC="$ROOT_DIR/specs/alloy/CrossShardCommit.als"

bash "$ROOT_DIR/scripts/formal/run_tlc.sh" \
  "tla-valid-cross-shard" "valid" "$TLA_SPEC" "$TLA_CFG" "success"

bash "$ROOT_DIR/scripts/formal/run_alloy.sh" \
  "alloy-valid-cross-shard" "valid" "$ALLOY_SPEC" "success"

mkdir -p "$TEMP_DIR/tla" "$TEMP_DIR/alloy"
cp "$TLA_SPEC" "$TEMP_DIR/tla/CrossShardCommit.tla"
cp "$TLA_CFG" "$TEMP_DIR/tla/CrossShardCommit.cfg"
cp "$ALLOY_SPEC" "$TEMP_DIR/alloy/CrossShardCommit.als"

python3 - "$TEMP_DIR/tla/CrossShardCommit.tla" <<'PY'
from pathlib import Path
import re
import sys

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")
updated, count = re.subn(
    r"AtomicCommit\s*==\s*~\(committed\s*/\\\s*aborted\)",
    "AtomicCommit == FALSE",
    text,
    count=1,
)
if count != 1:
    raise SystemExit("No se pudo generar el control negativo de TLC.")
path.write_text(updated, encoding="utf-8")
PY

python3 - "$TEMP_DIR/alloy/CrossShardCommit.als" <<'PY'
from pathlib import Path
import re
import sys

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")

updated, assertion_count = re.subn(
    r"assert\s+AtomicCommit\s*\{.*?\n\}",
    "assert AtomicCommit {\n  some none\n}",
    text,
    count=1,
    flags=re.DOTALL,
)
if assertion_count != 1:
    raise SystemExit(
        "No se pudo generar el control negativo de Alloy."
    )

updated, check_count = re.subn(
    r"(?m)^(\s*check\s+AtomicCommit\b.*?\bexpect\s+)0(\s*)$",
    r"\g<1>1\2",
    updated,
    count=1,
)
if check_count != 1:
    raise SystemExit(
        "No se pudo fijar expect 1 para AtomicCommit."
    )

path.write_text(updated, encoding="utf-8")
PY

bash "$ROOT_DIR/scripts/formal/run_tlc.sh" \
  "tla-negative-control" "negative-control" \
  "$TEMP_DIR/tla/CrossShardCommit.tla" "$TEMP_DIR/tla/CrossShardCommit.cfg" "failure"

bash "$ROOT_DIR/scripts/formal/run_alloy.sh" \
  "alloy-negative-control" "negative-control" \
  "$TEMP_DIR/alloy/CrossShardCommit.als" "failure"

python3 - "$RESULT_DIR" "$git_commit" <<'PY'
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

result_dir = Path(sys.argv[1])
summaries = []
for path in sorted((result_dir / "logs").glob("*.summary.json")):
    summaries.append(json.loads(path.read_text(encoding="utf-8")))

if len(summaries) != 4:
    raise SystemExit("No se generaron los cuatro reportes formales esperados.")
if not all(item.get("expectation_met") for item in summaries):
    raise SystemExit("Al menos una ejecucion formal no cumplio su expectativa.")
if not all(item.get("report_present") for item in summaries):
    raise SystemExit("Al menos una herramienta no genero reporte.")

manifest = {
    "schema_version": 1,
    "profile": "formal-research",
    "status": "passed",
    "generated_at_utc": datetime.now(timezone.utc).isoformat(),
    "git_commit": sys.argv[2],
    "negative_controls": {
        "scope": "controles temporales de CI",
        "scientific_mutants_deferred_to": "Fase 6",
    },
    "runs": summaries,
    "outputs": [
        "tool_versions.txt",
        "environment.json",
        "tla_runs.csv",
        "alloy_runs.csv",
        "execution_manifest.json",
        "logs/",
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
  execution_manifest.json; do
  if [[ ! -s "$RESULT_DIR/$output" ]]; then
    echo "No se genero el resultado obligatorio: $RESULT_DIR/$output" >&2
    exit 1
  fi
done

if ! grep -q "negative-control" "$RESULT_DIR/tla_runs.csv"; then
  echo "No se registro el control negativo de TLC." >&2
  exit 1
fi
if ! grep -q "negative-control" "$RESULT_DIR/alloy_runs.csv"; then
  echo "No se registro el control negativo de Alloy." >&2
  exit 1
fi

echo "Model checking cientifico completado correctamente."
