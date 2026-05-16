package dltlab.app;

import dltlab.blockchain.Block;
import dltlab.blockchain.BlockChain;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.transaction.UTXOPool;
import dltlab.wallet.Wallet;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Utilidades para crear escenarios reproducibles de demo y pruebas. */
public final class DemoData {
    private DemoData() {}

    public static BlockChain createChainWithGenesis(Wallet miner) {
        Block genesis = new Block(null, miner.getPublicKey(), List.of(), 1);
        return new BlockChain(genesis);
    }

    public static UTXO firstUtxo(UTXOPool pool) {
        return pool.getAllUTXO().iterator().next();
    }

    public static Optional<UTXO> findFirstOwnedUtxo(UTXOPool pool, PublicKey owner) {
        for (UTXO utxo : pool.getAllUTXO()) {
            Transaction.Output output = pool.getOutput(utxo);
            if (Arrays.equals(output.getRecipient().getEncoded(), owner.getEncoded())) {
                return Optional.of(utxo);
            }
        }
        return Optional.empty();
    }

    public static Transaction spendFirstOwned(UTXOPool pool, Wallet owner, Wallet recipient, long amount, long fee) {
        UTXO utxo = findFirstOwnedUtxo(pool, owner.getPublicKey())
                .orElseThrow(() -> new IllegalStateException("No se encontro UTXO disponible para " + owner.getLabel() + "."));
        return owner.createSpend(utxo, pool.getOutput(utxo), recipient.getPublicKey(), amount, fee);
    }
}
