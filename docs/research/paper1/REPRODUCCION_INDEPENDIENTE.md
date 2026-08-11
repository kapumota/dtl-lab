### Reproducción independiente de Fase 8E

#### Propósito

La Fase 8E verifica que una persona o ambiente distinto pueda ejecutar el artefacto siguiendo únicamente las instrucciones versionadas del repositorio.

La reproducción valida instalación, pruebas, smoke científico, regeneración del análisis y comparación de resultados. La matriz definitiva de 1272 tareas no se vuelve a ejecutar.

#### Frontera con Fase 8D

La Fase 8D conserva el contrato estadístico y genera datasets, tablas y figuras desde resultados raw validados.

La Fase 8E no modifica:

- `analysis-spec.json`
- scripts estadísticos de Fase 8D
- resultados raw
- resultados derivados de referencia
- modelos TLA+
- modelos Alloy
- implementación Java
- plan de 1272 tareas
- configuraciones y seeds congeladas

La Fase 8E agrega empaquetado, verificación, ejecución limpia, comparación de hashes e informes de incidencias.

#### Qué significa ambiente limpio

El ambiente limpio debe comenzar con un clon nuevo del repositorio y sin cambios rastreados.

Se reconocen tres niveles:

- preferido: usuario distinto y máquina Linux distinta
- aceptable: usuario distinto en la misma máquina Linux
- funcional: WSL o máquina virtual para validar instalación, smoke y hashes

WSL y virtualización no se usan para reinterpretar tiempos o memoria de la matriz definitiva. Las métricas principales permanecen asociadas al host Linux nativo registrado en Fase 8C.

#### Artefacto de reproducción

El artefacto externo contiene:

```text
paper1-q3-v1-reproduction/
  bundle-manifest.json
  checksums.sha256
  raw/
    paper1-q3-v1.tar.gz
    paper1-q3-v1.tar.gz.sha256
  reference/
    derived/
    tables/
    figures/
```

El respaldo raw no contiene directorios `*.tlc-meta`. Sí conserva los 1272 resultados terminales, sus payloads científicos, el plan, el ambiente, la procedencia y el manifiesto raw.

La referencia contiene los resultados de Fase 8D que deben regenerarse byte por byte.

#### Preparación en la máquina de origen

La creación del artefacto se realiza desde un commit limpio que contenga Fase 8E.

```bash
make experiment-reproduction-bundle \
  RAW_ARCHIVE=/home/project/backups/dtl-lab/paper1-q3-v1-20260804T094105Z.tar.gz \
  RAW_CHECKSUM=/home/project/backups/dtl-lab/paper1-q3-v1-20260804T094105Z.tar.gz.sha256 \
  REPRODUCTION_BUNDLE=/home/project/backups/dtl-lab/paper1-q3-v1-reproduction.tar.gz
```

El comando verifica el checksum del respaldo raw, valida el manifiesto derivado, copia datasets, tablas y figuras, y genera un TAR reproducible con checksum SHA-256.

#### Transferencia a otro ambiente

El artefacto y su checksum se copian a la máquina reproductora.

```bash
scp \
  /home/project/backups/dtl-lab/paper1-q3-v1-reproduction.tar.gz \
  /home/project/backups/dtl-lab/paper1-q3-v1-reproduction.tar.gz.sha256 \
  usuario@maquina-reproductora:/home/usuario/reproduction/
```

En la máquina receptora se verifica primero el checksum:

```bash
cd /home/usuario/reproduction
sha256sum -c paper1-q3-v1-reproduction.tar.gz.sha256
```

#### Clonación desde cero

```bash
mkdir -p /home/usuario/reproduction/work
cd /home/usuario/reproduction/work
git clone https://github.com/kapumota/dtl-lab.git
cd dtl-lab
```

El commit esperado está registrado en `bundle-manifest.json`. El runner detiene la reproducción si el clon usa otro commit.

#### Instalación siguiendo el README

El README principal enumera los paquetes del sistema y los comandos para instalar TLC y Alloy con versiones verificadas.

La preparación versionada se ejecuta con:

```bash
make experiment-reproduction-prepare
```

Este target reutiliza los instaladores existentes. No introduce un segundo mecanismo de instalación.

#### Ejecución independiente

```bash
make experiment-reproduction \
  REPRODUCTION_BUNDLE=/home/usuario/reproduction/paper1-q3-v1-reproduction.tar.gz \
  REPRODUCTION_OUTPUT=/home/usuario/reproduction/results/paper1-q3-v1
```

El flujo ejecuta:

1. validación del ambiente
2. verificación y extracción segura del artefacto
3. verificación del commit fuente
4. instalación verificada de TLC y Alloy
5. `make validate`
6. smoke científico de seis tareas
7. extracción del respaldo raw
8. regeneración completa de resultados derivados
9. comparación SHA-256 de datasets, tablas y figuras
10. generación del informe y registro de incidencias

#### Parte representativa regenerada

La reproducción no repite las 1272 ejecuciones formales porque ese costo no es necesario para validar el procedimiento editorial.

Se regeneran dos capas:

- seis tareas reales del smoke científico, con rutas representativas de TLC, Alloy y conformidad
- todo el análisis de Fase 8D a partir del raw respaldado, incluyendo ocho tablas y ocho figuras

Esto distingue reproducibilidad funcional de repetición completa del experimento de rendimiento.

#### Comparación de hashes

La comparación exige igualdad SHA-256 para cada archivo dentro de:

```text
reference/derived
reference/tables
reference/figures
```

Una diferencia, archivo ausente o archivo inesperado produce una incidencia y hace fallar el gate.

#### Registro de incidencias

El directorio de salida contiene:

```text
reproduction-report.json
reproduction-report.md
incidents.csv
environment.json
steps.tsv
logs/
comparison/
regenerated/
```

Cada paso conserva stdout, stderr y código de salida.

`incidents.csv` registra errores automáticos y advertencias del ambiente. Las incidencias manuales se agregan en una nueva fila sin modificar los resultados regenerados.

#### Interpretación

Un estado `reproducido` significa:

- clon y commit correctos
- ambiente compatible
- validación general correcta
- smoke científico correcto
- análisis regenerado correctamente
- todos los hashes coincidentes
- ninguna incidencia crítica pendiente

No significa que una segunda máquina haya repetido las 1272 mediciones de tiempo y memoria.

#### Gate estructural

```bash
make experiment-reproduction-structure
```

El gate:

- congela los contratos centrales de Fase 8D
- valida sintaxis Python, Bash y JSON
- comprueba las reglas de estilo documental
- crea dos artefactos sintéticos y exige hashes iguales
- verifica extracción segura
- comprueba una comparación correcta
- comprueba que una modificación conocida sea detectada
- confirma que los resultados experimentales existentes no cambien

#### Gate de cierre

La Fase 8E puede cerrarse cuando:

- el gate estructural pasa
- el artefacto se crea desde un commit limpio
- otro usuario o máquina completa el procedimiento
- el smoke científico pasa
- los datasets, tablas y figuras coinciden por SHA-256
- el informe declara estado `reproducido`
- las incidencias quedan documentadas y resueltas o justificadas
