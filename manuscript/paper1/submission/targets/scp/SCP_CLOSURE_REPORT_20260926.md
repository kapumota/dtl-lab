### Cierre editorial de Paper 1: continuidad del Work

Fecha de esta reanudación: 26 de septiembre de 2026.
Destino: Science of Computer Programming; Research Papers; Formal techniques.

**NOT READY TO SUBMIT**

El candidato editorial está construido y verificado. Faltan confirmaciones
administrativas y la comprobación completa de los requisitos vigentes del
portal. No se identifica un bloqueo científico nuevo ni se reabre la campaña.

#### 1. Estado desde el que se reanudó

El Work comenzó sobre main condensado `e41ef499a645107dbbae071508464e6183669a6f`.
Antes de esta continuación ya había recuperado la trazabilidad histórica,
realizado los ajustes editoriales y revisado visualmente las 42 páginas del
candidato de `579b7d1`.

Esta continuación comenzó en `bc905f8e15fea9d6b48a66c7a269aaa661255a25`,
con worktree limpio. El último paso completado era la corrección de espaciado
de las tablas 1 y 5 y de la puntuación duplicada en los títulos de las RQ.
Quedaba verificar sus consecuencias en el PDF y terminar el paquete y el informe.

G4 ya estaba cerrado documentalmente; F6 se conservaba como PASS histórico.
G1 y G2 seguían abiertos y eran el primer impedimento real del camino crítico.
G3 tenía un candidato construido y 8G-H no estaba congelado para submission.

#### 2. Rama

`paper1/cierre-editorial-scp-final`.

Se conserva la rama existente. No se reescribe historia ni se cambian tags científicos.

#### 3. Commits iniciales

| Función | Commit |
| --- | --- |
| Main auditado al iniciar el Work | e41ef499a645107dbbae071508464e6183669a6f |
| HEAD al retomar esta continuación | bc905f8e15fea9d6b48a66c7a269aaa661255a25 |
| Scientific freeze preservado | 06bea7de70971d5b22d705a2df19137122758c08 |

#### 4. Trabajo heredado y conservado

- Campaña definitiva y contratos congelados, RQ1-RQ4 y CL-01..CL-06.
- Reproducción con usuario `reproducer`, clon y workspace separados,
  reinstalación de TLC/Alloy, dos intentos documentados, 10/10 gates,
  32/32 hashes coincidentes, cero diferencias y cero incidencias pendientes.
- Alcance exacto: `process separation, same native Linux host, no hardware-independence claim`.
- Condensación editorial de main, matrices de evidencia y decisiones sobre claims.
- Cierre histórico de `d8eefaf37454cc637f6b7f9355beb12c715b4957`, preservado
  byte a byte bajo `historical-closure/`, y paquete histórico comprobado.
  Aquel cierre no era ancestro de main y su estado de revisión manual pendiente
  no equivalía a READY_TO_SUBMIT.
- Commit `579b7d1944da4aec95743061c8d6f1c123bb8bfe`: target SCP, título
  singular, Data Availability con URL real, DOI, figura metodológica,
  aclaración de liveness, constructor editorial y G4 documental.
- Commit `bc905f8e15fea9d6b48a66c7a269aaa661255a25`: correcciones puntuales
  de tablas y títulos. No se vuelven a implementar ni a auditar desde cero.

#### 5. Trabajo nuevo de esta continuación

Se compararon los píxeles de las 42 páginas con el candidato inspeccionado.
Treinta y ocho páginas no cambiaron; se inspeccionaron visualmente las páginas
11, 13, 18 y 26. Se regeneró únicamente un PNG de revisión truncado, no el estudio.

El ZIP de fuentes pasó la comprobación de integridad y coincidencia de los
hashes de sus 15 fuentes y tres archivos de soporte. Se extrajo en otro
directorio y se recompiló: produjo exactamente el mismo SHA-256 del PDF.

Se cerraron el registro visual, el manifiesto vigente y el mapa del paquete;
se prepararon hojas editables de portada y declaraciones, y se documentaron
los bloqueos humanos. No hubo ejecuciones científicas.

#### 6. Archivos modificados

La lista completa de esta rama respecto de main se conserva en
`CHANGED_FILES.txt` dentro de la entrega. Los cambios abarcan:

| Grupo | Archivos |
| --- | --- |
| Entrada y bibliografía | main.tex; references.bib |
| Texto y composición | sections/03-cross-shard-model.tex; 04-research-methodology.tex; 05-experimental-design.tex; 06-results.tex |
| Figura editorial | sections/evidence-workflow.tex |
| Preparación editorial | SCP_COVER_LETTER_DRAFT.md; build_submission.py; declaraciones Data Availability |
| Seguimiento | SCP_SUBMISSION_STATE.json; SCP_REQUIREMENTS_CHECKLIST.md; SCP_SUBMISSION_FILES_MANIFEST.md |
| Evidencia de cierre | SCP_CROSSCHECK_20260925.json; SCP_EDITORIAL_CHANGES_20260925.md; SCP_VISUAL_AUDIT_20260926.json; este informe |
| Material administrativo | SCP_TITLE_PAGE_DRAFT.md; SCP_DECLARATIONS_DRAFT.md; SCP_UPLOAD_MAP_CANDIDATE.md |
| Historia preservada | README y seis documentos bajo historical-closure/ |

Los árboles `src`, `specs`, `experiments/paper1`, `scripts/experiments`,
`scripts/formal` y `scripts/conformance` siguen sin diferencias respecto
del scientific freeze.

#### 7. 8G-G1

**MANUAL_BLOCKER / BLOCKED_AUTHOR_CONFIRMATION.**

Faltan nombres y orden de autoría, afiliaciones y direcciones, corresponding
author y correo, ORCID cuando corresponda y CRediT confirmado.
No se sustituyen por datos inferidos del nombre de la cuenta GitHub.
La portada del PDF declara explícitamente que falta confirmación.

#### 8. 8G-G2

**MANUAL_BLOCKER / BLOCKED_AUTHOR_CONFIRMATION.**

Faltan funding, competing interests, originalidad y ausencia de envío simultáneo,
aplicabilidad ética, confirmación del uso real de IA y de la revisión humana
descrita, y confirmación de que el autor puede entregar los datos bajo solicitud.

La declaración de IA existente describe asistencia editorial y controles de
consistencia; no atribuye a IA nuevas contribuciones científicas. No se supone
que la verificación de este Work sustituya la aprobación de los autores.

La Data Availability usa únicamente https://github.com/kapumota/dtl-lab.
Distingue materiales públicos de raw/material adicional bajo solicitud.
No se inventan DOI, Zenodo, release persistente ni una URL del raw.

#### 9. 8G-G3

**CANDIDATE_BUILT_PENDING_FINAL_METADATA_AND_PORTAL_REQUIREMENTS.**

Hay PDF, ZIP editable con bibliografía, tablas y figura, cinco highlights,
cover letter, hojas de portada y declaraciones, información del artifact,
provenance y manifiestos de hashes.

La guía oficial se consultó el 25 de septiembre. Se verificaron mediante
fuentes oficiales los fragmentos sobre keywords, Data Availability y highlights.
El acceso al texto completo devolvió HTTP 403 y `Site Unavailable`.
La búsqueda complementaria durante la continuación no resolvió esa limitación.
No se afirma haber comprobado íntegramente el flujo actual del portal.

Fuentes oficiales de requisitos:
https://www.sciencedirect.com/journal/science-of-computer-programming/publish/guide-for-authors
https://www.elsevier.support/publishing/answer/how-do-i-include-highlights-with-my-manuscript

Pendiente editorial concreto: comprobar guía completa/campos vigentes, completar
metadata confirmada y sincronizar los archivos exactos antes de 8G-H.
No se impone un DOI ni una segunda máquina como requisito.

#### 10. 8G-G4

**PASS_DOCUMENTARY, conservado.** No se detectaron discrepancias en el
cross-check con contratos, documentación de evidencia y fuentes históricas
preservadas. La corrección posterior de espaciado no cambia ningún número.

| Elemento | Valor preservado |
| --- | --- |
| Tareas programadas | 1272 |
| Warm-ups / medidas | 112 / 1160 |
| RQ1 medidas / completadas / TLC-large timeouts | 420 / 350 / 70 |
| RQ2 mutantes científicos predefinidos / mutation score | 10 / 1.0 |
| RQ3 ejecuciones / válidas / negativas | 600 / 300 / 300 |
| Diagnósticos negativos esperados | 300 |
| RQ4 medidas | 460: 420 compartidas con RQ1 y 40 TLC adicionales |
| Timeout / máximo RSS | 1800 s / 12288 MiB |
| Seeds | 2026001 a 2026030 |
| Herramientas científicas | TLA+ Tools 1.7.4; Alloy 6.2.0; Java 17; Python 3.12 |
| Reproducción histórica | 10/10 gates; 32/32 hashes; 0 incidencias |

Las definiciones completas de profiles y todas las cifras de las seis tablas
están registradas en `SCP_CROSSCHECK_20260925.json`.

| Herramienta / perfil | Mediana de tiempo (s) | Mediana RSS (KiB) |
| --- | ---: | ---: |
| Alloy small | 0.515306 | 179830 |
| Alloy medium | 0.766172 | 299268 |
| Alloy large | 1.568320 | 317612 |
| TLC small | 0.666802 | 143660 |
| TLC medium | 1.118120 | 601428 |
| TLC large | No estimada: 70 timeouts | No estimada |
| TLC, Normal | 0.867343 | 379846 |
| TLC, Insufficient quorum | 1.068210 | 493370 |
| TLC, Replay | 1.118420 | 604258 |
| TLC, Timeout | 1.569610 | 822820 |

Las últimas cuatro filas siguen el orden de la Tabla 6, cuyas definiciones se
conservan sin alteración. Las medianas no se convierten en una ley asintótica
ni en una comparación de superioridad entre herramientas. H4 no se confirma
retrospectivamente.

G4 no se presenta como una nueva auditoría byte a byte del raw externo.

#### 11. PDF

**PASS_CANDIDATE_LAYOUT_ONLY, 42/42 páginas.**

Fuente compilada: `bc905f8e15fea9d6b48a66c7a269aaa661255a25`, worktree limpio.
La revisión cubre tablas, figura, captions, notación, encabezados, caracteres,
enlaces, DOI, bibliografía y declaraciones. La composición de tablas 1 y 5 y
la puntuación de las RQ quedaron corregidas. No hay overfull boxes ni
referencias o citas indefinidas en el log final.

La figura de página 12 organiza las capas de evidencia; su leyenda no promete
refinement, equivalencia o prueba acumulativa. La página 10 aclara que
`EventuallyReleasedAfterTimeout` expresa esencialmente
`Aborted -> fundsReleased`, un invariante de estado.

Los DOI de Springer con `_8` se muestran y enlazan correctamente. Se conserva
la revisión de bibliografía y las declaraciones en páginas 40-42.
Dos avisos BibTeX por campos de páginas vacíos en proceedings no afectan la
compilación; no se inventaron páginas bibliográficas.

El registro `SCP_VISUAL_AUDIT_20260926.json` identifica cada página, sus
píxeles y la revisión heredada o posterior al cambio. El PASS es de maquetación.
No existe todavía un PDF final de submission con metadata confirmada.
Después de completarla, recompilar y revisar las páginas cambiadas; conservar
el resto solo si se demuestra que no cambió.

#### 12. Artifact y provenance

| Identidad | Revisión |
| --- | --- |
| Experimental baseline | 45cb114d61b1df8c605c50700f3cc72d48d157fe |
| Ejecución raw | 248ff938c9f028745b5e370469e4796bc00755f7 |
| Fuente de reproducción | 6cd88c377afd23fee4998882f91142d71e7d963e |
| Scientific freeze | 06bea7de70971d5b22d705a2df19137122758c08 |
| Protocolo | paper1-q3-v1 |
| Fuente del PDF candidato | bc905f8e15fea9d6b48a66c7a269aaa661255a25 |

La continuidad Git ya comprobada se conserva. Los hashes de plan, raw,
derived y bundle mantienen sus funciones documentadas en
`manuscript/paper1/artifact/PROVENANCE.md`, copiado en el paquete.

La reproducción sigue siendo válida en el mismo host Linux físico con
separación de procesos/usuario/workspace. No fue una nueva ejecución completa
de 1272 tareas ni una prueba de independencia de hardware.

El bundle externo 8E no está contenido en el clon ni en el paquete editorial.
Sus bytes no se recalcularon en este Work. Se conserva F6 PASS histórico,
sin falsear una verificación nueva. F7/F8 siguen diferidos no bloqueantes.

#### 13. Checksums

SHA-256 recalculados para los bytes actuales:

| Archivo | SHA-256 |
| --- | --- |
| SCP_manuscript_CANDIDATE.pdf | b25d29574fe722bfe698c1205c1010b5bf7a9e69e179a8b6dd7ac8fa0530663a |
| SCP_latex_source_CANDIDATE.zip | 8d8de950407eff817e1fe5d5d2d53e6dc87fc16bbb88d5e03d2bb42b3f2d1294 |

Hashes históricos preservados, con su alcance:

| Evidencia | SHA-256 | Verificación de este Work |
| --- | --- | --- |
| Paquete editorial histórico | e471c8c7ea55167250bda69bb721668f5c7cc5655c6e67f66d7a192241e67f79 | Recalculado; ocho hashes internos correctos |
| Raw archive 8E | d5b553de18d17da4c5b4278b4d13fe48e7e0898f7bbec494eece5e67f469b463 | F6 histórico conservado |
| Derived manifest 8E | 7a2622726f96e209171096a1c50109600c3af7c0416b595cad1a836fbc5b889f | F6 histórico conservado |
| Bundle de reproducción 8E | d464888e9f3e5d8cc64ef5d22cc7b7c24f83e3853f5825f18f23de26adf6a6e6 | F6 histórico conservado |

El manifiesto `SHA256SUMS.txt` del paquete registra cada archivo entregado.
El hash del archivo contenedor se entrega aparte para evitar autorreferencia.
Ninguno de estos hashes representa un freeze 8G-H completado.

#### 14. Bloqueos manuales

| Clase | Bloqueo concreto | Gate |
| --- | --- | --- |
| Administrativo | Identidad, orden, afiliaciones, contacto, ORCID aplicable y CRediT | G1 |
| Administrativo | Funding, conflictos, originalidad, ética, aprobación factual de IA y disponibilidad bajo solicitud | G2 |
| Editorial | Acceso y comprobación completa de guía y campos actuales del portal | G3 |
| Editorial | Completar metadata, compilar y auditar los bytes exactos resultantes | G3 / 8G-H |

No se identifica un bloqueo científico nuevo ni se impone un bloqueo adicional
de artifact/provenance. El orden para retomar es G1, G2, completar G3,
confirmar que G4 no fue invalidado, revisar el PDF afectado y cerrar 8G-H.

#### 15. Mejoras diferidas

`POST-SUBMISSION / REVIEWER-DRIVEN`: segunda máquina física, más bounds,
mutantes o trazas y generative testing.

`PAPER 2 / PHASE 9`: segundo protocolo, temporal liveness, fairness,
invariantes inductivas, refinement Java-TLA+ e integración de blockchain
productiva. No se implementó ninguna.

#### 16. Commit final y freeze

La fuente exacta del PDF permanece en `bc905f8e15fea9d6b48a66c7a269aaa661255a25`.
El commit documental que incorpora este informe y el estado final se registra
después de confirmar los archivos en `FINAL_REVISION.json` de la entrega y
en el informe al usuario. Así se evita una referencia circular al propio commit.

8G-H: **NOT_FROZEN**. No se creó tag de submission, fecha de freeze ni
submission ID. `ready_to_submit=false`; `submitted=false`.

#### 17. Veredicto único

**NOT READY TO SUBMIT**

