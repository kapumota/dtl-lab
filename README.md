### DLT-Lab

**DLT-Lab** es un laboratorio en Java para estudiar blockchains tipo **Bitcoin**, **ledgers distribuidos**, **mempool strategy**, **MEV**, **sharding**, **consenso** y **seguridad de DLT**. El proyecto está diseñado como un simulador modular, no como una **criptomoneda de producción**, con el objetivo de mostrar de forma ejecutable cómo interactúan los componentes principales de una infraestructura blockchain moderna: **validación de transacciones**, **construcción de bloques**, **estrategias de mempool**, **ordenamiento de transacciones**, **consenso distribuido**, **resiliencia adversarial de red**, **sharding**, **ataques** y **verificación de invariantes**.

#### **Arquitectura general del sistema**

DLT-Lab está organizado como un sistema modular. Cada módulo representa una parte concreta de una blockchain o ledger distribuido. Esta separación permite estudiar cada concepto de forma independiente, pero también permite integrarlos en una simulación completa.

El núcleo del sistema contiene módulos para criptografía, **transacciones**, **UTXO**, **blockchain**, **mempool**, **minería**, **MEV**, **DeFi**, **PoW adversarial**, **red P2P**, **consenso**, sharding, seguridad, verificación, métricas y visualización. Esta arquitectura evita que el proyecto sea una simple colección de clases aisladas. En cambio, lo convierte en una plataforma donde las transacciones pueden ser creadas, validadas, seleccionadas por mineros, incluidas en bloques, propagadas por nodos, divididas entre shards y evaluadas mediante ataques e invariantes.

#### **Estructura del repositorio y automatización**

DLT-Lab incluye scripts para ejecutar las partes principales del sistema. El script de pruebas compila el proyecto y ejecuta los tests, el script de demostración corre una simulación completa y el script de seguridad ejecuta la suite de ataques, invariantes y pruebas pseudoaleatorias. Esta organización permite validar el proyecto sin depender de un entorno de desarrollo específico.

Además, el repositorio incluye integración continua con GitHub Actions. Cada vez que se sube código al repositorio, el workflow ejecuta automáticamente las pruebas, la demo completa y los chequeos de seguridad. Esto ayuda a garantizar que el proyecto siga funcionando después de cualquier modificación.

#### **Blockchain tipo Bitcoin y modelo UTXO**

La primera capa técnica de DLT-Lab implementa una blockchain tipo Bitcoin a nivel elemental. El sistema utiliza el modelo UTXO, es decir, **"Unspent Transaction Output"**. En este modelo no se almacenan balances directos por usuario. En su lugar, el estado del ledger se representa como un conjunto de salidas no gastadas.

Una transacción consume UTXOs existentes y genera nuevos UTXOs. Cada input de una transacción referencia una salida anterior, mientras que cada output define un nuevo valor asociado a una clave pública. Este diseño permite representar de forma clara la propiedad de fondos y facilita la detección de intentos de doble gasto.

El proyecto también incluye firmas digitales RSA para demostrar la propiedad de los fondos. Para gastar un UTXO, el usuario debe firmar la transacción con su clave privada, y el sistema verifica esa firma utilizando la clave pública asociada al output anterior. Así se enseña uno de los principios centrales de Bitcoin: los fondos no se "mueven" directamente, sino que se demuestra criptográficamente el derecho a gastar salidas previas.

El validador de transacciones verifica que los UTXOs reclamados existan, que las firmas sean válidas, que no haya doble gasto, que los valores de salida no sean negativos y que la suma de las salidas no exceda la suma de las entradas. Estas reglas permiten preservar la consistencia del ledger y evitar la creación arbitraria de valor.

#### **Bloques, coinbase, forks y regla de mayor altura**

Sobre el modelo de transacciones se construye el módulo de blockchain. Este módulo incluye un bloque génesis, transacciones coinbase, bloques con referencia al hash del bloque padre, altura de bloque y un árbol de forks.

La presencia de forks es importante porque una blockchain distribuida no siempre crece como una lista lineal. En una red real, diferentes nodos pueden recibir bloques en distinto orden, o pueden producirse bloques competidores casi al mismo tiempo. Por eso, DLT-Lab representa la blockchain como un árbol de bloques, donde varias ramas pueden coexistir temporalmente.

El sistema aplica una regla de selección basada en la mayor altura. La rama con mayor altura se considera la cadena principal. Aunque esta regla es una simplificación educativa de los mecanismos reales usados por blockchains como Bitcoin, permite enseñar claramente cómo se decide qué historial tiene prioridad cuando existen forks.

También se modela la transacción coinbase, que representa la creación de nuevas monedas como recompensa por producir un bloque. Este componente permite estudiar la emisión controlada de valor dentro de la blockchain.

#### **Mempool, fees y estrategias de mineros**

DLT-Lab incorpora una mempool, que es el espacio donde se almacenan transacciones válidas que todavía no han sido incluidas en un bloque. La mempool permite estudiar cómo los mineros o productores de bloques deciden qué transacciones incluir cuando el espacio de bloque es limitado.

El sistema calcula la fee de una transacción como la diferencia entre la suma de sus inputs y la suma de sus outputs. Esto permite comparar estrategias de selección de transacciones. Por ejemplo, una política FIFO selecciona transacciones en el orden en que llegaron, una política de mayor fee prioriza las transacciones con mayor pago individual, una política MEV-aware reordena transacciones para capturar valor adicional; y una política package-aware analiza dependencias entre transacciones.

La Fase 1 agrega realismo económico a esta capa. El proyecto ahora estima el tamaño virtual de una transacción en vBytes, calcula fee rate en sats/vByte y permite construir bloques con una capacidad medida por espacio, no solo por cantidad de transacciones. Esto muestra por qué una transacción con fee absoluto alto puede perder frente a una transacción más pequeña con mejor densidad económica.

La mempool también puede operar con reglas de admisión realistas. `MempoolConfig` define capacidad máxima, fee rate mínimo de relay, activación de RBF y activación de eviction. Si la mempool está llena, `LowestFeeRateEvictionPolicy` descarta primero transacciones de bajo fee rate. Si una transacción nueva gasta el mismo UTXO que una transacción pendiente, `RbfPolicy` permite reemplazarla solo si mejora el fee total y el fee rate.

La selección package-aware es especialmente importante. En una mempool real, una transacción hija puede depender de una transacción padre. Si la hija paga una fee alta pero el padre paga una fee baja, una estrategia que analiza transacciones de forma individual podría ignorar el paquete completo. DLT-Lab modela este problema permitiendo que el minero evalúe grupos de transacciones dependientes y seleccione el conjunto que genera mayor beneficio total. En la Fase 1, esta política también puede respetar límites de vBytes, lo que permite modelar CPFP con mayor precisión.

Este módulo conecta directamente con problemas reales de selección de transacciones, block capacity, optimización de fees y comportamiento económico de los productores de bloques.

### Fase 2: DeFi y MEV con AMM constante

La Fase 2 agrega una capa DeFi para que el MEV deje de ser solo un reordenamiento abstracto de transacciones. El proyecto incorpora un AMM de producto constante basado en `x * y = k`, pools con reservas, fees en basis points, swaps con slippage y price impact, y simuladores para sandwich attack y backrun de arbitraje.

El nuevo modulo permite comparar el swap de una victima sin ataque contra el mismo swap rodeado por operaciones de un bot. De esta forma se calcula la ganancia del atacante, la perdida adicional de la victima y el pago MEV al productor del bloque.

#### **MEV y ordenamiento de transacciones**

DLT-Lab incluye un módulo de MEV básico. MEV significa **"Maximal Extractable Value"** y se refiere al valor adicional que un productor de bloques puede extraer al decidir qué transacciones incluir y en qué orden colocarlas.

El proyecto modela tres escenarios abstractos: **front-running**, **back-running** y **sandwich**. En el **front-running**, una transacción del atacante se coloca antes de una transacción objetivo para capturar una ventaja. En el **back-running**, la transacción del atacante se coloca inmediatamente después de una operación que crea una oportunidad económica. En el **sandwich**, el atacante coloca una transacción antes y otra después de la transacción de la víctima.

DLT-Lab no implementa un **exchange descentralizado** real ni contratos inteligentes complejos. En cambio, modela el efecto económico del ordenamiento de transacciones. Esto permite comparar un orden honesto con un orden  **MEV-aware** y calcular la diferencia en ingresos para el productor del bloque.

El sistema genera métricas específicas de MEV, incluyendo **tipo de oportunidad**, **ingreso bajo orden honesto**, **ingreso bajo orden MEV-aware**, **valor extraído** y **diferencia de revenue**. De esta forma, el proyecto permite estudiar por qué el orden de las transacciones no es un detalle menor, sino una fuente importante de incentivos económicos y riesgos de seguridad.

#### **Consenso avanzado y nodos adversariales**

El módulo de consenso permite simular una red distribuida de nodos. La red se representa mediante un **trust graph**, donde las conexiones indican qué nodos escuchan o confían en otros nodos. Esta estructura permite estudiar cómo la topología de la red afecta la propagación de información y el acuerdo entre participantes.

DLT-Lab incluye varios tipos de nodos. Los nodos honestos propagan información correctamente. Los nodos maliciosos actúan de forma adversarial. Los nodos censores omiten deliberadamente ciertas transacciones. Los nodos equivocadores envían información distinta a diferentes vecinos. Los nodos silenciosos no propagan información.

Con estos comportamientos, el proyecto permite simular situaciones donde la red no es completamente confiable. Se pueden observar efectos como censura, propagación incompleta, desacuerdo entre nodos y dificultad para alcanzar consenso cuando existen participantes adversariales.

El sistema registra métricas por ronda, como número de nodos participantes, proporción de nodos honestos, transacciones aceptadas, transacciones censuradas, radio de acuerdo y evolución del consenso. Estas métricas se exportan a CSV para análisis posterior.

### Fase 3: Resiliencia de red y consenso robusto

La Fase 3 agrega una capa adversarial mas fuerte sobre la red y el consenso. El proyecto incorpora una simulacion de selfish mining en PoW, una topologia P2P con ataque eclipse y un consenso ponderado por reputacion con evidencia explicita de equivocacion.

El modulo `pow` modela poder de hash atacante, bloques privados, bloques publicos, lead privado, bloques huerfanos y revenue relativo. Esto permite mostrar que un atacante puede obtener una recompensa observada diferente de su proporcion de hashrate cuando manipula la publicacion de bloques.

El modulo `network` agrega peers, tabla de vecinos, particiones, latencia de propagacion y aislamiento de victimas. El ataque eclipse se representa como control de los vecinos visibles de una victima, lo que permite medir nodos aislados, bloques ocultos, transacciones censuradas y probabilidad de particion.

El modulo `consensus` agrega mensajes firmados, evidencia de equivocacion, scoring reputacional, slashing implicito y consenso ponderado por reputacion. Si un nodo firma dos mensajes incompatibles para la misma ronda y el mismo topico, se genera `EquivocationEvidence` y se reduce su `ReputationScore`.

### Fase 4: Verificacion formal de commit cross-shard

La Fase 4 agrega especificaciones formales para el protocolo cross-shard. Las pruebas runtime verifican ejecuciones concretas, pero no exploran por si solas todos los interleavings posibles de bloqueo, recibo, commit, aborto y timeout. Por eso se agregan modelos TLA+ y Alloy centrados en las invariantes criticas del protocolo.

El modelo TLA+ se ubica en `specs/tla/` y define estados abstractos para el debito en el shard origen, la creacion del recibo, el consumo del recibo en el destino, la liberacion por timeout y la decision final. El modelo Alloy se ubica en `specs/alloy/` y permite revisar las mismas propiedades desde una perspectiva relacional.

Las invariantes principales son `NoDoubleMint`, `NoValueLoss`, `NoReceiptReplay`, `AtomicCommit` y `TimeoutReleasesFunds`. Estas propiedades expresan que el destino no puede crear valor sin recibo valido, que una sesion terminal no debe perder valor, que un recibo no puede reutilizarse, que una transferencia no puede estar confirmada y abortada a la vez, y que un timeout debe liberar fondos.

#### **Sharding avanzado y transacciones cross-shard**

El módulo de **sharding** divide el **ledger** en varios **shards**. Cada shard mantiene su propio estado, su propio conjunto de UTXOs y su propio grupo de validadores. Esta división permite estudiar cómo un ledger distribuido puede escalar al procesar transacciones en paralelo.

El principal desafío aparece cuando una transacción necesita mover valor de un shard a otro. DLT-Lab modela este caso mediante recibos cross-shard. El shard origen bloquea o consume un UTXO, genera un recibo y el shard destino verifica ese recibo antes de crear el nuevo UTXO correspondiente.

El sistema incluye protección contra **replay**, lo que impide que un mismo recibo **cross-shard** sea utilizado más de una vez. Sin esta protección, un atacante podría intentar presentar el mismo recibo repetidamente para crear valor duplicado en el shard destino.

También se incorporan validadores por **shard**, **quorum** y **timeouts**. El **quorum** representa el mínimo de validadores requerido para aceptar una operación dentro de un shard. Los **timeouts** evitan que una transferencia quede bloqueada indefinidamente. Si una operación cross-shard no se completa dentro del tiempo esperado, el sistema puede marcarla como expirada y liberar de forma segura los recursos bloqueados.

El commit atómico  busca preservar una propiedad esencial: una transferencia cross-shard no debe completarse parcialmente. No debe destruirse valor en el shard origen sin crearlo en el destino, ni debe crearse valor en el destino sin una prueba válida desde el origen.

#### **Seguridad, ataques e invariantes**

La última capa del proyecto convierte a DLT-Lab en un laboratorio de seguridad y verificación. El sistema incluye ataques elementales como **doble gasto**, **firma inválida**, **replay cross-shard**, **timeout cross-shard**, **forks adversariales** y **bloques con padres inválidos**.

Cada ataque está diseñado para comprobar si el sistema mantiene sus propiedades de seguridad. Por ejemplo, un ataque de doble gasto intenta usar el mismo UTXO más de una vez. Un ataque de firma inválida intenta gastar fondos sin autorización criptográfica. Un ataque de **replay cross-shard** intenta reutilizar un recibo ya consumido. Un ataque de timeout verifica que los fondos bloqueados no queden atrapados indefinidamente.

El proyecto también incluye verificación mediante invariantes. Una invariante es una propiedad que debe mantenerse siempre. Entre las invariantes verificadas se encuentran que ningún UTXO sea gastado dos veces, que no se acepten transacciones inválidas, que no existan outputs negativos, que no se cree valor arbitrariamente, que los bloques referencien padres válidos y que los recibos cross-shard no se consuman más de una vez.

Estas verificaciones se realizan en tiempo de ejecución. Después de ejecutar simulaciones o ataques, el sistema inspecciona el estado resultante y genera reportes automáticos indicando qué propiedades se mantuvieron y cuáles fallaron.

#### **Property-based testing y security score**

Además de pruebas unitarias tradicionales, DLT-Lab incluye una suite de pruebas pseudoaleatorias reproducibles. Esta técnica permite generar múltiples escenarios variando transacciones, UTXOs, forks, firmas inválidas, intentos de doble gasto y operaciones cross-shard.

El uso de una semilla fija permite que los escenarios sean reproducibles. Esto es importante porque si una prueba falla, se puede volver a ejecutar exactamente el mismo caso para depurar el problema.

El sistema calcula un security score de 0 a 100. Este puntaje resume el resultado de la suite de seguridad y verificación. Un score de 100 indica que todos los ataques simulados, pruebas pseudoaleatorias e invariantes evaluadas pasaron correctamente.

Este enfoque no reemplaza una verificación formal con herramientas como **TLA+**, **Alloy**, **Coq** o **Isabelle**, pero sí proporciona una base sólida de runtime verification y property-based testing para un proyecto educativo.

#### **Visualización y reportes**

DLT-Lab genera visualizaciones en ASCII y DOT. Las visualizaciones ASCII permiten inspeccionar directamente desde la consola estructuras como forks, shards y redes de consenso. Los archivos DOT pueden ser procesados con Graphviz para producir diagramas visuales más elaborados.

El proyecto también exporta métricas en CSV. Esto permite analizar los resultados con hojas de cálculo, scripts de Python u otras herramientas. Se generan métricas generales del sistema, métricas de MEV, métricas de consenso, métricas de sharding y métricas de seguridad.

Esta capacidad de generar reportes hace que el proyecto sea útil no solo como código, sino también como herramienta de análisis. Permite comparar estrategias, observar tendencias y documentar resultados de simulaciones.

> Revisa la carpeta de [documentación](https://github.com/kapumota/dtl-lab/tree/main/docs) de este proyecto para mayor información.

#### Estructura

```text
src/main/java/dltlab/
  app/             CLI y demo principal
  blockchain/      Bloques, cadena y forks
  consensus/       Trust graph, nodos, reputacion y simulador de consenso
  crypto/          Hashing, firmas y llaves
  mempool/         Mempool y estrategias de seleccion
  metrics/         Exportacion CSV y archivos de reporte
  mev/             Front-running, back-running, sandwich y metricas MEV
  network/         Peers, topologia P2P, propagacion y eclipse attack
  mining/          Minero y construccion de bloques por cantidad o vBytes
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

Requisito: Java 17+.

```bash
bash scripts/run_tests.sh
bash scripts/run_mempool_demo.sh
bash scripts/run_defi_mev_demo.sh
bash scripts/run_adversarial_demo.sh
bash scripts/run_formal_checks.sh
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

#### Comandos

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

#### Flujo recomendado por ramas para la Fase 1

```bash
git checkout main
git pull origin main
git checkout -b fase-1-realismo-economico-mempool
bash scripts/run_tests.sh
bash scripts/run_mempool_demo.sh
bash scripts/run_defi_mev_demo.sh
bash scripts/run_adversarial_demo.sh
git add .
git commit -m "Fase 1: agregar realismo economico de mempool"
git push -u origin fase-1-realismo-economico-mempool
```

Luego se abre un Pull Request hacia `main`. Si el CI pasa, la rama se puede fusionar. La documentación específica está en `docs/fase-1-realismo-economico.md`.

#### Reportes generados

Al ejecutar la demostración se crea la carpeta `reports/` con:

```text
reports/metrics.csv              Metricas generales de la simulacion
reports/mev_metrics.csv          Metricas especificas de MEV
reports/consensus_rounds.csv     Metricas de consenso por ronda
reports/sharding_rounds.csv      Metricas de sharding por ronda
reports/security_report.csv      Reporte CSV de seguridad y verificacion
reports/security_report.txt      Reporte legible de seguridad y verificacion
reports/consensus_network.txt    Red de consenso en ASCII
reports/consensus_network.dot    Red de consenso en formato Graphviz DOT
reports/adversarial_network_report.txt Reporte de selfish mining, eclipse y reputacion
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

#### Ejemplo: package-aware transaction selection

La demo construye un caso donde una transaccion hija tiene fee alto, pero solo es valida si se incluye tambien su padre.

```text
Paquete simple: padre fee=100, hija fee=300000, independiente fee=20000
Politica FIFO: selecciona 2 tx, fee efectivo 20100
Politica Mayor fee primero: selecciona 2 tx, fee efectivo 20100
Politica Paquetes con dependencias: selecciona 2 tx, fee efectivo 300100
```

Esto ilustra por que los mineros reales no solo miran transacciones aisladas: tambien deben analizar paquetes, dependencias y ordenamiento.

#### Ejemplo: MEV

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

#### Ejemplo: consenso avanzado

```text
Nodos: 48, honestos: 34, maliciosos: 14
Censores: 6, equivocadores: 5, silenciosos: 3
Grupo honesto mayoritario final: 34/34
Ratio de acuerdo honesto final: 100.00%
Ultimas metricas por ronda:
  Ronda 8: acuerdo honesto 100.00%, mensajes=2866, grupos=1
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

La fase de sharding modela un protocolo atomico simple:

```text
1. El shard origen valida y bloquea el UTXO.
2. Se crea un recibo cross-shard.
3. El shard destino confirma si alcanza quorum.
4. Si confirma, el origen consume el UTXO y el destino crea el nuevo output.
5. Si expira el timeout o falla el quorum, el origen libera el bloqueo.
```

#### Ejemplo: seguridad y verificacion

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

El workflow incluido ejecuta:

```text
1. Compilacion y pruebas: scripts/run_tests.sh
2. Demostración completa: scripts/run_demo.sh
3. Suite de seguridad y verificacion: scripts/run_security_checks.sh
```

Si esos tres comandos pasan localmente, GitHub Actions deberia pasar tambien.

#### Roadmap sugerido

- Agregar modelo de tamano de bloque y fee rate real.
- Agregar mempool eviction y reemplazo por fee.
- Agregar MEV con mercados AMM simulados y slippage real.
- Agregar selfish mining y eclipse attack mas detallados.
- Agregar consenso por pesos de reputacion y deteccion explicita de equivocation.
- Agregar specs en TLA+ o Alloy para cross-shard commit.
- Agregar dashboard web para visualizar forks, shards y ataques.


#### **Alcance y limitaciones del proyecto**

DLT-Lab debe entenderse como un simulador elemental. No es una criptomoneda real, no implementa todos los detalles de Bitcoin y no debe usarse para manejar fondos reales. El módulo MEV es abstracto. No implementa un DEX real, AMMs ni smart contracts complejos. Su objetivo es mostrar cómo el ordenamiento de transacciones puede generar valor extraíble.

El módulo de sharding también es educativo. El **commit cross-shard**, los **timeouts** y el **quorum** están modelados para enseñar los problemas principales, no para reemplazar protocolos industriales.

El consenso es una simulación. No implementa de forma completa **Nakamoto Consensus**, **PBFT** ni **proof-of-stake**. Sin embargo, permite estudiar conceptos importantes como nodos adversariales, censura, equivocación, propagación de información y convergencia.

La verificación se basa en invariantes en tiempo de ejecución y pruebas pseudoaleatorias. Esto permite detectar muchas clases de errores, aunque no equivale a una prueba formal matemática completa.






