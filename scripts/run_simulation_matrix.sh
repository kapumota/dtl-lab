#!/usr/bin/env bash
set -euo pipefail

seeds="${1:-1000}"
if ! [[ "$seeds" =~ ^[1-9][0-9]*$ ]]; then
  echo "La cantidad de seeds debe ser un entero positivo." >&2
  exit 2
fi

mkdir -p build/classes build/test-classes
javac -d build/classes $(find src/main/java -name "*.java")
javac -cp build/classes -d build/test-classes $(find src/test/java -name "*.java")

java -cp build/classes:build/test-classes dltlab.simulation.SimulationDeterminismTest
java -cp build/classes:build/test-classes dltlab.simulation.SimulationScenarioMatrixTest "$seeds"
