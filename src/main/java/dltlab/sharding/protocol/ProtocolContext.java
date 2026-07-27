package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardSession;
import dltlab.sharding.Shard;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntSupplier;

/** Dependencias controladas que necesita el protocolo para operar sobre el ledger. */
public final class ProtocolContext {
    private final List<Shard> shards;
    private final Map<String, CrossShardSession> sessions;
    private final int quorum;
    private final IntSupplier logicalTimeSupplier;
    private final FailureInjector failureInjector;

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

    public Shard shard(int shardId) {
        return shards.get(shardId);
    }

    public CrossShardSession session(String transferId) {
        return sessions.get(transferId);
    }

    public boolean hasSession(String transferId) {
        return sessions.containsKey(transferId);
    }

    public void putSession(CrossShardSession session) {
        sessions.put(session.transfer().id(), session);
    }

    public int quorum() {
        return quorum;
    }

    public int logicalTime() {
        return logicalTimeSupplier.getAsInt();
    }

    public void checkFailure(FailurePoint point) {
        failureInjector.check(point);
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
