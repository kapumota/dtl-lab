package dltlab.sharding;

import dltlab.sharding.protocol.AtomicCommitProtocol;
import dltlab.sharding.protocol.CrossShardProtocol;
import dltlab.sharding.protocol.ProtocolContext;
import dltlab.sharding.protocol.ProtocolResult;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Administra shards, validadores, reloj logico, sesiones y metricas agregadas. */
public class ShardManager {
    private final List<Shard> shards = new ArrayList<>();
    private final Map<String, CrossShardSession> sessions = new LinkedHashMap<>();
    private final List<ShardRoundMetric> roundMetrics = new ArrayList<>();
    private final int quorum;
    private final AtomicCommitProtocol protocol;
    private int currentRound = 0;

    public ShardManager(int shardCount) {
        this(shardCount, 4, 3);
    }

    public ShardManager(int shardCount, int validatorsPerShard, int quorum) {
        this(shardCount, validatorsPerShard, quorum, ProtocolContext.FailureInjector.none());
    }

    public ShardManager(int shardCount, int validatorsPerShard, int quorum,
                        ProtocolContext.FailureInjector failureInjector) {
        if (shardCount <= 0) {
            throw new IllegalArgumentException("Debe existir al menos un shard.");
        }
        if (validatorsPerShard <= 0) {
            throw new IllegalArgumentException("Cada shard debe tener al menos un validador.");
        }
        if (quorum <= 0 || quorum > validatorsPerShard) {
            throw new IllegalArgumentException("El quorum debe estar entre 1 y la cantidad de validadores por shard.");
        }
        this.quorum = quorum;
        for (int i = 0; i < shardCount; i++) {
            Shard shard = new Shard(i);
            for (int v = 0; v < validatorsPerShard; v++) {
                shard.addValidator(new ShardValidator("s" + i + "-v" + v, i, 1L, true, true));
            }
            shards.add(shard);
        }
        ProtocolContext context = new ProtocolContext(shards, sessions, quorum,
                () -> currentRound, failureInjector);
        this.protocol = new AtomicCommitProtocol(context);
        snapshotMetrics();
    }

    public List<Shard> getShards() {
        return Collections.unmodifiableList(shards);
    }

    public Shard getShard(int id) {
        return shards.get(id);
    }

    public int getQuorum() {
        return quorum;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    /** Sincroniza el reloj logico sin ejecutar expiraciones automaticas. */
    public void advanceClockTo(int round) {
        if (round < currentRound) {
            throw new IllegalArgumentException("La ronda logica no puede retroceder.");
        }
        currentRound = round;
    }

    public CrossShardProtocol getProtocol() {
        return protocol;
    }

    public List<CrossShardSession> getSessions() {
        return Collections.unmodifiableList(new ArrayList<>(sessions.values()));
    }

    public List<ShardRoundMetric> getRoundMetrics() {
        return Collections.unmodifiableList(roundMetrics);
    }

    public CrossShardSession getSession(String transferId) {
        return sessions.get(transferId);
    }

    public int assignShard(PublicKey key) {
        byte[] hash = dltlab.crypto.Hashing.sha256(key.getEncoded());
        return Byte.toUnsignedInt(hash[0]) % shards.size();
    }

    /** Cambia disponibilidad de todos los validadores de un shard para simular fallos. */
    public void setShardOnline(int shardId, boolean online) {
        for (ShardValidator validator : getShard(shardId).getValidators()) {
            validator.setOnline(online);
        }
    }

    /** Cambia disponibilidad de un validador puntual para simular fallos parciales. */
    public void setValidatorOnline(int shardId, int validatorIndex, boolean online) {
        getShard(shardId).getValidators().get(validatorIndex).setOnline(online);
    }

    /** Marca un validador como honesto o malicioso. */
    public void setValidatorHonest(int shardId, int validatorIndex, boolean honest) {
        getShard(shardId).getValidators().get(validatorIndex).setHonest(honest);
    }

    /** API simple de fases anteriores delegada al protocolo. */
    public Receipt lockAndCreateReceipt(CrossShardTransfer transfer) {
        return protocol.lockAndCreateReceipt(transfer);
    }

    /** API simple de fases anteriores delegada al protocolo. */
    public boolean commitReceipt(Receipt receipt) {
        boolean committed = protocol.commitReceipt(receipt);
        snapshotMetrics();
        return committed;
    }

    /** Inicia una transferencia atomica y conserva la firma publica anterior. */
    public CrossShardSession beginAtomicTransfer(CrossShardTransfer transfer, int timeoutRounds) {
        protocol.begin(transfer, timeoutRounds);
        snapshotMetrics();
        return sessions.get(transfer.id());
    }

    /** Entrega explicitamente el recibo al shard destino. */
    public boolean deliverAtomicReceipt(String transferId) {
        ProtocolResult result = protocol.deliverReceipt(transferId);
        snapshotMetrics();
        return result.success();
    }

    /** Intenta confirmar atomicamente una transferencia mediante el protocolo extraido. */
    public boolean commitAtomicTransfer(String transferId) {
        ProtocolResult result = protocol.commit(transferId);
        snapshotMetrics();
        return result.success();
    }

    /** Aborta una sesion mediante el protocolo extraido. */
    public boolean abortAtomicTransfer(String transferId, String reason) {
        ProtocolResult result = protocol.abort(transferId, reason);
        snapshotMetrics();
        return result.success();
    }

    /** Ejecuta un timeout explicito para simulaciones con orden controlado. */
    public boolean timeoutAtomicTransfer(String transferId) {
        ProtocolResult result = protocol.timeout(transferId);
        snapshotMetrics();
        return result.success();
    }

    /** Avanza la ronda logica, expira sesiones vencidas y captura metricas. */
    public void advanceRound() {
        currentRound++;
        expireTimedOutSessions();
        snapshotMetrics();
    }

    public void advanceRounds(int rounds) {
        for (int i = 0; i < rounds; i++) {
            advanceRound();
        }
    }

    private void expireTimedOutSessions() {
        for (CrossShardSession session : new ArrayList<>(sessions.values())) {
            if (!session.isTerminal() && currentRound > session.timeoutRound()) {
                protocol.timeout(session.transfer().id());
            }
        }
    }

    private void snapshotMetrics() {
        int pending = 0;
        int committed = 0;
        int aborted = 0;
        int timedOut = 0;
        int failed = 0;
        int locked = 0;
        long moved = 0L;
        int validators = 0;
        int online = 0;

        for (CrossShardSession session : sessions.values()) {
            switch (session.status()) {
                case COMMITTED -> {
                    committed++;
                    moved += session.transfer().amount();
                }
                case ABORTED -> aborted++;
                case TIMED_OUT -> timedOut++;
                case FAILED_VALIDATION -> failed++;
                default -> pending++;
            }
        }
        for (Shard shard : shards) {
            locked += shard.getLockedUtxos().size();
            validators += shard.getValidators().size();
            online += shard.onlineValidators();
        }
        roundMetrics.add(new ShardRoundMetric(currentRound, pending, committed, aborted, timedOut, failed,
                locked, moved, validators, online));
    }
}
