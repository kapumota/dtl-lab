### Paper 1: continuidad de G0-B, G1 y G2, 20 de noviembre de 2026

#### Revisión de procedencia

- Repositorio: `kapumota/dtl-lab`.
- Base anterior a G1: `0b45ce449ba82262058152f0c7bd0247166fbad5`.
- G1, journal y oráculo UTXO: `8ced907` (ejecutado y confirmado en Ubuntu).
- G2-P0, G2-P1 y G2-P2: comprobar los SHA concretos mediante `git log --oneline 8ced907..HEAD`.
- La campaña histórica `paper1-q3-v1` de 1.272 tareas **no** se volvió a ejecutar por estos gates. No atribuir a V1 resultados de V2.2.

#### Resultados confirmados por ejecución en Ubuntu

| Compuerta | Resultado | Alcance |
| --- | --- | --- |
| G1 | PASS | Journal append-only en memoria, conservación de UTXO, cuatro fallos y rollback con pruebas integradas. |
| G0-B V2.2 | 52/52 | Modelo TLA+ finito, alcanzabilidad y mutantes con contraejemplos esperados. |
| G2-P0 | PASS | Proyección de fronteras de operación Java. |
| G2-P1 | PASS_PILOTO (5/5) | Alcanzabilidad en TLC de rutas abstractas generadas a partir de testigos Java. |
| G2-P2 | PASS_P2_PILOTO | Auditoría de etapas intermedias, negativos y contención dentro de una sola JVM. |
| G2 completo | ABIERTO | No hay refinamiento interno Java–V2 demostrado. |
| G3 | PENDIENTE | Mutantes Java diferenciales después de definir los detectores válidos. |

SHA-256 del modelo reportado en G0-B / G2-P1: `22aa41d3767d0ff42b9dfeae9b0f103a5baaa4bc66fe7934cbf440c04e9401e7`.
SHA-256 de testigos G2-P1 reportado: `787c682f2b5ba8eda53086cda2258f8706eb7dfa023575b3f3e754192d9d3eb9`.

#### Restricciones científicas y problemas abiertos

1. Los `52/52` mezclan propiedades del modelo correcto, alcanzabilidad, tipos y mutantes con fallos esperados; no son 52 propiedades universales satisfechas. TLC se ejecutó con `-deadlock`, por lo que no se comprobó ausencia de deadlocks.
2. Los cuatro rollbacks de G2-P1 proyectan rutas abstractas equivalentes en sus fronteras. No reproducen individualmente el historial interno de cada fallo en TLC.
3. G2-P2 observa débito, cambio y crédito, pero el Java actual no ofrece un testigo de decisión irrevocable equiparable sin más a `DecideCommit` de V2.2.
4. El rollback puede ser *stutter* en la proyección del estado de negocio. El journal y las observaciones de diagnóstico conservan información y no necesariamente hacen *stutter*.
5. La competencia de G2-P2 ocurre dentro de `ShardManager` en una sola JVM, no es una prueba de consenso, concurrencia entre procesos, recuperación tras crash, linealizabilidad ni atomicidad distribuida.
6. G1 contiene un registro append-only en memoria, no durable ante fallo de proceso.

#### Ejecución desde este repositorio

```bash
bash scripts/research/paper1/verificar_java_g1_g2.sh "$HOME/dtl-gates-java-evidencia"
```

La instrucción anterior vuelve a ejecutar regresión y pruebas Java G1/G2, pero **no** reproduce por sí sola la comprobación formal de 52 casos, ni el replay de cinco rutas con TLC. Las fuentes textuales del modelo V2.2 y sus configuraciones están copiadas sin cambios en `specs/tla/research/paper1_v2_2/`, con SHA-256 propios. Para el replay formal completo siguen siendo necesarios el paquete V2.2 integral fijado y los ejecutores G0-B/G2-P1 correspondientes, conservados como artefactos experimentales externos. Deben conservarse sus manifiestos originales y el JAR TLC de versión fijada. Nunca interpretar un `PASS_JAVA_GATES` como `PASS G2` integral.

#### Evidencia breve archivada

Si existe `docs/research/paper1/evidencia-gates-2026-11-20/`, se depositan allí únicamente resúmenes seleccionados y sus SHA-256; los logs completos siguen en los directorios de ejecución en Ubuntu y deben preservarse para la reproducción independiente. No subir tokens, llaves privadas, rutas personales innecesarias ni binarios de herramientas.

#### Siguiente decisión

Cerrar o acotar con justificación la correspondencia entre la decisión de Java y `DecideCommit`, ampliar las trazas negativas y los rechazos no representables y ejecutar el replay formal de efectos internos en vez de inferir conformidad a partir de alcanzabilidad. Registrar las divergencias sin corregir V2.2 artificialmente. Después, diseñar G3 con criterios explícitos de activación, equivalencia y detección de mutantes Java.
