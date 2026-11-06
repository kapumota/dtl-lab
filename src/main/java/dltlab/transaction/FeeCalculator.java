package dltlab.transaction;

/** Calcula fees a partir del UTXO pool disponible. */
public final class FeeCalculator {
    private FeeCalculator() {}

    public static long fee(Transaction tx, UTXOPool pool) {
        if (tx.getInputs().isEmpty() || tx.getOutputs().isEmpty()) {
            return Long.MIN_VALUE;
        }
        try {
            long inputs = 0L;
            var claimed = new java.util.HashSet<UTXO>();
            for (Transaction.Input input : tx.getInputs()) {
                UTXO utxo = new UTXO(input.getPreviousTxHash(), input.getOutputIndex());
                Transaction.Output previous = pool.getOutput(utxo);
                if (previous == null || previous.getValue() < 0 || !claimed.add(utxo)) {
                    return Long.MIN_VALUE;
                }
                inputs = Math.addExact(inputs, previous.getValue());
            }
            return Math.subtractExact(inputs, tx.outputSum());
        } catch (ArithmeticException | IllegalArgumentException error) {
            return Long.MIN_VALUE;
        }
    }

    public static FeeRate feeRate(Transaction tx, UTXOPool pool) {
        long fee = fee(tx, pool);
        if (fee == Long.MIN_VALUE) {
            return new FeeRate(Long.MIN_VALUE, TransactionSizeEstimator.virtualSize(tx));
        }
        return new FeeRate(fee, TransactionSizeEstimator.virtualSize(tx));
    }
}
