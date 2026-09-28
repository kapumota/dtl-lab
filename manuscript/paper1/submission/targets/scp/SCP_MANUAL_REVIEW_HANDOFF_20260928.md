### Entrega para revisión manual del Paper 1

#### Estado

`READY_FOR_MANUAL_REVIEW`

Este estado no significa `READY_TO_SUBMIT`.

No se ha realizado ningún envío a Science of Computer Programming.

#### Rama

`paper1/cierre-editorial-scp-final`

#### Fuente del candidato

`616f06e98b769687b99a52e7d1073939a896c96d`

#### Scientific freeze

`06bea7de70971d5b22d705a2df19137122758c08`

#### Gates

- G1: PASS_AUTHOR_CONFIRMED
- G2: PASS_AUTHOR_DECLARATIONS_CONFIRMED
- G3: REVIEW_CANDIDATE_BUILT_PENDING_MANUAL_REVIEW_AND_FINAL_PORTAL_CHECK
- G4: PASS_DOCUMENTARY
- 8G-H: NOT_FROZEN

#### Candidato de revisión

PDF SHA-256:

`90bbaef6c82682eeddd736ab97dce0cfa26808937f8f5504afa4b90e90276dc9`

Source ZIP SHA-256:

`92a9325918acf7fc35077a7888dbd62c4cf543609c0e0a0a6bfab121a68e837e`

Páginas:

`43`

#### Build

El log final de LaTeX no contiene citas indefinidas, referencias indefinidas,
errores LaTeX, emergency stops ni fatal errors.

`main.bbl` existe y no está vacío.

El log final no contiene overfull boxes.

Los warnings observados en `compile.log` correspondían a pasadas intermedias
de `latexmk` y no permanecen en `main.log`.

#### Integridad científica

Las secciones científicas y `references.bib` permanecen sin cambios respecto
del candidato editorial auditado por Work.

Los árboles protegidos por el scientific freeze permanecen intactos.

No se ejecutaron nuevos experimentos científicos.

Los hechos principales comprobados en el PDF incluyen:

- 1272 tareas programadas
- 420 ejecuciones medidas de RQ1
- 350 completadas
- 70 ejecuciones TLC-large censuradas
- 10 predefined scientific mutants
- 600 ejecuciones RQ3
- 10/10 reproduction gates
- 32/32 hashes

#### Diferencia respecto del candidato anterior

El candidato anterior auditado por Work tenía 42 páginas.

El candidato actual tiene 43 páginas debido a la incorporación de metadata
definitiva de autoría y al consiguiente cambio de paginación.

Por esa razón debe realizarse una revisión visual completa del candidato
actual y no reutilizarse como equivalente la auditoría visual anterior.

#### Revisión manual pendiente

Revisar las 43 páginas, con atención especial a:

- título
- nombre y afiliación
- corresponding author
- correo
- abstract
- keywords
- saltos de página
- tablas
- figura
- referencias
- Data Availability
- declaración de IA
- página final

#### Antes del submission

Después de la revisión manual y, opcionalmente, una última auditoría
independiente con Work:

- corregir únicamente hallazgos justificados
- reconstruir el candidato si existe cualquier cambio
- verificar los requisitos vigentes del portal SCP
- cerrar G3
- congelar los bytes exactos mediante 8G-H
- marcar `READY_TO_SUBMIT` únicamente después del freeze

#### Estado de envío

`ready_to_submit = false`

`submitted = false`
