package dltlab.sharding.protocol;

import dltlab.sharding.Shard;
import dltlab.sharding.CrossShardStatus;
import dltlab.verification.LedgerValueOracle;
import java.util.List;

/** Comprueba secuencia, vistas inmutables y snapshots del journal. */
public final class CommitAttemptJournalTest {
    public static void main(String[] args) {
        var journal = new CommitAttemptJournal();
        var snapshot = LedgerValueOracle.capture(List.of(new Shard(0)));
        journal.append("t1/attempt-1", "t1", 3,
                CommitAttemptJournal.Stage.BEGIN, CrossShardStatus.RECEIPT_DELIVERED,
                2, snapshot, "Inicia tentativa");
        journal.append("t1/attempt-1", "t1", 3,
                CommitAttemptJournal.Stage.ROLLBACK_STARTED, CrossShardStatus.DESTINATION_PREPARED,
                3, snapshot, "Rollback iniciado");
        journal.append("t1/attempt-1", "t1", 4,
                CommitAttemptJournal.Stage.ROLLBACK_COMPLETED, CrossShardStatus.RECEIPT_DELIVERED,
                2, snapshot, "Rollback completo");
        var entries = journal.events();
        if (entries.size() != 3 || entries.get(2).sequence() != 2
                || entries.get(0).ledger() != snapshot) {
            throw new AssertionError("El journal no preserva secuencia o snapshot");
        }
        try {
            entries.clear();
            throw new AssertionError("La vista debe ser inmutable");
        } catch (UnsupportedOperationException expected) {
            // Vista protegida.
        }
        System.out.println("PASS: journal de commit con snapshots inmutables");
    }
}
