### Auditoría de citas 8F-G8-A

#### Identificación

Fase:

`8F-G8-A`

Fecha:

`2026-09-07`

Objetivo:

Verificar que las citas usadas en Background and Related Work estén respaldadas por las entradas bibliográficas esperadas, por fuentes primarias verificadas en la matriz y por la estructura argumentativa congelada en 8F-G6.

#### Resultado global

- 15 claves citadas;
- 15 entradas BibTeX;
- 15 trabajos con `primary_source_verified: yes`;
- cero claves faltantes;
- cero entradas bibliográficas huérfanas;
- S10 Presto no respalda claims del borrador;
- S20 ZooKeeper no respalda claims del borrador;
- las tres familias técnicas contienen sus comparadores obligatorios;
- la auditoría DOI/eprint no detectó discrepancias.

#### Matriz de auditoría

| Paper | Clave BibTeX | DOI o identificador | Screening | Fuente primaria | Estado |
| --- | --- | --- | --- | --- | --- |
| S01 | `schultz2025mongodb` | `10.14778/3750601.3750626` | include-direct | yes | PASS |
| S02 | `cirstea2025traces` | `10.1007/978-3-031-77382-2_8` | include-direct | yes | PASS |
| S03 | `hackett2025tracelink` | `10.1145/3763128` | include-direct | yes | PASS |
| S04 | `cordy2023mutation` | `10.1145/3611643.3613080` | include-direct | yes | PASS |
| S05 | `konnov2025ethereum` | `arXiv:2501.07958` | include-direct | yes | PASS |
| S06 | `albassam2018chainspace` | `10.14722/ndss.2018.23241` | include-direct | yes | PASS |
| S07 | `kokoriskogias2018omniledger` | `10.1109/SP.2018.000-5` | include-direct | yes | PASS |
| S08 | `hong2023prophet` | `10.1109/INFOCOM53939.2023.10228939` | include-direct | yes | PASS |
| S09 | `du2024cslap` | `10.1038/s41598-024-64945-1` | include-direct | yes | PASS |
| S11 | `qi2024lightcross` | `10.1109/INFOCOM52122.2024.10621127` | include-direct | yes | PASS |
| S18 | `howard2025ccf` | `USENIX:NSDI25` | include-direct | yes | PASS |
| S19 | `foo2023choreographic` | `10.1007/978-3-031-35257-7_8` | include-direct | yes | PASS |
| S21 | `wang2023mocket` | `10.1145/3552326.3587442` | include-direct | yes | PASS |
| S22 | `gao2025imocket` | `10.1145/3728883` | include-core | yes | PASS |
| S23 | `bornholt2021s3` | `10.1145/3477132.3483540` | include-direct | yes | PASS |

#### Decisiones bibliográficas conservadoras

Chainspace y Prophet pueden producir warnings de BibTeX por ausencia del campo `pages`. Estos warnings no se tratan como errores. No se añaden rangos de páginas sin una fuente bibliográfica suficientemente fiable y consistente.

Los DOI de SEFM y Choreographic PlusCal contienen un guion bajo. En `references.bib` se conserva el escape LaTeX `\_` para que `elsarticle-num` genere un `.bbl` compilable; la auditoría normaliza ese escape antes de comparar el identificador.

#### Trazabilidad por subsección

`Sharded ledgers and cross-shard commit`: S06, S07, S08, S09 y S11.

`Formal analysis of distributed and blockchain protocols`: S01, S04 y S05.

`Specification validation and implementation-model conformance`: S02, S03, S18, S19, S21, S22 y S23.

#### Gate de cierre

8F-G8-A queda cerrada cuando:

- este archivo existe;
- el script de auditoría termina con código 0;
- `git diff --check` pasa;
- el manuscript continúa compilando sin errores, citas indefinidas ni referencias indefinidas;
- no aparecen cambios fuera de `manuscript/paper1/`.

#### Siguiente fase

`8F-G8-B: novelty and claim audit`
