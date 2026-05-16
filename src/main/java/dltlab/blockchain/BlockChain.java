package dltlab.blockchain;

import dltlab.crypto.Hashing;
import dltlab.mempool.TransactionMempool;
import dltlab.transaction.Transaction;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXO;
import dltlab.transaction.UTXOPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Mantiene una blockchain con forks y UTXO pool asociado a cada bloque reciente. */
public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    private final Map<String, BlockNode> nodes = new LinkedHashMap<>();
    private final TransactionMempool transactionPool = new TransactionMempool();
    private BlockNode maxHeightNode;
    private long creationCounter = 0L;

    private static class BlockNode {
        private final Block block;
        private final int height;
        private final UTXOPool utxoPool;
        private final long order;

        private BlockNode(Block block, int height, UTXOPool utxoPool, long order) {
            this.block = block;
            this.height = height;
            this.utxoPool = utxoPool;
            this.order = order;
        }
    }

    public BlockChain(Block genesisBlock) {
        if (genesisBlock.getPreviousBlockHash() != null) {
            throw new IllegalArgumentException("El bloque genesis no debe tener padre.");
        }
        UTXOPool pool = new UTXOPool();
        addCoinbaseToPool(genesisBlock, pool);
        BlockNode genesis = new BlockNode(genesisBlock, 1, pool, creationCounter++);
        nodes.put(Hashing.hex(genesisBlock.getHash()), genesis);
        maxHeightNode = genesis;
    }

    public Block getMaxHeightBlock() {
        return maxHeightNode.block;
    }

    public int getMaxHeight() {
        return maxHeightNode.height;
    }

    public UTXOPool getMaxHeightUTXOPool() {
        return new UTXOPool(maxHeightNode.utxoPool);
    }

    public TransactionMempool getTransactionPool() {
        return transactionPool;
    }

    public boolean addBlock(Block block) {
        if (block.getPreviousBlockHash() == null) {
            return false; // No se aceptan nuevos bloques genesis.
        }

        BlockNode parent = nodes.get(Hashing.hex(block.getPreviousBlockHash()));
        if (parent == null) {
            return false;
        }

        int newHeight = parent.height + 1;
        if (newHeight <= maxHeightNode.height - CUT_OFF_AGE) {
            return false;
        }

        TxValidator validator = new TxValidator(parent.utxoPool);
        Transaction[] accepted = validator.handleTxs(block.getTransactions().toArray(new Transaction[0]));
        if (accepted.length != block.getTransactions().size()) {
            return false;
        }

        UTXOPool newPool = validator.getUtxoPool();
        addCoinbaseToPool(block, newPool);
        BlockNode node = new BlockNode(block, newHeight, newPool, creationCounter++);
        nodes.put(Hashing.hex(block.getHash()), node);

        for (Transaction tx : block.getTransactions()) {
            transactionPool.remove(tx);
        }

        if (newHeight > maxHeightNode.height) {
            maxHeightNode = node;
            pruneOldBlocks();
        } else if (newHeight == maxHeightNode.height && node.order < maxHeightNode.order) {
            // En la practica no ocurre porque order crece, pero deja explicita la regla de "mas antiguo".
            maxHeightNode = node;
        }
        return true;
    }

    public void addTransaction(Transaction tx) {
        transactionPool.add(tx);
    }

    public Collection<Block> getKnownBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (BlockNode node : nodes.values()) {
            blocks.add(node.block);
        }
        return Collections.unmodifiableList(blocks);
    }

    public boolean containsBlock(byte[] hash) {
        return nodes.containsKey(Hashing.hex(hash));
    }

    private void addCoinbaseToPool(Block block, UTXOPool pool) {
        Transaction coinbase = block.getCoinbase();
        for (int i = 0; i < coinbase.getOutputs().size(); i++) {
            pool.addUTXO(new UTXO(coinbase.getHash(), i), coinbase.getOutputs().get(i));
        }
    }

    private void pruneOldBlocks() {
        int minHeight = maxHeightNode.height - CUT_OFF_AGE;
        nodes.entrySet().removeIf(entry -> entry.getValue().height < minHeight);
    }
}
