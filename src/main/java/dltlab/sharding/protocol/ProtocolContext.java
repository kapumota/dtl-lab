package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardSession;
import dltlab.sharding.Shard;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntSupplier;

/**
 * Dependencias de confianza que el propietario del ledger entrega al protocolo.
 * El gestor conserva en privado su contexto y sus colecciones vivas.
 */
public final class ProtocolContext {
    private final List<Shard> shards;
    private final Map<String, CrossShardSession> sessions;
    private final int quorum;
    private final IntSupplier logicalTimeSupplier;
    private final FailureInjector failureInjector;
    private final List<ReceiptObservation> observations = new ArrayList<>();
    private boolean insideFailureHook;

    public ProtocolContext(List<Shard> shards, Map<String, CrossShardSession> sessions, int quorum,
                           IntSupplier logicalTimeSupplier, FailureInjector failureInjector) {
        this.shards = Objects.requireNonNull(shards, "La lista de shards es obligatoria.");
        this.sessions = Objects.requireNonNull(sessions, "El registro de sesiones es obligatorio.");
        if (quorum <= 0) {
            throw new IllegalArgumentException("El quorum debe ser positivo.");
        }
        this.quorum = quorum;
        this.logicalTimeSupplier = Objects.requireNonNull(logicalTimeSupplier,
                "El proveedor de tiempo logico es obligatorio.");
        this.failureInjector = failureInjector == null ? FailureInjector.none() : failureInjector;
    }

    Shard shard(int shardId) {
        return shards.get(shardId);
    }

    CrossShardSession session(String transferId) {
        return sessions.get(transferId);
    }

    boolean hasSession(String transferId) {
        return sessions.containsKey(transferId);
    }

    void putSession(CrossShardSession session) {
        sessions.put(session.transfer().id(), session);
    }

    int quorum() {
        return quorum;
    }

    int logicalTime() {
        return logicalTimeSupplier.getAsInt();
    }

    void observe(String receiptId, ReceiptObservation.Kind kind) {
        observations.add(new ReceiptObservation(receiptId, kind));
    }

    List<ReceiptObservation> observations() {
        return List.copyOf(observations);
    }

    void debit(String receiptId, Shard shard, UTXO utxo) {
        var pool = shard.getUtxoPool();
        var before = pool.getOutput(utxo);
        pool.removeUTXO(utxo);
        if (before != null && !pool.contains(utxo)) {
            observeEffect(receiptId, ReceiptObservation.Kind.DEBIT, shard, utxo, before);
        }
    }

    void credit(String receiptId, Shard shard, UTXO utxo, Transaction.Output output) {
        var pool = shard.getUtxoPool();
        var before = pool.getOutput(utxo);
        pool.addUTXO(utxo, output);
        if (before != output && pool.getOutput(utxo) == output) {
            observeEffect(receiptId, ReceiptObservation.Kind.CREDIT, shard, utxo, output);
        }
    }

    private void observeEffect(String receiptId, ReceiptObservation.Kind kind, Shard shard,
                               UTXO utxo, Transaction.Output output) {
        observations.add(new ReceiptObservation(receiptId, kind,
                new ReceiptObservation.LedgerEffect(shard.getId(), utxo.key(), output.getValue())));
    }

    void requireOutsideHook() {
        if (insideFailureHook) {
            throw new IllegalStateException("El hook de fallos no puede reentrar en el gestor ni en el protocolo.");
        }
    }

    void checkFailure(FailurePoint point) {
        insideFailureHook = true;
        try {
            failureInjector.check(point);
        } finally {
            insideFailureHook = false;
        }
    }

    /** Puntos controlados para probar rollback sin alterar el ledger de produccion. */
    public enum FailurePoint {
        AFTER_RECEIPT_CONSUMED,
        AFTER_SOURCE_DEBIT,
        DURING_TARGET_CREDIT,
        AFTER_TARGET_CREDIT
    }

    @FunctionalInterface
    public interface FailureInjector {
        void check(FailurePoint point);

        static FailureInjector none() {
            return point -> { };
        }
    }
}
