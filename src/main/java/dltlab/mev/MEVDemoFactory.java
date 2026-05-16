package dltlab.mev;

import dltlab.crypto.Hashing;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.transaction.UTXOPool;
import dltlab.wallet.Wallet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Construye escenarios MEV reproducibles para la demo y las pruebas. */
public class MEVDemoFactory {
    public List<MEVScenario> createScenarios() {
        List<MEVScenario> scenarios = new ArrayList<>();
        scenarios.add(frontRunningScenario());
        scenarios.add(backRunningScenario());
        scenarios.add(sandwichScenario());
        return scenarios;
    }

    private MEVScenario frontRunningScenario() {
        Wallet user = new Wallet("usuario-front");
        Wallet normal = new Wallet("usuario-normal");
        Wallet searcher = new Wallet("buscador-front");
        Wallet sink = new Wallet("receptor-front");
        UTXOPool pool = new UTXOPool();

        Transaction victim = spendFromFreshUtxo(pool, "front-victim", user, sink, 800_000L, 1_000L);
        Transaction normalTx = spendFromFreshUtxo(pool, "front-normal", normal, sink, 500_000L, 2_000L);
        Transaction frontRun = spendFromFreshUtxo(pool, "front-bot", searcher, sink, 200_000L, 5_000L);

        Map<String, Transaction> txs = new LinkedHashMap<>();
        txs.put("usuario_trade", victim);
        txs.put("tx_normal", normalTx);
        txs.put("bot_front_run", frontRun);

        return new MEVScenario(
                "Front-running abstracto",
                txs,
                List.of("usuario_trade", "tx_normal", "bot_front_run"),
                List.of(MEVOpportunity.frontRun(
                        "usuario_trade",
                        "bot_front_run",
                        35_000L,
                        "El productor del bloque mueve la transaccion del bot antes de la orden visible del usuario."
                )),
                pool
        );
    }

    private MEVScenario backRunningScenario() {
        Wallet actor = new Wallet("actor-back");
        Wallet normal = new Wallet("normal-back");
        Wallet searcher = new Wallet("buscador-back");
        Wallet sink = new Wallet("receptor-back");
        UTXOPool pool = new UTXOPool();

        Transaction target = spendFromFreshUtxo(pool, "back-target", actor, sink, 700_000L, 1_500L);
        Transaction normalTx = spendFromFreshUtxo(pool, "back-normal", normal, sink, 450_000L, 2_000L);
        Transaction backRun = spendFromFreshUtxo(pool, "back-bot", searcher, sink, 250_000L, 6_000L);

        Map<String, Transaction> txs = new LinkedHashMap<>();
        txs.put("evento_objetivo", target);
        txs.put("tx_normal", normalTx);
        txs.put("bot_back_run", backRun);

        return new MEVScenario(
                "Back-running abstracto",
                txs,
                List.of("evento_objetivo", "tx_normal", "bot_back_run"),
                List.of(MEVOpportunity.backRun(
                        "evento_objetivo",
                        "bot_back_run",
                        42_000L,
                        "El bot se coloca inmediatamente despues de un evento que cambia el estado economico."
                )),
                pool
        );
    }

    private MEVScenario sandwichScenario() {
        Wallet user = new Wallet("usuario-sandwich");
        Wallet normal = new Wallet("normal-sandwich");
        Wallet searcherA = new Wallet("buscador-compra");
        Wallet searcherB = new Wallet("buscador-venta");
        Wallet sink = new Wallet("receptor-sandwich");
        UTXOPool pool = new UTXOPool();

        Transaction victim = spendFromFreshUtxo(pool, "sandwich-victim", user, sink, 900_000L, 1_000L);
        Transaction normalTx = spendFromFreshUtxo(pool, "sandwich-normal", normal, sink, 500_000L, 1_500L);
        Transaction before = spendFromFreshUtxo(pool, "sandwich-before", searcherA, sink, 300_000L, 7_000L);
        Transaction after = spendFromFreshUtxo(pool, "sandwich-after", searcherB, sink, 300_000L, 7_000L);

        Map<String, Transaction> txs = new LinkedHashMap<>();
        txs.put("usuario_swap", victim);
        txs.put("tx_normal", normalTx);
        txs.put("bot_compra_antes", before);
        txs.put("bot_venta_despues", after);

        return new MEVScenario(
                "Sandwich abstracto",
                txs,
                List.of("usuario_swap", "tx_normal", "bot_compra_antes", "bot_venta_despues"),
                List.of(MEVOpportunity.sandwich(
                        "usuario_swap",
                        "bot_compra_antes",
                        "bot_venta_despues",
                        80_000L,
                        "El bot rodea la operacion del usuario con una transaccion antes y otra despues."
                )),
                pool
        );
    }

    private Transaction spendFromFreshUtxo(UTXOPool pool, String seed, Wallet owner, Wallet recipient,
                                           long amount, long fee) {
        UTXO utxo = new UTXO(Hashing.sha256(seed.getBytes(java.nio.charset.StandardCharsets.UTF_8)), 0);
        Transaction.Output output = new Transaction.Output(amount + fee + 100_000L, owner.getPublicKey());
        pool.addUTXO(utxo, output);
        return owner.createSpend(utxo, output, recipient.getPublicKey(), amount, fee);
    }
}
