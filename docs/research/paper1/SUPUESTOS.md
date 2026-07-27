### Supuestos del Paper 1

#### Estado

Estos supuestos son iniciales. Deben revisarse cuando el protocolo Java y los modelos formales se amplíen.

#### A1: identidad de shards

Cada shard posee un identificador único y mantiene un estado UTXO independiente.

#### A2: identidad de transferencias

Cada transferencia cross-shard posee un identificador único dentro de una ejecución.

#### A3: identidad de recibos

Cada transferencia genera como máximo un recibo válido. El recibo está asociado a una transferencia, un shard origen y un shard destino.

#### A4: autenticidad abstracta del recibo

El modelo formal trata la autenticidad del recibo como una precondición abstracta. La verificación criptográfica completa no forma parte del Paper 1.

#### A5: quorum

Una decisión que depende de validadores solo puede avanzar cuando se alcanza el quorum configurado.

La corrección del mecanismo de consenso que produce las aprobaciones queda fuera del alcance.

#### A6: reloj lógico

Los timeouts se expresan inicialmente mediante rondas lógicas. No se asume correspondencia directa con tiempo de pared.

#### A7: entrega de mensajes

La red puede retrasar, duplicar, descartar o reordenar mensajes en escenarios definidos. Las propiedades de liveness requerirán supuestos explícitos de entrega y fairness.

#### A8: estados terminales

`COMMITTED`, `ABORTED`, `TIMED_OUT` y `FAILED_VALIDATION` se consideran terminales en el baseline actual.

Una fase posterior deberá impedir explícitamente transiciones desde estados terminales.

#### A9: conservación de valor

El valor de una transferencia se conserva cuando, al finalizar la sesión, ocurre exactamente uno de los siguientes resultados:

- el valor se acredita en el shard destino y se debita definitivamente en el origen;
- el valor no se acredita en destino y el UTXO origen queda disponible.

#### A10: ejecución determinista

Los experimentos Java futuros deberán registrar seed, configuración y orden de eventos. Una misma configuración y seed deberá producir la misma traza.

#### A11: verificación acotada

TLC y Alloy exploran configuraciones finitas. Los resultados se limitan a los bounds y constantes documentados.

#### A12: implementación no refinada todavía

Las especificaciones existentes se consideran modelos abstractos relacionados con el módulo Java. En el baseline no existe una prueba de refinamiento ni un verificador de conformidad de trazas.
