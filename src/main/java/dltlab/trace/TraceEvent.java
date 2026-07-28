package dltlab.trace;

import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.protocol.ProtocolAction;
import dltlab.simulation.SimulationEventType;

import java.util.Objects;

/** Evento concreto y determinista observado en la implementacion Java. */
public record TraceEvent(
        int schemaVersion,
        long step,
        long simulationSequence,
        Long sessionSequence,
        int logicalRound,
        Kind kind,
        String subjectId,
        String transferId,
        SimulationEventType simulationEventType,
        String outcome,
        ProtocolAction javaAction,
        CrossShardStatus previousStatus,
        CrossShardStatus nextStatus,
        Integer sourceShard,
        Integer targetShard,
        String sourceUtxoKey,
        String receiptId,
        Long amount,
        Integer sourceApprovals,
        Integer sourceValidators,
        Integer targetApprovals,
        Integer targetValidators,
        String detail
) {
    /** Clasifica observaciones del simulador y transiciones del protocolo. */
    public enum Kind {
        SIMULATION_EVENT,
        PROTOCOL_TRANSITION
    }

    public TraceEvent {
        if (schemaVersion != 1) {
            throw new IllegalArgumentException(
                    "La version del esquema de evento debe ser 1.");
        }
        if (step < 0 || simulationSequence < 0 || logicalRound < 0) {
            throw new IllegalArgumentException(
                    "El paso, la secuencia de simulacion y la ronda logica "
                            + "deben ser no negativos.");
        }
        if (sessionSequence != null && sessionSequence < 0) {
            throw new IllegalArgumentException(
                    "La secuencia de sesion no puede ser negativa.");
        }
        kind = Objects.requireNonNull(
                kind,
                "El tipo de evento es obligatorio."
        );
        subjectId = requireText(
                subjectId,
                "El identificador del sujeto es obligatorio."
        );
        simulationEventType = Objects.requireNonNull(
                simulationEventType,
                "El tipo de evento de simulacion es obligatorio."
        );
        outcome = requireText(
                outcome,
                "El resultado observado es obligatorio."
        );
        detail = requireText(
                detail,
                "El detalle observado es obligatorio."
        );

        if (transferId == null) {
            requireAbsentTransferMetadata(
                    sourceShard,
                    targetShard,
                    sourceUtxoKey,
                    receiptId,
                    amount,
                    sourceApprovals,
                    sourceValidators,
                    targetApprovals,
                    targetValidators
            );
        } else {
            transferId = requireText(
                    transferId,
                    "El identificador de transferencia es obligatorio."
            );
            validateTransferMetadata(
                    sourceShard,
                    targetShard,
                    sourceUtxoKey,
                    receiptId,
                    amount,
                    sourceApprovals,
                    sourceValidators,
                    targetApprovals,
                    targetValidators
            );
        }

        if (kind == Kind.PROTOCOL_TRANSITION) {
            if (transferId == null) {
                throw new IllegalArgumentException(
                        "Una transicion del protocolo debe identificar "
                                + "su transferencia.");
            }
            if (sessionSequence == null) {
                throw new IllegalArgumentException(
                        "Una transicion del protocolo debe indicar "
                                + "su secuencia de sesion.");
            }
            javaAction = Objects.requireNonNull(
                    javaAction,
                    "La accion Java es obligatoria."
            );
            nextStatus = Objects.requireNonNull(
                    nextStatus,
                    "El estado siguiente es obligatorio."
            );
            if (javaAction == ProtocolAction.CREATE_SESSION
                    && previousStatus != null) {
                throw new IllegalArgumentException(
                        "La creacion de sesion no debe tener estado anterior.");
            }
            if (javaAction != ProtocolAction.CREATE_SESSION
                    && previousStatus == null) {
                throw new IllegalArgumentException(
                        "Una transicion debe indicar su estado anterior.");
            }
        } else {
            if (sessionSequence != null
                    || javaAction != null
                    || previousStatus != null
                    || nextStatus != null) {
                throw new IllegalArgumentException(
                        "Un evento de simulacion no debe declarar "
                                + "una transicion del protocolo.");
            }
        }
    }

    public boolean isProtocolTransition() {
        return kind == Kind.PROTOCOL_TRANSITION;
    }

    private static void requireAbsentTransferMetadata(
            Integer sourceShard,
            Integer targetShard,
            String sourceUtxoKey,
            String receiptId,
            Long amount,
            Integer sourceApprovals,
            Integer sourceValidators,
            Integer targetApprovals,
            Integer targetValidators
    ) {
        if (sourceShard != null
                || targetShard != null
                || sourceUtxoKey != null
                || receiptId != null
                || amount != null
                || sourceApprovals != null
                || sourceValidators != null
                || targetApprovals != null
                || targetValidators != null) {
            throw new IllegalArgumentException(
                    "Un evento sin transferencia no debe incluir "
                            + "metadatos de transferencia.");
        }
    }

    private static void validateTransferMetadata(
            Integer sourceShard,
            Integer targetShard,
            String sourceUtxoKey,
            String receiptId,
            Long amount,
            Integer sourceApprovals,
            Integer sourceValidators,
            Integer targetApprovals,
            Integer targetValidators
    ) {
        if (sourceShard == null
                || targetShard == null
                || sourceShard < 0
                || targetShard < 0
                || sourceShard.equals(targetShard)) {
            throw new IllegalArgumentException(
                    "Los shards de origen y destino deben ser distintos "
                            + "y no negativos.");
        }
        requireText(
                sourceUtxoKey,
                "La clave del UTXO origen es obligatoria."
        );
        requireText(
                receiptId,
                "El identificador de recibo es obligatorio."
        );
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException(
                    "El monto de la transferencia debe ser positivo.");
        }
        validateApprovals(
                sourceApprovals,
                sourceValidators,
                false,
                "origen"
        );
        validateApprovals(
                targetApprovals,
                targetValidators,
                true,
                "destino"
        );
    }

    private static void validateApprovals(
            Integer approvals,
            Integer validators,
            boolean allowEmpty,
            String shardName
    ) {
        if (allowEmpty && approvals == null && validators == null) {
            return;
        }
        if (approvals == null
                || validators == null
                || validators <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de validadores del shard "
                            + shardName
                            + " debe ser positiva.");
        }
        if (approvals < 0 || approvals > validators) {
            throw new IllegalArgumentException(
                    "Las aprobaciones del shard "
                            + shardName
                            + " deben estar entre cero y la cantidad "
                            + "de validadores.");
        }
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
