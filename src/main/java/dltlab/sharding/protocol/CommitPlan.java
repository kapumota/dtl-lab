package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardSession;
import dltlab.sharding.Shard;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;

/** Plan validado con todas las mutaciones requeridas para el commit. */
public record CommitPlan(
        CrossShardSession session,
        Shard source,
        Shard target,
        UTXO sourceUtxo,
        Transaction.Output sourceOutput,
        UTXO changeUtxo,
        Transaction.Output changeOutput,
        UTXO targetUtxo,
        Transaction.Output targetOutput,
        int targetApprovals,
        int targetValidators,
        LedgerSnapshot snapshot
) {
    public CommitPlan {
        if (session == null || source == null || target == null || sourceUtxo == null
                || sourceOutput == null || targetUtxo == null || targetOutput == null
                || snapshot == null) {
            throw new IllegalArgumentException("El plan de commit contiene datos obligatorios ausentes.");
        }
        if ((changeUtxo == null) != (changeOutput == null)) {
            throw new IllegalArgumentException("El UTXO de cambio y su salida deben existir juntos.");
        }
        if (targetValidators <= 0 || targetApprovals < 0 || targetApprovals > targetValidators) {
            throw new IllegalArgumentException("Las aprobaciones del destino son invalidas.");
        }
    }
}
