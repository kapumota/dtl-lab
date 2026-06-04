package dltlab.mining;

import dltlab.blockchain.Block;
import dltlab.blockchain.BlockChain;
import dltlab.mempool.FeeRatePolicy;
import dltlab.transaction.Transaction;
import dltlab.wallet.Wallet;

import java.util.List;

/** Construye bloques candidatos usando capacidad de bloque medida en vBytes. */
public class BlockTemplateBuilder {
    private final FeeRatePolicy policy;

    public BlockTemplateBuilder() {
        this(new FeeRatePolicy());
    }

    public BlockTemplateBuilder(FeeRatePolicy policy) {
        this.policy = policy;
    }

    public List<Transaction> selectTransactions(BlockChain chain, long maxBlockVBytes) {
        return policy.selectByVirtualSize(
                chain.getTransactionPool().getAll(),
                chain.getMaxHeightUTXOPool(),
                maxBlockVBytes
        );
    }

    public Block build(BlockChain chain, Wallet minerWallet, long maxBlockVBytes) {
        List<Transaction> selected = selectTransactions(chain, maxBlockVBytes);
        return new Block(chain.getMaxHeightBlock().getHash(), minerWallet.getPublicKey(), selected, chain.getMaxHeight() + 1);
    }
}
