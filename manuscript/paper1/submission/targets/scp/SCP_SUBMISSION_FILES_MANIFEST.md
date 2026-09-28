### Manifiesto vigente del candidato SCP

Estado: `NOT_READY_TO_SUBMIT`. Actualizado el 26 de septiembre de 2026.

Este documento sustituye únicamente el inventario operativo anterior de 8G-E.
Los cierres y hashes históricos se conservan en Git y en `historical-closure/`.

#### Fuentes y construcción

Fuente exacta del manuscrito: `bc905f8e15fea9d6b48a66c7a269aaa661255a25`.
Scientific freeze: `06bea7de70971d5b22d705a2df19137122758c08`.
Constructor: `build_submission.py`, sin llamadas a scripts científicos.

El ZIP conserva las rutas relativas LaTeX y contiene las secciones, bibliografía,
tablas editables, figura vectorial TikZ, declaraciones, clase y estilo de Elsevier
y su licencia. La extracción en un directorio separado recompila con éxito y
produce el mismo SHA-256 del PDF. No es el ZIP aplanado del cierre histórico.

#### Archivos y hashes

| Archivo | SHA-256 |
| --- | --- |
| SCP_manuscript_CANDIDATE.pdf | b25d29574fe722bfe698c1205c1010b5bf7a9e69e179a8b6dd7ac8fa0530663a |
| SCP_latex_source_CANDIDATE.zip | 8d8de950407eff817e1fe5d5d2d53e6dc87fc16bbb88d5e03d2bb42b3f2d1294 |

El resto del inventario y su uso están en `SCP_UPLOAD_MAP_CANDIDATE.md`.
El paquete de revisión incluye `SHA256SUMS.txt` para todos sus archivos y
`FINAL_REVISION.json` para el commit documental exacto de la entrega.

#### Gates

| Gate | Estado |
| --- | --- |
| Compilación y fuentes | PASS_TECHNICAL_CANDIDATE |
| Revisión visual | PASS_CANDIDATE_LAYOUT_ONLY, 42 páginas |
| G1 / G2 | MANUAL_BLOCKER |
| G3 | Candidato preparado; metadata y requisitos completos del portal pendientes |
| G4 | PASS_DOCUMENTARY, conservado |
| F6 | PASS histórico conservado |
| F7 / F8 | DEFERRED_NON_BLOCKING |
| 8G-H | NOT_FROZEN |

#### Siguiente acción

Completar G1 y G2 con información humana confirmada y terminar G3.
No se requiere crear un DOI, release, nueva campaña ni segunda máquina física.
El PDF de submission todavía no existe con metadata confirmada.
