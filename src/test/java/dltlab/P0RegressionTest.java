package dltlab;

import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.Receipt;
import dltlab.sharding.ShardManager;
import dltlab.transaction.Transaction;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXO;
import dltlab.transaction.UTXOPool;
import dltlab.wallet.Wallet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class P0RegressionTest {
    private static final Wallet OWNER = new Wallet("propietario");
    private static final Wallet RECIPIENT = new Wallet("destinatario");
    private static final UTXO SOURCE = new UTXO(new byte[]{1, 2, 3}, 0);

    private P0RegressionTest() {}

    public static void main(String[] args) {
        switch (args.length == 0 ? "fixed" : args[0]) {
            case "P01" -> forgedReceipt();
            case "P02" -> differentRecipients();
            case "P03" -> unsignedTransfer();
            case "P04" -> originalOverflow();
            case "fixed" -> {
                forgedReceipt();
                differentRecipients();
                unsignedTransfer();
                originalOverflow();
                P0AdditionalChecks.run();
            }
            default -> throw new IllegalArgumentException("Sonda desconocida.");
        }
        System.out.println("PASS");
    }

    private static void forgedReceipt() {
        ShardManager manager = new ShardManager(2);
        var before = ledger(manager);
        boolean accepted = manager.commitReceipt(new Receipt("fabricado", "sin-sesion",
                0, 1, "sin-origen", 100, RECIPIENT.getPublicKey()));
        check(before.equals(ledger(manager)), "P01: el recibo fabricado modifico el ledger.");
        check(!accepted, "P01: se acepto un recibo sin sesion.");
    }

    private static void differentRecipients() {
        ShardManager manager = new ShardManager(2);
        var before = ledger(manager);
        var first = new CrossShardTransfer(0, 1, SOURCE, 10, OWNER.getPublicKey());
        var second = new CrossShardTransfer(0, 1, SOURCE, 10, RECIPIENT.getPublicKey());
        check(before.equals(ledger(manager)), "P02: calcular el ID modifico el ledger.");
        check(!first.id().equals(second.id()), "P02: dos destinatarios comparten ID.");
    }

    private static void unsignedTransfer() {
        ShardManager.Setup setup = new ShardManager.Setup(2);
        setup.addInitialUtxo(0, SOURCE, new Transaction.Output(100, OWNER.getPublicKey()));
        ShardManager manager = setup.start();
        var before = ledger(manager);
        var transfer = new CrossShardTransfer(0, 1, SOURCE, 10, RECIPIENT.getPublicKey());
        try {
            manager.beginAtomicTransfer(transfer, 3);
            manager.commitAtomicTransfer(transfer.id());
        } catch (IllegalArgumentException rejected) {
            check(before.equals(ledger(manager)), "P03: hubo mutacion al rechazar la firma ausente.");
            return;
        }
        throw new AssertionError("P03: la solicitud sin firma alcanzo el commit. Ledger intacto: "
                + before.equals(ledger(manager)));
    }

    private static void originalOverflow() {
        Transaction tx = outputs(Long.MAX_VALUE, Long.MAX_VALUE);
        TxValidator validator = new TxValidator(new UTXOPool());
        check(validator.handleTxs(new Transaction[]{tx}).length == 0, "P04: se acepto el overflow original.");
        check(validator.getUtxoPool().size() == 0, "P04: el rechazo modifico el ledger.");
    }

    static Transaction outputs(long... values) {
        Transaction tx = new Transaction();
        for (long value : values) {
            tx.addOutput(value, RECIPIENT.getPublicKey());
        }
        return tx;
    }

    static List<ShardState> ledger(ShardManager manager) {
        return manager.getShards().stream().map(shard -> new ShardState(
                contents(shard.getUtxoPool()), Set.copyOf(shard.getLockedUtxos()),
                Set.copyOf(shard.getConsumedReceipts()))).toList();
    }

    static Map<UTXO, Transaction.Output> contents(UTXOPool pool) {
        Map<UTXO, Transaction.Output> entries = new LinkedHashMap<>();
        pool.getAllUTXO().forEach(utxo -> entries.put(utxo, pool.getOutput(utxo)));
        return entries;
    }

    record ShardState(Map<UTXO, Transaction.Output> outputs, Set<String> locks, Set<String> receipts) {}

    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
