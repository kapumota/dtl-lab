### Auditoría de novedad y claims 8F-G8-B

#### Identificación

Fase:

`8F-G8-B`

Fecha:

`2026-09-07`

Objetivo:

Auditar claims de prioridad, correctitud, conformance, rendimiento, generalización y reproducibilidad en las secciones 02 a 09 del Paper 1.

#### Resultado global

- no se detectaron claims absolutos de prioridad;
- no se utiliza `the first`;
- no se utiliza `the only`;
- no se utiliza `no prior work`;
- no se utiliza `to our knowledge`;
- se conservaron los límites explícitos de RQ1, RQ2, RQ3 y RQ4;
- se reescribieron claims que podían exceder la evidencia;
- no se introdujo ningún claim nuevo de novedad.

#### Cambios aplicados

| Sección | Decisión | Motivo | Resultado |
| --- | --- | --- | --- |
| 02 | REWORD | Evitar presentar los argumentos de trabajos previos como garantías absolutas. | These systems establish that atomic cross-shard commit mechanisms and protocol-level correctness arguments predate the present work. |
| 02 | REWORD | Evitar un claim historiográfico más fuerte de lo necesario. | Their work demonstrates prior use of mutation-based specification assessment as a methodological approach. |
| 02 | REWORD | Sustituir `mature technique` por una afirmación directamente respaldada por los comparadores. | These studies show that implementation-model trace validation has been investigated across multiple distributed-systems settings. |
| 04 | REWORD | El diseño demuestra detección de mutantes predefinidos; no establece representatividad general de `realistic mutations`. | Do TLC and Alloy detect predefined scientific mutations that remove controls from the cross-shard protocol? |
| 05 | REWORD | Evitar que `functional correctness` se interprete como claim general de correctitud. | Continuous integration is used for functional checks, structural integrity, and reproducibility checks, but its execution times are not mixed with the definitive performance measurements. |
| 07 | REWORD | Expresar el resultado observado en lugar de una preservación potencialmente más fuerte. | The RQ1 results provide bounded evidence that no violations of the declared properties were observed across the completed configurations. |
| 07 | REWORD | Mantener RQ1 en términos de ausencia observada de violaciones. | The experiment supports the narrower conclusion that no violations were observed in the completed bounded configurations, but it cannot extend that conclusion to unexplored states or to the censored TLC-large executions. |
| 07 | REWORD | Usar `controls` en lugar de `guarantees` para los mutantes. | and the same verification framework reacts when selected protocol controls are intentionally weakened. |
| 07 | REWORD | Evitar que `logical correctness` se lea como correctitud establecida del protocolo. | RQ4 shows that the verification campaign is constrained not only by logical outcomes but also by the cost of exploring increasingly large bounded configurations. |
| 07 | REWORD | Con tres niveles, `confirms` es más fuerte que la evidencia disponible. | The Spearman rank association of 1.0 for both metrics is consistent with a monotonic ordering across the three observed profile levels. |
| 07 | REWORD | Distinguir finalización acotada de una noción general de verificación exitosa. | Conversely, completed bounded formal verification and mutation detection remain statements about the formal representations. |
| 08 | REWORD | Evitar que `successful bounded verification` se interprete como prueba positiva general. | Consequently, completion without a counterexample within the bounded profiles does not imply that the same properties would remain tractable, or that no counterexamples would emerge, under substantially larger bounds or different protocol topologies. |
| 08 | REWORD | Describir exactamente qué habría significado completar los checks. | prevents treating all scheduled RQ1 executions as completed checks without detected violations. |
| 09 | PASS | La formulación ya separa integridad de correctitud científica. | Sin cambio |

#### Claims autorizados

- `bounded property verification`;
- `no counterexample was observed within the completed bounded configurations`;
- `mutation-based property validation`;
- `sensitivity to the predefined scientific mutant catalogue`;
- `bounded implementation-model trace conformance`;
- `verification-cost characterization`;
- `complementary TLA+ and Alloy models`;
- `evidence of growth without establishing a nonlinear or asymptotic law`;
- `independent reproduction from a separate clone and workspace on the same physical host`.

#### Claims prohibidos

- prioridad absoluta como `the first` o `the only`;
- prueba general de correctitud del protocolo;
- equivalencia semántica o behavioral equivalence entre Java y TLA+;
- refinement general establecido por RQ3;
- superioridad absoluta de TLC frente a Alloy o viceversa;
- prueba de crecimiento no lineal en RQ4;
- generalización a todas las ejecuciones Java o despliegues arbitrarios;
- hardware-independent reproducibility como resultado ya establecido.

#### Decisiones específicas

RQ1 se expresa preferentemente como ausencia observada de violaciones en configuraciones acotadas completadas, no como prueba general de preservación.

RQ2 se restringe al catálogo de mutantes científicos predefinidos; no se afirma representatividad completa de defectos.

RQ3 conserva la denominación `bounded implementation-model trace conformance` y excluye refinement general, behavioral equivalence e implementation correctness.

RQ4 conserva comparaciones within-tool. El valor de Spearman igual a 1.0 se interpreta como ordenamiento monotónico observado en tres perfiles Alloy, no como confirmación de una ley funcional o no lineal.

La reproducción independiente mantiene explícitamente la limitación de usar el mismo host físico.

#### Gate de cierre

8F-G8-B queda cerrada cuando:

- `CLAIM_AUDIT.md` existe;
- los claims absolutos de prioridad están ausentes;
- las fronteras obligatorias de interpretación permanecen en 02 a 09;
- `git diff --check` pasa;
- el manuscrito compila sin errores, citas indefinidas ni referencias indefinidas;
- todos los cambios permanecen dentro de `manuscript/paper1/`.

#### Siguiente fase

`8F-G8-C: terminology consistency audit`
