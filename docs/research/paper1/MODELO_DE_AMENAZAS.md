### Modelo de amenazas

#### Objetivo

El modelo de amenazas define los fallos y comportamientos adversariales que el Paper 1 estudiará. No describe todos los ataques posibles contra una blockchain real.

#### Activos protegidos

- conservación del valor transferido;
- unicidad del consumo de recibos;
- consistencia de la decisión terminal;
- disponibilidad del UTXO origen después de un abort o timeout;
- requisito de quorum;
- correspondencia entre el estado Java y el modelo formal.

#### Capacidades del adversario

El adversario o el entorno de fallos puede:

- duplicar un recibo o mensaje;
- retrasar la entrega de un recibo;
- descartar un mensaje;
- reordenar mensajes;
- intentar consumir un recibo más de una vez;
- dejar un shard o validadores fuera de línea;
- provocar quorum insuficiente;
- intentar ejecutar commit después de abort o timeout;
- intentar acreditar destino sin una evidencia válida del origen;
- provocar carreras lógicas entre commit y timeout.

#### Capacidades excluidas

El adversario no puede, dentro del alcance inicial:

- romper las primitivas criptográficas;
- falsificar claves privadas;
- modificar directamente la memoria del proceso Java;
- controlar el sistema operativo o la JVM;
- alterar los archivos de resultados después de una ejecución confiable;
- comprometer una mayoría arbitraria sin respetar el modelo de quorum;
- ejecutar ataques económicos MEV, selfish mining o eclipse como parte del claim principal.

#### Fallos accidentales incluidos

- excepción durante una transición;
- destino sin disponibilidad;
- timeout antes de la entrega;
- configuración con quorum insuficiente;
- operación repetida por reintento;
- orden de eventos distinto al esperado.

#### Propiedades de seguridad relacionadas

- `NoDoubleMint`: no crear valor en destino sin recibo válido.
- `NoValueLoss`: una sesión terminal no destruye valor.
- `NoReceiptReplay`: un recibo no se consume más de una vez.
- `AtomicCommit`: commit y abort no pueden coexistir.
- `TimeoutReleasesFunds`: una expiración libera el origen.
- `TerminalStateIrreversibility`: una decisión terminal no cambia.
- `QuorumRequired`: una decisión protegida no avanza sin quorum.

#### Escenarios existentes del baseline

El baseline ya contiene:

- un ataque de replay que intenta consumir el mismo recibo dos veces;
- un escenario de timeout con el shard destino fuera de línea;
- validación de quorum en origen y destino;
- comprobaciones runtime sobre recibos y UTXO bloqueados.

Estos escenarios son evidencia inicial de comportamiento. No sustituyen la exploración sistemática de interleavings ni la verificación formal ejecutada.

#### Limitaciones iniciales

El baseline no modela una red explícita de mensajes para el protocolo cross-shard. Las operaciones Java principales son síncronas y coordinadas por `ShardManager`.

El modelo TLA+ actual representa una sesión acotada mediante variables globales. El modelo Alloy actual describe estados observables, pero no una secuencia completa de estados ordenados.

Estas limitaciones se tratarán en fases posteriores.
