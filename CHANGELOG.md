### Changelog

Todos los cambios importantes de este proyecto se documentan en este archivo.

El proyecto sigue una evolución por fases y usa tags versionados para preservar hitos técnicos. El formato está inspirado en Keep a Changelog y el versionado sigue una lógica compatible con Semantic Versioning.

#### [Unreleased] - Fase 8: evaluación experimental Q3

#### Fase 8B

* Agrega un inventario ejecutable derivado de propiedades, mutantes y trazas existentes.
* Construye un plan determinista de 1272 tareas con hashes SHA-256.
* Agrega un runner serial y reanudable con resultados por tarea.
* Captura ambiente, procedencia, stdout, stderr, tiempo y memoria.
* Separa resultados raw de resultados derivados.
* Agrega un smoke test temporal que valida reanudación sin producir resultados definitivos.
* Mantiene congelados factores, configuraciones, seeds y recursos de Fase 8A.
* Mantiene sin cambios Java, TLA+, Alloy, JSONL y conformidad.

#### Fase 8A

* Congela RQ1 a RQ4 y las hipótesis H1 a H4 antes de los resultados.
* Define factores, niveles y catorce familias de configuración válidas.
* Fija treinta seeds para conformidad multiseed.
* Fija dos calentamientos y diez repeticiones medidas.
* Declara timeout, memoria, hardware, versiones y tratamiento de ejecuciones incompletas.
* Agrega el gate `make experiment-protocol`.
* Mantiene sin cambios Java, TLA+, Alloy y la conformidad de Fase 7.

#### [1.1.0-rc.1] - Fase 7: conformidad acotada basada en trazas

#### Fase 7E

* Agrega `make conformance-research` como perfil científico único.
* Integra los catálogos válido y negativo con una misma seed.
* Genera manifiesto, resumen y matriz reproducibles.
* Registra procedencia Git, versión de TLC y hashes de entrada.
* Publica resultados de conformidad como artefacto de GitHub Actions.
* Cierra RQ3 como conformidad acotada basada en trazas.

#### Fase 7D

* Agrega un corpus determinista de diez trazas corruptas.
* Reutiliza `TraceConformanceChecker` sin modificar el oraculo TLC.
* Exige rechazo y coincidencia de paso, accion y transferencia.
* Agrega `make conformance-negative` y un manifiesto reproducible.
* Cubre commit invalido, replay, credito sin recibo, quorum y topologia.
* Mantiene fuera de alcance la integracion cientifica y CI de Fase 7E.

#### Fase 7C

* Agrega generación determinista de módulos TLA+ para replay.
* Ejecuta TLC como oráculo sobre los operadores reales de `CrossShardCommit`.
* Agrega diagnóstico estructurado de paso, acción y transferencia rechazada.
* Agrega catálogo de replay para los diez escenarios válidos.
* Agrega `make conformance-replay` y resultados reproducibles.
* Mantiene fuera de alcance las mutaciones negativas de Fase 7D.

#### Fase 7B

* Agrega el módulo `dltlab.conformance` para proyectar trazas concretas.
* Agrega estados tipados para las variables del modelo TLA+.
* Agrega acciones abstractas con procedencia y justificación.
* Expande la preparación del destino en consumo de recibo y votos canónicos.
* Rechaza cambios de identidad o topología dentro de una transferencia.
* Agrega pruebas sobre los diez escenarios deterministas.
* Mantiene fuera de alcance la evaluación de `Next`, TLC y la decisión de conformidad.

#### Fase 7A

* Agrega el módulo `dltlab.trace` para registrar y exportar ejecuciones concretas.
* Agrega JSONL versionado con configuración, eventos y estados finales.
* Agrega el esquema `specs/trace/trace-schema-v1.json`.
* Agrega exportación reproducible del catálogo de diez escenarios.
* Agrega hashes separados para contenido y archivo completo.
* Agrega pruebas de determinismo, cobertura de red y ausencia de campos formales.

#### Integración

* Reutiliza `SimulationRun.trace()` para el orden global y las observaciones de red.
* Reutiliza `CrossShardSession.events()` para las transiciones reales del protocolo.
* Consume `TraceExecution` sin modificar el contrato JSONL de Fase 7A.
* No modifica `AtomicCommitProtocol`, `CrossShardSession`, TLA+ ni Alloy.
* Integra trazas concretas, abstracción, replay y mutaciones sin modificar el protocolo ni el modelo formal.

#### [1.1.0-beta.2] - Cierre científico de la Fase 6

#### Agregado

* Agrega `NoValueLossAtTermination` en TLA+ y Alloy.
* Agrega seguimiento de decisión terminal en TLA+.
* Agrega `TerminalStateIrreversibility` en TLA+ y Alloy.
* Registra la propiedad objetivo de cada mutante Alloy.
* Registra commit fuente, commit ejecutado, referencia y contexto de GitHub Actions.
* Agrega documentación de cierre científico previa a la conformidad Java-TLA+.

#### Corregido

* Impide que un mutante Alloy sea aceptado por violar una propiedad distinta de su objetivo.
* Completa `violated_properties` desde los resultados del solver.
* Alinea preguntas, contribuciones, propiedades y mapeo Java-TLA+ con el modelo multisesión vigente.
* Aclara que la liberación posterior a timeout es una invariante acotada de estado y no una prueba general de vivacidad.

#### [1.1.0-beta.1] - Fase 6: modelo multisesión y mutantes científicos

#### Agregado

* Agrega seis configuraciones TLA+ válidas.
* Agrega estados ordenados en Alloy.
* Agrega cinco mutantes TLA+ y cinco mutantes Alloy.
* Agrega diecisiete ejecuciones formales y diez contraejemplos.
* Publica la matriz formal como artefacto de GitHub Actions.

#### [1.1.0-alpha.1] - Fases 2 y 3 del Paper 1

#### Agregado

* Agrega la máquina de estados cross-shard.
* Extrae el protocolo atómico de `ShardManager`.
* Agrega preparación, aplicación, snapshot y rollback reproducible.

#### [1.0.0] - Fase 4: Verificación formal cross-shard

#### Agregado

* Agrega especificación formal TLA+ para el protocolo de commit cross-shard.
* Agrega configuración TLC en `specs/tla/CrossShardCommit.cfg`.
* Agrega modelo Alloy en `specs/alloy/CrossShardCommit.als`.
* Agrega documentación técnica en `docs/formal-verification.md`.
* Agrega script `scripts/run_formal_checks.sh`.
* Agrega validación estructural de especificaciones formales en `TestRunner`.

#### Invariantes modeladas

* `NoDoubleMint`: el destino no puede crear valor sin recibo válido.
* `NoValueLoss`: si el origen debitó fondos, el destino confirma o el origen libera.
* `NoReceiptReplay`: un recibo no puede consumirse más de una vez.
* `AtomicCommit`: una transferencia no puede quedar simultáneamente abortada y confirmada.
* `TimeoutReleasesFunds`: toda sesión vencida libera el UTXO bloqueado del origen.

#### Impacto

* El proyecto pasa de simulación ejecutable a una arquitectura con especificación formal.
* El protocolo cross-shard puede analizarse mediante model checking.
* La versión `1.0.0` representa el cierre funcional de las cuatro fases principales.

#### [0.9.0] - Fase 3: Resiliencia de red y consenso adversarial

#### Agregado

* Agrega módulo `dltlab.pow` para simulación de selfish mining.
* Agrega `HashPowerDistribution`, `SelfishMiningState`, `SelfishMiningStrategy`, `SelfishMiningSimulator` y `MiningRewardMetrics`.
* Agrega módulo `dltlab.network` para modelar peers, tablas de peers, particiones y ataques eclipse.
* Agrega `Peer`, `PeerTable`, `NetworkPartition`, `EclipseAttackSimulator`, `MessagePropagationSimulator` y `EclipseAttackResult`.
* Agrega evidencia explícita de equivocación en consenso.
* Agrega `SignedConsensusMessage`, `EquivocationEvidence`, `ReputationScore`, `ReputationWeightedConsensus`, `SlashingEvent` y `ReputationConsensusResult`.
* Agrega demo adversarial mediante `scripts/run_adversarial_demo.sh`.

#### Impacto

* El proyecto deja de modelar solo nodos honestos o maliciosos de forma abstracta.
* La red ahora puede representar aislamiento, censura, control de peers y particiones.
* El consenso incorpora reputación, evidencia de comportamiento contradictorio y penalización implícita.

#### [0.8.0] - Fase 2: DeFi y MEV con AMM constante

#### Agregado

* Agrega módulo `dltlab.defi`.
* Agrega `Token`, `AmmPool`, `SwapOrder`, `SwapResult`, `ConstantProductMarketMaker`, `SlippageCalculator` y `ArbitrageScenario`.
* Agrega simulación de AMM con producto constante `x * y = k`.
* Agrega cálculo de `amountIn`, `amountOut`, `feeBps`, slippage, price impact y reservas antes y después del swap.
* Agrega `SandwichAttackSimulator` y `BackrunArbitrageSimulator`.
* Agrega `DeFiMEVScenario`, `SandwichAttackResult` y `BackrunArbitrageResult`.
* Agrega demo DeFi MEV mediante `scripts/run_defi_mev_demo.sh`.

#### Impacto

* El MEV deja de ser solo reordenamiento abstracto de transacciones.
* El valor extraíble se calcula desde un mercado simulado.
* El proyecto puede medir pérdida de la víctima, ganancia atacante, impacto de precio, slippage y pago al productor.

#### [0.7.0] - Fase 1: Realismo económico de mempool

#### Agregado

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

#### Impacto

* La mempool deja de comportarse como una cola simple.
* La selección de transacciones pasa de fee absoluto o cantidad de transacciones a densidad económica.
* El bloque puede construirse usando `maxBlockVBytes`.
* La mempool puede controlar `maxMempoolVBytes` y `minRelayFeeRate`.

#### [0.6.0] - Base previa a las fases

#### Existente

* Simulación base de blockchain.
* Wallets, transacciones y UTXO.
* Mempool inicial.
* Minería y construcción de bloques.
* MEV abstracto.
* Consenso adversarial básico.
* Sharding y commit cross-shard.
* Seguridad runtime e invariantes ejecutables.
* Exportación de métricas y reportes.

#### Limitación

* El MEV no estaba conectado todavía a mercados DeFi reales.
* La mempool todavía no modelaba completamente fee rate, RBF, CPFP y eviction.
* El consenso adversarial todavía no incluía reputación explícita ni evidencia formalizada de equivocación.
* La verificación runtime no exploraba todos los interleavings posibles del protocolo cross-shard.
