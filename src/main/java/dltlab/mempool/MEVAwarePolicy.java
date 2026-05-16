package dltlab.mempool;

import dltlab.transaction.FeeCalculator;
import dltlab.transaction.Transaction;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXOPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Politica simplificada que suma un bono MEV al fee para estudiar transaction ordering. */
public class MEVAwarePolicy implements MempoolPolicy {
    private final Map<String, Long> mevBonusByTxId = new HashMap<>();

    public void registerOpportunity(Transaction tx, long extractableValue) {
        mevBonusByTxId.put(tx.id(), extractableValue);
    }

    @Override
    public String name() {
        return "MEV simplificado";
    }

    @Override
    public List<Transaction> select(Collection<Transaction> candidates, UTXOPool pool, int maxCount) {
        List<Transaction> ordered = new ArrayList<>(candidates);
        ordered.sort(Comparator.comparingLong((Transaction tx) -> score(tx, pool)).reversed());

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

    private long score(Transaction tx, UTXOPool pool) {
        long fee = FeeCalculator.fee(tx, pool);
        if (fee == Long.MIN_VALUE) {
            return Long.MIN_VALUE;
        }
        return fee + mevBonusByTxId.getOrDefault(tx.id(), 0L);
    }
}
