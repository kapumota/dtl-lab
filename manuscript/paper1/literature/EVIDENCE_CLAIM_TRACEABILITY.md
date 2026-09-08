### Auditoría de trazabilidad evidencia-claim 8F-G8-F

#### Identificación

Fase:

`8F-G8-F`

Fecha:

`2026-09-07`

Objetivo:

Verificar que los claims científicos centrales del Paper 1 estén respaldados por evidencia identificable y que cada claim conserve explícitamente su límite interpretativo.

#### Matriz de trazabilidad

| ID | RQ/capa | Claim autorizado | Fuente en manuscrito/artefacto | Evidencia | Límite obligatorio |
| --- | --- | --- | --- | --- | --- |
| CL-01 | RQ1 | No se observaron violaciones en las configuraciones bounded completadas. | 06 Results; experiment-spec.json | 420 ejecuciones medidas; 350 completadas; 70 TLC-large censuradas; cero violaciones observadas en las completadas. | No constituye prueba no acotada; los 70 timeouts se conservan como censura. |
| CL-02 | RQ2 | Las propiedades detectan el catálogo predefinido de mutantes científicos. | 06 Results; 07 Discussion | 10/10 mutantes detectados; MutationScore = 1.0. | Describe sensibilidad al catálogo evaluado; no demuestra completitud frente a defectos arbitrarios. |
| CL-03 | RQ3 | Las trazas evaluadas muestran bounded implementation-model trace conformance. | 03 Model; 04 Methodology; 06 Results | 600 ejecuciones; población válida aceptada y población deliberadamente corrupta rechazada con el diagnóstico esperado. | No es refinement general ni behavioral equivalence; cubre el catálogo observable y bounds evaluados. |
| CL-04 | RQ4 | El costo de verificación muestra crecimiento empírico dentro de las herramientas evaluadas. | 05 Experimental Design; 06 Results | Alloy: medianas 0.515306, 0.766172 y 1.568320 s; Spearman = 1.0; TLC-large censurado por timeout. | No demuestra ley no lineal ni complejidad asintótica; no habilita comparaciones absolutas TLC-vs-Alloy. |
| CL-05 | Reproducibility | El artefacto analítico fue reproducido desde un clone y workspace separados. | 09 Reproducibility and Artifact | 10/10 gates; 32/32 hashes coincidentes; cero incidentes. | Mismo host físico; no es independencia de hardware ni rerun completo de las 1272 tareas; hash identity no implica corrección científica. |
| CL-06 | Contribution | La contribución es un workflow integrado y reproducible de evidencia para un protocolo cross-shard ejecutable. | 02 Background and Related Work; 04 Methodology; 09 Reproducibility | Integra bounded property verification, mutation-based property validation, bounded implementation-model trace conformance, verification-cost characterization y reproducción del artefacto. | Gap acotado al comparator set con full text verificado; no es claim de prioridad global. |

#### Relaciones entre capas

`RQ1 -> RQ2 -> RQ3 -> RQ4 -> reproducibility` no representa una cadena de prueba deductiva. Las capas aportan evidencia complementaria sobre propiedades bounded, sensibilidad a defectos, correspondencia de trazas, costo de verificación y reconstrucción del artefacto.

#### Claims autorizados

- `bounded property verification`;
- ausencia de violaciones observadas en configuraciones bounded completadas;
- `mutation-based property validation`;
- sensibilidad al catálogo predefinido de mutantes científicos;
- `bounded implementation-model trace conformance`;
- `verification-cost characterization`;
- evidencia de crecimiento sin ley no lineal ni asintótica;
- reproducción independiente desde clone/workspace separados en el mismo host físico;
- workflow integrado y reproducible de evidencia.

#### Claims que deben seguir prohibidos

- prueba general o no acotada de correctitud;
- general refinement entre Java y los modelos formales;
- behavioral equivalence entre implementación y especificaciones;
- superioridad absoluta de TLC o Alloy;
- ley no lineal o complejidad asintótica derivada de RQ4;
- generalización a toda ejecución Java o todo deployment cross-shard;
- independencia de hardware ya demostrada;
- claims globales de prioridad como `the first`, `the only` o equivalentes.

#### Uso en las fases siguientes

Esta matriz será el contrato para 8F-H, 8F-I y 8F-J. Introduction, Conclusions y Abstract solo podrán elevar claims que aparezcan aquí y deberán conservar el límite correspondiente.

#### Gate de cierre

- RQ1-RQ4 tienen evidencia y límite explícitos;
- reproducción tiene evidencia y límite explícitos;
- el posicionamiento bibliográfico conserva el scope del comparator set;
- el protocolo congelado `paper1-q3-v1` sigue presente;
- no reaparecen claims absolutos de prioridad;
- no se modifican cifras experimentales ni resultados.

Estado:

`PASS`

#### Siguiente fase

`8F-G8-G: final closure of Related Work and evidence contract`
