### Modelo de fallos

#### Propósito

Este documento especifica los fallos que el Paper 1 pretende representar. Complementa `MODELO_DE_AMENAZAS.md`: el modelo de amenazas describe capacidades adversariales, mientras que este documento clasifica fallos operativos y de protocolo.

#### Fallos incluidos en el baseline

El baseline permite representar parcialmente:

- shard destino offline mediante validadores no disponibles;
- quorum insuficiente en origen;
- quorum insuficiente en destino;
- replay del mismo recibo;
- timeout de una sesión;
- identificador de sesión repetido;
- UTXO inexistente o bloqueado;
- monto inválido;
- transferencia entre el mismo shard.

#### Fallos de red previstos

Las fases posteriores deberán representar explícitamente:

- retraso de mensajes;
- pérdida de mensajes;
- duplicación de mensajes;
- reordenamiento de mensajes;
- entrega después del timeout;
- partición temporal entre shards;
- recuperación posterior de un shard.

#### Fallos de validadores previstos

El modelo podrá incluir:

- validador offline;
- quorum insuficiente;
- validador deshonesto que aprueba una operación inválida;
- validador deshonesto que rechaza una operación válida;
- respuestas inconsistentes dentro de una misma sesión.

La Fase 1 no define todavía un protocolo BFT completo ni reglas de slashing para el commit cross-shard.

#### Fallos de implementación previstos

Se estudiarán mutantes o inyecciones controladas que representen:

- crédito destino antes de validar el recibo;
- consumo repetido del recibo;
- commit después de abort;
- timeout sin liberación de fondos;
- omisión del quorum;
- excepción después del débito del origen;
- excepción después del crédito del destino;
- rollback incompleto;
- transición desde un estado terminal;
- asociación incorrecta entre recibo y transferencia.

#### Fallos fuera del alcance inicial

No se incluyen inicialmente:

- ruptura de SHA-256;
- falsificación de firmas mediante compromiso criptográfico;
- corrupción arbitraria de memoria;
- caída física permanente de todos los shards;
- ataques de denegación de servicio a escala de producción;
- consenso completo de una blockchain industrial;
- reorganizaciones profundas de la cadena durante el protocolo;
- MEV y manipulación económica del ordenamiento;
- contratos inteligentes reales.

#### Modelo fail-stop

Para la primera evaluación, los componentes offline se tratarán principalmente como fallos fail-stop:

- dejan de responder;
- no producen transiciones válidas mientras permanecen offline;
- pueden recuperarse en escenarios que lo declaren explícitamente.

Los fallos Byzantine se limitarán a mutantes concretos y comportamientos definidos, no a una capacidad adversarial ilimitada.

#### Persistencia y recuperación

El baseline mantiene el estado en memoria. Por ello:

- no se modela recuperación después de reiniciar el proceso;
- no se garantiza persistencia durable de sesiones;
- los resultados se limitan a una ejecución del simulador.

Una extensión de persistencia queda fuera del Paper 1 inicial salvo que resulte necesaria para responder las preguntas de investigación.

#### Regla de reproducibilidad

Todo escenario de fallo deberá registrar:

- identificador del escenario;
- configuración;
- seed cuando exista aleatoriedad;
- secuencia de eventos;
- estado inicial;
- estado final;
- propiedad esperada;
- resultado observado.
