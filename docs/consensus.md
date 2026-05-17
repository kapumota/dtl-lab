### Consenso avanzado en DLT-Lab

Esta fase modela consenso distribuido con una red de confianza dirigida. La relacion `sender -> receiver` significa que el **receiver** escucha propuestas del sender.

#### Tipos de nodos

```text
Honesto: retransmite transacciones conocidas y acepta las que superan un umbral de votos.
Censor: retransmite todo excepto una transaccion objetivo.
Equivocador: envia vistas diferentes a seguidores diferentes.
Silencioso: no ayuda a propagar propuestas.
```

#### Metricas por ronda

Cada ronda exporta:

```text
- cantidad de mensajes
- transacciones unicas propagadas
- grupo honesto mayoritario
- ratio de acuerdo honesto
- salidas honestas que contienen la transaccion censurada
- cantidad de grupos de consenso observados.
```

El archivo generado es:

```text
reports/consensus_rounds.csv
```

#### Visualizacion

La red se exporta en dos formatos:

```text
reports/consensus_network.txt
reports/consensus_network.dot
```

El archivo DOT puede renderizarse con Graphviz:

```bash
dot -Tpng reports/consensus_network.dot -o reports/consensus_network.png
```

#### Limitacion

Esta fase no pretende ser un protocolo BFT completo. Es un laboratorio inicial para observar efectos de confianza, censura, equivocacion y propagacion de transacciones.
