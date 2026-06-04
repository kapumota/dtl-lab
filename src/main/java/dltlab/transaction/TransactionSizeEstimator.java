package dltlab.transaction;

/** Estima el tamano virtual de una transaccion en vBytes para politicas de fee rate. */
public final class TransactionSizeEstimator {
    private TransactionSizeEstimator() {}

    public static long virtualSize(Transaction tx) {
        // En esta version educativa no hay witness separado. Por eso se aproxima vBytes con bytes serializados.
        long rawSize = tx.getRawData().length;
        return Math.max(1L, rawSize);
    }
}
