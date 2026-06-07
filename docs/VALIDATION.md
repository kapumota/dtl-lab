### Validación reproducible

#### Objetivo

Este documento describe la validación mínima reproducible de DLT-Lab como software ejecutable, testeable y verificable.

La validación no depende únicamente de badges visuales en el README. El badge principal de CI apunta a un workflow real de GitHub Actions y el badge de validación apunta a un workflow adicional que ejecuta `scripts/validate.sh`.

#### Comando principal

```bash
make validate
```

También puede ejecutarse directamente:

```bash
bash scripts/validate.sh
```

#### Qué se valida

- Estructura mínima del repositorio.
- Existencia de `README.md`, `LICENCE`, `CHANGELOG.md`, `pom.xml`, `src/main/java`, `src/test/java`, `specs/tla` y `specs/alloy`.
- Disponibilidad de Java y `javac`.
- Compilación opcional con Maven cuando `mvn` está disponible.
- Compilación por `javac` mediante los scripts del proyecto.
- Ejecución del `TestRunner` del proyecto.
- Ejecución de una demo focalizada de mempool para comprobar el CLI y el pipeline de simulación.
- Validación del flujo de seguridad mediante el workflow principal `java-ci.yml`.
- Validación formal estructural de especificaciones TLA+ y Alloy.
- Validación de salidas mínimas generadas por demo y verificación formal.
- Ausencia de artefactos binarios versionados como `build`, `target`, `.class`, `.jar`, `.war` o `.ear`.

#### Relación con los badges

El README muestra badges para resumir el estado técnico del proyecto. Los badges importantes son:

- `CI`: ejecuta el workflow principal `.github/workflows/java-ci.yml`.
- `validation`: ejecuta `.github/workflows/validation.yml`, que llama a `scripts/validate.sh`.
- `version`: identifica la versión declarada del laboratorio.
- `license`: declara la licencia MIT del proyecto.
- `Java`, `tests`, `security`, `formal` y `demo`: resumen capacidades verificables del proyecto.

Si `scripts/validate.sh` falla, el workflow de validación falla y el badge `validation` deja de mostrar un estado exitoso.

#### Alcance

Esta validación demuestra que DLT-Lab funciona como software ejecutable y reproducible. No reemplaza una auditoría criptográfica, una revisión formal completa con TLC/Alloy Analyzer ni una evaluación de seguridad productiva. El proyecto sigue siendo un laboratorio técnico y educativo para estudiar DLT, mempool, MEV, DeFi, consenso adversarial, sharding, seguridad runtime y verificación formal.
