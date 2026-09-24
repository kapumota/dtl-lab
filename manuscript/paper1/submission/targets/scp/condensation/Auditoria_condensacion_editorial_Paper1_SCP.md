CONDENSACIÓN RECOMENDADA

### 1. Diagnóstico global

El manuscrito puede reducirse aproximadamente un 23 % manteniendo su contenido científico y su autosuficiencia para revisión por pares. Recomiendo un presupuesto de **9 544 palabras para las diez secciones**, frente a las **12 446 actuales** según el conteo normalizado de esta auditoría. El ahorro recomendado es de **2 902 palabras**. No es necesario eliminar resultados, reducir bounds, modificar hipótesis ni desplazar la metodología científica fuera del artículo.

La contribución resulta identificable e interesante como integración reproducible de evidencia acotada para un protocolo ejecutable. Sin embargo, está sobredocumentada en su explicación argumentativa. Varias secciones vuelven a presentar la función de RQ1-RQ4, y la sección de reproducción describe reiteradamente el inventario, las etapas, el resultado y las mismas fronteras de interpretación. Esa repetición reduce la visibilidad del aporte central.

Las 51 páginas no son, por sí mismas, una objeción científica ni una infracción editorial que esta auditoría esté atribuyendo a SCP. Parte de esa extensión procede legítimamente de elsarticle preprint 12pt, las listas y las tablas. Aun así, el contenido real no requiere toda la extensión actual: Discussion ocupa 1 578 palabras y Reproducibility 1 599, pese a que buena parte de sus explicaciones ya aparece en Methodology, Results o dentro de la misma sección. No recomiendo mantener las 51 páginas por defecto.

#### Material efectivamente verificado

Se leyeron directamente en GitHub los doce archivos científicos requeridos: main.tex, las diez secciones y references.bib, en el commit `5195d94b2ade1933a726d1d2106cc35a11c37f47`. También se leyeron los cinco documentos de cierre solicitados en `d8eefaf37454cc637f6b7f9355beb12c715b4957`. Todos existían con los nombres indicados.

- [Fuentes científicas post-auditoría](https://github.com/kapumota/dtl-lab/tree/5195d94b2ade1933a726d1d2106cc35a11c37f47/manuscript/paper1)
- [Cierre independiente](https://github.com/kapumota/dtl-lab/blob/d8eefaf37454cc637f6b7f9355beb12c715b4957/manuscript/paper1/submission/targets/scp/SCP_WORK_CLOSURE_AUDIT.md)
- [Freeze post-auditoría](https://github.com/kapumota/dtl-lab/blob/d8eefaf37454cc637f6b7f9355beb12c715b4957/manuscript/paper1/submission/targets/scp/SCP_8G_H_POST_AUDIT_FREEZE.md)
- [Estado 8G-H](https://github.com/kapumota/dtl-lab/blob/d8eefaf37454cc637f6b7f9355beb12c715b4957/manuscript/paper1/submission/targets/scp/SCP_8G_H_STATE.json)
- [Estado de submission](https://github.com/kapumota/dtl-lab/blob/d8eefaf37454cc637f6b7f9355beb12c715b4957/manuscript/paper1/submission/targets/scp/SCP_SUBMISSION_STATE.json)
- [Cinco correcciones documentadas](https://github.com/kapumota/dtl-lab/blob/d8eefaf37454cc637f6b7f9355beb12c715b4957/manuscript/paper1/submission/targets/scp/SCP_POST_AUDIT_CORRECTIONS.md)

El cierre y los JSON finales declaran `READY_FOR_MANUAL_FINAL_REVIEW`, con revisión manual y metadata administrativa todavía pendientes. El freeze conserva su estado histórico previo al closure check. Se respeta esa secuencia, sin reabrir los gates ni interpretar el documento histórico como una regresión del estado final.

Además, había una copia local del PDF y del ZIP editorial. Se calcularon sus SHA-256 y coincidieron con el freeze:

| Objeto | SHA-256 comprobado |
| --- | --- |
| SCP_manuscript.pdf | `9c4bc207b205ec609dc52a4ebf15d73c40913abf17645911ff9aef2b79e91cff` |
| SCP_latex_source.zip | `6d533a0a056f609c0f11f95224325fa142817c16755b0ced426af155041bbc6f` |

Se confirmó que el PDF contiene 51 páginas. Los blobs Git de las diez secciones de la copia editorial coinciden con los recuperados del commit fuente. Por tanto, las referencias a líneas de esta auditoría corresponden al texto científico del candidato correcto. La portada y las declaraciones de la entrada editorial del ZIP contienen la adaptación a SCP, distinta de la entrada genérica main.tex del repositorio. No se utiliza esa diferencia documentada para reabrir la auditoría anterior.

Se inspeccionaron las seis tablas en las páginas 20, 25, 27, 28, 29 y 30 del PDF exacto. Esta inspección sirve para decidir qué conservar durante la condensación. No constituye una nueva auditoría científica ni sustituye la revisión manual final de todo el artículo.

El TAR con digest `e471c8c7ea55167250bda69bb721668f5c7cc5655c6e67f66d7a192241e67f79` se usa únicamente como referencia documental de provenance. No se recalculó su hash en esta ejecución, porque no era necesario para acceder a las fuentes. No se ejecutaron experimentos, reproducciones, regeneraciones de resultados ni una compilación modificada. No se modificó ningún archivo de dtl-lab, ni se crearon ramas, commits o PR. Los únicos entregables nuevos son este informe y su matriz de auditoría.

#### Método y perímetro del conteo

El conteo parte de las diez fuentes .tex. Se eliminan comentarios, comandos de formato, etiquetas, claves de citas y referencias internas. Se conserva el texto legible, los identificadores científicos, las listas, los encabezados y datos de las tablas y los tokens alfanuméricos de expresiones. Los saltos discrecionales de identificadores se unen. No se cuentan tokens compuestos exclusivamente por signos. Se excluyen los títulos de sección/subsección, abstract, título editorial, keywords, bibliografía y declaraciones administrativas.

Se trata de un conteo editorial reproducible y aproximado, no de una declaración sobre el método de conteo que pueda utilizar el journal. Da **12 446**, solo **10 palabras menos, aproximadamente 0,08 %**, que las 12 456 declaradas. La diferencia es irrelevante para el diagnóstico. Los cuatro presupuestos de esta auditoría utilizan exactamente el mismo perímetro, sin restar tablas en un escenario y sumarlas en otro.

Fuera de ese perímetro se conservan, sin propuesta de recorte, aproximadamente 216 palabras de abstract, 59 de Data availability y 58 de declaración de IA. Añadiendo esos tres bloques, el texto contabilizado sería aproximadamente 12 779 palabras antes de editar y 9 877 en la propuesta recomendada, todavía sin bibliografía ni encabezados. Esto evita presentar el objetivo de las diez secciones como si incluyera todo el contenido del PDF.

#### Cuánto es necesario y cuánto es reducible

Estimo que **unas 9 100 a 9 600 palabras**, bajo este perímetro, bastan para conservar una exposición científicamente completa. Es una estimación editorial, no un mínimo informacional demostrado. Los targets por párrafo son presupuestos de edición, no resultados de una reescritura ya realizada.

| Nivel | Ahorro | Proporción del texto actual | Lectura editorial |
| --- | ---: | ---: | --- |
| SAFE REDUCTION | 2 299 | 18,47 % | Recortes de riesgo NONE/LOW, sin aplicar los recortes MEDIUM de Results. |
| RECOMMENDED REDUCTION | 2 902 | 23,32 % | Eliminar recapitulaciones y condensar explicaciones, preservando límites locales. |
| MAXIMUM REASONABLE REDUCTION | 3 336 | 26,80 % | Límite de trabajo estimado, exige revisar fluidez y referencias después de editar. |

Solo **342 palabras** se proponen para eliminación completa mediante DELETE_REDUNDANT. El resto del ahorro recomendado se distribuye en **2 010 palabras por COMPRESS**, **536 por MERGE** y **14 en un párrafo MOVE_TO_ARTIFACT parcial**. No se afirma que 2 902 palabras carezcan de valor científico: muchas expresan ideas necesarias con más extensión de la requerida. Tampoco se presupone que el ahorro por traslado operativo sea grande.

### 2. Objetivos de longitud

SAFE TARGET: 10 147 palabras

RECOMMENDED TARGET: 9 544 palabras

AGGRESSIVE LOWER BOUND: 9 110 palabras

Los objetivos proceden de los presupuestos que siguen. Su precisión aritmética permite controlar la edición, pero no implica que una diferencia de diez o cien palabras determine por sí misma la calidad científica.

**Escenario A, alrededor de 10 000 palabras.** Es viable. El target conservador de 10 147 mantiene margen para transiciones, recapitulaciones breves y límites junto a los resultados. No aplica recortes de riesgo MEDIUM en Results. Llegar exactamente a 10 000 no parece requerir una modificación científica, aunque no aporta una ventaja que justifique convertirlo en cuota rígida.

**Escenario B, alrededor de 9 500 palabras.** Es el recomendado. La suma párrafo por párrafo es 9 544. Conserva todas las tablas, la máquina de estados, la proyección, las siete propiedades, las cuatro hipótesis, las unidades experimentales, las reglas de censura, los resultados, las cuatro categorías de amenazas y la reproducción histórica con sus límites. El cuerpo seguiría siendo autosuficiente para peer review.

**Escenario C, alrededor de 9 000 palabras.** Es plausible aproximarse, pero no recomiendo prometer 9 000 antes de revisar una versión condensada. El presupuesto mínimo razonable identificado es 9 110. La diferencia de 110 palabras está dentro de la incertidumbre editorial y no constituye una frontera científica exacta. A partir de este entorno, nuevos recortes empiezan a competir con la explicación de la abstracción, las relaciones entre RQ, las mitigaciones de amenazas y la interpretación de la reproducción. No se justifica sacrificar esas funciones para alcanzar un número redondo.

### 3. Presupuesto por sección

| Sección | Palabras actuales | SAFE TARGET | RECOMMENDED TARGET | Mínimo razonable | Ahorro recomendado | Riesgo |
| --- | --- | --- | --- | --- | --- | --- |
| Introduction | 834 | 690 | 660 | 640 | 174 | LOW, con operaciones NONE |
| Background and Related Work | 1073 | 930 | 899 | 850 | 174 | LOW, con operaciones NONE |
| Cross-Shard Transaction Model | 1101 | 1000 | 974 | 950 | 127 | LOW, con operaciones NONE |
| Research Methodology | 1343 | 1080 | 1029 | 970 | 314 | LOW, con operaciones NONE |
| Experimental Design | 1114 | 1050 | 1014 | 990 | 100 | LOW, con operaciones NONE |
| Results | 1696 | 1647 | 1453 | 1400 | 243 | MEDIUM en recortes interpretativos |
| Discussion | 1578 | 1090 | 1007 | 950 | 571 | LOW, con operaciones NONE |
| Threats to Validity | 1616 | 1290 | 1225 | 1150 | 391 | LOW, con operaciones NONE |
| Reproducibility and Artifact | 1599 | 1020 | 958 | 900 | 641 | LOW, con operaciones NONE |
| Conclusions | 492 | 350 | 325 | 310 | 167 | LOW, con operaciones NONE |
| TOTAL | 12446 | 10147 | 9544 | 9110 | 2902 | Condicionado a las protecciones de la matriz |

El presupuesto recomendado de cada sección coincide exactamente con la suma de sus targets individuales en la matriz. Los presupuestos SAFE y mínimo son escenarios alternativos por sección, no ahorros que puedan sumarse al recomendado. El SAFE de Results conserva íntegros los párrafos marcados con riesgo MEDIUM.

#### Introduction y Related Work

Introduction puede pasar de 834 a 660 palabras, un recorte de **174**, dentro del intervalo solicitado. El principal exceso no es la mera presencia de cifras, sino la combinación de las cuatro RQ, otra explicación de las cuatro capas, un resumen detallado de resultados y una exposición relativamente larga de reproducción. Se conserva evidencia suficiente para orientar al lector y se acorta la navegación final.

Related Work puede pasar de 1 073 a 899 palabras. La eliminación más clara es 02.12, que vuelve a enumerar trabajos y técnicas ya descritos en 02.02-02.10. La posición canónica permanece en 02.13. No se elimina ninguno de los antecedentes solicitados: Chainspace, OmniLedger, Prophet, CSLAP, LightCross, MongoDB/TLA+, Cordy, Ethereum multiformalismo, Cirstea, TraceLink, CCF, Choreographic PlusCal, Mocket, iMocket y S3 ShardStore. Se conserva su asociación con las citas. La auditoría evalúa cómo los utiliza el manuscrito, sin afirmar que se haya repetido aquí la revisión bibliográfica externa.

#### Research Methodology

| Subsección | Actual | Recomendado | Ahorro |
| --- | --- | --- | --- |
| Study design | 180 | 119 | 61 |
| Research questions and hypotheses | 269 | 235 | 34 |
| Methodological layers | 486 | 424 | 62 |
| Triangulation of evidence | 170 | 103 | 67 |
| Treatment of incomplete executions | 97 | 64 | 33 |
| Interpretation boundaries | 141 | 84 | 57 |

Research questions and hypotheses debe conservar las preguntas y H1-H4. Methodological layers debe conservar las unidades, criterios de detección, definición de MutationScore, proyección, diagnóstico y variables. Triangulation cumple una función diferente: explicar por qué una capa aborda una insuficiencia de otra. Esa relación debe permanecer, pero no necesita volver a definir todas las capas. Interpretation boundaries puede convertirse en una síntesis breve con referencias a las definiciones canónicas. La subsección de ejecuciones incompletas mantiene el principio metodológico, mientras Experimental Design conserva su implementación operativa exacta.

H4 es especialmente sensible: no se debe reemplazar la hipótesis preregistrada de crecimiento no lineal por una hipótesis retrospectiva de simple crecimiento. Se preservan tanto su formulación como la constatación posterior de que no quedó confirmada en su forma no lineal.

#### Discussion

| Subsección | Actual | Recomendado | Ahorro |
| --- | --- | --- | --- |
| Bounded verification evidence | 254 | 155 | 99 |
| Mutation sensitivity and property adequacy | 295 | 174 | 121 |
| Implementation-model trace conformance | 285 | 198 | 87 |
| Verification cost and scalability | 385 | 251 | 134 |
| Cross-layer evidence triangulation | 359 | 229 | 130 |

Discussion puede perder **571 palabras** sin suprimir sus cinco funciones interpretativas. No atribuyo todo ese ahorro a cifras duplicadas: solo unos **100 a 150** corresponderían a recapitulaciones numéricas y narración de tablas, especialmente 07.05, 07.10, 07.17 y 07.18. El resto procede de explicar una sola vez por qué importa la sensibilidad, por qué un diagnóstico localizado aporta más que un rechazo genérico y cómo se relacionan las capas.

Debe mantenerse íntegro el argumento de 07.14 sobre qué faltaría para hablar de refinement. Igualmente, 07.28 conserva que ninguna capa elimina las limitaciones de las otras. Reducir esos pasajes a un simple «no es una prueba» empobrecería la interpretación.

#### Threats to Validity

| Subsección | Actual | Recomendado | Ahorro |
| --- | --- | --- | --- |
| Construct validity | 402 | 291 | 111 |
| Internal validity | 373 | 275 | 98 |
| External validity | 394 | 289 | 105 |
| Conclusion validity | 447 | 370 | 77 |

La reducción recomendada es de **391 palabras, aproximadamente 24 %**, conservando Construct, Internal, External y Conclusion validity. La estrategia es unir cada amenaza con su mitigación y evitar repetir catálogos completos de parámetros. No se elimina una amenaza porque el mismo concepto ya aparezca en Methodology o Discussion.

Se preservan especialmente 08.30, sobre el catálogo de mutantes no aleatorio, 08.31-08.32, sobre seeds y escenarios que no son una muestra aleatoria de toda ejecución Java, 08.33, sobre tres niveles ordinales, y 08.35, sobre el sesgo de resumir solo completadas. La condición same-host está explícita en Reproducibility, cuya subsección de fronteras se conserva. Si posteriormente se integra allí una referencia desde External validity, debe ser una remisión breve, sin una nueva explicación duplicada ni inferencia de independencia de hardware.

#### Reproducibility and Artifact

| Subsección | Actual | Recomendado | Ahorro |
| --- | --- | --- | --- |
| Artifact scope | 374 | 208 | 166 |
| Reproduction workflow | 310 | 233 | 77 |
| Independent reproduction evidence | 307 | 230 | 77 |
| Integrity and traceability | 284 | 185 | 99 |
| Reproducibility boundaries | 324 | 102 | 222 |

Es la mayor oportunidad individual: **1 599 a 958 palabras**, un ahorro de **641**, aproximadamente 40 %. Artifact scope presenta cinco capas y después vuelve a explicar sus funciones. Workflow describe las etapas. Independent reproduction vuelve a distinguir smoke de análisis regenerado. Integrity repite la diferencia entre identidad de contenido y corrección científica. Boundaries vuelve a enumerar las capacidades antes de reiterar sus límites.

La versión canónica propuesta debe mantener: inventario compacto en 09.02-09.06, seis clases smoke en 09.12, regeneración desde raw en 09.13, significado de gates en 09.20, 32/32 en 09.21, no full rerun en 09.22, condiciones de independencia en 09.24, fuente empírica en 09.26 y límite de los hashes en 09.28. Las advertencias finales se conservan de forma breve, especialmente la limitación de portabilidad de 09.40, que sí aporta información adicional.

#### Conclusions

Conclusions puede pasar de **492 a 325 palabras**, dentro del intervalo solicitado de 300 a 350. Las seis unidades actuales pueden aportar a un cierre de cuatro o cinco párrafos. El texto que comienza «The combined evidence should therefore be interpreted as complementary rather than cumulative proof» repite Discussion y puede integrarse en la frase de contribución. El cierre que comienza «Overall, the study shows that a cross-shard protocol can be evaluated through a single reproducible workflow» vuelve a formular el aporte. Se conserva de ese último párrafo el future work: bounds mayores, catálogo de mutantes y trazas más amplio y reproducción en otra máquina.

### 4. Matriz completa párrafo por párrafo

La matriz identifica **332 unidades**. Incluye todos los párrafos sustantivos de las diez secciones, las cuatro RQ introductorias, las siete definiciones de propiedades y, adicionalmente, listas, hipótesis, ecuaciones y tablas. Las listas cortas que forman un solo inventario se mantienen como una unidad con su frase introductoria. Las cuatro RQ y las siete propiedades tienen entradas individuales con sufijos a-d y a-g. No se presenta una selección de ejemplos.

Los identificadores son estables para las líneas del commit fuente. Por ejemplo, 09.24 corresponde a la sección 9, unidad 24. Los rangos se refieren a líneas .tex, no a líneas del PDF. Las primeras palabras se normalizan solo para retirar sintaxis LaTeX, y se muestran diez cuando existen, o todo el bloque si es más corto. No se inventan palabras para alcanzar un mínimo de ocho.

Cada fila posee exactamente una clasificación editorial. En KEEP el target es igual al actual. En DELETE_REDUNDANT el target es cero y se identifica dónde queda el contenido equivalente. En MERGE el target representa la contribución residual de esa unidad al párrafo fusionado, no un nuevo párrafo independiente. Para impedir doble conteo, la suma de los targets de las unidades fusionadas es el presupuesto total que pueden aportar al texto receptor.

Los targets son estimaciones redaccionales. **La justificación y las condiciones de conservación prevalecen sobre el número.** Si una frase requiere algunas palabras adicionales para preservar el significado, deben conservarse. Ninguna fila autoriza a borrar una cifra, un supuesto o un límite solo para cumplir el presupuesto.

En la columna analítica, **F** es la función científica, **U** la información única o específica que debe conservarse, **R** la información repetida, **En** su ubicación adicional y **J** la justificación concreta. Las demás columnas contienen sección/subsección, archivo mediante el encabezado de cada tabla, líneas, incipit, clasificación, palabras actuales/objetivo/ahorro y riesgo. La matriz CSV acompañante expande estos campos en columnas separadas, sin abreviarlos.

#### 1. Introduction

Archivo: `manuscript/paper1/sections/01-introduction.tex`. [Fuente canónica](https://github.com/kapumota/dtl-lab/blob/5195d94b2ade1933a726d1d2106cc35a11c37f47/manuscript/paper1/sections/01-introduction.tex).

| ID | Subsección | Líneas | Primeras palabras | Clasificación | Actual / objetivo / ahorro | Riesgo | Función, contenido y justificación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 01.01 | Sin subsección | 4 | Sharding is a common strategy for increasing the capacity of | COMPRESS | 105 / 90 / 15 | LOW | F: Plantear problema. U: Coordinación entre shards y cinco antecedentes. R: Antecedentes detallados. En: 02.02-02.04. J: Acortar contexto conservando problema y citas. |
| 01.02 | Sin subsección | 6 | Formal methods provide one way to reason about protocol behavior | COMPRESS | 95 / 75 / 20 | LOW | F: Presentar métodos. U: Tres funciones distintas de evidencia. R: Descripción de capas. En: 04.19-04.40. J: Dejar una frase por técnica y conservar citas. |
| 01.03 | Sin subsección | 8 | This paper studies an executable cross-shard commit protocol implemented in | COMPRESS | 82 / 70 / 12 | LOW | F: Delimitar objeto. U: Simulador Java y ausencia de equivalencia entre modelos. R: Operaciones del protocolo. En: 03.01-03.20. J: Resumir inventario sin sugerir cliente de producción. |
| 01.04 | Sin subsección | 10 | The central methodological objective is to connect several forms of | COMPRESS | 128 / 108 / 20 | LOW | F: Definir contribución y gap. U: Restricción al comparator set con full text verificado. R: Enumeración extensa de capas. En: 02.13-02.14. J: Conservar alcance de revisión y eliminar transición defensiva. |
| 01.05 | Sin subsección | 12-14 | The study is organized around four research questions: | KEEP | 8 / 8 / 0 | NONE | F: Declarar RQ1-RQ4. U: Introducción al inventario que sigue. R: Formulación metodológica. En: 04.06-04.16. J: Conservar conexión con las entradas que siguen. |
| 01.05a | Sin subsección | 15 | RQ1. What bounded property-verification outcomes are obtained for the valid | KEEP | 19 / 19 / 0 | NONE | F: Formular RQ1. U: RQ1. What bounded property-verification outcomes are obtained for the valid TLA+ and Alloy models across the declared experimental configurations?. R: Formulación metodológica. En: 04.06-04.16. J: Mantener esta definición completa, con su alcance y condiciones. |
| 01.05b | Sin subsección | 17 | RQ2. Do the evaluated TLA+ and Alloy properties detect predefined | KEEP | 17 / 17 / 0 | NONE | F: Formular RQ2. U: RQ2. Do the evaluated TLA+ and Alloy properties detect predefined scientific mutations that remove selected protocol controls?. R: Formulación metodológica. En: 04.06-04.16. J: Mantener esta definición completa, con su alcance y condiciones. |
| 01.05c | Sin subsección | 19 | RQ3. Do selected valid and deliberately corrupted Java executions satisfy | KEEP | 23 / 23 / 0 | NONE | F: Formular RQ3. U: RQ3. Do selected valid and deliberately corrupted Java executions satisfy the expected bounded implementation-model trace-conformance outcomes under the declared projection and scenario catalogue?. R: Formulación metodológica. En: 04.06-04.16. J: Mantener esta definición completa, con su alcance y condiciones. |
| 01.05d | Sin subsección | 21 | RQ4. How does verification cost vary across the evaluated bound | KEEP | 20 / 20 / 0 | NONE | F: Formular RQ4. U: RQ4. How does verification cost vary across the evaluated bound profiles and TLC fault profiles under the frozen execution protocol?. R: Formulación metodológica. En: 04.06-04.16. J: Mantener esta definición completa, con su alcance y condiciones. |
| 01.06 | Sin subsección | 24 | The evidence produced for these questions is intentionally layered. RQ1 | COMPRESS | 80 / 45 / 35 | LOW | F: Delimitar evidencia introductoria. U: Resumen local de límites RQ1-RQ4. R: Explicaciones de cada capa. En: 04.19-04.40 y 08. J: Conservar límites en versión breve y remitir a metodología. |
| 01.07 | Sin subsección | 26 | The definitive campaign scheduled 1, 272 tasks. For RQ1, 420 measured | COMPRESS | 117 / 95 / 22 | LOW | F: Resumir evidencia. U: 1272 y resultados esenciales RQ1-RQ4. R: Resultados completos. En: 06.04, 06.15, 06.28, 06.54. J: Reducir conexiones narrativas conservando cifras y censura. |
| 01.08 | Sin subsección | 28 | Reproducibility is treated as a separate evidence layer. The analytical | COMPRESS | 78 / 60 / 18 | LOW | F: Resumir reproducción. U: 10/10, 32/32 y separación de proceso en mismo host. R: Interpretación detallada del hash. En: 09.20-09.24, 09.28. J: Conservar condiciones y no full rerun en resumen compacto. |
| 01.09 | Sin subsección | 30 | The remainder of the paper is structured as follows. Background | COMPRESS | 62 / 30 / 32 | NONE | F: Orientar estructura. U: Orden de lectura. R: Títulos de secciones. En: Encabezados 02-10. J: Una oración de navegación basta. |

#### 2. Background and Related Work

Archivo: `manuscript/paper1/sections/02-background-related-work.tex`. [Fuente canónica](https://github.com/kapumota/dtl-lab/blob/5195d94b2ade1933a726d1d2106cc35a11c37f47/manuscript/paper1/sections/02-background-related-work.tex).

| ID | Subsección | Líneas | Primeras palabras | Clasificación | Actual / objetivo / ahorro | Riesgo | Función, contenido y justificación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 02.01 | Sin subsección | 4-12 | The literature relevant to this study spans four closely related | COMPRESS | 81 / 50 / 31 | LOW | F: Orientar antecedentes. U: Cuatro áreas del comparator set. R: No novedad individual. En: 02.13-02.14. J: Nombrar áreas sin adelantar toda la conclusión. |
| 02.02 | Sharded ledgers and cross-shard commit | 17-26 | Sharded ledgers have long treated atomic cross-shard processing as a | KEEP | 75 / 75 / 0 | NONE | F: Posicionar atomic commit. U: S-BAC, Atomix y mecanismos previos. R: Contexto introductorio. En: 01.01. J: Conservar diferencias y referencias. |
| 02.03 | Sharded ledgers and cross-shard commit | 28-36 | More recent systems focus on reducing conflicts, latency, or coordination | KEEP | 66 / 66 / 0 | NONE | F: Posicionar trabajos recientes. U: Prophet, CSLAP y LightCross. R: Mención de nombres. En: 01.01. J: Conservar mecanismos y propiedades atribuidas. |
| 02.04 | Sharded ledgers and cross-shard commit | 38-47 | The cited cross-shard systems are therefore not studies without correctness | COMPRESS | 89 / 75 / 14 | LOW | F: Precisar contraste experimental. U: Throughput/latencia distintos del coste de verificación. R: Frontera de contribución. En: 02.13. J: Acortar transición sin negar evidencia de corrección ajena. |
| 02.05 | Formal analysis of distributed and blockchain protocols | 52-60 | Formal modeling and model checking have been applied directly to | KEEP | 77 / 77 / 0 | NONE | F: Discutir antecedente cercano. U: MongoDB, WiredTiger y vínculo implementación/modelo. R: Mención introductoria. En: 01.02. J: Es un comparador central que no debe degradarse. |
| 02.06 | Formal analysis of distributed and blockchain protocols | 62-69 | Mutation-based assessment of formal specifications also predates this work. Cordy | COMPRESS | 74 / 65 / 9 | LOW | F: Posicionar mutación. U: Precedente Cordy y catálogo dirigido propio. R: Sensibilidad limitada al catálogo. En: 04.29, 08.07. J: Conservar método previo y diferencia de alcance. |
| 02.07 | Formal analysis of distributed and blockchain protocols | 71-79 | The use of multiple formal encodings and deliberate defects is | KEEP | 82 / 82 / 0 | NONE | F: Delimitar novedad multiformalismo. U: Ethereum 3SF, Apalache, SMT, Alloy y defectos. R: No novedad aislada. En: 02.14. J: Mantener contraejemplo a una supuesta prioridad. |
| 02.08 | Specification validation and implementation-model conformance | 84-91 | A substantial body of work addresses the gap between verified | KEEP | 68 / 68 / 0 | NONE | F: Posicionar validación de trazas. U: Cirstea, observación parcial e instrumentación Java. R: Descripción general de trace validation. En: 01.02. J: Conservar tradeoff instrumental específico. |
| 02.09 | Specification validation and implementation-model conformance | 93-103 | TraceLink further automates the relation between distributed implementations and verified | COMPRESS | 82 / 75 / 7 | LOW | F: Comparar validación integrada. U: TraceLink, reproducción parcial y CCF/CI. R: Integración de técnicas. En: 02.14. J: Conservar ambos trabajos y sus diferencias. |
| 02.10 | Specification validation and implementation-model conformance | 105-117 | Other approaches generate or guide implementation checks directly from formal | COMPRESS | 98 / 90 / 8 | LOW | F: Comparar alternativas. U: PlusCal, Mocket, iMocket y S3 ShardStore. R: Pluralidad de métodos. En: 02.12. J: Agrupar transiciones sin quitar mecanismos ni citas. |
| 02.11 | Specification validation and implementation-model conformance | 119-125 | These results constrain the interpretation of our RQ3. We do | COMPRESS | 64 / 45 / 19 | LOW | F: Delimitar RQ3 frente a literatura. U: Catálogo cross-shard con diagnósticos esperados. R: No novedad general de trace validation. En: 02.08-02.10, 04.14. J: Dejar contraste específico y remisión a metodología. |
| 02.12 | Positioning of this work | 130-141 | The reviewed literature shows substantial prior coverage of the individual | DELETE_REDUNDANT | 68 / 0 / 68 | NONE | F: Recapitular antecedentes. U: Ninguna información o cita nueva frente a párrafos previos. R: Inventario de métodos y trabajos. En: 02.02-02.10. J: Eliminar solo si todas las referencias permanecen en sus párrafos canónicos. |
| 02.13 | Positioning of this work | 143-152 | Within the comparator set for which full text was verified | KEEP | 86 / 86 / 0 | NONE | F: Enunciar gap verificable. U: Comparator set limitado y combinación no identificada. R: Resumen introductorio del gap. En: 01.04. J: Mantener la formulación canónica sin prioridad global. |
| 02.14 | Positioning of this work | 154-161 | Accordingly, the contribution of DTL-Lab is best characterized as an | COMPRESS | 63 / 45 / 18 | LOW | F: Cerrar posicionamiento. U: Contribución de integración con límites por capa. R: No novedad de técnicas aisladas. En: 02.01, 02.07, 02.13. J: Cerrar en dos frases sin volver a enumerar todo. |

#### 3. Cross-Shard Transaction Model

Archivo: `manuscript/paper1/sections/03-cross-shard-model.tex`. [Fuente canónica](https://github.com/kapumota/dtl-lab/blob/5195d94b2ade1933a726d1d2106cc35a11c37f47/manuscript/paper1/sections/03-cross-shard-model.tex).

| ID | Subsección | Líneas | Primeras palabras | Clasificación | Actual / objetivo / ahorro | Riesgo | Función, contenido y justificación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 03.01 | System abstraction | 7 | We study a cross-shard transaction protocol in which a transfer | KEEP | 45 / 45 / 0 | NONE | F: Definir sistema. U: UTXO, origen/destino e identificador de sesión. R: Descripción introductoria. En: 01.03. J: Información constitutiva del modelo. |
| 03.02 | System abstraction | 9 | A session coordinates the source-side lock, the creation and delivery | COMPRESS | 37 / 30 / 7 | LOW | F: Resumir sesión. U: Coordinación normal y fallos cubiertos. R: Máquina y rollback. En: 03.05-03.14. J: Acortar inventario sin cambiar operaciones. |
| 03.03 | System abstraction | 11 | The executable implementation separates shard management from transaction-state mutation. The | COMPRESS | 52 / 40 / 12 | LOW | F: Definir frontera de implementación. U: Shard manager separado de atomic commit. R: Objeto simulador. En: 01.03. J: Retener responsabilidades y eliminar explicación genérica. |
| 03.04 | System abstraction | 13 | Receipt authenticity is treated as an abstract protocol precondition rather | KEEP | 48 / 48 / 0 | NONE | F: Fijar supuestos. U: Autenticidad abstracta, quorum sin consenso y tiempo lógico. R: Scope posterior. En: 03.24. J: Supuestos necesarios junto a abstracción inicial. |
| 03.05 | Cross-shard session state machine | 18-30 | Each transfer is represented by a finite-state session. The implementation | KEEP | 23 / 23 / 0 | NONE | F: Enumerar estados concretos. U: Nueve estados Java. R: Camino normal y proyección. En: 03.06, 03.17. J: Mantener inventario completo. |
| 03.06 | Cross-shard session state machine | 32-44 | The normal successful execution follows CREATED SOURCE_LOCKED RECEIPT_CREATED RECEIPT_DELIVERED DESTINATION_PREPARED | KEEP | 11 / 11 / 0 | NONE | F: Mostrar transición nominal. U: Orden de los seis estados de éxito. R: Inventario de estados. En: 03.05. J: La secuencia aporta relación única. |
| 03.07 | Cross-shard session state machine | 46 | The states COMMITTED, ABORTED, TIMED_OUT, and FAILED_VALIDATION are terminal. The | KEEP | 20 / 20 / 0 | NONE | F: Definir terminalidad. U: Cuatro estados terminales sin salida. R: Propiedad terminal. En: 03.21. J: No confundir definición del sistema con resultado. |
| 03.08 | Cross-shard session state machine | 48 | Abort and validation-failure transitions may occur from nonterminal states. A | KEEP | 37 / 37 / 0 | NONE | F: Definir transiciones de fallo. U: Timeout no permitido desde CREATED. R: Límites de sesión. En: 03.05. J: Regla semántica indispensable. |
| 03.09 | Cross-shard session state machine | 50 | The transition relation explicitly rejects inconsistent executions such as commit | KEEP | 56 / 56 / 0 | NONE | F: Definir rechazos y eventos. U: Transiciones inválidas y campos de evento inmutable. R: Trazas observables. En: 04.31, 05.35. J: Base de conformance que debe quedar en el paper. |
| 03.10 | Atomic commit and rollback | 55-62 | The executable commit operation is divided into four conceptual stages: | KEEP | 31 / 31 / 0 | NONE | F: Definir atomic commit. U: Cuatro etapas de commit/rollback. R: Desarrollo posterior. En: 03.11-03.13. J: Lista orienta semántica de aplicación. |
| 03.11 | Atomic commit and rollback | 64 | Before modifying ledger state, the protocol constructs a commit plan | KEEP | 40 / 40 / 0 | NONE | F: Definir snapshot. U: Componentes restaurables antes de mutación. R: Restauración posterior. En: 03.13. J: Necesario para evaluar atomicidad. |
| 03.12 | Atomic commit and rollback | 66 | During a normal commit, the destination is prepared, the receipt | KEEP | 38 / 38 / 0 | NONE | F: Definir efecto normal. U: Orden de consumo, débito, cambio y crédito. R: Máquina nominal. En: 03.06. J: Efectos de ledger distintos de estados. |
| 03.13 | Atomic commit and rollback | 68 | A failure during this sequence invokes rollback before the protocol | KEEP | 58 / 58 / 0 | NONE | F: Definir rollback. U: Restauración y ausencia de transición inversa sintética. R: Snapshot. En: 03.11. J: No convertir rollback en otra decisión de protocolo. |
| 03.14 | Atomic commit and rollback | 70 | The implementation also exposes deterministic failure-injection points around receipt consumption, | KEEP | 30 / 30 / 0 | NONE | F: Definir inyección determinista. U: Puntos de fallo en consumo, débito y crédito. R: Reproducibilidad general. En: 05.03. J: Mantener mecanismo concreto. |
| 03.15 | Concurrent formal abstraction | 75 | The executable protocol is complemented by bounded formal models in | MERGE | 30 / 15 / 15 | LOW | F: Introducir abstracción. U: Complemento formal acotado. R: Objeto de modelos. En: 03.16, 01.03. J: Integrar apertura con variables representadas en 03.16. |
| 03.16 | Concurrent formal abstraction | 77 | The formal abstractions represent transfer status, source and target shards, | KEEP | 38 / 38 / 0 | NONE | F: Definir estado formal. U: Variables retenidas y colapso de estados concretos. R: Proyección. En: 03.17. J: Definición científica central. |
| 03.17 | Concurrent formal abstraction | 79 | The Java-to-formal projection maps CREATED to Pending; SOURCE_LOCKED, RECEIPT_CREATED, and | KEEP | 26 / 26 / 0 | NONE | F: Definir proyección. U: Mapa Java a Pending/Locked/Prepared/Committed/Aborted. R: Uso metodológico. En: 04.31. J: Conservar todas las correspondencias. |
| 03.18 | Concurrent formal abstraction | 81 | TLA+ additionally stores the first abstract terminal decision in terminalStatus. | KEEP | 42 / 42 / 0 | NONE | F: Distinguir encodings. U: terminalStatus y secuencia Alloy. R: Mutante commit-after-abort. En: 06.18, 08.02. J: Justifica propiedades distintas sin equivalencia. |
| 03.19 | Concurrent formal abstraction | 83 | Concurrent configurations vary the number of shards and active transfers | COMPRESS | 44 / 32 / 12 | LOW | F: Delimitar concurrencia. U: Tipos de comportamiento y finitud. R: Configuraciones concretas. En: 05.04, 05.09. J: Mantener acotación y reducir descripción anticipada. |
| 03.20 | Concurrent formal abstraction | 85 | TLA+ and Alloy provide two formal representations of the same | COMPRESS | 45 / 30 / 15 | LOW | F: Delimitar comparación formal. U: Representaciones complementarias sin equivalencia. R: Comparabilidad de costes. En: 04.40, 08.03. J: Conservar advertencia local sin desarrollar RQ4. |
| 03.21 | Evaluated protocol properties | 90-93 | Seven protocol properties define the main bounded verification target. | KEEP | 9 / 9 / 0 | NONE | F: Definir siete propiedades. U: Introducción al inventario que sigue. R: Resultados por propiedad. En: 06.06, 06.17. J: Conservar conexión con las entradas que siguen. |
| 03.21a | Evaluated protocol properties | 94-95 | NoReceiptReplay. A cross-shard receipt must not be consumed more than | KEEP | 11 / 11 / 0 | NONE | F: Definir propiedad 1. U: NoReceiptReplay. A cross-shard receipt must not be consumed more than once. R: Resultados por propiedad. En: 06.06, 06.17. J: Mantener esta definición completa, con su alcance y condiciones. |
| 03.21b | Evaluated protocol properties | 97-98 | DestinationCreditRequiresValidReceipt. Destination-side credit requires a receipt accepted as valid by | KEEP | 12 / 12 / 0 | NONE | F: Definir propiedad 2. U: DestinationCreditRequiresValidReceipt. Destination-side credit requires a receipt accepted as valid by the protocol. R: Resultados por propiedad. En: 06.06, 06.17. J: Mantener esta definición completa, con su alcance y condiciones. |
| 03.21c | Evaluated protocol properties | 100-101 | DecisionConsistency. A committed transfer must not simultaneously be represented as | KEEP | 20 / 20 / 0 | NONE | F: Definir propiedad 3. U: DecisionConsistency. A committed transfer must not simultaneously be represented as having released its source funds through an abort-like terminal outcome. R: Resultados por propiedad. En: 06.06, 06.17. J: Mantener esta definición completa, con su alcance y condiciones. |
| 03.21d | Evaluated protocol properties | 103-104 | NoValueLossAtTermination. At a terminal state, transferred value must either have | KEEP | 22 / 22 / 0 | NONE | F: Definir propiedad 4. U: NoValueLossAtTermination. At a terminal state, transferred value must either have been committed to the destination or remain recoverable on the source side. R: Resultados por propiedad. En: 06.06, 06.17. J: Mantener esta definición completa, con su alcance y condiciones. |
| 03.21e | Evaluated protocol properties | 106-107 | TerminalStateIrreversibility. Once the protocol reaches its first terminal decision, a | KEEP | 22 / 22 / 0 | NONE | F: Definir propiedad 5. U: TerminalStateIrreversibility. Once the protocol reaches its first terminal decision, a later execution step must not replace it with an incompatible terminal outcome. R: Resultados por propiedad. En: 06.06, 06.17. J: Mantener esta definición completa, con su alcance y condiciones. |
| 03.21f | Evaluated protocol properties | 109-110 | EventuallyReleasedAfterTimeout. An abstract transfer represented as Aborted must have released | KEEP | 28 / 28 / 0 | NONE | F: Definir propiedad 6. U: EventuallyReleasedAfterTimeout. An abstract transfer represented as Aborted must have released its source funds. This property captures the release obligation associated with the modeled timeout and non-commit terminal behavior. R: Resultados por propiedad. En: 06.06, 06.17. J: Mantener esta definición completa, con su alcance y condiciones. |
| 03.21g | Evaluated protocol properties | 112-113 | QuorumRequired. A commit requires the relevant validator quorum. TLA+ parameterizes | KEEP | 25 / 25 / 0 | NONE | F: Definir propiedad 7. U: QuorumRequired. A commit requires the relevant validator quorum. TLA+ parameterizes this threshold through Quorum, whereas the evaluated Alloy model requires at least two validator votes. R: Resultados por propiedad. En: 06.06, 06.17. J: Mantener esta definición completa, con su alcance y condiciones. |
| 03.22 | Evaluated protocol properties | 117 | These properties cover replay protection, destination authorization, decision consistency, conservation | COMPRESS | 38 / 18 / 20 | LOW | F: Conectar propiedades con evaluación. U: Uso del mismo conjunto en modelos válidos y mutantes. R: Nombres y funciones de propiedades. En: 03.21. J: Quitar segunda enumeración y conservar conexión experimental. |
| 03.23 | Scope of the model | 122 | The model deliberately excludes several concerns. | DELETE_REDUNDANT | 6 / 0 / 6 | NONE | F: Introducir exclusiones. U: Ninguna, encabezado ya anuncia scope. R: Título Scope of the model. En: 03.24. J: Eliminar frase vacía sin contenido técnico. |
| 03.24 | Scope of the model | 124 | First, receipt authenticity is abstracted rather than derived from a | COMPRESS | 83 / 65 / 18 | LOW | F: Delimitar alcance. U: Eventos de red proyectados como stuttering. R: Autenticidad, consenso y tiempo lógico. En: 03.04. J: Remitir a supuestos pero conservar stuttering y red acotada. |
| 03.25 | Scope of the model | 126 | Most importantly, both formal representations operate over finite configurations. Absence | COMPRESS | 38 / 27 / 11 | LOW | F: Limitar inferencia formal. U: Ausencia acotada de contraejemplos. R: Límite formal. En: 04.08, 08.28. J: Mantener una oración canónica local. |
| 03.26 | Scope of the model | 128 | The relationship between the Java implementation and the formal model | COMPRESS | 36 / 25 / 11 | LOW | F: Separar relación implementación/modelo. U: Conformance separado y no refinement/equivalencia. R: Límite RQ3. En: 04.14, 07.14. J: Conservar frontera y remitir al método. |

#### 4. Research Methodology

Archivo: `manuscript/paper1/sections/04-research-methodology.tex`. [Fuente canónica](https://github.com/kapumota/dtl-lab/blob/5195d94b2ade1933a726d1d2106cc35a11c37f47/manuscript/paper1/sections/04-research-methodology.tex).

| ID | Subsección | Líneas | Primeras palabras | Clasificación | Actual / objetivo / ahorro | Riesgo | Función, contenido y justificación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 04.01 | Study design | 7 | This study evaluates a cross-shard commit protocol through a multi-method | COMPRESS | 30 / 22 / 8 | LOW | F: Definir diseño multimétodo. U: Objeto de evaluación y métodos. R: Introducción y capas. En: 01.06, 04.19-04.40. J: Abrir sin desarrollar cada capa. |
| 04.02 | Study design | 9 | The design was frozen before the definitive experimental campaign. Research | KEEP | 49 / 49 / 0 | NONE | F: Fijar protocolo previo. U: Momento del freeze y nueva versión ante cambios. R: Protocolo concreto. En: 05.01. J: Mantener regla contra adaptación post hoc. |
| 04.03 | Study design | 11 | The methodology addresses four complementary questions. RQ1 evaluates the valid | MERGE | 61 / 28 / 33 | LOW | F: Relacionar RQ con métodos. U: Mapa global de cuatro preguntas. R: RQ e hipótesis y capas. En: 04.06-04.40. J: Integrar en 04.01 sin repetir cuatro descripciones extensas. |
| 04.04 | Study design | 13 | This combination is intended to avoid relying on a single | MERGE | 40 / 20 / 20 | LOW | F: Justificar triangulación. U: Una evidencia no basta. R: Triangulación. En: 04.42-04.47. J: Integrar justificación en cierre de Study design. |
| 04.05 | Research questions and hypotheses | 18 | The study addresses the following research questions and preregistered hypotheses. | DELETE_REDUNDANT | 10 / 0 / 10 | NONE | F: Anunciar RQ/H. U: Ninguna adicional al título. R: Encabezado Research questions and hypotheses. En: 04.06-04.16. J: Eliminar presentación vacía. |
| 04.06 | Research questions and hypotheses / RQ1: Preservation of declared properties. | 22 | Does the cross-shard commit protocol preserve the declared properties under | KEEP | 17 / 17 / 0 | NONE | F: Formular RQ1. U: Propiedades e interleavings bajo bounds. R: RQ introductoria. En: 01.05. J: Conservar pregunta junto a H1. |
| 04.07 | Research questions and hypotheses / RQ1: Preservation of declared properties. | 24-28 | The corresponding hypothesis is: H1. Valid models produce no property | KEEP | 15 / 15 / 0 | NONE | F: Declarar H1. U: Hipótesis preregistrada literal. R: Respuesta a H1. En: 06.12. J: No reescribir hipótesis después de resultados. |
| 04.08 | Research questions and hypotheses / RQ1: Preservation of declared properties. | 30 | RQ1 concerns bounded model behavior. Completion without a counterexample is | COMPRESS | 29 / 23 / 6 | LOW | F: Precisar interpretación H1. U: Unidad propiedad/herramienta/configuración/bound. R: Boundedness general. En: 03.25, 04.22. J: Mantener condicionamiento de la inferencia. |
| 04.09 | Research questions and hypotheses / RQ2: Defect-detection capability. | 34 | Do TLC and Alloy detect predefined scientific mutations that remove | KEEP | 15 / 15 / 0 | NONE | F: Formular RQ2. U: Detección de controles removidos. R: Pregunta introductoria. En: 01.05. J: Conservar junto a H2. |
| 04.10 | Research questions and hypotheses / RQ2: Defect-detection capability. | 36-40 | The corresponding hypothesis is: H2. Each scientific mutant produces a | KEEP | 16 / 16 / 0 | NONE | F: Declarar H2. U: Violación de propiedad designada. R: Criterio detector. En: 04.26. J: Hipótesis debe seguir explícita. |
| 04.11 | Research questions and hypotheses / RQ2: Defect-detection capability. | 42 | RQ2 evaluates the sensitivity of the property suite to deliberately | COMPRESS | 35 / 25 / 10 | LOW | F: Definir detección. U: Target obligatorio y otras violaciones posibles. R: Regla de detección. En: 04.26. J: Retener admisión de violaciones adicionales. |
| 04.12 | Research questions and hypotheses / RQ3: Bounded implementation-model trace conformance. | 46 | Do observable traces produced by the Java implementation correspond to | KEEP | 23 / 23 / 0 | NONE | F: Formular RQ3. U: Secuencias Java admitidas por TLA+. R: Pregunta introductoria. En: 01.05. J: Mantener tool y alcance. |
| 04.13 | Research questions and hypotheses / RQ3: Bounded implementation-model trace conformance. | 48-52 | The corresponding hypothesis is: H3. Valid traces are accepted and | KEEP | 18 / 18 / 0 | NONE | F: Declarar H3. U: Aceptación y rechazo en punto esperado. R: Clasificación y diagnóstico. En: 04.34-04.35. J: Hipótesis debe incluir punto de rechazo. |
| 04.14 | Research questions and hypotheses / RQ3: Bounded implementation-model trace conformance. | 54 | RQ3 is interpreted as bounded implementation-model trace conformance. It does | KEEP | 27 / 27 / 0 | NONE | F: Limitar H3. U: No refinement ni behavioral equivalence. R: Threats y Discussion. En: 07.14, 08.05. J: Límite canónico próximo a la pregunta. |
| 04.15 | Research questions and hypotheses / RQ4: Verification cost. | 58 | How does verification cost change as the number of shards, | KEEP | 17 / 17 / 0 | NONE | F: Formular RQ4. U: Factores y coste. R: Pregunta introductoria. En: 01.05. J: Conservar pregunta sin prometer una ley. |
| 04.16 | Research questions and hypotheses / RQ4: Verification cost. | 60-64 | The preregistered hypothesis is: H4. Model-checking cost grows nonlinearly with | KEEP | 17 / 17 / 0 | NONE | F: Declarar H4 original. U: Hipótesis nonlinear preregistrada. R: H4 no confirmada. En: 06.54. J: Nunca reemplazarla retrospectivamente por monotonicidad. |
| 04.17 | Research questions and hypotheses / RQ4: Verification cost. | 66 | H4 is treated as an empirical hypothesis rather than an | COMPRESS | 30 / 22 / 8 | LOW | F: Distinguir hipótesis de supuesto. U: H4 depende de evidencia observada. R: Límite de inferencia. En: 06.53. J: Retener distinción y quitar redundancia verbal. |
| 04.18 | Methodological layers | 71 | The four research questions form three verification and validation layers | COMPRESS | 14 / 10 / 4 | LOW | F: Organizar capas. U: Tres capas de validación y una de coste. R: Mapa de RQ. En: 04.03. J: Una línea suficiente. |
| 04.19 | Methodological layers / Bounded property-verification layer | 75 | RQ1 evaluates the valid TLA+ and Alloy specifications against the | MERGE | 16 / 12 / 4 | LOW | F: Definir objeto RQ1. U: Siete propiedades válidas. R: Definiciones del modelo. En: 03.21. J: Integrar con unidad experimental 04.20. |
| 04.20 | Methodological layers / Bounded property-verification layer | 77-84 | The experimental unit consists of: a protocol property; a formal | KEEP | 17 / 17 / 0 | NONE | F: Definir unidad RQ1. U: Propiedad, herramienta, configuración, repetición. R: Factores concretos. En: 05.04. J: Mantener cada componente. |
| 04.21 | Methodological layers / Bounded property-verification layer | 86 | The response variables include property outcome, state-space information when exposed | COMPRESS | 27 / 22 / 5 | LOW | F: Definir variables RQ1. U: Resultados, recursos, exit y contraejemplo. R: Campos exactos. En: 05.32-05.33. J: Agrupar variables conservando familias y remisión. |
| 04.22 | Methodological layers / Bounded property-verification layer | 88 | A completed execution without a counterexample provides evidence only within | COMPRESS | 30 / 24 / 6 | LOW | F: Definir validez de ejecución. U: Completadas vs timeout/OOM. R: Política detallada. En: 05.36-05.38. J: Mantener que censura no es verificación exitosa. |
| 04.23 | Methodological layers / Mutation-based property validation layer | 92 | RQ2 evaluates scientific mutants designed to remove specific protocol controls. | MERGE | 10 / 6 / 4 | LOW | F: Introducir RQ2. U: Mutantes científicos. R: Objeto de H2. En: 04.09-04.11. J: Integrar en 04.25. |
| 04.24 | Methodological layers / Mutation-based property validation layer | 94-102 | The evaluated defect classes are: removal of replay protection; destination | COMPRESS | 31 / 18 / 13 | LOW | F: Definir clases de defecto. U: Cinco controles intervenidos. R: Catálogo y tabla de mutantes. En: 05.14, 06.17. J: Enumeración compacta con referencia al catálogo concreto. |
| 04.25 | Methodological layers / Mutation-based property validation layer | 104-111 | The experimental unit consists of: a scientific mutant; a formal | KEEP | 17 / 17 / 0 | NONE | F: Definir unidad RQ2. U: Mutante, herramienta, configuración y repetición. R: Repeticiones del diseño. En: 05.15. J: Conservar unidad para evitar pseudorreplicación. |
| 04.26 | Methodological layers / Mutation-based property validation layer | 113 | Each mutant has one predefined target property. Detection is credited | MERGE | 17 / 12 / 5 | LOW | F: Fijar crédito de detección. U: Solo target predefinido. R: Definición canónica. En: 04.11. J: Integrar criterio con unidad 04.25. |
| 04.27 | Methodological layers / Mutation-based property validation layer | 115-123 | The principal aggregate measure is the mutation score, MutationScore = | KEEP | 13 / 13 / 0 | NONE | F: Definir MutationScore. U: Cociente detectados/mutantes. R: Aplicación numérica. En: 06.15. J: Conservar ecuación y significado. |
| 04.28 | Methodological layers / Mutation-based property validation layer | 125 | where (N_ detected ) is the number of predefined mutants | KEEP | 24 / 24 / 0 | NONE | F: Definir denominador/numerador. U: Conteo por mutante detectado por su target. R: Regla target. En: 04.11. J: No confundir 10 mutantes con 100 runs. |
| 04.29 | Methodological layers / Mutation-based property validation layer | 127 | The mutation score is restricted to the predefined mutant catalogue | KEEP | 25 / 25 / 0 | NONE | F: Limitar MutationScore. U: No completitud para todo defecto. R: Amenaza de muestreo. En: 08.30. J: Conservar límite junto a definición. |
| 04.30 | Methodological layers / Bounded implementation-model trace-conformance layer | 131 | RQ3 evaluates traces generated by the Java implementation. | MERGE | 8 / 5 / 3 | LOW | F: Introducir trazas. U: Procedencia Java. R: Pregunta RQ3. En: 04.12. J: Integrar en proyección 04.31. |
| 04.31 | Methodological layers / Bounded implementation-model trace-conformance layer | 133 | Concrete protocol executions are recorded deterministically and projected through the | KEEP | 32 / 32 / 0 | NONE | F: Definir proyección observable. U: Registro determinista y mapa Java-TLA+. R: Proyección del modelo. En: 03.17. J: Referencia semántica necesaria. |
| 04.32 | Methodological layers / Bounded implementation-model trace-conformance layer | 135-141 | The experimental unit consists of: a valid scenario or negative | KEEP | 19 / 19 / 0 | NONE | F: Definir unidad RQ3. U: Caso/semilla/replay. R: Diseño multiseed. En: 05.17-05.21. J: Conservar diferencia con repeticiones temporales. |
| 04.33 | Methodological layers / Bounded implementation-model trace-conformance layer | 143-148 | Two trace classes are evaluated: valid traces generated from supported | KEEP | 20 / 20 / 0 | NONE | F: Definir poblaciones. U: Trazas válidas y corrupción predefinida. R: Catálogos de resultados. En: 06.32-06.33. J: Necesario para interpretar rechazo. |
| 04.34 | Methodological layers / Bounded implementation-model trace-conformance layer | 150 | A valid trace is successfully classified when the projected action | KEEP | 32 / 32 / 0 | NONE | F: Definir clasificación correcta. U: Aceptación válida y rechazo esperado. R: H3. En: 04.13. J: Criterio científico central. |
| 04.35 | Methodological layers / Bounded implementation-model trace-conformance layer | 152 | For negative traces, the methodology also checks whether the observed | KEEP | 23 / 23 / 0 | NONE | F: Definir diagnóstico. U: Paso abstracto, concreto, acción e ID. R: Campos de resultados. En: 05.35. J: No reducir diagnóstico a fallo genérico. |
| 04.36 | Methodological layers / Bounded implementation-model trace-conformance layer | 154 | This procedure evaluates a finite catalogue of scenarios and mutations. | COMPRESS | 23 / 15 / 8 | LOW | F: Delimitar catálogo. U: Finitud de escenarios y mutaciones. R: Frontera RQ3. En: 04.14. J: Recordatorio breve con referencia. |
| 04.37 | Methodological layers / Verification-cost characterization layer | 158 | RQ4 characterizes the computational cost of bounded verification. | MERGE | 8 / 5 / 3 | LOW | F: Introducir RQ4. U: Coste acotado. R: Pregunta RQ4. En: 04.15. J: Integrar en unidad 04.38. |
| 04.38 | Methodological layers / Verification-cost characterization layer | 160-167 | The experimental unit consists of: a formal tool; a bound | KEEP | 19 / 19 / 0 | NONE | F: Definir unidad RQ4. U: Herramienta, perfil, fault profile y repetición. R: Diseño concreto. En: 05.04. J: Mantener todos los componentes. |
| 04.39 | Methodological layers / Verification-cost characterization layer | 169-179 | The principal response variables are: elapsed execution time; maximum resident | KEEP | 29 / 29 / 0 | NONE | F: Definir respuestas RQ4. U: Tiempo, RSS, estados, depth/scope, censura, ratios. R: Campos exactos. En: 05.32-05.33. J: Lista ya compacta y metodológicamente útil. |
| 04.40 | Methodological layers / Verification-cost characterization layer | 181 | TLC and Alloy expose different state representations and exploration semantics. | COMPRESS | 32 / 25 / 7 | LOW | F: Limitar comparación RQ4. U: Análisis dentro de cada herramienta. R: Threats de comparabilidad. En: 08.03-08.04. J: Mantener razón semántica. |
| 04.41 | Triangulation of evidence | 186 | The methodological layers address different failure modes in the scientific | MERGE | 11 / 7 / 4 | LOW | F: Introducir triangulación. U: Diferentes fallos argumentativos. R: Estudio multimétodo. En: 04.04. J: Integrar en explicación 04.42. |
| 04.42 | Triangulation of evidence | 188 | RQ1 asks whether the valid bounded models exhibit violations of | COMPRESS | 32 / 24 / 8 | LOW | F: Conectar RQ1/RQ2. U: Ausencia de contraejemplo no prueba sensibilidad. R: Discussion. En: 07.05-07.06. J: Conservar vínculo lógico. |
| 04.43 | Triangulation of evidence | 190 | RQ2 therefore introduces deliberately defective models and evaluates whether their | MERGE | 15 / 10 / 5 | LOW | F: Explicar control por mutación. U: Debilitamiento y target. R: Definición RQ2. En: 04.11. J: Fusionar con 04.42. |
| 04.44 | Triangulation of evidence | 192 | RQ3 addresses a different limitation: evidence obtained only from formal | COMPRESS | 48 / 28 / 20 | LOW | F: Conectar formal e implementación. U: Modelos solos no garantizan correspondencia Java. R: Definición RQ3. En: 04.31-04.35. J: Mantener función lógica sin redescribir replay. |
| 04.45 | Triangulation of evidence | 194 | RQ4 then characterizes the computational conditions under which the bounded | MERGE | 15 / 9 / 6 | LOW | F: Conectar coste con evidencia. U: Condiciones computacionales de obtención. R: Unidad RQ4. En: 04.38. J: Integrar en 04.44 como cierre. |
| 04.46 | Triangulation of evidence | 196-210 | The resulting methodological structure can be summarized as: valid formal | COMPRESS | 21 / 10 / 11 | LOW | F: Sintetizar correspondencias. U: Mapa RQ1-RQ4. R: Explicación inmediatamente anterior. En: 04.42-04.45. J: Pasar ecuación de correspondencias a texto breve. |
| 04.47 | Triangulation of evidence | 212 | The first three layers provide complementary evidence about bounded property | MERGE | 28 / 15 / 13 | LOW | F: Cerrar triangulación. U: Complementariedad y envelope de coste. R: Inicio de capas. En: 04.18, 04.42-04.45. J: Fusionar cierre sin recapitular cada capa. |
| 04.48 | Treatment of incomplete executions | 217 | Experimental outcomes are retained according to their observed status. Timeout, | COMPRESS | 27 / 18 / 9 | LOW | F: Declarar política de outcomes. U: Retención aun si desfavorables. R: Reglas operativas. En: 05.36-05.38. J: Mantener principio y remitir a diseño. |
| 04.49 | Treatment of incomplete executions | 219 | A failed instrumentation attempt may be repeated only under the | COMPRESS | 33 / 20 / 13 | LOW | F: Limitar reintentos. U: Solo instrumentación según protocolo. R: Regla concreta. En: 05.38. J: Conservar prohibición de repetir por resultado. |
| 04.50 | Treatment of incomplete executions | 221 | Timeout and out-of-memory observations are retained as censored results. They | COMPRESS | 25 / 20 / 5 | LOW | F: Definir censura. U: Sin valores artificiales ni éxito imputado. R: Política completa. En: 05.37. J: Mantener núcleo metodológico. |
| 04.51 | Treatment of incomplete executions | 223 | This policy prevents incomplete runs from being silently converted into | MERGE | 12 / 6 / 6 | LOW | F: Explicar finalidad de censura. U: Evitar convertir incompletos en positivos. R: 04.48 y 04.50. En: 04.48, 04.50. J: Integrar finalidad en párrafo anterior. |
| 04.52 | Interpretation boundaries | 228 | The study adopts explicit boundaries for interpretation. | DELETE_REDUNDANT | 7 / 0 / 7 | NONE | F: Introducir límites. U: Ninguna adicional al encabezado. R: Título Interpretation boundaries. En: 04.53-04.57. J: Eliminar frase introductoria vacía. |
| 04.53 | Interpretation boundaries | 230 | First, model checking is bounded by the finite configurations and | COMPRESS | 30 / 18 / 12 | LOW | F: Recordar límite formal. U: Boundedness. R: Definición RQ1. En: 04.08. J: Recordatorio corto con referencia canónica. |
| 04.54 | Interpretation boundaries | 232 | Second, mutation analysis evaluates only the predefined scientific mutant catalogue | COMPRESS | 19 / 12 / 7 | LOW | F: Recordar límite de mutación. U: Catálogo no completo. R: Definición MutationScore. En: 04.29. J: Conservar frontera en síntesis. |
| 04.55 | Interpretation boundaries | 234 | Third, trace replay evaluates bounded implementation-model conformance over the declared | COMPRESS | 24 / 15 / 9 | LOW | F: Recordar límite de conformance. U: No refinement/equivalencia. R: Definición RQ3. En: 04.14. J: Conservar términos sin nueva explicación extensa. |
| 04.56 | Interpretation boundaries | 236 | Fourth, TLC and Alloy use different semantics and cost units. | COMPRESS | 29 / 17 / 12 | LOW | F: Recordar comparación válida. U: No superioridad TLC-Alloy. R: Definición RQ4. En: 04.40. J: Reducir al límite esencial. |
| 04.57 | Interpretation boundaries | 238 | Finally, the evaluated configurations are abstractions of cross-shard transaction behavior. | COMPRESS | 32 / 22 / 10 | LOW | F: Limitar generalización. U: Modelos y ambiente no representan toda producción. R: Scope y External validity. En: 03.24, 08.18-08.26. J: Mantener alcance general en cierre compacto. |

#### 5. Experimental Design

Archivo: `manuscript/paper1/sections/05-experimental-design.tex`. [Fuente canónica](https://github.com/kapumota/dtl-lab/blob/5195d94b2ade1933a726d1d2106cc35a11c37f47/manuscript/paper1/sections/05-experimental-design.tex).

| ID | Subsección | Líneas | Primeras palabras | Clasificación | Actual / objetivo / ahorro | Riesgo | Función, contenido y justificación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 05.01 | Frozen experimental protocol | 7-11 | The definitive experiment follows protocol paper1-q3-v1, which was frozen before | COMPRESS | 35 / 27 / 8 | LOW | F: Identificar protocolo. U: paper1-q3-v1 y freeze previo. R: Principio metodológico. En: 04.02. J: Conservar identificador y resumir inventario de reglas. |
| 05.02 | Frozen experimental protocol | 13-17 | The scientific baseline corresponds to release v1.1.0-rc.1. The frozen toolchain | KEEP | 34 / 34 / 0 | NONE | F: Fijar toolchain. U: Release y versiones concretas. R: Instrumentación repetida. En: 05.26. J: Versiones necesarias para reproducir. |
| 05.03 | Frozen experimental protocol | 19-23 | The definitive design contains bounded property verification, scientific mutants, bounded | COMPRESS | 39 / 24 / 15 | LOW | F: Delimitar matriz definitiva. U: Prohibición de añadir configuraciones post hoc. R: Freeze y familias. En: 04.02, 05.04. J: Conservar regla y acortar inventario. |
| 05.04 | Configuration matrix | 28-38 | The experimental matrix contains fourteen configuration families: six valid-model scalability | KEEP | 53 / 53 / 0 | NONE | F: Enumerar familias. U: 6+2+2+4 familias. R: Expansión a tareas. En: 05.05. J: Descomposición necesaria para revisar diseño. |
| 05.05 | Configuration matrix | 40-44 | The fourteen frozen configuration families expand, according to their properties, | KEEP | 38 / 38 / 0 | NONE | F: Explicar tamaño campaña. U: 1272 con warmups y case-seed RQ3. R: Overview Results. En: 06.01. J: Mantener reglas de expansión. |
| 05.06 | Configuration matrix | 46-48 | Each task carries a protocol identifier, configuration identifier, tool, model | COMPRESS | 20 / 16 / 4 | LOW | F: Identificar registros. U: IDs, configuración, seed/repetición y provenance. R: Campos de outputs. En: 05.35. J: Agrupar metadata sin excluir identidad de tareas. |
| 05.07 | Bound profiles | 53-55 | Three ordered profiles define the principal bounded scalability configurations. Table | COMPRESS | 13 / 8 / 5 | NONE | F: Introducir tabla. U: Tres perfiles ordenados. R: Tabla inmediata. En: 05.08. J: Remisión breve suficiente. |
| 05.08 | Bound profiles | 57-79 | Bound profiles used in the definitive experiment. Profile Shards Transfers | KEEP | 42 / 42 / 0 | NONE | F: Documentar bounds. U: Parámetros TLC y scopes Alloy separados. R: Texto aclaratorio. En: 05.09. J: Mantener Tabla 1 completa y formato auditado. |
| 05.09 | Bound profiles | 81-88 | The profile records share the shard, transfer, and validator counts | KEEP | 82 / 82 / 0 | NONE | F: Aclarar semántica Tabla 1. U: Receipt/Message scopes y Alloy >=2 votos. R: Tabla parcial y propiedad quorum. En: 05.08, 03.21. J: No reabrir B2 ni borrar diferencias entre herramientas. |
| 05.10 | Bound profiles | 90-93 | The profiles are experimental bundles rather than independent one-factor perturbations. | KEEP | 24 / 24 / 0 | NONE | F: Definir perfil compuesto. U: Factores varían simultáneamente. R: Consecuencia inferencial. En: 05.11. J: Necesario para no atribuir causalidad a un factor. |
| 05.11 | Bound profiles | 95-97 | Consequently, profile-level cost changes are interpreted as changes associated with | MERGE | 24 / 17 / 7 | LOW | F: Limitar causalidad. U: Cambios asociados al bundle completo. R: Definición de perfiles. En: 05.10. J: Integrar consecuencia con definición. |
| 05.12 | Bound profiles | 99-100 | The catalog profile is reserved for the scenario catalogue used | KEEP | 15 / 15 / 0 | NONE | F: Definir catalog profile. U: Perfil reservado a escenarios RQ3. R: Diseño trazas. En: 05.16-05.21. J: Evitar confusión con small/medium/large. |
| 05.13 | Mutation configurations | 105 | Mutation experiments are executed at the medium profile. | KEEP | 8 / 8 / 0 | NONE | F: Fijar perfil de mutación. U: Medium. R: Descripción Results. En: 06.14. J: Parámetro de diseño imprescindible. |
| 05.14 | Mutation configurations | 107-116 | Five scientific defect classes are instantiated for each formal representation: | KEEP | 35 / 35 / 0 | NONE | F: Enumerar defectos. U: Cinco clases por representación. R: Methodology y tabla RQ2. En: 04.24, 06.17. J: Conservar catálogo ejecutado. |
| 05.15 | Mutation configurations | 118-120 | Each mutant has a predefined target property. Ten measured repetitions | KEEP | 27 / 27 / 0 | NONE | F: Fijar replicación de mutantes. U: Target, 2 warmups, 10 measured y consistencia. R: Resultados mutantes. En: 06.14-06.21. J: Diseño no sustituible por resultados. |
| 05.16 | Trace-conformance configurations | 125 | RQ3 uses a separate multiseed design. | MERGE | 6 / 4 / 2 | LOW | F: Introducir multiseed. U: RQ3 separado. R: Diseño subsiguiente. En: 05.17, 05.21. J: Integrar con tamaños de poblaciones. |
| 05.17 | Trace-conformance configurations | 127-132 | The catalogue contains ten valid scenarios and ten negative cases. | COMPRESS | 22 / 18 / 4 | LOW | F: Definir población válida. U: 10 casos por 30 semillas igual300. R: RQ3 Results. En: 06.27-06.28. J: Integrar ecuación en prosa preservando factores. |
| 05.18 | Trace-conformance configurations | 134-138 | valid trace executions and 10 30 = 300 | MERGE | 7 / 6 / 1 | LOW | F: Definir población negativa. U: 10 por 30 igual300 negativos. R: Población válida paralela. En: 05.17. J: Fusionar en una oración con ambos tamaños. |
| 05.19 | Trace-conformance configurations | 140 | negative trace executions. | MERGE | 3 / 2 / 1 | LOW | F: Completar población negativa. U: Etiqueta negativa. R: Ecuación previa. En: 05.18. J: Unir fragmento sintáctico a la oración anterior. |
| 05.20 | Trace-conformance configurations | 142-143 | The definitive seed sequence contains the integer values 2026001 through | KEEP | 11 / 11 / 0 | NONE | F: Fijar seeds. U: 2026001 a 2026030. R: Semillas genéricas. En: 04.32. J: Conservar rango exacto. |
| 05.21 | Trace-conformance configurations | 145-148 | RQ3 therefore contains 600 TLC replay executions. Each case-seed pair | KEEP | 31 / 31 / 0 | NONE | F: Definir ejecuciones RQ3. U: 600, una por par, sin warmup y razón. R: Resultados RQ3. En: 06.27. J: No convertir seeds en réplicas de rendimiento. |
| 05.22 | Repetitions and execution order | 153-162 | For model-checking configurations used to measure time and memory, the | KEEP | 29 / 29 / 0 | NONE | F: Fijar repeticiones temporales. U: 2 warmups, 10 medidas, serial y exclusión warmups. R: Mitigación interna. En: 08.10-08.11. J: Conservar reglas completas. |
| 05.23 | Repetitions and execution order | 164-167 | Logical outcomes are expected to remain consistent across repetitions. The | KEEP | 30 / 30 / 0 | NONE | F: Definir sentido de réplica. U: Consistencia lógica sin independencia lógica nueva. R: RQ2 Results. En: 06.20. J: Protege contra pseudorreplicación. |
| 05.24 | Repetitions and execution order | 169-170 | No run is repeated merely because it produces a counterexample, | MERGE | 16 / 10 / 6 | LOW | F: Restringir reruns. U: No repetir por resultado desfavorable. R: Regla canónica. En: 05.38. J: Integrar remisión breve en repeticiones. |
| 05.25 | Resource limits | 175-184 | Each measured model-checking execution is constrained by the following resource | KEEP | 30 / 30 / 0 | NONE | F: Fijar recursos. U: 1800s, 12288MiB, serial, 1 worker, SAT4J. R: Toolchain y censura. En: 05.02, 05.37. J: Todos los valores son indispensables. |
| 05.26 | Resource limits | 186-187 | Execution-time and maximum-resident-memory measurements are collected through /usr/bin/time -v. | MERGE | 9 / 5 / 4 | LOW | F: Identificar medición. U: GNU time -v. R: Toolchain. En: 05.02. J: Mantener remisión dentro de recursos. |
| 05.27 | Resource limits | 189-191 | Timeout and out-of-memory outcomes are considered scientifically meaningful scalability outcomes. | COMPRESS | 23 / 15 / 8 | LOW | F: Conservar outcomes negativos. U: Timeout/OOM permanecen. R: Política canónica. En: 05.37. J: Resumir en una oración con referencia. |
| 05.28 | Execution environment | 196-197 | The definitive timing and memory campaign is executed on a | KEEP | 14 / 14 / 0 | NONE | F: Definir host campaña. U: Linux nativo dedicado. R: Internal validity. En: 08.10. J: Condición de medida necesaria. |
| 05.29 | Execution environment | 199-203 | Virtualized environments, WSL, and shared continuous-integration runners are excluded from | COMPRESS | 40 / 30 / 10 | LOW | F: Separar CI de campaña. U: Exclusión WSL, virtualización, CI compartida para timing. R: Host dedicado. En: 05.28. J: Mantener exclusiones y uso funcional de CI. |
| 05.30 | Execution environment | 205-208 | The recorded environment includes the operating system and kernel, processor | KEEP | 27 / 27 / 0 | NONE | F: Definir metadata ambiental. U: OS, kernel, CPU, RAM, versiones, commit, virtualización. R: Provenance general. En: 09.25. J: Mantener inventario sin trasladar información esencial. |
| 05.31 | Measured outcomes | 213 | The experiment records both logical and resource-oriented outcomes. | MERGE | 8 / 5 / 3 | LOW | F: Introducir outcomes. U: Resultados lógicos y recursos. R: Listas siguientes. En: 05.32-05.33. J: Fusionar apertura con listas. |
| 05.32 | Measured outcomes | 215-226 | For TLC, recorded measurements include: property result; generated states; distinct | KEEP | 25 / 25 / 0 | NONE | F: Definir outputs TLC. U: Todos los campos TLC. R: Variables generales. En: 04.21. J: Lista concreta ya compacta. |
| 05.33 | Measured outcomes | 228-237 | For Alloy, recorded measurements include: assertion result; solver and scope; | KEEP | 25 / 25 / 0 | NONE | F: Definir outputs Alloy. U: Todos los campos Alloy. R: Variables generales. En: 04.21. J: Lista concreta ya compacta. |
| 05.34 | Measured outcomes | 239-240 | TLC state counts and Alloy scopes are not treated as | KEEP | 13 / 13 / 0 | NONE | F: Limitar unidad estado/scope. U: No equivalencia de unidades. R: Comparabilidad. En: 04.40. J: Advertencia necesaria junto a campos. |
| 05.35 | Measured outcomes | 242-245 | For trace conformance, the recorded fields include case identifier, seed, | KEEP | 32 / 32 / 0 | NONE | F: Definir outputs replay. U: Diagnóstico desglosado, recursos, commit, input hashes. R: Criterio de diagnóstico. En: 04.35. J: Mantener trazabilidad del replay. |
| 05.36 | Incomplete executions and censoring | 250-252 | Experimental executions may terminate as completed runs, counterexamples, timeouts, out-of-memory | KEEP | 16 / 16 / 0 | NONE | F: Definir clasificación outcomes. U: Completado, counterexample, timeout, OOM, tool, instrumentation. R: Methodology. En: 04.48. J: Conservar distinciones canónicas. |
| 05.37 | Incomplete executions and censoring | 254-256 | Timeout and out-of-memory outcomes are retained as censored observations. Their | KEEP | 27 / 27 / 0 | NONE | F: Definir censura operativa. U: Sin imputar límites ni valores sintéticos. R: Principio metodológico. En: 04.50. J: Conservar texto canónico. |
| 05.38 | Incomplete executions and censoring | 258-260 | Unexpected tool exits remain tool errors. Instrumentation failures may be | KEEP | 29 / 29 / 0 | NONE | F: Definir retry/error. U: Tool error distinto de instrumentation y regla retry. R: Methodology. En: 04.49. J: No colapsar fallo científico e instrumental. |
| 05.39 | Incomplete executions and censoring | 262-263 | This treatment preserves unfavorable outcomes and prevents selective removal of | MERGE | 16 / 8 / 8 | LOW | F: Explicar mitigación. U: Evitar selección de outcomes. R: Principio general. En: 04.51. J: Fusionar motivación con 05.38. |
| 05.40 | Statistical analysis | 268-270 | Time and memory distributions are summarized using the median and | KEEP | 24 / 24 / 0 | NONE | F: Definir estadísticos. U: Mediana, IQR, min, max, CV. R: Resultados costes. En: 06.41-06.48. J: No quitar medidas definidas. |
| 05.41 | Statistical analysis | 272-273 | A 95% bootstrap confidence interval for the median is estimated | KEEP | 14 / 14 / 0 | NONE | F: Definir bootstrap. U: 95% y 10000 remuestreos. R: Ninguna duplicación operativa completa. En: Ninguna. J: Conservar parámetros exactos. |
| 05.42 | Statistical analysis | 275-276 | For proportions, including mutation detection and trace classification, Wilson 95% | KEEP | 14 / 14 / 0 | NONE | F: Definir intervalos de proporciones. U: Wilson95%. R: Resultado RQ3. En: 06.31. J: Mantener método de intervalo. |
| 05.43 | Statistical analysis | 278-281 | RQ4 reports profile-level medians and within-tool growth ratios. Spearman rank | KEEP | 30 / 30 / 0 | NONE | F: Definir RQ4 descriptivo. U: Medianas, ratios y Spearman condicionado a perfiles completados. R: Resultados RQ4. En: 06.43. J: Conservar condición de aplicabilidad. |
| 05.44 | Statistical analysis | 283-286 | TLC and Alloy are not compared through absolute execution-time claims. | COMPRESS | 27 / 20 / 7 | LOW | F: Limitar comparación. U: Sin superioridad absoluta entre herramientas. R: Methodology y Construct validity. En: 04.40, 08.03. J: Una frase local con causa semántica. |
| 05.45 | Statistical analysis | 288-290 | The three ordered profiles provide descriptive evidence about bounded cost | COMPRESS | 27 / 20 / 7 | LOW | F: Limitar curva inferida. U: Tres perfiles no establecen ley. R: Conclusion validity. En: 08.33-08.36. J: Mantener limitación próxima al método estadístico. |

#### 6. Results

Archivo: `manuscript/paper1/sections/06-results.tex`. [Fuente canónica](https://github.com/kapumota/dtl-lab/blob/5195d94b2ade1933a726d1d2106cc35a11c37f47/manuscript/paper1/sections/06-results.tex).

| ID | Subsección | Líneas | Primeras palabras | Clasificación | Actual / objetivo / ahorro | Riesgo | Función, contenido y justificación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 06.01 | Experimental campaign overview | 7-10 | The definitive campaign contained 1272 scheduled experimental tasks across the | KEEP | 34 / 34 / 0 | NONE | F: Definir campaña reportada. U: 1272 y measured vs warmups. R: Diseño. En: 05.05. J: Mantener universo de evidencia. |
| 06.02 | Experimental campaign overview | 12-14 | The results are presented in the order of the research | COMPRESS | 23 / 12 / 11 | LOW | F: Orientar lectura. U: Orden RQ y política de censura. R: Títulos y diseño. En: 05.36-05.38. J: Quitar navegación repetida conservando remisión. |
| 06.03 | RQ1: Bounded property verification | 19-20 | RQ1 asks whether the valid formal models satisfy the seven | COMPRESS | 17 / 10 / 7 | LOW | F: Introducir RQ1. U: Objeto de resultado. R: Pregunta metodológica. En: 04.06. J: Recordatorio breve. |
| 06.04 | RQ1: Bounded property verification | 22-24 | Across both formal tools and the three bound profiles, RQ1 | KEEP | 31 / 31 / 0 | NONE | F: Reportar agregado RQ1. U: 420, 350, 70 y cero contraejemplos completados. R: Tabla2. En: 06.06. J: Mantener resultado esencial junto al denominador. |
| 06.05 | RQ1: Bounded property verification | 26-27 | Table summarizes the result by tool and bound profile. | COMPRESS | 9 / 6 / 3 | NONE | F: Remitir a tabla. U: Localización Tabla2. R: Caption. En: 06.06. J: Remisión más corta. |
| 06.06 | RQ1: Bounded property verification | 29-47 | RQ1 bounded property-verification results by tool and profile. Tool Profile | KEEP | 56 / 56 / 0 | NONE | F: Reportar RQ1 detallado. U: Distribución por herramienta y perfil. R: Narrativa agregada. En: 06.04. J: Conservar todos los valores de Tabla2. |
| 06.07 | RQ1: Bounded property verification | 49-51 | For Alloy, all seven properties completed without a counterexample under | COMPRESS | 28 / 20 / 8 | MEDIUM | F: Explicar Alloy RQ1. U: 7 propiedades, 3 perfiles, 10 repeticiones, 210. R: Tabla2 y diseño. En: 06.06, 05.22. J: Acortar sin perder relación entre unidades y total. |
| 06.08 | RQ1: Bounded property verification | 53-58 | For TLC, all seven properties completed without a counterexample under | COMPRESS | 49 / 38 / 11 | MEDIUM | F: Explicar TLC RQ1. U: 140 completadas y 70 a 1800s. R: Tabla2 y recursos. En: 06.06, 05.25. J: Mantener plazo y denominador de censura. |
| 06.09 | RQ1: Bounded property verification | 60-62 | No out-of-memory or tool-error outcome was observed in the RQ1 | KEEP | 23 / 23 / 0 | NONE | F: Reportar ausencia OOM/errores. U: Cero OOM y tool errors RQ1. R: Censura TLC-large. En: 06.04. J: No omitir outcomes fuera de tabla. |
| 06.10 | Answer to RQ1 | 67-69 | Within the 350 completed bounded model-checking runs, no violation of | MERGE | 26 / 20 / 6 | MEDIUM | F: Responder RQ1. U: 350 completadas sin violación. R: Agregado. En: 06.04. J: Integrar respuesta con 06.11-06.12. |
| 06.11 | Answer to RQ1 | 71-73 | However, the 70 large-profile TLC measurements did not complete within | KEEP | 26 / 26 / 0 | NONE | F: Delimitar evidencia incompleta. U: 70 incompletas no son checks exitosos. R: Censura operativa. En: 05.37. J: Mantener advertencia en respuesta. |
| 06.12 | Answer to RQ1 | 75-77 | H1 is therefore partially supported under censoring. The result is | KEEP | 26 / 26 / 0 | NONE | F: Resolver H1. U: Parcialmente apoyada bajo censura y no prueba no acotada. R: Discussion. En: 07.03. J: Conservar conclusión epistemológica exacta. |
| 06.13 | RQ2: Mutation-based property validation | 82-84 | RQ2 evaluates whether the declared property suite detects predefined scientific | COMPRESS | 18 / 10 / 8 | LOW | F: Introducir RQ2. U: Defectos que eliminan controles. R: Pregunta metodológica. En: 04.09. J: Reducir recordatorio. |
| 06.14 | RQ2: Mutation-based property validation | 86-89 | The experiment contained ten scientific mutants: five instantiated in the | KEEP | 35 / 35 / 0 | NONE | F: Definir muestra RQ2. U: 5+5, medium, 10 réplicas, 100 medidas. R: Diseño. En: 05.13-05.15. J: Mantener denominadores distintos. |
| 06.15 | RQ2: Mutation-based property validation | 91-102 | All ten scientific mutants were detected through their designated target | KEEP | 28 / 28 / 0 | NONE | F: Reportar detección. U: 10/10 target, consistencia, score1.0. R: Tabla3. En: 06.17. J: Conservar evidencia aunque ecuación pueda ir inline. |
| 06.16 | RQ2: Mutation-based property validation | 104 | Table summarizes the aggregate result. | KEEP | 5 / 5 / 0 | NONE | F: Remitir a Tabla3. U: Ubicación de detalle. R: Caption. En: 06.17. J: Frase ya mínima. |
| 06.17 | RQ2: Mutation-based property validation | 106-167 | RQ2 scientific mutants, target properties, and detection results. Tool Mutant | KEEP | 64 / 64 / 0 | NONE | F: Reportar mutantes individuales. U: Diez mutantes y targets por herramienta. R: Texto de diferencias. En: 06.18. J: Conservar Tabla3 íntegra. |
| 06.18 | RQ2: Mutation-based property validation | 169-174 | The commit-after-abort defect is associated with different designated target properties | KEEP | 39 / 39 / 0 | NONE | F: Explicar target distinto. U: Alloy TerminalStateIrreversibility y TLC DecisionConsistency. R: Construct validity. En: 08.02. J: Protege interpretación de tabla. |
| 06.19 | RQ2: Mutation-based property validation | 176-179 | The evaluated defect classes covered removal of replay protection, destination | COMPRESS | 31 / 20 / 11 | MEDIUM | F: Resumir defectos cubiertos. U: Cinco controles. R: Tabla3 y catálogo. En: 06.17, 05.14. J: Mantener alcance, acortar enumeración repetida. |
| 06.20 | RQ2: Mutation-based property validation | 181-184 | Detection was evaluated at the mutant level rather than by | KEEP | 34 / 34 / 0 | NONE | F: Evitar pseudorreplicación. U: 100 runs no son100 mutantes. R: Unidad RQ2. En: 04.25, 04.28. J: Conservar distinción explícita. |
| 06.21 | RQ2: Mutation-based property validation | 186-188 | The repeated measurements also verified consistency of the logical outcome: | MERGE | 24 / 12 / 12 | MEDIUM | F: Reportar estabilidad. U: Mismo target en las 10 repeticiones. R: Detección inicial. En: 06.15. J: Fusionar con resultado manteniendo estabilidad. |
| 06.22 | RQ2: Mutation-based property validation | 190-194 | No absolute execution-time comparison between TLC and Alloy is used | COMPRESS | 39 / 18 / 21 | MEDIUM | F: Limitar comparaciones en RQ2. U: Resultado es detección, no tiempo absoluto. R: Comparabilidad. En: 04.40, 05.44. J: Mantener límite breve sin derivar superioridad. |
| 06.23 | Answer to RQ2 | 199-201 | All ten predefined scientific mutants were detected through their target | MERGE | 24 / 18 / 6 | MEDIUM | F: Responder RQ2. U: 10/10 y score1.0. R: Resultado inicial. En: 06.15. J: Fusionar con 06.24-06.25 manteniendo respuesta visible. |
| 06.24 | Answer to RQ2 | 203-204 | H2 is therefore supported for the predefined scientific mutant catalogue. | KEEP | 10 / 10 / 0 | NONE | F: Resolver H2. U: Apoyo restringido al catálogo. R: Resultado. En: 06.23. J: Conservar estado de hipótesis. |
| 06.25 | Answer to RQ2 | 206-210 | This result demonstrates that the evaluated property suite is sensitive | COMPRESS | 37 / 28 / 9 | MEDIUM | F: Limitar generalización RQ2. U: Sensibilidad, no completitud. R: Methodology y Threats. En: 04.29, 08.30. J: No suprimir límite de catálogo. |
| 06.26 | RQ3: Bounded implementation-model trace conformance | 215-218 | RQ3 evaluates whether observable traces produced by the Java implementation | COMPRESS | 27 / 18 / 9 | LOW | F: Introducir RQ3. U: Java-TLA+ bajo escenarios/bounds. R: Pregunta metodológica. En: 04.12. J: Acortar recordatorio conservando modelo. |
| 06.27 | RQ3: Bounded implementation-model trace conformance | 220-222 | The experiment contained ten valid scenarios and ten deliberately corrupted | KEEP | 25 / 25 / 0 | NONE | F: Definir muestra RQ3. U: 10+10 casos, 30 seeds, 600. R: Diseño. En: 05.17-05.21. J: Mantener estructura poblacional. |
| 06.28 | RQ3: Bounded implementation-model trace conformance | 224-227 | All 300 valid traces were accepted by the bounded replay | KEEP | 34 / 34 / 0 | NONE | F: Reportar RQ3. U: 300 aceptadas, 300 rechazadas, 300 diagnósticos. R: Tabla4. En: 06.30. J: Resultado principal debe quedar explícito. |
| 06.29 | RQ3: Bounded implementation-model trace conformance | 229-230 | Table summarizes the aggregate classification results. | KEEP | 6 / 6 / 0 | NONE | F: Remitir a Tabla4. U: Ubicación del resumen. R: Caption. En: 06.30. J: Frase mínima. |
| 06.30 | RQ3: Bounded implementation-model trace conformance | 232-250 | RQ3 bounded trace-conformance results. Trace class Cases Runs Correct classification | KEEP | 26 / 26 / 0 | NONE | F: Reportar clasificación agregada. U: Poblaciones y diagnósticos. R: Narrativa principal. En: 06.28. J: Conservar Tabla4 completa. |
| 06.31 | RQ3: Bounded implementation-model trace conformance | 252-257 | At the individual-case level, every one of the twenty scenarios | KEEP | 53 / 53 / 0 | NONE | F: Reportar precisión por caso. U: 20 casos, 30/30, Wilson[0.8865, 1]. R: Método estadístico. En: 05.42. J: Única cuantificación del intervalo, no recortar. |
| 06.32 | RQ3: Bounded implementation-model trace conformance | 259-263 | The valid catalogue includes normal commit, timeout-related behavior, duplicated receipts, | KEEP | 35 / 35 / 0 | NONE | F: Identificar escenarios válidos. U: Diez escenarios concretos. R: Catálogo general. En: 04.33. J: Necesario para evaluar cobertura. |
| 06.33 | RQ3: Bounded implementation-model trace conformance | 265-270 | The negative catalogue contains deliberately corrupted traces, including commit from | KEEP | 43 / 43 / 0 | NONE | F: Identificar corrupciones. U: Diez corrupciones concretas. R: Catálogo general. En: 04.33. J: No trasladar fuera del paper. |
| 06.34 | RQ3: Bounded implementation-model trace conformance | 272-276 | The diagnostic criterion is evaluated only for negative cases because | COMPRESS | 45 / 35 / 10 | MEDIUM | F: Interpretar diagnóstico reportado. U: Solo negativos y mismatch esperado, no fallo genérico. R: Definición metodológica. En: 04.35. J: Conservar distinción científica local. |
| 06.35 | Answer to RQ3 | 281-283 | All 300 valid traces were accepted, all 300 deliberately corrupted | MERGE | 23 / 20 / 3 | MEDIUM | F: Responder RQ3. U: Tres resultados300/300. R: Resultado inicial. En: 06.28. J: Integrar con 06.36 sin borrar respuesta. |
| 06.36 | Answer to RQ3 | 285 | H3 is therefore supported for the predefined bounded trace catalogue. | KEEP | 10 / 10 / 0 | NONE | F: Resolver H3. U: Apoyo al catálogo acotado. R: Resultado. En: 06.35. J: Mantener estado y alcance. |
| 06.37 | Answer to RQ3 | 287-291 | This result provides evidence of bounded implementation-model trace conformance between | KEEP | 45 / 45 / 0 | NONE | F: Delimitar RQ3. U: No refinement, equivalencia ni garantía universal. R: Discussion y Threats. En: 07.14, 08.05. J: Mantener límite junto a respuesta. |
| 06.38 | RQ4: Bounded verification cost | 296-298 | RQ4 characterizes how the computational cost of bounded verification changes | COMPRESS | 24 / 17 / 7 | LOW | F: Introducir RQ4. U: Perfiles de tamaño y fallos. R: Pregunta metodológica. En: 04.15. J: Reducir recordatorio. |
| 06.39 | RQ4: Bounded verification cost | 300-303 | The RQ4 evidence comprises 460 measured executions. Of these, 420 | KEEP | 32 / 32 / 0 | NONE | F: Definir evidencia RQ4. U: 460, 420 compartidas RQ1 y 40 adicionales. R: Diseño. En: 05.04. J: Evitar sumar dos veces ejecuciones compartidas. |
| 06.40 | Cost across bound profiles | 308-309 | Table summarizes elapsed time, memory, and available state-space measurements for | COMPRESS | 14 / 10 / 4 | LOW | F: Remitir a Tabla5. U: Tiempo, RSS y perfil. R: Caption. En: 06.41. J: No atribuir a tabla columnas de estados ausentes. |
| 06.41 | Cost across bound profiles | 311-335 | RQ4 cost by formal tool and bound profile. Tool Profile | KEEP | 59 / 59 / 0 | NONE | F: Reportar coste por perfil. U: Todos tiempos, RSS, censura y denominadores. R: Narrativa. En: 06.42, 06.44. J: Conservar Tabla5 íntegra. |
| 06.42 | Cost across bound profiles | 337-341 | All Alloy profiles completed. Median elapsed time increased from 0.515306 | COMPRESS | 44 / 23 / 21 | MEDIUM | F: Describir Alloy. U: Monotonía de tiempo y RSS. R: Todos los valores en Tabla5. En: 06.41. J: Conservar cifras en tabla y una frase de tendencia. |
| 06.43 | Cost across bound profiles | 343-346 | Across the three Alloy profiles, Spearman rank association between the | KEEP | 40 / 40 / 0 | NONE | F: Reportar asociación y ratios. U: Spearman1.0, ratios3.043 y 1.766. R: Discussion. En: 07.17. J: Datos que no aparecen en Tabla5. |
| 06.44 | Cost across bound profiles | 348-352 | For TLC, the small and medium profiles completed, with median | COMPRESS | 45 / 31 / 14 | MEDIUM | F: Describir TLC completado. U: Estados distintos14 y 24336. R: Tiempo/RSS en Tabla5. En: 06.41. J: Conservar estados y resumir solo valores tabulados. |
| 06.45 | Cost across bound profiles | 354-357 | The large TLC profile did not yield completed measurements. All | KEEP | 30 / 30 / 0 | NONE | F: Reportar TLC-large. U: 70 timeout1800s y sin medianas completadas. R: Tabla5. En: 06.41. J: Mantener significado de guiones. |
| 06.46 | Cost across bound profiles | 359-363 | The available TLC measurements exhibit increasing cost from small to | COMPRESS | 43 / 30 / 13 | MEDIUM | F: Delimitar forma de crecimiento. U: Dos niveles completos, no análisis a tres. R: Respuesta H4. En: 06.53-06.54. J: Mantener límite local con menos recapitulación. |
| 06.47 | Cost under enabled fault profiles | 368-370 | The additional TLC fault experiment evaluates four medium-bound configurations. All | KEEP | 20 / 20 / 0 | NONE | F: Definir fault experiment. U: 40 medium sin timeout/OOM/tool errors. R: Tabla6. En: 06.48. J: Preservar outcomes no incluidos en tabla. |
| 06.48 | Cost under enabled fault profiles | 372-390 | RQ4 TLC cost under enabled fault profiles. Fault profile Runs | KEEP | 33 / 33 / 0 | NONE | F: Reportar costes por fallo. U: Cuatro perfiles y todos sus valores. R: Narrativa. En: 06.49. J: Conservar Tabla6 íntegra. |
| 06.49 | Cost under enabled fault profiles | 392-396 | The enabled fault condition therefore affects the cost observed within | COMPRESS | 39 / 27 / 12 | MEDIUM | F: Describir extremos de fallo. U: Timeout máximo y normal mínimo. R: Tabla6. En: 06.48. J: Acortar comparación sin generalizar. |
| 06.50 | Cost under enabled fault profiles | 398-400 | These measurements characterize the evaluated fault profiles only. They are | KEEP | 21 / 21 / 0 | NONE | F: Limitar ranking de fallos. U: Solo perfiles evaluados. R: Discussion. En: 07.20. J: Advertencia necesaria junto a resultados. |
| 06.51 | Answer to RQ4 | 405-406 | The experiments provide evidence that bounded verification cost increases as | MERGE | 14 / 8 / 6 | MEDIUM | F: Abrir respuesta RQ4. U: Crecimiento observado. R: Descripción siguiente. En: 06.52. J: Integrar en 06.52. |
| 06.52 | Answer to RQ4 | 408-411 | For Alloy, all three ordered profiles completed and both median | COMPRESS | 37 / 27 / 10 | MEDIUM | F: Sintetizar RQ4. U: Alloy3 niveles, TLC2 y large censurado. R: Datos previos. En: 06.41-06.46. J: Mantener contraste de completitud. |
| 06.53 | Answer to RQ4 | 413-416 | These observations support a descriptive claim of increasing verification cost. | MERGE | 37 / 24 / 13 | MEDIUM | F: Separar crecimiento de H4. U: Datos insuficientes para nonlinear. R: Veredicto siguiente. En: 06.54. J: Integrar justificación sin diluir H4. |
| 06.54 | Answer to RQ4 | 418-420 | Accordingly, H4 remains unconfirmed in its nonlinear form: the experiment | KEEP | 25 / 25 / 0 | NONE | F: Resolver H4. U: Nonlinear no confirmada, sin ley asintótica. R: Discussion y Threats. En: 07.21, 08.36. J: Conclusión esencial intocable. |
| 06.55 | Answer to RQ4 | 422-425 | All cost comparisons are interpreted within each formal tool. The | COMPRESS | 31 / 23 / 8 | MEDIUM | F: Limitar comparación entre tools. U: Sin superioridad absoluta por semánticas diferentes. R: Método y Threats. En: 04.40, 08.03. J: Conservar límite en respuesta final. |

#### 7. Discussion

Archivo: `manuscript/paper1/sections/07-discussion.tex`. [Fuente canónica](https://github.com/kapumota/dtl-lab/blob/5195d94b2ade1933a726d1d2106cc35a11c37f47/manuscript/paper1/sections/07-discussion.tex).

| ID | Subsección | Líneas | Primeras palabras | Clasificación | Actual / objetivo / ahorro | Riesgo | Función, contenido y justificación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 07.01 | Bounded verification evidence | 7-14 | The RQ1 results provide bounded evidence that no violations of | COMPRESS | 65 / 35 / 30 | LOW | F: Interpretar cobertura RQ1. U: Relevancia de obligaciones complementarias. R: Resultados y siete propiedades. En: 06.04, 03.21. J: Retener interpretación y abreviar inventario de propiedades. |
| 07.02 | Bounded verification evidence | 16-23 | The strength of this evidence is determined by the explored | COMPRESS | 71 / 42 / 29 | LOW | F: Interpretar censura. U: Timeout delimita espacio verificado, no violación. R: Resultados por perfil. En: 06.08, 06.45. J: Conservar significado sin renarrar perfiles completos. |
| 07.03 | Bounded verification evidence | 25-30 | This distinction is important for interpreting H1. The experiment supports | COMPRESS | 58 / 35 / 23 | LOW | F: Delimitar H1. U: No extender ausencia a estados no explorados. R: Respuesta RQ1. En: 06.12. J: Mantener inferencia específica en versión breve. |
| 07.04 | Bounded verification evidence | 32-38 | The complementary use of TLC and Alloy strengthens the bounded | COMPRESS | 60 / 43 / 17 | LOW | F: Interpretar doble formalismo. U: Corroboración sin espacios ni pruebas equivalentes. R: Modelo y Construct validity. En: 03.18-03.20, 08.01. J: Mantener distinción y acortar introducción. |
| 07.05 | Mutation sensitivity and property adequacy | 43-50 | RQ2 complements the absence-of-counterexample evidence from RQ1 by testing whether | COMPRESS | 74 / 42 / 32 | LOW | F: Interpretar sensibilidad. U: Mutantes hacen más informativa la satisfacción válida. R: 10/10, score, réplicas. En: 06.15, 06.23. J: Una referencia numérica breve y priorizar interpretación. |
| 07.06 | Mutation sensitivity and property adequacy | 52-60 | This distinction strengthens the interpretation of the formal verification results. | MERGE | 76 / 43 / 33 | LOW | F: Explicar adecuación dirigida. U: Mecanismos ejercitados por mutantes. R: Interpretación anterior y catálogo. En: 07.05, 05.14. J: Fusionar con 07.05 sin otra lista extensa. |
| 07.07 | Mutation sensitivity and property adequacy | 62-68 | The mutation results also expose the role of tool-specific formal | COMPRESS | 50 / 35 / 15 | LOW | F: Interpretar encodings. U: Misma intención de defecto con targets distintos. R: Detalle exacto. En: 06.18. J: Mantener significado y remitir a Tabla3. |
| 07.08 | Mutation sensitivity and property adequacy | 70-75 | The interpretation nevertheless remains bounded by the predefined mutation catalogue. | COMPRESS | 50 / 32 / 18 | LOW | F: Limitar score perfecto. U: No completitud frente a errores arbitrarios. R: Methodology y Threats. En: 04.29, 08.30. J: Advertencia interpretativa corta. |
| 07.09 | Mutation sensitivity and property adequacy | 77-82 | Accordingly, the contribution of RQ2 is evidence of property sensitivity | MERGE | 45 / 22 / 23 | LOW | F: Cerrar RQ1/RQ2. U: Satisfacción y sensibilidad complementarias. R: 07.05-07.06. En: 07.05-07.06. J: Fusionar cierre evitando tercera formulación. |
| 07.10 | Implementation-model trace conformance | 87-92 | RQ3 connects the formal models with the executable Java implementation | COMPRESS | 54 / 25 / 29 | LOW | F: Introducir interpretación RQ3. U: Vínculo observado Java-TLA+. R: 300/300 y diagnósticos. En: 06.28, 06.35. J: Remitir a Results sin repetir todos los numeradores. |
| 07.11 | Implementation-model trace conformance | 94-99 | These results provide evidence that the abstraction used by the | COMPRESS | 49 / 38 / 11 | LOW | F: Interpretar controles negativos. U: Rechazo dirigido refuerza aceptación de válidas. R: Resultados descriptivos. En: 06.34. J: Conservar aporte del diseño positivo/negativo. |
| 07.12 | Implementation-model trace conformance | 101-105 | The diagnostic agreement is particularly relevant. A generic rejection would | MERGE | 41 / 30 / 11 | LOW | F: Interpretar localización de error. U: Diagnóstico esperado aporta más que fallo genérico. R: Explicación anterior. En: 07.11, 06.34. J: Fusionar con 07.11 preservando distinción. |
| 07.13 | Implementation-model trace conformance | 107-113 | The conformance result nevertheless has a deliberately limited scope. The | COMPRESS | 58 / 37 / 21 | LOW | F: Limitar alcance observable. U: Abstracción elegida, no todas las ejecuciones. R: 10+10 y 30 seeds. En: 06.27, 08.24. J: Mantener límite sin volver a contar catálogo. |
| 07.14 | Implementation-model trace conformance | 115-120 | In particular, this evidence must not be interpreted as a | KEEP | 50 / 50 / 0 | NONE | F: Explicar falta de refinement. U: Relación semántica completa requerida vs trazas observadas. R: Advertencia metodológica. En: 04.14. J: No es redundancia funcional, pues explica por qué. |
| 07.15 | Implementation-model trace conformance | 122-125 | RQ3 therefore contributes an implementation-facing validation layer: the formal abstraction | MERGE | 33 / 18 / 15 | LOW | F: Cerrar papel de RQ3. U: Confrontación con implementación. R: Interpretación de 07.11. En: 07.11. J: Fusionar cierre con argumento principal. |
| 07.16 | Verification cost and scalability | 130-134 | RQ4 shows that the verification campaign is constrained not only | COMPRESS | 48 / 30 / 18 | LOW | F: Interpretar RQ4. U: Recursos condicionan evidencia. R: Resultado crecimiento/H4. En: 06.53-06.54. J: Priorizar significado de coste. |
| 07.17 | Verification cost and scalability | 136-142 | For Alloy, all three ordered bound profiles completed. Median elapsed | COMPRESS | 68 / 43 / 25 | LOW | F: Interpretar Alloy. U: Tres ordinales no identifican forma funcional. R: Medianas y Spearman. En: 06.41-06.43. J: Mantener argumento inferencial, quitar renarración numérica. |
| 07.18 | Verification cost and scalability | 144-150 | The TLC results expose a stronger scalability boundary. The small | COMPRESS | 68 / 42 / 26 | LOW | F: Interpretar TLC-large. U: Censura completa impide mediana/comparación a tres niveles. R: 70 y 1800s. En: 06.45. J: Una mención suficiente del evento y sus consecuencias. |
| 07.19 | Verification cost and scalability | 152-156 | The TLC-large outcome should not be interpreted as an experimental | MERGE | 45 / 30 / 15 | LOW | F: Interpretar timeout legítimo. U: Exclusión ocultaría envelope de verificación. R: Censura ya interpretada. En: 07.18. J: Fusionar sin presentar timeout como éxito. |
| 07.20 | Verification cost and scalability | 158-163 | The enabled-fault profiles provide an additional view of verification cost. | COMPRESS | 53 / 40 / 13 | LOW | F: Interpretar fault profiles. U: Estructura de comportamiento afecta coste. R: Orden y nombres en Tabla6. En: 06.48-06.49. J: Conservar implicación y abreviar recapitulación. |
| 07.21 | Verification cost and scalability | 165-170 | The evidence therefore supports a descriptive conclusion that bounded verification | COMPRESS | 52 / 34 / 18 | LOW | F: Delimitar crecimiento. U: No nonlinear ni ley asintótica. R: Razones en 07.17-07.18. En: 07.17-07.18, 06.54. J: Síntesis de límites sin repetir todos los niveles. |
| 07.22 | Verification cost and scalability | 172-177 | Finally, the cost results must be interpreted within each formal | COMPRESS | 51 / 32 / 19 | LOW | F: Limitar comparación formal. U: Medidas absolutas no establecen superioridad. R: Methodology y Threats. En: 04.40, 08.03. J: Mantener límite local condensado. |
| 07.23 | Cross-layer evidence triangulation | 182-185 | The four research questions are intended to provide complementary evidence | KEEP | 29 / 29 / 0 | NONE | F: Abrir síntesis integrada. U: Capas no son cuatro pruebas independientes. R: Methodology. En: 04.42-04.47. J: Es interpretación global útil, no simple repetición. |
| 07.24 | Cross-layer evidence triangulation | 187-193 | RQ1 examines whether the declared safety and liveness-related properties survive | COMPRESS | 59 / 35 / 24 | LOW | F: Integrar RQ1/RQ2. U: RQ2 cubre insuficiencia de satisfacción válida. R: Interpretación de mutación. En: 07.05-07.06. J: Acortar explicación ya desarrollada en misma sección. |
| 07.25 | Cross-layer evidence triangulation | 195-201 | Conversely, completed bounded formal verification and mutation detection remain statements | COMPRESS | 54 / 33 / 21 | LOW | F: Integrar RQ3. U: Formalismo no garantiza comportamiento Java. R: Interpretación RQ3. En: 07.11, 07.14. J: Mantener transición lógica sin redescribir procedimiento. |
| 07.26 | Cross-layer evidence triangulation | 203-208 | RQ4 addresses a different limitation: whether the verification evidence can | COMPRESS | 50 / 30 / 20 | LOW | F: Integrar RQ4. U: Coste hace explícita factibilidad. R: Interpretación de coste. En: 07.16-07.21. J: Sintetizar consecuencia metodológica. |
| 07.27 | Cross-layer evidence triangulation | 210-217 | Taken together, these layers form an evidence chain: bounded property | MERGE | 20 / 10 / 10 | LOW | F: Resumir cadena. U: Orden de capas. R: Esquema metodológico. En: 04.46. J: Integrar cadena en texto, evitar segundo bloque destacado. |
| 07.28 | Cross-layer evidence triangulation | 219-225 | The value of this chain lies in the different failure | COMPRESS | 65 / 48 / 17 | LOW | F: Delimitar combinación. U: Ninguna capa elimina límites de otra. R: Amenazas por capa. En: 08 y 07.24-07.26. J: Mantener principio central de no acumulación probatoria. |
| 07.29 | Cross-layer evidence triangulation | 227-231 | The combined result is therefore not a general proof of | COMPRESS | 38 / 24 / 14 | LOW | F: Cerrar inferencia global. U: Argumento reproducible, no prueba general. R: Apertura de triangulación. En: 07.23. J: Cierre corto con límite explícito. |
| 07.30 | Cross-layer evidence triangulation | 233-237 | This integration is central to the methodological contribution of the | MERGE | 44 / 20 / 24 | LOW | F: Reafirmar contribución. U: Integración con recursos y límites. R: Contribución y cierre previo. En: 02.14, 07.29. J: Fusionar con 07.29. |

#### 8. Threats to Validity

Archivo: `manuscript/paper1/sections/08-threats-to-validity.tex`. [Fuente canónica](https://github.com/kapumota/dtl-lab/blob/5195d94b2ade1933a726d1d2106cc35a11c37f47/manuscript/paper1/sections/08-threats-to-validity.tex).

| ID | Subsección | Líneas | Primeras palabras | Clasificación | Actual / objetivo / ahorro | Riesgo | Función, contenido y justificación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 08.01 | Construct validity | 7-13 | A first construct-validity threat concerns the relationship between the TLA+ | COMPRESS | 55 / 42 / 13 | LOW | F: Definir amenaza constructo formal. U: Propiedades homónimas no son semánticamente idénticas. R: Modelo. En: 03.18-03.20. J: Mantener amenaza sin extensa presentación. |
| 08.02 | Construct validity | 15-22 | This threat is mitigated by defining the experimental claims at | MERGE | 56 / 40 / 16 | LOW | F: Mitigar constructo formal. U: Obligaciones y target específico por encoding. R: Ejemplo completo en Results. En: 06.18. J: Unir amenaza y mitigación, sin quitar ejemplo esencial. |
| 08.03 | Construct validity | 24-30 | A second threat concerns the use of verification-cost metrics across | COMPRESS | 57 / 42 / 15 | LOW | F: Definir amenaza métrica. U: Unidades de exploración no equivalentes. R: Methodology. En: 04.40. J: Retener causa de no comparabilidad. |
| 08.04 | Construct validity | 32-36 | To reduce this threat, cost trends are interpreted within each | MERGE | 42 / 28 / 14 | LOW | F: Mitigar comparabilidad. U: Análisis dentro de herramienta. R: Regla metodológica. En: 05.44. J: Fusionar con 08.03 conservando prohibición de superioridad. |
| 08.05 | Construct validity | 38-44 | A third construct-validity threat affects the implementation-model conformance experiment. RQ3 | COMPRESS | 62 / 46 / 16 | LOW | F: Definir constructo de conformance. U: Abstracción observable vs estado Java completo. R: Modelo y Methodology. En: 03.17, 04.31. J: Mantener amenaza, no moverla fuera de Threats. |
| 08.06 | Construct validity | 46-50 | This limitation is addressed by freezing the trace representation, scenario | MERGE | 44 / 33 / 11 | LOW | F: Mitigar sesgo de abstracción. U: Freeze de mapa/catálogo y diagnósticos negativos. R: Método replay. En: 04.31-04.35. J: Fusionar con 08.05 preservando mitigación. |
| 08.07 | Construct validity | 52-57 | Finally, mutation sensitivity is an operational proxy for the adequacy | COMPRESS | 47 / 35 / 12 | LOW | F: Definir proxy de adecuación. U: Mutación dirigida no taxonomía exhaustiva. R: Catálogo en diseño. En: 05.14. J: Mantener amenaza de constructo distinta de generalización. |
| 08.08 | Construct validity | 59-62 | Accordingly, the mutation score is interpreted only with respect to | MERGE | 39 / 25 / 14 | LOW | F: Limitar proxy mutacional. U: Score1.0 no completa detección. R: Definición MutationScore. En: 04.29. J: Integrar con 08.07. |
| 08.09 | Internal validity | 67-70 | A first internal-validity threat concerns variation in host load and | COMPRESS | 31 / 24 / 7 | LOW | F: Definir ruido ambiental. U: Carga ajena afecta tiempo/RSS. R: Condición host. En: 05.28. J: Conservar mecanismo de confusión. |
| 08.10 | Internal validity | 72-75 | This threat is mitigated by executing the definitive campaign on | MERGE | 40 / 29 / 11 | LOW | F: Mitigar ruido. U: Linux dedicado y serialización. R: Diseño ambiente/recursos. En: 05.25, 05.28-05.29. J: Unir amenaza y mitigación. |
| 08.11 | Internal validity | 77-81 | A second threat concerns startup and runtime effects that may | COMPRESS | 46 / 35 / 11 | LOW | F: Mitigar arranque/runtime. U: Warmups retenidos pero fuera de análisis. R: Diseño de repeticiones. En: 05.22. J: Mantener causa y medida de mitigación. |
| 08.12 | Internal validity | 83-87 | A third threat concerns nondeterminism in scenario generation and execution | COMPRESS | 43 / 32 / 11 | LOW | F: Mitigar nondeterminismo. U: Seeds/familias congeladas sin adaptación. R: Freeze y seeds. En: 05.01, 05.20. J: Conservar amenaza y control específico. |
| 08.13 | Internal validity | 89-93 | A fourth threat is measurement instrumentation. Elapsed time and maximum | COMPRESS | 34 / 27 / 7 | LOW | F: Definir amenaza instrumental. U: Medición OS y parsers específicos. R: Outcomes registrados. En: 05.32-05.38. J: Mantener posible confusión con resultado científico. |
| 08.14 | Internal validity | 95-99 | To reduce this risk, instrumentation errors are classified separately from | MERGE | 38 / 28 / 10 | LOW | F: Mitigar instrumentación. U: Separación error y retry predefinido. R: Política retry. En: 05.38. J: Fusionar con 08.13. |
| 08.15 | Internal validity | 101-105 | Timeout handling constitutes an additional internal-validity concern. A run that | COMPRESS | 48 / 36 / 12 | LOW | F: Explicar sesgo de timeout. U: Tiempo final desconocido e imputación sesgada. R: Política de censura. En: 05.37. J: Mantener por qué la sustitución por 1800 sesga. |
| 08.16 | Internal validity | 107-111 | The protocol therefore retains timeout and out-of-memory outcomes as censored | MERGE | 44 / 30 / 14 | LOW | F: Mitigar censura. U: Retención sin éxito imputado ni reruns selectivos. R: Política operativa. En: 05.37-05.38. J: Fusionar con 08.15. |
| 08.17 | Internal validity | 113-118 | Finally, the definitive experiment is governed by a frozen protocol | COMPRESS | 49 / 34 / 15 | LOW | F: Mitigar adaptación post hoc. U: Freeze previo reduce selección de diseño. R: Inventario de parámetros. En: 04.02, 05.01. J: Mantener amenaza y abreviar lista de factores. |
| 08.18 | External validity | 123-127 | A first external-validity limitation is that the study evaluates one | COMPRESS | 40 / 30 / 10 | LOW | F: Delimitar población de protocolos. U: Un protocolo y sus representaciones. R: Objeto de estudio. En: 03.01. J: Mantener alcance externo explícito. |
| 08.19 | External validity | 129-134 | In particular, the results should not be generalized directly to | MERGE | 46 / 34 / 12 | LOW | F: Delimitar producción. U: Simulador y diferencias de consenso/red/cripto/adversario. R: Modelo. En: 03.04, 03.24. J: Fusionar con 08.18 conservando exclusiones. |
| 08.20 | External validity | 136-141 | A second limitation concerns the bounded formal configurations. The small, | COMPRESS | 46 / 34 / 12 | LOW | F: Delimitar tamaños. U: Perfiles finitos no despliegues arbitrarios. R: Tabla1. En: 05.08-05.11. J: Remitir a parámetros sin repetirlos todos. |
| 08.21 | External validity | 143-146 | Consequently, completion without a counterexample within the bounded profiles does | MERGE | 33 / 24 / 9 | LOW | F: Limitar extrapolación de bounds. U: Mayor tamaño puede cambiar tractabilidad/counterexamples. R: Amenaza anterior. En: 08.20. J: Conservar ambas consecuencias al fusionar. |
| 08.22 | External validity | 148-153 | A third limitation concerns the mutation catalogue. The experiment contains | COMPRESS | 47 / 34 / 13 | LOW | F: Delimitar catálogo mutacional. U: Posibles defectos e interacciones no incluidos. R: Catálogo. En: 05.14. J: Mantener amenaza externa, no sustituir por construct validity. |
| 08.23 | External validity | 155-157 | The mutation results should therefore be generalized only to the | MERGE | 26 / 18 / 8 | LOW | F: Limitar generalización mutación. U: No bugs/arbitrarios ni adversarios generales. R: Amenaza anterior. En: 08.22. J: Fusionar sin extender clases evaluadas. |
| 08.24 | External validity | 159-164 | A fourth limitation concerns implementation-model trace conformance. The trace experiment | COMPRESS | 48 / 36 / 12 | LOW | F: Delimitar escenarios. U: 10+10 y 30 seeds no exhaustivos. R: Resultados catálogo. En: 06.27, 06.32-06.33. J: Conservar no exhaustividad y referencia a cobertura. |
| 08.25 | External validity | 166-168 | The RQ3 result therefore supports generalization to the evaluated trace | MERGE | 26 / 18 / 8 | LOW | F: Limitar generalización RQ3. U: Solo clases/abstracción/bounds. R: Amenaza anterior. En: 08.24. J: Fusionar sin garantía para todo workload. |
| 08.26 | External validity | 170-175 | Finally, the verification-cost observations depend on the selected formal models, | COMPRESS | 50 / 39 / 11 | LOW | F: Delimitar costes externos. U: Dependencia de modelos/tools/ambiente/recursos. R: Diseño y límites RQ4. En: 05.02, 05.25, 06.54. J: Mantener factores de dependencia. |
| 08.27 | External validity | 177-180 | These limitations are addressed by restricting the claims to the | COMPRESS | 32 / 22 / 10 | LOW | F: Mitigar generalización. U: Restricción de claims y protocolo preservado. R: Mitigaciones por amenaza. En: 08.18-08.26. J: Cierre breve sin repetir inventario completo. |
| 08.28 | Conclusion validity | 185-190 | A first conclusion-validity threat concerns the interpretation of absence of | COMPRESS | 54 / 42 / 12 | LOW | F: Definir amenaza inferencia RQ1. U: Ausencia no prueba estadística/general, censura total. R: Respuesta RQ1. En: 06.12. J: Mantener razonamiento inferencial. |
| 08.29 | Conclusion validity | 192-195 | The RQ1 conclusion is therefore restricted to the completed bounded | MERGE | 29 / 20 / 9 | LOW | F: Restringir conclusión RQ1. U: Solo completadas, no convertir censura en éxito. R: Amenaza anterior. En: 08.28. J: Integrar conclusión con amenaza. |
| 08.30 | Conclusion validity | 197-203 | A second threat concerns the mutation result. Detection of all | KEEP | 72 / 72 / 0 | NONE | F: Delimitar inferencia mutacional. U: Diez mutantes no muestra aleatoria de defectos. R: Score en Results. En: 06.23-06.25. J: Información metodológica crítica, no inferir probabilidad universal. |
| 08.31 | Conclusion validity | 205-211 | A third threat concerns the perfect observed classification rates in | COMPRESS | 61 / 48 / 13 | LOW | F: Delimitar muestreo RQ3. U: Seeds y clases no muestra aleatoria de Java. R: Tamaños en Results. En: 06.27. J: Mantener razón de alcance de tasas perfectas. |
| 08.32 | Conclusion validity | 213-217 | Accordingly, the observed classification proportions and their associated interval estimates | MERGE | 32 / 24 / 8 | LOW | F: Limitar intervalos RQ3. U: IC caracteriza población evaluada, no garantía general. R: Amenaza anterior. En: 08.31. J: Fusionar conservando interpretación de Wilson. |
| 08.33 | Conclusion validity | 219-223 | The principal conclusion-validity limitation affects RQ4. Alloy provides only three | KEEP | 44 / 44 / 0 | NONE | F: Delimitar Spearman. U: Tres niveles/rho1 no identifican curva. R: Discussion. En: 07.17. J: Conservar amenaza inferencial canónica. |
| 08.34 | Conclusion validity | 225-229 | For TLC, only the small and medium profiles provide completed | COMPRESS | 39 / 32 / 7 | LOW | F: Delimitar TLC. U: Dos niveles completos, sin distribución large. R: Resultados coste. En: 06.45-06.46. J: Preservar ambas restricciones. |
| 08.35 | Conclusion validity | 231-235 | Censoring also affects descriptive summaries. Medians computed from completed executions | KEEP | 33 / 33 / 0 | NONE | F: Definir sesgo de completadas. U: Censura selectiva puede subestimar carga. R: Censura operativa. En: 05.37. J: Consecuencia única, no redundante. |
| 08.36 | Conclusion validity | 237-241 | For these reasons, H4 is interpreted as evidence of increasing | COMPRESS | 41 / 30 / 11 | LOW | F: Cerrar inferencia H4. U: Crecimiento sin nonlinear/asintótico. R: H4 en Results. En: 06.54. J: Mantener dictamen exacto. |
| 08.37 | Conclusion validity | 243-247 | More generally, the study emphasizes effect direction, bounded property outcomes, | COMPRESS | 42 / 25 / 17 | LOW | F: Delimitar fuerza global. U: No claims de significación no sustentados. R: Resumen de evidencia. En: 07.29. J: Cierre compacto sin otra lista de resultados. |

#### 9. Reproducibility and Artifact

Archivo: `manuscript/paper1/sections/09-reproducibility-artifact.tex`. [Fuente canónica](https://github.com/kapumota/dtl-lab/blob/5195d94b2ade1933a726d1d2106cc35a11c37f47/manuscript/paper1/sections/09-reproducibility-artifact.tex).

| ID | Subsección | Líneas | Primeras palabras | Clasificación | Actual / objetivo / ahorro | Riesgo | Función, contenido y justificación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 09.01 | Artifact scope | 7-11 | The reproducibility artifact is designed to preserve the complete evidence | COMPRESS | 42 / 25 / 17 | LOW | F: Definir artifact. U: Cadena completa y no solo tablas/código. R: Resumen de capas. En: 09.02-09.06. J: Apertura breve con objeto claro. |
| 09.02 | Artifact scope | 13-18 | The first artifact layer contains the scientific source material. This | COMPRESS | 52 / 35 / 17 | LOW | F: Inventariar fuentes. U: Java, TLA+ y Alloy con configs/comandos. R: Modelo y toolchain. En: 03, 05.02. J: Conservar tipos de fuentes sin redescribir estudio. |
| 09.03 | Artifact scope | 20-26 | The second layer contains the frozen experimental definition. It records | COMPRESS | 57 / 35 / 22 | LOW | F: Inventariar protocolo. U: Definición congelada separada de outcomes. R: Diseño completo. En: 05.01-05.45. J: Remitir a diseño conservando función de definición. |
| 09.04 | Artifact scope | 28-33 | The third layer contains the preserved experimental evidence. Raw task | COMPRESS | 55 / 35 / 20 | LOW | F: Inventariar evidencia. U: Raw, derivados, tablas y figuras preservados. R: Workflow de regeneración. En: 09.13, 09.26. J: Conservar cadena de datos sin duplicar su desarrollo. |
| 09.05 | Artifact scope | 35-40 | The fourth layer contains the reproduction machinery. It provides scripts | COMPRESS | 43 / 28 / 15 | LOW | F: Inventariar machinery. U: Scripts de validación, smoke, regeneración, comparación. R: Workflow detallado. En: 09.10-09.14. J: Inventario breve, no manual. |
| 09.06 | Artifact scope | 42-45 | The final layer contains integrity and traceability evidence. Manifests, content | COMPRESS | 30 / 22 / 8 | LOW | F: Inventariar integridad. U: Manifests, hashes, versiones e informes. R: Integrity and traceability. En: 09.25-09.29. J: Conservar tipos y dejar explicación canónica después. |
| 09.07 | Artifact scope | 47-52 | These layers serve different reproducibility purposes. The scientific source defines | DELETE_REDUNDANT | 46 / 0 / 46 | NONE | F: Recapitular cinco capas. U: Ninguna información adicional. R: Inventario inmediatamente anterior. En: 09.02-09.06. J: Eliminar segunda explicación de función de cada capa. |
| 09.08 | Artifact scope | 54-59 | The artifact is therefore intended to support inspection, partial re-execution, | COMPRESS | 49 / 28 / 21 | LOW | F: Delimitar capacidades. U: Inspección, re-ejecución parcial y regeneración. R: Límite reproducción. En: 09.22-09.24. J: Síntesis local con remisión, no desarrollo completo. |
| 09.09 | Reproduction workflow | 64-66 | The reproduction workflow separates structural validation, selective re-execution, analysis regeneration, | COMPRESS | 16 / 10 / 6 | LOW | F: Introducir workflow. U: Etapas diferenciadas. R: Descripción siguiente. En: 09.10-09.14. J: Apertura mínima. |
| 09.10 | Reproduction workflow | 68-71 | The first stage validates the expected artifact structure and checks | COMPRESS | 33 / 20 / 13 | LOW | F: Explicar validación estructural. U: Comprobar insumos antes de ejecutar. R: Inventario fuentes. En: 09.02-09.06. J: Conservar orden y reducir lista duplicada. |
| 09.11 | Reproduction workflow | 73-76 | The second stage prepares an isolated reproduction workspace from the | COMPRESS | 32 / 22 / 10 | LOW | F: Explicar aislamiento. U: Registrar revisión y no tocar referencia. R: Evidencia de clon separado. En: 09.18, 09.24. J: Mantener aislamiento sin detalles de directorios. |
| 09.12 | Reproduction workflow | 78-83 | The third stage executes a representative smoke suite. The suite | KEEP | 52 / 52 / 0 | NONE | F: Definir smoke suite. U: Válido/mutado TLC, Alloy y traza válida/corrupta. R: Resultado reproducción. En: 09.22. J: Mantener las seis clases ejecutadas. |
| 09.13 | Reproduction workflow | 85-88 | The fourth stage regenerates the paper-level analysis from the preserved | KEEP | 32 / 32 / 0 | NONE | F: Definir regeneración. U: Pipeline común desde raw preservado a análisis completo. R: Inventario de evidencia. En: 09.04. J: Canónico para entender qué se regeneró. |
| 09.14 | Reproduction workflow | 90-93 | The fifth stage compares the regenerated outputs against the preserved | COMPRESS | 30 / 22 / 8 | LOW | F: Definir comparación. U: Regenerado vs. referencia mediante hashes/gates. R: Integridad. En: 09.27. J: Conservar acción y objeto comparado. |
| 09.15 | Reproduction workflow | 95-99 | A reproduction verdict is produced only after these stages have | COMPRESS | 36 / 22 / 14 | LOW | F: Definir veredicto. U: Requiere todas las etapas, no un comando exitoso. R: Etapas previas. En: 09.10-09.14. J: Conservar criterio de cierre en frase corta. |
| 09.16 | Reproduction workflow | 101-105 | This workflow intentionally distinguishes two reproducibility goals. Representative re-execution checks | COMPRESS | 37 / 26 / 11 | LOW | F: Distinguir objetivos. U: Operatividad de smoke vsregeneración de análisis. R: Descripción de etapas. En: 09.12-09.13. J: Mantener distinción sin redescribir mecanismos. |
| 09.17 | Reproduction workflow | 107-111 | Neither operation should be interpreted as a new independent measurement | COMPRESS | 42 / 27 / 15 | LOW | F: Limitar independencia empírica. U: Raw sigue fuente, no nuevas medidas completas. R: Resultado no-full-rerun. En: 09.22-09.23. J: Conservar advertencia local breve. |
| 09.18 | Independent reproduction evidence | 116-118 | The artifact was evaluated through an independent reproduction attempt performed | MERGE | 29 / 18 / 11 | LOW | F: Describir separación histórica. U: Clon/workspace distinto del productor. R: Entorno detallado. En: 09.24. J: Fusionar con 09.24 conservando las tres separaciones. |
| 09.19 | Independent reproduction evidence | 120-128 | The reproduction used source revision 6cd88c377afd23fee4998882f91142d71e7d963e. The packaged artifact used | MOVE_TO_ARTIFACT | 38 / 24 / 14 | LOW | F: Identificar intento histórico. U: Commit 6cd88... y digest d464. R: Rutas absolutas operativas. En: 09.11 y registro histórico citado por 09.19. J: Retener identidad del intento, trasladar solo rutas absolutas a provenance del artifact. |
| 09.20 | Independent reproduction evidence | 130-134 | The reproduction workflow completed all ten predefined validation gates. These | KEEP | 35 / 35 / 0 | NONE | F: Reportar gates. U: 10/10, categorías y cero incidentes. R: Workflow. En: 09.10-09.15. J: Conservar significado de diez gates, sin inventar lista de diez nombres. |
| 09.21 | Independent reproduction evidence | 136-140 | The regenerated analysis products were compared with the preserved reference | COMPRESS | 41 / 30 / 11 | LOW | F: Reportar hashes. U: 32/32 outputs coincidentes. R: 10/10 y cero incidentes ya reportados. En: 09.20. J: Conservar32/32, quitar repetición de otros contadores. |
| 09.22 | Independent reproduction evidence | 142-146 | The executable part of the reproduction consisted of the representative | KEEP | 45 / 45 / 0 | NONE | F: Delimitar ejecución histórica. U: Smoke en lugar de full rerun y regeneración completa. R: Workflow general. En: 09.12-09.13. J: Canónico para alcance del intento exitoso. |
| 09.23 | Independent reproduction evidence | 148-154 | This distinction is essential for interpreting the reproduction result. The | COMPRESS | 53 / 30 / 23 | LOW | F: Interpretar reproducción. U: Operatividad y reconstrucción, sin nuevas medidas completas. R: Descripción anterior. En: 09.22. J: Interpretación corta, sin repetir workflow. |
| 09.24 | Independent reproduction evidence | 156-162 | The reproduction also provides process separation, but not full hardware | COMPRESS | 66 / 48 / 18 | LOW | F: Delimitar independencia. U: Usuario, clon/workspace, mismo host Linux nativo, no hardware independence. R: Threats/repro boundaries. En: 09.39. J: Conservar todas las condiciones y segunda máquina como futuro. |
| 09.25 | Integrity and traceability | 167-171 | The artifact preserves traceability across the main stages of the | COMPRESS | 41 / 28 / 13 | LOW | F: Definir vínculo de versiones. U: Revision enlaza fuentes y scripts, protocolo identifica campaña. R: Inventario integridad. En: 09.06. J: Mantener relación entre identidades. |
| 09.26 | Integrity and traceability | 173-177 | The preserved raw results form the primary empirical record of | COMPRESS | 37 / 25 / 12 | LOW | F: Definir fuente empírica. U: Raw es registro primario, no números manuales. R: Regeneración. En: 09.13. J: Conservar fuente de autoridad y acortar derivados enumerados. |
| 09.27 | Integrity and traceability | 179-182 | Integrity manifests provide a second traceability layer. Cryptographic content hashes | COMPRESS | 36 / 24 / 12 | LOW | F: Definir uso de manifiesto. U: Rehash y comparación determinista. R: Etapa de comparación. En: 09.14. J: Conservar mecanismo en explicación canónica. |
| 09.28 | Integrity and traceability | 184-187 | A matching hash establishes content identity for the corresponding artifact. | KEEP | 36 / 36 / 0 | NONE | F: Limitar hash matching. U: Identidad de contenido no corrección científica. R: Advertencias repetidas. En: 09.32, 09.41. J: Conservar formulación canónica. |
| 09.29 | Integrity and traceability | 189-193 | Conversely, a hash mismatch is treated as an observable reproduction | COMPRESS | 36 / 25 / 11 | LOW | F: Definir tratamiento mismatch. U: Investigar y registrar gate/incident. R: Reportes generales. En: 09.06. J: No perder conducta ante discrepancia. |
| 09.30 | Integrity and traceability | 195-204 | The resulting traceability chain can be summarized as: source revision | KEEP | 23 / 23 / 0 | NONE | F: Resumir provenance. U: Revisión/protocolo/raw/derivados/manifest/comparación. R: Explicación precedente. En: 09.25-09.29. J: Cadena compacta útil para inspección independiente. |
| 09.31 | Integrity and traceability | 206-209 | This chain supports bidirectional inspection. A value reported in a | COMPRESS | 37 / 24 / 13 | LOW | F: Explicar inspección bidireccional. U: Tabla hacia raw y regenerado hacia manifest. R: Cadena. En: 09.30. J: Conservar utilidad con una oración. |
| 09.32 | Integrity and traceability | 211-215 | The integrity layer is intentionally separated from experimental interpretation. Hash | DELETE_REDUNDANT | 38 / 0 / 38 | NONE | F: Reiterar límite de integridad. U: Ninguna distinción nueva. R: Hash no corrección. En: 09.28. J: Eliminar duplicado conservando versión canónica cercana. |
| 09.33 | Reproducibility boundaries | 220-221 | The artifact supports several forms of reproducibility, but each has | DELETE_REDUNDANT | 13 / 0 / 13 | NONE | F: Introducir límites. U: Ninguna, el título ya lo dice. R: Encabezado Reproducibility boundaries. En: 09.38-09.40. J: Eliminar frase vacía. |
| 09.34 | Reproducibility boundaries | 223-225 | First, the scientific sources, frozen protocol, raw observations, derived outputs, | DELETE_REDUNDANT | 28 / 0 / 28 | NONE | F: Repetir inventario auditable. U: Ninguna información nueva. R: Inventario y cadena. En: 09.02-09.06, 09.30. J: Eliminar segunda enumeración de insumos. |
| 09.35 | Reproducibility boundaries | 227-230 | Second, the representative smoke suite permits partial re-execution of the | DELETE_REDUNDANT | 35 / 0 / 35 | NONE | F: Repetir smoke scope. U: Ninguna información nueva. R: Smoke suite canónica. En: 09.12. J: Eliminar recapitulación manteniendo seis clases ejecutadas en 09.12. |
| 09.36 | Reproducibility boundaries | 232-235 | Third, the complete paper-level analysis can be regenerated from the | DELETE_REDUNDANT | 33 / 0 / 33 | NONE | F: Repetir regeneración. U: Ninguna información nueva. R: Workflow y resultado histórico. En: 09.13, 09.22. J: Eliminar recapitulación, conservar origen raw. |
| 09.37 | Reproducibility boundaries | 237-239 | Fourth, content hashes and reproduction reports provide integrity and process | DELETE_REDUNDANT | 24 / 0 / 24 | NONE | F: Repetir integridad de proceso. U: Ninguna información nueva. R: Comparación y reportes. En: 09.27-09.29. J: Eliminar duplicado de mecanismos. |
| 09.38 | Reproducibility boundaries | 241-244 | These capabilities do not imply full experimental replication. The independent | COMPRESS | 34 / 22 / 12 | LOW | F: Recordar no full replication. U: No full rerun, raw permanece fuente. R: Resultado histórico. En: 09.22. J: Conservar límite breve en subsección de fronteras. |
| 09.39 | Reproducibility boundaries | 246-250 | The evaluation also does not establish hardware-independent reproducibility. The successful | COMPRESS | 45 / 27 / 18 | LOW | F: Recordar no hardware independence. U: Mismo host y segunda máquina como mayor validación. R: Condiciones del intento. En: 09.24. J: Reducir reiteración, conservar límite explícito. |
| 09.40 | Reproducibility boundaries | 252-255 | Similarly, the artifact does not claim universal reproducibility across arbitrary | KEEP | 33 / 33 / 0 | NONE | F: Limitar portabilidad. U: OS, CPU, tools, JVM y recursos pueden alterar outputs/coste. R: Mismo host no cubre estos factores. En: 09.24. J: Limitación adicional única, debe permanecer. |
| 09.41 | Reproducibility boundaries | 257-262 | Finally, deterministic integrity checks should not be confused with scientific | COMPRESS | 45 / 20 / 25 | LOW | F: Recordar hash no validez. U: Integridad distinta de conclusión científica. R: Versión canónica. En: 09.28. J: Recordatorio corto con referencia. |
| 09.42 | Reproducibility boundaries | 264-268 | The artifact should therefore be interpreted as supporting reproducible inspection, | DELETE_REDUNDANT | 34 / 0 / 34 | NONE | F: Recapitular capacidades y límites. U: Ninguna información nueva. R: Inventario, workflow y límites. En: 09.08, 09.38-09.41. J: Eliminar cierre que repite íntegramente la subsección. |

#### 10. Conclusions

Archivo: `manuscript/paper1/sections/10-conclusions.tex`. [Fuente canónica](https://github.com/kapumota/dtl-lab/blob/5195d94b2ade1933a726d1d2106cc35a11c37f47/manuscript/paper1/sections/10-conclusions.tex).

| ID | Subsección | Líneas | Primeras palabras | Clasificación | Actual / objetivo / ahorro | Riesgo | Función, contenido y justificación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 10.01 | Sin subsección | 4 | This paper evaluated an executable cross-shard commit protocol through a | COMPRESS | 58 / 38 / 20 | LOW | F: Sintetizar contribución. U: Integración de capas y freeze. R: Introduction y Discussion. En: 01.04, 07.29. J: Mantener contribución con menos enumeración. |
| 10.02 | Sin subsección | 6 | For RQ1, the valid TLA+ and Alloy models produced no | COMPRESS | 97 / 73 / 24 | LOW | F: Sintetizar RQ1/RQ2. U: 420/350/70, 0 violaciones completadas, 10/10, score1.0 y límites. R: Resultados completos. En: 06.04, 06.12, 06.23-06.25. J: Conservar datos esenciales y scope en formulación compacta. |
| 10.03 | Sin subsección | 8 | For RQ3, the 600 evaluated executions supported the expected bounded | COMPRESS | 114 / 85 / 29 | LOW | F: Sintetizar RQ3/RQ4. U: 600, aceptación/rechazo, diagnósticos, coste creciente y límites. R: Results/Discussion. En: 06.28, 06.37, 06.54-06.55. J: Mantener ambas RQ y no refinement/no leyes/no superioridad. |
| 10.04 | Sin subsección | 10 | The combined evidence should therefore be interpreted as complementary rather | MERGE | 73 / 22 / 51 | LOW | F: Cerrar interpretación integrada. U: Complementariedad no prueba acumulativa. R: Discussion y freeze. En: 07.28, 04.02. J: Fusionar con 10.01, sin reenumerar parámetros congelados. |
| 10.05 | Sin subsección | 12 | The analytical artifact was independently exercised from a separate clone | COMPRESS | 69 / 60 / 9 | LOW | F: Sintetizar reproducción. U: 10/10, 32/32, separaciones, mismo host, no full rerun/no hardware. R: Resultado histórico. En: 09.20-09.24. J: Mantener todos los calificadores esenciales. |
| 10.06 | Sin subsección | 14 | Overall, the study shows that a cross-shard protocol can be | COMPRESS | 81 / 47 / 34 | LOW | F: Cerrar y definir futuro. U: Ampliar bounds, mutantes, trazas y segunda máquina. R: Contribución repetida. En: 10.01, 07.30. J: Eliminar segunda síntesis y conservar futuro mínimo. |

### 5. Mapa de redundancias cruzadas

Las apariciones se cuentan por unidad que expresa el concepto o una variante semántica relevante, no por cada coincidencia de una palabra. Se indican los ID para permitir revisión manual del conteo. Los números son aproximados porque un pasaje puede describir el límite mediante una consecuencia, sin repetir su nombre literal. Una aparición en definición, otra en interpretación y otra en amenaza pueden ser necesarias las tres.

Los ahorros por concepto **no son aditivos**: una misma oración puede cubrir boundedness, censura y no unbounded proof. La única suma de ahorro sin duplicaciones es la de la matriz, 2 902 palabras. Los intervalos de esta sección orientan la priorización, no crean un segundo presupuesto.

#### Bounded verification is not unbounded proof

Apariciones aproximadas: **8**. Secciones: 1, 3, 4, 6, 7, 8, 10.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 01.07, 03.25, 04.08, 04.53, 06.12, 07.03, 08.28, 10.02 |
| Texto canónico que debe conservarse | 03.25 y 04.08: significado del resultado acotado. 06.12: veredicto H1. 08.28: amenaza inferencial. |
| Apariciones comprimibles | 01.07 03.25 04.08 04.53 07.03 08.28 10.02 |
| Apariciones fusionables | Ninguna eliminación del límite local |
| Apariciones eliminables | Ninguna |
| Ahorro orientativo no aditivo | 60-95 palabras |

#### Mutation score does not establish completeness

Apariciones aproximadas: **11**. Secciones: 4, 6, 7, 8, 10.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 04.29, 04.54, 06.25, 07.08, 07.09, 08.07, 08.08, 08.22, 08.23, 08.30, 10.02 |
| Texto canónico que debe conservarse | 04.29: límite de definición. 08.30: mutantes no aleatorios. 06.25: alcance del resultado. |
| Apariciones comprimibles | 04.54 06.25 07.08 08.07 08.22 10.02 |
| Apariciones fusionables | 07.09 con 07.05-07.06, 08.08 con 08.07, 08.23 con 08.22 |
| Apariciones eliminables | Ninguna |
| Ahorro orientativo no aditivo | 70-110 palabras |

#### Trace conformance is not refinement

Apariciones aproximadas: **8**. Secciones: 1, 3, 4, 6, 7, 10.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 01.06, 03.26, 04.14, 04.36, 04.55, 06.37, 07.14, 10.03 |
| Texto canónico que debe conservarse | 04.14: frontera del método. 07.14: explicación semántica. 06.37: frontera del resultado. |
| Apariciones comprimibles | 01.06 03.26 04.36 04.55 10.03 |
| Apariciones fusionables | Recordatorios breves, no fusión de Methodology con Threats |
| Apariciones eliminables | Ninguna |
| Ahorro orientativo no aditivo | 35-65 palabras |

#### Trace conformance is not behavioral equivalence

Apariciones aproximadas: **8**. Secciones: 1, 3, 4, 6, 7, 8, 10.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 01.06, 03.26, 04.14, 04.55, 06.37, 07.14, 08.05, 10.03 |
| Texto canónico que debe conservarse | 04.14 y 07.14. 08.05 conserva amenaza de observar solo una abstracción. |
| Apariciones comprimibles | 01.06 03.26 04.55 08.05 10.03 |
| Apariciones fusionables | 08.06 se une a 08.05 con su mitigación |
| Apariciones eliminables | Ninguna |
| Ahorro orientativo no aditivo | 25-45 palabras |

#### TLC and Alloy are not directly comparable

Apariciones aproximadas: **14**. Secciones: 1, 3, 4, 5, 6, 7, 8, 10.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 01.03, 01.06, 03.20, 04.40, 04.56, 05.34, 05.44, 06.22, 06.55, 07.04, 07.22, 08.03, 08.04, 10.03 |
| Texto canónico que debe conservarse | 03.18-03.20: semánticas. 04.40: regla del método. 08.03-08.04: amenaza y mitigación. |
| Apariciones comprimibles | 01.03 01.06 03.20 04.40 04.56 05.44 06.22 06.55 07.04 07.22 08.03 10.03 |
| Apariciones fusionables | 08.04 con 08.03 |
| Apariciones eliminables | Ninguna |
| Ahorro orientativo no aditivo | 100-160 palabras |

#### TLC-large timeout censoring

Apariciones aproximadas: **20**. Secciones: 1, 6, 7, 8, 10.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 01.07, 06.06, 06.08, 06.09, 06.11, 06.41, 06.45, 06.46, 06.52, 07.02, 07.03, 07.18, 07.19, 07.26, 08.16, 08.28, 08.34, 08.36, 10.02, 10.03 |
| Texto canónico que debe conservarse | 06.06 y 06.41: datos. 06.11 y 06.45: significado. 05.37: regla. 08.35: sesgo de completadas. |
| Apariciones comprimibles | 01.07 06.08 06.46 06.52 07.02 07.03 07.18 07.26 08.28 08.34 08.36 10.02 10.03 |
| Apariciones fusionables | 07.19 con 07.18, 08.16 con 08.15 |
| Apariciones eliminables | Ninguna |
| Ahorro orientativo no aditivo | 100-170 palabras |

#### H4 does not establish nonlinear growth

Apariciones aproximadas: **9**. Secciones: 1, 5, 6, 7, 8, 10.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 01.07, 05.45, 06.53, 06.54, 07.16, 07.21, 08.33, 08.36, 10.03 |
| Texto canónico que debe conservarse | 04.16 conserva hipótesis original positiva. 06.54 conserva no confirmación. 08.33 justifica insuficiencia. |
| Apariciones comprimibles | 01.07 05.45 07.16 07.21 08.36 10.03 |
| Apariciones fusionables | 06.53 con 06.54 |
| Apariciones eliminables | Ninguna |
| Ahorro orientativo no aditivo | 45-80 palabras |

#### H4 does not establish asymptotic growth

Apariciones aproximadas: **6**. Secciones: 1, 5, 6, 7, 8, 10.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 01.06, 05.45, 06.54, 07.21, 08.36, 10.03 |
| Texto canónico que debe conservarse | 06.54 y 08.36: ausencia de inferencia asintótica. |
| Apariciones comprimibles | 01.06 05.45 07.21 08.36 10.03 |
| Apariciones fusionables | Con límites nonlinear en los mismos párrafos |
| Apariciones eliminables | Ninguna |
| Ahorro orientativo no aditivo | 20-40 palabras |

#### Same-host reproduction / no hardware independence

Apariciones aproximadas: **5**. Secciones: 1, 9, 10.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 01.08, 09.24, 09.39, 09.42, 10.05 |
| Texto canónico que debe conservarse | 09.24: usuario, clon, workspace separados, mismo host Linux nativo. |
| Apariciones comprimibles | 01.08 09.24 09.39 10.05 |
| Apariciones fusionables | 09.18 con 09.24 |
| Apariciones eliminables | 09.42 |
| Ahorro orientativo no aditivo | 45-80 palabras |

#### Not a full rerun of 1272 tasks

Apariciones aproximadas: **8**. Secciones: 1, 9, 10.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 01.08, 09.08, 09.12, 09.17, 09.22, 09.23, 09.38, 10.05 |
| Texto canónico que debe conservarse | 09.22: smoke ejecutado y análisis completo regenerado desde raw. 09.12 define casos smoke. |
| Apariciones comprimibles | 01.08 09.08 09.17 09.23 09.38 10.05 |
| Apariciones fusionables | Advertencias locales compactas |
| Apariciones eliminables | Ninguna supresión de 09.22 |
| Ahorro orientativo no aditivo | 45-80 palabras |

#### Hash equality does not prove scientific correctness

Apariciones aproximadas: **5**. Secciones: 1, 9, 10.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 01.08, 09.28, 09.32, 09.41, 10.05 |
| Texto canónico que debe conservarse | 09.28: identidad de contenido distinta de corrección científica. |
| Apariciones comprimibles | 01.08 09.41 10.05 |
| Apariciones fusionables | Ninguna necesaria |
| Apariciones eliminables | 09.32 |
| Ahorro orientativo no aditivo | 55-75 palabras |

#### RQ1-RQ4 are complementary evidence layers

Apariciones aproximadas: **30**. Secciones: 1, 2, 4, 7, 10.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 01.02, 01.04, 01.06, 02.01, 02.11, 02.13, 02.14, 04.01, 04.03, 04.04, 04.18, 04.41, 04.42, 04.43, 04.44, 04.45, 04.46, 04.47, 07.09, 07.23, 07.24, 07.25, 07.26, 07.27, 07.28, 07.29, 07.30, 10.01, 10.04, 10.06 |
| Texto canónico que debe conservarse | 04.19-04.40: definiciones/unidades. 04.42-04.45: relación lógica. 07.28: ninguna capa borra límites de otra. |
| Apariciones comprimibles | 01.02 01.04 01.06 02.01 02.11 02.14 04.01 04.18 04.42 04.44 04.46 07.24 07.25 07.26 07.28 07.29 10.01 10.06 |
| Apariciones fusionables | 04.03-04.04, 04.41, 04.43, 04.45, 04.47, 07.09, 07.27, 07.30, 10.04 según matriz |
| Apariciones eliminables | No eliminar relación lógica |
| Ahorro orientativo no aditivo | 280-430 palabras |

#### Frozen protocol prevents post hoc adaptation

Apariciones aproximadas: **7**. Secciones: 4, 5, 8, 10.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 04.02, 05.01, 05.03, 08.06, 08.12, 08.17, 10.04 |
| Texto canónico que debe conservarse | 04.02: momento/regla de versionado. 05.01: identificador. 08.17: amenaza interna. |
| Apariciones comprimibles | 05.01 05.03 08.12 08.17 |
| Apariciones fusionables | 08.06 con 08.05, 10.04 con 10.01 |
| Apariciones eliminables | Ninguna eliminación de 04.02 |
| Ahorro orientativo no aditivo | 45-75 palabras |

#### Artifact supports inspection and regeneration

Apariciones aproximadas: **22**. Secciones: 9.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 09.05, 09.06, 09.08, 09.09, 09.11, 09.13, 09.14, 09.15, 09.16, 09.17, 09.19, 09.20, 09.21, 09.22, 09.23, 09.27, 09.31, 09.34, 09.36, 09.38, 09.41, 09.42 |
| Texto canónico que debe conservarse | 09.10-09.14: qué se hace. 09.20-09.22: qué ocurrió. 09.31: inspección bidireccional. |
| Apariciones comprimibles | 09.05 09.06 09.08 09.09 09.11 09.14 09.15 09.16 09.17 09.21 09.23 09.27 09.31 09.38 09.41 |
| Apariciones fusionables | 09.18 con 09.24 |
| Apariciones eliminables | 09.34, 09.36, 09.42, además inventario duplicado09.07 |
| Ahorro orientativo no aditivo | 160-250 palabras |

#### Preserved raw observations are the empirical source

Apariciones aproximadas: **12**. Secciones: 9.

| Campo | Evaluación |
| --- | --- |
| Ubicaciones | 09.04, 09.05, 09.08, 09.10, 09.13, 09.17, 09.22, 09.23, 09.26, 09.30, 09.36, 09.38 |
| Texto canónico que debe conservarse | 09.13: entrada del pipeline. 09.26: autoridad empírica. 09.22: alcance histórico. |
| Apariciones comprimibles | 09.04 09.05 09.08 09.10 09.17 09.23 09.26 09.38 |
| Apariciones fusionables | Ninguna fusión de raw y resultados regenerados |
| Apariciones eliminables | 09.36 |
| Ahorro orientativo no aditivo | 70-110 palabras |


La repetición más amplia es la relación de complementariedad entre las capas, con aproximadamente treinta unidades relacionadas. No todas son redundantes. Las definiciones de las unidades experimentales, la justificación de triangulación y el argumento de que ninguna capa subsana los límites de otra cumplen funciones diferentes. Lo reducible es volver a enumerar, en igual extensión y con igual función, qué hace RQ1, luego RQ2, luego RQ3 y luego RQ4.

### 6. Elementos intocables

#### Definiciones, unidades y límites que deben permanecer

En esta auditoría «intocable» significa contenido que no puede desaparecer ni cambiar de significado. No obliga a conservar toda frase exactamente igual, salvo las hipótesis preregistradas y los valores que deben permanecer literalmente fieles.

- **Modelo:** 03.04, 03.05-03.14, 03.16-03.18 y las siete entradas de 03.21. Preservar supuestos, estados terminales, restricción del timeout, snapshot, rollback sin transición inversa sintética, puntos de fallo, proyección y diferencias entre representaciones.
- **Scope:** en 03.24 deben permanecer los eventos proyectados como stuttering, la autenticidad abstracta, el consenso fuera de alcance, tiempo lógico y red acotada. Puede abreviarse su repetición, no eliminarse el supuesto.
- **Hipótesis y unidades:** 04.06-04.16, 04.20, 04.25, 04.27-04.29, 04.31-04.35 y 04.38-04.39. Preservar la unidad mutante, el target de detección y los campos del diagnóstico. H4 mantiene la forma no lineal preregistrada.
- **Diseño concreto:** toolchain en 05.02, familias en 05.04, expansión en 05.05, Tabla 1 y aclaración 05.09, perfil medium para mutantes, seeds 2026001-2026030, 2 warmups y 10 measured, serialización, recursos 1800 s y 12288 MiB, un worker TLC, SAT4J, reglas de outcomes y censura, bootstrap de 10 000 remuestreos y Wilson 95 %.
- **Inferencia:** 06.12, 06.20, 06.25, 06.31, 06.34, 06.37, 06.39, 06.43-06.45, 06.50 y 06.54-06.55. Deben sobrevivir los denominadores, IC, diagnóstico, ejecuciones compartidas, estados, ratios, censura y fronteras de claims.
- **Interpretación y amenazas:** 07.14, 07.28, 08.30-08.35, además de todas las amenazas y mitigaciones identificadas en la matriz. Una condensación de Threats no puede dejar únicamente una lista de palabras como boundedness, censoring y generalization.
- **Reproducción:** 09.12-09.13, 09.20-09.24, 09.26, 09.28 y 09.40. Las cifras y condiciones pueden expresarse con menos palabras, pero nunca salir por completo del cuerpo principal.

#### Datos inmutables

| Objeto | Contenido que debe conservarse |
| --- | --- |
| Campaña | 1272 scheduled, 1160 measured, 112 warmups, 1188 completed, 84 timeout, 0 OOM, 0 tool errors. |
| RQ1 | 420 measured, 350 completed, 70 TLC-large censored, 0 observed violations entre configuraciones completadas. |
| RQ2 | 10/10 predefined scientific mutants, score 1.0. Cinco por representación, cien measured runs no son cien mutantes independientes. |
| RQ3 | 600 ejecuciones, 300 válidas aceptadas, 300 corruptas rechazadas, diagnósticos esperados coincidentes. Veinte casos, treinta seeds por caso. |
| RQ3, intervalo | Wilson 95 % por caso 30/30, aproximadamente [0.8865, 1.0], sin extrapolación probabilística universal. |
| RQ4 | Caracterización del coste. 460 medidas incluyen 420 compartidas con RQ1 y 40 adicionales de fault profiles. No ley no lineal/asintótica ni superioridad absoluta. |
| Reproducción | 10/10 predefined gates, 32/32 expected hashes, usuario, clon y workspace separados, mismo host físico Linux nativo. No full rerun de 1272, no independencia de hardware. |

Los totales de campaña que no se desarrollan explícitamente en la prosa actual se toman como invariantes declarados por el encargo. Esta auditoría no afirma haber vuelto a verificar los 1272 registros raw. La partición 420 + 100 + 600 + 40 = 1160 es coherente con las unidades descritas. La futura condensación no debe introducir un doble conteo sumando RQ1 y RQ4 como poblaciones disjuntas.

#### Tablas, figuras y ecuaciones

El cuerpo contiene **seis tablas y seis ecuaciones display**. No se encuentran figuras gráficas insertadas en las diez secciones. Las figuras de análisis mencionadas al describir el artifact son productos del artifact, no figuras presentes que puedan eliminarse del manuscrito.

| Elemento | Localización | Clasificación | Decisión y riesgo |
| --- | --- | --- | --- |
| Tabla 1, bound profiles | 05.08, líneas 57-79, PDF p.20 | KEEP | Mantener todas las columnas, valores, encabezados y configuración visual auditada. Riesgo HIGH si se confunden parámetros y scopes. |
| Tabla 2, RQ1 | 06.06, líneas 29-47, PDF p.25 | KEEP | Mantener measured, completed, no counterexample y timeout, incluido TLC-large con cero completed. |
| Tabla 3, mutantes | 06.17, líneas 106-167, PDF p.27 | KEEP | Mantener diez mutantes, herramienta, target específico y 10/10 por fila. |
| Tabla 4, conformance | 06.30, líneas 232-250, PDF p.28 | KEEP | Mantener dos poblaciones y que matching diagnostics corresponde a negativos, sin convertir el guion de válidas en cero fallos. |
| Tabla 5, coste por perfil | 06.41, líneas 311-335, PDF p.29 | KEEP | Mantener medianas, RSS en KiB, denominadores, timeouts y ausencia de medianas de completadas para TLC-large. |
| Tabla 6, fault profiles | 06.48, líneas 372-390, PDF p.30 | KEEP | Mantener cuatro perfiles medium, diez runs cada uno y todos los tiempos/RSS. |
| Ecuación 1, camino nominal | 03.06, líneas 32-44 | KEEP | La relación ordenada entre estados aporta información que el inventario no expresa por sí solo. |
| Ecuación 2, definición MutationScore | 04.27, líneas 115-123, más definición 04.28 | KEEP | Preservar numerador y denominador por mutante, no por repetición. |
| Ecuación 3, mapa RQ1-RQ4 | 04.46, líneas 196-210 | MERGE_WITH_TEXT | Integrar correspondencias en la triangulación. No es una igualdad ni una propiedad formal que requiera bloque matemático. |
| Ecuación 4, válidas 10×30=300 | 05.17, líneas 127-132 | MERGE_WITH_TEXT | Conservar producto y población en prosa, junto al producto de negativas. |
| Ecuación 5, negativas 10×30=300 | 05.18, líneas 134-138 y continuación 05.19 | MERGE_WITH_TEXT | Mismo criterio, sin perder que las poblaciones son distintas. |
| Ecuación 6, score 10/10=1.0 | 06.15, líneas 91-102 | MERGE_WITH_TEXT | Puede expresarse dentro de la frase del resultado. La matriz no descuenta palabras por esta operación, solo permite eliminar un bloque vertical redundante. |
| Cadena de evidencia en quote | 07.27, líneas 210-217 | MERGE_WITH_TEXT | Integrarla en la síntesis. Evitar que las flechas sugieran prueba acumulativa o dependencia lógica no demostrada. |
| Cadena de provenance en quote | 09.30, líneas 195-204 | KEEP | Mantener relación entre revisión, protocolo, raw, derivados, manifest y comparación. Es un resumen compacto de trazabilidad. |

Los captions de las seis tablas ya son breves. No identifico un ahorro sustancial mediante KEEP_AND_COMPRESS_CAPTION. Tampoco encuentro una tabla o ecuación científica única que justifique REMOVE_ONLY_IF_FULLY_REDUNDANT. Las conversiones a texto conservan su información, no la eliminan.

La Tabla 1 conserva explícitamente TLC quorum, TLC receipt copies, Alloy Receipt scope y Alloy State scope. La explicación 05.09 mantiene Message scopes 10, 20 y 30 y el predicado de Alloy de al menos dos votos en los tres perfiles. Los quorum de TLC 2, 2 y 3 no se reutilizan como umbrales Alloy. No se propone ningún cambio de fuente, escalado o columnas que reabra B2.

#### Claims que no pueden fortalecerse

No reemplazar «no observed violations among completed bounded configurations» por «the protocol is correct». No convertir 10/10 mutantes en cobertura completa de defectos. No convertir 600 replays en refinement, behavioral equivalence o conformidad de toda ejecución Java. No convertir el orden de tres perfiles en una ley de crecimiento. No convertir tiempos absolutos entre representaciones distintas en superioridad de TLC o Alloy. No convertir hashes iguales en validez científica, ni separación de usuario en independencia de hardware. No convertir el gap del comparator set con full text verificado en prioridad mundial.

### 7. Material movible al artifact

La mayor parte del texto de Reproducibility explica significado científico y debe seguir en el paper, aunque más brevemente. **No recomiendo mover la sección entera, el diseño ni Threats a material suplementario.**

El único traslado operativo concreto de la matriz es **09.19, líneas 120-128**. Se pueden retirar del cuerpo las rutas absolutas `/home/reproducer/dtl-lab-reproduction` y `/home/reproducer/reproduction/results/` y conservarlas en el registro de provenance del intento histórico. Deben permanecer en el paper la identidad del intento, la revisión fuente `6cd88c377afd23fee4998882f91142d71e7d963e`, el digest histórico `d464888e9f3e5d8cc64ef5d22cc7b7c24f83e3853f5825f18f23de26adf6a6e6` y las condiciones de aislamiento descritas en 09.24. No deben confundirse con el commit editorial ni con el digest del paquete SCP.

El target de ese párrafo es 24 palabras frente a 38. El ahorro de 14 combina la salida de las rutas y la condensación de su envoltura narrativa. No significa que las dos rutas ocupen por sí solas catorce palabras. Al materializar la edición, la referencia al registro histórico deberá permitir recuperar las rutas exactas. No se afirma aquí que esas dos rutas literales estén ya duplicadas en la guía genérica.

Se verificó también la [guía de reproducción versionada](https://github.com/kapumota/dtl-lab/blob/5195d94b2ade1933a726d1d2106cc35a11c37f47/docs/research/paper1/REPRODUCCION_INDEPENDIENTE.md). Ya documenta comandos make, estructura del bundle, parámetros de entrada/salida, instalación, smoke de seis tareas, regeneración, comparación SHA-256 e informes. Por ello, el artículo no necesita incorporar ese manual. Esta verificación documental no implicó ejecutar ninguno de sus comandos.

No propongo trasladar 05.30 sobre metadata ambiental, 05.35 sobre campos de diagnóstico, 09.12 sobre las clases smoke ni 09.20-09.24 sobre resultados y límites. Esos pasajes permiten evaluar científicamente la reproducción. El detalle exhaustivo de nombres de manifests, comandos y logs puede permanecer en la guía, pero no se contabiliza un ahorro adicional por texto que el artículo no contiene.

### 8. Riesgos de sobrecondensación

El mínimo razonable de 9 110 es un límite de planificación, no un umbral universal ni una garantía obtenida mediante una reescritura. Tiene una incertidumbre de al menos unas centenas de palabras por las transiciones y las referencias cruzadas que resulten necesarias. Llegar a 9 000 podría ser posible, pero no está demostrado que resulte mejor.

| Recorte peligroso | Lo que se perdería o alteraría | Protección |
| --- | --- | --- |
| Reducir el modelo a nombres de estados | Semántica de rollback, timeout y proyección. | Mantener 03.07-03.18 y propiedades completas. |
| Sustituir Methodology por una tabla de RQ | Unidades experimentales, target de mutación y criterio de diagnóstico. | Conservar definiciones de 04.20-04.40. |
| Suprimir cautelas en Results porque están en Threats | El lector podría interpretar 350 completadas como toda RQ1 o censura como éxito. | Mantener límites pegados a respuestas de RQ. |
| Borrar la diferencia entre repeticiones y unidades | 100 runs podrían parecer 100 mutantes o 30 seeds una muestra aleatoria universal. | Conservar 05.23, 06.20 y 08.30-08.32. |
| Reducir Discussion a recapitulación de resultados | Se pierde la explicación del valor de diagnósticos y controles negativos. | Conservar 07.11-07.14 y 07.28. |
| Convertir Threats en una lista telegráfica | No queda claro qué sesgo produce cada amenaza ni cómo se mitiga. | Mantener las cuatro categorías con parejas amenaza/mitigación. |
| Reducir Reproducibility a «10/10 y 32/32» | Se confunden smoke, raw histórico, regeneración, proceso separado y hardware. | Mantener 09.12-09.13 y 09.20-09.28. |
| Borrar el límite del comparator set | El gap se transforma en un claim de prioridad global. | Mantener 02.13 y su resumen preciso en Introduction. |

Después del mínimo propuesto, las reservas de recorte de bajo riesgo son pequeñas. Una reducción adicional sostenida tendría que quitar ejemplos semánticos, reducir la conexión entre método y resultado o trasladar información necesaria al artifact. Esa sería una degradación del paper, aunque todos los números continuaran presentes.

El objetivo recomendado deja **434 palabras de margen** respecto del mínimo estimado. Ese margen tiene utilidad editorial: permite explicar límites con claridad sin depender excesivamente de referencias a otras secciones.

### 9. Proyección de páginas

El PDF exacto tiene las siguientes fronteras: Introduction comienza en p.2, Related Work en p.4, Model en p.8, Methodology en p.12, Design en p.19, Results en p.25, Discussion en p.31, Threats en p.36, Reproducibility en p.41 y Conclusions en p.46. Data availability, declaración de IA y bibliografía comienzan en p.48. Varias secciones comparten página, de modo que estos comienzos no deben sumarse como extensiones enteras independientes.

Manteniendo elsarticle, preprint y 12pt, sin modificar márgenes, interlineado, escalado o template:

| Escenario | Palabras de diez secciones | Estimación de PDF completo |
| --- | ---: | ---: |
| Actual | 12 446 | 51 páginas comprobadas |
| SAFE TARGET | 10 147 | Aproximadamente 42-44 páginas |
| RECOMMENDED TARGET | 9 544 | Aproximadamente 39-42 páginas |
| AGGRESSIVE LOWER BOUND | 9 110 | Aproximadamente 38-40 páginas |

La estimación separa aproximadamente cuatro o cinco páginas de contenido/espacio poco sensible al recorte, como portada, bibliografía y declaraciones, del cuerpo susceptible de condensación. Ajusta el resto por la proporción de palabras y deja margen para floats, listas, títulos y saltos. No presupone que reducir un 23 % de palabras reduzca exactamente un 23 % de páginas.

Integrar los productos 10×30 y el esquema RQ en prosa podría recuperar espacio vertical adicional sin alterar información. No se utiliza ese efecto para prometer un número exacto. **Son estimaciones de repaginación, no resultados de una compilación de un manuscrito editado.**

### 10. Plan de edición

Este es un plan para una ejecución posterior. Ninguno de los recortes ha sido aplicado al manuscrito en esta auditoría.

1. **Fijar el perímetro y los elementos protegidos.** Usar las mismas fuentes, conservar la Tabla 1 auditada y mantener una lista de números, hipótesis y límites que deberán sobrevivir. No tocar experimental data, analysis scripts ni modelos.
2. **Aplicar DELETE_REDUNDANT de riesgo NONE fuera de Results.** Eliminar 02.12, 03.23, 04.05, 04.52, 09.07, 09.32-09.37 y 09.42, comprobando sus receptores canónicos. Son doce unidades y 342 palabras. En 02.12 comprobar que cada cita siga en su párrafo de antecedentes.
3. **Acortar navegación y conectores NONE.** Introduction 01.09 y remisiones de Design. Dejar los pequeños ajustes NONE de Results para su revisión específica posterior, de modo que no se empiece editando resultados.
4. **Condensar Reproducibility de riesgo LOW.** Mantener inventario, workflow, evidencia histórica y fronteras con sus funciones distintas. Conservar seis clases smoke, 10/10, 32/32, cero incidentes, mismo host y origen raw. Realizar el único traslado operativo de 09.19 con un enlace preciso a su registro histórico.
5. **Condensar Discussion de riesgo LOW.** Sustituir renarración de números por referencias a las tablas, manteniendo la interpretación, el valor del diagnóstico y que ninguna capa subsana las limitaciones de otra.
6. **Condensar Methodology de riesgo LOW.** Conservar hipótesis y unidades. Reducir las segundas explicaciones de las RQ en Study design, Triangulation e Interpretation boundaries. No fusionar un criterio de detección con una observación del resultado.
7. **Condensar Threats de riesgo LOW.** Unir cada amenaza a su mitigación dentro de su categoría. Verificar que no desaparezcan no aleatoriedad, sesgo de completadas, escasez de niveles ni dependencia ambiental.
8. **Ajustar Introduction, Related Work, Model y Design de riesgo LOW.** Mantener contribución y comparator set, todas las referencias necesarias, supuestos, proyección, propiedades, parámetros y métodos estadísticos. Introduction debe recortar aproximadamente 174 palabras, sin convertirse en una lista desnuda de RQ.
9. **Condensar Conclusions a unas 325 palabras.** Integrar la complementariedad en la contribución y conservar resultados, límites de reproducción y future work mínimo. No reescribir todavía abstract ni declaraciones, que quedan fuera de este presupuesto.
10. **Revisar Results al final.** Primero sus remisiones y recordatorios NONE/LOW. Solo después considerar los recortes MEDIUM, que suman como máximo 194 palabras en la propuesta recomendada. Mantener las seis tablas y toda cifra única, IC, estados, ratios, outcomes, respuestas RQ y límites. Los valores que desaparezcan de una narración duplicada deben permanecer completos en la tabla correspondiente.
11. **Comprobar la edición documental.** Comparar con la matriz, reconciliar los totales, revisar las fusiones y las referencias, y compilar el PDF únicamente para validación editorial cuando se autorice la edición. Inspeccionar tablas y cortes de página después de repaginar. No ejecutar experimentos ni regenerar resultados científicos para realizar este control.
12. **Detenerse en el recomendado salvo justificación textual.** Explorar el mínimo de 9 110 solo si la versión de unas 9 544 palabras todavía contiene redundancias concretas. No recortar por cuota ni rebajar la claridad para alcanzar 9 000.

#### Control de cierre de la futura edición

La versión condensada deberá conservar una correspondencia revisable entre cada modificación y los ID de esta matriz. Debe seguir siendo posible responder, leyendo el artículo, qué se evaluó, con qué unidades y bounds, qué terminó y qué quedó censurado, qué significa cada hipótesis, qué se reprodujo realmente y qué no puede concluirse. La compilación limpia es necesaria para el cierre editorial, pero no demuestra por sí misma preservación del significado científico.

### 11. Gate final

La condensación propuesta cambia extensión y organización argumentativa, sin necesitar cambios en métodos, resultados o claims. La reducción recomendada es de 2 902 palabras, hacia un presupuesto de 9 544 para las diez secciones. El gate expresa viabilidad editorial de este plan, no certifica una edición que todavía no se ha realizado ni sustituye el estado de revisión manual final del candidato congelado.

SAFE_TO_CONDENSE_WITHOUT_SCIENTIFIC_CHANGE
