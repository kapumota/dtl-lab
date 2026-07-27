### Protocolo de commit cross-shard

#### Propósito

Este documento define el contrato conceptual del protocolo de commit cross-shard que será estudiado en el Paper 1. La definición se deriva del comportamiento actual de `ShardManager`, `CrossShardSession`, `CrossShardStatus`, `Receipt`, `Shard` y las invariantes runtime existentes.

La Fase 1 fijó el vocabulario del protocolo. La Fase 2 implementa la máquina de estados Java sin modificar todavía las especificaciones TLA+ y Alloy.

#### Participantes

El protocolo considera los siguientes participantes:

- shard origen;
- shard destino;
- conjunto de validadores del shard origen;
- conjunto de validadores del shard destino;
- coordinador de la sesión cross-shard;
- emisor de la transferencia;
- receptor de la transferencia.

En el baseline actual, `ShardManager` actúa como coordinador central de la simulación. Esta decisión se conserva como descripción del estado existente y no se presenta como una arquitectura distribuida completa.

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

1. validar la transferencia en el shard origen;
2. crear una sesión en `CREATED`;
3. comprobar el quorum del shard origen;
4. bloquear el UTXO y registrar `SOURCE_LOCKED`;
5. crear el recibo y registrar `RECEIPT_CREATED`;
6. comprobar que la sesión no haya vencido;
7. comprobar el quorum del shard destino;
8. registrar `RECEIPT_DELIVERED`;
9. marcar el recibo como consumido en el shard destino;
10. registrar `DESTINATION_PREPARED`;
11. debitar el UTXO origen;
12. crear el UTXO correspondiente en el shard destino;
13. crear un UTXO de cambio en el shard origen cuando corresponda;
14. marcar la sesión como `COMMITTED`.

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

#### Limitaciones después de la Fase 2

La máquina de estados ya distingue las etapas principales y conserva eventos por sesión. Permanecen las siguientes limitaciones:

- no existe una cola explícita de mensajes entre shards;
- la pérdida, duplicación y reordenamiento de red no se ejecutan todavía como fallos programables;
- el commit se ejecuta de forma síncrona dentro de `ShardManager`;
- no existe rollback explícito ante una excepción intermedia;
- los eventos todavía no se exportan a un formato de conformidad;
- las propiedades de liveness no están expresadas como fórmulas temporales completas;
- no existe conformidad automatizada entre Java y TLA+.

#### Regla para fases posteriores

Toda modificación de Java, TLA+ o Alloy deberá preservar esta definición o actualizarla explícitamente en el mismo Pull Request. Ninguna fase posterior debe introducir una transición, precondición o garantía nueva sin reflejarla en este documento y en `MATRIZ_DE_TRAZABILIDAD.md`.
