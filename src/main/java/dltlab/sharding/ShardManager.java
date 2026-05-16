package dltlab.sharding;

import dltlab.crypto.Hashing;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Coordina shards, validadores, recibos y transferencias cross-shard. */
public class ShardManager {
    private final List<Shard> shards = new ArrayList<>();
    private final Map<String, CrossShardSession> sessions = new LinkedHashMap<>();
    private final List<ShardRoundMetric> roundMetrics = new ArrayList<>();
    private final int quorum;
    private int currentRound = 0;

    public ShardManager(int shardCount) {
        this(shardCount, 4, 3);
    }

    public ShardManager(int shardCount, int validatorsPerShard, int quorum) {
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
        byte[] hash = Hashing.sha256(key.getEncoded());
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

    /** API simple de fases anteriores: bloquea el UTXO origen y crea un recibo. */
    public Receipt lockAndCreateReceipt(CrossShardTransfer transfer) {
        Shard source = getShard(transfer.sourceShardId());
        validateSourceTransfer(transfer, source);
        if (source.isLocked(transfer.sourceUtxo().key())) {
            throw new IllegalStateException("El UTXO origen ya esta bloqueado.");
        }
        source.lockUtxo(transfer.sourceUtxo().key());
        return createReceipt(transfer);
    }

    /** API simple de fases anteriores: consume un recibo en el destino con proteccion contra replay. */
    public boolean commitReceipt(Receipt receipt) {
        Shard target = getShard(receipt.targetShardId());
        if (!target.markReceiptConsumed(receipt.receiptId())) {
            return false; // Proteccion contra replay del mismo recibo.
        }
        byte[] syntheticTxHash = Hashing.sha256(("cross-shard:" + receipt.receiptId()).getBytes());
        target.getUtxoPool().addUTXO(new UTXO(syntheticTxHash, 0), new Transaction.Output(receipt.amount(), receipt.recipient()));
        snapshotMetrics();
        return true;
    }

    /**
     * Inicia una transferencia cross-shard atomica: valida el shard origen, bloquea el UTXO
     * y deja una sesion pendiente hasta que el shard destino haga commit o venza el timeout.
     */
    public CrossShardSession beginAtomicTransfer(CrossShardTransfer transfer, int timeoutRounds) {
        if (timeoutRounds <= 0) {
            throw new IllegalArgumentException("El timeout debe ser positivo.");
        }
        if (sessions.containsKey(transfer.id())) {
            throw new IllegalStateException("Ya existe una sesion para esta transferencia.");
        }
        Shard source = getShard(transfer.sourceShardId());
        validateSourceTransfer(transfer, source);
        int approvals = source.approvingValidators();
        if (approvals < quorum) {
            CrossShardSession failed = new CrossShardSession(transfer, createReceipt(transfer), currentRound,
                    currentRound + timeoutRounds, approvals, source.getValidators().size());
            failed.markFailedValidation("El shard origen no alcanzo quorum para bloquear el UTXO.");
            sessions.put(transfer.id(), failed);
            snapshotMetrics();
            return failed;
        }
        if (!source.lockUtxo(transfer.sourceUtxo().key())) {
            throw new IllegalStateException("El UTXO origen ya esta bloqueado por otra transferencia.");
        }
        Receipt receipt = createReceipt(transfer);
        CrossShardSession session = new CrossShardSession(transfer, receipt, currentRound,
                currentRound + timeoutRounds, approvals, source.getValidators().size());
        sessions.put(transfer.id(), session);
        snapshotMetrics();
        return session;
    }

    /** Intenta confirmar atomicamente una transferencia pendiente en el shard destino. */
    public boolean commitAtomicTransfer(String transferId) {
        CrossShardSession session = sessions.get(transferId);
        if (session == null || session.isTerminal()) {
            return false;
        }
        if (currentRound > session.timeoutRound()) {
            timeoutSession(session);
            snapshotMetrics();
            return false;
        }
        Shard target = getShard(session.transfer().targetShardId());
        int approvals = target.approvingValidators();
        if (approvals < quorum) {
            session.markFailedValidation("El shard destino no alcanzo quorum para consumir el recibo.");
            getShard(session.transfer().sourceShardId()).unlockUtxo(session.transfer().sourceUtxo().key());
            snapshotMetrics();
            return false;
        }
        if (!target.markReceiptConsumed(session.receipt().receiptId())) {
            session.markFailedValidation("El recibo ya habia sido consumido en el shard destino.");
            getShard(session.transfer().sourceShardId()).unlockUtxo(session.transfer().sourceUtxo().key());
            snapshotMetrics();
            return false;
        }
        finalizeSourceDebit(session);
        byte[] syntheticTxHash = Hashing.sha256(("atomic-cross-shard:" + session.receipt().receiptId()).getBytes());
        target.getUtxoPool().addUTXO(new UTXO(syntheticTxHash, 0),
                new Transaction.Output(session.receipt().amount(), session.receipt().recipient()));
        session.markCommitted(approvals, target.getValidators().size());
        snapshotMetrics();
        return true;
    }

    /** Aborta manualmente una sesion pendiente y libera el UTXO origen. */
    public boolean abortAtomicTransfer(String transferId, String reason) {
        CrossShardSession session = sessions.get(transferId);
        if (session == null || session.isTerminal()) {
            return false;
        }
        getShard(session.transfer().sourceShardId()).unlockUtxo(session.transfer().sourceUtxo().key());
        session.markAborted(reason == null ? "Transferencia abortada manualmente." : reason);
        snapshotMetrics();
        return true;
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
        for (CrossShardSession session : sessions.values()) {
            if (!session.isTerminal() && currentRound > session.timeoutRound()) {
                timeoutSession(session);
            }
        }
    }

    private void timeoutSession(CrossShardSession session) {
        getShard(session.transfer().sourceShardId()).unlockUtxo(session.transfer().sourceUtxo().key());
        session.markTimedOut("La transferencia vencio antes de ser confirmada por el shard destino.");
    }

    private void validateSourceTransfer(CrossShardTransfer transfer, Shard source) {
        if (transfer.sourceShardId() == transfer.targetShardId()) {
            throw new IllegalArgumentException("Una transferencia cross-shard debe mover valor entre shards distintos.");
        }
        if (!source.getUtxoPool().contains(transfer.sourceUtxo())) {
            throw new IllegalArgumentException("El UTXO origen no existe en el shard indicado.");
        }
        Transaction.Output output = source.getUtxoPool().getOutput(transfer.sourceUtxo());
        if (transfer.amount() <= 0) {
            throw new IllegalArgumentException("El monto cross-shard debe ser positivo.");
        }
        if (transfer.amount() > output.getValue()) {
            throw new IllegalArgumentException("El monto cross-shard excede el valor disponible.");
        }
    }

    private Receipt createReceipt(CrossShardTransfer transfer) {
        String receiptId = Hashing.hex(Hashing.sha256(("receipt:" + transfer.id()).getBytes()));
        return new Receipt(receiptId, transfer.id(), transfer.sourceShardId(), transfer.targetShardId(),
                transfer.sourceUtxo().key(), transfer.amount(), transfer.recipient());
    }

    private void finalizeSourceDebit(CrossShardSession session) {
        CrossShardTransfer transfer = session.transfer();
        Shard source = getShard(transfer.sourceShardId());
        Transaction.Output original = source.getUtxoPool().getOutput(transfer.sourceUtxo());
        source.getUtxoPool().removeUTXO(transfer.sourceUtxo());
        source.unlockUtxo(transfer.sourceUtxo().key());
        long change = original.getValue() - transfer.amount();
        if (change > 0) {
            byte[] changeHash = Hashing.sha256(("cross-shard-change:" + session.receipt().receiptId()).getBytes());
            source.getUtxoPool().addUTXO(new UTXO(changeHash, 0), new Transaction.Output(change, original.getRecipient()));
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
                case PENDING -> pending++;
                case COMMITTED -> {
                    committed++;
                    moved += session.transfer().amount();
                }
                case ABORTED -> aborted++;
                case TIMED_OUT -> timedOut++;
                case FAILED_VALIDATION -> failed++;
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
