### Hoja de ruta del Paper 1

#### Principio de integración

Cada fase se desarrolla en una rama corta creada desde `main` actualizado. La fase se integra mediante Pull Request antes de crear la siguiente rama.

No se mantienen ramas de fases futuras abiertas de forma simultánea.

#### Fase 0: baseline de investigación

Rama: `paper1/fase-0-baseline-investigacion`

Objetivos:

- fijar preguntas de investigación;
- definir alcance y exclusiones;
- documentar supuestos;
- documentar el modelo de amenazas;
- identificar contribuciones esperadas;
- construir la matriz de trazabilidad;
- conservar intacto el comportamiento del software.

Salida esperada:

- documentación en `docs/research/paper1/`;
- orientación para `results/` y `experiments/`;
- parche sin cambios de código.

#### Fase 1: contrato del protocolo

Rama prevista: `paper1/fase-1-contrato-protocolo`

Objetivos:

- definir acciones y precondiciones;
- separar safety y liveness;
- definir estados y transiciones permitidas;
- definir la relación inicial Java-TLA+.

#### Fase 2: máquina de estados Java

Rama prevista: `paper1/fase-2-maquina-estados`

Objetivos:

- ampliar los estados observables;
- validar transiciones;
- impedir cambios desde estados terminales;
- registrar historial de transición.

#### Fase 3: protocolo atómico separado

Rama prevista: `paper1/fase-3-protocolo-atomico`

Objetivos:

- extraer la lógica cross-shard de `ShardManager`;
- separar prepare, validate, apply y rollback;
- impedir estados parciales observables.

#### Fase 4: interleavings deterministas

Rama prevista: `paper1/fase-4-interleavings-deterministas`

Objetivos:

- crear reloj lógico y cola de eventos;
- modelar retraso, duplicación, pérdida y reordenamiento;
- ejecutar escenarios reproducibles por seed.

#### Fase 5: model checking ejecutable

Rama prevista: `paper1/fase-5-model-checking-ejecutable`

Objetivos:

- hacer TLC obligatorio en el perfil de investigación;
- ejecutar Alloy automáticamente;
- fijar versiones de herramientas;
- generar resultados estructurados.

#### Fase 6: modelo multisesión y mutantes

Rama prevista: `paper1/fase-6-modelo-multisesion-mutantes`

Objetivos:

- representar múltiples transferencias;
- representar mensajes y fallos;
- crear mutantes formales;
- almacenar contraejemplos.

Gate esperado:

- mínimo técnico para preparar un envío Q4.

#### Fase 7: conformidad Java-TLA+

Rama prevista: `paper1/fase-7-conformidad-java-tla`

Objetivos:

- exportar trazas Java;
- definir una función de abstracción;
- aceptar trazas válidas;
- rechazar trazas corruptas.

Gate esperado:

- diferenciación principal para un envío Q3.

#### Fase 8: evaluación y artefacto

Rama prevista: `paper1/fase-8-evaluacion-artefacto`

Objetivos:

- ejecutar la matriz experimental;
- generar tablas y figuras;
- congelar configuraciones;
- crear un comando de reproducción;
- preparar release y snapshot del envío.

#### Fase 9 opcional: fortalecimiento teórico

Rama prevista: `paper1/fase-9-prueba-inductiva-liveness`

Objetivos opcionales:

- invariantes inductivas;
- fairness explícita;
- prueba asistida;
- relación de refinamiento más fuerte.

Esta fase no debe retrasar el primer envío Q3 o Q4.
