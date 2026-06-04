### Changelog

Todos los cambios importantes de este proyecto se documentan en este archivo.

El proyecto sigue una evolución por fases y usa tags versionados para preservar hitos técnicos. El formato está inspirado en Keep a Changelog y el versionado sigue una lógica compatible con Semantic Versioning.

#### [1.0.0] - Fase 4: Verificación formal cross-shard

##### Agregado

* Agrega especificación formal TLA+ para el protocolo de commit cross-shard.
* Agrega configuración TLC en `specs/tla/CrossShardCommit.cfg`.
* Agrega modelo Alloy en `specs/alloy/CrossShardCommit.als`.
* Agrega documentación técnica en `docs/formal-verification.md`.
* Agrega script `scripts/run_formal_checks.sh`.
* Agrega validación estructural de especificaciones formales en `TestRunner`.

##### Invariantes modeladas

* `NoDoubleMint`: el destino no puede crear valor sin recibo válido.
* `NoValueLoss`: si el origen debitó fondos, el destino confirma o el origen libera.
* `NoReceiptReplay`: un recibo no puede consumirse más de una vez.
* `AtomicCommit`: una transferencia no puede quedar simultáneamente abortada y confirmada.
* `TimeoutReleasesFunds`: toda sesión vencida libera el UTXO bloqueado del origen.

##### Impacto

* El proyecto pasa de simulación ejecutable a una arquitectura con especificación formal.
* El protocolo cross-shard puede analizarse mediante model checking.
* La versión `1.0.0` representa el cierre funcional de las cuatro fases principales.

#### [0.9.0] - Fase 3: Resiliencia de red y consenso adversarial

##### Agregado

* Agrega módulo `dltlab.pow` para simulación de selfish mining.
* Agrega `HashPowerDistribution`, `SelfishMiningState`, `SelfishMiningStrategy`, `SelfishMiningSimulator` y `MiningRewardMetrics`.
* Agrega módulo `dltlab.network` para modelar peers, tablas de peers, particiones y ataques eclipse.
* Agrega `Peer`, `PeerTable`, `NetworkPartition`, `EclipseAttackSimulator`, `MessagePropagationSimulator` y `EclipseAttackResult`.
* Agrega evidencia explícita de equivocación en consenso.
* Agrega `SignedConsensusMessage`, `EquivocationEvidence`, `ReputationScore`, `ReputationWeightedConsensus`, `SlashingEvent` y `ReputationConsensusResult`.
* Agrega demo adversarial mediante `scripts/run_adversarial_demo.sh`.

##### Impacto

* El proyecto deja de modelar solo nodos honestos o maliciosos de forma abstracta.
* La red ahora puede representar aislamiento, censura, control de peers y particiones.
* El consenso incorpora reputación, evidencia de comportamiento contradictorio y penalización implícita.

#### [0.8.0] - Fase 2: DeFi y MEV con AMM constante

##### Agregado

* Agrega módulo `dltlab.defi`.
* Agrega `Token`, `AmmPool`, `SwapOrder`, `SwapResult`, `ConstantProductMarketMaker`, `SlippageCalculator` y `ArbitrageScenario`.
* Agrega simulación de AMM con producto constante `x * y = k`.
* Agrega cálculo de `amountIn`, `amountOut`, `feeBps`, slippage, price impact y reservas antes y después del swap.
* Agrega `SandwichAttackSimulator` y `BackrunArbitrageSimulator`.
* Agrega `DeFiMEVScenario`, `SandwichAttackResult` y `BackrunArbitrageResult`.
* Agrega demo DeFi MEV mediante `scripts/run_defi_mev_demo.sh`.

##### Impacto

* El MEV deja de ser solo reordenamiento abstracto de transacciones.
* El valor extraíble se calcula desde un mercado simulado.
* El proyecto puede medir pérdida de la víctima, ganancia atacante, impacto de precio, slippage y pago al productor.

#### [0.7.0] - Fase 1: Realismo económico de mempool

##### Agregado

* Agrega tamaño virtual de transacciones en vBytes.
* Agrega cálculo de fee rate en sats/vByte.
* Agrega `FeeRate` y `TransactionSizeEstimator`.
* Agrega `MempoolConfig`, `MempoolEntry` y `MempoolAdmissionResult`.
* Agrega política `FeeRatePolicy`.
* Agrega `RbfPolicy` para reemplazo por fee.
* Agrega `EvictionPolicy` y `LowestFeeRateEvictionPolicy`.
* Agrega `BlockTemplateBuilder` para construir bloques por densidad económica.
* Extiende `PackageAwarePolicy` para soportar CPFP con límites de vBytes.
* Agrega demo específica mediante `scripts/run_mempool_demo.sh`.

##### Impacto

* La mempool deja de comportarse como una cola simple.
* La selección de transacciones pasa de fee absoluto o cantidad de transacciones a densidad económica.
* El bloque puede construirse usando `maxBlockVBytes`.
* La mempool puede controlar `maxMempoolVBytes` y `minRelayFeeRate`.

#### [0.6.0] - Base previa a las fases

##### Existente

* Simulación base de blockchain.
* Wallets, transacciones y UTXO.
* Mempool inicial.
* Minería y construcción de bloques.
* MEV abstracto.
* Consenso adversarial básico.
* Sharding y commit cross-shard.
* Seguridad runtime e invariantes ejecutables.
* Exportación de métricas y reportes.

##### Limitación

* El MEV no estaba conectado todavía a mercados DeFi reales.
* La mempool todavía no modelaba completamente fee rate, RBF, CPFP y eviction.
* El consenso adversarial todavía no incluía reputación explícita ni evidencia formalizada de equivocación.
* La verificación runtime no exploraba todos los interleavings posibles del protocolo cross-shard.
