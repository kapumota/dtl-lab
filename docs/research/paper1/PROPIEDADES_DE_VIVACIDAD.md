### Propiedades de vivacidad

#### Propósito

Este documento define las propiedades de liveness del Paper 1. Una propiedad de liveness expresa que un evento deseable ocurre eventualmente bajo supuestos explícitos.

La Fase 1 solo define estas propiedades. El modelo TLA+ actual no las expresa todavía de forma completa.

#### Diferencia entre safety y liveness

La propiedad:

```text
expired => fundsReleased
```

es una invariante de estado. Indica que no debe existir un estado expirado sin fondos liberados.

Una propiedad temporal de liveness exige algo diferente:

```text
si una sesión permanece bloqueada y alcanza su timeout,
entonces eventualmente los fondos serán liberados
```

La segunda afirmación requiere transiciones futuras y supuestos de fairness.

#### L1. EventuallyDecided

Toda sesión válida que se inicia debe alcanzar eventualmente uno de estos estados terminales:

- `COMMITTED`;
- `ABORTED`;
- `TIMED_OUT`;
- `FAILED_VALIDATION`.

Supuestos preliminares:

- el reloj lógico continúa avanzando;
- las acciones habilitadas no se posponen para siempre;
- el coordinador no deja de ejecutar permanentemente;
- el timeout es finito.

#### L2. EventuallyReleasedAfterTimeout

Si una sesión no terminal alcanza su timeout sin haber confirmado el destino, el UTXO origen debe quedar eventualmente desbloqueado y disponible.

Supuestos preliminares:

- `advanceRound` o su mecanismo equivalente continúa ejecutándose;
- la acción de expiración tiene fairness;
- no existe una falla permanente del almacenamiento del shard origen.

#### L3. EventuallyProcessedAfterDelivery

Si un recibo válido se entrega al shard destino antes del timeout, el shard destino permanece operativo y existe quorum, entonces la sesión debe procesarse eventualmente hasta una decisión terminal.

La decisión puede ser commit o fallo de validación, según el estado del recibo y las reglas del protocolo.

#### L4. EventuallyCommittedUnderHealthyConditions

Una transferencia válida debe terminar eventualmente en `COMMITTED` cuando:

- origen y destino permanecen operativos;
- ambos alcanzan quorum;
- el recibo no se pierde;
- el recibo se entrega antes del timeout;
- no existe replay previo;
- no ocurre una excepción interna;
- las acciones habilitadas reciben fairness.

Esta propiedad es más fuerte que `EventuallyDecided` y requiere supuestos más restrictivos.

#### L5. NoPermanentLock

Ningún UTXO puede permanecer bloqueado indefinidamente por una sesión que no progresa.

La sesión debe terminar por commit, abort, fallo de validación o timeout.

#### Fairness preliminar

Las propiedades de liveness requieren declarar fairness. Se consideran inicialmente:

- weak fairness para la expiración de una sesión cuyo timeout ya fue alcanzado;
- weak fairness para el procesamiento de un recibo entregado y habilitado;
- progreso continuo del reloj lógico;
- ausencia de bloqueo permanente del coordinador.

La formulación exacta se realizará en la Fase 6.

#### Sincronía parcial

El modelo de investigación puede asumir sincronía parcial:

- antes de un instante desconocido, los mensajes pueden sufrir retrasos arbitrarios;
- después de ese instante, los mensajes entre componentes operativos se entregan dentro de un límite finito;
- el timeout debe elegirse considerando el límite de entrega posterior a la estabilización.

La Fase 1 no fija todavía valores numéricos para ese límite.

#### Limitaciones actuales

El baseline no incluye:

- red explícita de mensajes;
- scheduler de eventos;
- fairness formal;
- propiedad temporal de decisión eventual;
- propiedad temporal de liberación eventual;
- ejecución TLC obligatoria para liveness.

Por ello, ninguna propiedad de este documento debe marcarse todavía como demostrada.
