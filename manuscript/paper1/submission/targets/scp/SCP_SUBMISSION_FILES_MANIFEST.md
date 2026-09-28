### Manifiesto vigente del candidato SCP

Estado: `READY_FOR_MANUAL_REVIEW`. Actualizado el 28 de septiembre de 2026.

Este documento sustituye únicamente el inventario operativo anterior de 8G-E.
Los cierres y hashes históricos se conservan en Git y en `historical-closure/`.

#### Fuentes y construcción

Fuente exacta del candidato de revisión: `e37290a810e235b7039c0820b8062afddf65ab33`.
Scientific freeze: `06bea7de70971d5b22d705a2df19137122758c08`.
Constructor: `build_submission.py`, sin llamadas a scripts científicos.

El ZIP conserva las rutas relativas LaTeX y contiene las secciones, bibliografía,
tablas editables, figura vectorial TikZ, declaraciones, clase y estilo de Elsevier
y su licencia. La extracción en un directorio separado recompila con éxito y
produce el mismo SHA-256 del PDF. No es el ZIP aplanado del cierre histórico.

#### Archivos y hashes

| Archivo | SHA-256 |
| --- | --- |
| SCP_manuscript_CANDIDATE.pdf | 90bbaef6c82682eeddd736ab97dce0cfa26808937f8f5504afa4b90e90276dc9 |
| SCP_latex_source_CANDIDATE.zip | 92a9325918acf7fc35077a7888dbd62c4cf543609c0e0a0a6bfab121a68e837e |

El resto del inventario y su uso están en `SCP_UPLOAD_MAP_CANDIDATE.md`.
El paquete de revisión incluye `SHA256SUMS.txt` para todos sus archivos y
`FINAL_REVISION.json` para el commit documental exacto de la entrega.

#### Gates

| Gate | Estado |
| --- | --- |
| Compilación y fuentes | PASS_REVIEW_CANDIDATE |
| Revisión visual | PENDING_MANUAL_REVIEW, 43 páginas |
| G1 / G2 | PASS, información y declaraciones confirmadas por el autor |
| G3 | Candidato de revisión construido; revisión manual y portal final pendientes |
| G4 | PASS_DOCUMENTARY, conservado |
| F6 | PASS histórico conservado |
| F7 / F8 | DEFERRED_NON_BLOCKING |
| 8G-H | NOT_FROZEN |

#### Siguiente acción

G1 y G2 están cerrados. Revisar manualmente las 43 páginas y completar G3 antes del freeze 8G-H.
No se requiere crear un DOI, release, nueva campaña ni segunda máquina física.
El PDF de submission todavía no existe con metadata confirmada.
