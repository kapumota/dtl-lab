### Cierre de la Fase 8E

#### Propósito

Este documento registra el cierre de la Fase 8E del Paper 1 después de completar una reproducción independiente del protocolo `paper1-q3-v1`.

La Fase 8E valida que un usuario distinto pueda obtener un clon limpio del repositorio, preparar las herramientas versionadas, ejecutar la validación general y el smoke científico, regenerar el análisis de Fase 8D desde el respaldo raw y comprobar los resultados mediante SHA-256.

La matriz definitiva de 1272 tareas no se vuelve a ejecutar en esta fase.

#### Artefacto definitivo de reproducción

El artefacto utilizado para la reproducción final queda identificado por:

```text
protocol_id:
paper1-q3-v1

source_commit:
6cd88c377afd23fee4998882f91142d71e7d963e

bundle_sha256:
d464888e9f3e5d8cc64ef5d22cc7b7c24f83e3853f5825f18f23de26adf6a6e6
```

El `source_commit` corresponde al estado de `main` que contiene la infraestructura de reproducción independiente y la corrección del aislamiento de archivos temporales detectada durante el primer intento.

El bundle definitivo permanece externo al repositorio Git y no se modifica durante la reproducción.

#### Ambiente de reproducción

La reproducción independiente final se ejecutó con:

```text
host:
kapumota

usuario:
reproducer

sistema:
Linux x86_64

WSL:
no

virtualización:
ninguna

Java:
OpenJDK 17.0.19

Python:
3.12.3
```

El procedimiento comenzó desde un clon nuevo del repositorio y sin `.formal-tools`.

TLC 1.7.4 y Alloy 6.2.0 fueron preparados nuevamente mediante los instaladores versionados del proyecto.

#### Primer intento R1

El primer intento independiente utilizó un usuario diferente en el mismo host Linux.

El bundle del primer intento estaba asociado al commit:

```text
64605ab736ac181c11d6dc9b00375e3cd04c81e0
```

y tenía SHA-256:

```text
1c659170ca53da1519e78cf6d1516ed84a653e4aeb748ce2727c220138bb994f
```

El intento detectó una incidencia operacional en `scripts/validate.sh`.

La validación utilizaba nombres temporales globales fijos dentro de `/tmp`. Archivos creados previamente por otro usuario produjeron errores de permisos durante `make validate`.

A pesar de esta incidencia:

* el ambiente fue compatible
* el bundle fue verificado correctamente
* el commit fuente coincidió
* TLC y Alloy se instalaron correctamente
* el smoke científico pasó
* el respaldo raw se extrajo correctamente
* el análisis de Fase 8D se regeneró correctamente
* los 32 artefactos comparados coincidieron por SHA-256.

Por tanto, la incidencia no alteró los resultados científicos.

#### Resolución de la incidencia

La incidencia fue corregida mediante el PR #24.

La corrección:

* reemplazó las rutas temporales globales por un directorio privado creado mediante `mktemp -d`
* agregó limpieza automática mediante `trap`
* evitó colisiones entre usuarios y ejecuciones concurrentes
* agregó un gate estructural para impedir la reintroducción de rutas fijas `/tmp/dtl_*`

La condición original fue reproducida deliberadamente creando los antiguos archivos temporales con otro propietario y permisos restrictivos. Después de la corrección, `make validate` terminó correctamente bajo esa condición.

El merge de la corrección produjo el commit:

```text
6cd88c377afd23fee4998882f91142d71e7d963e
```

#### Segundo intento R1

El segundo intento comenzó nuevamente desde un clon limpio y utilizó el bundle definitivo asociado al commit corregido.

Todos los gates terminaron correctamente:

```text
environment          correcto
bundle_verify        correcto
source_commit        correcto
install_tlc          correcto
install_alloy        correcto
validate             correcto
scientific_smoke     correcto
raw_extract          correcto
regenerate_analysis  correcto
compare_hashes       correcto
```

El informe final declaró:

```text
status:
reproducido

incidencias:
0
```

#### Comparación de resultados

La comparación exacta de los resultados regenerados produjo:

```text
archivos comparados:
32

archivos coincidentes:
32

archivos diferentes:
0
```

La reproducción confirmó por SHA-256 los datasets derivados, tablas y figuras de referencia de la Fase 8D.

#### Interpretación

La Fase 8E demuestra reproducibilidad funcional del artefacto bajo un usuario independiente y un clon limpio.

La reproducción no constituye una segunda medición completa de las 1272 tareas de rendimiento. Los tiempos y memoria utilizados para las preguntas experimentales permanecen asociados a la ejecución definitiva de Fase 8C.

Una reproducción adicional en otra máquina Linux o mediante WSL puede incorporarse posteriormente como evidencia complementaria, pero no constituye un requisito para el cierre de esta fase.

#### Estado de cierre

Se verificaron los criterios establecidos para la Fase 8E:

* gate estructural correcto
* bundle construido desde un commit limpio
* reproducción realizada por un usuario distinto
* clon y commit correctos
* herramientas versionadas preparadas desde cero
* validación general correcta
* smoke científico correcto
* análisis de Fase 8D regenerado desde raw
* 32 de 32 artefactos coincidentes por SHA-256
* cero diferencias
* cero incidencias pendientes
* informe final con estado `reproducido`.

Estado final:

```text
FASE 8E: CERRADA
```

La siguiente etapa del Paper 1 es la Fase 8F, dedicada a la construcción y revisión del manuscrito.
