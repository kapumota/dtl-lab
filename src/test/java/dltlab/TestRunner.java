package dltlab;

import dltlab.app.DemoData;
import dltlab.consensus.AdvancedConsensusResult;
import dltlab.consensus.AdvancedConsensusSimulator;
import dltlab.consensus.ConsensusConfig;
import dltlab.consensus.ConsensusMetricsCsvExporter;
import dltlab.consensus.ReputationConsensusResult;
import dltlab.consensus.ReputationWeightedConsensus;
import dltlab.consensus.SignedConsensusMessage;
import dltlab.blockchain.Block;
import dltlab.blockchain.BlockChain;
import dltlab.defi.AmmPool;
import dltlab.defi.ArbitrageScenario;
import dltlab.defi.ConstantProductMarketMaker;
import dltlab.defi.SwapOrder;
import dltlab.defi.SwapResult;
import dltlab.defi.Token;
import dltlab.mempool.HighestFeePolicy;
import dltlab.transaction.TransactionSizeEstimator;
import dltlab.transaction.FeeCalculator;
import dltlab.mempool.TransactionMempool;
import dltlab.mempool.MempoolConfig;
import dltlab.mempool.MempoolAdmissionResult;
import dltlab.mempool.FeeRatePolicy;
import dltlab.mempool.PackageAwarePolicy;
import dltlab.mining.Miner;
import dltlab.network.EclipseAttackResult;
import dltlab.network.EclipseAttackSimulator;
import dltlab.network.PeerTable;
import dltlab.pow.HashPowerDistribution;
import dltlab.pow.MiningRewardMetrics;
import dltlab.pow.SelfishMiningSimulator;
import dltlab.pow.SelfishMiningStrategy;
import dltlab.security.CrossShardReplayAttack;
import dltlab.security.CrossShardTimeoutAttack;
import dltlab.security.DoubleSpendAttack;
import dltlab.security.InvalidSignatureAttack;
import dltlab.security.PropertyBasedSecuritySuite;
import dltlab.security.SecurityReportCsvExporter;
import dltlab.security.SecurityScoreReport;
import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.Receipt;
import dltlab.sharding.ShardManager;
import dltlab.sharding.ShardMetricsCsvExporter;
import dltlab.visualization.ConsensusNetworkVisualizer;
import dltlab.visualization.ForkTreeVisualizer;
import dltlab.visualization.ShardVisualizer;
import dltlab.metrics.CsvExporter;
import dltlab.metrics.SimulationMetrics;
import dltlab.mev.BackrunArbitrageResult;
import dltlab.mev.BackrunArbitrageSimulator;
import dltlab.mev.DeFiMEVScenario;
import dltlab.mev.MEVDemoFactory;
import dltlab.mev.MEVMetricsCsvExporter;
import dltlab.mev.MEVScenario;
import dltlab.mev.MEVScenarioResult;
import dltlab.mev.MEVSimulator;
import dltlab.mev.MEVType;
import dltlab.mev.SandwichAttackResult;
import dltlab.mev.SandwichAttackSimulator;
import dltlab.transaction.Transaction;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXO;
import dltlab.transaction.UTXOPool;
import dltlab.wallet.Wallet;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.Random;
import java.util.stream.Collectors;

/** Pruebas minimas sin dependencias externas para que el CI sea simple. */
public class TestRunner {
    public static void main(String[] args) {
        testValidTransaction();
        testDoubleSpendRejected();
        testInvalidSignatureRejected();
        testBlockAdded();
        testFakeGenesisRejected();
        testCrossShardReplayRejected();
        testPackageAwareSelection();
        testFeeRateSelectionByVirtualSize();
        testMempoolEvictionByFeeRate();
        testRbfReplacement();
        testMevScenarios();
        testDefiAmmAndMev();
        testSelfishMiningSimulator();
        testEclipseAttackSimulator();
        testReputationWeightedConsensus();
        testAdvancedConsensus();
        testAdvancedSharding();
        testVisualizationAndCsvExport();
        testSecurityAttacks();
        testPropertyBasedSecuritySuite();
        System.out.println("Todas las pruebas pasaron correctamente.");
    }

    private static void testValidTransaction() {
        Wallet miner = new Wallet("minero");
        Wallet alice = new Wallet("alice");
        BlockChain chain = DemoData.createChainWithGenesis(miner);
        UTXOPool pool = chain.getMaxHeightUTXOPool();
        UTXO coinbase = DemoData.firstUtxo(pool);
        Transaction tx = miner.createSpend(coinbase, pool.getOutput(coinbase), alice.getPublicKey(), 1_000_000L, 1000L);
        assertTrue(new TxValidator(pool).isValidTx(tx), "Una transaccion firmada por el dueno debe ser valida.");
    }

    private static void testDoubleSpendRejected() {
        Wallet miner = new Wallet("minero");
        Wallet alice = new Wallet("alice");
        Wallet bob = new Wallet("bob");
        BlockChain chain = DemoData.createChainWithGenesis(miner);
        UTXOPool pool = chain.getMaxHeightUTXOPool();
        UTXO coinbase = DemoData.firstUtxo(pool);
        Transaction.Output output = pool.getOutput(coinbase);
        Transaction tx1 = miner.createSpend(coinbase, output, alice.getPublicKey(), 1_000_000L, 1000L);
        Transaction tx2 = miner.createSpend(coinbase, output, bob.getPublicKey(), 1_000_000L, 2000L);
        Transaction[] accepted = new TxValidator(pool).handleTxs(new Transaction[]{tx1, tx2});
        assertEquals(1, accepted.length, "Solo una transaccion debe sobrevivir al doble gasto.");
    }

    private static void testInvalidSignatureRejected() {
        Wallet owner = new Wallet("dueno");
        Wallet attacker = new Wallet("atacante");
        Wallet recipient = new Wallet("receptor");
        BlockChain chain = DemoData.createChainWithGenesis(owner);
        UTXOPool pool = chain.getMaxHeightUTXOPool();
        UTXO coinbase = DemoData.firstUtxo(pool);
        Transaction.Output output = pool.getOutput(coinbase);
        Transaction forged = new Transaction();
        forged.addInput(coinbase.getTxHash(), coinbase.getOutputIndex());
        forged.addOutput(output.getValue() - 1000L, recipient.getPublicKey());
        forged.signInput(0, attacker.getPrivateKey());
        forged.finalizeTransaction();
        assertFalse(new TxValidator(pool).isValidTx(forged), "Una firma de otra wallet debe ser rechazada.");
    }

    private static void testBlockAdded() {
        Wallet miner = new Wallet("minero");
        Wallet alice = new Wallet("alice");
        BlockChain chain = DemoData.createChainWithGenesis(miner);
        UTXOPool pool = chain.getMaxHeightUTXOPool();
        UTXO coinbase = DemoData.firstUtxo(pool);
        Transaction tx = miner.createSpend(coinbase, pool.getOutput(coinbase), alice.getPublicKey(), 1_000_000L, 1000L);
        chain.addTransaction(tx);
        boolean added = new Miner(miner, new HighestFeePolicy(), 5).mineAndAdd(chain);
        assertTrue(added, "El bloque con transaccion valida debe agregarse.");
        assertEquals(2, chain.getMaxHeight(), "La altura debe aumentar despues del bloque valido.");
    }

    private static void testFakeGenesisRejected() {
        Wallet miner = new Wallet("minero");
        BlockChain chain = DemoData.createChainWithGenesis(miner);
        Block fakeGenesis = new Block(null, miner.getPublicKey(), List.of(), 1);
        assertFalse(chain.addBlock(fakeGenesis), "Un segundo bloque genesis debe ser rechazado.");
    }

    private static void testCrossShardReplayRejected() {
        Wallet alice = new Wallet("alice");
        Wallet bob = new Wallet("bob");
        ShardManager manager = new ShardManager(2);
        UTXO utxo = new UTXO(new byte[]{1, 2, 3}, 0);
        manager.getShard(0).getUtxoPool().addUTXO(utxo, new Transaction.Output(10_000L, alice.getPublicKey()));
        Receipt receipt = manager.lockAndCreateReceipt(new CrossShardTransfer(0, 1, utxo, 5000L, bob.getPublicKey()));
        assertTrue(manager.commitReceipt(receipt), "El primer uso del recibo debe aceptarse.");
        assertFalse(manager.commitReceipt(receipt), "El replay del recibo debe rechazarse.");
    }


    private static void testPackageAwareSelection() {
        Wallet miner = new Wallet("minero");
        Wallet alice = new Wallet("alice");
        Wallet bob = new Wallet("bob");
        Wallet carol = new Wallet("carol");
        Wallet dan = new Wallet("dan");
        BlockChain chain = DemoData.createChainWithGenesis(miner);
        UTXOPool genesisPool = chain.getMaxHeightUTXOPool();
        UTXO coinbase = DemoData.firstUtxo(genesisPool);
        Transaction toAlice = miner.createSpend(coinbase, genesisPool.getOutput(coinbase), alice.getPublicKey(), 8_000_000L, 1000L);
        chain.addTransaction(toAlice);
        assertTrue(new Miner(miner, new HighestFeePolicy(), 5).mineAndAdd(chain), "Debe minarse el bloque inicial.");

        UTXOPool pool = chain.getMaxHeightUTXOPool();
        Transaction independent = DemoData.spendFirstOwned(pool, miner, dan, 1_000_000L, 20_000L);
        Transaction parent = DemoData.spendFirstOwned(pool, alice, bob, 1_000_000L, 100L);
        Transaction child = bob.createSpend(new UTXO(parent.getHash(), 0), parent.getOutputs().get(0), carol.getPublicKey(), 600_000L, 300_000L);

        List<Transaction> selected = new PackageAwarePolicy().select(List.of(independent, child, parent), pool, 2);
        Set<String> selectedIds = selected.stream().map(Transaction::id).collect(Collectors.toSet());
        assertEquals(2, selected.size(), "El paquete padre-hijo debe caber en el bloque pequeno.");
        assertTrue(selectedIds.contains(parent.id()), "La politica debe incluir la transaccion padre.");
        assertTrue(selectedIds.contains(child.id()), "La politica debe incluir la transaccion hija de alto fee.");
    }


    private static void testFeeRateSelectionByVirtualSize() {
        Wallet owner = new Wallet("dueno_fee_rate");
        Wallet recipient = new Wallet("receptor_fee_rate");
        UTXOPool pool = new UTXOPool();
        Transaction largeHighFee = createSyntheticSpend(pool, owner, recipient, 11, 1_000_000L, 8_000L, 160);
        Transaction smallBetterRate = createSyntheticSpend(pool, owner, recipient, 12, 1_000_000L, 4_000L, 0);

        assertTrue(FeeCalculator.fee(largeHighFee, pool) > FeeCalculator.fee(smallBetterRate, pool),
                "La transaccion grande debe pagar mayor fee absoluto.");
        assertTrue(FeeCalculator.feeRate(smallBetterRate, pool).compareTo(FeeCalculator.feeRate(largeHighFee, pool)) > 0,
                "La transaccion pequena debe tener mejor fee rate por vByte.");

        long maxBlockVBytes = Math.max(
                TransactionSizeEstimator.virtualSize(largeHighFee),
                TransactionSizeEstimator.virtualSize(smallBetterRate)
        );
        List<Transaction> selected = new FeeRatePolicy().selectByVirtualSize(List.of(largeHighFee, smallBetterRate), pool, maxBlockVBytes);
        assertEquals(1, selected.size(), "El bloque pequeno debe seleccionar solo una transaccion.");
        assertTrue(selected.get(0).id().equals(smallBetterRate.id()),
                "La seleccion por fee rate debe preferir la transaccion mas densa economicamente.");
    }

    private static void testMempoolEvictionByFeeRate() {
        Wallet owner = new Wallet("dueno_eviction");
        Wallet recipient = new Wallet("receptor_eviction");
        UTXOPool pool = new UTXOPool();
        Transaction lowRate = createSyntheticSpend(pool, owner, recipient, 21, 1_000_000L, 1_000L, 0);
        Transaction highRate = createSyntheticSpend(pool, owner, recipient, 22, 1_000_000L, 8_000L, 0);
        long maxMempoolVBytes = Math.max(
                TransactionSizeEstimator.virtualSize(lowRate),
                TransactionSizeEstimator.virtualSize(highRate)
        ) + 1L;

        TransactionMempool mempool = new TransactionMempool(new MempoolConfig(maxMempoolVBytes, 1L, true, true));
        assertTrue(mempool.admit(lowRate, pool).accepted(), "La primera transaccion debe entrar en la mempool.");
        MempoolAdmissionResult result = mempool.admit(highRate, pool);

        assertTrue(result.accepted(), "La transaccion de mayor fee rate debe ser aceptada.");
        assertTrue(mempool.contains(highRate), "La mempool debe conservar la transaccion de mayor fee rate.");
        assertFalse(mempool.contains(lowRate), "La mempool debe descartar la transaccion de menor fee rate.");
        assertEquals(1, result.evictedTransactions().size(), "La admission debe reportar una transaccion descartada.");
    }

    private static void testRbfReplacement() {
        Wallet owner = new Wallet("dueno_rbf");
        Wallet recipient = new Wallet("receptor_rbf");
        UTXOPool pool = new UTXOPool();
        UTXO utxo = syntheticUtxo(31);
        Transaction.Output output = new Transaction.Output(1_000_000L, owner.getPublicKey());
        pool.addUTXO(utxo, output);

        Transaction original = owner.createSpend(utxo, output, recipient.getPublicKey(), 100_000L, 1_000L);
        Transaction replacement = owner.createSpend(utxo, output, recipient.getPublicKey(), 100_000L, 8_000L);
        TransactionMempool mempool = new TransactionMempool(new MempoolConfig(100_000L, 1L, true, false));

        assertTrue(mempool.admit(original, pool).accepted(), "La transaccion original debe entrar en la mempool.");
        MempoolAdmissionResult result = mempool.admit(replacement, pool);

        assertTrue(result.accepted(), "La transaccion reemplazo debe aceptarse por RBF.");
        assertTrue(result.replacedTransaction() != null, "El resultado debe indicar la transaccion reemplazada.");
        assertTrue(result.replacedTransaction().id().equals(original.id()), "RBF debe reemplazar la transaccion original.");
        assertTrue(mempool.contains(replacement), "La mempool debe conservar el reemplazo.");
        assertFalse(mempool.contains(original), "La mempool debe retirar la transaccion original.");
    }


    private static void testMevScenarios() {
        MEVSimulator simulator = new MEVSimulator();
        List<MEVScenario> scenarios = new MEVDemoFactory().createScenarios();
        assertEquals(3, scenarios.size(), "La demo MEV debe incluir front-running, back-running y sandwich.");

        boolean sawFront = false;
        boolean sawBack = false;
        boolean sawSandwich = false;
        List<MEVScenarioResult> results = new java.util.ArrayList<>();

        for (MEVScenario scenario : scenarios) {
            MEVScenarioResult result = simulator.evaluate(scenario);
            results.add(result);
            assertTrue(result.mevMinerRevenue() > result.honestMinerRevenue(),
                    "El orden MEV debe aumentar el ingreso del productor del bloque en la demo.");

            if (result.primaryType() == MEVType.FRONT_RUNNING) {
                sawFront = true;
                assertBefore(result.mevOrderLabels(), "bot_front_run", "usuario_trade",
                        "En front-running, el bot debe aparecer antes del usuario.");
            } else if (result.primaryType() == MEVType.BACK_RUNNING) {
                sawBack = true;
                assertBefore(result.mevOrderLabels(), "evento_objetivo", "bot_back_run",
                        "En back-running, el bot debe aparecer inmediatamente despues del evento objetivo.");
            } else if (result.primaryType() == MEVType.SANDWICH) {
                sawSandwich = true;
                assertBefore(result.mevOrderLabels(), "bot_compra_antes", "usuario_swap",
                        "En sandwich, la compra del bot debe ir antes del swap del usuario.");
                assertBefore(result.mevOrderLabels(), "usuario_swap", "bot_venta_despues",
                        "En sandwich, la venta del bot debe ir despues del swap del usuario.");
            }
        }

        Path out = new MEVMetricsCsvExporter().export(results, Path.of("build", "test-reports", "mev-metrics-test.csv"));
        assertTrue(Files.exists(out), "El CSV especifico de MEV debe escribirse en disco.");
        assertTrue(sawFront && sawBack && sawSandwich, "Deben cubrirse los tres tipos de MEV basico.");
    }

    private static void testDefiAmmAndMev() {
        Token usdc = Token.of("USDC", 6);
        Token eth = Token.of("ETH", 18);
        AmmPool pool = new AmmPool("USDC-ETH prueba", usdc, eth, 1_000_000.0, 500.0, 30);
        ConstantProductMarketMaker marketMaker = new ConstantProductMarketMaker();
        double invariantBefore = pool.invariant();
        SwapResult swap = marketMaker.execute(pool, new SwapOrder("usuario", usdc, 50_000.0, 0.0));

        assertTrue(swap.amountOut() > 0.0, "El swap debe producir salida positiva.");
        assertTrue(swap.slippagePercent() > 0.0, "Un swap grande debe generar slippage positivo.");
        assertTrue(pool.invariant() >= invariantBefore, "La fee del AMM debe conservar o aumentar el producto de reservas.");

        DeFiMEVScenario sandwichScenario = new DeFiMEVScenario(
                "Sandwich prueba",
                new AmmPool("USDC-ETH sandwich", usdc, eth, 1_000_000.0, 500.0, 30),
                new SwapOrder("usuario_swap", usdc, 50_000.0, 0.0),
                new SwapOrder("bot_compra_antes", usdc, 20_000.0, 0.0),
                0.20
        );
        SandwichAttackResult sandwich = new SandwichAttackSimulator().simulate(sandwichScenario);
        assertTrue(sandwich.attackerProfit() > 0.0, "El sandwich debe calcular ganancia del atacante.");
        assertTrue(sandwich.victimSlippageWithAttack() > sandwich.victimSlippageWithoutAttack(),
                "El sandwich debe empeorar el slippage total de la victima.");
        assertTrue(sandwich.victimWithAttack().amountOut() < sandwich.victimWithoutAttack().amountOut(),
                "La victima debe recibir menos salida con el sandwich.");
        assertTrue(sandwich.builderPayment() > 0.0, "El productor debe recibir un pago MEV positivo.");

        ArbitrageScenario arbitrageScenario = new ArbitrageScenario(
                "Arbitraje prueba",
                new AmmPool("Pool ETH barato", usdc, eth, 1_000_000.0, 520.0, 30),
                new AmmPool("Pool ETH caro", usdc, eth, 1_000_000.0, 480.0, 30),
                usdc,
                10_000.0
        );
        BackrunArbitrageResult arbitrage = new BackrunArbitrageSimulator().simulate(arbitrageScenario);
        assertTrue(arbitrage.profit() > 0.0, "El arbitraje entre pools desbalanceados debe ser rentable.");
    }


    private static void testSelfishMiningSimulator() {
        MiningRewardMetrics metrics = new SelfishMiningSimulator(SelfishMiningStrategy.educationalDefault())
                .simulate(HashPowerDistribution.fromAttackerShare(0.35), 5_000, 2026L);
        assertTrue(metrics.privateBlocksMined() > 0, "El atacante debe minar bloques privados en la simulacion.");
        assertTrue(metrics.publicBlocksMined() > 0, "La red honesta debe minar bloques publicos.");
        assertTrue(metrics.orphanRate() > 0.0, "Selfish mining debe producir bloques huerfanos.");
        assertTrue(metrics.maxPrivateLead() > 0, "La estrategia debe alcanzar lead privado positivo.");
        assertTrue(metrics.relativeRevenue() >= 0.0 && metrics.relativeRevenue() <= 1.0,
                "El revenue relativo debe estar normalizado.");
        assertTrue(metrics.profitabilityThreshold() > 0.0 && metrics.profitabilityThreshold() < 0.5,
                "El umbral de rentabilidad debe estar en un rango educativo razonable.");
    }

    private static void testEclipseAttackSimulator() {
        PeerTable table = PeerTable.educationalTopology();
        EclipseAttackResult result = new EclipseAttackSimulator().simulate(table, Set.of(0), Set.of(6, 7, 8), 3, 9, 120L);
        assertEquals(10, result.totalPeers(), "La topologia educativa debe tener diez peers.");
        assertEquals(3, result.controlledPeers(), "La demo debe controlar tres peers vecinos de la victima.");
        assertEquals(1, result.isolatedNodes(), "La victima debe quedar aislada por vecinos controlados.");
        assertTrue(result.partitionProbability() == 1.0, "La probabilidad de particion debe ser total para una victima aislada.");
        assertTrue(result.averageLatencyMs() > 0.0, "La latencia promedio debe ser positiva.");
    }

    private static void testReputationWeightedConsensus() {
        List<SignedConsensusMessage> messages = List.of(
                SignedConsensusMessage.sign(0, 3, "bloque", "bloque-a"),
                SignedConsensusMessage.sign(1, 3, "bloque", "bloque-a"),
                SignedConsensusMessage.sign(2, 3, "bloque", "bloque-a"),
                SignedConsensusMessage.sign(3, 3, "bloque", "bloque-b"),
                SignedConsensusMessage.sign(3, 3, "bloque", "bloque-c")
        );
        ReputationConsensusResult result = ReputationWeightedConsensus.educationalDefault().evaluate(messages);
        assertEquals(1, result.evidence().size(), "Debe detectarse una evidencia de equivocacion.");
        assertEquals(1, result.slashingEvents().size(), "Debe generarse un evento de slashing reputacional.");
        assertTrue(result.finalScores().get(3).score() < 1.0, "El nodo equivocado debe perder reputacion.");
        assertTrue(result.selectedValue().equals("bloque-a"), "El valor con mayor peso honesto debe ganar.");
    }

    private static void testAdvancedConsensus() {
        Wallet miner = new Wallet("minero");
        Wallet alice = new Wallet("alice");
        Wallet bob = new Wallet("bob");
        BlockChain chain = DemoData.createChainWithGenesis(miner);
        UTXOPool pool = chain.getMaxHeightUTXOPool();
        UTXO coinbase = DemoData.firstUtxo(pool);
        Transaction tx1 = miner.createSpend(coinbase, pool.getOutput(coinbase), alice.getPublicKey(), 2_000_000L, 1000L);
        Transaction tx2 = miner.createSpend(coinbase, pool.getOutput(coinbase), bob.getPublicKey(), 1_500_000L, 2000L);
        Transaction tx3 = miner.createSpend(coinbase, pool.getOutput(coinbase), alice.getPublicKey(), 1_000_000L, 3000L);
        Set<Transaction> universe = Set.of(tx1, tx2, tx3);

        ConsensusConfig config = new ConsensusConfig(30, 6, 0.45, 0.30, 0.60, 0.25, 0.50, 0.30);
        AdvancedConsensusResult result = new AdvancedConsensusSimulator().run(universe, config, new Random(23));

        assertEquals(30, result.totalNodes(), "La simulacion debe respetar el tamano de red configurado.");
        assertTrue(result.honestNodes() > result.maliciousNodes(), "La demo debe mantener mayoria honesta.");
        assertTrue(result.censoringNodes() > 0, "La simulacion avanzada debe incluir nodos censores.");
        assertTrue(result.equivocatingNodes() > 0, "La simulacion avanzada debe incluir nodos equivocadores.");
        assertEquals(config.rounds(), result.roundMetrics().size(), "Debe existir una metrica por ronda.");
        assertTrue(result.honestAgreementRatio() >= 0.0 && result.honestAgreementRatio() <= 1.0,
                "El ratio de acuerdo honesto debe estar normalizado.");
        assertTrue(result.censorshipSuccessRatio() >= 0.0 && result.censorshipSuccessRatio() <= 1.0,
                "El exito de censura observado debe estar normalizado.");

        ConsensusNetworkVisualizer visualizer = new ConsensusNetworkVisualizer();
        assertTrue(visualizer.renderAscii(result).contains("Red de consenso"),
                "La visualizacion ASCII de consenso debe producir texto legible.");
        assertTrue(visualizer.renderDot(result).contains("digraph consenso"),
                "La visualizacion DOT de consenso debe generarse.");
        Path csv = new ConsensusMetricsCsvExporter().export(result, Path.of("build", "test-reports", "consensus-rounds-test.csv"));
        assertTrue(Files.exists(csv), "El CSV de consenso por ronda debe escribirse en disco.");
    }


    private static void testAdvancedSharding() {
        Wallet alice = new Wallet("alice");
        Wallet bob = new Wallet("bob");
        Wallet carol = new Wallet("carol");
        Wallet dan = new Wallet("dan");
        ShardManager manager = new ShardManager(3, 4, 3);

        UTXO successUtxo = new UTXO(new byte[]{10, 1, 1}, 0);
        manager.getShard(0).getUtxoPool().addUTXO(successUtxo, new Transaction.Output(7000L, alice.getPublicKey()));
        CrossShardTransfer success = new CrossShardTransfer(0, 1, successUtxo, 4000L, bob.getPublicKey());
        CrossShardSession successSession = manager.beginAtomicTransfer(success, 3);
        assertTrue(manager.getShard(0).isLocked(successUtxo.key()), "El UTXO origen debe quedar bloqueado mientras esta pendiente.");
        assertTrue(manager.commitAtomicTransfer(success.id()), "El commit atomico debe completarse con quorum en destino.");
        assertTrue(successSession.status() == CrossShardStatus.COMMITTED, "La sesion exitosa debe terminar como COMMITTED.");
        assertFalse(manager.getShard(0).isLocked(successUtxo.key()), "El commit debe liberar el bloqueo del origen.");
        assertFalse(manager.getShard(0).getUtxoPool().contains(successUtxo), "El UTXO origen debe consumirse al confirmar.");
        assertTrue(manager.getShard(1).getUtxoPool().totalValue() >= 4000L, "El shard destino debe recibir el monto transferido.");

        UTXO timeoutUtxo = new UTXO(new byte[]{10, 2, 2}, 0);
        manager.getShard(1).getUtxoPool().addUTXO(timeoutUtxo, new Transaction.Output(8000L, carol.getPublicKey()));
        CrossShardTransfer timeout = new CrossShardTransfer(1, 2, timeoutUtxo, 5000L, dan.getPublicKey());
        CrossShardSession timeoutSession = manager.beginAtomicTransfer(timeout, 1);
        manager.advanceRounds(2);
        assertTrue(timeoutSession.status() == CrossShardStatus.TIMED_OUT, "La sesion sin commit debe expirar por timeout.");
        assertFalse(manager.getShard(1).isLocked(timeoutUtxo.key()), "El timeout debe liberar el bloqueo del origen.");
        assertTrue(manager.getShard(1).getUtxoPool().contains(timeoutUtxo), "El timeout no debe consumir el UTXO origen.");

        UTXO failedUtxo = new UTXO(new byte[]{10, 3, 3}, 0);
        manager.getShard(0).getUtxoPool().addUTXO(failedUtxo, new Transaction.Output(6000L, alice.getPublicKey()));
        CrossShardTransfer failed = new CrossShardTransfer(0, 2, failedUtxo, 3000L, bob.getPublicKey());
        CrossShardSession failedSession = manager.beginAtomicTransfer(failed, 3);
        manager.setShardOnline(2, false);
        assertFalse(manager.commitAtomicTransfer(failed.id()), "El commit debe fallar si el shard destino no alcanza quorum.");
        assertTrue(failedSession.status() == CrossShardStatus.FAILED_VALIDATION, "La sesion debe registrar fallo de validacion.");
        assertFalse(manager.getShard(0).isLocked(failedUtxo.key()), "El fallo de validacion debe liberar el bloqueo del origen.");
        manager.setShardOnline(2, true);

        Path csv = new ShardMetricsCsvExporter().export(manager.getRoundMetrics(), Path.of("build", "test-reports", "sharding-rounds-test.csv"));
        assertTrue(Files.exists(csv), "El CSV de sharding por ronda debe escribirse en disco.");
        assertTrue(new ShardVisualizer().renderAscii(manager).contains("Transferencias cross-shard"),
                "La visualizacion de shards debe incluir sesiones cross-shard.");
    }

    private static void testVisualizationAndCsvExport() {
        Wallet miner = new Wallet("minero");
        BlockChain chain = DemoData.createChainWithGenesis(miner);
        String forks = new ForkTreeVisualizer().renderAscii(chain);
        assertTrue(forks.contains("Arbol de forks"), "La visualizacion de forks debe producir texto legible.");
        String forkDot = new ForkTreeVisualizer().renderDot(chain);
        assertTrue(forkDot.contains("digraph forks"), "La visualizacion DOT de forks debe generarse.");

        ShardManager manager = new ShardManager(2);
        String shards = new ShardVisualizer().renderAscii(manager);
        assertTrue(shards.contains("Mapa de shards"), "La visualizacion de shards debe producir texto legible.");

        SimulationMetrics metrics = new SimulationMetrics();
        metrics.put("prueba", 123L);
        Path out = new CsvExporter().export(metrics, Path.of("build", "test-reports", "metrics-test.csv"));
        assertTrue(Files.exists(out), "El CSV de metricas debe escribirse en disco.");
    }

    private static void testSecurityAttacks() {
        assertTrue(new DoubleSpendAttack().run().defenseWorked(), "La defensa contra doble gasto debe pasar.");
        assertTrue(new InvalidSignatureAttack().run().defenseWorked(), "La defensa contra firma invalida debe pasar.");
        assertTrue(new CrossShardReplayAttack().run().defenseWorked(), "La defensa contra replay cross-shard debe pasar.");
        assertTrue(new CrossShardTimeoutAttack().run().defenseWorked(), "La defensa contra timeout cross-shard debe pasar.");
    }

    private static void testPropertyBasedSecuritySuite() {
        SecurityScoreReport report = new PropertyBasedSecuritySuite(2026L, 5).runAll();
        assertTrue(report.allPassed(), "La suite property-based de seguridad no debe detectar fallas.");
        assertTrue(report.score() == 100.0, "El security score debe ser 100 cuando no hay fallas.");
        assertTrue(report.totalIterations() >= 25, "La suite debe ejecutar multiples propiedades pseudoaleatorias.");
        Path csv = new SecurityReportCsvExporter().export(report, Path.of("build", "test-reports", "security-report-test.csv"));
        assertTrue(Files.exists(csv), "El CSV de seguridad debe escribirse en disco.");
    }



    private static Transaction createSyntheticSpend(UTXOPool pool,
                                                    Wallet owner,
                                                    Wallet recipient,
                                                    int seed,
                                                    long inputValue,
                                                    long fee,
                                                    int extraOutputs) {
        UTXO utxo = syntheticUtxo(seed);
        Transaction.Output previous = new Transaction.Output(inputValue, owner.getPublicKey());
        pool.addUTXO(utxo, previous);

        long valueForOutputs = inputValue - fee;
        if (valueForOutputs <= extraOutputs) {
            throw new IllegalArgumentException("Fondos insuficientes para construir la transaccion sintetica.");
        }

        Transaction tx = new Transaction();
        tx.addInput(utxo.getTxHash(), utxo.getOutputIndex());
        for (int i = 0; i < extraOutputs; i++) {
            tx.addOutput(1L, recipient.getPublicKey());
        }
        tx.addOutput(valueForOutputs - extraOutputs, recipient.getPublicKey());
        tx.signInput(0, owner.getPrivateKey());
        tx.finalizeTransaction();
        return tx;
    }

    private static UTXO syntheticUtxo(int seed) {
        return new UTXO(new byte[]{(byte) seed, (byte) (seed * 3), (byte) (seed * 7)}, 0);
    }


    private static void assertBefore(List<String> labels, String first, String second, String message) {
        int firstIndex = labels.indexOf(first);
        int secondIndex = labels.indexOf(second);
        if (firstIndex < 0 || secondIndex < 0 || firstIndex >= secondIndex) {
            throw new AssertionError(message + " Orden=" + labels);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError(message);
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " Esperado=" + expected + ", actual=" + actual);
        }
    }
}
