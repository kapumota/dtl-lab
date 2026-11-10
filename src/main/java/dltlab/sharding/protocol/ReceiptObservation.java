package dltlab.sharding.protocol;

/** Hecho local observado durante la vida de un recibo. */
public record ReceiptObservation(String receiptId, Kind kind, LedgerEffect effect) {
    public ReceiptObservation(String receiptId, Kind kind) {
        this(receiptId, kind, null);
    }

    public record LedgerEffect(int shardId, String utxoKey, long amount) {}

    public enum Kind {
        ISSUED, DELIVERED, ATTEMPT, DEBIT, CREDIT, CONFIRMED, ROLLED_BACK, REJECTED
    }
}
