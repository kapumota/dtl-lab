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

La comparación cuantitativa final entre herramientas usa el protocolo congelado de la Fase 8A y se ejecutará en las Fases 8C y 8D.

#### C3: conformidad acotada Java-TLA+

Estado: alcanzada para el catálogo declarado.

Evidencia:

- formato JSONL estable y determinista;
- función de abstracción documentada y ejecutable;
- TLC usado como oráculo sobre operadores reales;
- diez escenarios válidos aceptados;
- diez trazas corruptas rechazadas;
- diagnósticos de paso, acción y transferencia;
- manifiesto integrado reproducible.

La denominación usada es `bounded implementation-model trace conformance`.

#### C4: artefacto reproducible

Estado: alcanzada para el perfil de conformidad.

Existen herramientas fijadas, comandos reproducibles, resultados raw, manifiestos con procedencia y artefactos de CI. Las tablas finales del paper y el snapshot editorial corresponden a la Fase 8.

#### Plan experimental Q3

Estado: protocolo congelado en la Fase 8A.

La evaluación define RQ1 a RQ4, H1 a H4, treinta seeds, catorce familias de configuración, dos calentamientos, diez repeticiones medidas, límites de recursos y análisis estadístico reproducible.

La Fase 8A no agrega resultados ni modifica la evidencia semántica de las Fases 0 a 7E.

#### Contribuciones no reclamadas

El Paper 1 no reclamará:

- blockchain de producción;
- protocolo BFT industrial;
- seguridad criptográfica completa;
- superioridad de rendimiento;
- verificación no acotada de toda la implementación;
- refinamiento matemático general;
- prueba de vivacidad sin supuestos formales de fairness.
