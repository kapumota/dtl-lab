### DTL-LAB Paper 1: G2-P3, decisión sobre correspondencia interna y de frontera

**Revisión de referencia:** `ab9c86762108895526b190215953a746f920d341` (PR #40 fusionado en `main`). **G1:** `8ced907`.

#### Propósito

Cerrar la pregunta científica concreta de G2 sin rediseñar el protocolo V1 ni reabrir la campaña de 1.272 tareas. Este incremento NO afirma refinamiento de estados internos, consistencia distribuida, linealizabilidad ni recuperación después de caída de un proceso.

La correspondencia verificable tiene dos niveles que no deben mezclarse:

1. **Fronteras de operación:** G2-P0 comprueba estado de negocio de entrada/salida. G2-P1 verifica alcanzabilidad de cinco rutas abstractas en TLC. G2-P2 comprueba efectos Java, negativos y contención en una JVM. Los cuatro fallos restituyen el ledger y la sesión, pero pueden dejar observaciones diagnósticas.
2. **Estados internos:** la auditoría G1 registra en tres escenarios un débito parcial real seguido de rollback. V2.2 exige `DecideCommit` para ejecutar `ApplySrc` y no contiene una transición que restaure esa operación hacia el estado `Locked` después del débito. Por ello no se debe atribuir una correspondencia *uno-a-uno* entre esos estados intermedios sin redefinir justificadamente la abstracción.

#### Evidencia nueva G2-P3

- `V2DecisionWitnessTest`: **cinco** testigos nuevos desde `ShardManager` real, un commit y cuatro fallos. Comprueba el resultado de `V2IntermediateAudit`, la presencia del débito y del crédito, el rollback exacto y la ausencia de un testigo observado de decisión irrevocable. Exporta TSV tipado; no crea eventos ficticios.
- `verificar_decision_g2_p3.py`: construye un **observador TLA+ de historia fuera del modelo congelado**. `seenDebited` se activa cuando `srcState = "Debited"`. El observador comprueba mediante TLC, para las constantes de `correct_shared.cfg`, que el modelo correcto no retorna a `Locked` ni revoca su decisión después del débito.
- Control anti-vacuidad independiente: `ReachDebited` debe fallar por un contraejemplo explícito. Un PASS de invariantes sin alcanzabilidad del débito es insuficiente.

El generador copia el modelo V2.2 idéntico a un directorio temporal de salida y **no modifica** las 15 entradas congeladas del repositorio. Exige el SHA-256 `22aa41d...` y el SHA-1 del JAR TLC 1.7.4 fijado.

#### Interpretación del resultado

`PASS_LIMITADO_G2_P3` implica exclusivamente:

1. Cinco testigos Java revisados y clasificables bajo el contrato G2-P2.
2. TLC validó la propiedad histórica en el alcance finito `correct_shared.cfg` de V2.2, sin mutación.
3. TLC alcanzó un estado de débito, descartando vacuidad de esa propiedad por falta de activación.
4. Se documenta la **incompatibilidad con un mapeo de pasos internos que observe y conserve individualmente el débito**. No es una demostración de imposibilidad para todas las abstracciones concebibles, ni de refinamiento completo en fronteras.

`G2-P1 PASS_PILOTO` prueba alcanzabilidad de rutas abstractas, no equivalencia de cada paso Java. El historial de G1 es diagnóstico y append-only **en memoria**, no durable tras un crash. Los mutantes de G0-B y las 1.272 tareas V1 son conjuntos distintos.

#### Criterio de cierre de G2

Si G2-P3 supera el gate, puede **cerrarse la evaluación de conformidad de frontera con alcance limitado**, documentando expresamente la exclusión del refinamiento interno. Si la contribución científica necesita un refinamiento interno completo, el dictamen debe ser **NO GO para esa afirmación**; no introducir una decisión irrevocable artificial en Java ni editar V2.2 solo para conseguir un PASS.

A continuación G3 deberá medir detección diferencial real entre TLC, conformidad de frontera y oráculo Java, con mutantes alcanzados, equivalentes, detectados y no detectados, evitando atribuir a TLC una observación que el modelo no representa.

#### Ejecución

Desde el clon de `dtl-lab` actualizado y con una rama de investigación nueva:

```bash
bash scripts/research/paper1/g2_p3/ejecutar_g2_p3.sh "$HOME/dtl-g2-p3-evidencia"
cat "$HOME/dtl-g2-p3-evidencia/ESTADO.txt"
cat "$HOME/dtl-g2-p3-evidencia/DICTAMEN_G2_P3.json"
```

El ejecutor **no modifica archivos versionados**. Requiere el JAR TLC 1.7.4 instalado o la variable `TLA2TOOLS_JAR`. TLC se invoca con `-deadlock` para conservar el alcance de la campaña original, por lo que **NO verifica ausencia de deadlocks**.
