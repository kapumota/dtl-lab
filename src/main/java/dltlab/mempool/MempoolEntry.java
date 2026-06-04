package dltlab.mempool;

import dltlab.transaction.FeeCalculator;
import dltlab.transaction.FeeRate;
import dltlab.transaction.Transaction;
import dltlab.transaction.TransactionSizeEstimator;
import dltlab.transaction.UTXOPool;

/** Entrada enriquecida de mempool con fee, tamano virtual y orden de llegada. */
public record MempoolEntry(Transaction transaction, long fee, long virtualSize, long arrivalOrder) {
    public static MempoolEntry from(Transaction tx, UTXOPool pool, long arrivalOrder) {
        return new MempoolEntry(tx, FeeCalculator.fee(tx, pool), TransactionSizeEstimator.virtualSize(tx), arrivalOrder);
    }

    public FeeRate feeRate() {
        return new FeeRate(fee, virtualSize);
    }
}
