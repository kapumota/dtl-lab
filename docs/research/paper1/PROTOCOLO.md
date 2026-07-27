### Protocolo de commit cross-shard

#### Propósito

Este documento define el contrato conceptual del protocolo de commit cross-shard que será estudiado en el Paper 1. La definición se deriva del comportamiento actual de `ShardManager`, `CrossShardSession`, `CrossShardStatus`, `Receipt`, `Shard` y las invariantes runtime existentes.

La Fase 1 fijó el vocabulario del protocolo. La Fase 2 implementó la máquina de estados Java. La Fase 3 extrae la ejecución atómica a `AtomicCommitProtocol` sin modificar todavía TLA+ y Alloy.

#### Participantes

El protocolo considera los siguientes participantes:

- shard origen;
- shard destino;
- conjunto de validadores del shard origen;
- conjunto de validadores del shard destino;
- coordinador de la sesión cross-shard;
- emisor de la transferencia;
- receptor de la transferencia.

`ShardManager` conserva la administración general de la simulación. `AtomicCommitProtocol` actúa como coordinador de las operaciones cross-shard y recibe sus dependencias mediante `ProtocolContext`. Esta arquitectura sigue siendo centralizada y no se presenta como un protocolo distribuido completo.

#### Activos protegidos

El protocolo protege:

- el UTXO ubicado en el shard origen;
- el valor asociado a la transferencia;
- el recibo cross-shard;
- la unicidad de la decisión terminal;
- la liberación del UTXO cuando la sesión no puede completarse.

#### Entradas principales

Una transferencia cross-shard contiene, como mínimo:

- identificador de transferencia;
- identificador del shard origen;
- identificador del shard destino;
- referencia al UTXO origen;
- monto transferido;
- clave pública del receptor;
- cantidad de rondas antes del timeout.

#### Precondiciones

Una transferencia puede iniciarse cuando:

1. el shard origen y el shard destino son diferentes;
2. el UTXO origen existe;
3. el UTXO origen no está bloqueado por otra sesión;
4. el monto es positivo;
5. el monto no excede el valor disponible;
6. no existe otra sesión con el mismo identificador;
7. el timeout es positivo;
8. el shard origen alcanza el quorum requerido.

#### Flujo exitoso actual

El flujo exitoso implementado es:

1. `ShardManager` delega el inicio a `AtomicCommitProtocol`;
2. el protocolo valida origen, quorum, bloqueo y creación del recibo;
3. la sesión queda en `RECEIPT_CREATED`;
4. `deliverReceipt` registra `RECEIPT_DELIVERED`;
5. `prepareCommit` valida el destino y construye `CommitPlan`;
6. el plan captura un `LedgerSnapshot`;
7. `applyCommit` registra `DESTINATION_PREPARED`;
8. consume el recibo;
9. debita el UTXO origen y crea el cambio;
10. acredita el UTXO destino;
11. registra `COMMITTED`;
12. si ocurre una excepción, `rollback` restaura el snapshot antes de propagar `ProtocolException`.

#### Atomicidad de aplicación

La aplicación está separada en:

```text
prepareCommit
applyCommit
rollback
```

El rollback restaura UTXOs, bloqueo, recibo y checkpoint de sesión. Por tanto, un fallo controlado no deja crédito parcial, débito aislado ni transición terminal falsa.

#### Flujo de timeout actual

El flujo de timeout del baseline es:

1. avanzar el reloj lógico por rondas;
2. identificar sesiones no terminales cuya ronda actual supera la ronda de timeout;
3. desbloquear el UTXO origen;
4. conservar el UTXO original en el shard origen;
5. marcar la sesión como `TIMED_OUT`.

#### Flujo de abort actual

El flujo de abort manual es:

1. localizar una sesión no terminal;
2. desbloquear el UTXO origen;
3. marcar la sesión como `ABORTED`;
4. registrar la razón del abort.

#### Flujo de fallo de validación actual

Una sesión puede terminar como `FAILED_VALIDATION` cuando:

- el shard origen no alcanza quorum;
- el shard destino no alcanza quorum;
- el recibo ya fue consumido en el shard destino.

Cuando el fallo ocurre después del bloqueo del origen, el baseline desbloquea el UTXO antes de terminar la sesión.

#### Decisión terminal

Los estados terminales actuales son:

- `COMMITTED`;
- `ABORTED`;
- `TIMED_OUT`;
- `FAILED_VALIDATION`.

Una sesión terminal no debe aceptar un nuevo commit, abort o timeout.

#### Garantías esperadas

El protocolo pretende preservar:

- ausencia de creación duplicada de valor;
- ausencia de pérdida de valor en estados terminales;
- consumo único de recibos;
- exclusión mutua entre commit y abort;
- liberación del UTXO después de timeout;
- requisito de quorum para las decisiones que dependen de validadores.

Estas garantías son objetivos de investigación. La Fase 1 no afirma todavía que estén demostradas para todos los interleavings.

#### Limitaciones después de la Fase 3

El protocolo ya está separado de `ShardManager` y dispone de rollback ejecutable. Permanecen las siguientes limitaciones:

- no existe una cola explícita de mensajes entre shards;
- la pérdida, duplicación y reordenamiento de red todavía no forman un scheduler;
- el protocolo sigue ejecutándose de forma síncrona;
- el rollback se evalúa con fallos inyectados y no con todos los interleavings concurrentes;
- los eventos todavía no se exportan a un formato de conformidad;
- las propiedades de liveness no están expresadas como fórmulas temporales completas;
- no existe conformidad automatizada entre Java y TLA+.

#### Regla para fases posteriores

Toda modificación de Java, TLA+ o Alloy deberá preservar esta definición o actualizarla explícitamente en el mismo Pull Request. Ninguna fase posterior debe introducir una transición, precondición o garantía nueva sin reflejarla en este documento y en `MATRIZ_DE_TRAZABILIDAD.md`.
