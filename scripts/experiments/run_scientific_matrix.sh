#!/usr/bin/env bash
set -euo pipefail
export PYTHONDONTWRITEBYTECODE=1

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MODE="${1:-smoke}"
RUN_DIR="${2:-}"

if [[ "$MODE" != "smoke" && "$MODE" != "definitive" ]]; then
  echo "Uso: run_scientific_matrix.sh <smoke|definitive> [directorio]." >&2
  exit 2
fi

if [[ -z "$RUN_DIR" ]]; then
  if [[ "$MODE" == "smoke" ]]; then
    RUN_DIR="$ROOT_DIR/results/experiments/smoke-v1"
  else
    RUN_DIR="$ROOT_DIR/results/experiments/raw/paper1-q3-v1"
  fi
fi

TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TEMP_DIR"' EXIT

bash "$ROOT_DIR/scripts/experiments/check_experimental_structure.sh"

python3 -B "$ROOT_DIR/scripts/experiments/build_experiment_plan.py"   --spec "$ROOT_DIR/experiments/paper1/experiment-spec.json"   --configurations "$ROOT_DIR/experiments/paper1/configurations.csv"   --seeds "$ROOT_DIR/experiments/paper1/seeds.txt"   --cases "$ROOT_DIR/experiments/paper1/cases.json"   --output-plan "$TEMP_DIR/full-plan.jsonl"   --output-manifest "$TEMP_DIR/full-plan-manifest.json"

python3 -B "$ROOT_DIR/scripts/experiments/run_experiment_matrix.py"   --repository-root "$ROOT_DIR"   --plan "$TEMP_DIR/full-plan.jsonl"   --manifest "$TEMP_DIR/full-plan-manifest.json"   --run-dir "$TEMP_DIR/dry-run"   --executor "$ROOT_DIR/scripts/experiments/scientific_executor.py"   --dry-run

if [[ "$MODE" == "smoke" ]]; then
  rm -rf "$RUN_DIR"
  python3 -B "$ROOT_DIR/scripts/experiments/build_scientific_smoke_plan.py"     --plan "$TEMP_DIR/full-plan.jsonl"     --manifest "$TEMP_DIR/full-plan-manifest.json"     --profiles "$ROOT_DIR/experiments/paper1/execution-profiles.json"     --output-plan "$TEMP_DIR/plan.jsonl"     --output-manifest "$TEMP_DIR/plan-manifest.json"

  python3 -B "$ROOT_DIR/scripts/experiments/prepare_scientific_runtime.py"     --repository-root "$ROOT_DIR"     --mode smoke     --output "$TEMP_DIR/runtime.json"

  python3 -B "$ROOT_DIR/scripts/experiments/run_experiment_matrix.py"     --repository-root "$ROOT_DIR"     --plan "$TEMP_DIR/plan.jsonl"     --manifest "$TEMP_DIR/plan-manifest.json"     --run-dir "$RUN_DIR"     --executor "$ROOT_DIR/scripts/experiments/scientific_executor.py"     --phase 8C     --smoke

  cp "$TEMP_DIR/runtime.json" "$RUN_DIR/runtime.json"

  python3 -B "$ROOT_DIR/scripts/experiments/validate_experiment_run.py"     --run-dir "$RUN_DIR"     --expect-completed 6     --expect-run-kind scientific_smoke

  python3 -B "$ROOT_DIR/scripts/experiments/finalize_experiment_run.py"     --run-dir "$RUN_DIR"     --expected-tasks 6     --run-kind scientific_smoke

  echo "El smoke científico de Fase 8C pasó correctamente."
  exit 0
fi

mkdir -p "$RUN_DIR"

python3 -B "$ROOT_DIR/scripts/experiments/check_timing_host.py"   --repository-root "$ROOT_DIR"   --output "$RUN_DIR/timing-host.json"

python3 -B "$ROOT_DIR/scripts/experiments/prepare_scientific_runtime.py"   --repository-root "$ROOT_DIR"   --mode definitive   --output "$RUN_DIR/runtime.json"

python3 -B "$ROOT_DIR/scripts/experiments/run_experiment_matrix.py"   --repository-root "$ROOT_DIR"   --plan "$TEMP_DIR/full-plan.jsonl"   --manifest "$TEMP_DIR/full-plan-manifest.json"   --run-dir "$RUN_DIR"   --executor "$ROOT_DIR/scripts/experiments/scientific_executor.py"   --phase 8C

python3 -B "$ROOT_DIR/scripts/experiments/validate_experiment_run.py"   --run-dir "$RUN_DIR"   --expect-run-kind definitive

python3 -B "$ROOT_DIR/scripts/experiments/finalize_experiment_run.py"   --run-dir "$RUN_DIR"   --expected-tasks 1272   --run-kind definitive

echo "La matriz científica de Fase 8C terminó correctamente."
echo "Resultados raw: $RUN_DIR."
