### Preguntas de investigación

#### RQ1: preservación de propiedades de seguridad

¿El protocolo de commit cross-shard preserva las propiedades declaradas bajo los interleavings explorados dentro de los bounds documentados?

Propiedades observadas:

- `NoReceiptReplay`;
- `DestinationCreditRequiresValidReceipt`;
- `DecisionConsistency`;
- `NoValueLossAtTermination`;
- `TerminalStateIrreversibility`;
- `EventuallyReleasedAfterTimeout` como invariante acotada de estado;
- `QuorumRequired`.

Resultados observables:

- resultado por propiedad y herramienta;
- estados generados y estados distintos en TLC;
- profundidad explorada;
- tiempo y memoria;
- configuración exacta;
- contraejemplos.

#### RQ2: capacidad de detección de defectos

¿TLC y Alloy detectan mutaciones realistas que eliminan controles del protocolo cross-shard?

Mutaciones evaluadas:

- ausencia de protección contra replay;
- crédito en destino sin recibo válido;
- commit posterior a abort;
- timeout sin liberación de fondos;
- aceptación sin quorum suficiente.

Cada mutante declara una propiedad objetivo. La ejecución solo se considera correcta cuando esa propiedad produce la violación esperada.

#### RQ3: conformidad entre implementación y modelo

¿Las trazas observables producidas por la implementación Java corresponden a secuencias de acciones admitidas por el modelo TLA+ dentro de los escenarios y bounds evaluados?

Resultados observables:

- diez escenarios válidos evaluados y aceptados por TLC;
- diez trazas corruptas evaluadas y rechazadas;
- diez diagnósticos coincidentes con paso, acción y transferencia;
- seed, configuración y herramienta fijadas;
- manifiesto integrado con procedencia y hashes.

RQ3 queda respondida afirmativamente dentro de los escenarios, bounds, seed y mutaciones declarados. La respuesta se presenta como `bounded implementation-model trace conformance`, no como prueba general de refinamiento.

#### RQ4: costo de verificación

¿Cómo crece el costo de verificación al aumentar shards, transferencias concurrentes, bounds y fallos habilitados?

Hipótesis H4:

El costo de model checking crece de forma no lineal con el número de transferencias concurrentes y shards.

H4 permanece pendiente de evaluación y no debe redactarse como resultado confirmado antes de ejecutar la matriz.

Resultados observables:

- estados generados y estados distintos;
- profundidad o scope;
- tiempo total;
- memoria residente máxima;
- timeout y falta de memoria;
- razones de crecimiento dentro de cada herramienta;
- configuración y repetición exactas.

#### Exclusiones

El Paper 1 no estudia TPS frente a otros simuladores, latencias p50 o p99, MEV cross-shard, workloads históricos de Ethereum ni superioridad de rendimiento. Esos temas permanecen fuera del alcance.
