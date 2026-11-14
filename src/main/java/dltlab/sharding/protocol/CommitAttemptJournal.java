package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardStatus;
import dltlab.verification.LedgerValueOracle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Registro de diagnostico append-only EN MEMORIA, independiente del checkpoint
 * de la sesion. No constituye un log durable ante caida de proceso o host.
 * Cada entrada conserva un snapshot inmutable del ledger en la etapa observada.
 */
public final class CommitAttemptJournal {
    public enum Stage {
        BEGIN, PREPARED, RECEIPT_CONSUMED, SOURCE_DEBITED,
        DESTINATION_CREDITED, COMMIT_COMPLETED, FAILURE_POINT_REACHED,
        ROLLBACK_STARTED, ROLLBACK_COMPLETED, ROLLBACK_FAILED
    }

    public record Entry(
            long sequence,
            String attemptId,
            String transferId,
            long logicalTime,
            Stage stage,
            CrossShardStatus sessionStatus,
            long sessionVersion,
            LedgerValueOracle.Snapshot ledger,
            String detail
    ) {
        public Entry {
            if (sequence < 0 || logicalTime < 0 || sessionVersion < 0) {
                throw new IllegalArgumentException("Secuencia, tiempo o version invalidos");
            }
            if (attemptId == null || attemptId.isBlank()
                    || transferId == null || transferId.isBlank()) {
                throw new IllegalArgumentException("Identificadores obligatorios");
            }
            Objects.requireNonNull(stage, "Etapa obligatoria");
            Objects.requireNonNull(sessionStatus, "Estado obligatorio");
            Objects.requireNonNull(ledger, "Snapshot obligatorio");
            Objects.requireNonNull(detail, "Detalle obligatorio");
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    public synchronized Entry append(
            String attemptId,
            String transferId,
            long logicalTime,
            Stage stage,
            CrossShardStatus sessionStatus,
            long sessionVersion,
            LedgerValueOracle.Snapshot ledger,
            String detail
    ) {
        Entry event = new Entry(entries.size(), attemptId, transferId, logicalTime,
                stage, sessionStatus, sessionVersion, ledger, detail);
        entries.add(event);
        return event;
    }

    public synchronized List<Entry> events() {
        return List.copyOf(entries);
    }
}
