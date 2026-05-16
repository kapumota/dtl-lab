package dltlab.sharding;

/** Metrica agregada de sharding para una ronda logica de simulacion. */
public record ShardRoundMetric(
        int round,
        int pendingTransfers,
        int committedTransfers,
        int abortedTransfers,
        int timedOutTransfers,
        int failedValidationTransfers,
        int lockedUtxos,
        long totalValueMoved,
        int totalValidators,
        int onlineValidators
) {}
