package dltlab.trace;

import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.protocol.ProtocolAction;
import dltlab.sharding.protocol.ProtocolEvent;
import dltlab.simulation.SimulationEventType;
import dltlab.simulation.SimulationRun;
import dltlab.simulation.SimulationScenario;
import dltlab.simulation.SimulationTraceEntry;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Construye una traza concreta sin duplicar la logica del protocolo. */
public final class TraceRecorder {
    public TraceExecution record(
            SimulationScenario scenario,
            SimulationRun run
    ) {
        Objects.requireNonNull(
                scenario,
                "El escenario de simulacion es obligatorio."
        );
        Objects.requireNonNull(
                run,
                "La ejecucion de simulacion es obligatoria."
        );

        List<CrossShardSession> sessions = run.manager()
                .getSessions()
                .stream()
                .sorted(Comparator.comparing(
                        session -> session.transfer().id()
                ))
                .toList();

        if (sessions.isEmpty()) {
            throw new IllegalArgumentException(
                    "La simulacion debe contener al menos "
                            + "una sesion cross-shard.");
        }

        Map<String, CrossShardSession> sessionsById =
                new LinkedHashMap<>();
        Map<String, ArrayDeque<ProtocolEvent>> pendingEvents =
                new LinkedHashMap<>();

        for (CrossShardSession session : sessions) {
            String transferId = session.transfer().id();
            sessionsById.put(transferId, session);
            pendingEvents.put(
                    transferId,
                    new ArrayDeque<>(session.events())
            );
        }

        List<SimulationTraceEntry> orderedSimulationTrace =
                new ArrayList<>(run.trace());
        orderedSimulationTrace.sort(
                Comparator.comparingLong(
                        SimulationTraceEntry::sequence
                )
        );

        List<TraceEvent> events = new ArrayList<>();
        for (SimulationTraceEntry entry : orderedSimulationTrace) {
            CrossShardSession session = sessionsById.get(
                    entry.transferId()
            );
            ProtocolEvent protocolEvent = matchProtocolEvent(
                    entry,
                    session,
                    pendingEvents
            );

            if (protocolEvent != null
                    && protocolEvent.logicalTime() != entry.round()) {
                throw new IllegalStateException(
                        "La ronda del simulador no coincide con "
                                + "el tiempo logico del protocolo para "
                                + entry.transferId()
                                + ".");
            }

            events.add(toTraceEvent(
                    events.size(),
                    entry,
                    session,
                    protocolEvent
            ));
        }

        for (Map.Entry<String, ArrayDeque<ProtocolEvent>> entry
                : pendingEvents.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                throw new IllegalStateException(
                        "Quedaron eventos del protocolo sin una observacion "
                                + "de simulacion para "
                                + entry.getKey()
                                + ": "
                                + entry.getValue().size()
                                + ".");
            }
        }

        TreeMap<String, CrossShardStatus> finalStates =
                new TreeMap<>();
        for (CrossShardSession session : sessions) {
            finalStates.put(
                    session.transfer().id(),
                    session.status()
            );
        }

        return new TraceExecution(
                1,
                scenario.name(),
                run.seed(),
                run.manager().getShards().size(),
                run.manager().getQuorum(),
                "sha256:" + run.traceHash(),
                events,
                finalStates
        );
    }

    private static ProtocolEvent matchProtocolEvent(
            SimulationTraceEntry entry,
            CrossShardSession session,
            Map<String, ArrayDeque<ProtocolEvent>> pendingEvents
    ) {
        if (session == null) {
            return null;
        }

        ArrayDeque<ProtocolEvent> queue = pendingEvents.get(
                session.transfer().id()
        );
        if (queue == null || queue.isEmpty()) {
            return null;
        }

        ProtocolAction expectedAction = expectedAction(entry);
        if (expectedAction == null) {
            return null;
        }

        ProtocolEvent protocolEvent = queue.peekFirst();
        if (protocolEvent.action() != expectedAction) {
            throw new IllegalStateException(
                    "La accion observada "
                            + expectedAction
                            + " no coincide con el evento "
                            + protocolEvent.action()
                            + " de la sesion "
                            + session.transfer().id()
                            + ".");
        }

        return queue.removeFirst();
    }

    private static ProtocolAction expectedAction(
            SimulationTraceEntry entry
    ) {
        if (entry.type() == SimulationEventType.BEGIN_TRANSFER
                && "EJECUTADO".equals(entry.outcome())) {
            return ProtocolAction.CREATE_SESSION;
        }

        if (!"APLICADO".equals(entry.outcome())) {
            return null;
        }

        return switch (entry.type()) {
            case LOCK_SOURCE -> ProtocolAction.LOCK_SOURCE;
            case CREATE_RECEIPT -> ProtocolAction.CREATE_RECEIPT;
            case DELIVER_RECEIPT -> ProtocolAction.DELIVER_RECEIPT;
            case PREPARE_DESTINATION ->
                    ProtocolAction.PREPARE_DESTINATION;
            case COMMIT_DESTINATION ->
                    ProtocolAction.COMMIT_DESTINATION;
            case ABORT_TRANSFER -> ProtocolAction.ABORT_TRANSFER;
            case EXPIRE_TRANSFER -> ProtocolAction.EXPIRE_TRANSFER;
            case FAIL_VALIDATION -> ProtocolAction.FAIL_VALIDATION;
            default -> null;
        };
    }

    private static TraceEvent toTraceEvent(
            long step,
            SimulationTraceEntry entry,
            CrossShardSession session,
            ProtocolEvent protocolEvent
    ) {
        boolean protocolTransition = protocolEvent != null;
        TraceEvent.Kind kind = protocolTransition
                ? TraceEvent.Kind.PROTOCOL_TRANSITION
                : TraceEvent.Kind.SIMULATION_EVENT;

        String transferId = session == null
                ? null
                : session.transfer().id();
        Integer sourceShard = session == null
                ? null
                : session.transfer().sourceShardId();
        Integer targetShard = session == null
                ? null
                : session.transfer().targetShardId();
        String sourceUtxoKey = session == null
                ? null
                : session.transfer().sourceUtxo().key();
        String receiptId = session == null
                ? null
                : session.receipt().receiptId();
        Long amount = session == null
                ? null
                : session.transfer().amount();
        Integer sourceApprovals = session == null
                ? null
                : session.sourceApprovals();
        Integer sourceValidators = session == null
                ? null
                : session.sourceValidators();

        boolean targetQuorumObserved = protocolEvent != null
                && switch (protocolEvent.action()) {
                    case PREPARE_DESTINATION,
                         COMMIT_DESTINATION -> true;
                    default -> protocolEvent.previousStatus()
                            == CrossShardStatus.DESTINATION_PREPARED;
                };

        Integer targetApprovals = session != null
                && targetQuorumObserved
                ? session.targetApprovals()
                : null;
        Integer targetValidators = session != null
                && targetQuorumObserved
                ? session.targetValidators()
                : null;

        return new TraceEvent(
                1,
                step,
                entry.sequence(),
                protocolTransition
                        ? protocolEvent.sequence()
                        : null,
                entry.round(),
                kind,
                entry.transferId(),
                transferId,
                entry.type(),
                entry.outcome(),
                protocolTransition
                        ? protocolEvent.action()
                        : null,
                protocolTransition
                        ? protocolEvent.previousStatus()
                        : null,
                protocolTransition
                        ? protocolEvent.nextStatus()
                        : null,
                sourceShard,
                targetShard,
                sourceUtxoKey,
                receiptId,
                amount,
                sourceApprovals,
                sourceValidators,
                targetApprovals,
                targetValidators,
                entry.detail()
        );
    }
}
