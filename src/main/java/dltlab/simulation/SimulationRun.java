package dltlab.simulation;

import dltlab.crypto.Hashing;
import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.ShardManager;
import dltlab.sharding.protocol.ProtocolAction;
import dltlab.sharding.protocol.ProtocolEvent;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Ejecucion determinista que coordina reloj, eventos, red y protocolo real. */
public final class SimulationRun {
    private final long seed;
    private final ShardManager manager;
    private final SimulationClock clock;
    private final EventScheduler scheduler;
    private final DeterministicRandom random;
    private final NetworkFaultModel faultModel;
    private final List<SimulationTraceEntry> trace = new ArrayList<>();
    private final Map<String, Integer> observedProtocolEvents = new LinkedHashMap<>();
    private long nextTraceSequence;
    private long nextMessageSequence;

    public SimulationRun(int shardCount, int validatorsPerShard, int quorum,
                         long seed, NetworkFaultModel faultModel) {
        this.seed = seed;
        this.manager = new ShardManager(shardCount, validatorsPerShard, quorum);
        this.clock = new SimulationClock();
        this.scheduler = new EventScheduler(clock, manager::advanceClockTo);
        this.random = new DeterministicRandom(seed);
        this.faultModel = Objects.requireNonNull(faultModel,
                "El modelo de fallos de red es obligatorio.");
    }

    public long seed() {
        return seed;
    }

    public ShardManager manager() {
        return manager;
    }

    public SimulationClock clock() {
        return clock;
    }

    public EventScheduler scheduler() {
        return scheduler;
    }

    public DeterministicRandom random() {
        return random;
    }

    public void addFunds(int shardId, UTXO utxo, long value) {
        if (value <= 0) {
            throw new IllegalArgumentException("El valor inicial debe ser positivo.");
        }
        manager.getShard(shardId).getUtxoPool().addUTXO(utxo,
                new Transaction.Output(value, null));
    }

    public void scheduleBegin(CrossShardTransfer transfer, int timeoutRounds,
                              int round, int priority) {
        scheduler.schedule(round, priority, SimulationEventType.BEGIN_TRANSFER,
                transfer.id(), "Inicio de transferencia cross-shard.", () -> {
                    manager.beginAtomicTransfer(transfer, timeoutRounds);
                    captureProtocolEvents(transfer.id());
                });
    }

    public void scheduleSendReceipt(String transferId, int round, int priority) {
        scheduler.schedule(round, priority, SimulationEventType.SEND_RECEIPT,
                transferId, "Envio del recibo a la red simulada.", () -> sendReceipt(transferId, priority));
    }

    public void scheduleCommit(String transferId, int round, int priority) {
        scheduler.schedule(round, priority, SimulationEventType.COMMIT_DESTINATION,
                transferId, "Intento de commit en el shard destino.", () -> {
                    CrossShardSession session = requireSession(transferId);
                    boolean sourceLockedBefore = sourceLocked(session);
                    boolean success = manager.commitAtomicTransfer(transferId);
                    captureProtocolEvents(transferId);
                    record(SimulationEventType.COMMIT_DESTINATION, transferId,
                            success ? "ACEPTADO" : "RECHAZADO", session.reason());
                    recordReleaseIfNeeded(session, sourceLockedBefore);
                });
    }

    public void scheduleExpire(String transferId, int round, int priority) {
        scheduler.schedule(round, priority, SimulationEventType.EXPIRE_TRANSFER,
                transferId, "Intento explicito de timeout.", () -> {
                    CrossShardSession session = requireSession(transferId);
                    boolean sourceLockedBefore = sourceLocked(session);
                    boolean success = manager.timeoutAtomicTransfer(transferId);
                    captureProtocolEvents(transferId);
                    record(SimulationEventType.EXPIRE_TRANSFER, transferId,
                            success ? "ACEPTADO" : "RECHAZADO", session.reason());
                    recordReleaseIfNeeded(session, sourceLockedBefore);
                });
    }

    public void scheduleAbort(String transferId, String reason, int round, int priority) {
        scheduler.schedule(round, priority, SimulationEventType.ABORT_TRANSFER,
                transferId, "Abort solicitado por el escenario.", () -> {
                    CrossShardSession session = requireSession(transferId);
                    boolean sourceLockedBefore = sourceLocked(session);
                    boolean success = manager.abortAtomicTransfer(transferId, reason);
                    captureProtocolEvents(transferId);
                    record(SimulationEventType.ABORT_TRANSFER, transferId,
                            success ? "ACEPTADO" : "RECHAZADO", session.reason());
                    recordReleaseIfNeeded(session, sourceLockedBefore);
                });
    }

    public void scheduleShardAvailability(int shardId, boolean online, int round, int priority) {
        SimulationEventType type = online
                ? SimulationEventType.SHARD_ONLINE : SimulationEventType.SHARD_OFFLINE;
        scheduler.schedule(round, priority, type, "shard-" + shardId,
                online ? "El shard vuelve a estar disponible." : "El shard deja de estar disponible.",
                () -> manager.setShardOnline(shardId, online));
    }

    public ScheduledEvent schedule(int round, int priority, SimulationEventType type,
                                   String transferId, String description, Runnable action) {
        return scheduler.schedule(round, priority, type, transferId, description, action);
    }

    public void runAll() {
        while (scheduler.hasNext()) {
            ScheduledEvent event = scheduler.next();
            record(event.type(), event.transferId(), "EJECUTADO", event.description());
            try {
                event.action().run();
            } catch (RuntimeException error) {
                record(event.type(), event.transferId(), "ERROR",
                        error.getClass().getSimpleName() + ": " + safeMessage(error));
            }
        }
    }

    public List<SimulationTraceEntry> trace() {
        return List.copyOf(trace);
    }

    public String traceText() {
        StringBuilder builder = new StringBuilder();
        for (SimulationTraceEntry entry : trace) {
            builder.append(entry.render()).append('\n');
        }
        return builder.toString();
    }

    public String traceHash() {
        return Hashing.hex(Hashing.sha256(traceText().getBytes(StandardCharsets.UTF_8)));
    }

    public String finalStateText() {
        return manager.getSessions().stream()
                .sorted(Comparator.comparing(session -> session.transfer().id()))
                .map(session -> session.transfer().id() + "=" + session.status())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("sin-sesiones");
    }

    public boolean traceContains(SimulationEventType type) {
        return trace.stream().anyMatch(entry -> entry.type() == type);
    }

    private void sendReceipt(String transferId, int basePriority) {
        CrossShardSession session = requireSession(transferId);
        NetworkMessage message = new NetworkMessage(
                "msg-" + nextMessageSequence++, transferId,
                session.transfer().sourceShardId(), session.transfer().targetShardId(),
                clock.now(), session.timeoutRound(), 0);
        DeliveryDecision decision = faultModel.decide(message, clock, random);
        if (decision.dropped()) {
            record(SimulationEventType.DROP_MESSAGE, transferId, "APLICADO", decision.reason());
            return;
        }
        if (decision.delayRounds() > 0) {
            record(SimulationEventType.DELAY_MESSAGE, transferId, "APLICADO",
                    decision.reason() + " Rondas: " + decision.delayRounds() + ".");
        }
        if (decision.copies() > 1) {
            record(SimulationEventType.DUPLICATE_MESSAGE, transferId, "APLICADO",
                    decision.reason() + " Copias: " + decision.copies() + ".");
        }
        int deliveryRound = Math.addExact(clock.now(), decision.delayRounds());
        for (int copy = 0; copy < decision.copies(); copy++) {
            NetworkMessage delivery = copy == 0 ? message : message.copy(copy);
            int deliveryPriority = basePriority + decision.priorityOffset() + copy;
            scheduler.schedule(deliveryRound, deliveryPriority,
                    SimulationEventType.DELIVER_RECEIPT, transferId,
                    "Entrega del mensaje " + delivery.messageId() + ".",
                    () -> deliverReceipt(delivery));
        }
    }

    private void deliverReceipt(NetworkMessage message) {
        boolean success = manager.deliverAtomicReceipt(message.transferId());
        CrossShardSession session = manager.getSession(message.transferId());
        if (session != null) {
            captureProtocolEvents(message.transferId());
        }
        record(SimulationEventType.DELIVER_RECEIPT, message.transferId(),
                success ? "ACEPTADO" : "RECHAZADO",
                session == null ? "La sesion no existe." : session.reason());
    }

    private void captureProtocolEvents(String transferId) {
        CrossShardSession session = manager.getSession(transferId);
        if (session == null) {
            return;
        }
        int observed = observedProtocolEvents.getOrDefault(transferId, 0);
        List<ProtocolEvent> events = session.events();
        for (int index = observed; index < events.size(); index++) {
            ProtocolEvent event = events.get(index);
            if (event.action() == ProtocolAction.CREATE_SESSION) {
                continue;
            }
            record(map(event.action()), transferId, "APLICADO",
                    event.previousStatus() + " -> " + event.nextStatus() + ". " + event.reason());
        }
        observedProtocolEvents.put(transferId, events.size());
    }

    private SimulationEventType map(ProtocolAction action) {
        return switch (action) {
            case CREATE_SESSION -> SimulationEventType.BEGIN_TRANSFER;
            case LOCK_SOURCE -> SimulationEventType.LOCK_SOURCE;
            case CREATE_RECEIPT -> SimulationEventType.CREATE_RECEIPT;
            case DELIVER_RECEIPT -> SimulationEventType.DELIVER_RECEIPT;
            case PREPARE_DESTINATION -> SimulationEventType.PREPARE_DESTINATION;
            case COMMIT_DESTINATION -> SimulationEventType.COMMIT_DESTINATION;
            case ABORT_TRANSFER -> SimulationEventType.ABORT_TRANSFER;
            case EXPIRE_TRANSFER -> SimulationEventType.EXPIRE_TRANSFER;
            case FAIL_VALIDATION -> SimulationEventType.FAIL_VALIDATION;
        };
    }

    private CrossShardSession requireSession(String transferId) {
        CrossShardSession session = manager.getSession(transferId);
        if (session == null) {
            throw new IllegalStateException("La sesion " + transferId + " no existe.");
        }
        return session;
    }

    private boolean sourceLocked(CrossShardSession session) {
        return manager.getShard(session.transfer().sourceShardId())
                .isLocked(session.transfer().sourceUtxo().key());
    }

    private void recordReleaseIfNeeded(CrossShardSession session, boolean lockedBefore) {
        if (lockedBefore && !sourceLocked(session)) {
            record(SimulationEventType.RELEASE_SOURCE, session.transfer().id(),
                    "APLICADO", "El bloqueo del UTXO origen fue liberado.");
        }
    }

    private void record(SimulationEventType type, String transferId,
                        String outcome, String detail) {
        trace.add(new SimulationTraceEntry(nextTraceSequence++, clock.now(), type,
                transferId, outcome, detail));
    }

    private static String safeMessage(RuntimeException error) {
        return error.getMessage() == null ? "Sin mensaje de error." : error.getMessage();
    }
}
