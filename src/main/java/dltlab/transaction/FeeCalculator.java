package dltlab.transaction;

/** Calcula fees a partir del UTXO pool disponible. */
public final class FeeCalculator {
    private FeeCalculator() {}

    public static long fee(Transaction tx, UTXOPool pool) {
        long inputs = 0L;
        for (Transaction.Input input : tx.getInputs()) {
            Transaction.Output previous = pool.getOutput(new UTXO(input.getPreviousTxHash(), input.getOutputIndex()));
            if (previous == null) {
                return Long.MIN_VALUE;
            }
            inputs += previous.getValue();
        }
        return inputs - tx.outputSum();
    }
}
