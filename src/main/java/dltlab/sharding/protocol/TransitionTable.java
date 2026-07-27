package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardStatus;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Tabla central e inmutable de transiciones permitidas por el protocolo. */
public final class TransitionTable {
    private static final Set<ProtocolTransition> TRANSITIONS = buildTransitions();

    private TransitionTable() {
    }

    public static boolean isAllowed(CrossShardStatus current, ProtocolAction action,
                                    CrossShardStatus next) {
        return TRANSITIONS.stream().anyMatch(transition -> transition.matches(current, action, next));
    }

    public static boolean canTransition(CrossShardStatus current, CrossShardStatus next) {
        return TRANSITIONS.stream()
                .anyMatch(transition -> transition.from() == current && transition.to() == next);
    }

    public static List<ProtocolTransition> allowedFrom(CrossShardStatus current) {
        return TRANSITIONS.stream()
                .filter(transition -> transition.from() == current)
                .toList();
    }

    public static Set<ProtocolTransition> transitions() {
        return TRANSITIONS;
    }

    private static Set<ProtocolTransition> buildTransitions() {
        LinkedHashSet<ProtocolTransition> transitions = new LinkedHashSet<>();
        add(transitions, CrossShardStatus.CREATED, ProtocolAction.LOCK_SOURCE,
                CrossShardStatus.SOURCE_LOCKED);
        add(transitions, CrossShardStatus.SOURCE_LOCKED, ProtocolAction.CREATE_RECEIPT,
                CrossShardStatus.RECEIPT_CREATED);
        add(transitions, CrossShardStatus.RECEIPT_CREATED, ProtocolAction.DELIVER_RECEIPT,
                CrossShardStatus.RECEIPT_DELIVERED);
        add(transitions, CrossShardStatus.RECEIPT_DELIVERED, ProtocolAction.PREPARE_DESTINATION,
                CrossShardStatus.DESTINATION_PREPARED);
        add(transitions, CrossShardStatus.DESTINATION_PREPARED, ProtocolAction.COMMIT_DESTINATION,
                CrossShardStatus.COMMITTED);

        for (CrossShardStatus status : nonTerminalStatuses()) {
            add(transitions, status, ProtocolAction.ABORT_TRANSFER, CrossShardStatus.ABORTED);
            add(transitions, status, ProtocolAction.FAIL_VALIDATION,
                    CrossShardStatus.FAILED_VALIDATION);
        }
        for (CrossShardStatus status : lockedStatuses()) {
            add(transitions, status, ProtocolAction.EXPIRE_TRANSFER, CrossShardStatus.TIMED_OUT);
        }
        return Collections.unmodifiableSet(transitions);
    }

    private static List<CrossShardStatus> nonTerminalStatuses() {
        return List.of(
                CrossShardStatus.CREATED,
                CrossShardStatus.SOURCE_LOCKED,
                CrossShardStatus.RECEIPT_CREATED,
                CrossShardStatus.RECEIPT_DELIVERED,
                CrossShardStatus.DESTINATION_PREPARED
        );
    }

    private static List<CrossShardStatus> lockedStatuses() {
        return List.of(
                CrossShardStatus.SOURCE_LOCKED,
                CrossShardStatus.RECEIPT_CREATED,
                CrossShardStatus.RECEIPT_DELIVERED,
                CrossShardStatus.DESTINATION_PREPARED
        );
    }

    private static void add(Set<ProtocolTransition> transitions, CrossShardStatus from,
                            ProtocolAction action, CrossShardStatus to) {
        transitions.add(new ProtocolTransition(from, action, to));
    }
}
