### Mapa del paquete candidato SCP

Estado: `READY_FOR_MANUAL_REVIEW`. Los nombres de categorías del portal deben
comprobarse en el flujo de envío vigente. No se realizó ningún submission.

| Archivo o contenido | Uso previsto | Estado |
| --- | --- | --- |
| SCP_manuscript_CANDIDATE.pdf | Manuscrito para revisión humana | 43 páginas; rebuild técnico completado; revisión visual manual pendiente |
| SCP_latex_source_CANDIDATE.zip | Fuentes editables, bibliografía, tablas y figura TikZ | ZIP íntegro y recompilación exacta verificados |
| Highlights.txt | Highlights editables separados | Cinco entradas, hasta 85 caracteres |
| Cover_Letter_SCP_DRAFT.md | Base editable para la carta | Datos administrativos confirmados; comprobación final del portal pendiente |
| Title_Page_SCP_DRAFT.md | Datos de portada confirmados | G1 CLOSED |
| Declarations_SCP_DRAFT.md | CRediT y declaraciones confirmadas | G2 CLOSED |
| AUTHOR_CONFIRMATIONS.md | Confirmaciones administrativas del autor | CONFIRMED |
| SCP_AI_DECLARATION.tex | Declaración de IA incorporada al PDF | Uso confirmado por el autor; rebuild pendiente |
| SCP_DATA_AVAILABILITY.md | Texto editorial y alcance de disponibilidad | Confirmado por el autor |
| Artifact_Provenance.md | Procedencia documental preservada | Evidencia histórica; no es un bundle raw nuevo |
| SCP_CLOSURE_REPORT_20260926.md | Informe de cierre y pendientes | Documento de auditoría |
| SCP_CROSSCHECK_20260925.json | Comparaciones numéricas y de contratos | G4 PASS_DOCUMENTARY |
| SCP_VISUAL_AUDIT_20260926.json | Registro de las 42 páginas | PASS_CANDIDATE_LAYOUT_ONLY |
| BUILD_PROVENANCE.json | Commit y hashes de las fuentes compiladas | Fuente e37290a; worktree limpio al construir |
| FINAL_REVISION.json | Commit documental exacto de esta entrega | Se genera después del commit |
| SHA256SUMS.txt | Hashes de todos los archivos del paquete | Excluye únicamente el propio manifiesto |

Las seis tablas y la figura se incluyen como fuentes LaTeX editables. No existen
imágenes externas necesarias para compilar. No se inventa un suplemento
científico: el bundle 8E continúa externo y conserva su identidad histórica.

El archivo contenedor es un paquete de revisión. No debe subirse como si fuera
el submission final. Con G1/G2 cerrados y tras la comprobación completa de G3, preparar los
archivos que solicite el portal y congelar exactamente esos bytes en 8G-H.
