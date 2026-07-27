### Alcance del Paper 1

#### Unidad principal de estudio

La unidad principal de estudio es la transferencia de valor cross-shard implementada en el paquete `dltlab.sharding` y modelada parcialmente en TLA+ y Alloy.

El protocolo comprende:

1. validación de la transferencia en el shard origen;
2. bloqueo del UTXO origen;
3. creación de un recibo cross-shard;
4. validación del quorum;
5. consumo del recibo en el shard destino;
6. débito definitivo en origen;
7. crédito en destino;
8. commit, abort o timeout;
9. liberación segura del UTXO cuando la operación no confirma.

#### Componentes incluidos

- `CrossShardTransfer`;
- `CrossShardSession`;
- `CrossShardStatus`;
- `Receipt`;
- `Shard`;
- `ShardValidator`;
- `ShardManager`;
- ataques de replay y timeout;
- invariantes runtime relacionadas con recibos y bloqueos;
- `specs/tla/CrossShardCommit.tla`;
- `specs/tla/CrossShardCommit.cfg`;
- `specs/alloy/CrossShardCommit.als`;
- scripts y documentación de verificación formal;
- pruebas del comportamiento cross-shard.

#### Componentes utilizados solo como infraestructura

Los siguientes componentes pueden utilizarse para construir datos o estados, pero no son contribuciones del Paper 1:

- wallets;
- firmas RSA educativas;
- modelo UTXO general;
- exportadores CSV genéricos;
- CLI;
- visualización ASCII o DOT;
- suite de seguridad general.

#### Componentes excluidos

- mempool, RBF, CPFP y eviction;
- construcción económica de bloques;
- AMM y DeFi;
- front-running, back-running y sandwich;
- selfish mining;
- eclipse attack;
- consenso ponderado por reputación;
- benchmarking con BlockEmulator;
- TPS y latencias de rendimiento;
- workloads históricos de Ethereum;
- despliegue de una red blockchain real.

#### Frontera entre Paper 1 y Paper 2

El Paper 1 estudia corrección, detección de defectos y conformidad del protocolo.

El Paper 2 estudiará rendimiento, sharding, MEV cross-shard, workloads y comparación experimental. Ninguna métrica o implementación específica del Paper 2 se incorporará al Paper 1 salvo que sea necesaria para reproducir una propiedad de seguridad.

#### Relación con `reports`, `results` y `experiments`

- `reports/` conserva salidas operativas generadas por demos y validaciones existentes.
- `results/` almacenará resultados científicos versionados o instrucciones para regenerarlos.
- `experiments/` almacenará configuraciones y scripts de experimentación del paper.

La Fase 0 solo agrega archivos de orientación. No agrega resultados experimentales ni archivos generados.

#### Tipo de afirmación permitida

Los resultados de TLC y Alloy se describirán como verificación acotada sobre las configuraciones documentadas.

No se utilizarán expresiones como "todos los estados posibles" sin indicar el modelo, las constantes y los bounds utilizados.
