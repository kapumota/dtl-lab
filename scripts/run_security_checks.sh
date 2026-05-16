#!/usr/bin/env bash
set -euo pipefail
mkdir -p build/classes
javac -d build/classes $(find src/main/java -name "*.java")
java -cp build/classes dltlab.app.DltLabCLI security
