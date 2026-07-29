### Experimentos del Paper 1

#### Propósito

Este directorio contiene el protocolo legible por máquina de la evaluación Q3.

La Fase 8A congela el diseño. No ejecuta la matriz definitiva ni almacena resultados finales.

#### Archivos

- `experiment-spec.json`: contrato experimental;
- `configurations.csv`: combinaciones permitidas;
- `seeds.txt`: treinta seeds definitivas.

#### Validación

```bash
make experiment-protocol
```

El comando valida consistencia, cobertura de RQ, hipótesis, factores, repeticiones, recursos, configuraciones y seeds.

#### Separación de fases

La Fase 8B implementará la infraestructura de ejecución.

La Fase 8C ejecutará la matriz definitiva.

La Fase 8D generará análisis, tablas y figuras.

Ningún resultado definitivo pertenece a este directorio durante la Fase 8A.
