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

Resultados previstos:

- cantidad de trazas Java evaluadas;
- trazas aceptadas;
- trazas rechazadas;
- causa y paso del rechazo;
- cobertura de acciones;
- seeds y configuraciones.

RQ3 permanece pendiente hasta completar la Fase 7. La respuesta se presentará como `bounded implementation-model trace conformance`, no como prueba general de refinamiento.

#### Pregunta secundaria de escalabilidad

¿Cómo crece el costo del model checking al aumentar shards, transferencias, recibos y fallos habilitados?

Esta pregunta se evaluará en la Fase 8 mediante tablas generadas desde los resultados raw.

#### Exclusiones

El Paper 1 no estudia TPS frente a otros simuladores, latencias p50 o p99, MEV cross-shard, workloads históricos de Ethereum ni superioridad de rendimiento. Esos temas permanecen fuera del alcance.
