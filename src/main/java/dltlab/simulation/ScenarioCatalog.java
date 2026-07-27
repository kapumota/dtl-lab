package dltlab.simulation;

import dltlab.sharding.CrossShardTransfer;
import dltlab.transaction.UTXO;

import java.nio.ByteBuffer;

/** Construye escenarios configurables sin usar threads ni tiempo del sistema. */
public final class ScenarioCatalog {
    private ScenarioCatalog() {
    }

    public static SimulationRun run(SimulationScenario scenario, long seed) {
        SimulationRun run = create(scenario, seed);
        run.runAll();
        return run;
    }

    public static SimulationRun create(SimulationScenario scenario, long seed) {
        return switch (scenario) {
            case S01_NORMAL_COMMIT -> normalCommit(seed);
            case S02_TIMEOUT_BEFORE_DELIVERY -> timeoutBeforeDelivery(seed);
            case S03_DUPLICATED_RECEIPT -> duplicatedReceipt(seed);
            case S04_DELAYED_AFTER_TIMEOUT -> delayedAfterTimeout(seed);
            case S05_COMMIT_TIMEOUT_SAME_ROUND -> commitTimeoutSameRound(seed);
            case S06_SAME_UTXO_CONFLICT -> sameUtxoConflict(seed);
            case S07_BIDIRECTIONAL_TRANSFERS -> bidirectionalTransfers(seed);
            case S08_TEMPORARY_TARGET_OUTAGE -> temporaryTargetOutage(seed);
            case S09_INSUFFICIENT_QUORUM -> insufficientQuorum(seed);
            case S10_MULTIPLE_CONCURRENT_SESSIONS -> multipleConcurrentSessions(seed);
        };
    }

    private static SimulationRun normalCommit(long seed) {
        SimulationRun run = new SimulationRun(2, 4, 3, seed, new NoFaultModel());
        CrossShardTransfer transfer = fundedTransfer(run, seed, 1, 0, 1, 10_000L, 4_000L);
        run.scheduleBegin(transfer, 5, 0, 0);
        run.scheduleSendReceipt(transfer.id(), 1, 0);
        run.scheduleCommit(transfer.id(), 2, 0);
        return run;
    }

    private static SimulationRun timeoutBeforeDelivery(long seed) {
        SimulationRun run = new SimulationRun(2, 4, 3, seed, new DroppedReceiptModel());
        CrossShardTransfer transfer = fundedTransfer(run, seed, 2, 0, 1, 10_000L, 4_000L);
        run.scheduleBegin(transfer, 1, 0, 0);
        run.scheduleSendReceipt(transfer.id(), 0, 10);
        run.scheduleExpire(transfer.id(), 2, 0);
        run.scheduleCommit(transfer.id(), 3, 20);
        return run;
    }

    private static SimulationRun duplicatedReceipt(long seed) {
        SimulationRun run = new SimulationRun(2, 4, 3, seed, new DuplicateReceiptModel());
        CrossShardTransfer transfer = fundedTransfer(run, seed, 3, 0, 1, 12_000L, 5_000L);
        run.scheduleBegin(transfer, 5, 0, 0);
        run.scheduleSendReceipt(transfer.id(), 1, 0);
        run.scheduleCommit(transfer.id(), 2, 0);
        return run;
    }

    private static SimulationRun delayedAfterTimeout(long seed) {
        SimulationRun run = new SimulationRun(2, 4, 3, seed, new DelayedReceiptModel(4));
        CrossShardTransfer transfer = fundedTransfer(run, seed, 4, 0, 1, 11_000L, 4_000L);
        run.scheduleBegin(transfer, 2, 0, 0);
        run.scheduleSendReceipt(transfer.id(), 0, 10);
        run.scheduleExpire(transfer.id(), 3, 0);
        run.scheduleCommit(transfer.id(), 4, 20);
        return run;
    }

    private static SimulationRun commitTimeoutSameRound(long seed) {
        SimulationRun run = new SimulationRun(2, 4, 3, seed, new CommitTimeoutRaceModel());
        CrossShardTransfer transfer = fundedTransfer(run, seed, 5, 0, 1, 13_000L, 6_000L);
        run.scheduleBegin(transfer, 1, 0, 0);
        run.scheduleSendReceipt(transfer.id(), 0, 0);
        boolean commitFirst = run.random().nextBoolean();
        run.scheduleCommit(transfer.id(), 1, commitFirst ? 10 : 20);
        run.scheduleExpire(transfer.id(), 1, commitFirst ? 20 : 10);
        return run;
    }

    private static SimulationRun sameUtxoConflict(long seed) {
        SimulationRun run = new SimulationRun(3, 4, 3, seed, new NoFaultModel());
        UTXO shared = deterministicUtxo(seed, 6);
        run.addFunds(0, shared, 15_000L);
        CrossShardTransfer first = new CrossShardTransfer(0, 1, shared, 5_000L, null);
        CrossShardTransfer second = new CrossShardTransfer(0, 2, shared, 4_000L, null);
        boolean firstWins = run.random().nextBoolean();
        run.scheduleBegin(first, 5, 0, firstWins ? 0 : 1);
        run.scheduleBegin(second, 5, 0, firstWins ? 1 : 0);
        run.scheduleSendReceipt(first.id(), 1, 0);
        run.scheduleSendReceipt(second.id(), 1, 1);
        run.scheduleCommit(first.id(), 2, 0);
        run.scheduleCommit(second.id(), 2, 1);
        return run;
    }

    private static SimulationRun bidirectionalTransfers(long seed) {
        SimulationRun run = new SimulationRun(2, 4, 3, seed, new NoFaultModel());
        CrossShardTransfer first = fundedTransfer(run, seed, 7, 0, 1, 10_000L, 3_000L);
        CrossShardTransfer second = fundedTransfer(run, seed, 8, 1, 0, 9_000L, 2_000L);
        int firstPriority = run.random().nextInt(10);
        int secondPriority = run.random().nextInt(10);
        run.scheduleBegin(first, 5, 0, firstPriority);
        run.scheduleBegin(second, 5, 0, secondPriority);
        run.scheduleSendReceipt(first.id(), 1, secondPriority);
        run.scheduleSendReceipt(second.id(), 1, firstPriority);
        run.scheduleCommit(first.id(), 2, firstPriority);
        run.scheduleCommit(second.id(), 2, secondPriority);
        return run;
    }

    private static SimulationRun temporaryTargetOutage(long seed) {
        SimulationRun run = new SimulationRun(2, 4, 3, seed, new DelayedReceiptModel(2));
        CrossShardTransfer transfer = fundedTransfer(run, seed, 9, 0, 1, 10_000L, 4_000L);
        run.scheduleBegin(transfer, 6, 0, 0);
        run.scheduleShardAvailability(1, false, 1, 0);
        run.scheduleSendReceipt(transfer.id(), 1, 10);
        run.scheduleShardAvailability(1, true, 3, 0);
        run.scheduleCommit(transfer.id(), 4, 0);
        return run;
    }

    private static SimulationRun insufficientQuorum(long seed) {
        SimulationRun run = new SimulationRun(2, 4, 3, seed, new NoFaultModel());
        CrossShardTransfer transfer = fundedTransfer(run, seed, 10, 0, 1, 10_000L, 4_000L);
        run.scheduleBegin(transfer, 5, 0, 0);
        run.scheduleSendReceipt(transfer.id(), 1, 0);
        run.scheduleShardAvailability(1, false, 1, 10);
        run.scheduleCommit(transfer.id(), 2, 0);
        return run;
    }

    private static SimulationRun multipleConcurrentSessions(long seed) {
        SimulationRun run = new SimulationRun(4, 4, 3, seed, new ReorderedMessageModel(1));
        for (int index = 0; index < 6; index++) {
            int source = index % 4;
            int target = (source + 1 + (index % 2)) % 4;
            CrossShardTransfer transfer = fundedTransfer(run, seed, 20 + index,
                    source, target, 20_000L + index, 5_000L + index);
            int beginPriority = run.random().nextInt(20);
            int sendPriority = run.random().nextInt(20);
            int commitPriority = run.random().nextInt(20);
            run.scheduleBegin(transfer, 8, 0, beginPriority);
            run.scheduleSendReceipt(transfer.id(), 1, sendPriority);
            run.scheduleCommit(transfer.id(), 3, commitPriority);
            run.scheduleCommit(transfer.id(), 5, commitPriority + 20);
        }
        return run;
    }

    private static CrossShardTransfer fundedTransfer(SimulationRun run, long seed, int marker,
                                                      int source, int target, long value, long amount) {
        UTXO utxo = deterministicUtxo(seed, marker);
        run.addFunds(source, utxo, value);
        return new CrossShardTransfer(source, target, utxo, amount, null);
    }

    private static UTXO deterministicUtxo(long seed, int marker) {
        byte[] bytes = ByteBuffer.allocate(Long.BYTES + Integer.BYTES)
                .putLong(seed)
                .putInt(marker)
                .array();
        return new UTXO(bytes, 0);
    }
}
