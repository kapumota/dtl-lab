### Auditoría de Introduction 8F-H

#### Identificación

Fase:

`8F-H`

Fecha:

`2026-09-07`

#### Objetivo

Integrar una Introduction completa gobernada por el contrato CL-01..CL-06 y por el corpus bibliográfico congelado en 8F-G8.

#### Estructura argumental

1. problema de coordinación cross-shard;
2. papel complementario de formal methods, mutation assessment y trace conformance;
3. objeto de estudio: protocolo ejecutable de DTL-Lab;
4. gap acotado al comparator set;
5. RQ1-RQ4;
6. capas de evidencia y límites;
7. resumen cuantitativo de resultados;
8. reproducibilidad y límites;
9. organización del paper.

#### Gate científico

- `bounded property verification`: presente;
- `mutation-based property validation`: presente;
- `bounded implementation-model trace conformance`: presente;
- `verification-cost characterization`: presente;
- RQ1-RQ4: presentes;
- gap limitado al comparator set: presente;
- no priority claim global: preservado;
- no general refinement: preservado;
- no behavioral equivalence: preservado;
- no asymptotic/nonlinear law: preservado;
- same-host reproducibility boundary: preservado.

#### Gate bibliográfico

La Introduction utiliza únicamente claves ya presentes en `references.bib` y provenientes del corpus Related Work congelado.

#### Gate cuantitativo

Se preservan únicamente resultados ya establecidos:

- 1272 tareas programadas;
- RQ1: 420 medidas, 350 completadas, 70 censuradas;
- RQ2: 10/10 mutantes, MutationScore = 1.0;
- RQ3: 600 ejecuciones;
- reproducción: 10/10 gates y 32/32 hashes.

#### Estado

`PASS`

#### Siguiente fase

`8F-I: Conclusions`
