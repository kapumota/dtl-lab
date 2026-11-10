package dltlab;

import dltlab.blockchain.Block;
import dltlab.blockchain.BlockChain;
import dltlab.crypto.Hashing;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.wallet.Wallet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class P1ABlockIntegrityTest {
    private static final Wallet MINER = new Wallet("minero");
    private static final Wallet RECIPIENT = new Wallet("destinatario");

    private P1ABlockIntegrityTest() {}

    public static void main(String[] args) {
        String probe = args.length == 0 ? "all" : args[0];
        if (probe.equals("all") || probe.equals("P09")) coinbaseIdentity();
        if (probe.equals("all") || probe.equals("P10")) exposedCoinbase();
        if (probe.equals("all") || probe.equals("P10-snapshot")) immutableBlock();
        System.out.println("PASS " + probe);
    }

    private static void coinbaseIdentity() {
        Block genesis = block(null, 1, List.of());
        BlockChain chain = new BlockChain(genesis);
        Block second = block(genesis, 2, List.of());
        Block third = block(second, 3, List.of());
        check(chain.addBlock(second) && chain.addBlock(third), "Los bloques validos deben aceptarse.");
        check(chain.getMaxHeightUTXOPool().totalValue() == 3 * Block.COINBASE_REWARD,
                "P09: se perdio una recompensa del mismo minero.");
        for (Block block : List.of(genesis, second, third)) {
            check(chain.getMaxHeightUTXOPool().contains(reward(block)), "Cada recompensa tiene su UTXO.");
        }

        Transaction payment = MINER.createSpend(reward(genesis), genesis.getCoinbase().getOutputs().get(0),
                RECIPIENT.getPublicKey(), 100, 0);
        Block sibling = block(genesis, 2, List.of(payment));
        check(!reward(sibling).equals(reward(second)) && chain.addBlock(sibling),
                "Los hermanos con contenido distinto tienen coinbase propia.");
        Block forkThird = block(sibling, 3, List.of());
        check(!reward(forkThird).equals(reward(third)) && chain.addBlock(forkThird),
                "Los padres distintos distinguen la recompensa a igual altura.");
        Block forkFourth = block(forkThird, 4, List.of());
        check(chain.addBlock(forkFourth) && chain.getMaxHeight() == 4, "La bifurcacion valida debe avanzar.");
        var forkPool = chain.getMaxHeightUTXOPool();
        check(forkPool.contains(reward(sibling)) && forkPool.contains(reward(forkThird))
                && !forkPool.contains(reward(second)) && !forkPool.contains(reward(genesis)),
                "El ledger del fork conserva sus recompensas y el gasto del genesis.");
        check(forkPool.totalValue() == 4 * Block.COINBASE_REWARD, "El fork conserva el valor.");
        int known = chain.getKnownBlocks().size();
        check(!chain.addBlock(forkFourth) && !chain.addBlock(block(forkFourth, 6, List.of()))
                && chain.getKnownBlocks().size() == known && chain.getMaxHeight() == 4,
                "Duplicados y alturas incoherentes deben rechazarse sin cambiar la cadena.");
    }

    private static void exposedCoinbase() {
        Block genesis = block(null, 1, List.of());
        byte[] hash = genesis.getHash();
        rejectMutation(() -> genesis.getCoinbase().addOutput(1, MINER.getPublicKey()));
        check(Arrays.equals(hash, genesis.getHash())
                && genesis.getCoinbase().outputSum() == Block.COINBASE_REWARD,
                "P10: la coinbase comprometida debe conservar su valor y hash.");
    }

    private static void immutableBlock() {
        Block genesis = block(null, 1, List.of());
        BlockChain chain = new BlockChain(genesis);
        Transaction payment = MINER.createSpend(reward(genesis), genesis.getCoinbase().getOutputs().get(0),
                RECIPIENT.getPublicKey(), 100, 0);
        byte[] signature = payment.getInputs().get(0).getSignature();
        payment.getHash();
        payment.getInputs().get(0).setSignature(new byte[]{1});
        check(Arrays.equals(Hashing.sha256(payment.getRawData()), payment.getHash()),
                "P10: modificar una firma deja el hash de transaccion obsoleto.");
        payment.getInputs().get(0).setSignature(signature);

        var originals = new ArrayList<>(List.of(payment));
        Block block = block(genesis, 2, originals);
        byte[] blockHash = block.getHash();
        Transaction committed = block.getTransactions().get(0);
        byte[] raw = committed.getRawData();
        originals.clear();
        payment.addOutput(1, MINER.getPublicKey());
        payment.getInputs().get(0).setSignature(new byte[]{3});
        check(Arrays.equals(raw, committed.getRawData()), "P10: el bloque comparte la transaccion original.");

        rejectMutation(() -> committed.addOutput(1, MINER.getPublicKey()));
        rejectMutation(() -> committed.addInput(new byte[]{1}, 0));
        rejectMutation(() -> committed.signInput(0, MINER.getPrivateKey()));
        rejectMutation(() -> committed.getInputs().get(0).setSignature(new byte[]{4}));
        rejectMutation(() -> block.getCoinbase().addOutput(1, MINER.getPublicKey()));
        rejectMutation(() -> block.getTransactions().clear());
        check(chain.addBlock(block) && chain.containsBlock(blockHash), "El bloque comprometido debe aceptarse.");
        Block known = chain.getMaxHeightBlock();
        known.getHash()[0] ^= 1;
        known.getPreviousBlockHash()[0] ^= 1;
        known.getTransactions().get(0).getInputs().get(0).getSignature()[0] ^= 1;
        check(Arrays.equals(blockHash, known.getHash()) && Arrays.equals(raw, committed.getRawData())
                && Arrays.equals(committed.getHash(), Hashing.sha256(raw))
                && chain.getMaxHeightUTXOPool().contains(new UTXO(committed.getHash(), 0)),
                "El contenido, los hashes y el ledger deben permanecer coherentes.");
    }

    private static Block block(Block parent, int height, List<Transaction> transactions) {
        return new Block(parent == null ? null : parent.getHash(), MINER.getPublicKey(), transactions, height);
    }

    private static UTXO reward(Block block) {
        return new UTXO(block.getCoinbase().getHash(), 0);
    }

    private static void rejectMutation(Runnable operation) {
        try {
            operation.run();
        } catch (IllegalStateException | UnsupportedOperationException expected) {
            return;
        }
        throw new AssertionError("P10: una mutacion del contenido comprometido fue aceptada.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
