# Seguridad y verificacion en DLT-Lab

La fase 6 agrega una capa de seguridad ejecutable. El objetivo no es demostrar seguridad matematica completa, sino convertir propiedades importantes de DLT en pruebas reproducibles, reportes y metricas que puedan ejecutarse localmente y en CI.

## Componentes

```text
PropertyBasedSecuritySuite
  Ejecuta escenarios pseudoaleatorios con una seed fija.

SecurityScoreReport
  Agrega resultados y calcula un security score de 0 a 100.

SecurityReportCsvExporter
  Exporta el reporte a CSV para auditoria y GitHub Actions.

InvariantChecker
  Ejecuta invariantes sobre el estado observable del ledger.
```

## Propiedades evaluadas

```text
- Resistencia a doble gasto UTXO.
- Rechazo de firmas invalidas.
- Validacion de forks y parents.
- Proteccion contra replay cross-shard.
- Limpieza segura de timeouts cross-shard.
- Invariantes runtime sobre genesis, UTXOs, recibos y bloqueos.
```

## Invariantes ejecutables

```text
Genesis unico
  Solo debe existir un bloque genesis sin padre.

UTXO no negativo
  Ningun UTXO observado debe tener valor negativo.

Recibos sin replay
  Un recibo cross-shard no debe consumirse mas de una vez en el mismo shard destino.

Cross-shard sin bloqueos colgados
  Una sesion terminal no debe dejar bloqueado el UTXO origen.
```

## Comando dedicado

```bash
bash scripts/run_security_checks.sh
```

O directamente:

```bash
java -cp build/classes dltlab.app.DltLabCLI security
```

## Reportes generados

```text
reports/security_report.csv
reports/security_report.txt
```

## Como leer el security score

```text
100/100  Todas las propiedades pasaron en todas las iteraciones.
<100     Al menos una propiedad fallo en una o mas iteraciones.
```

Las pruebas usan una seed fija para que el resultado sea reproducible. Si una propiedad falla, el mismo escenario puede repetirse.

## Limite intencional

Esto es property-based testing educativo y runtime verification. No reemplaza una especificacion formal en TLA+, Alloy, Coq o Isabelle. La idea es dejar una base clara para extender el proyecto hacia verificacion formal real.
