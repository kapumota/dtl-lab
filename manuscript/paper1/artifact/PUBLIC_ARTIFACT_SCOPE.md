### Alcance público del artifact del Paper 1

#### Propósito

Este documento define qué categorías de archivos pueden formar parte del artifact científico público asociado al Paper 1.

El artifact público no es una copia completa del repositorio DLT-Lab, no es el paquete editorial de Science of Computer Programming y no sustituye al scientific manuscript freeze.

Su objetivo es proporcionar el conjunto mínimo suficiente de software, modelos, contratos experimentales, evidencia y herramientas necesario para inspeccionar, regenerar y verificar la evidencia científica declarada por el Paper 1.

#### Baselines de referencia

Scientific manuscript freeze:

`06bea7de70971d5b22d705a2df19137122758c08`

Experimental baseline:

`45cb114d61b1df8c605c50700f3cc72d48d157fe`

Independent reproduction source:

`6cd88c377afd23fee4998882f91142d71e7d963e`

Baseline de `main` al abrir 8G-F2:

`6ff8534710b7b3a0be5d481d61d26e0346dac3d2`

Artifact-release revision:

`PENDING`

Submission revision:

`PENDING`

El artifact final no debe atribuir estos roles a un mismo commit.

#### Clasificación

Cada componente candidato se clasifica en una de cuatro categorías.

`INCLUDE`

El componente forma parte del alcance científico público y debe estar disponible en el artifact final, salvo que una auditoría posterior descubra un problema verificable.

`CONDITIONAL`

El componente solo se incorpora si resulta necesario para compilar, ejecutar, interpretar o verificar la evidencia incluida.

`PENDING_F4`

El componente debe formar parte de la evaluación del artifact, pero su archivo físico, integridad, procedencia o relación con el bundle histórico todavía debe verificarse en 8G-F4.

`EXCLUDE`

El componente no pertenece al artifact científico público.

#### INCLUDE: contratos experimentales

Incluir:

`experiments/paper1/experiment-spec.json`

`experiments/paper1/configurations.csv`

`experiments/paper1/seeds.txt`

`experiments/paper1/cases.json`

`experiments/paper1/result-schema-v1.json`

`experiments/paper1/execution-profiles.json`

`experiments/paper1/analysis-spec.json`

`experiments/paper1/reproduction-spec.json`

`experiments/paper1/README.md`

Estos archivos definen la campaña, las configuraciones, las seeds, el contrato de resultados, la traducción ejecutable, el análisis y la reproducción.

No contienen los resultados definitivos.

#### INCLUDE: modelos formales

Incluir los modelos científicos utilizados por el Paper 1.

TLA+:

`specs/tla/CrossShardCommit.tla`

`specs/tla/CrossShardCommit.cfg`

`specs/tla/configs/`

`specs/tla/mutants/`

Alloy:

`specs/alloy/CrossShardCommit.als`

`specs/alloy/mutants/`

`specs/alloy/scopes/`

También incluir:

`specs/trace/trace-schema-v1.json`

Los modelos mutantes se incluyen porque RQ2 depende explícitamente de su detección.

#### INCLUDE: scripts científicos

Incluir los scripts necesarios para ejecutar o verificar las capas científicas del Paper 1.

Esto comprende las rutas científicas bajo:

`scripts/formal/`

`scripts/conformance/`

`scripts/experiments/`

También incluir:

`scripts/export_trace_catalog.sh`

Los scripts de instalación de TLC y Alloy pueden incluirse como soporte reproducible.

Los binarios descargados de herramientas externas no se incorporan automáticamente al artifact.

#### INCLUDE: build y versiones

Incluir:

`Makefile`

`pom.xml`

`LICENCE`

`scripts/formal/tool_versions.env`

El artifact debe conservar una forma explícita de identificar las versiones requeridas de Java, Python, TLC y Alloy.

#### INCLUDE: implementación directamente asociada al estudio

La implementación Java que materializa el protocolo cross-shard y sus capas experimentales pertenece al alcance científico.

Las rutas candidatas principales son:

`src/main/java/dltlab/sharding/`

`src/main/java/dltlab/simulation/`

`src/main/java/dltlab/trace/`

`src/main/java/dltlab/conformance/`

`src/main/java/dltlab/verification/`

y sus pruebas directamente asociadas bajo:

`src/test/java/dltlab/sharding/`

`src/test/java/dltlab/simulation/`

`src/test/java/dltlab/trace/`

`src/test/java/dltlab/conformance/`

La inclusión exacta se materializará mediante archivos concretos en F7.

No se debe interpretar la inclusión de una clase como evidencia de que todo DLT-Lab formó parte del estudio.

#### CONDITIONAL: dependencias transitivas del software

Componentes generales de DLT-Lab pueden ser necesarios para compilar o ejecutar el protocolo cross-shard.

Por ejemplo, clases bajo:

`src/main/java/dltlab/transaction/`

u otros módulos generales pueden incorporarse si una dependencia real del código científico lo exige.

La inclusión de una dependencia de compilación no convierte ese módulo en objeto experimental ni en evidencia científica.

F4 y F6 deben identificar y comprobar la mínima clausura de dependencias necesaria.

#### INCLUDE: documentación científica mínima

El artifact debe incluir documentación suficiente para interpretar el protocolo y ejecutar la reproducción sin depender de documentación editorial.

Son candidatos de inclusión:

`docs/research/paper1/PROTOCOLO_EXPERIMENTAL_Q3.md`

`docs/research/paper1/INFRAESTRUCTURA_EXPERIMENTAL.md`

`docs/research/paper1/EJECUCION_MATRIZ_EXPERIMENTAL.md`

`docs/research/paper1/ANALISIS_RESULTADOS_EXPERIMENTALES.md`

`docs/research/paper1/REPRODUCCION_INDEPENDIENTE.md`

`docs/research/paper1/FORMATO_DE_TRAZAS.md`

`docs/research/paper1/FUNCION_DE_ABSTRACCION.md`

`docs/research/paper1/MAPEO_JAVA_TLA.md`

`docs/research/paper1/REPLAY_TLC.md`

`docs/research/paper1/CORPUS_NEGATIVO_TRAZAS.md`

La selección final debe evitar duplicación innecesaria.

El artifact final tendrá además un README específico generado en F7.

#### PENDING_F4: evidencia raw definitiva

La evidencia raw de la campaña debe preservarse públicamente si su integridad y procedencia pueden verificarse.

La ruta esperada es:

`results/experiments/raw/paper1-q3-v1/`

En el workspace auditado existen, entre otros:

`environment.json`

`provenance.json`

`raw-manifest.json`

`runtime.json`

`state.json`

`timing-host.json`

`console.log`

Sin embargo, los resultados generados bajo `results/experiments/` no están actualmente versionados de forma ordinaria en Git.

Por tanto, estos archivos no se declaran todavía como artifact final verificado.

8G-F4 debe localizar la copia canónica, verificar hashes, relacionarla con la campaña definitiva y establecer su procedencia antes de incorporarla al archive público.

El archivo de lock no forma parte de la evidencia científica final.

#### PENDING_F4: resultados derivados

Las rutas esperadas son:

`results/experiments/derived/paper1-q3-v1/`

`results/experiments/tables/paper1-q3-v1/`

`results/experiments/figures/paper1-q3-v1/`

Los resultados derivados deben poder reconstruirse desde raw mediante scripts versionados.

La copia final incluida en el artifact debe verificarse contra los manifests y contra la regeneración realizada en F6.

#### Separación entre raw y derived

`raw` representa observaciones directas de la campaña experimental y metadatos asociados a su ejecución.

Los archivos raw no deben editarse manualmente.

`derived` representa datos consolidados, estadísticas y respuestas estructuradas calculadas desde raw.

`tables` y `figures` son productos derivados secundarios.

La existencia de una tabla o figura no sustituye a sus datos raw.

La reproducción independiente puede regenerar análisis y comparar hashes, pero no sustituye a la campaña definitiva de 1272 tareas.

Los tiempos del smoke científico no deben mezclarse con las mediciones definitivas.

#### PENDING_F4: bundle histórico 8E

El bundle de reproducción independiente de Fase 8E tiene como referencia conocida:

Source commit:

`6cd88c377afd23fee4998882f91142d71e7d963e`

SHA-256:

`d464888e9f3e5d8cc64ef5d22cc7b7c24f83e3853f5825f18f23de26adf6a6e6`

Este bundle es evidencia histórica de reproducción.

No se presume que sea idéntico al artifact final de publicación.

F4 debe localizar el archivo físico, verificar el SHA-256, inspeccionar su contenido y determinar qué partes son reutilizables.

#### CONDITIONAL: outputs de conformidad y herramientas formales

Las rutas generadas bajo:

`results/formal/`

`results/traces/`

`results/conformance/`

no se incorporan automáticamente.

F4 debe determinar si contienen evidencia no representada de manera suficiente dentro de la campaña experimental definitiva.

Si son necesarios para verificar una afirmación científica, se incorporarán con provenance y hashes.

Si duplican evidencia regenerable o evidencia ya preservada en la campaña definitiva, podrán excluirse del archive final.

#### EXCLUDE: estrategia editorial interna

No incluir en el artifact científico público:

`manuscript/paper1/submission/targets/JOURNAL_TARGET_MATRIX.md`

`manuscript/paper1/submission/targets/JOURNAL_TARGET_MATRIX.csv`

`manuscript/paper1/submission/targets/TARGET_CASCADE.md`

perfiles internos de adaptación por revista,

análisis de APC,

rankings editoriales,

estrategias de fallback,

cover letters,

checklists del portal,

estados internos de submission,

razones de rechazo futuras,

material de negociación o estrategia editorial.

Estos archivos pueden permanecer en la historia del repositorio.

Su exclusión del artifact no elimina ni reescribe la historia Git.

#### EXCLUDE: package editorial del manuscrito

El artifact científico no es el bundle de submission.

Por defecto no incluir:

`manuscript/paper1/main.tex`

`manuscript/paper1/sections/`

`manuscript/paper1/references.bib`

`manuscript/paper1/highlights.txt`

declaraciones administrativas,

cover letter,

archivos específicos del portal editorial.

La versión enviada del manuscrito y el artifact reproducible tendrán provenance relacionado, pero son productos distintos.

#### EXCLUDE: auditoría bibliográfica interna

Los archivos bajo:

`manuscript/paper1/literature/`

no forman parte del artifact reproducible por defecto.

Documentan la construcción y auditoría bibliográfica del manuscrito, pero no son necesarios para ejecutar el protocolo, verificar los resultados o regenerar el análisis.

#### EXCLUDE: software general no utilizado como evidencia

DLT-Lab contiene componentes que exceden el alcance del Paper 1.

Por defecto quedan fuera del artifact científico los módulos generales de blockchain, mempool, MEV, DeFi, minería, consenso, red, PoW, visualización, wallets y demos que no formen parte de la clausura de dependencias del estudio cross-shard.

Si una clase general resulta necesaria únicamente como dependencia de compilación, puede incorporarse bajo la categoría `CONDITIONAL`.

La presencia de ese código en el archive no implica que haya sido evaluado por el Paper 1.

#### EXCLUDE: artefactos locales y herramientas descargadas

No incluir:

`.git/`

`.formal-tools/`

`build/`

`target/`

`out/`

archivos temporales,

archivos de IDE,

locks locales,

caches,

binarios descargados de TLC o Alloy.

Las herramientas externas deben obtenerse mediante instrucciones reproducibles y versiones identificadas.

#### Regla de empaquetado

El artifact final no se construirá copiando indiscriminadamente todo el repositorio ni mediante un `git archive` sin selección.

F7 construirá un archive determinista a partir del contrato público aprobado, del inventario verificado en F4 y de los gates de integridad de F5 y F6.

F7 deberá producir como mínimo:

un README del artifact,

metadata de citación,

`ARTIFACT_MANIFEST.json`,

`SHA256SUMS.txt`,

un archive reproducible.

#### Relación con fases posteriores

8G-F3 separará explícitamente en el README raíz el software general DLT-Lab, el estudio científico Paper 1 y su artifact público.

8G-F4 localizará el bundle 8E, la evidencia raw, los resultados derivados y las dependencias necesarias.

8G-F5 cerrará la cadena de provenance.

8G-F6 verificará raw, manifests, análisis regenerado y smoke.

8G-F7 construirá el artifact determinista.

8G-F8 auditará el archive antes de preparar release y depósito persistente.

#### Gate 8G-F2

8G-F2 puede cerrarse cuando las categorías públicas están definidas,

el material editorial interno está explícitamente excluido,

el software general no se presenta como evidencia científica,

raw y derived están separados conceptualmente,

el bundle 8E está diferenciado del artifact final,

las dependencias externas no se incorporan como binarios sin auditoría,

los componentes todavía no verificados permanecen marcados como `PENDING_F4`,

no se inventan hashes, DOI, release ni URL persistente,

no se modifica el scientific freeze ni ningún resultado científico.
