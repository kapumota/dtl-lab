package dltlab.mempool;

import dltlab.transaction.FeeCalculator;
import dltlab.transaction.FeeRate;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.transaction.UTXOPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Politica educativa de reemplazo por fee para conflictos sobre los mismos UTXO. */
public class RbfPolicy {
    public List<Transaction> findConflicts(Transaction candidate, Collection<Transaction> currentTransactions) {
        List<Transaction> conflicts = new ArrayList<>();
        for (Transaction current : currentTransactions) {
            if (spendsAnySameInput(candidate, current)) {
                conflicts.add(current);
            }
        }
        return conflicts;
    }

    public boolean canReplace(Transaction candidate, List<Transaction> conflicts, UTXOPool pool) {
        if (conflicts.isEmpty()) {
            return false;
        }

        long candidateFee = FeeCalculator.fee(candidate, pool);
        if (candidateFee == Long.MIN_VALUE) {
            return false;
        }

        long conflictFee = 0L;
        FeeRate bestConflictRate = new FeeRate(Long.MIN_VALUE, 1L);
        for (Transaction conflict : conflicts) {
            long fee = FeeCalculator.fee(conflict, pool);
            if (fee == Long.MIN_VALUE) {
                return false;
            }
            conflictFee += fee;
            FeeRate rate = FeeCalculator.feeRate(conflict, pool);
            if (rate.compareTo(bestConflictRate) > 0) {
                bestConflictRate = rate;
            }
        }

        FeeRate candidateRate = FeeCalculator.feeRate(candidate, pool);
        return candidateFee > conflictFee && candidateRate.compareTo(bestConflictRate) > 0;
    }

    private boolean spendsAnySameInput(Transaction left, Transaction right) {
        for (Transaction.Input leftInput : left.getInputs()) {
            UTXO leftUtxo = new UTXO(leftInput.getPreviousTxHash(), leftInput.getOutputIndex());
            for (Transaction.Input rightInput : right.getInputs()) {
                UTXO rightUtxo = new UTXO(rightInput.getPreviousTxHash(), rightInput.getOutputIndex());
                if (leftUtxo.equals(rightUtxo)) {
                    return true;
                }
            }
        }
        return false;
    }
}
