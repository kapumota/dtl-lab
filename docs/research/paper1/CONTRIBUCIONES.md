### Contribuciones del Paper 1

#### C1: especificación formal multisesión

Estado: alcanzada dentro de bounds explícitos.

Evidencia:

- TLA+ con funciones por transferencia;
- Alloy con estados ordenados;
- varias transferencias, shards, recibos, mensajes y votos;
- siete propiedades ejecutadas;
- configuraciones y resultados reproducibles.

No se reclama verificación para una cantidad arbitraria de participantes.

#### C2: evaluación mediante mutantes y contraejemplos

Estado: alcanzada para TLC y Alloy.

Evidencia:

- cinco mutantes por herramienta;
- propiedad objetivo por mutante;
- diez contraejemplos almacenados;
- resultados estructurados;
- validación de que cada mutante Alloy viola su objetivo.

La comparación cuantitativa final entre herramientas corresponde a la Fase 8.

#### C3: conformidad acotada Java-TLA+

Estado: pendiente.

Condiciones:

- formato estable de trazas;
- función de abstracción documentada y ejecutable;
- checker que use el modelo formal como oráculo;
- escenarios válidos y trazas corruptas;
- resultados reproducibles.

La denominación prevista es `bounded implementation-model trace conformance`.

#### C4: artefacto reproducible

Estado: parcial.

Ya existen herramientas fijadas, comandos reproducibles, resultados raw y artefactos de CI. Faltan la generación de trazas, la conformidad, las tablas del paper y el snapshot final de la Fase 8.

#### Contribuciones no reclamadas

El Paper 1 no reclamará:

- blockchain de producción;
- protocolo BFT industrial;
- seguridad criptográfica completa;
- superioridad de rendimiento;
- verificación no acotada de toda la implementación;
- refinamiento matemático general;
- prueba de vivacidad sin supuestos formales de fairness.
