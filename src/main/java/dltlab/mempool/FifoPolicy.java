package dltlab.mempool;

import dltlab.transaction.Transaction;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXOPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Politica simple: respeta el orden de llegada. */
public class FifoPolicy implements MempoolPolicy {
    @Override
    public String name() {
        return "FIFO";
    }

    @Override
    public List<Transaction> select(Collection<Transaction> candidates, UTXOPool pool, int maxCount) {
        TxValidator validator = new TxValidator(pool);
        List<Transaction> selected = new ArrayList<>();
        for (Transaction tx : candidates) {
            if (selected.size() >= maxCount) break;
            if (validator.isValidTx(tx)) {
                validator.applyTransaction(tx);
                selected.add(tx);
            }
        }
        return selected;
    }
}
