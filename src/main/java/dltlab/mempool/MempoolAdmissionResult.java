package dltlab.mempool;

import dltlab.transaction.Transaction;

import java.util.List;

/** Resultado de admision de una transaccion en una mempool con reglas economicas. */
public record MempoolAdmissionResult(boolean accepted,
                                     String reason,
                                     List<Transaction> evictedTransactions,
                                     Transaction replacedTransaction) {
    public static MempoolAdmissionResult accepted(String reason) {
        return new MempoolAdmissionResult(true, reason, List.of(), null);
    }

    public static MempoolAdmissionResult acceptedWithEvictions(String reason, List<Transaction> evictedTransactions) {
        return new MempoolAdmissionResult(true, reason, List.copyOf(evictedTransactions), null);
    }

    public static MempoolAdmissionResult acceptedWithReplacement(String reason,
                                                                 List<Transaction> evictedTransactions,
                                                                 Transaction replacedTransaction) {
        return new MempoolAdmissionResult(true, reason, List.copyOf(evictedTransactions), replacedTransaction);
    }

    public static MempoolAdmissionResult rejected(String reason) {
        return new MempoolAdmissionResult(false, reason, List.of(), null);
    }
}
