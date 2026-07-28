### Matriz de trazabilidad

#### Matriz principal

| Propiedad o elemento | Evidencia Java | Evidencia formal | Brecha restante | Fase |
|---|---|---|---|---|
| estado de sesión | `CrossShardStatus`, `TransitionTable`, eventos | `status[t]`, `terminalStatus[t]`, estados Alloy | cerrada mediante abstracción tipada | 7B |
| historial de sesión | `ProtocolEvent` ordenado | secuencia abstracta de estados | cerrada mediante JSONL determinista | 7A |
| bloqueo de origen | protocolo atómico y escenarios concurrentes | `locked[t]`, `LockTransfer` | cerrada mediante mapeo y replay | 7B y 7C |
| recibos | creación, entrega, consumo y replay | `receiptOwner`, `receiptUseCount`, mensajes | cerrada con copia canónica documentada | 7B |
| crédito destino | UTXO destino creado por commit | `destinationCredit[t]` | cerrada mediante estado abstracto tipado | 7B |
| votos y quorum | validadores disponibles y quorum | `votes[t]`, `QuorumRequired` | cerrada mediante votos canónicos y mutación | 7B y 7D |
| conservación de valor | commit, timeout y rollback | `NoValueLossAtTermination` | contrastada mediante replay y corpus negativo | 7C y 7D |
| decisión terminal | tabla Java e irreversibilidad | `TerminalStateIrreversibility` | contrastada mediante replay y commit tras abort | 7C y 7D |
| replay | ataque runtime y escenario S03 | `NoReceiptReplay`, mutante | cerrada con consumo repetido rechazado por TLC | 7D |
| timeout | ronda lógica y liberación | invariante acotada de liberación | liveness general fuera del alcance | trabajo futuro |
| model checking | ejecución reproducible | diecisiete runs y diez mutantes | tablas finales | 8 |
| procedencia | seed y commit Java | commit fuente y commit ejecutado | cerrada con manifiesto y hashes integrados | 7E |
| conformidad Java-TLA+ | diez escenarios válidos | replay TLC y diez mutaciones rechazadas | cerrada dentro del catálogo declarado | 7A a 7E |
| artefacto reproducible | scripts y workflows | manifiesto, resumen, matriz y artefacto CI | snapshot editorial pendiente | 8 |

#### Evidencia cerrada hasta Fase 7E

- máquina de estados Java;
- protocolo atómico y rollback;
- simulación determinista S01 a S10;
- TLA+ y Alloy multisesión;
- siete propiedades ejecutadas;
- cinco mutantes por herramienta;
- contraejemplos almacenados;
- propiedad objetivo validada en Alloy;
- procedencia de resultados;
- JSONL determinista y función de abstracción;
- replay TLC de diez escenarios válidos;
- corpus de diez trazas corruptas;
- manifiesto científico integrado y artefacto CI.

#### Regla de actualización

Cada PR del Paper 1 actualizará esta matriz cuando cambie una propiedad, transición, función de abstracción, mutante, formato de traza o resultado experimental.
