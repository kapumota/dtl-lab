### Arquitectura de DLT-Lab

DLT-Lab esta organizado como un laboratorio modular. Cada paquete enseña una idea especifica de blockchain o DLT.

```text
Wallets -> Transactions -> Mempool -> Miner -> BlockChain -> Verification / Security Suite
                         |                         |
                         |                         v
                         |                  Fork Visualization
                         v
                  Mempool Strategies
                         |
                         v
                  Package-aware Selection
                         |
                         v
                  MEV Ordering Lab

Advanced Consensus Lab <---------------------- Metrics CSV / DOT

Sharding Lab -> Atomic Cross-shard Sessions -> Shard Metrics/Visualization
```
#### Principios

1. El modelo base es UTXO, inspirado en Bitcoin.
2. La mempool es explicita para estudiar seleccion de transacciones.
3. El minero puede usar estrategias distintas, incluyendo package-aware y MEV.
4. La blockchain acepta forks y selecciona la rama de mayor altura.
5. El sharding se modela con shards independientes, validadores, recibos cross-shard, quorum, timeouts y sesiones atomicas educativas.
6. La seguridad se ensena mediante ataques reproducibles e invariantes ejecutables.
7. La demo genera reportes para que el comportamiento sea visible y auditable.
8. El consenso avanzado usa un grafo de confianza dirigido con nodos honestos, censores, equivocadores y silenciosos.
9. La fase de seguridad ejecuta ataques, invariantes y pruebas pseudoaleatorias reproducibles.

#### Seleccion package-aware

La politica `PackageAwarePolicy` crea paquetes de transacciones con dependencias internas. Si una transaccion hija gasta una salida creada por una transaccion padre que todavia esta en la mempool, ambas se evaluan como un paquete ordenado.

Esto permite comparar:

```text
Seleccion ingenua:       tx independiente + tx padre
Seleccion package-aware: tx padre + tx hija de alto fee
```

#### Laboratorio MEV

El paquete `dltlab.mev` contiene escenarios reproducibles para estudiar:

```text
- front-running abstracto
- back-running abstracto
- sandwich abstracto
- orden honesto vs orden MEV-aware
- ingreso minero honesto vs ingreso minero MEV-aware
- metricas CSV especificas de MEV
```

El objetivo no es simular toda la economia de Ethereum o un DEX real. El objetivo inicial es hacer visible el problema de ordenamiento: dos bloques con las mismas transacciones pueden tener resultados economicos distintos dependiendo del orden.

#### Sharding avanzado

El paquete `dltlab.sharding` modela transferencias cross-shard con estados visibles:

```text
CREATED -> SOURCE_LOCKED -> RECEIPT_CREATED
RECEIPT_CREATED -> RECEIPT_DELIVERED -> DESTINATION_PREPARED -> COMMITTED
SOURCE_LOCKED o posterior -> TIMED_OUT
cualquier estado no terminal permitido -> ABORTED
cualquier estado no terminal permitido -> FAILED_VALIDATION
```

Cada shard tiene validadores configurables. La transferencia solo avanza si el origen y el destino alcanzan quorum. Cada sesión conserva eventos de transición con tiempo lógico. Si no se confirma antes del timeout, el UTXO origen se libera.

#### Visualizacion

La visualizacion tiene dos formatos:

- ASCII: facil de leer en terminal.
- DOT: util para renderizar con Graphviz.

La demostración escribe ambos formatos en `reports/`.

#### Consenso avanzado

El paquete `dltlab.consensus` contiene una simulacion con:

```text
- trust graph dirigido
- nodos honestos
- nodos censores
- nodos equivocadores
- nodos silenciosos
- metricas por ronda
- visualizacion DOT/ASCII de la red.
```

Un nodo censor elimina una transaccion objetivo de sus propuestas. Un nodo equivocador envia subconjuntos distintos a seguidores distintos. Un nodo silencioso no retransmite nada.

El objetivo educativo es observar convergencia, propagacion, grupos de consenso y efecto de comportamientos maliciosos sin implementar aun un protocolo BFT completo.

#### Seguridad y verificacion

Se agrega una **suite property-based** reproducible con seed fija. Evalua doble gasto, firmas invalidas, validacion de forks, replay cross-shard, timeouts e invariantes runtime. Los resultados se exportan a `reports/security_report.csv` y `reports/security_report.txt`.
