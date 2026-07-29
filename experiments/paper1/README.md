### Experimentos del Paper 1

#### Propósito

Este directorio contiene el protocolo legible por máquina, el inventario ejecutable y la traducción científica de perfiles para la evaluación Q3.

#### Archivos congelados de Fase 8A

- `experiment-spec.json`: contrato experimental
- `configurations.csv`: combinaciones permitidas
- `seeds.txt`: treinta seeds definitivas

Estos archivos no cambian durante la Fase 8C.

#### Archivos de infraestructura de Fase 8B

- `cases.json`: inventario derivado de propiedades, mutantes y trazas
- `result-schema-v1.json`: contrato de cada resultado raw

Estos archivos no agregan unidades experimentales fuera del protocolo congelado.

#### Traducción de Fase 8C

- `execution-profiles.json`: constantes TLC, scopes Alloy y tareas del smoke científico

La traducción convierte perfiles congelados en entradas ejecutables. No modifica modelos base ni crea configuraciones ajenas a `configurations.csv`.

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

#### Separación de fases

La Fase 8C genera resultados raw y su manifiesto de integridad.

La Fase 8D generará resultados derivados, tablas y figuras.

Ningún resultado definitivo se almacena dentro de `experiments/paper1`.
