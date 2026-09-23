### Procedencia del artifact científico del Paper 1

#### Estado

Gate: 8G-F5

Estado: `READY_FOR_REVIEW`

#### Propósito

Este documento establece la procedencia verificable entre la campaña experimental, la evidencia raw, el análisis derivado, la reproducción independiente y la preparación del artifact para publicación.

La procedencia distingue explícitamente los enlaces criptográficos por SHA-256 de las relaciones históricas entre revisiones Git.

Los commits enumerados cumplen funciones diferentes y no deben interpretarse como una única revisión canónica.

#### Protocolo

Protocol ID:

`paper1-q3-v1`

#### Revisiones principales

Experimental baseline:

`45cb114d61b1df8c605c50700f3cc72d48d157fe`

Raw execution commit:

`248ff938c9f028745b5e370469e4796bc00755f7`

Independent reproduction source:

`6cd88c377afd23fee4998882f91142d71e7d963e`

Scientific manuscript freeze:

`06bea7de70971d5b22d705a2df19137122758c08`

Publication preparation base para 8G-F5:

`e499594d29317fbe1652c4ad7ac4124082f07aa9`

#### Ejecución raw definitiva

`environment.json` registra como commit de ejecución `248ff938c9f028745b5e370469e4796bc00755f7`.

Worktree tracked limpio durante la captura:

`true`

Tareas terminales:

`1272`

Resultados terminales:

`1272`

Conteos por estado:

```json
{
  "completed": 1188,
  "timeout": 84
}
```

#### Identificadores criptográficos de raw

SHA-256 de `plan.jsonl`:

`15c42ee169284eb0db6f4f2bef160784bf582e071e15de9e681b39793034d339`

SHA-256 de `plan-manifest.json`:

`e0295c6cbdfb5d0b216dd851dbd7b28a30a3e4967712820e87d720ba36492f45`

SHA-256 de `provenance.json`:

`32ffd82fbfa3699a1d344e3a6d48ea013476784a32da9c8e1e60c7e1350f1b6e`

SHA-256 de `environment.json`:

`bd054e2bf89ef3cf1778a7edbcc84c204f2a791cb79b3a3601c5e408954013fd`

SHA-256 de `raw-manifest.json`:

`eae45c54fce2c534c61226225a0c6d92a55fea46db2b50ef8985a730922675d7`

SHA-256 del archive raw preservado en 8E:

`d5b553de18d17da4c5b4278b4d13fe48e7e0898f7bbec494eece5e67f469b463`

#### Análisis derivado

SHA-256 de `derived-manifest.json`:

`7a2622726f96e209171096a1c50109600c3af7c0416b595cad1a836fbc5b889f`

Filas derivadas:

`1272`

Tablas:

`8`

Figuras:

`8`

Raw modificado por el análisis:

`false`

`derived-manifest.json` referencia mediante SHA-256 `raw-manifest.json`, `provenance.json`, `environment.json`, `plan.jsonl` y `plan-manifest.json`.

#### Bundle histórico 8E

Source commit:

`6cd88c377afd23fee4998882f91142d71e7d963e`

SHA-256 del bundle:

`d464888e9f3e5d8cc64ef5d22cc7b7c24f83e3853f5825f18f23de26adf6a6e6`

`bundle-manifest.json` registra el SHA-256 del archive raw y el SHA-256 del `derived-manifest.json` de referencia.

El bundle histórico sigue siendo evidencia de reproducción independiente y no se presume idéntico al artifact final.

#### Cadena criptográfica de evidencia

Los siguientes enlaces son verificables mediante los hashes almacenados en los manifests.

```text
plan.jsonl
15c42ee169284eb0db6f4f2bef160784bf582e071e15de9e681b39793034d339
        |
        | SHA-256 registrado
        v
raw-manifest.json
eae45c54fce2c534c61226225a0c6d92a55fea46db2b50ef8985a730922675d7
        |
        | input_sha256
        v
derived-manifest.json
7a2622726f96e209171096a1c50109600c3af7c0416b595cad1a836fbc5b889f
        |
        | SHA-256 registrado en bundle-manifest
        v
bundle 8E
d464888e9f3e5d8cc64ef5d22cc7b7c24f83e3853f5825f18f23de26adf6a6e6
```

`provenance.json`, `environment.json` y `plan-manifest.json` también están ligados a `raw-manifest.json` mediante SHA-256.

#### Lineage Git verificado

Las siguientes relaciones fueron comprobadas mediante `git merge-base --is-ancestor`.

```text
[1] Experimental baseline
45cb114d61b1df8c605c50700f3cc72d48d157fe
        |
        | ancestry Git verificado
        v
[2] Raw execution commit
248ff938c9f028745b5e370469e4796bc00755f7
        |
        | ancestry Git verificado
        v
[3] Independent reproduction source
6cd88c377afd23fee4998882f91142d71e7d963e
        |
        | ancestry Git verificado
        v
[4] Scientific manuscript freeze
06bea7de70971d5b22d705a2df19137122758c08
        |
        | ancestry Git verificado
        v
[5] Publication preparation base
e499594d29317fbe1652c4ad7ac4124082f07aa9
```

Esta relación Git no sustituye a la cadena SHA-256. Ambas evidencias son complementarias.

#### Continuidad hasta el submission

La cadena de publicación no termina en el scientific manuscript freeze. Desde la base de preparación actual continúa mediante gates todavía pendientes.

```text
publication preparation base
e499594d29317fbe1652c4ad7ac4124082f07aa9
        |
        | 8G-F5
        | cierre documental de provenance
        v
provenance closure
ESTE_DOCUMENTO
        |
        | 8G-F6
        | verificación ejecutable
        v
verification revision
PENDING
        |
        | 8G-F7
        | construcción determinista
        v
artifact candidate revision
PENDING
        |
        | 8G-F8
        | auditoría, release y depósito
        v
artifact-release revision
PENDING
        |
        | 8G-G1 a 8G-G6
        | cierre editorial
        v
submission candidate revision
PENDING
        |
        | 8G-H
        | freeze exacto del paquete enviado
        v
submission revision
PENDING
```

`ESTE_DOCUMENTO` representa la revisión que cerrará F5. Su hash Git no puede escribirse dentro del mismo commit sin crear una referencia circular.

El commit real que cierre F5 quedará registrado por Git y podrá incorporarse posteriormente al freeze exacto de 8G-H.

#### Relaciones verificadas

- experimental baseline -> raw execution commit: `PASS`
- raw execution commit -> independent reproduction source: `PASS`
- independent reproduction source -> scientific manuscript freeze: `PASS`
- scientific manuscript freeze -> publication preparation base: `PASS`
- plan -> raw manifest mediante SHA-256: `PASS`
- provenance -> raw manifest mediante SHA-256: `PASS`
- environment -> raw manifest mediante SHA-256: `PASS`
- raw manifest -> derived manifest mediante SHA-256: `PASS`
- raw archive -> bundle manifest mediante SHA-256: `PASS`
- derived manifest -> bundle manifest mediante SHA-256: `PASS`
- checksum externo del bundle 8E: `PASS`

#### Revisiones todavía pendientes

Verification revision:

`PENDING`

Artifact candidate revision:

`PENDING`

Artifact-release revision:

`PENDING`

Submission candidate revision:

`PENDING`

Submission revision:

`PENDING`

DOI:

`PENDING`

Persistent URL:

`PENDING`

#### Límites de 8G-F5

8G-F5 no vuelve a ejecutar las 1272 tareas.

8G-F5 no regenera todavía el análisis.

8G-F5 no ejecuta todavía el smoke científico.

8G-F5 no construye el artifact final.

8G-F5 no crea release, DOI ni URL persistente.

La verificación ejecutable corresponde a 8G-F6 y la construcción determinista corresponde a 8G-F7.

#### Gate de cierre

8G-F5 puede cerrarse cuando este documento pase la auditoría documental, no contenga placeholders no intencionales y el staged diff no modifique ciencia.
