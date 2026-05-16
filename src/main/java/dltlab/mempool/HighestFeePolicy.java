package dltlab.mempool;

import dltlab.transaction.FeeCalculator;
import dltlab.transaction.Transaction;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXOPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Politica: intenta priorizar las transacciones que pagan mayor fee. */
public class HighestFeePolicy implements MempoolPolicy {
    @Override
    public String name() {
        return "Mayor fee primero";
    }

    @Override
    public List<Transaction> select(Collection<Transaction> candidates, UTXOPool pool, int maxCount) {
        List<Transaction> ordered = new ArrayList<>(candidates);
        ordered.sort(Comparator.comparingLong((Transaction tx) -> FeeCalculator.fee(tx, pool)).reversed());

        TxValidator validator = new TxValidator(pool);
        List<Transaction> selected = new ArrayList<>();
        for (Transaction tx : ordered) {
            if (selected.size() >= maxCount) break;
            if (validator.isValidTx(tx)) {
                validator.applyTransaction(tx);
                selected.add(tx);
            }
        }
        return selected;
    }
}
