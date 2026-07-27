package dltlab.verification;

import dltlab.sharding.CrossShardSession;
import dltlab.sharding.Shard;

/** Verifica que las sesiones cross-shard terminales no dejen UTXOs bloqueados. */
public class NoStuckCrossShardInvariant implements Invariant {
    @Override
    public VerificationResult check(LedgerState state) {
        if (state.shardManager() == null) {
            return new VerificationResult("Cross-shard sin bloqueos colgados", true,
                    "No hay shards configurados en esta simulacion.");
        }
        for (CrossShardSession session : state.shardManager().getSessions()) {
            if (!session.isTerminal()) {
                continue;
            }
            Shard source = state.shardManager().getShard(session.transfer().sourceShardId());
            if (source.isLocked(session.transfer().sourceUtxo().key())) {
                return new VerificationResult("Cross-shard sin bloqueos colgados", false,
                        "La transferencia " + session.transfer().id().substring(0, 10)
                                + " termino como " + session.status() + " pero dejo un UTXO bloqueado.");
            }
        }
        return new VerificationResult("Cross-shard sin bloqueos colgados", true,
                "Las sesiones terminadas liberaron o consumieron sus UTXOs origen correctamente.");
    }
}
