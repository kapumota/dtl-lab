#!/usr/bin/env bash
set -euo pipefail
export PYTHONDONTWRITEBYTECODE=1

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TEMP_DIR"' EXIT

snapshot_raw() {
  python3 -B - "$ROOT_DIR/results/experiments/raw" <<'PY'
import hashlib
import sys
from pathlib import Path

root = Path(sys.argv[1])
if not root.exists():
    raise SystemExit(0)
for path in sorted(root.rglob("*")):
    if path.is_file():
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        print(f"{path.relative_to(root)}\t{digest}")
PY
}

RAW_SNAPSHOT_BEFORE="$(snapshot_raw)"

require_file() {
  local path="$1"
  if [[ ! -f "$path" ]]; then
    echo "Falta el archivo obligatorio: $path." >&2
    exit 1
  fi
}

require_text() {
  local path="$1"
  local text="$2"
  if ! grep -Fq -- "$text" "$path"; then
    echo "Falta el texto obligatorio '$text' en $path." >&2
    exit 1
  fi
}

require_blob() {
  local path="$1"
  local expected="$2"
  local actual
  actual="$(git -C "$ROOT_DIR" hash-object "$ROOT_DIR/$path")"
  if [[ "$actual" != "$expected" ]]; then
    echo "El contrato de la fase anterior cambió: $path." >&2
    exit 1
  fi
}

require_blob \
  "experiments/paper1/experiment-spec.json" \
  "e9f37bbccdfe59976340edbef52875b0da200ae2"
require_blob \
  "experiments/paper1/configurations.csv" \
  "4f89c055e14d0aa4306db42cbe5afbf493cf9bc0"
require_blob \
  "experiments/paper1/seeds.txt" \
  "3dccbd9cff45d88592766293d96b2d63b5bac1e3"
require_blob \
  "docs/research/paper1/PROTOCOLO_EXPERIMENTAL_Q3.md" \
  "aaa7931d8c3f92b51872cc3dcbcca848ced9e2f9"
require_blob \
  "experiments/paper1/cases.json" \
  "c2859f39a5d81ae45992df501e2dc1dd77bc5a19"
require_blob \
  "scripts/experiments/experiment_io.py" \
  "3a7c8531e21509e97002b7efe76672017a1b4428"
require_blob \
  "scripts/experiments/build_experiment_plan.py" \
  "a06ec250d6fac116cb1ab2b36cc22a34ff3f341a"
require_blob \
  "scripts/experiments/scientific_executor.py" \
  "a8fc24587b1fb84b4103138635c195f965753dc1"
require_blob \
  "scripts/experiments/finalize_experiment_run.py" \
  "6d72727c1062bd9705640368050e7eb34aaa1d45"
require_blob \
  "scripts/experiments/validate_experiment_run.py" \
  "40ecdcea446ae939e0e78854a48afd5c1de9df8c"
require_blob \
  "scripts/experiments/run_scientific_matrix.sh" \
  "b4b764ab67494b4e06f694338d71b33b6c946675"

required_files=(
  "docs/research/paper1/ANALISIS_RESULTADOS_EXPERIMENTALES.md"
  "experiments/paper1/analysis-spec.json"
  "scripts/experiments/analysis_statistics.py"
  "scripts/experiments/analysis_svg.py"
  "scripts/experiments/build_experiment_analysis.py"
  "scripts/experiments/run_experiment_analysis.sh"
  "scripts/experiments/check_experiment_analysis_structure.sh"
)

for path in "${required_files[@]}"; do
  require_file "$ROOT_DIR/$path"
done

require_text "$ROOT_DIR/Makefile" "experiment-analysis-structure:"
require_text "$ROOT_DIR/Makefile" "experiment-analysis:"
require_text "$ROOT_DIR/scripts/run_tests.sh" "check_experiment_analysis_structure.sh"
require_text \
  "$ROOT_DIR/docs/research/paper1/ANALISIS_RESULTADOS_EXPERIMENTALES.md" \
  "Los resultados raw son de solo lectura."
require_text \
  "$ROOT_DIR/docs/research/paper1/ANALISIS_RESULTADOS_EXPERIMENTALES.md" \
  "ocho tablas"
require_text \
  "$ROOT_DIR/docs/research/paper1/ANALISIS_RESULTADOS_EXPERIMENTALES.md" \
  "ocho figuras"

python3 -B - "$TEMP_DIR/raw" "$ROOT_DIR" <<'PY'
import json
import sys
from collections import Counter
from pathlib import Path

raw = Path(sys.argv[1])
root = Path(sys.argv[2])
sys.path.insert(0, str(root / "scripts/experiments"))

from experiment_io import (
    calculate_sha256_bytes,
    calculate_sha256_file,
    canonical_json_bytes,
    write_json_atomic,
    write_jsonl_atomic,
)

raw.mkdir(parents=True)
(raw / "snapshots").mkdir()
(raw / "tasks").mkdir()

specs = [
    ("RQ1R4-TLC-SMALL", ["RQ1", "RQ4"], "tlc", "valid", "formal", "NoReceiptReplay", "small", "normal", "measured", 1, None, "completed", "passed", {"states_generated": 120, "distinct_states": 80, "depth": 12, "tool_elapsed_seconds": 0.40, "tool_max_memory_kb": 900}),
    ("RQ1R4-TLC-SMALL", ["RQ1", "RQ4"], "tlc", "valid", "formal", "NoReceiptReplay", "small", "normal", "measured", 2, None, "completed", "passed", {"states_generated": 130, "distinct_states": 85, "depth": 13, "tool_elapsed_seconds": 0.45, "tool_max_memory_kb": 920}),
    ("RQ1R4-TLC-MEDIUM", ["RQ1", "RQ4"], "tlc", "valid", "formal", "DecisionConsistency", "medium", "normal", "measured", 1, None, "completed", "passed", {"states_generated": 420, "distinct_states": 260, "depth": 22, "tool_elapsed_seconds": 0.90, "tool_max_memory_kb": 1500}),
    ("RQ1R4-ALLOY-SMALL", ["RQ1", "RQ4"], "alloy", "valid", "formal", "NoReceiptReplay", "small", "normal", "measured", 1, None, "completed", "passed", {"scope": "6 State", "counterexamples": 0, "solve_duration_ms": 120, "tool_elapsed_seconds": 0.25, "tool_max_memory_kb": 1100}),
    ("RQ1R4-ALLOY-MEDIUM", ["RQ1", "RQ4"], "alloy", "valid", "formal", "DecisionConsistency", "medium", "normal", "measured", 1, None, "completed", "passed", {"scope": "8 State", "counterexamples": 0, "solve_duration_ms": 280, "tool_elapsed_seconds": 0.55, "tool_max_memory_kb": 1600}),
    ("RQ2-TLC-MUTANTS", ["RQ2"], "tlc", "mutant", "formal", "tla-mutant-no-replay", "medium", "normal", "measured", 1, None, "completed", "counterexample", {"states_generated": 60, "distinct_states": 40, "depth": 8, "tool_elapsed_seconds": 0.20, "tool_max_memory_kb": 850}),
    ("RQ2-ALLOY-MUTANTS", ["RQ2"], "alloy", "mutant", "formal", "alloy-mutant-no-replay", "medium", "normal", "measured", 1, None, "completed", "counterexample", {"scope": "8 State", "counterexamples": 1, "solve_duration_ms": 90, "tool_elapsed_seconds": 0.18, "tool_max_memory_kb": 1000}),
    ("RQ3-TLC-VALID", ["RQ3"], "tlc", "valid", "valid", "S01_NORMAL_COMMIT", "catalog", "catalog", "measured", 1, 2026001, "completed", "passed", {"accepted": True, "diagnostic_matches": False, "checked_abstract_steps": 8, "tool_elapsed_seconds": 0.30, "tool_max_memory_kb": 800}),
    ("RQ3-TLC-NEGATIVE", ["RQ3"], "tlc", "mutant", "negative", "M01_COMMIT_FROM_PENDING", "catalog", "catalog", "measured", 1, 2026001, "completed", "counterexample", {"accepted": False, "diagnostic_matches": True, "checked_abstract_steps": 4, "rejected_abstract_step": 3, "rejected_concrete_step": 2, "rejected_action": "Commit", "transfer_id": "t1", "tool_elapsed_seconds": 0.35, "tool_max_memory_kb": 820}),
    ("RQ4-TLC-FAULT-NORMAL", ["RQ4"], "tlc", "valid", "formal", "fault-normal", "medium", "normal", "measured", 1, None, "completed", "passed", {"states_generated": 350, "distinct_states": 230, "depth": 20, "tool_elapsed_seconds": 0.75, "tool_max_memory_kb": 1400}),
    ("RQ4-TLC-FAULT-TIMEOUT", ["RQ4"], "tlc", "valid", "formal", "fault-timeout", "medium", "timeout", "measured", 1, None, "timeout", None, {}),
    ("RQ1R4-TLC-LARGE", ["RQ1", "RQ4"], "tlc", "valid", "formal", "NoReceiptReplay", "large", "normal", "warmup", 1, None, "completed", "passed", {"states_generated": 900, "distinct_states": 600, "depth": 35, "tool_elapsed_seconds": 1.80, "tool_max_memory_kb": 2600}),
]

tasks = []
results = []
for order, spec in enumerate(specs, start=1):
    (
        configuration_id,
        rq_ids,
        tool,
        model_kind,
        case_type,
        case_id,
        bound_profile,
        fault_profile,
        repetition_kind,
        repetition_index,
        seed,
        status,
        logical_outcome,
        metrics,
    ) = spec
    task_id = "__".join(
        [
            configuration_id,
            case_id,
            f"seed-{seed}" if seed is not None else "seed-none",
            repetition_kind,
            f"{repetition_index:02d}",
        ]
    )
    task = {
        "schema_version": 1,
        "protocol_id": "paper1-q3-v1",
        "planned_order": order,
        "task_id": task_id,
        "configuration_id": configuration_id,
        "rq_ids": rq_ids,
        "tool": tool,
        "model_kind": model_kind,
        "bound_profile": bound_profile,
        "fault_profile": fault_profile,
        "case_type": case_type,
        "case_id": case_id,
        "expected_property": (
            "NoReceiptReplay"
            if "no-replay" in case_id.lower()
            else case_id
            if case_type == "formal" and not case_id.startswith("fault-")
            else None
        ),
        "model": None,
        "configuration": None,
        "seed": seed,
        "repetition_kind": repetition_kind,
        "repetition_index": repetition_index,
        "timeout_seconds": 1800,
        "max_rss_mb": 12288,
    }
    task["task_sha256"] = calculate_sha256_bytes(canonical_json_bytes(task))
    tasks.append(task)

    task_dir = raw / "tasks" / task_id
    task_dir.mkdir(parents=True)
    write_json_atomic(task_dir / "task.json", task)
    for filename in ("stdout.txt", "stderr.txt", "time.txt"):
        (task_dir / filename).write_text("", encoding="utf-8")

    payload = {}
    executor_path = ""
    if status == "completed":
        payload = {
            "schema_version": 1,
            "phase": "8C",
            "protocol_id": "paper1-q3-v1",
            "status": "completed",
            "expectation_met": True,
            "task_id": task_id,
            "configuration_id": configuration_id,
            "rq_ids": rq_ids,
            "tool": tool.upper(),
            "case_id": case_id,
            "case_type": case_type,
            "model_kind": model_kind,
            "bound_profile": bound_profile,
            "fault_profile": fault_profile,
            "repetition_kind": repetition_kind,
            "repetition_index": repetition_index,
            "seed": seed,
            "property": task["expected_property"],
            "logical_outcome": logical_outcome,
            "metrics": metrics,
            "details": {},
            "message": "La tarea científica terminó con el resultado esperado.",
        }
        write_json_atomic(task_dir / "executor-result.json", payload)
        executor_path = f"tasks/{task_id}/executor-result.json"

    elapsed = round(0.5 + order * 0.1, 6)
    result = {
        "schema_version": 1,
        "protocol_id": "paper1-q3-v1",
        "task_id": task_id,
        "task_sha256": task["task_sha256"],
        "status": status,
        "started_at_utc": "2026-08-04T00:00:00+00:00",
        "finished_at_utc": "2026-08-04T00:00:01+00:00",
        "elapsed_seconds": elapsed,
        "max_rss_kb": None if status == "timeout" else 1000 + order * 100,
        "exit_code": 0 if status == "completed" else None,
        "expectation_met": True if status == "completed" else None,
        "stdout_path": f"tasks/{task_id}/stdout.txt",
        "stderr_path": f"tasks/{task_id}/stderr.txt",
        "time_path": f"tasks/{task_id}/time.txt",
        "executor_result_path": executor_path,
        "message": "Resultado sintético para el gate estructural.",
        "executor_payload": payload,
    }
    write_json_atomic(task_dir / "result.json", result)
    results.append(
        {
            "task_id": task_id,
            "path": f"tasks/{task_id}/result.json",
            "sha256": calculate_sha256_file(task_dir / "result.json"),
        }
    )

write_jsonl_atomic(raw / "snapshots/plan.jsonl", tasks)
plan_sha = calculate_sha256_file(raw / "snapshots/plan.jsonl")
plan_manifest = {
    "schema_version": 1,
    "phase": "8B",
    "protocol_id": "paper1-q3-v1",
    "status": "planned",
    "plan_sha256": plan_sha,
    "counts": {
        "tasks_total": len(tasks),
        "warmup_tasks": sum(task["repetition_kind"] == "warmup" for task in tasks),
        "measured_tasks": sum(task["repetition_kind"] == "measured" for task in tasks),
    },
}
write_json_atomic(raw / "snapshots/plan-manifest.json", plan_manifest)
write_json_atomic(
    raw / "environment.json",
    {
        "schema_version": 1,
        "phase": "8C",
        "platform": {"system": "Linux", "machine": "x86_64"},
        "python": {"version": "3.12"},
    },
)
write_json_atomic(
    raw / "provenance.json",
    {
        "schema_version": 1,
        "phase": "8C",
        "protocol_id": "paper1-q3-v1",
        "run_kind": "definitive",
        "created_at_utc": "2026-08-04T00:00:00+00:00",
        "plan_sha256": plan_sha,
        "results_are_raw": True,
        "derived_results_generated": False,
    },
)
status_counter = Counter(spec[11] for spec in specs)
write_json_atomic(
    raw / "state.json",
    {
        "schema_version": 1,
        "phase": "8C",
        "protocol_id": "paper1-q3-v1",
        "terminal_results_by_status": dict(sorted(status_counter.items())),
    },
)
write_json_atomic(
    raw / "raw-manifest.json",
    {
        "schema_version": 1,
        "phase": "8C",
        "protocol_id": "paper1-q3-v1",
        "run_kind": "definitive",
        "status": "complete",
        "completed_at_utc": "2026-08-04T00:00:01+00:00",
        "plan_sha256": plan_sha,
        "plan_manifest_sha256": calculate_sha256_file(raw / "snapshots/plan-manifest.json"),
        "environment_sha256": calculate_sha256_file(raw / "environment.json"),
        "provenance_sha256": calculate_sha256_file(raw / "provenance.json"),
        "counts": {
            "tasks_total": len(tasks),
            "results_total": len(tasks),
            "expectation_met": sum(spec[11] == "completed" for spec in specs),
            "by_status": dict(sorted(status_counter.items())),
        },
        "results": results,
        "derived_results_generated": False,
        "tables_generated": False,
        "figures_generated": False,
    },
)
PY

python3 -B "$ROOT_DIR/scripts/experiments/validate_experiment_run.py" \
  --run-dir "$TEMP_DIR/raw" \
  --expect-run-kind definitive

run_analysis() {
  python3 -B "$ROOT_DIR/scripts/experiments/build_experiment_analysis.py" \
    --run-dir "$TEMP_DIR/raw" \
    --experiment-spec "$ROOT_DIR/experiments/paper1/experiment-spec.json" \
    --analysis-spec "$ROOT_DIR/experiments/paper1/analysis-spec.json" \
    --derived-dir "$TEMP_DIR/derived" \
    --tables-dir "$TEMP_DIR/tables" \
    --figures-dir "$TEMP_DIR/figures" \
    --expect-tasks 12
}

snapshot_outputs() {
  python3 -B - "$TEMP_DIR/derived" "$TEMP_DIR/tables" "$TEMP_DIR/figures" <<'PY'
import hashlib
import sys
from pathlib import Path

for root_name in sys.argv[1:]:
    root = Path(root_name)
    for path in sorted(root.rglob("*")):
        if path.is_file():
            digest = hashlib.sha256(path.read_bytes()).hexdigest()
            print(f"{root.name}/{path.relative_to(root)}\t{digest}")
PY
}

run_analysis
OUTPUT_SNAPSHOT_FIRST="$(snapshot_outputs)"
run_analysis
OUTPUT_SNAPSHOT_SECOND="$(snapshot_outputs)"
if [[ "$OUTPUT_SNAPSHOT_FIRST" != "$OUTPUT_SNAPSHOT_SECOND" ]]; then
  echo "La Fase 8D no produce salidas deterministas." >&2
  exit 1
fi

python3 -B - "$TEMP_DIR" <<'PY'
import csv
import json
import sys
from pathlib import Path

root = Path(sys.argv[1])
derived = root / "derived"
tables = root / "tables"
figures = root / "figures"
manifest = json.loads(
    (derived / "derived-manifest.json").read_text(encoding="utf-8")
)
if manifest.get("phase") != "8D":
    raise SystemExit("El manifiesto derivado no declara Fase 8D.")
if manifest.get("raw_results_modified") is not False:
    raise SystemExit("El manifiesto no conserva raw como solo lectura.")
counts = manifest.get("counts", {})
if counts.get("task_rows") != 12:
    raise SystemExit("El dataset sintético debe contener doce tareas.")
if counts.get("measured_rows") != 11:
    raise SystemExit("El dataset sintético debe contener once mediciones.")
if counts.get("tables") != 8 or counts.get("figures") != 8:
    raise SystemExit("Faltan tablas o figuras.")
if len(list(tables.glob("*.csv"))) != 8:
    raise SystemExit("Deben existir ocho tablas CSV.")
if len(list(tables.glob("*.md"))) != 8:
    raise SystemExit("Deben existir ocho tablas Markdown.")
if len(list(figures.glob("*.svg"))) != 8:
    raise SystemExit("Deben existir ocho figuras SVG.")
with (derived / "task-results.csv").open(encoding="utf-8", newline="") as stream:
    rows = list(csv.DictReader(stream))
if len(rows) != 12:
    raise SystemExit("task-results.csv no conserva las doce tareas.")
if sum(row["exclusion_reason"] == "calentamiento" for row in rows) != 1:
    raise SystemExit("El calentamiento no fue separado correctamente.")
findings = json.loads((derived / "rq-findings.json").read_text(encoding="utf-8"))
questions = findings.get("research_questions", {})
if questions.get("RQ2", {}).get("mutation_score") != 1.0:
    raise SystemExit("El mutation score sintético debe ser uno.")
if questions.get("RQ3", {}).get("status") != "respaldada":
    raise SystemExit("La conformidad sintética debe respaldar H3.")
PY

python3 -B - "$ROOT_DIR" <<'PY'
import ast
import sys
import unicodedata
from pathlib import Path

root = Path(sys.argv[1])
python_paths = [
    root / "scripts/experiments/analysis_statistics.py",
    root / "scripts/experiments/analysis_svg.py",
    root / "scripts/experiments/build_experiment_analysis.py",
]
for path in python_paths:
    text = path.read_text(encoding="utf-8")
    ast.parse(text, filename=str(path))
    if "\u2013" in text or "\u2014" in text:
        raise SystemExit(f"{path}: contiene guiones tipográficos.")
    if "=" * 8 in text:
        raise SystemExit(f"{path}: contiene un separador no permitido.")

docs = [
    root / "docs/research/paper1/ANALISIS_RESULTADOS_EXPERIMENTALES.md",
    root / "experiments/paper1/README.md",
    root / "results/experiments/README.md",
]
for path in docs:
    text = path.read_text(encoding="utf-8")
    if "\u2013" in text or "\u2014" in text:
        raise SystemExit(f"{path}: contiene guiones tipográficos.")
    if "=" * 8 in text:
        raise SystemExit(f"{path}: contiene un separador no permitido.")
    inside_code = False
    for line_number, line in enumerate(text.splitlines(), start=1):
        if line.lstrip().startswith("```"):
            inside_code = not inside_code
            continue
        if not inside_code and line.startswith("#"):
            level = len(line) - len(line.lstrip("#"))
            if level not in (3, 4):
                raise SystemExit(
                    f"{path}:{line_number}: título de nivel {level}."
                )
    for character in text:
        if unicodedata.category(character) == "So":
            raise SystemExit(f"{path}: contiene un símbolo no permitido.")
PY

bash -n "$ROOT_DIR/scripts/experiments/run_experiment_analysis.sh"
bash -n "$ROOT_DIR/scripts/experiments/check_experiment_analysis_structure.sh"
python3 -m json.tool "$ROOT_DIR/experiments/paper1/analysis-spec.json" >/dev/null

RAW_SNAPSHOT_AFTER="$(snapshot_raw)"
if [[ "$RAW_SNAPSHOT_BEFORE" != "$RAW_SNAPSHOT_AFTER" ]]; then
  echo "El gate de Fase 8D modificó resultados raw existentes." >&2
  exit 1
fi

echo "La estructura de análisis de Fase 8D pasó correctamente."
