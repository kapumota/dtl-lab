### Manifiesto vigente del candidato SCP

Estado: `READY_FOR_MANUAL_REVIEW`. Actualizado el 1 de octubre de 2026.

Este documento sustituye únicamente el inventario operativo anterior de 8G-E.
Los cierres y hashes históricos se conservan en Git y en `historical-closure/`.

#### Fuentes y construcción

Fuente exacta del candidato corregido de revisión: `7ac0a4faa6a145639704fe2e92f08110f9561acc`.
Baseline de la auditoría independiente final: `bea8811bce393c05233be31f620b113d6defca27`.
Scientific freeze: `06bea7de70971d5b22d705a2df19137122758c08`.
Constructor: `build_submission.py`, sin llamadas a scripts científicos.

El ZIP conserva las rutas relativas LaTeX y contiene las secciones, bibliografía,
tablas editables, figura vectorial TikZ, declaraciones, clase y estilo de Elsevier
y su licencia. La extracción en un directorio separado recompila con éxito y
produce el mismo SHA-256 del PDF. No es el ZIP aplanado del cierre histórico.

#### Archivos y hashes

| Archivo | SHA-256 |
| --- | --- |
| SCP_manuscript_CANDIDATE.pdf | c1ab7ad8c512062136017d1797d9e3cbcd17b1e3d970b8c887b8d9363e65d2a2 |
| SCP_latex_source_CANDIDATE.zip | 1ba404d576f20b65a92abc27ca98bf408abff1de3dead6f390613cce50723fae |

El resto del inventario y su uso están en `SCP_UPLOAD_MAP_CANDIDATE.md`.
El paquete de revisión incluye `SHA256SUMS.txt` para todos sus archivos y
`FINAL_REVISION.json` para el commit documental exacto de la entrega.

#### Gates

| Gate | Estado |
| --- | --- |
| Compilación y fuentes | PASS_REVIEW_CANDIDATE |
| Revisión visual | PASS auditoría independiente de 43 páginas; aprobación manual del autor pendiente |
| G1 / G2 | PASS, información y declaraciones confirmadas por el autor |
| G3 | Candidato corregido construido; aprobación manual del autor y portal final pendientes |
| G4 | PASS_DOCUMENTARY, conservado |
| F6 | PASS histórico conservado |
| F7 / F8 | DEFERRED_NON_BLOCKING |
| 8G-H | NOT_FROZEN |

#### Siguiente acción

G1 y G2 están cerrados. M1 y M3 están resueltos, M2 se acepta como detalle cosmético. Completar la aprobación manual del autor y la comprobación final del portal antes del freeze 8G-H.
No se requiere crear un DOI, release, nueva campaña ni segunda máquina física.
El candidato actual contiene la metadata confirmada. El PDF final congelado para submission todavía no existe.
