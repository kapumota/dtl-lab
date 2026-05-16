package dltlab.mempool;

import dltlab.transaction.FeeCalculator;
import dltlab.transaction.Transaction;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXOPool;

import java.util.List;

/** Reporte pequeno para comparar estrategias de mempool. */
public record SelectionReport(String policyName, int selectedCount, long totalFee) {
    public static SelectionReport from(String policyName, List<Transaction> txs, UTXOPool pool) {
        long total = 0L;
        TxValidator validator = new TxValidator(pool);
        for (Transaction tx : txs) {
            long fee = FeeCalculator.fee(tx, validator.getUtxoPool());
            if (fee != Long.MIN_VALUE && validator.isValidTx(tx)) {
                total += fee;
                validator.applyTransaction(tx);
            }
        }
        return new SelectionReport(policyName, txs.size(), total);
    }
}
