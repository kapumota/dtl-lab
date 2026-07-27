### Paper 1: commit cross-shard formalmente verificado

#### Estado

Este directorio contiene la documentación de investigación del Paper 1 de DLT-Lab.

- Fase actual: Fase 4, interleavings y simulación determinista.
- Estado: scheduler discreto, red reproducible y diez escenarios ejecutables.
- Baseline de investigación: commit `34f4c088b9f5db3e3b54824de69db8589fd06de3`.
- Commit padre de la Fase 4: `d9658d1`.
- Versión visible del software: `v1.1.0-alpha.1`.
- Rama de trabajo: `paper1/fase-4-interleavings-deterministas`.

La Fase 4 agrega simulación discreta sobre el protocolo extraído. El scheduler controla ronda, prioridad, entrega, pérdida, duplicación, retraso y reordenamiento sin threads reales. TLA+ y Alloy permanecen sin cambios.

#### Objetivo del Paper 1

El Paper 1 estudiará un protocolo de commit cross-shard con bloqueo del UTXO origen, creación y consumo de recibos, quorum de validadores, commit, abort y recuperación por timeout.

La contribución esperada no es presentar DLT-Lab completo como un nuevo simulador blockchain. El objeto de estudio es el protocolo cross-shard y la relación entre:

- implementación Java;
- invariantes runtime;
- especificación TLA+;
- modelo Alloy;
- escenarios adversariales reproducibles;
- conformidad acotada entre trazas Java y acciones formales.

#### Documentos de la Fase 0

- `PREGUNTAS_DE_INVESTIGACION.md`: preguntas de investigación y resultados observables.
- `CONTRIBUCIONES.md`: contribuciones esperadas y condiciones para reclamarlas.
- `ALCANCE.md`: componentes incluidos y excluidos.
- `SUPUESTOS.md`: supuestos del protocolo y del entorno de evaluación.
- `MODELO_DE_AMENAZAS.md`: activos, capacidades del adversario y fallos estudiados.
- `HOJA_DE_RUTA.md`: fases técnicas hasta el artefacto reproducible.
- `MATRIZ_DE_TRAZABILIDAD.md`: relación entre propiedades, código, modelos y pruebas.
- `GLOSARIO_BILINGUE.md`: terminología española e inglesa utilizada en el paper.

#### Documentos de la Fase 1

- `PROTOCOLO.md`: contrato conceptual del protocolo cross-shard actual.
- `MAQUINA_DE_ESTADOS.md`: estados y transiciones del baseline y objetivo.
- `PROPIEDADES_DE_SEGURIDAD.md`: propiedades de safety y su estado actual.
- `PROPIEDADES_DE_VIVACIDAD.md`: propiedades temporales y supuestos de fairness.
- `MODELO_DE_FALLOS.md`: clasificación de fallos incluidos y excluidos.
- `MAPEO_JAVA_TLA.md`: correspondencia conceptual entre Java y TLA+.


#### Documentos de las Fases 3 y 4

- `ARQUITECTURA_PROTOCOLO_ATOMICO.md`: separación de responsabilidades, plan de commit, snapshot, puntos de fallo y rollback.
- `SIMULACION_DETERMINISTA.md`: reloj lógico, scheduler, red, modelos de fallos, escenarios y matriz de seeds.

#### Relación con la documentación existente

La documentación de este directorio no reemplaza:

- `docs/architecture.md`;
- `docs/formal-verification.md`;
- `specs/tla/README.md`;
- `README.md`.

Esos archivos describen el software y la verificación formal histórica del repositorio. Este directorio define la conversión del componente cross-shard en un artefacto de investigación.

#### Reglas de trabajo

- Los comentarios y cadenas de texto del código se mantienen en español.
- Las firmas de funciones y los identificadores técnicos nuevos se escriben en inglés.
- Las propiedades formales conservan sus nombres canónicos en inglés.
- Los títulos de la documentación usan `###`.
- Los subtítulos de la documentación usan `####`.
- No se introducen resultados que no hayan sido generados por herramientas o experimentos reproducibles.
- No se afirma verificación completa cuando el resultado corresponde a un espacio de estados acotado.

#### Criterios de cierre de la Fase 0

La Fase 0 se considera completa cuando:

1. Las preguntas de investigación están definidas.
2. El alcance evita mezclar el Paper 1 con el trabajo futuro sobre MEV y benchmarking.
3. Los supuestos y el modelo de amenazas son explícitos.
4. Las contribuciones esperadas se distinguen de los resultados ya obtenidos.
5. La matriz de trazabilidad identifica las brechas entre Java, TLA+, Alloy y pruebas runtime.
6. No se modifica código de producción ni comportamiento existente.
7. `make validate` continúa pasando después de aplicar el parche.

#### Criterios de cierre de la Fase 1

La Fase 1 se considera completa cuando:

1. El flujo actual del protocolo está descrito sin alterar el código.
2. Los estados y transiciones existentes se distinguen de los estados objetivo.
3. Las propiedades de safety y liveness están separadas.
4. El modelo de fallos está delimitado.
5. El mapeo Java-TLA+ identifica correspondencias y brechas sin afirmar refinamiento.
6. La matriz de trazabilidad se actualiza.
7. `make validate` continúa pasando después de aplicar el parche.


#### Implementación de la Fase 2

- `CrossShardStatus` distingue cinco estados no terminales y cuatro estados terminales.
- `TransitionTable` concentra las transiciones permitidas.
- `CrossShardSession.transitionTo` valida estado, acción, razón y tiempo lógico.
- `ProtocolEvent` conserva una traza inmutable por sesión.
- `ShardManager` registra bloqueo, creación, entrega, preparación y commit.
- `NoStuckCrossShardInvariant` usa `isTerminal` para reconocer los nuevos estados intermedios.
- `scripts/run_tests.sh` ejecuta tres suites específicas de la máquina de estados.

#### Criterios de cierre de la Fase 2

La Fase 2 se considera completa cuando:

1. Todas las transiciones principales permitidas tienen pruebas.
2. Las transiciones terminales prohibidas generan `InvalidTransitionException`.
3. El commit directo desde `CREATED` se rechaza.
4. El timeout antes del bloqueo se rechaza.
5. El historial de eventos es ordenado e inmutable.
6. `ShardManager` conserva sus métodos públicos y las pruebas anteriores continúan pasando.
7. `make validate`, `scripts/run_tests.sh` y las validaciones existentes continúan pasando.
8. TLA+ y Alloy permanecen sin cambios en esta fase.


#### Implementación de la Fase 3

- `CrossShardProtocol` define la API del protocolo extraído.
- `AtomicCommitProtocol` controla inicio, entrega, commit, abort, timeout y rollback.
- `ProtocolContext` separa dependencias de ledger, quorum y tiempo lógico.
- `CommitPlan` calcula las mutaciones antes de aplicarlas.
- `LedgerSnapshot` conserva el estado necesario para restauración exacta.
- `CrossShardSession.SessionCheckpoint` restaura estado, razón y eventos.
- `Shard.unmarkReceiptConsumed` revierte el consumo parcial de un recibo.
- `ShardManager` conserva administración y métricas, pero delega la lógica del protocolo.

#### Criterios de cierre de la Fase 3

La Fase 3 se considera completa cuando:

1. `ShardManager` no contiene el algoritmo detallado del commit.
2. El protocolo separa preparación, aplicación y rollback.
3. Un fallo después de consumir el recibo restaura el recibo.
4. Un fallo entre débito y crédito restaura el UTXO origen.
5. Un fallo durante o después del crédito elimina el UTXO parcial.
6. El checkpoint de sesión evita estados y eventos parciales.
7. Un commit exitoso produce una única decisión terminal.
8. Las pruebas de Fase 2 y las invariantes runtime continúan pasando.
9. TLA+ y Alloy permanecen sin cambios.
10. El commit fusionado puede etiquetarse como `v1.1.0-alpha.1`.


#### Implementación de la Fase 4

- `SimulationClock` elimina dependencia del tiempo del sistema.
- `EventQueue` y `EventScheduler` definen un orden total reproducible.
- `DeterministicRandom` implementa SplitMix64 con seed explícita.
- `NetworkFaultModel` controla entrega, pérdida, retraso, duplicación y reordenamiento.
- `SimulationRun` ejecuta el protocolo real y genera trazas estables.
- `ScenarioCatalog` contiene los escenarios `S01` a `S10`.
- `SimulationDeterminismTest` compara texto, hash y estado final.
- `SimulationScenarioMatrixTest` ejecuta la matriz de seeds.

#### Criterios de cierre de la Fase 4

La Fase 4 se considera completa cuando:

1. La misma seed produce exactamente la misma traza y estado final.
2. Ningún componente consulta el reloj del sistema.
3. Los eventos se ordenan por ronda, prioridad y secuencia.
4. Los seis modelos de red son ejecutables y reproducibles.
5. Los escenarios `S01` a `S10` tienen pruebas de resultado.
6. La carrera commit-timeout produce una única decisión terminal.
7. CI ejecuta al menos 100 seeds por escenario.
8. El runner local ejecuta al menos 1000 seeds por escenario.
9. Las pruebas e invariantes de las fases anteriores continúan pasando.
10. TLA+ y Alloy permanecen sin cambios.
