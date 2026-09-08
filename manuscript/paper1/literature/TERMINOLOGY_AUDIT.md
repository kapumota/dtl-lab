### Auditoría de consistencia terminológica 8F-G8-C

#### Identificación

Fase:

`8F-G8-C`

Fecha:

`2026-09-07`

Objetivo:

Fijar una terminología estable para las cuatro capas de evidencia del Paper 1 y eliminar variantes que puedan alterar el alcance interpretativo de RQ1-RQ4.

#### Terminología canónica

| RQ | Término canónico | Uso |
| --- | --- | --- |
| RQ1 | `bounded property verification` | Evidencia de propiedades en configuraciones finitas completadas |
| RQ2 | `mutation-based property validation` | Sensibilidad del conjunto de propiedades al catálogo de mutantes predefinido |
| RQ3 | `bounded implementation-model trace conformance` | Correspondencia de trazas observables dentro del catálogo y los bounds evaluados |
| RQ4 | `verification-cost characterization` | Caracterización empírica de costo dentro de cada herramienta |

#### Reglas de terminología

- `bounded implementation-model trace conformance` es la denominación oficial de RQ3;
- no usar `refinement` como resultado establecido;
- no usar `behavioral equivalence` como resultado establecido;
- `implementation-to-model` puede describir mecanismos de trabajos citados, pero no es la denominación de nuestro RQ3;
- `mutation-based property validation` es la denominación oficial de RQ2;
- `verification-cost characterization` no implica una ley de complejidad;
- `within-tool` conserva el alcance de las comparaciones de costo;
- `warm-up` es la forma ortográfica utilizada en el manuscrito.

#### Resultado global

- RQ1 usa `bounded property verification`;
- RQ2 usa `mutation-based property validation`;
- RQ3 usa `bounded implementation-model trace conformance`;
- RQ4 usa `verification-cost characterization`;
- `warm-up` quedó normalizado;
- no permanecen variantes no canónicas de auto-descripción en las secciones 04 a 09;
- las expresiones `refinement` y `behavioral equivalence` se conservan únicamente cuando describen literatura previa o delimitan negativamente nuestros claims.

#### Gate de cierre

8F-G8-C queda cerrada cuando las cuatro expresiones canónicas aparecen en Research Methodology, las variantes no canónicas están ausentes, `warm-up` está normalizado, los límites sobre refinement y behavioral equivalence permanecen, `git diff --check` pasa y el manuscrito compila sin errores, citas indefinidas ni referencias indefinidas.

#### Siguiente fase

`8F-G8-D: orthography and symbols audit`
