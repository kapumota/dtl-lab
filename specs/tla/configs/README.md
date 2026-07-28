### Configuraciones TLA+

#### Configuraciones válidas

- `2shards-1transfer.cfg`;
- `2shards-2transfers.cfg`;
- `3shards-3transfers.cfg`;
- `duplicate-receipt.cfg`;
- `delayed-message.cfg`;
- `timeout-race.cfg`.

Cada configuración válida ejecuta `TypeOK` y las siete propiedades científicas del baseline `v1.1.0-beta.2`.

#### Configuraciones de mutantes

El subdirectorio `mutants/` selecciona una propiedad objetivo por mutante. `commit-after-abort.cfg` usa `TerminalStateIrreversibility` para detectar el cambio de una decisión terminal.
