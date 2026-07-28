### Matriz de trazabilidad

#### Matriz principal

| Propiedad o elemento | Evidencia Java | Evidencia formal | Brecha restante | Fase |
|---|---|---|---|---|
| estado de sesión | `CrossShardStatus`, `TransitionTable`, eventos | `status[t]`, `terminalStatus[t]`, estados Alloy | función de abstracción ejecutable | 7B |
| historial de sesión | `ProtocolEvent` ordenado | secuencia abstracta de estados | formato común de trazas | 7A |
| bloqueo de origen | protocolo atómico y escenarios concurrentes | `locked[t]`, `LockTransfer` | relacionar snapshot Java con estado abstracto | 7B |
| recibos | creación, entrega, consumo y replay | `receiptOwner`, `receiptUseCount`, mensajes | mapear eventos y copias de recibo | 7B |
| crédito destino | UTXO destino creado por commit | `destinationCredit[t]` | definir observación estable del crédito | 7B |
| votos y quorum | validadores disponibles y quorum | `votes[t]`, `QuorumRequired` | exportar votos observables | 7A y 7B |
| conservación de valor | commit, timeout y rollback | `NoValueLossAtTermination` | conformidad de trazas | 7C |
| decisión terminal | tabla Java e irreversibilidad | `TerminalStateIrreversibility` | replay formal de transiciones | 7C |
| replay | ataque runtime y escenario S03 | `NoReceiptReplay`, mutante | relacionar rechazo Java con acción formal | 7C y 7D |
| timeout | ronda lógica y liberación | invariante acotada de liberación | liveness general fuera del alcance | trabajo futuro |
| model checking | ejecución reproducible | diecisiete runs y diez mutantes | tablas finales | 8 |
| procedencia | seed y commit Java | commit fuente y commit ejecutado | unificar con manifiesto de conformidad | 7E |
| conformidad Java-TLA+ | pendiente | modelo abstracto vigente | trazas, abstracción, checker y corpus negativo | 7 |
| artefacto reproducible | scripts y workflows | resultados raw y checksums | comando final y snapshot | 8 |

#### Evidencia cerrada hasta Fase 6.1

- máquina de estados Java;
- protocolo atómico y rollback;
- simulación determinista S01 a S10;
- TLA+ y Alloy multisesión;
- siete propiedades ejecutadas;
- cinco mutantes por herramienta;
- contraejemplos almacenados;
- propiedad objetivo validada en Alloy;
- procedencia de resultados.

#### Regla de actualización

Cada PR del Paper 1 actualizará esta matriz cuando cambie una propiedad, transición, función de abstracción, mutante, formato de traza o resultado experimental.
