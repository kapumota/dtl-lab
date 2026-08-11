#!/usr/bin/env bash
set -euo pipefail
mkdir -p build/classes build/test-classes
javac -d build/classes $(find src/main/java -name "*.java")
javac -cp build/classes -d build/test-classes $(find src/test/java -name "*.java")
java -cp build/classes:build/test-classes dltlab.sharding.CrossShardStateMachineTest
java -cp build/classes:build/test-classes dltlab.sharding.TerminalStateTest
java -cp build/classes:build/test-classes dltlab.sharding.InvalidTransitionTest
java -cp build/classes:build/test-classes dltlab.sharding.protocol.AtomicCommitProtocolTest
java -cp build/classes:build/test-classes dltlab.sharding.protocol.AtomicCommitRollbackTest
java -cp build/classes:build/test-classes dltlab.simulation.SimulationDeterminismTest
DTL_SIMULATION_SEEDS=100 java -cp build/classes:build/test-classes dltlab.simulation.SimulationScenarioMatrixTest
java -cp build/classes:build/test-classes dltlab.trace.TraceExportTest
java -cp build/classes:build/test-classes dltlab.conformance.TraceAbstractionTest
java -cp build/classes:build/test-classes dltlab.conformance.TraceReplayGeneratorTest
java -cp build/classes:build/test-classes dltlab.conformance.NegativeTraceCatalogTest
bash scripts/conformance/check_conformance_structure.sh
bash scripts/experiments/check_experimental_structure.sh

unexpected_experiment_paths="$(
  find results/experiments \
    -mindepth 1 \
    -maxdepth 1 \
    ! -name README.md \
    ! -name smoke-v1 \
    ! -name raw \
    ! -name derived \
    ! -name tables \
    ! -name figures \
    ! -name reproduction \
    ! -name '.smoke-v1.lock' \
    -print
)"

if [[ -n "$unexpected_experiment_paths" ]]; then
  echo "Existen rutas experimentales inesperadas:" >&2
  printf '%s\n' "$unexpected_experiment_paths" >&2
  exit 1
fi

if find results/experiments \
  -mindepth 1 \
  -maxdepth 1 \
  \( -name smoke-v1 -o -name raw \) \
  -print -quit |
  grep -q .; then
  echo "Se valida Fase 8B mediante el gate compatible de Fase 8C."
else
  bash scripts/experiments/check_experiment_infrastructure.sh
fi

bash scripts/experiments/check_scientific_matrix_structure.sh
bash scripts/experiments/check_experiment_analysis_structure.sh
bash scripts/experiments/check_independent_reproduction_structure.sh
java -cp build/classes:build/test-classes dltlab.TestRunner
