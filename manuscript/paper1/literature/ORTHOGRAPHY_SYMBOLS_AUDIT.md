### Auditoría de ortografía y símbolos 8F-G8-D

#### Identificación

Fase:

`8F-G8-D`

Fecha:

`2026-09-07`

Objetivo:

Normalizar ortografía española y símbolos en la documentación de investigación del Paper 1, y verificar que el manuscrito LaTeX no contenga residuos Markdown, guiones largos, flechas Unicode, emojis ni símbolos decorativos no deseados.

#### Alcance

La corrección ortográfica automática se restringe a documentos narrativos Markdown de autoría propia. `SEARCH_LOG.md` y `SEED_PAPERS.md` solo reciben normalización de headings para preservar títulos, DOI y metadata externa. No se modifica `RELATED_WORK_MATRIX.csv`, `references.bib`, títulos bibliográficos ni metadata externa.

El manuscrito `.tex` se somete a lint de símbolos y residuos de formato, pero G8-D no reescribe su prosa científica en inglés.

#### Resultado global

- correcciones de tildes de alta confianza: 0;
- normalizaciones de símbolos: 0;
- normalizaciones de encabezados Markdown: 0;
- documentos Markdown modificados: 0;
- cero headings fuera de `###` y `####` después de la normalización;
- cero guiones largos Unicode en los documentos auditados;
- cero flechas Unicode en los documentos auditados;
- cero emojis o símbolos pictográficos;
- cero fences Markdown residuales en el manuscrito LaTeX;
- cero separadores Markdown residuales en el manuscrito LaTeX.

#### Convenciones fijadas

- títulos Markdown: `###`;
- subtítulos Markdown: `####`;
- flecha hacia la derecha en documentación: `->`;
- flecha hacia la izquierda en documentación: `<-`;
- relación bidireccional en documentación: `<->`;
- guiones largos Unicode: no permitidos;
- emojis y símbolos decorativos: no permitidos;
- términos técnicos ingleses, claves BibTeX, DOI, rutas, hashes y código: no se corrigen ortográficamente.

#### Archivos modificados

- ninguno; la documentación ya estaba normalizada.

#### Gate de cierre

8F-G8-D queda cerrada cuando:

- `ORTHOGRAPHY_SYMBOLS_AUDIT.md` existe;
- `git diff --check` pasa;
- los Markdown auditados usan únicamente `###` y `####`;
- no aparecen guiones largos, flechas Unicode, emojis ni residuos Markdown;
- el manuscrito continúa compilando sin errores, citas indefinidas ni referencias indefinidas;
- no se modifican datos científicos, DOI, claves bibliográficas ni resultados experimentales.

#### Siguiente fase

`8F-G8-E: LaTeX and manuscript integration audit`
