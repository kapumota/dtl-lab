### Preguntas de investigación

#### Propósito

Las preguntas de investigación delimitan qué debe demostrar el Paper 1. En la Fase 0 se definen preguntas y observables. Todavía no se registran respuestas ni conclusiones.

#### RQ1: preservación de propiedades de seguridad

¿El protocolo de commit cross-shard preserva atomicidad, conservación de valor y no reutilización de recibos bajo todos los interleavings explorados dentro de los bounds documentados?

Resultados observables:

- resultado de cada invariante en TLC;
- resultado de cada assertion en Alloy;
- cantidad de estados generados y estados distintos;
- profundidad o diámetro del espacio explorado;
- contraejemplos encontrados;
- configuración exacta de shards, transferencias, recibos y fallos.

Propiedades iniciales relacionadas:

- `NoDoubleMint`;
- `NoValueLoss`;
- `NoReceiptReplay`;
- `AtomicCommit`;
- `TimeoutReleasesFunds`.

#### RQ2: capacidad de detección de defectos

¿TLC, Alloy y las invariantes runtime detectan mutaciones realistas que eliminan controles del protocolo cross-shard?

Mutaciones previstas:

- ausencia de protección contra replay;
- crédito en destino sin recibo válido;
- commit posterior a abort;
- timeout sin liberación de fondos;
- aceptación sin quorum suficiente.

Resultados observables:

- propiedad violada;
- herramienta que detecta la violación;
- contraejemplo mínimo;
- tiempo de detección;
- tamaño del espacio explorado;
- diferencia entre el protocolo correcto y el mutante.

#### RQ3: conformidad entre implementación y modelo

¿Las trazas observables producidas por la implementación Java corresponden a secuencias de acciones admitidas por el modelo TLA+ dentro de los escenarios y bounds evaluados?

Resultados observables:

- cantidad de trazas Java evaluadas;
- trazas aceptadas por el verificador de conformidad;
- trazas rechazadas;
- causa del rechazo;
- cobertura de acciones del protocolo;
- seeds y configuraciones utilizadas.

La respuesta a esta pregunta se presentará como conformidad acotada basada en trazas. No se reclamará una prueba general de refinamiento mientras no exista una demostración formal suficiente.

#### Pregunta secundaria de escalabilidad

¿Cómo crece el costo del model checking al aumentar la cantidad de shards, transferencias concurrentes, recibos y fallos habilitados?

Resultados observables:

- estados generados;
- estados distintos;
- tiempo de ejecución;
- memoria utilizada;
- configuraciones que producen explosión de estados.

Esta pregunta es secundaria. No reemplaza las preguntas sobre seguridad y conformidad.

#### Exclusión del Paper 2

Las siguientes preguntas no pertenecen al Paper 1:

- comparación de TPS con BlockEmulator;
- latencia p50, p95 o p99;
- extracción de MEV cross-shard;
- políticas de ordenamiento económico;
- comparación con Shadow o Ethshadow;
- workloads históricos de Ethereum.

Esos temas corresponden al futuro Paper 2.
