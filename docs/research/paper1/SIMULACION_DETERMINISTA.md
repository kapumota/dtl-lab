### Simulación determinista de interleavings cross-shard

#### Propósito

La Fase 4 agrega una capa de simulación discreta para reproducir distintos órdenes de eventos sin crear threads y sin consultar el reloj del sistema. La simulación ejecuta el protocolo real de la Fase 3 mediante `ShardManager` y `AtomicCommitProtocol`.

La capa nueva no vuelve a implementar commit, rollback, validación de quorum ni mutaciones del ledger. Su responsabilidad es controlar el orden lógico de llamadas, la entrega de mensajes y los fallos de red reproducibles.

#### Componentes

El paquete `dltlab.simulation` contiene:

- `SimulationClock`: reloj lógico monotónico;
- `ScheduledEvent`: evento con ronda, prioridad y secuencia;
- `EventQueue`: cola ordenada por ronda, prioridad y secuencia;
- `EventScheduler`: planificador sin threads;
- `NetworkMessage`: mensaje de recibo inmutable;
- `NetworkFaultModel`: contrato para decidir entrega, pérdida, retraso o duplicación;
- `DeterministicRandom`: generador SplitMix64 controlado por seed;
- `SimulationRun`: ejecución completa con traza estable;
- `ScenarioCatalog`: construcción de los diez escenarios mínimos.

#### Orden total de eventos

Cada evento se ordena por:

1. ronda lógica;
2. prioridad entera;
3. secuencia de inserción.

Este orden permite cambiar interleavings modificando datos de configuración del escenario, sin cambiar la implementación del protocolo.

No se utilizan:

- `Thread`;
- `ExecutorService`;
- `System.currentTimeMillis`;
- `System.nanoTime`;
- fechas del sistema;
- esperas activas.

#### Eventos observables

La traza puede incluir:

```text
BEGIN_TRANSFER
LOCK_SOURCE
CREATE_RECEIPT
SEND_RECEIPT
DELIVER_RECEIPT
PREPARE_DESTINATION
COMMIT_DESTINATION
EXPIRE_TRANSFER
RELEASE_SOURCE
ABORT_TRANSFER
DUPLICATE_MESSAGE
DROP_MESSAGE
DELAY_MESSAGE
FAIL_VALIDATION
SHARD_OFFLINE
SHARD_ONLINE
```

Los eventos de estado se derivan de `ProtocolEvent`. Los eventos de red y disponibilidad pertenecen exclusivamente a la capa de simulación.

#### Formato de traza

Cada entrada usa un formato textual estable:

```text
secuencia|r=ronda|tipo|transferencia|resultado|detalle
```

Ejemplo:

```text
00003|r=0001|DELIVER_RECEIPT|transfer-id|ACEPTADO|Recibo entregado al shard destino.
```

`SimulationRun.traceHash()` calcula SHA-256 sobre la traza completa. La misma seed, el mismo escenario y la misma versión del código deben producir el mismo texto y el mismo hash.

#### Generador determinista

`DeterministicRandom` implementa SplitMix64 dentro del repositorio. No delega el orden de los escenarios a generadores externos ni al estado global de la JVM.

La seed controla:

- prioridades de eventos concurrentes;
- orden de transferencias bidireccionales;
- ganador de la carrera commit-timeout;
- retrasos y prioridades del modelo de reordenamiento.

#### Modelos de fallos

`NetworkFaultModel` expone:

```java
DeliveryDecision decide(
        NetworkMessage message,
        SimulationClock clock,
        DeterministicRandom random
);
```

Las implementaciones incluidas son:

- `NoFaultModel`;
- `DuplicateReceiptModel`;
- `DelayedReceiptModel`;
- `DroppedReceiptModel`;
- `ReorderedMessageModel`;
- `CommitTimeoutRaceModel`.

`DeliveryDecision` expresa pérdida, retraso, cantidad de copias y ajuste de prioridad. El scheduler aplica la decisión sin modificar el código del protocolo.

#### Semántica de la ronda límite

El timeout explícito del protocolo puede ejecutarse cuando el reloj lógico alcanza la ronda límite. El commit también puede intentarse en esa ronda si el recibo fue entregado.

Esta regla permite representar `S05` mediante dos eventos habilitados en la misma ronda. La prioridad y la secuencia determinan cuál decisión terminal ocurre primero.

El comportamiento automático heredado de `ShardManager.advanceRound()` continúa expirando sesiones cuando la ronda supera el límite. La extensión de esta fase solo hace observable la carrera en el scheduler explícito.

#### Escenarios mínimos

| ID | Escenario | Resultado esperado |
|---|---|---|
| `S01` | commit normal | una sesión `COMMITTED` |
| `S02` | timeout antes de entregar recibo | recibo perdido y sesión `TIMED_OUT` |
| `S03` | recibo duplicado | una entrega aceptada, duplicado rechazado y commit único |
| `S04` | recibo retrasado después del timeout | sesión `TIMED_OUT` |
| `S05` | commit y timeout en la misma ronda | una única decisión `COMMITTED` o `TIMED_OUT` |
| `S06` | dos transferencias sobre el mismo UTXO | un inicio aceptado y otro rechazado |
| `S07` | transferencias A a B y B a A | ambas sesiones `COMMITTED` |
| `S08` | caída temporal del shard destino | recuperación y commit posterior |
| `S09` | quorum insuficiente | sesión `FAILED_VALIDATION` |
| `S10` | múltiples sesiones concurrentes | seis sesiones con interleavings reproducibles |

#### Validación reducida

`scripts/run_tests.sh` ejecuta:

```bash
DTL_SIMULATION_SEEDS=100 \
  java -cp build/classes:build/test-classes \
  dltlab.simulation.SimulationScenarioMatrixTest
```

Esto produce al menos 100 seeds para cada uno de los diez escenarios dentro del flujo usado por CI.

#### Validación local del artefacto

La matriz local se ejecuta con 1000 seeds por escenario:

```bash
bash scripts/run_simulation_matrix.sh 1000
```

El argumento permite ampliar la exploración sin modificar código:

```bash
bash scripts/run_simulation_matrix.sh 5000
```

#### Garantías comprobadas

Las pruebas comprueban:

1. la misma seed produce exactamente la misma traza;
2. el hash de la traza es estable;
3. el reloj no retrocede;
4. el orden de desempate es estable;
5. toda sesión creada termina en un estado terminal;
6. ninguna sesión terminal deja el UTXO origen bloqueado;
7. la carrera commit-timeout explora ambos resultados con diferentes seeds;
8. los diez escenarios pasan con 100 y 1000 seeds por escenario;
9. las pruebas de las Fases 2 y 3 continúan pasando.

#### Alcance y límites

Esta fase no agrega threads reales, latencia física ni comportamiento probabilístico externo. La concurrencia se representa como interleaving de eventos discretos.

TLA+ y Alloy permanecen sin cambios. La traducción de estos escenarios al modelo formal multisesión corresponde a la Fase 6. La serialización para conformidad Java-TLA+ corresponde a la Fase 7.
