package dltlab.verification;

import dltlab.sharding.Shard;

import java.util.HashSet;
import java.util.Set;

/** Verifica que los recibos cross-shard consumidos no aparezcan duplicados. */
public class NoReceiptReplayInvariant implements Invariant {
    @Override
    public VerificationResult check(LedgerState state) {
        if (state.shardManager() == null) {
            return new VerificationResult("Recibos sin replay", true, "No hay shards configurados en esta simulacion.");
        }
        Set<String> global = new HashSet<>();
        for (Shard shard : state.shardManager().getShards()) {
            for (String receiptId : shard.getConsumedReceipts()) {
                String key = shard.getId() + ":" + receiptId;
                if (!global.add(key)) {
                    return new VerificationResult("Recibos sin replay", false, "Recibo duplicado detectado: " + receiptId + ".");
                }
            }
        }
        return new VerificationResult("Recibos sin replay", true, "No se detectaron recibos cross-shard duplicados.");
    }
}
