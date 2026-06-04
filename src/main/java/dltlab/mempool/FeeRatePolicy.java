package dltlab.mempool;

import dltlab.transaction.FeeCalculator;
import dltlab.transaction.FeeRate;
import dltlab.transaction.Transaction;
import dltlab.transaction.TransactionSizeEstimator;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXOPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Politica realista: prioriza mayor fee rate y respeta capacidad de bloque en vBytes. */
public class FeeRatePolicy implements MempoolPolicy {
    @Override
    public String name() {
        return "Mayor fee rate por vByte";
    }

    @Override
    public List<Transaction> select(Collection<Transaction> candidates, UTXOPool pool, int maxCount) {
        List<Transaction> ordered = orderByFeeRate(candidates, pool);
        TxValidator validator = new TxValidator(pool);
        List<Transaction> selected = new ArrayList<>();
        for (Transaction tx : ordered) {
            if (selected.size() >= maxCount) {
                break;
            }
            if (validator.isValidTx(tx)) {
                validator.applyTransaction(tx);
                selected.add(tx);
            }
        }
        return selected;
    }

    public List<Transaction> selectByVirtualSize(Collection<Transaction> candidates, UTXOPool pool, long maxBlockVBytes) {
        if (maxBlockVBytes <= 0) {
            throw new IllegalArgumentException("La capacidad del bloque debe ser positiva.");
        }

        List<Transaction> ordered = orderByFeeRate(candidates, pool);
        TxValidator validator = new TxValidator(pool);
        List<Transaction> selected = new ArrayList<>();
        long usedVBytes = 0L;
        for (Transaction tx : ordered) {
            long txVBytes = TransactionSizeEstimator.virtualSize(tx);
            if (usedVBytes + txVBytes > maxBlockVBytes) {
                continue;
            }
            if (validator.isValidTx(tx)) {
                validator.applyTransaction(tx);
                selected.add(tx);
                usedVBytes += txVBytes;
            }
        }
        return selected;
    }

    private List<Transaction> orderByFeeRate(Collection<Transaction> candidates, UTXOPool pool) {
        List<Transaction> ordered = new ArrayList<>(candidates);
        ordered.sort(Comparator
                .comparing((Transaction tx) -> FeeCalculator.feeRate(tx, pool))
                .reversed()
                .thenComparing(Comparator.comparingLong((Transaction tx) -> FeeCalculator.fee(tx, pool)).reversed()));
        return ordered;
    }
}
