#!/usr/bin/env bash
set -euo pipefail

# Validacion reproducible para DLT-Lab.
# El objetivo es comprobar compilacion, pruebas, demos, seguridad runtime y verificacion formal estructural.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:--Djava.security.egd=file:/dev/./urandom}"

require_file() {
  local path="$1"
  if [[ ! -f "$path" ]]; then
    echo "Error: falta el archivo requerido: $path" >&2
    exit 1
  fi
}

require_dir() {
  local path="$1"
  if [[ ! -d "$path" ]]; then
    echo "Error: falta el directorio requerido: $path" >&2
    exit 1
  fi
}

echo "Validando DLT-Lab..."

echo "1. Verificando estructura minima"
require_file README.md
require_file LICENCE
require_file CHANGELOG.md
require_file pom.xml
require_file .github/workflows/java-ci.yml
require_file scripts/run_tests.sh
require_file scripts/run_demo.sh
require_file scripts/run_security_checks.sh
require_file scripts/run_formal_checks.sh
require_dir src/main/java/dltlab
require_dir src/test/java/dltlab
require_dir specs/tla
require_dir specs/alloy

require_file specs/tla/CrossShardCommit.tla
require_file specs/tla/CrossShardCommit.cfg
require_file specs/alloy/CrossShardCommit.als

echo "2. Verificando herramientas Java"
java -version
javac -version

echo "3. Verificando que no existan artefactos binarios versionados"
if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  if git ls-files | grep -E '(^|/)(build|target|out)(/|$)|\.(class|jar|war|ear)$|\.bak-badges-validation$' > /tmp/dtl_generated_files.txt; then
    echo "Error: hay artefactos generados versionados:" >&2
    cat /tmp/dtl_generated_files.txt >&2
    exit 1
  fi
else
  echo "Repositorio Git no detectado. Se omite la revision de archivos versionados."
fi

echo "4. Compilando con Maven si esta disponible"
if command -v mvn >/dev/null 2>&1; then
  mvn -q -DskipTests compile
else
  echo "Maven no encontrado. Se continua con la compilacion por javac de los scripts del proyecto."
fi

echo "5. Ejecutando pruebas automatizadas"
bash scripts/run_tests.sh

echo "6. Ejecutando demo focalizada de mempool"
bash scripts/run_mempool_demo.sh > /tmp/dtl_mempool_demo.txt
test -s /tmp/dtl_mempool_demo.txt

echo "7. Ejecutando verificacion formal estructural"
bash scripts/run_formal_checks.sh > /tmp/dtl_formal_checks.txt
test -s /tmp/dtl_formal_checks.txt

echo "8. Verificando salidas de validacion"
grep -q "CPFP" /tmp/dtl_mempool_demo.txt
grep -q "Verificacion formal" /tmp/dtl_formal_checks.txt

echo "Validacion completada correctamente."
