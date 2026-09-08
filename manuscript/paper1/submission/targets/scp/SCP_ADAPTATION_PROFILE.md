### SCP Adaptation Profile 8G-C

#### Identificación

Target:

`Science of Computer Programming`

ISSN:

`0167-6423`

Fecha de verificación:

`2026-09-07`

Scientific freeze commit:

`06bea7d`

Baseline HEAD:

`06bea7de70971d5b22d705a2df19137122758c08`

#### Article route

Track:

`Research Papers`

Primary line:

`Formal techniques`

Secondary fit:

`Experimental software technology`

No usar:

`Software Track / Original Software Publication`

Motivo:

El paper presenta una evaluación científica de un protocolo ejecutable y de su evidencia formal/experimental. DTL-Lab es parte del objeto experimental, pero el artículo no es una descripción corta de herramienta OSP.

#### Scope fit

SCP cubre explícitamente:

- specification;
- validation;
- verification;
- testing;
- formal techniques;
- experimental software technology;
- implementations and experiments with systems and methods.

Fit del Paper 1:

`VERY HIGH`

#### Título

Baseline:

`Reproducible Multi-Layer Evaluation of Cross-Shard Commit Protocols`

Título recomendado para SCP:

`Reproducible Multi-Layer Evaluation of a Cross-Shard Commit Protocol`

Motivo del cambio:

El estudio evalúa un protocolo cross-shard concreto. El singular reduce riesgo de generalización implícita sin alterar CL-01..CL-06.

Estado:

`EDITORIAL_DELTA_RECOMMENDED`

#### Abstract

Palabras actuales:

`216`

Citas en abstract:

`0`

Estado:

`PASS`

El abstract es autónomo y contiene purpose, method, resultados principales y límites.

#### Keywords

Número:

`7`

SCP permite:

`1-7`

Estado:

`PASS_AT_UPPER_LIMIT`

Keywords actuales:

- `cross-shard transactions`;
- `distributed ledgers`;
- `formal verification`;
- `model checking`;
- `mutation analysis`;
- `trace conformance`;
- `reproducibility`;

No añadir una octava keyword.

#### Highlights

Número:

`5`

Límite:

`3-5 bullets; <=85 caracteres incluyendo espacios`

Estado:

`PASS`

- 73 caracteres: Complementary formal models provide bounded cross-shard property evidence
- 80 caracteres: Controlled mutations test whether protocol properties detect weakened safeguards
- 79 caracteres: Trace checks connect selected implementation behavior to the formal abstraction
- 75 caracteres: Verification cost exposes practical limits of the bounded evidence campaign
- 78 caracteres: Artifact reproduction recovers expected analysis outputs from a separate clone

Se mantienen como archivo editable separado:

`manuscript/paper1/highlights.txt`

#### LaTeX

Baseline:

`elsarticle`

SCP:

Elsevier recomienda su template LaTeX y requiere los fuentes editables relevantes.

Estado:

`PASS`

No se necesita migración de clase para el primer submission.

#### References

Bibitems validados en 8F-J:

`15`

SCP no exige formato estricto de referencias en el primer submission si el estilo es consistente; recomienda DOI y aplica el estilo final tras aceptación.

Estado:

`PASS`

No reescribir `references.bib` en 8G-C.

#### Research data

SCP anima a declarar disponibilidad de research data y permite enlazar datasets/repositorios.

Estado:

`REQUIRED_FOR_OUR_SUBMISSION_STRATEGY`

Preparar en 8G-D/8G-F:

- source code;
- formal models;
- frozen experiment specification;
- raw observations;
- derived outputs;
- analysis scripts;
- reproduction scripts;
- manifests/checksums;
- release/DOI cuando existan.

#### Generative AI declaration

Estado:

`REQUIRED`

Elsevier exige una sección separada antes de References cuando IA generativa realizó cambios sustantivos de redacción u organización.

Se insertará en:

`8G-D`

No se considera parte de la contribución científica.

#### Author/title-page metadata

Estado:

`PENDING`

Antes del submission deben estar completos:

- author names;
- affiliations;
- corresponding author;
- email;
- postal address;
- phone number if portal requires it;
- ORCID where provided.

#### Competing interests and funding

Estado:

`PENDING_AUTHOR_CONFIRMATION`

Resolver en 8G-D.

#### Concurrent submission

Estado:

`MUST_CONFIRM_BEFORE_SUBMIT`

Un mismo manuscrito no puede estar sometido simultáneamente a otra revista.

#### Preprint

SCP permite preprints y no los considera publicación previa bajo su política.

Estado:

`OPTIONAL`

No publicar preprint automáticamente en 8G-C.

#### Hard page/word limit

Estado:

`NO_HARD_LIMIT_IDENTIFIED_IN_CURRENT_GUIDE_EVIDENCE`

No condensar el paper por iniciativa propia para SCP.

#### Scientific delta

Cambios científicos requeridos:

`NONE`

Cambios editoriales recomendados:

1. título singular para reflejar un solo protocolo;
2. completar author/title-page metadata;
3. añadir declaraciones administrativas;
4. producir cover letter SCP;
5. enlazar data/artifact cuando el release esté disponible.

#### Gate

`PASS`

#### Siguiente fase

`8G-D: SCP declarations and cover letter`
