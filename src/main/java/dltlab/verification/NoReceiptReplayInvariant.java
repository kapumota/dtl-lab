package dltlab.verification;

import dltlab.sharding.protocol.ReceiptObservation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Comprueba efectos confirmados a partir de observaciones del ledger, fuera del registro de deduplicacion. */
public class NoReceiptReplayInvariant implements Invariant {
    @Override
    public VerificationResult check(LedgerState state) {
        return checkObservations(state.shardManager() == null
                ? List.of() : state.shardManager().receiptObservations());
    }

    static VerificationResult checkObservations(List<ReceiptObservation> observations) {
        Map<String, ReceiptHistory> histories = new HashMap<>();
        for (ReceiptObservation observation : observations) {
            if (observation.kind() == ReceiptObservation.Kind.REJECTED) {
                continue;
            }
            ReceiptHistory history = histories.computeIfAbsent(observation.receiptId(),
                    ignored -> new ReceiptHistory());
            if (!history.accept(observation)) {
                return failure(observation.receiptId());
            }
        }
        for (var entry : histories.entrySet()) {
            if (entry.getValue().attempt) {
                return failure(entry.getKey());
            }
        }
        return new VerificationResult("Recibos sin replay", true,
                "Cada recibo observado tiene como maximo un credito y un consumo definitivos.");
    }

    private static VerificationResult failure(String receiptId) {
        return new VerificationResult("Recibos sin replay", false,
                "Evidencia incoherente o efecto confirmado duplicado para el recibo " + receiptId + ".");
    }

    private static final class ReceiptHistory {
        private boolean issued;
        private boolean delivered;
        private boolean attempt;
        private boolean confirmed;
        private int credits;
        private int debits;

        private boolean accept(ReceiptObservation observation) {
            return switch (observation.kind()) {
                case ISSUED -> {
                    if (issued) yield false;
                    issued = true;
                    yield true;
                }
                case DELIVERED -> {
                    if (!issued || delivered) yield false;
                    delivered = true;
                    yield true;
                }
                case ATTEMPT -> {
                    if (!delivered || attempt) yield false;
                    attempt = true;
                    credits = 0;
                    debits = 0;
                    yield true;
                }
                case DEBIT -> attempt && observation.effect() != null && ++debits > 0;
                case CREDIT -> attempt && observation.effect() != null && ++credits > 0;
                case ROLLED_BACK -> {
                    if (!attempt) yield false;
                    attempt = false;
                    yield true;
                }
                case CONFIRMED -> {
                    if (!attempt || confirmed || credits != 1 || debits != 1) yield false;
                    attempt = false;
                    confirmed = true;
                    yield true;
                }
                case REJECTED -> true;
            };
        }
    }
}
