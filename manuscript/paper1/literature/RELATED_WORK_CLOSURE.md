### Cierre del bloque 8F-G8

#### Identificación

Fase:

`8F-G8-G`

Fecha:

`2026-09-07`

Objetivo:

Cerrar formalmente Related Work y el contrato de evidencia del Paper 1 antes de redactar Introduction, Conclusions y Abstract.

#### Artefactos de auditoría cerrados

- 8F-G8-A: `CITATION_AUDIT.md`;
- 8F-G8-B: `CLAIM_AUDIT.md`;
- 8F-G8-C: `TERMINOLOGY_AUDIT.md`;
- 8F-G8-D: `ORTHOGRAPHY_SYMBOLS_AUDIT.md`;
- 8F-G8-E: `LATEX_INTEGRATION_AUDIT.md`;
- 8F-G8-F: `EVIDENCE_CLAIM_TRACEABILITY.md`.

#### Corpus bibliográfico congelado

- matriz final: 17 trabajos;
- IDs: `S01 S02 S03 S04 S05 S06 S07 S08 S09 S10 S11 S18 S19 S20 S21 S22 S23`;
- 15 trabajos con cita sustantiva en Related Work;
- S10 y S20 permanecen fuera de los claims sustantivos por no contar con full text verificado;
- la búsqueda estructurada queda cerrada para Paper 1 salvo corrección bibliográfica necesaria.

#### Related Work congelado

La sección conserva cuatro subsecciones:

1. `Sharded ledgers and cross-shard commit`;
2. `Formal analysis of distributed and blockchain protocols`;
3. `Specification validation and implementation-model conformance`;
4. `Positioning of this work`.

El gap permanece limitado al comparator set revisado y no constituye un claim de prioridad global.

#### Contrato científico congelado

- RQ1: `bounded property verification`;
- RQ2: `mutation-based property validation`;
- RQ3: `bounded implementation-model trace conformance`;
- RQ4: `verification-cost characterization`;
- reproducción: clone/workspace separados en el mismo host físico;
- contribución: workflow integrado y reproducible de evidencia.

Los límites definidos en `EVIDENCE_CLAIM_TRACEABILITY.md` son obligatorios para Introduction, Conclusions y Abstract.

#### Gate editorial

- secciones 02-09 presentes;
- cero placeholders `TODO`, `TBD`, `FIXME`, `PLACEHOLDER` o `to be written` en 02-09;
- cero claims absolutos de prioridad;
- compilación LaTeX limpia;
- cero citas indefinidas;
- cero referencias indefinidas;
- cero Overfull hbox SEVERE.

#### Estado

`PASS`

El bloque `8F-G8` queda cerrado y congelado.

#### Siguiente fase

`8F-H: Introduction`
