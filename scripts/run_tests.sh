#!/usr/bin/env bash
set -euo pipefail
mkdir -p build/classes build/test-classes
javac -d build/classes $(find src/main/java -name "*.java")
javac -cp build/classes -d build/test-classes $(find src/test/java -name "*.java")
java -cp build/classes:build/test-classes dltlab.TestRunner
