#!/usr/bin/env bash
set -euo pipefail

# Ejecuta las pruebas Java de G1 y G2-P0/P1/P2 sin modificar la campaña histórica.
repo="$(git rev-parse --show-toplevel)"
out="$(realpath -m "${1:-$HOME/dtl-gates-java-evidencia}")"
mkdir -p "$out"
printf 'FAIL: comprobación incompleta.\n' > "$out/ESTADO.txt"

cd "$repo"
if ! git merge-base --is-ancestor 8ced907 HEAD; then
  printf 'ERROR: el commit G1 8ced907 no pertenece al historial de esta rama.\n' >&2
  exit 3
fi

for cls in \
  dltlab.sharding.protocol.CommitAttemptJournalTest \
  dltlab.sharding.protocol.LedgerValueOracleIntegrationTest \
  dltlab.sharding.protocol.CommitAuditIntegrationTest \
  dltlab.conformance.v2.V2BoundaryProjectionTest \
  dltlab.conformance.v2.V2WitnessExportTest \
  dltlab.conformance.v2.V2IntermediateAuditTest; do
  rel="src/test/java/${cls//.//}.java"
  if [[ ! -f "$rel" ]]; then
    printf 'ERROR: falta la prueba obligatoria %s\n' "$rel" >&2
    exit 4
  fi
done

{
  printf 'Fecha: '; date --iso-8601=seconds
  printf 'Commit: '; git rev-parse HEAD
  printf 'Rama: '; git branch --show-current
  printf 'Cambios locales:\n'; git status --short
  printf 'Java:\n'; java -version 2>&1
  printf 'Javac:\n'; javac -version 2>&1
} > "$out/entorno.txt"

bash scripts/run_tests.sh > "$out/regresion.log" 2>&1 || {
  tail -n 50 "$out/regresion.log" >&2
  printf 'FAIL: regresión Java anterior.\n' >&2
  exit 5
}
: > "$out/gates.log"
for cls in \
  dltlab.sharding.protocol.CommitAttemptJournalTest \
  dltlab.sharding.protocol.LedgerValueOracleIntegrationTest \
  dltlab.sharding.protocol.CommitAuditIntegrationTest \
  dltlab.conformance.v2.V2BoundaryProjectionTest \
  dltlab.conformance.v2.V2IntermediateAuditTest; do
  printf 'Prueba: %s\n' "$cls" | tee -a "$out/gates.log"
  java -cp build/classes:build/test-classes "$cls" >> "$out/gates.log" 2>&1 || {
    tail -n 60 "$out/gates.log" >&2
    printf 'FAIL: %s\n' "$cls" >&2
    exit 6
  }
done
printf 'Prueba: V2WitnessExportTest\n' | tee -a "$out/gates.log"
java -cp build/classes:build/test-classes \
  dltlab.conformance.v2.V2WitnessExportTest "$out" \
  >> "$out/gates.log" 2>&1 || {
  tail -n 60 "$out/gates.log" >&2
  printf 'FAIL: exportación de testigos Java.\n' >&2
  exit 7
}

printf 'PASS_JAVA_GATES: regresión y pruebas Java G1/G2-P0/P1/P2. No incluye TLC V2.2 ni refinamiento.\n' > "$out/ESTADO.txt"
(
  cd "$out"
  find . -type f ! -name EVIDENCIA_SHA256SUMS.txt -print0 | \
    sort -z | xargs -0 -r sha256sum > EVIDENCIA_SHA256SUMS.txt
)
cat "$out/ESTADO.txt"
