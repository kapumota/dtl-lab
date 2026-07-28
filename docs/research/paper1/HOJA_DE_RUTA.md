### Hoja de ruta del Paper 1

#### Principio de integración

Cada fase se desarrolla en una rama corta creada desde `main` actualizado y se integra mediante Pull Request antes de abrir la siguiente.

#### Fases cerradas

- Fase 0: baseline de investigación.
- Fase 1: contrato del protocolo.
- Fase 2: máquina de estados Java.
- Fase 3: protocolo atómico y rollback.
- Fase 4: interleavings deterministas.
- Fase 5: model checking ejecutable.
- Fase 6: modelo multisesión y mutantes.
- Fase 6.1: cierre científico y documental.

#### Fase 6.1: cierre científico

Rama: `paper1/fase-6-1-cierre-cientifico`

Objetivos:

- alinear RQ1 con las propiedades ejecutadas;
- agregar conservación de valor e irreversibilidad terminal;
- exigir propiedad objetivo en mutantes Alloy;
- registrar procedencia inequívoca;
- actualizar documentación y versión;
- evitar componentes de conformidad.

Gate:

- baseline `v1.1.0-beta.2` listo para iniciar trazas.

#### Fase 7A: exportación de trazas

Rama prevista: `paper1/fase-7a-exportacion-trazas`

Objetivos:

- definir JSONL versionado;
- exportar configuración, seed, eventos y estado final;
- garantizar serialización y hash deterministas;
- no implementar todavía replay TLC.

#### Fase 7B: función de abstracción

Rama prevista: `paper1/fase-7b-funcion-abstraccion`

Objetivos:

- mapear estados Java a estados TLA+;
- mapear eventos a acciones formales o `Stutter`;
- conservar identidades de transferencia y shard.

#### Fase 7C: replay formal con TLC

Rama prevista: `paper1/fase-7c-replay-tlc`

Objetivos:

- convertir trazas abstractas en restricciones formales;
- usar TLC como oráculo;
- aceptar trazas conformantes;
- localizar transiciones rechazadas.

#### Fase 7D: corpus negativo

Rama prevista: `paper1/fase-7d-trazas-corruptas`

Objetivos:

- generar mutaciones de trazas;
- rechazar commit después de abort, replay, crédito sin recibo y commit sin quorum;
- reportar paso, transferencia y causa.

#### Fase 7E: integración de conformidad

Rama prevista: `paper1/fase-7e-integracion-conformidad`

Objetivos:

- agregar comando científico;
- integrar CI;
- publicar resultados;
- cerrar RQ3 como conformidad acotada basada en trazas.

Gate Q3:

- modelo formal relacionado y contrastado con la implementación ejecutable.

#### Fase 8: evaluación y artefacto

Objetivos:

- ejecutar la matriz experimental;
- generar tablas y figuras;
- congelar configuraciones;
- preparar release y snapshot del envío.

#### Fase 9 opcional

Objetivos:

- invariantes inductivas;
- fairness explícita;
- liveness temporal;
- relación de refinamiento más fuerte.

Esta fase no debe retrasar el primer envío Q3 o Q4.
