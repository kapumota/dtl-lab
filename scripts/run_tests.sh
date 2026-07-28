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
java -cp build/classes:build/test-classes dltlab.TestRunner
