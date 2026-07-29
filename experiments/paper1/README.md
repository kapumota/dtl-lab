### Experimentos del Paper 1

#### Propósito

Este directorio contiene el protocolo legible por máquina y el inventario ejecutable de la evaluación Q3.

La Fase 8A congela el diseño.

La Fase 8B implementa la infraestructura sin ejecutar la matriz definitiva.

#### Archivos congelados de Fase 8A

- `experiment-spec.json`: contrato experimental
- `configurations.csv`: combinaciones permitidas
- `seeds.txt`: treinta seeds definitivas

Estos archivos no cambian durante la Fase 8B.

#### Archivos de infraestructura

- `cases.json`: inventario derivado de propiedades, mutantes y trazas
- `result-schema-v1.json`: contrato de cada resultado raw

El inventario no agrega unidades experimentales fuera del protocolo congelado.

#### Validación del protocolo

```bash
make experiment-protocol
```

El comando valida consistencia, cobertura de RQ, hipótesis, factores, repeticiones, recursos, configuraciones y seeds.

#### Validación de infraestructura

```bash
make experiment-infrastructure
```

El comando construye el plan, valida hashes, ejecuta un smoke test temporal y comprueba reanudación.

#### Separación de fases

La Fase 8B construye infraestructura y contratos de salida.

La Fase 8C ejecutará la matriz definitiva con executors reales.

La Fase 8D generará análisis, tablas y figuras desde resultados raw.

Ningún resultado definitivo se almacena en este directorio.
