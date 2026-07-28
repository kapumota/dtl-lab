### Cierre de conformidad de la Fase 7

#### Propósito

La Fase 7E integra la exportación de trazas, la función de abstracción, el replay TLC y el corpus negativo en un perfil científico único y reproducible.

El cierre usa la denominación `bounded implementation-model trace conformance`. No presenta los resultados como una prueba general de refinamiento, equivalencia total Java-TLA+ ni verificación completa de la implementación.

#### Baseline

La integración se construye sobre:

- Fase 7A: JSONL determinista de ejecuciones concretas;
- Fase 7B: función de abstracción tipada;
- Fase 7C: replay que usa TLC como oráculo;
- Fase 7D: diez mutaciones controladas con rechazo esperado.

Ninguno de esos componentes cambia su semántica en la Fase 7E.

#### Comando científico

```bash
make conformance-research
```

El comando ejecuta con una misma seed:

1. el catálogo de diez escenarios válidos;
2. el corpus de diez trazas corruptas;
3. la validación de aceptación y rechazo;
4. la integración de manifiestos;
5. la generación del artefacto científico.

#### Artefacto

La salida predeterminada es:

```text
results/conformance/research-v1/
```

Contiene:

```text
research-v1/
├── valid/
├── negative/
├── manifest.json
├── summary.json
├── summary.md
└── conformance_matrix.csv
```

#### Procedencia

`manifest.json` registra:

- commit fuente;
- commit ejecutado;
- referencia fuente;
- repositorio;
- evento y ejecución de GitHub Actions;
- seed;
- versión y SHA-1 de TLC;
- hashes SHA-256 de los manifiestos de entrada;
- conteos de aceptación, rechazo y diagnóstico.

#### Gate científico

La ejecución se considera correcta únicamente cuando:

- TLC acepta los diez escenarios válidos;
- TLC rechaza las diez trazas corruptas;
- los diez diagnósticos coinciden con su paso objetivo;
- ambos catálogos usan la misma seed;
- el JAR de TLC coincide con la versión fijada;
- los resultados integrados son deterministas;
- el workflow publica el artefacto de conformidad.

#### Respuesta a RQ3

Dentro de los escenarios, seeds, bounds y mutaciones declarados, las trazas válidas proyectadas son admitidas por los operadores de `CrossShardCommit.tla` y las trazas corruptas son rechazadas en el punto esperado.

Esta respuesta es acotada. No cubre todas las ejecuciones Java posibles, todas las topologías, todas las pérdidas de red ni una cantidad arbitraria de transferencias o validadores.

#### Release

El cierre técnico de la Fase 7 corresponde a:

```text
v1.1.0-rc.1
```

El tag se crea únicamente después de fusionar el PR, confirmar el CI de `main` y comprobar un árbol local limpio.
