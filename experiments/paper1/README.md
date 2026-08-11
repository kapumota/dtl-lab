### Experimentos del Paper 1

#### Propósito

Este directorio contiene el protocolo legible por máquina, el inventario ejecutable, la traducción científica de perfiles el contrato de análisis y el contrato de reproducción para la evaluación Q3.

#### Archivos congelados de Fase 8A

- `experiment-spec.json`: contrato experimental
- `configurations.csv`: combinaciones permitidas
- `seeds.txt`: treinta seeds definitivas

Estos archivos no cambian durante las fases 8C, 8D y 8E.

#### Archivos de infraestructura de Fase 8B

- `cases.json`: inventario derivado de propiedades, mutantes y trazas
- `result-schema-v1.json`: contrato de cada resultado raw

Estos archivos no agregan unidades experimentales fuera del protocolo congelado.

#### Traducción de Fase 8C

- `execution-profiles.json`: constantes TLC, scopes Alloy y tareas del smoke científico

La traducción convierte perfiles congelados en entradas ejecutables. No modifica modelos base ni crea configuraciones ajenas a `configurations.csv`.

#### Contrato de Fase 8D

- `analysis-spec.json`: reglas versionadas para derivados, estadística, tablas, figuras y amenazas a la validez

El contrato de análisis no cambia factores, configuraciones, seeds, repeticiones ni límites de recursos. Solo materializa el plan estadístico congelado en Fase 8A.

#### Contrato de Fase 8E

- `reproduction-spec.json`: ambiente limpio, smoke representativo, comparación SHA-256 e incidencias

El contrato de reproducción no repite la matriz completa. Verifica el procedimiento en otro usuario o máquina y regenera el análisis desde el raw respaldado.

#### Validación del protocolo

```bash
make experiment-protocol
```

#### Validación de infraestructura

```bash
make experiment-infrastructure
```

#### Validación científica estructural

```bash
make experiment-scientific-structure
```

#### Smoke científico

```bash
make experiment-scientific-smoke
```

El smoke ejecuta seis tareas reales. Sus tiempos no pertenecen al análisis principal.

#### Matriz definitiva

```bash
make experiment-matrix
```

La matriz completa contiene 1272 tareas y solo se ejecuta en Linux nativo dedicado.

#### Validación del análisis

```bash
make experiment-analysis-structure
```

El gate usa un fixture sintético temporal y comprueba determinismo, estilo e inmutabilidad de `raw`.

#### Análisis definitivo

```bash
make experiment-analysis
```

Este comando valida la matriz definitiva y genera resultados derivados, ocho tablas y ocho figuras.

#### Validación de reproducción

```bash
make experiment-reproduction-structure
```

#### Reproducción independiente

```bash
make experiment-reproduction \
  REPRODUCTION_BUNDLE=/ruta/paper1-q3-v1-reproduction.tar.gz
```

#### Separación de fases

La Fase 8C genera resultados raw y su manifiesto de integridad.

La Fase 8D genera resultados derivados, tablas y figuras sin volver a ejecutar herramientas formales.

La Fase 8E ejecuta un smoke representativo, regenera el análisis y compara hashes sin modificar 8C o 8D.

Ningún resultado definitivo se almacena dentro de `experiments/paper1`.
