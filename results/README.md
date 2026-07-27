### Resultados de investigación

#### Propósito

Este directorio almacenará resultados científicos del Paper 1 o instrucciones para regenerarlos.

La Fase 0 no incorpora resultados experimentales. No se deben presentar tablas vacías como evidencia obtenida.

#### Diferencia con `reports`

- `reports/` contiene salidas operativas de demos, seguridad y validación del software existente.
- `results/` contendrá evidencia científica asociada a configuraciones, herramientas y commits identificables.

#### Estructura prevista

- `formal/raw/`: logs originales de TLC y Alloy.
- `formal/processed/`: CSV y resúmenes generados por scripts.
- `formal/counterexamples/`: contraejemplos versionados.
- `formal/traces/`: trazas Java usadas en conformidad.
- `formal/tables/`: tablas derivadas para el manuscrito.

#### Reglas

- Conservar los datos raw.
- Generar resultados procesados mediante scripts.
- Registrar hashes, versiones y configuración.
- No editar manualmente cifras destinadas al paper.
- Diferenciar claramente resultados piloto de resultados definitivos.
