package dltlab.verification;

import dltlab.sharding.Shard;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXOPool;

/** Verifica que ningun UTXO tenga valor negativo. */
public class NoNegativeUtxoInvariant implements Invariant {
    @Override
    public VerificationResult check(LedgerState state) {
        UTXOPool mainPool = state.blockChain().getMaxHeightUTXOPool();
        for (Transaction.Output output : mainPool.getAllOutputs()) {
            if (output.getValue() < 0) {
                return new VerificationResult("UTXO no negativo", false, "Se encontro un UTXO negativo en la cadena principal.");
            }
        }
        if (state.shardManager() != null) {
            for (Shard shard : state.shardManager().getShards()) {
                for (Transaction.Output output : shard.getUtxoPool().getAllOutputs()) {
                    if (output.getValue() < 0) {
                        return new VerificationResult("UTXO no negativo", false, "Se encontro un UTXO negativo en shard " + shard.getId() + ".");
                    }
                }
            }
        }
        return new VerificationResult("UTXO no negativo", true, "Todos los UTXOs observados tienen valor valido.");
    }
}
