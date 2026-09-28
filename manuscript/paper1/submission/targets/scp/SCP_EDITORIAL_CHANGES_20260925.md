### Cambios editoriales del candidato SCP

#### Base y alcance

Base: `e41ef499a645107dbbae071508464e6183669a6f`.
Rama: `paper1/cierre-editorial-scp-final`.
Scientific freeze: `06bea7de70971d5b22d705a2df19137122758c08`.

La versión condensada de main se conserva. No se ejecutaron experimentos,
smoke científico, regeneración estadística ni nueva reproducción.

#### Correcciones

- Target SCP y título singular en la entrada LaTeX.
- URL pública real de DTL-Lab en Data Availability y cover letter.
- Restauración de las dos URLs bibliográficas corregidas históricamente.
- Codificación de los dos DOI compatible con el estilo y enlaces del PDF.
- Aclaración de `EventuallyReleasedAfterTimeout` como invariante de estado,
  esencialmente `Aborted -> fundsReleased`, sin liveness temporal general.
- Figura editorial vectorial de las capas de evidencia, con TLA+ y Alloy
  en paralelo y leyenda que descarta equivalencia, refinement o prueba acumulativa.
- Ajuste de composición de la proyección Java y fuentes vectoriales legibles.
- Declaración de IA existente incorporada al PDF, pendiente de confirmación final humana.
- Constructor editorial que no llama scripts científicos.
- Recuperación de seis documentos del cierre histórico en una carpeta separada.

#### Integridad y G4

`SCP_CROSSCHECK_20260925.json` registra la comparación de todas las cifras
tabuladas con el ZIP histórico verificado, perfiles, recursos, versiones,
seeds, diseño de tareas, continuidad Git y reproducción documentada.

Los árboles `src`, `specs`, `experiments/paper1`, `scripts/experiments`,
`scripts/formal` y `scripts/conformance` no presentan diferencias respecto
del scientific freeze al inicio de esta tarea y no se modifican aquí.

El estado G4 es documental. Los bytes del bundle externo 8E no están en
este entorno. Se conserva la verificación histórica F6 sin presentar una
nueva verificación del raw. F7 y F8 permanecen diferidos y no bloqueantes.

#### Requisitos editoriales consultados el 25 de septiembre de 2026

La guía oficial fue localizada y sus fragmentos indexados confirman
1 a 7 keywords y que se anima a declarar disponibilidad de datos:

https://www.sciencedirect.com/journal/science-of-computer-programming/publish/guide-for-authors

El acceso al texto completo devolvió HTTP 403 mediante búsqueda y
`Site Unavailable` en el navegador. No se afirma haber comprobado íntegramente
la guía ni los campos actuales de Editorial Manager. La comprobación final
de requisitos del portal queda pendiente antes de cerrar G3.

La documentación oficial de Elsevier respalda los highlights separados,
3 a 5 entradas, cada una con un máximo de 85 caracteres:

https://www.elsevier.support/publishing/answer/how-do-i-include-highlights-with-my-manuscript

Se mantiene `Highlights.txt` como archivo editable separado. No se impone
un DOI, depósito o release inexistente, ni un límite nuevo de páginas.

#### Dependencias editoriales

Clase y estilo obtenidos del repositorio de su mantenedor:
https://github.com/STM-Document-Engineering/elsarticle

Commit: `04aee43a9049051128c5d58b617dcd05e34df8b4`.
Se incluyen en el ZIP junto con su licencia cuando está disponible.
El constructor registra sus SHA-256 y los de cada fuente utilizada.

#### Mejoras diferidas

`POST-SUBMISSION / REVIEWER-DRIVEN`: segundo host físico, bounds adicionales,
catálogos mayores y pruebas generativas de trazas.

`PAPER 2 / PHASE 9`: segundo protocolo, liveness temporal general, fairness,
invariantes inductivas, refinement formal e integración de blockchain de producción.

Ninguna de estas mejoras bloquea este candidato.
