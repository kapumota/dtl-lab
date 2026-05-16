package dltlab.mining;

import dltlab.blockchain.Block;
import dltlab.blockchain.BlockChain;
import dltlab.mempool.MempoolPolicy;
import dltlab.transaction.Transaction;
import dltlab.wallet.Wallet;

import java.util.List;

/** Minero educativo: selecciona transacciones de la mempool y propone un bloque. */
public class Miner {
    private final Wallet wallet;
    private final MempoolPolicy policy;
    private final int maxTransactionsPerBlock;

    public Miner(Wallet wallet, MempoolPolicy policy, int maxTransactionsPerBlock) {
        this.wallet = wallet;
        this.policy = policy;
        this.maxTransactionsPerBlock = maxTransactionsPerBlock;
    }

    public Block mineCandidate(BlockChain chain) {
        List<Transaction> selected = policy.select(
                chain.getTransactionPool().getAll(),
                chain.getMaxHeightUTXOPool(),
                maxTransactionsPerBlock
        );
        return new Block(chain.getMaxHeightBlock().getHash(), wallet.getPublicKey(), selected, chain.getMaxHeight() + 1);
    }

    public boolean mineAndAdd(BlockChain chain) {
        return chain.addBlock(mineCandidate(chain));
    }

    public String policyName() {
        return policy.name();
    }
}
