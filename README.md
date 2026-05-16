# DLT-Lab

**DLT-Lab** es un laboratorio educativo en Java para estudiar blockchains tipo Bitcoin, ledgers distribuidos, mempool strategy, MEV, sharding, consenso y seguridad de DLT.

La regla de estilo del proyecto es:

- **Codigo estructural en ingles**: clases, metodos, paquetes y nombres tecnicos.
- **Explicacion y salidas en espanol**: comentarios, mensajes de consola, documentacion y reportes.

> Esta version no incluye PDFs ni carpetas de asignaciones. Es un proyecto limpio y orientado a repositorio.

## Estado de la version v0.6

La **v0.6** completa la fase 6 a nivel educativo e integra todas las fases del laboratorio:

```text
Fase 0  Limpieza, paquetes, scripts, CI y pom.xml basico.
Fase 1  Bitcoin core: UTXO, firmas, doble gasto, bloques, forks, coinbase y mempool.
Fase 2  Mempool y mineros: fees, block capacity, miner strategies y package-aware selection.
Fase 3  MEV basico: front-running, back-running, sandwich y revenue comparison.
Fase 4  Consenso avanzado: trust graph, nodos honestos/maliciosos/censores/equivocadores y metricas.
Fase 5  Sharding avanzado: shards, UTXO por shard, recibos, replay protection, quorum y timeouts.
Fase 6  Seguridad y verificacion: ataques, invariantes, property-based tests y reportes automaticos.
```

## Que muestra

- Validacion de transacciones con modelo UTXO.
- Firmas digitales RSA para demostrar propiedad de fondos.
- Prevencion de doble gasto.
- Blockchain con bloque genesis, coinbase, forks y regla de mayor altura.
- Mempool con estrategias FIFO, mayor fee, MEV simplificado y seleccion **package-aware**.
- Seleccion package-aware con dependencias padre-hijo dentro de la mempool.
- **MEV basico completo** con front-running, back-running y sandwich abstractos.
- Comparacion entre orden honesto y orden MEV-aware.
- Comparacion detallada de ingreso del productor del bloque.
- **Consenso avanzado** con trust graph configurable.
- Nodos honestos, censores, equivocadores y silenciosos.
- Metricas de consenso por ronda y exportacion CSV.
- **Sharding avanzado** con commit atomico educativo, timeouts, fallos de validadores y quorum por shard.
- Recibos cross-shard, proteccion contra replay y liberacion segura de UTXOs bloqueados.
- Laboratorio de ataques: doble gasto, firma invalida, replay cross-shard y timeout cross-shard.
- **Property-based security suite** con escenarios pseudoaleatorios reproducibles.
- **Security score** y reportes automaticos de seguridad/verificacion.
- Visualizaciones ASCII y DOT para forks, shards y red de consenso.
- CI con GitHub Actions.

## Estructura

```text
src/main/java/dltlab/
  app/             CLI y demo principal
  blockchain/      Bloques, cadena y forks
  consensus/       Trust graph, nodos y simulador de consenso
  crypto/          Hashing, firmas y llaves
  mempool/         Mempool y estrategias de seleccion
  metrics/         Exportacion CSV y archivos de reporte
  mev/             Front-running, back-running, sandwich y metricas MEV
  mining/          Minero y construccion de bloques
  security/        Ataques, property-based suite, security score y CSV de seguridad
  sharding/        Shards, validadores, commit atomico, recibos y transacciones cross-shard
  transaction/     Transacciones, UTXO y validador
  verification/    Invariantes ejecutables
  visualization/   Visualizaciones ASCII y DOT
  wallet/          Wallets para firmar transacciones
src/test/java/dltlab/
  TestRunner.java  Pruebas sin dependencias externas
```

## Ejecutar localmente

Requisito: Java 17+.

```bash
bash scripts/run_tests.sh
bash scripts/run_demo.sh
bash scripts/run_security_checks.sh
```

Tambien puedes compilar manualmente:

```bash
mkdir -p build/classes
javac -d build/classes $(find src/main/java -name "*.java")
java -cp build/classes dltlab.app.DltLabCLI demo full
```

El proyecto incluye `pom.xml` basico para estructura Maven, pero los scripts siguen siendo el camino principal porque no requieren dependencias externas.

## Comandos

```bash
java -cp build/classes dltlab.app.DltLabCLI demo full
java -cp build/classes dltlab.app.DltLabCLI demo mev
java -cp build/classes dltlab.app.DltLabCLI demo consensus
java -cp build/classes dltlab.app.DltLabCLI demo sharding
java -cp build/classes dltlab.app.DltLabCLI verify
java -cp build/classes dltlab.app.DltLabCLI security
java -cp build/classes dltlab.app.DltLabCLI attack double-spend
java -cp build/classes dltlab.app.DltLabCLI attack invalid-signature
java -cp build/classes dltlab.app.DltLabCLI attack cross-shard-replay
java -cp build/classes dltlab.app.DltLabCLI attack cross-shard-timeout
```

## Reportes generados

Al ejecutar la demo se crea la carpeta `reports/` con:

```text
reports/metrics.csv              Metricas generales de la simulacion
reports/mev_metrics.csv          Metricas especificas de MEV
reports/consensus_rounds.csv     Metricas de consenso por ronda
reports/sharding_rounds.csv      Metricas de sharding por ronda
reports/security_report.csv      Reporte CSV de seguridad y verificacion
reports/security_report.txt      Reporte legible de seguridad y verificacion
reports/consensus_network.txt    Red de consenso en ASCII
reports/consensus_network.dot    Red de consenso en formato Graphviz DOT
reports/forks.txt                Arbol de forks en ASCII
reports/forks.dot                Arbol de forks en formato Graphviz DOT
reports/shards.txt               Mapa de shards en ASCII
reports/shards.dot               Mapa de shards en formato Graphviz DOT
```

Los archivos `.dot` pueden renderizarse con Graphviz:

```bash
dot -Tpng reports/forks.dot -o reports/forks.png
dot -Tpng reports/shards.dot -o reports/shards.png
dot -Tpng reports/consensus_network.dot -o reports/consensus_network.png
```

## Ejemplo: package-aware transaction selection

La demo construye un caso donde una transaccion hija tiene fee alto, pero solo es valida si se incluye tambien su padre.

```text
Paquete educativo: padre fee=100, hija fee=300000, independiente fee=20000
Politica FIFO: selecciona 2 tx, fee efectivo 20100
Politica Mayor fee primero: selecciona 2 tx, fee efectivo 20100
Politica Paquetes con dependencias: selecciona 2 tx, fee efectivo 300100
```

Esto ilustra por que los mineros reales no solo miran transacciones aisladas: tambien deben analizar paquetes, dependencias y ordenamiento.

## Ejemplo: MEV

```text
Escenario MEV: Sandwich abstracto
  Tipo: sandwich
  Orden honesto: [usuario_swap, tx_normal, bot_compra_antes, bot_venta_despues]
  Orden MEV: [bot_compra_antes, usuario_swap, bot_venta_despues, tx_normal]
  Fees con orden honesto: 16500
  Fees con orden MEV: 16500
  Valor MEV extraido: 80000
  Ingreso minero honesto: 16500
  Ingreso minero MEV-aware: 96500
  Diferencia de ingreso: 80000
```

El modelo MEV es abstracto: no implementa un DEX real, sino que muestra por que el orden de transacciones puede modificar el ingreso economico del productor del bloque.

## Ejemplo: consenso avanzado

```text
Nodos: 48, honestos: 34, maliciosos: 14
Censores: 6, equivocadores: 5, silenciosos: 3
Grupo honesto mayoritario final: 34/34
Ratio de acuerdo honesto final: 100.00%
Ultimas metricas por ronda:
  Ronda 8: acuerdo honesto 100.00%, mensajes=2866, grupos=1
```

## Ejemplo: sharding avanzado

```text
Sesiones cross-shard: 3
Confirmadas: 1
Timeouts: 1
Fallos de validacion: 1
Transferencia 0->1 estado=COMMITTED
Transferencia 1->2 estado=TIMED_OUT
Transferencia 0->2 estado=FAILED_VALIDATION
```

La fase de sharding modela un protocolo atomico educativo:

```text
1. El shard origen valida y bloquea el UTXO.
2. Se crea un recibo cross-shard.
3. El shard destino confirma si alcanza quorum.
4. Si confirma, el origen consume el UTXO y el destino crea el nuevo output.
5. Si expira el timeout o falla el quorum, el origen libera el bloqueo.
```

## Ejemplo: seguridad y verificacion

```text
Reporte de seguridad y verificacion
Seed: 2026
Security score: 100.00/100
[PASS] Resistencia a doble gasto UTXO
[PASS] Resistencia a firmas invalidas
[PASS] Validacion de forks y parents
[PASS] Seguridad cross-shard replay/timeout
[PASS] Invariantes runtime pseudoaleatorias
```

## GitHub Actions

El workflow incluido ejecuta:

```text
1. Compilacion y pruebas: scripts/run_tests.sh
2. Demo completa: scripts/run_demo.sh
3. Suite de seguridad y verificacion: scripts/run_security_checks.sh
```

Si esos tres comandos pasan localmente, GitHub Actions deberia pasar tambien.

## Roadmap sugerido

- Agregar modelo de tamano de bloque y fee rate real.
- Agregar mempool eviction y reemplazo por fee.
- Agregar MEV con mercados AMM simulados y slippage real.
- Agregar selfish mining y eclipse attack mas detallados.
- Agregar consenso por pesos de reputacion y deteccion explicita de equivocation.
- Agregar specs en TLA+ o Alloy para cross-shard commit.
- Agregar dashboard web para visualizar forks, shards y ataques.
