### Propiedades de seguridad

#### Propósito

Este documento define las propiedades de safety del Paper 1. Una propiedad de safety expresa que un estado incorrecto nunca debe alcanzarse.

Las propiedades se clasifican como existentes, parciales o propuestas. La clasificación evita atribuir al baseline garantías que todavía no han sido ejecutadas o demostradas.

#### S1. NoDoubleMint

Si existe crédito en el shard destino, debe existir un recibo válido asociado a una transferencia y ese recibo debe consumirse una sola vez.

Estado actual:

- declarada en TLA+ y Alloy;
- relacionada con la protección runtime contra replay;
- no verificada todavía sobre múltiples transferencias y múltiples shards.

#### S2. NoValueLossAtTermination

Cuando una sesión alcanza un estado terminal después de bloquear o debitar el origen, el valor debe encontrarse en el destino o debe estar nuevamente disponible en el origen.

Estado actual:

- aproximada por `NoValueLoss` en TLA+;
- aproximada por la liberación de UTXO en abort, timeout y fallos de validación;
- no cubre todavía excepciones intermedias ni rollback.

#### S3. NoReceiptReplay

Un recibo cross-shard puede consumirse como máximo una vez en el shard destino.

Estado actual:

- protección implementada mediante el registro de recibos consumidos;
- ataque runtime de replay existente;
- invariante runtime existente;
- propiedad declarada en TLA+ y Alloy;
- falta estudiar duplicación y reordenamiento mediante una red explícita de mensajes.

#### S4. DecisionConsistency

Una sesión no puede terminar simultáneamente como commit y abort. Tampoco puede adoptar más de una decisión terminal a lo largo de su ejecución.

Estado actual:

- `AtomicCommit` evita la combinación abstracta de commit y abort en el modelo TLA+;
- `isTerminal` limita nuevas operaciones sobre sesiones terminales en Java;
- falta una tabla de transiciones ejecutable y una propiedad de irreversibilidad.

#### S5. TerminalStateIrreversibility

Una vez que una sesión alcanza `COMMITTED`, `ABORTED`, `TIMED_OUT` o `FAILED_VALIDATION`, no puede regresar a un estado no terminal ni cambiar a otro estado terminal.

Estado actual:

- implícita parcialmente en `isTerminal`;
- no existe como propiedad formal independiente;
- deberá incorporarse en Java, TLA+ y Alloy.

#### S6. SourceDebitRequiresCommit

El UTXO origen solo puede eliminarse definitivamente cuando la operación alcanza un commit válido.

Estado actual:

- el débito definitivo ocurre dentro de `commitAtomicTransfer`;
- abort y timeout desbloquean el UTXO sin eliminarlo;
- falta comprobar fallos entre el débito y el crédito destino.

#### S7. DestinationCreditRequiresValidReceipt

No puede crearse un UTXO en el shard destino sin un recibo válido, no consumido, asociado a la sesión y aceptado con el quorum requerido.

Estado actual:

- comprobada parcialmente en `commitAtomicTransfer`;
- declarada parcialmente por `NoDoubleMint`;
- falta expresar identidad completa entre transferencia, recibo, shard y crédito.

#### S8. QuorumRequired

El bloqueo en origen y el commit en destino requieren al menos el quorum configurado de validadores disponibles y honestos según el modelo adoptado.

Estado actual:

- comprobación Java existente en origen y destino;
- no existe una propiedad específica en TLA+ o Alloy;
- la definición de honestidad y voto deberá precisarse en fases posteriores.

#### S9. NoStuckFundsAtTermination

Una sesión terminal no puede conservar bloqueado el UTXO origen.

Estado actual:

- existe `NoStuckCrossShardInvariant` en Java;
- se relaciona con `TimeoutReleasesFunds` y `NoValueLoss`;
- falta evaluar todos los estados terminales bajo fallos intermedios.

#### S10. UniqueSessionIdentity

No pueden existir dos sesiones activas con el mismo identificador de transferencia.

Estado actual:

- `ShardManager` rechaza identificadores de sesión repetidos;
- no existe una propiedad formal equivalente;
- deberá evaluarse junto con sesiones concurrentes.

#### Dependencias entre propiedades

Las propiedades no son independientes:

- `NoDoubleMint` depende de `NoReceiptReplay` y `DestinationCreditRequiresValidReceipt`;
- `NoValueLossAtTermination` depende de `SourceDebitRequiresCommit` y `NoStuckFundsAtTermination`;
- `DecisionConsistency` se fortalece con `TerminalStateIrreversibility`;
- `QuorumRequired` limita las transiciones de bloqueo y commit.

#### Criterio de evidencia

Una propiedad solo podrá marcarse como verificada cuando exista:

1. una definición formal no ambigua;
2. una correspondencia con estados y acciones del protocolo;
3. una ejecución reproducible de TLC, Alloy o una prueba runtime apropiada;
4. un reporte versionado con configuración, resultado y alcance;
5. una declaración explícita de los bounds cuando la verificación sea acotada.
