package dltlab.security;

import dltlab.app.DemoData;
import dltlab.blockchain.Block;
import dltlab.blockchain.BlockChain;
import dltlab.mining.Miner;
import dltlab.mempool.HighestFeePolicy;
import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.Receipt;
import dltlab.sharding.ShardManager;
import dltlab.transaction.Transaction;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXO;
import dltlab.transaction.UTXOPool;
import dltlab.verification.GenesisParentInvariant;
import dltlab.verification.Invariant;
import dltlab.verification.InvariantChecker;
import dltlab.verification.LedgerState;
import dltlab.verification.NoNegativeUtxoInvariant;
import dltlab.verification.NoReceiptReplayInvariant;
import dltlab.verification.NoStuckCrossShardInvariant;
import dltlab.verification.VerificationReport;
import dltlab.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Suite de pruebas pseudoaleatorias y reproducibles.
 * No intenta reemplazar verificacion formal real; convierte invariantes clave en pruebas ejecutables.
 */
public class PropertyBasedSecuritySuite {
    private final long seed;
    private final int iterations;
    private final Random random;

    public PropertyBasedSecuritySuite(long seed, int iterations) {
        if (iterations <= 0) {
            throw new IllegalArgumentException("Las iteraciones deben ser positivas.");
        }
        this.seed = seed;
        this.iterations = iterations;
        this.random = new Random(seed);
    }

    public SecurityScoreReport runAll() {
        List<SecurityPropertyResult> results = new ArrayList<>();
        results.add(checkDoubleSpendResistance());
        results.add(checkInvalidSignatureResistance());
        results.add(checkForkValidation());
        results.add(checkCrossShardReplayAndTimeoutSafety());
        results.add(checkRuntimeInvariants());
        return new SecurityScoreReport(seed, results);
    }

    private SecurityPropertyResult checkDoubleSpendResistance() {
        int passed = 0;
        String failure = "";
        Wallet owner = new Wallet("propietario_ds");
        Wallet a = new Wallet("receptor_a");
        Wallet b = new Wallet("receptor_b");
        for (int i = 0; i < iterations; i++) {
            BlockChain chain = DemoData.createChainWithGenesis(owner);
            UTXOPool pool = chain.getMaxHeightUTXOPool();
            UTXO coinbase = DemoData.firstUtxo(pool);
            Transaction.Output output = pool.getOutput(coinbase);
            long amountA = 1_000L + random.nextInt(200_000);
            long amountB = 1_000L + random.nextInt(200_000);
            Transaction tx1 = owner.createSpend(coinbase, output, a.getPublicKey(), amountA, 1000L);
            Transaction tx2 = owner.createSpend(coinbase, output, b.getPublicKey(), amountB, 2000L);
            Transaction[] accepted = new TxValidator(pool).handleTxs(random.nextBoolean()
                    ? new Transaction[]{tx1, tx2}
                    : new Transaction[]{tx2, tx1});
            if (accepted.length <= 1) {
                passed++;
            } else {
                failure = "Se aceptaron dos transacciones que gastaban el mismo UTXO.";
                break;
            }
        }
        return result("Resistencia a doble gasto UTXO", passed, failure,
                "Nunca se aceptan dos gastos del mismo UTXO en un mismo lote.");
    }

    private SecurityPropertyResult checkInvalidSignatureResistance() {
        int passed = 0;
        String failure = "";
        Wallet owner = new Wallet("dueno_sig");
        Wallet attacker = new Wallet("atacante_sig");
        Wallet receiver = new Wallet("receptor_sig");
        for (int i = 0; i < iterations; i++) {
            BlockChain chain = DemoData.createChainWithGenesis(owner);
            UTXOPool pool = chain.getMaxHeightUTXOPool();
            UTXO coinbase = DemoData.firstUtxo(pool);
            Transaction.Output output = pool.getOutput(coinbase);
            Transaction forged = new Transaction();
            forged.addInput(coinbase.getTxHash(), coinbase.getOutputIndex());
            long fee = 1000L + random.nextInt(5000);
            forged.addOutput(output.getValue() - fee, receiver.getPublicKey());
            forged.signInput(0, attacker.getPrivateKey());
            forged.finalizeTransaction();
            if (!new TxValidator(pool).isValidTx(forged)) {
                passed++;
            } else {
                failure = "Una transaccion firmada por una llave equivocada fue aceptada.";
                break;
            }
        }
        return result("Resistencia a firmas invalidas", passed, failure,
                "Las transacciones forjadas con llaves incorrectas son rechazadas.");
    }

    private SecurityPropertyResult checkForkValidation() {
        int passed = 0;
        String failure = "";
        Wallet miner = new Wallet("minero_fork");
        Wallet receiver = new Wallet("receptor_fork");
        for (int i = 0; i < iterations; i++) {
            BlockChain chain = DemoData.createChainWithGenesis(miner);
            Block genesis = chain.getMaxHeightBlock();
            Block fakeGenesis = new Block(null, miner.getPublicKey(), List.of(), 1);
            Block unknownParent = new Block(new byte[]{99, 88, 77, (byte) i}, miner.getPublicKey(), List.of(), 2);
            UTXOPool pool = chain.getMaxHeightUTXOPool();
            UTXO coinbase = DemoData.firstUtxo(pool);
            Transaction tx = miner.createSpend(coinbase, pool.getOutput(coinbase), receiver.getPublicKey(),
                    1_000L + random.nextInt(100_000), 1000L);
            chain.addTransaction(tx);
            boolean mined = new Miner(miner, new HighestFeePolicy(), 10).mineAndAdd(chain);
            Block siblingFork = new Block(genesis.getHash(), receiver.getPublicKey(), List.of(), 2);
            boolean siblingAccepted = chain.addBlock(siblingFork);
            if (!chain.addBlock(fakeGenesis) && !chain.addBlock(unknownParent) && mined && siblingAccepted && chain.getMaxHeight() >= 2) {
                passed++;
            } else {
                failure = "La blockchain acepto un genesis falso, parent desconocido o no manejo bien un fork valido.";
                break;
            }
        }
        return result("Validacion de forks y parents", passed, failure,
                "Se rechazan parents inexistentes y genesis falsos; forks validos se mantienen.");
    }

    private SecurityPropertyResult checkCrossShardReplayAndTimeoutSafety() {
        int passed = 0;
        String failure = "";
        Wallet alice = new Wallet("alice_shard");
        Wallet bob = new Wallet("bob_shard");
        for (int i = 0; i < iterations; i++) {
            ShardManager manager = new ShardManager(3, 4, 3);
            UTXO replayUtxo = new UTXO(new byte[]{(byte) i, 1, 2, 3}, 0);
            manager.getShard(0).getUtxoPool().addUTXO(replayUtxo, new Transaction.Output(10_000L, alice.getPublicKey()));
            Receipt receipt = manager.lockAndCreateReceipt(new CrossShardTransfer(0, 1, replayUtxo, 5000L, bob.getPublicKey()));
            boolean firstCommit = manager.commitReceipt(receipt);
            boolean replayRejected = !manager.commitReceipt(receipt);

            UTXO timeoutUtxo = new UTXO(new byte[]{(byte) i, 4, 5, 6}, 0);
            manager.getShard(1).getUtxoPool().addUTXO(timeoutUtxo, new Transaction.Output(8000L, alice.getPublicKey()));
            CrossShardTransfer transfer = new CrossShardTransfer(1, 2, timeoutUtxo, 3000L + random.nextInt(1000), bob.getPublicKey());
            CrossShardSession session = manager.beginAtomicTransfer(transfer, 1);
            manager.advanceRounds(2);
            boolean timeoutSafe = session.status() == CrossShardStatus.TIMED_OUT
                    && !manager.getShard(1).isLocked(timeoutUtxo.key())
                    && manager.getShard(1).getUtxoPool().contains(timeoutUtxo);
            if (firstCommit && replayRejected && timeoutSafe) {
                passed++;
            } else {
                failure = "Fallo proteccion contra replay o limpieza de timeout cross-shard.";
                break;
            }
        }
        return result("Seguridad cross-shard replay/timeout", passed, failure,
                "Los recibos no se consumen dos veces y los timeouts liberan el UTXO origen.");
    }

    private SecurityPropertyResult checkRuntimeInvariants() {
        int passed = 0;
        String failure = "";
        List<Invariant> invariants = List.of(
                new NoNegativeUtxoInvariant(),
                new GenesisParentInvariant(),
                new NoReceiptReplayInvariant(),
                new NoStuckCrossShardInvariant()
        );
        Wallet miner = new Wallet("minero_inv");
        Wallet alice = new Wallet("alice_inv");
        Wallet bob = new Wallet("bob_inv");
        for (int i = 0; i < iterations; i++) {
            BlockChain chain = DemoData.createChainWithGenesis(miner);
            UTXOPool pool = chain.getMaxHeightUTXOPool();
            UTXO coinbase = DemoData.firstUtxo(pool);
            Transaction tx = miner.createSpend(coinbase, pool.getOutput(coinbase), alice.getPublicKey(),
                    1_000_000L + random.nextInt(100_000), 1000L + random.nextInt(5000));
            chain.addTransaction(tx);
            new Miner(miner, new HighestFeePolicy(), 5).mineAndAdd(chain);

            ShardManager shardManager = new ShardManager(2, 3, 2);
            UTXO shardUtxo = new UTXO(new byte[]{(byte) i, 9, 9, 9}, 0);
            shardManager.getShard(0).getUtxoPool().addUTXO(shardUtxo, new Transaction.Output(9000L, alice.getPublicKey()));
            CrossShardTransfer transfer = new CrossShardTransfer(0, 1, shardUtxo, 4000L, bob.getPublicKey());
            shardManager.beginAtomicTransfer(transfer, 3);
            if (random.nextBoolean()) {
                shardManager.commitAtomicTransfer(transfer.id());
            } else {
                shardManager.abortAtomicTransfer(transfer.id(), "Abortado por prueba pseudoaleatoria.");
            }

            VerificationReport report = new InvariantChecker().check(new LedgerState(chain, shardManager), invariants);
            if (report.allPassed()) {
                passed++;
            } else {
                failure = "Una invariante fallo en escenario pseudoaleatorio: " + report.render().replace('\n', ' ');
                break;
            }
        }
        return result("Invariantes runtime pseudoaleatorias", passed, failure,
                "Genesis unico, UTXOs no negativos, sin replay y sin bloqueos terminales colgados.");
    }

    private SecurityPropertyResult result(String name, int passed, String failure, String successDetail) {
        int failed = iterations - passed;
        return new SecurityPropertyResult(name, iterations, passed, failed, failure.isBlank() ? successDetail : failure);
    }
}
