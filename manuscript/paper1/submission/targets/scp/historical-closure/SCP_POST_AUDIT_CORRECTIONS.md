### Correcciones posteriores a la auditoría independiente

#### Scientific baseline

`06bea7de70971d5b22d705a2df19137122758c08`

#### Candidato auditado

Commit:

`3277639da222cc017dffc9bb99e7655a6c307b3b`

Audit package SHA-256:

`eef219955ed571b8e1dbb8cfe62ca4850e81c5a8416c95c1cc59ba440ddd149c`

#### Motivo

La auditoría independiente confirmó la integridad del paquete, la compilabilidad, la coherencia de RQ1 a RQ4 y la preservación del contenido científico respecto del freeze.

La auditoría identificó cinco correcciones editoriales concretas antes de la revisión manual final.

#### Deltas autorizados

1. Añadir la URL pública de DTL-Lab a Data Availability y al cover letter.
2. Aclarar en la Tabla 1 qué parámetros corresponden a TLC y qué scopes corresponden a Alloy.
3. Corregir la representación de dos URLs DOI con underscore.
4. Sustituir `ten mutation classes` por `ten predefined scientific mutants`.
5. Uniformizar `DLT-Lab` como `DTL-Lab`.

#### Alcance

Estas correcciones no modifican:

- resultados experimentales
- número de tareas
- configuraciones ejecutadas
- seeds
- censura
- mutation score
- resultados de trace conformance
- caracterización de verification cost
- evidencia de reproducción histórica

No se ejecutan nuevos experimentos.

No se repite la campaña de 1272 tareas.

No se realiza una nueva reproducción.

#### Estado

`POST_AUDIT_REBUILD_PASS_READY_FOR_WORK_CLOSURE_CHECK`
