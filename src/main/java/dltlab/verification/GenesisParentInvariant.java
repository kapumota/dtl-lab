package dltlab.verification;

import dltlab.blockchain.Block;

/** Verifica que solo exista un bloque genesis sin padre. */
public class GenesisParentInvariant implements Invariant {
    @Override
    public VerificationResult check(LedgerState state) {
        int genesisCount = 0;
        for (Block block : state.blockChain().getKnownBlocks()) {
            if (block.getPreviousBlockHash() == null) {
                genesisCount++;
            }
        }
        if (genesisCount == 1) {
            return new VerificationResult("Genesis unico", true, "Solo se observo un bloque genesis.");
        }
        return new VerificationResult("Genesis unico", false, "Cantidad de bloques genesis observados: " + genesisCount + ".");
    }
}
