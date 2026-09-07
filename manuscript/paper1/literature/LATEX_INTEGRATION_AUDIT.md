### Auditoría de integración LaTeX 8F-G8-E

#### Identificación

Fase:

`8F-G8-E`

Fecha:

`2026-09-07`

Objetivo:

Verificar la integración física del manuscrito del Paper 1: estructura, orden de secciones, labels y referencias internas, citas y BibTeX, compilación y advertencias tipográficas.

#### Estructura del manuscrito

- 02: `Background and Related Work` -> `manuscript/paper1/sections/02-background-related-work.tex`;
- 03: `Cross-Shard Transaction Model` -> `manuscript/paper1/sections/03-cross-shard-model.tex`;
- 04: `Research Methodology` -> `manuscript/paper1/sections/04-research-methodology.tex`;
- 05: `Experimental Design` -> `manuscript/paper1/sections/05-experimental-design.tex`;
- 06: `Results` -> `manuscript/paper1/sections/06-results.tex`;
- 07: `Discussion` -> `manuscript/paper1/sections/07-discussion.tex`;
- 08: `Threats to Validity` -> `manuscript/paper1/sections/08-threats-to-validity.tex`;
- 09: `Reproducibility and Artifact` -> `manuscript/paper1/sections/09-reproducibility-artifact.tex`;

Las secciones 02-09 están incluidas exactamente una vez y en orden.

Introduction, Conclusions, Abstract, title, keywords y highlights permanecen fuera del alcance de G8-E porque se completarán en 8F-H, 8F-I y 8F-J.

#### Integridad de referencias

- labels únicos: 70;
- referencias internas encontradas: 8;
- labels duplicados: 0;
- referencias internas sin label: 0;
- claves citadas: 15;
- entradas BibTeX: 15;
- citas sin BibTeX: 0;
- entradas BibTeX no citadas: 0;
- `\bibitem` generados: 15.

#### Compilación

- `latexmk -pdf -interaction=nonstopmode main.tex`: PASS;
- errores LaTeX (`^!`): 0;
- citas indefinidas: 0;
- referencias indefinidas: 0;
- `main.pdf`: generado;
- `main.bbl`: generado.

#### Advertencias tipográficas

- Overfull hbox totales: 2;
- MINOR (<= 5 pt): 1;
- MODERATE (> 5 y <= 20 pt): 1;
- SEVERE (> 20 pt): 0;
- Underfull hbox: 12.

Clasificación usada en G8-E:

- `MINOR`: no bloquea;
- `MODERATE`: no bloquea si la legibilidad permanece adecuada;
- `SEVERE`: bloquea el cierre de G8-E.

#### Gate de cierre

Los gates estructurales, bibliográficos y de compilación permanecen superados.

No existen Overfull hbox `SEVERE` después de la corrección tipográfica.

Estado:

`PASS`

#### Siguiente fase

`8F-G8-F: evidence-to-claim traceability audit`
