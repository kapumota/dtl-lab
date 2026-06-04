package dltlab.app;

import dltlab.blockchain.Block;
import dltlab.blockchain.BlockChain;
import dltlab.consensus.AdvancedConsensusResult;
import dltlab.consensus.AdvancedConsensusSimulator;
import dltlab.consensus.ConsensusConfig;
import dltlab.consensus.ConsensusMetricsCsvExporter;
import dltlab.crypto.Hashing;
import dltlab.defi.AmmPool;
import dltlab.defi.ArbitrageScenario;
import dltlab.defi.ConstantProductMarketMaker;
import dltlab.defi.SwapOrder;
import dltlab.defi.SwapResult;
import dltlab.defi.Token;
import dltlab.mempool.FifoPolicy;
import dltlab.transaction.TransactionSizeEstimator;
import dltlab.transaction.FeeCalculator;
import dltlab.mempool.TransactionMempool;
import dltlab.mempool.MempoolConfig;
import dltlab.mempool.MempoolAdmissionResult;
import dltlab.mempool.FeeRatePolicy;
import dltlab.mempool.HighestFeePolicy;
import dltlab.mempool.MEVAwarePolicy;
import dltlab.mempool.MempoolPolicy;
import dltlab.mempool.PackageAwarePolicy;
import dltlab.mempool.SelectionReport;
import dltlab.metrics.CsvExporter;
import dltlab.mev.MEVDemoFactory;
import dltlab.mev.MEVMetricsCsvExporter;
import dltlab.mev.MEVScenario;
import dltlab.mev.MEVScenarioResult;
import dltlab.mev.MEVSimulator;
import dltlab.metrics.ReportFiles;
import dltlab.metrics.SimulationMetrics;
import dltlab.mev.BackrunArbitrageResult;
import dltlab.mev.BackrunArbitrageSimulator;
import dltlab.mev.DeFiMEVScenario;
import dltlab.mev.SandwichAttackResult;
import dltlab.mev.SandwichAttackSimulator;
import dltlab.mining.Miner;
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
import dltlab.sharding.ShardManager;
import dltlab.sharding.ShardMetricsCsvExporter;
import dltlab.transaction.Transaction;
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
import dltlab.visualization.ConsensusNetworkVisualizer;
import dltlab.visualization.ForkTreeVisualizer;
import dltlab.visualization.ShardVisualizer;
import dltlab.wallet.Wallet;
import dltlab.wallet.WalletRegistry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Ejecuta una demo integrada de los modulos principales del laboratorio. */
public class DemoRunner {
    public void runFullDemo() {
        SimulationMetrics metrics = new SimulationMetrics();
        System.out.println("Demostración de DLT-Lab");
        System.out.println("--------------------------");

        System.out.println("[1] Creando wallets...");
        WalletRegistry registry = new WalletRegistry();
        Wallet minerWallet = registry.create("minero");
        Wallet alice = registry.create("alice");
        Wallet bob = registry.create("bob");
        Wallet carol = registry.create("carol");
        Wallet dan = registry.create("dan");
        metrics.put("wallets_creadas", registry.getWallets().size());
        System.out.println("    Wallets creadas: " + registry.getWallets().size());
        System.out.println("    Direccion corta del minero: " + minerWallet.shortAddress());

        System.out.println("\n[2] Creando blockchain tipo Bitcoin y primera transaccion...");
        BlockChain chain = DemoData.createChainWithGenesis(minerWallet);
        Block genesis = chain.getMaxHeightBlock();
        UTXOPool genesisPool = chain.getMaxHeightUTXOPool();
        UTXO coinbase = DemoData.firstUtxo(genesisPool);
        Transaction txToAlice = minerWallet.createSpend(coinbase, genesisPool.getOutput(coinbase), alice.getPublicKey(), 8_000_000L, 1500L);
        chain.addTransaction(txToAlice);
        Miner firstMiner = new Miner(minerWallet, new HighestFeePolicy(), 5);
        boolean minedFirst = firstMiner.mineAndAdd(chain);
        metrics.put("primer_bloque_minado", minedFirst ? 1 : 0);
        metrics.put("altura_despues_primer_bloque", chain.getMaxHeight());
        System.out.println("    Primer bloque minado: " + minedFirst + ", altura actual: " + chain.getMaxHeight());

        System.out.println("\n[3] Generando mempool con dependencias padre-hijo...");
        UTXOPool currentPool = chain.getMaxHeightUTXOPool();
        Transaction independentTx = DemoData.spendFirstOwned(currentPool, minerWallet, dan, 1_000_000L, 20_000L);
        Transaction parentTx = DemoData.spendFirstOwned(currentPool, alice, bob, 1_000_000L, 100L);
        UTXO parentOutput = new UTXO(parentTx.getHash(), 0);
        Transaction childTx = bob.createSpend(parentOutput, parentTx.getOutputs().get(0), carol.getPublicKey(), 600_000L, 300_000L);

        // Orden intencional: primero una transaccion independiente, luego la hija, luego la padre.
        // Asi se ve que FIFO y highest-fee pueden perder el paquete rentable si el bloque es pequeño.
        chain.addTransaction(independentTx);
        chain.addTransaction(childTx);
        chain.addTransaction(parentTx);
        metrics.put("mempool_tx_iniciales", chain.getTransactionPool().size());
        System.out.println("    Transacciones en mempool: " + chain.getTransactionPool().size());
        System.out.println("    Paquete educativo: padre fee=100, hija fee=300000, independiente fee=20000");

        SelectionReport fifo = comparePolicy(new FifoPolicy(), chain, 2);
        SelectionReport highest = comparePolicy(new HighestFeePolicy(), chain, 2);
        SelectionReport packageAware = comparePolicy(new PackageAwarePolicy(), chain, 2);
        MEVAwarePolicy mevPolicy = new MEVAwarePolicy();
        mevPolicy.registerOpportunity(childTx, 50_000L);
        SelectionReport mev = comparePolicy(mevPolicy, chain, 2);
        metrics.put("fee_fifo", fifo.totalFee());
        metrics.put("fee_highest_fee", highest.totalFee());
        metrics.put("fee_package_aware", packageAware.totalFee());
        metrics.put("fee_mev_simplificado", mev.totalFee());

        System.out.println("\n[4] Ejecutando MEV basico completo...");
        MEVSimulator mevSimulator = new MEVSimulator();
        List<MEVScenarioResult> mevResults = new ArrayList<>();
        for (MEVScenario scenario : new MEVDemoFactory().createScenarios()) {
            MEVScenarioResult result = mevSimulator.evaluate(scenario);
            mevResults.add(result);
            metrics.put("mev_" + slug(result.scenarioName()) + "_valor_extraido", result.extractedValue());
            metrics.put("mev_" + slug(result.scenarioName()) + "_diferencia_ingreso", result.revenueDelta());
            System.out.println(result.render().indent(4));
        }

        System.out.println("\n[5] Ejecutando MEV DeFi con AMM constante...");
        SandwichAttackResult sandwich = new SandwichAttackSimulator().simulate(defaultSandwichScenario());
        BackrunArbitrageResult arbitrage = new BackrunArbitrageSimulator().simulate(defaultArbitrageScenario());
        metrics.put("defi_sandwich_ganancia_atacante", sandwich.attackerProfit());
        metrics.put("defi_sandwich_perdida_victima", sandwich.victimLossInInputToken());
        metrics.put("defi_sandwich_pago_productor", sandwich.builderPayment());
        metrics.put("defi_arbitraje_ganancia", arbitrage.profit());
        System.out.println(sandwich.render().indent(4));
        System.out.println(arbitrage.render().indent(4));

        System.out.println("\n[6] Minando con package-aware y creando un fork...");
        Miner packageMiner = new Miner(minerWallet, new PackageAwarePolicy(), 2);
        boolean minedSecond = packageMiner.mineAndAdd(chain);
        Block fork = new Block(genesis.getHash(), carol.getPublicKey(), List.of(), 2);
        boolean forkAccepted = chain.addBlock(fork);
        metrics.put("segundo_bloque_minado", minedSecond ? 1 : 0);
        metrics.put("fork_alternativo_aceptado", forkAccepted ? 1 : 0);
        metrics.put("altura_maxima", chain.getMaxHeight());
        metrics.put("bloques_conocidos", chain.getKnownBlocks().size());
        System.out.println("    Segundo bloque minado: " + minedSecond);
        System.out.println("    Fork alternativo aceptado: " + forkAccepted);
        System.out.println("    Altura maxima actual: " + chain.getMaxHeight());
        System.out.println("    Bloques conocidos recientes: " + chain.getKnownBlocks().size());

        System.out.println("\n[7] Visualizando arbol de forks...");
        ForkTreeVisualizer forkVisualizer = new ForkTreeVisualizer();
        String forkAscii = forkVisualizer.renderAscii(chain);
        System.out.println(forkAscii.indent(4));

        System.out.println("\n[8] Ejecutando consenso avanzado con trust graph, censura y equivocacion...");
        Set<Transaction> universe = new HashSet<>(List.of(txToAlice, parentTx, childTx, independentTx));
        ConsensusConfig consensusConfig = ConsensusConfig.educationalDefault();
        AdvancedConsensusResult consensus = new AdvancedConsensusSimulator().run(universe, consensusConfig, new Random(7));
        metrics.put("consenso_nodos_totales", consensus.totalNodes());
        metrics.put("consenso_nodos_honestos", consensus.honestNodes());
        metrics.put("consenso_nodos_maliciosos", consensus.maliciousNodes());
        metrics.put("consenso_nodos_censores", consensus.censoringNodes());
        metrics.put("consenso_nodos_equivocadores", consensus.equivocatingNodes());
        metrics.put("consenso_grupo_honesto_mayoritario", consensus.largestConsensusGroup());
        metrics.put("consenso_ratio_acuerdo_honesto", consensus.honestAgreementRatio());
        metrics.put("consenso_exito_censura", consensus.censorshipSuccessRatio());
        System.out.printf("    Nodos: %d, honestos: %d, maliciosos: %d%n",
                consensus.totalNodes(), consensus.honestNodes(), consensus.maliciousNodes());
        System.out.printf("    Censores: %d, equivocadores: %d, silenciosos: %d%n",
                consensus.censoringNodes(), consensus.equivocatingNodes(), consensus.silentNodes());
        System.out.printf("    Grupo honesto mayoritario final: %d/%d%n",
                consensus.largestConsensusGroup(), consensus.honestNodes());
        System.out.printf("    Ratio de acuerdo honesto final: %.2f%%%n", consensus.honestAgreementRatio() * 100.0);
        System.out.printf("    Exito de censura observado: %.2f%%%n", consensus.censorshipSuccessRatio() * 100.0);

        ConsensusNetworkVisualizer consensusVisualizer = new ConsensusNetworkVisualizer();
        String consensusAscii = consensusVisualizer.renderAscii(consensus);
        System.out.println("    Ultimas metricas por ronda:");
        consensus.roundMetrics().stream().skip(Math.max(0, consensus.roundMetrics().size() - 3)).forEach(metric ->
                System.out.printf("      Ronda %d: acuerdo honesto %.2f%%, mensajes=%d, grupos=%d%n",
                        metric.round(), metric.honestAgreementRatio() * 100.0, metric.totalMessages(), metric.consensusGroups()));

        System.out.println("\n[9] Simulando sharding avanzado con commit atomico, timeouts y fallos...");
        ShardManager shardManager = buildAdvancedShardingDemo(alice, bob, carol, dan);
        metrics.put("shards", shardManager.getShards().size());
        metrics.put("sharding_sesiones", shardManager.getSessions().size());
        metrics.put("sharding_confirmadas", countSessions(shardManager, CrossShardStatus.COMMITTED));
        metrics.put("sharding_timeout", countSessions(shardManager, CrossShardStatus.TIMED_OUT));
        metrics.put("sharding_fallo_validacion", countSessions(shardManager, CrossShardStatus.FAILED_VALIDATION));
        System.out.println("    Sesiones cross-shard: " + shardManager.getSessions().size());
        System.out.println("    Confirmadas: " + countSessions(shardManager, CrossShardStatus.COMMITTED));
        System.out.println("    Timeouts: " + countSessions(shardManager, CrossShardStatus.TIMED_OUT));
        System.out.println("    Fallos de validacion: " + countSessions(shardManager, CrossShardStatus.FAILED_VALIDATION));
        for (CrossShardSession session : shardManager.getSessions()) {
            System.out.println("    Transferencia " + session.transfer().id().substring(0, 10)
                    + " " + session.transfer().sourceShardId() + "->" + session.transfer().targetShardId()
                    + " estado=" + session.status() + " motivo=" + session.reason());
        }

        System.out.println("\n[10] Visualizando shards...");
        ShardVisualizer shardVisualizer = new ShardVisualizer();
        String shardAscii = shardVisualizer.renderAscii(shardManager);
        System.out.println(shardAscii.indent(4));

        System.out.println("\n[11] Ejecutando ataques educativos...");
        System.out.println("    " + new DoubleSpendAttack().run().render().replace("\n", " | "));
        System.out.println("    " + new InvalidSignatureAttack().run().render().replace("\n", " | "));
        System.out.println("    " + new CrossShardReplayAttack().run().render().replace("\n", " | "));
        System.out.println("    " + new CrossShardTimeoutAttack().run().render().replace("\n", " | "));

        System.out.println("\n[12] Verificando invariantes...");
        VerificationReport report = verificationReport(chain, shardManager);
        metrics.put("invariantes_ejecutadas", 4);
        System.out.println(report.render());

        System.out.println("[13] Ejecutando suite de seguridad property-based...");
        SecurityScoreReport securityReport = new PropertyBasedSecuritySuite(2026L, 6).runAll();
        metrics.put("security_score", securityReport.score());
        metrics.put("security_iteraciones", securityReport.totalIterations());
        metrics.put("security_fallas", securityReport.totalFailed());
        System.out.println(securityReport.render());

        System.out.println("[14] Exportando reportes...");
        Path metricsPath = new CsvExporter().export(metrics, Path.of("reports", "metrics.csv"));
        Path mevMetricsPath = new MEVMetricsCsvExporter().export(mevResults, Path.of("reports", "mev_metrics.csv"));
        Path forksTxt = ReportFiles.write(Path.of("reports", "forks.txt"), forkAscii);
        Path forksDot = ReportFiles.write(Path.of("reports", "forks.dot"), forkVisualizer.renderDot(chain));
        Path shardsTxt = ReportFiles.write(Path.of("reports", "shards.txt"), shardAscii);
        Path shardsDot = ReportFiles.write(Path.of("reports", "shards.dot"), shardVisualizer.renderDot(shardManager));
        Path consensusCsv = new ConsensusMetricsCsvExporter().export(consensus, Path.of("reports", "consensus_rounds.csv"));
        Path consensusTxt = ReportFiles.write(Path.of("reports", "consensus_network.txt"), consensusAscii);
        Path consensusDot = ReportFiles.write(Path.of("reports", "consensus_network.dot"), consensusVisualizer.renderDot(consensus));
        Path shardingCsv = new ShardMetricsCsvExporter().export(shardManager.getRoundMetrics(), Path.of("reports", "sharding_rounds.csv"));
        Path securityCsv = new SecurityReportCsvExporter().export(securityReport, Path.of("reports", "security_report.csv"));
        Path securityTxt = ReportFiles.write(Path.of("reports", "security_report.txt"), securityReport.render());
        System.out.println("    CSV de metricas: " + metricsPath);
        System.out.println("    CSV especifico de MEV: " + mevMetricsPath);
        System.out.println("    CSV de consenso por ronda: " + consensusCsv);
        System.out.println("    CSV de sharding por ronda: " + shardingCsv);
        System.out.println("    CSV de seguridad/verificacion: " + securityCsv);
        System.out.println("    Reporte de seguridad TXT: " + securityTxt);
        System.out.println("    Visualizacion forks TXT: " + forksTxt);
        System.out.println("    Visualizacion forks DOT: " + forksDot);
        System.out.println("    Visualizacion shards TXT: " + shardsTxt);
        System.out.println("    Visualizacion shards DOT: " + shardsDot);
        System.out.println("    Visualizacion consenso TXT: " + consensusTxt);
        System.out.println("    Visualizacion consenso DOT: " + consensusDot);
    }


    public void runMempoolEconomicsOnly() {
        System.out.println("Demostración DLT-Lab mempool economica");
        System.out.println("-------------------------------------");
        Wallet owner = new Wallet("dueno_mempool");
        Wallet recipient = new Wallet("receptor_mempool");
        UTXOPool pool = new UTXOPool();

        Transaction largeHighFee = createSyntheticSpend(pool, owner, recipient, 41, 1_000_000L, 8_000L, 160);
        Transaction smallBetterRate = createSyntheticSpend(pool, owner, recipient, 42, 1_000_000L, 4_000L, 0);
        long maxBlockVBytes = Math.max(
                TransactionSizeEstimator.virtualSize(largeHighFee),
                TransactionSizeEstimator.virtualSize(smallBetterRate)
        );
        List<Transaction> selected = new FeeRatePolicy().selectByVirtualSize(List.of(largeHighFee, smallBetterRate), pool, maxBlockVBytes);
        System.out.println("\n[1] Seleccion por fee rate y capacidad en vBytes");
        printTxEconomics("grande_fee_alto", largeHighFee, pool);
        printTxEconomics("pequena_fee_rate_alto", smallBetterRate, pool);
        System.out.println("    Capacidad de bloque en vBytes: " + maxBlockVBytes);
        System.out.println("    Seleccionada: " + selected.get(0).shortId());

        System.out.println("\n[2] Eviction de mempool por bajo fee rate");
        Transaction lowRate = createSyntheticSpend(pool, owner, recipient, 43, 1_000_000L, 1_000L, 0);
        Transaction highRate = createSyntheticSpend(pool, owner, recipient, 44, 1_000_000L, 8_000L, 0);
        long maxMempoolVBytes = Math.max(
                TransactionSizeEstimator.virtualSize(lowRate),
                TransactionSizeEstimator.virtualSize(highRate)
        ) + 1L;
        TransactionMempool evictionMempool = new TransactionMempool(new MempoolConfig(maxMempoolVBytes, 1L, true, true));
        System.out.println("    Admision baja: " + evictionMempool.admit(lowRate, pool).reason());
        MempoolAdmissionResult evictionResult = evictionMempool.admit(highRate, pool);
        System.out.println("    Admision alta: " + evictionResult.reason());
        System.out.println("    Descartadas: " + evictionResult.evictedTransactions().size());
        System.out.println("    Mempool conserva alta: " + evictionMempool.contains(highRate));

        System.out.println("\n[3] Reemplazo por fee con RBF");
        UTXO rbfUtxo = syntheticUtxo(45);
        Transaction.Output rbfOutput = new Transaction.Output(1_000_000L, owner.getPublicKey());
        pool.addUTXO(rbfUtxo, rbfOutput);
        Transaction original = owner.createSpend(rbfUtxo, rbfOutput, recipient.getPublicKey(), 100_000L, 1_000L);
        Transaction replacement = owner.createSpend(rbfUtxo, rbfOutput, recipient.getPublicKey(), 100_000L, 8_000L);
        TransactionMempool rbfMempool = new TransactionMempool(new MempoolConfig(100_000L, 1L, true, false));
        System.out.println("    Original: " + rbfMempool.admit(original, pool).reason());
        MempoolAdmissionResult rbfResult = rbfMempool.admit(replacement, pool);
        System.out.println("    Reemplazo: " + rbfResult.reason());
        System.out.println("    Mempool conserva reemplazo: " + rbfMempool.contains(replacement));

        System.out.println("\n[4] CPFP con paquete padre-hijo");
        Wallet alice = new Wallet("alice_cpfp");
        Wallet bob = new Wallet("bob_cpfp");
        UTXO parentInput = syntheticUtxo(46);
        Transaction.Output parentOutput = new Transaction.Output(1_000_000L, alice.getPublicKey());
        pool.addUTXO(parentInput, parentOutput);
        Transaction parent = alice.createSpend(parentInput, parentOutput, bob.getPublicKey(), 800_000L, 100L);
        Transaction child = bob.createSpend(new UTXO(parent.getHash(), 0), parent.getOutputs().get(0), recipient.getPublicKey(), 500_000L, 200_000L);
        long packageLimit = TransactionSizeEstimator.virtualSize(parent) + TransactionSizeEstimator.virtualSize(child);
        List<Transaction> packageSelected = new PackageAwarePolicy().selectByVirtualSize(List.of(child, parent), pool, packageLimit);
        System.out.println("    Paquete seleccionado: " + packageSelected.size() + " transacciones");
        System.out.println("    Incluye padre: " + packageSelected.stream().anyMatch(tx -> tx.id().equals(parent.id())));
        System.out.println("    Incluye hija: " + packageSelected.stream().anyMatch(tx -> tx.id().equals(child.id())));
    }


    public void runMevOnly() {
        System.out.println("Demostración MEV DLT-Lab");
        System.out.println("----------------------");
        MEVSimulator simulator = new MEVSimulator();
        List<MEVScenarioResult> results = new ArrayList<>();
        for (MEVScenario scenario : new MEVDemoFactory().createScenarios()) {
            MEVScenarioResult result = simulator.evaluate(scenario);
            results.add(result);
            System.out.println(result.render());
        }
        Path out = new MEVMetricsCsvExporter().export(results, Path.of("reports", "mev_metrics.csv"));
        System.out.println("CSV especifico de MEV: " + out);

        System.out.println();
        runDefiMevOnly();
    }

    public void runDefiMevOnly() {
        System.out.println("Demostración DeFi MEV DLT-Lab");
        System.out.println("-----------------------------");
        DeFiMEVScenario sandwichScenario = defaultSandwichScenario();
        SandwichAttackResult sandwich = new SandwichAttackSimulator().simulate(sandwichScenario);
        System.out.println(sandwich.render());

        ArbitrageScenario arbitrageScenario = defaultArbitrageScenario();
        BackrunArbitrageResult arbitrage = new BackrunArbitrageSimulator().simulate(arbitrageScenario);
        System.out.println(arbitrage.render());

        Path report = ReportFiles.write(Path.of("reports", "defi_mev_report.txt"),
                sandwich.render() + System.lineSeparator() + arbitrage.render());
        System.out.println("Reporte DeFi MEV TXT: " + report);
    }

    public void runConsensusOnly() {
        System.out.println("Demostración DLT-Lab Consenso avanzado");
        System.out.println("------------------------------------");
        Wallet miner = new Wallet("minero");
        Wallet alice = new Wallet("alice");
        Wallet bob = new Wallet("bob");
        BlockChain chain = DemoData.createChainWithGenesis(miner);
        UTXOPool pool = chain.getMaxHeightUTXOPool();
        UTXO coinbase = DemoData.firstUtxo(pool);
        Transaction tx1 = miner.createSpend(coinbase, pool.getOutput(coinbase), alice.getPublicKey(), 2_000_000L, 1000L);
        Transaction tx2 = miner.createSpend(coinbase, pool.getOutput(coinbase), bob.getPublicKey(), 1_500_000L, 2000L);
        Transaction tx3 = miner.createSpend(coinbase, pool.getOutput(coinbase), alice.getPublicKey(), 1_000_000L, 3000L);
        Set<Transaction> universe = new HashSet<>(List.of(tx1, tx2, tx3));

        AdvancedConsensusResult result = new AdvancedConsensusSimulator()
                .run(universe, ConsensusConfig.educationalDefault(), new Random(19));
        ConsensusNetworkVisualizer visualizer = new ConsensusNetworkVisualizer();
        System.out.println(visualizer.renderAscii(result));
        Path csv = new ConsensusMetricsCsvExporter().export(result, Path.of("reports", "consensus_rounds.csv"));
        Path txt = ReportFiles.write(Path.of("reports", "consensus_network.txt"), visualizer.renderAscii(result));
        Path dot = ReportFiles.write(Path.of("reports", "consensus_network.dot"), visualizer.renderDot(result));
        System.out.println("CSV de consenso por ronda: " + csv);
        System.out.println("Visualizacion consenso TXT: " + txt);
        System.out.println("Visualizacion consenso DOT: " + dot);
    }


    public void runShardingOnly() {
        System.out.println("Demostración DLT-Lab sharding avanzado");
        System.out.println("------------------------------------");
        Wallet alice = new Wallet("alice");
        Wallet bob = new Wallet("bob");
        Wallet carol = new Wallet("carol");
        Wallet dan = new Wallet("dan");
        ShardManager shardManager = buildAdvancedShardingDemo(alice, bob, carol, dan);
        ShardVisualizer visualizer = new ShardVisualizer();
        String ascii = visualizer.renderAscii(shardManager);
        System.out.println(ascii);
        Path csv = new ShardMetricsCsvExporter().export(shardManager.getRoundMetrics(), Path.of("reports", "sharding_rounds.csv"));
        Path txt = ReportFiles.write(Path.of("reports", "shards.txt"), ascii);
        Path dot = ReportFiles.write(Path.of("reports", "shards.dot"), visualizer.renderDot(shardManager));
        System.out.println("CSV de sharding por ronda: " + csv);
        System.out.println("Visualizacion shards TXT: " + txt);
        System.out.println("Visualizacion shards DOT: " + dot);
    }

    public void runVerificationOnly() {
        Wallet miner = new Wallet("minero");
        BlockChain chain = DemoData.createChainWithGenesis(miner);
        VerificationReport report = verificationReport(chain, new ShardManager(2));
        System.out.println(report.render());
    }

    public void runSecurityOnly() {
        System.out.println("Demostración de DLT-Lab seguridad y verificacion");
        System.out.println("------------------------------------------");
        SecurityScoreReport report = new PropertyBasedSecuritySuite(2026L, 8).runAll();
        System.out.println(report.render());
        Path csv = new SecurityReportCsvExporter().export(report, Path.of("reports", "security_report.csv"));
        Path txt = ReportFiles.write(Path.of("reports", "security_report.txt"), report.render());
        System.out.println("CSV de seguridad/verificacion: " + csv);
        System.out.println("Reporte de seguridad TXT: " + txt);
        if (!report.allPassed()) {
            throw new IllegalStateException("La suite de seguridad detecto fallas. Revisar reports/security_report.csv");
        }
    }


    private DeFiMEVScenario defaultSandwichScenario() {
        Token usdc = Token.of("USDC", 6);
        Token eth = Token.of("ETH", 18);
        AmmPool pool = new AmmPool("USDC-ETH principal", usdc, eth, 1_000_000.0, 500.0, 30);
        SwapOrder victim = new SwapOrder("usuario_swap", usdc, 50_000.0, 0.0);
        SwapOrder attackerFrontRun = new SwapOrder("bot_compra_antes", usdc, 20_000.0, 0.0);
        return new DeFiMEVScenario("Sandwich USDC-ETH", pool, victim, attackerFrontRun, 0.20);
    }

    private ArbitrageScenario defaultArbitrageScenario() {
        Token usdc = Token.of("USDC", 6);
        Token eth = Token.of("ETH", 18);
        AmmPool cheapEthPool = new AmmPool("Pool ETH barato", usdc, eth, 1_000_000.0, 520.0, 30);
        AmmPool expensiveEthPool = new AmmPool("Pool ETH caro", usdc, eth, 1_000_000.0, 480.0, 30);
        return new ArbitrageScenario("Backrun por desbalance USDC-ETH", cheapEthPool, expensiveEthPool, usdc, 10_000.0);
    }

    private SwapResult quoteSwap(AmmPool pool, SwapOrder order) {
        return new ConstantProductMarketMaker().quote(pool, order);
    }

    private Transaction createSyntheticSpend(UTXOPool pool,
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

    private UTXO syntheticUtxo(int seed) {
        return new UTXO(new byte[]{(byte) seed, (byte) (seed * 3), (byte) (seed * 7)}, 0);
    }

    private void printTxEconomics(String label, Transaction tx, UTXOPool pool) {
        System.out.printf("    %s: fee=%d, vBytes=%d, feeRate=%.4f sats/vByte%n",
                label,
                FeeCalculator.fee(tx, pool),
                TransactionSizeEstimator.virtualSize(tx),
                FeeCalculator.feeRate(tx, pool).satsPerVByte());
    }


    private ShardManager buildAdvancedShardingDemo(Wallet alice, Wallet bob, Wallet carol, Wallet dan) {
        ShardManager shardManager = new ShardManager(3, 4, 3);

        UTXO successUtxo = new UTXO(Hashing.sha256("utxo-shard-success".getBytes()), 0);
        UTXO timeoutUtxo = new UTXO(Hashing.sha256("utxo-shard-timeout".getBytes()), 0);
        UTXO failedUtxo = new UTXO(Hashing.sha256("utxo-shard-failed".getBytes()), 0);
        shardManager.getShard(0).getUtxoPool().addUTXO(successUtxo, new Transaction.Output(7000L, alice.getPublicKey()));
        shardManager.getShard(1).getUtxoPool().addUTXO(timeoutUtxo, new Transaction.Output(8000L, bob.getPublicKey()));
        shardManager.getShard(0).getUtxoPool().addUTXO(failedUtxo, new Transaction.Output(6000L, carol.getPublicKey()));

        CrossShardTransfer success = new CrossShardTransfer(0, 1, successUtxo, 4000L, dan.getPublicKey());
        shardManager.beginAtomicTransfer(success, 3);
        shardManager.commitAtomicTransfer(success.id());

        CrossShardTransfer timeout = new CrossShardTransfer(1, 2, timeoutUtxo, 5000L, alice.getPublicKey());
        shardManager.beginAtomicTransfer(timeout, 1);
        shardManager.advanceRounds(2);

        CrossShardTransfer failed = new CrossShardTransfer(0, 2, failedUtxo, 3000L, bob.getPublicKey());
        shardManager.beginAtomicTransfer(failed, 3);
        shardManager.setShardOnline(2, false);
        shardManager.commitAtomicTransfer(failed.id());
        shardManager.setShardOnline(2, true);

        return shardManager;
    }

    private int countSessions(ShardManager manager, CrossShardStatus status) {
        int count = 0;
        for (CrossShardSession session : manager.getSessions()) {
            if (session.status() == status) count++;
        }
        return count;
    }

    private SelectionReport comparePolicy(MempoolPolicy policy, BlockChain chain, int maxCount) {
        List<Transaction> selected = policy.select(chain.getTransactionPool().getAll(), chain.getMaxHeightUTXOPool(), maxCount);
        SelectionReport report = SelectionReport.from(policy.name(), selected, chain.getMaxHeightUTXOPool());
        System.out.println("    Politica " + report.policyName() + ": selecciona " + report.selectedCount()
                + " tx, fee efectivo " + report.totalFee() + ", orden " + shortOrder(selected));
        return report;
    }

    private String slug(String text) {
        return text.toLowerCase(java.util.Locale.ROOT)
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String shortOrder(List<Transaction> selected) {
        List<String> ids = new ArrayList<>();
        for (Transaction tx : selected) {
            ids.add(tx.shortId());
        }
        return ids.toString();
    }

    private VerificationReport verificationReport(BlockChain chain, ShardManager shardManager) {
        List<Invariant> invariants = new ArrayList<>();
        invariants.add(new NoNegativeUtxoInvariant());
        invariants.add(new GenesisParentInvariant());
        invariants.add(new NoReceiptReplayInvariant());
        invariants.add(new NoStuckCrossShardInvariant());
        return new InvariantChecker().check(new LedgerState(chain, shardManager), invariants);
    }
}
