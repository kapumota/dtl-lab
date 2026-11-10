package dltlab.sharding.protocol;

import dltlab.sharding.Shard;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;

import java.util.HashMap;
import java.util.List;

import static dltlab.sharding.protocol.ReceiptObservation.Kind.*;

/** Ejecuta escrituras adversariales reales en shards aislados, sin exponer el gestor. */
public final class ReceiptFaultFixture {
    private ReceiptFaultFixture() {}

    public static List<ReceiptObservation> confirmedDuplicate(boolean sameAttempt, boolean duplicateDebit) {
        Shard source = new Shard(0);
        Shard target = new Shard(1);
        ProtocolContext context = new ProtocolContext(List.of(source, target), new HashMap<>(), 1,
                () -> 0, ProtocolContext.FailureInjector.none());
        String id = "recibo-instrumentado";
        context.observe(id, ISSUED);
        context.observe(id, DELIVERED);
        context.observe(id, ATTEMPT);
        UTXO initial = new UTXO(new byte[]{1}, 0);
        UTXO other = new UTXO(new byte[]{2}, 0);
        var output = new Transaction.Output(10, null);
        source.getUtxoPool().addUTXO(initial, output);
        source.getUtxoPool().addUTXO(other, output);
        context.debit(id, source, initial);
        context.credit(id, target, initial, output);
        target.markReceiptConsumed(id);
        if (!sameAttempt) {
            context.observe(id, CONFIRMED);
            context.observe(id, ATTEMPT);
            context.debit(id, source, other);
            context.credit(id, target, other, output);
        } else if (duplicateDebit) {
            context.debit(id, source, other);
        } else {
            context.credit(id, target, other, output);
        }
        context.observe(id, CONFIRMED);
        if (target.getConsumedReceipts().size() != 1) {
            throw new AssertionError("El Set anterior debe seguir mostrando un unico recibo.");
        }
        return context.observations();
    }
}
