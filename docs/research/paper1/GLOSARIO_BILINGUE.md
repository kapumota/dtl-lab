### Glosario bilingüe

#### Propósito

El repositorio mantiene la documentación de investigación en español y conserva los términos técnicos canónicos en inglés cuando son necesarios para el paper, el código o las herramientas.

#### Términos del protocolo

| Español | Inglés canónico | Uso recomendado |
|---|---|---|
| commit atómico | atomic commit | Nombre del protocolo y de la propiedad central |
| transferencia cross-shard | cross-shard transfer | Operación que mueve valor entre shards |
| shard origen | source shard | Shard que bloquea o debita el UTXO |
| shard destino | destination shard | Shard que valida el recibo y acredita valor |
| recibo cross-shard | cross-shard receipt | Evidencia asociada a una transferencia |
| bloqueo de fondos | funds locking | Estado temporal del UTXO origen |
| liberación de fondos | funds release | Recuperación del UTXO después de abort o timeout |
| quorum | quorum | Cantidad mínima de aprobaciones |
| estado terminal | terminal state | Estado que no admite nuevas transiciones |
| tiempo de espera | timeout | Condición de expiración de una sesión |

#### Términos de verificación

| Español | Inglés canónico | Uso recomendado |
|---|---|---|
| propiedad de seguridad | safety property | Propiedad que excluye estados incorrectos |
| propiedad de vivacidad | liveness property | Propiedad que exige progreso bajo supuestos explícitos |
| invariante | invariant | Propiedad que debe mantenerse en estados alcanzables |
| model checking | model checking | Exploración automática de estados |
| espacio de estados | state space | Conjunto de estados explorados |
| contraejemplo | counterexample | Ejecución que viola una propiedad |
| bound | bound | Límite finito de la exploración |
| fairness | fairness | Supuesto sobre habilitación y ejecución de acciones |
| refinamiento | refinement | Relación entre una implementación y una especificación |
| conformidad de trazas | trace conformance | Comparación entre trazas observadas y acciones permitidas |
| mutante | mutant | Variante defectuosa creada de forma controlada |

#### Términos de reproducibilidad

| Español | Inglés canónico | Uso recomendado |
|---|---|---|
| artefacto reproducible | reproducible artifact | Código, configuración y resultados regenerables |
| datos crudos | raw data | Salida sin transformación manual |
| resultados procesados | processed results | Tablas derivadas mediante scripts |
| manifiesto de ejecución | execution manifest | Configuración y versiones de una ejecución |
| semilla | seed | Valor que controla una ejecución pseudoaleatoria |

#### Convenciones

- Las firmas de funciones y clases nuevas se escriben en inglés.
- Los comentarios y cadenas de texto se escriben en español.
- Las propiedades formales conservan nombres como `NoValueLoss` y `AtomicCommit`.
- El manuscrito podrá usar la traducción inglesa y citar este glosario para mantener correspondencia con el artefacto.
