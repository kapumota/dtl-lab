package dltlab.sharding;

import dltlab.sharding.protocol.AtomicCommitProtocol;
import dltlab.sharding.protocol.CrossShardProtocol;
import dltlab.sharding.protocol.ProtocolContext;
import dltlab.sharding.protocol.ProtocolResult;
import dltlab.sharding.protocol.ReceiptObservation;

import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;

import java.security.PublicKey;
import java.util.ArrayList;
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
    private volatile int currentRound = 0;

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
        synchronized (protocol) {
            protocol.requireOutsideHook();
            return shards.stream().map(Shard::snapshot).toList();
        }
    }

    public Shard getShard(int id) {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            return shards.get(id).snapshot();
        }
    }

    /** Capacidad de inicializacion que se consume antes de entregar el gestor. */
    public static final class Setup {
        private ShardManager manager;

        public Setup(int shardCount) {
            this(shardCount, 4, 3, ProtocolContext.FailureInjector.none());
        }

        public Setup(int shardCount, int validatorsPerShard, int quorum) {
            this(shardCount, validatorsPerShard, quorum, ProtocolContext.FailureInjector.none());
        }

        public Setup(int shardCount, int validatorsPerShard, int quorum,
                     ProtocolContext.FailureInjector failureInjector) {
            manager = new ShardManager(shardCount, validatorsPerShard, quorum, failureInjector);
        }

        public synchronized void addInitialUtxo(int shardId, UTXO utxo, Transaction.Output output) {
            var pool = requireOpen().shards.get(shardId).getUtxoPool();
            if (utxo == null || pool.contains(utxo) || output == null
                    || output.getValue() <= 0 || output.getRecipient() == null) {
                throw new IllegalArgumentException("La salida inicial es invalida o duplicada.");
            }
            pool.addUTXO(utxo, new Transaction.Output(output.getValue(),
                    CrossShardTransfer.canonicalKey(output.getRecipient())));
        }

        public synchronized ShardManager start() {
            ShardManager initialized = requireOpen();
            manager = null;
            return initialized;
        }

        private ShardManager requireOpen() {
            if (manager == null) {
                throw new IllegalStateException("La carga inicial ya termino.");
            }
            return manager;
        }
    }

    public int getQuorum() {
        return quorum;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    /** Sincroniza el reloj logico sin ejecutar expiraciones automaticas. */
    public void advanceClockTo(int round) {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            if (round < currentRound) {
                throw new IllegalArgumentException("La ronda logica no puede retroceder.");
            }
            currentRound = round;
        }
    }

    public List<ReceiptObservation> receiptObservations() {
        return protocol.receiptObservations();
    }

    public CrossShardProtocol getProtocol() {
        return protocol;
    }

    public List<CrossShardSession> getSessions() {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            return sessions.values().stream().map(CrossShardSession::snapshot).toList();
        }
    }

    public List<ShardRoundMetric> getRoundMetrics() {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            return List.copyOf(roundMetrics);
        }
    }

    public CrossShardSession getSession(String transferId) {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            CrossShardSession session = sessions.get(transferId);
            return session == null ? null : session.snapshot();
        }
    }

    public int assignShard(PublicKey key) {
        byte[] hash = dltlab.crypto.Hashing.sha256(key.getEncoded());
        return Byte.toUnsignedInt(hash[0]) % shards.size();
    }

    /** Cambia disponibilidad de todos los validadores de un shard para simular fallos. */
    public void setShardOnline(int shardId, boolean online) {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            for (ShardValidator validator : shards.get(shardId).getValidators()) {
                validator.setOnline(online);
            }
        }
    }

    /** Cambia disponibilidad de un validador puntual para simular fallos parciales. */
    public void setValidatorOnline(int shardId, int validatorIndex, boolean online) {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            shards.get(shardId).getValidators().get(validatorIndex).setOnline(online);
        }
    }

    /** Marca un validador como honesto o malicioso. */
    public void setValidatorHonest(int shardId, int validatorIndex, boolean honest) {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            shards.get(shardId).getValidators().get(validatorIndex).setHonest(honest);
        }
    }

    /** API simple de fases anteriores delegada al protocolo. */
    public Receipt lockAndCreateReceipt(CrossShardTransfer transfer) {
        return protocol.lockAndCreateReceipt(transfer);
    }

    /** API simple de fases anteriores delegada al protocolo. */
    public boolean commitReceipt(Receipt receipt) {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            boolean committed = protocol.commitReceipt(receipt);
            snapshotMetrics();
            return committed;
        }
    }

    /** Inicia una transferencia atomica y conserva la firma publica anterior. */
    public CrossShardSession beginAtomicTransfer(CrossShardTransfer transfer, int timeoutRounds) {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            protocol.begin(transfer, timeoutRounds);
            snapshotMetrics();
            return getSession(transfer.id());
        }
    }

    /** Entrega explicitamente el recibo al shard destino. */
    public boolean deliverAtomicReceipt(String transferId) {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            ProtocolResult result = protocol.deliverReceipt(transferId);
            snapshotMetrics();
            return result.success();
        }
    }

    /** Intenta confirmar atomicamente una transferencia mediante el protocolo extraido. */
    public boolean commitAtomicTransfer(String transferId) {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            ProtocolResult result = protocol.commit(transferId);
            snapshotMetrics();
            return result.success();
        }
    }

    /** Aborta una sesion mediante el protocolo extraido. */
    public boolean abortAtomicTransfer(String transferId, String reason) {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            ProtocolResult result = protocol.abort(transferId, reason);
            snapshotMetrics();
            return result.success();
        }
    }

    /** Ejecuta un timeout explicito para simulaciones con orden controlado. */
    public boolean timeoutAtomicTransfer(String transferId) {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            ProtocolResult result = protocol.timeout(transferId);
            snapshotMetrics();
            return result.success();
        }
    }

    /** Avanza la ronda logica, expira sesiones vencidas y captura metricas. */
    public void advanceRound() {
        synchronized (protocol) {
            protocol.requireOutsideHook();
            currentRound = Math.incrementExact(currentRound);
            expireTimedOutSessions();
            snapshotMetrics();
        }
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
