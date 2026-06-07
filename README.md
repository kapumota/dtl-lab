### DLT-Lab

[![CI](https://github.com/kapumota/dtl-lab/actions/workflows/java-ci.yml/badge.svg?branch=main)](https://github.com/kapumota/dtl-lab/actions/workflows/java-ci.yml)
[![validation](https://github.com/kapumota/dtl-lab/actions/workflows/validation.yml/badge.svg?branch=main)](https://github.com/kapumota/dtl-lab/actions/workflows/validation.yml)
![version](https://img.shields.io/badge/version-v1.0.1-orange)
[![license](https://img.shields.io/badge/license-MIT-green)](LICENCE)
![Java](https://img.shields.io/badge/Java-17%2B-blue?logo=openjdk)
![tests](https://img.shields.io/badge/tests-custom%20runner-brightgreen)
![security](https://img.shields.io/badge/security-runtime%20invariants-teal)
![formal](https://img.shields.io/badge/formal-TLA%2B%20%2B%20Alloy-blue)
![demo](https://img.shields.io/badge/demo-CLI%20simulator-blue)

**DLT-Lab** es un laboratorio en Java para estudiar blockchains tipo **Bitcoin**, **ledgers distribuidos**, **mempool strategy**, **MEV**, **DeFi**, **sharding**, **consenso**, **resiliencia adversarial de red**, **seguridad de DLT** y **verificación formal**.

El proyecto está diseñado como un **simulador modular**, no como una criptomoneda de producción. Su objetivo es mostrar de forma ejecutable cómo interactúan los componentes principales de una infraestructura blockchain moderna: validación de transacciones, construcción de bloques, estrategias de mempool, ordenamiento económico de transacciones, mercados AMM simulados, consenso distribuido, ataques de red, sharding, invariantes runtime y especificaciones formales.

DLT-Lab llega a una versión `1.0.0` después de cuatro fases principales:

* Fase 1: realismo económico de mempool.
* Fase 2: DeFi y MEV con AMM constante.
* Fase 3: resiliencia de red y consenso adversarial.
* Fase 4: verificación formal de commit cross-shard.


#### Validación reproducible del software

DLT-Lab se valida mediante un flujo reproducible conectado a GitHub Actions. El workflow `.github/workflows/validation.yml` ejecuta el script principal:

```bash
make validate
```

Este proceso comprueba estructura mínima del repositorio, compilación Java, pruebas automatizadas, demo CLI focalizada, seguridad runtime desde el flujo principal y verificación formal estructural.

La validación local también puede ejecutarse con:

```bash
bash scripts/validate.sh
```

El objetivo de esta sección es que los badges del README no sean solo decorativos: deben representar una validación ejecutable del software.

#### Estado actual del proyecto

DLT-Lab integra actualmente:

* Blockchain tipo Bitcoin con modelo UTXO.
* Firmas RSA para demostrar propiedad de fondos.
* Bloques, coinbase, forks y regla de mayor altura.
* Mempool con fee absoluto, fee rate, vBytes, RBF, CPFP y eviction.
* Construcción de bloques por cantidad de transacciones o por capacidad en vBytes.
* MEV abstracto con front-running, back-running y sandwich.
* MEV económico con AMM de producto constante.
* DeFi simulado con swaps, slippage, price impact y backrun de arbitraje.
* Consenso avanzado con nodos honestos, censores, silenciosos y equivocadores.
* Selfish mining en PoW.
* Ataque eclipse sobre una topología P2P.
* Consenso ponderado por reputación y evidencia explícita de equivocación.
* Sharding con recibos cross-shard, quorum, timeout y protección contra replay.
* Seguridad runtime con ataques e invariantes ejecutables.
* Especificaciones TLA+ y Alloy para commit cross-shard.
* Reportes CSV, TXT, DOT y visualizaciones ASCII.

#### Arquitectura general del sistema

DLT-Lab está organizado como un sistema modular. Cada módulo representa una parte concreta de una blockchain o ledger distribuido. Esta separación permite estudiar cada concepto de forma independiente y también integrarlo en una simulación completa.

El núcleo del sistema contiene módulos para criptografía, transacciones, UTXO, blockchain, mempool, minería, MEV, DeFi, PoW adversarial, red P2P, consenso, sharding, seguridad, verificación, métricas y visualización.

Esta arquitectura evita que el proyecto sea una colección de clases aisladas. En cambio, lo convierte en una plataforma donde las transacciones pueden ser creadas, validadas, seleccionadas por mineros, incluidas en bloques, propagadas por nodos, afectadas por estrategias MEV, divididas entre shards y evaluadas mediante ataques, invariantes y especificaciones formales.

#### Blockchain tipo Bitcoin y modelo UTXO

La primera capa técnica de DLT-Lab implementa una blockchain tipo Bitcoin a nivel elemental. El sistema utiliza el modelo UTXO, es decir, **Unspent Transaction Output**. En este modelo no se almacenan balances directos por usuario. En su lugar, el estado del ledger se representa como un conjunto de salidas no gastadas.

Una transacción consume UTXOs existentes y genera nuevos UTXOs. Cada input referencia una salida anterior, mientras que cada output define un nuevo valor asociado a una clave pública. Este diseño permite representar de forma clara la propiedad de fondos y facilita la detección de intentos de doble gasto.

El proyecto incluye firmas digitales RSA para demostrar la propiedad de los fondos. Para gastar un UTXO, el usuario debe firmar la transacción con su clave privada, y el sistema verifica esa firma utilizando la clave pública asociada al output anterior. Así se enseña uno de los principios centrales de Bitcoin: los fondos no se mueven directamente, sino que se demuestra criptográficamente el derecho a gastar salidas previas.

El validador de transacciones verifica que los UTXOs reclamados existan, que las firmas sean válidas, que no haya doble gasto dentro de la misma transacción, que los valores de salida no sean negativos y que la suma de salidas no exceda la suma de entradas. Estas reglas preservan la consistencia del ledger y evitan la creación arbitraria de valor.

#### Bloques, coinbase, forks y regla de mayor altura

Sobre el modelo de transacciones se construye el módulo de blockchain. Este módulo incluye un bloque génesis, transacciones coinbase, bloques con referencia al hash del bloque padre, altura de bloque y un árbol de forks.

La presencia de forks es importante porque una blockchain distribuida no siempre crece como una lista lineal. En una red real, diferentes nodos pueden recibir bloques en distinto orden, o pueden producirse bloques competidores casi al mismo tiempo. Por eso, DLT-Lab representa la blockchain como un árbol de bloques, donde varias ramas pueden coexistir temporalmente.

El sistema aplica una regla de selección basada en la mayor altura. La rama con mayor altura se considera la cadena principal. Aunque esta regla es una simplificación educativa de mecanismos reales, permite enseñar claramente cómo se decide qué historial tiene prioridad cuando existen forks.

También se modela la transacción coinbase, que representa la creación de nuevas monedas como recompensa por producir un bloque. Este componente permite estudiar la emisión controlada de valor dentro de la blockchain.

#### Fase 1: Realismo económico de mempool

DLT-Lab incorpora una mempool, que es el espacio donde se almacenan transacciones válidas que todavía no han sido incluidas en un bloque. La mempool permite estudiar cómo los mineros o productores de bloques deciden qué transacciones incluir cuando el espacio de bloque es limitado.

La Fase 1 agrega realismo económico a esta capa. El proyecto estima el tamaño virtual de una transacción en vBytes, calcula fee rate en sats/vByte y permite construir bloques con una capacidad medida por espacio, no solo por cantidad de transacciones.

Esto permite mostrar por qué una transacción con fee absoluto alto puede perder frente a una transacción más pequeña con mejor densidad económica.

El sistema incluye:

* `FeeRate`
* `TransactionSizeEstimator`
* `MempoolConfig`
* `MempoolEntry`
* `MempoolAdmissionResult`
* `FeeRatePolicy`
* `RbfPolicy`
* `EvictionPolicy`
* `LowestFeeRateEvictionPolicy`
* `BlockTemplateBuilder`

`MempoolConfig` define capacidad máxima de mempool, capacidad máxima de bloque, fee rate mínimo de relay, activación de RBF y activación de eviction.

Si la mempool está llena, `LowestFeeRateEvictionPolicy` descarta primero transacciones de bajo fee rate. Si una transacción nueva gasta el mismo UTXO que una transacción pendiente, `RbfPolicy` permite reemplazarla solo si mejora el fee total y el fee rate.

La selección package-aware permite modelar CPFP. Si una transacción hija paga una fee alta, pero depende de un padre con fee baja, el minero puede evaluar el paquete completo en lugar de analizar cada transacción de forma aislada.

Este módulo conecta directamente con problemas reales de selección de transacciones, block capacity, optimización de fees y comportamiento económico de productores de bloques.

#### Fase 2: DeFi y MEV con AMM constante

DLT-Lab incluye dos niveles de MEV.

El primer nivel es abstracto. Modela front-running, back-running y sandwich como estrategias de reordenamiento de transacciones.

El segundo nivel, agregado en la Fase 2, incorpora una capa DeFi simulada. Esta capa permite que el MEV deje de ser solo un reordenamiento abstracto y pase a calcularse desde un mercado AMM.

El proyecto incorpora:

* `Token`
* `AmmPool`
* `SwapOrder`
* `SwapResult`
* `ConstantProductMarketMaker`
* `SlippageCalculator`
* `ArbitrageScenario`
* `SandwichAttackSimulator`
* `BackrunArbitrageSimulator`
* `DeFiMEVScenario`

El AMM usa la regla de producto constante:

```text
x * y = k
```

El sistema calcula:

* `amountIn`
* `amountOut`
* `feeBps`
* `slippage`
* `priceImpact`
* `reservesBefore`
* `reservesAfter`

Con esta capa, DLT-Lab puede comparar el swap de una víctima sin ataque contra el mismo swap rodeado por operaciones de un bot. De esta forma se calcula la ganancia del atacante, la pérdida adicional de la víctima y el pago MEV al productor del bloque.

DLT-Lab no implementa contratos inteligentes reales ni un DEX de producción. Sin embargo, sí implementa un AMM simulado suficiente para estudiar slippage, price impact, sandwich attack y backrun de arbitraje.

#### MEV y ordenamiento de transacciones

MEV significa **Maximal Extractable Value** y se refiere al valor adicional que un productor de bloques puede extraer al decidir qué transacciones incluir y en qué orden colocarlas.

DLT-Lab modela tres escenarios clásicos:

* Front-running: una transacción del atacante se coloca antes de una transacción objetivo.
* Back-running: una transacción del atacante se coloca después de una operación que crea oportunidad económica.
* Sandwich: el atacante coloca una transacción antes y otra después de la transacción de la víctima.

El sistema genera métricas específicas de MEV, incluyendo tipo de oportunidad, ingreso bajo orden honesto, ingreso bajo orden MEV-aware, valor extraído, diferencia de revenue, pérdida de la víctima y ganancia del atacante cuando se usa el modelo DeFi.

De esta forma, el proyecto permite estudiar por qué el orden de las transacciones no es un detalle menor, sino una fuente importante de incentivos económicos y riesgos de seguridad.

#### Consenso avanzado y nodos adversariales

El módulo de consenso permite simular una red distribuida de nodos. La red se representa mediante un trust graph, donde las conexiones indican qué nodos escuchan o confían en otros nodos. Esta estructura permite estudiar cómo la topología afecta la propagación de información y el acuerdo entre participantes.

DLT-Lab incluye varios tipos de nodos:

* Nodos honestos.
* Nodos censores.
* Nodos equivocadores.
* Nodos silenciosos.
* Nodos maliciosos.

Los nodos honestos propagan información correctamente. Los censores omiten deliberadamente ciertas transacciones. Los equivocadores envían información contradictoria a diferentes vecinos. Los silenciosos no propagan información.

El sistema registra métricas por ronda, como número de nodos participantes, proporción de nodos honestos, transacciones aceptadas, transacciones censuradas, ratio de acuerdo y evolución del consenso. Estas métricas se exportan a CSV para análisis posterior.

#### Fase 3: Resiliencia de red y consenso robusto

La Fase 3 agrega una capa adversarial más fuerte sobre la red y el consenso. El proyecto incorpora simulación de selfish mining en PoW, topología P2P con ataque eclipse y consenso ponderado por reputación con evidencia explícita de equivocación.

El módulo `pow` modela:

* Poder de hash atacante.
* Poder de hash honesto.
* Bloques privados.
* Bloques públicos.
* Lead privado.
* Bloques huérfanos.
* Revenue esperado.
* Revenue observado.
* Orphan rate.
* Umbral aproximado de rentabilidad.

El módulo `network` agrega:

* Peers.
* Tabla de vecinos.
* Particiones de red.
* Latencia de propagación.
* Aislamiento de víctimas.
* Simulación de eclipse attack.

El ataque eclipse se representa como control de los vecinos visibles de una víctima. Esto permite medir nodos aislados, bloques ocultos, transacciones censuradas y probabilidad de partición.

El módulo `consensus` agrega:

* `SignedConsensusMessage`
* `EquivocationEvidence`
* `ReputationScore`
* `ReputationWeightedConsensus`
* `SlashingEvent`
* `ReputationConsensusResult`

La regla central es:

```text
Si un nodo firma dos mensajes incompatibles para la misma ronda y el mismo tópico,
se genera evidencia de equivocación y se reduce su reputación.
```

Esto permite pasar de una simulación simple de nodos honestos contra nodos maliciosos a un modelo con evidencia, reputación y penalización implícita.

#### Sharding avanzado y transacciones cross-shard

El módulo de sharding divide el ledger en varios shards. Cada shard mantiene su propio estado, su propio conjunto de UTXOs y su propio grupo de validadores. Esta división permite estudiar cómo un ledger distribuido puede escalar al procesar transacciones en paralelo.

El principal desafío aparece cuando una transacción necesita mover valor de un shard a otro. DLT-Lab modela este caso mediante recibos cross-shard. El shard origen bloquea o consume un UTXO, genera un recibo y el shard destino verifica ese recibo antes de crear el nuevo UTXO correspondiente.

El sistema incluye protección contra replay, lo que impide que un mismo recibo cross-shard sea utilizado más de una vez. Sin esta protección, un atacante podría intentar presentar el mismo recibo repetidamente para crear valor duplicado en el shard destino.

También se incorporan validadores por shard, quorum y timeouts. El quorum representa el mínimo de validadores requerido para aceptar una operación dentro de un shard. Los timeouts evitan que una transferencia quede bloqueada indefinidamente. Si una operación cross-shard no se completa dentro del tiempo esperado, el sistema puede marcarla como expirada y liberar de forma segura los recursos bloqueados.

El commit atómico busca preservar una propiedad esencial: una transferencia cross-shard no debe completarse parcialmente. No debe destruirse valor en el shard origen sin crearlo en el destino, ni debe crearse valor en el destino sin una prueba válida desde el origen.

#### Fase 4: Verificación formal de commit cross-shard

La Fase 4 agrega especificaciones formales para el protocolo cross-shard.

Las pruebas runtime verifican ejecuciones concretas, pero no exploran por sí solas todos los interleavings posibles de bloqueo, recibo, commit, aborto y timeout. Por eso se agregan modelos TLA+ y Alloy centrados en las invariantes críticas del protocolo.

El modelo TLA+ se ubica en:

```text
specs/tla/
```

El modelo Alloy se ubica en:

```text
specs/alloy/
```

Las invariantes principales son:

* `NoDoubleMint`: el destino no puede crear valor sin recibo válido.
* `NoValueLoss`: si el origen debitó fondos, el destino confirma o el origen libera.
* `NoReceiptReplay`: un recibo no puede consumirse más de una vez.
* `AtomicCommit`: una transferencia no puede quedar simultáneamente abortada y confirmada.
* `TimeoutReleasesFunds`: toda sesión vencida libera el UTXO bloqueado del origen.

Esta fase no reemplaza las pruebas runtime. Las complementa con una especificación formal que permite razonar sobre estados e interleavings del protocolo cross-shard.

#### Seguridad, ataques e invariantes

La capa de seguridad convierte a DLT-Lab en un laboratorio de pruebas adversariales. El sistema incluye ataques elementales como doble gasto, firma inválida, replay cross-shard, timeout cross-shard, forks adversariales y bloques con padres inválidos.

Cada ataque está diseñado para comprobar si el sistema mantiene sus propiedades de seguridad. Por ejemplo, un ataque de doble gasto intenta usar el mismo UTXO más de una vez. Un ataque de firma inválida intenta gastar fondos sin autorización criptográfica. Un ataque de replay cross-shard intenta reutilizar un recibo ya consumido. Un ataque de timeout verifica que los fondos bloqueados no queden atrapados indefinidamente.

El proyecto también incluye verificación mediante invariantes runtime. Una invariante es una propiedad que debe mantenerse siempre. Entre las invariantes verificadas se encuentran:

* Ningún UTXO debe gastarse dos veces.
* No deben aceptarse transacciones inválidas.
* No deben existir outputs negativos.
* No debe crearse valor arbitrariamente.
* Los bloques deben referenciar padres válidos.
* Los recibos cross-shard no deben consumirse más de una vez.

Estas verificaciones se realizan en tiempo de ejecución. Después de ejecutar simulaciones o ataques, el sistema inspecciona el estado resultante y genera reportes automáticos indicando qué propiedades se mantuvieron.

#### Property-based testing y security score

Además de pruebas unitarias tradicionales, DLT-Lab incluye una suite de pruebas pseudoaleatorias reproducibles. Esta técnica permite generar múltiples escenarios variando transacciones, UTXOs, forks, firmas inválidas, intentos de doble gasto y operaciones cross-shard.

El uso de una semilla fija permite que los escenarios sean reproducibles. Esto es importante porque si una prueba falla, se puede volver a ejecutar exactamente el mismo caso para depurar el problema.

El sistema calcula un security score de 0 a 100. Este puntaje resume el resultado de la suite de seguridad y verificación. Un score de 100 indica que todos los ataques simulados, pruebas pseudoaleatorias e invariantes evaluadas pasaron correctamente.

Este enfoque no reemplaza una prueba formal matemática, pero sí proporciona una base sólida de runtime verification y property-based testing.

#### Visualización y reportes

DLT-Lab genera visualizaciones en ASCII y DOT. Las visualizaciones ASCII permiten inspeccionar directamente desde la consola estructuras como forks, shards y redes de consenso. Los archivos DOT pueden ser procesados con Graphviz para producir diagramas visuales más elaborados.

El proyecto también exporta métricas en CSV y reportes TXT. Esto permite analizar los resultados con hojas de cálculo, scripts de Python u otras herramientas. Se generan métricas generales del sistema, métricas de MEV, métricas de consenso, métricas de sharding, métricas de seguridad y reportes adversariales.

#### Versiones y changelog

El historial de cambios por fase debe documentarse en:

```text
CHANGELOG.md
```

Tags principales:

```text
v0.7.0-fase-1-realismo-economico
v0.8.0-fase-2-defi-amm-mev
v0.9.0-fase-3-red-consenso-adversarial
v1.0.0-fase-4-verificacion-formal-cross-shard
```

#### Estructura

```text
src/main/java/dltlab/
  app/             CLI y demo principal
  blockchain/      Bloques, cadena y forks
  consensus/       Trust graph, nodos, reputacion y simulador de consenso
  crypto/          Hashing, firmas y llaves
  defi/            AMM constante, swaps, slippage y arbitraje
  mempool/         Mempool, fee rate, RBF, CPFP y eviction
  metrics/         Exportacion CSV y archivos de reporte
  mev/             MEV abstracto, sandwich, backrun y MEV DeFi
  mining/          Minero y construccion de bloques por cantidad o vBytes
  network/         Peers, topologia P2P, propagacion y eclipse attack
  pow/             Selfish mining, hashrate y metricas de recompensa
  security/        Ataques, property-based suite, security score y CSV de seguridad
  sharding/        Shards, validadores, commit atomico, recibos y transacciones cross-shard
  transaction/     Transacciones, UTXO y validador
  verification/    Invariantes ejecutables
  visualization/   Visualizaciones ASCII y DOT
  wallet/          Wallets para firmar transacciones

src/test/java/dltlab/
  TestRunner.java  Pruebas sin dependencias externas

specs/
  tla/             Especificacion TLA+ del commit cross-shard
  alloy/           Modelo Alloy del commit cross-shard
```

#### Ejecutar localmente

Requisito:

```text
Java 17+
```

Ejecuta la validación completa:

```bash
bash scripts/run_tests.sh
bash scripts/run_mempool_demo.sh
bash scripts/run_defi_mev_demo.sh
bash scripts/run_adversarial_demo.sh
bash scripts/run_formal_checks.sh
bash scripts/run_demo.sh
bash scripts/run_security_checks.sh
```

También puedes compilar manualmente:

```bash
mkdir -p build/classes
javac -d build/classes $(find src/main/java -name "*.java")
java -cp build/classes dltlab.app.DltLabCLI demo full
```

El proyecto incluye un `pom.xml` básico para estructura Maven, pero los scripts siguen siendo el camino principal porque no requieren dependencias externas.

#### Comandos disponibles

```bash
java -cp build/classes dltlab.app.DltLabCLI demo full
java -cp build/classes dltlab.app.DltLabCLI demo mempool
java -cp build/classes dltlab.app.DltLabCLI demo mev
java -cp build/classes dltlab.app.DltLabCLI demo consensus
java -cp build/classes dltlab.app.DltLabCLI demo adversarial
java -cp build/classes dltlab.app.DltLabCLI demo sharding
java -cp build/classes dltlab.app.DltLabCLI verify
java -cp build/classes dltlab.app.DltLabCLI security
java -cp build/classes dltlab.app.DltLabCLI attack double-spend
java -cp build/classes dltlab.app.DltLabCLI attack invalid-signature
java -cp build/classes dltlab.app.DltLabCLI attack cross-shard-replay
java -cp build/classes dltlab.app.DltLabCLI attack cross-shard-timeout
```

#### Reportes generados

Al ejecutar la demostración se crea la carpeta `reports/` con archivos como:

```text
reports/metrics.csv                       Metricas generales de la simulacion
reports/mev_metrics.csv                   Metricas especificas de MEV
reports/consensus_rounds.csv              Metricas de consenso por ronda
reports/sharding_rounds.csv               Metricas de sharding por ronda
reports/security_report.csv               Reporte CSV de seguridad y verificacion
reports/security_report.txt               Reporte legible de seguridad y verificacion
reports/consensus_network.txt             Red de consenso en ASCII
reports/consensus_network.dot             Red de consenso en formato Graphviz DOT
reports/adversarial_network_report.txt    Reporte de selfish mining, eclipse y reputacion
reports/defi_mev_report.txt               Reporte de MEV economico con AMM
reports/forks.txt                         Arbol de forks en ASCII
reports/forks.dot                         Arbol de forks en formato Graphviz DOT
reports/shards.txt                        Mapa de shards en ASCII
reports/shards.dot                        Mapa de shards en formato Graphviz DOT
```

Los archivos `.dot` pueden renderizarse con Graphviz:

```bash
dot -Tpng reports/forks.dot -o reports/forks.png
dot -Tpng reports/shards.dot -o reports/shards.png
dot -Tpng reports/consensus_network.dot -o reports/consensus_network.png
```

#### Ejemplo: package-aware transaction selection

La demo construye un caso donde una transacción hija tiene fee alto, pero solo es válida si se incluye también su padre.

```text
Paquete simple: padre fee=100, hija fee=300000, independiente fee=20000
Politica FIFO: selecciona 2 tx, fee efectivo 20100
Politica Mayor fee primero: selecciona 2 tx, fee efectivo 20100
Politica Paquetes con dependencias: selecciona 2 tx, fee efectivo 300100
```

Esto ilustra por qué los mineros reales no solo miran transacciones aisladas. También deben analizar paquetes, dependencias, fee rate y ordenamiento.

#### Ejemplo: MEV abstracto

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

#### Ejemplo: MEV con AMM

```text
Escenario: sandwich sobre AMM constante
Swap victima: 50000.000000 USDC -> ETH
Slippage victima sin ataque: 5.034052%
Slippage victima con sandwich: 8.631259%
Ganancia atacante: 1855.669816 USDC
Perdida adicional victima: 0.899302 ETH equivalente a 1798.603010 USDC
Pago al productor: 371.133963 USDC
Revenue productor: fees + pago MEV = 371.133963 USDC
```

#### Ejemplo: consenso avanzado

```text
Nodos: 48, honestos: 34, maliciosos: 14
Censores: 6, equivocadores: 5, silenciosos: 3
Grupo honesto mayoritario final: 34/34
Ratio de acuerdo honesto final: 100.00%
Ultimas metricas por ronda:
  Ronda 8: acuerdo honesto 100.00%, mensajes=2866, grupos=1
```

#### Ejemplo: red adversarial

```text
Selfish mining
Hashrate atacante: 0.35
Bloques aceptados atacante: 41
Bloques aceptados honestos: 59
Orphan rate: 12.00%
Revenue relativo atacante: 41.00%

Eclipse attack
Peers controlados: 8
Nodos aislados: 1
Bloques ocultos: 3
Transacciones censuradas: 5

Consenso con reputacion
Evidencias de equivocacion: 1
Eventos de slashing implicito: 1
```

#### Ejemplo: sharding avanzado

```text
Sesiones cross-shard: 3
Confirmadas: 1
Timeouts: 1
Fallos de validacion: 1
Transferencia 0->1 estado=COMMITTED
Transferencia 1->2 estado=TIMED_OUT
Transferencia 0->2 estado=FAILED_VALIDATION
```

La fase de sharding modela un protocolo atómico simple:

```text
1. El shard origen valida y bloquea el UTXO.
2. Se crea un recibo cross-shard.
3. El shard destino confirma si alcanza quorum.
4. Si confirma, el origen consume el UTXO y el destino crea el nuevo output.
5. Si expira el timeout o falla el quorum, el origen libera el bloqueo.
```

#### Ejemplo: seguridad y verificación

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

#### GitHub Actions

El workflow incluido ejecuta la validación base del proyecto:

```text
1. Compilacion y pruebas.
2. Demostracion completa.
3. Suite de seguridad y verificacion.
```

Localmente se recomienda ejecutar además las demos específicas:

```bash
bash scripts/run_mempool_demo.sh
bash scripts/run_defi_mev_demo.sh
bash scripts/run_adversarial_demo.sh
bash scripts/run_formal_checks.sh
```

Si todos los scripts pasan localmente, el proyecto está en un estado consistente para subir cambios.

#### Flujo recomendado para futuras mejoras

Para cambios futuros se recomienda trabajar por ramas y Pull Request:

```bash
git checkout main
git pull origin main
git checkout -b mejora-nombre-corto

bash scripts/run_tests.sh
bash scripts/run_demo.sh
bash scripts/run_security_checks.sh

git add .
git commit -m "Descripcion breve del cambio"
git push -u origin mejora-nombre-corto
```

Después se abre un Pull Request hacia `main`.

Para cambios pequeños de documentación, como actualizar `README.md` o `CHANGELOG.md`, se puede trabajar directo en `main` si la rama no está protegida. Sin embargo, usar Pull Request mantiene una práctica más profesional.

#### Roadmap futuro

Las cuatro fases principales ya están implementadas. Las mejoras futuras sugeridas son:

* Agregar dashboard web para visualizar forks, shards, mempool, AMM y ataques.
* Agregar benchmarks reproducibles por escenario.
* Agregar integración opcional con TLC en GitHub Actions.
* Agregar integración opcional con Alloy CLI.
* Agregar releases de GitHub asociados a cada tag.
* Agregar más escenarios DeFi con múltiples pools.
* Agregar exportación JSON para reportes.
* Agregar pruebas de regresión para métricas clave.
* Agregar documentación de arquitectura con diagramas renderizados desde DOT.

#### Alcance y limitaciones del proyecto

DLT-Lab debe entenderse como un simulador experimental. No es una criptomoneda real, no implementa todos los detalles de Bitcoin y no debe usarse para manejar fondos reales.

El módulo MEV tiene dos niveles. El primero es abstracto y muestra front-running, back-running y sandwich como reordenamiento. El segundo usa un AMM simulado de producto constante. Aun así, no implementa smart contracts reales, contratos EVM, mempool pública real ni un DEX de producción.

El módulo DeFi es una simulación matemática simplificada. Sirve para estudiar slippage, price impact y arbitraje, pero no reemplaza un protocolo AMM real.

El módulo de sharding también es educativo. El commit cross-shard, los timeouts y el quorum están modelados para enseñar los problemas principales, no para reemplazar protocolos industriales.

El consenso es una simulación. No implementa de forma completa Nakamoto Consensus, PBFT ni proof-of-stake. Sin embargo, permite estudiar conceptos importantes como nodos adversariales, censura, equivocación, propagación de información, reputación, eclipse attack y convergencia.

La verificación se divide en dos niveles. El primer nivel usa invariantes runtime y pruebas pseudoaleatorias. El segundo nivel agrega especificaciones TLA+ y Alloy. Estas especificaciones ayudan a razonar sobre propiedades críticas, pero siguen siendo modelos abstractos y deben mantenerse alineadas con la implementación Java.
