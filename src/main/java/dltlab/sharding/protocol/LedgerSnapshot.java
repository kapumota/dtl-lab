package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardSession;
import dltlab.transaction.Transaction;

/** Estado previo necesario para restaurar un commit interrumpido. */
public record LedgerSnapshot(
        boolean sourceUtxoPresent,
        Transaction.Output sourceOutput,
        boolean sourceLocked,
        boolean receiptConsumed,
        boolean changeUtxoPresent,
        Transaction.Output changeOutput,
        boolean targetUtxoPresent,
        Transaction.Output targetOutput,
        CrossShardSession.SessionCheckpoint sessionCheckpoint
) {
    public LedgerSnapshot {
        if (sourceUtxoPresent && sourceOutput == null) {
            throw new IllegalArgumentException("El snapshot del origen debe conservar su salida.");
        }
        if (sessionCheckpoint == null) {
            throw new IllegalArgumentException("El checkpoint de la sesion es obligatorio.");
        }
    }
}
