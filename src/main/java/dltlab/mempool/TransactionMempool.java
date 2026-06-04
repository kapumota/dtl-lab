package dltlab.mempool;

import dltlab.transaction.FeeCalculator;
import dltlab.transaction.FeeRate;
import dltlab.transaction.Transaction;
import dltlab.transaction.TransactionSizeEstimator;
import dltlab.transaction.UTXOPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Mempool global: transacciones pendientes antes de ser minadas. */
public class TransactionMempool {
    private final Map<String, Transaction> transactions = new LinkedHashMap<>();
    private final Map<String, Long> arrivalOrderById = new LinkedHashMap<>();
    private final MempoolConfig config;
    private final RbfPolicy rbfPolicy;
    private final EvictionPolicy evictionPolicy;
    private long arrivalCounter = 0L;

    public TransactionMempool() {
        this(MempoolConfig.unbounded(), new RbfPolicy(), new LowestFeeRateEvictionPolicy());
    }

    public TransactionMempool(MempoolConfig config) {
        this(config, new RbfPolicy(), new LowestFeeRateEvictionPolicy());
    }

    public TransactionMempool(MempoolConfig config, RbfPolicy rbfPolicy, EvictionPolicy evictionPolicy) {
        this.config = config;
        this.rbfPolicy = rbfPolicy;
        this.evictionPolicy = evictionPolicy;
    }

    public void add(Transaction tx) {
        transactions.put(tx.id(), tx);
        arrivalOrderById.putIfAbsent(tx.id(), arrivalCounter++);
    }

    public MempoolAdmissionResult admit(Transaction tx, UTXOPool pool) {
        long fee = FeeCalculator.fee(tx, pool);
        if (fee == Long.MIN_VALUE) {
            return MempoolAdmissionResult.rejected("Transaccion rechazada porque gasta un UTXO desconocido.");
        }

        FeeRate feeRate = FeeCalculator.feeRate(tx, pool);
        if (!feeRate.isAtLeast(config.minRelayFeeRateSatsPerVByte())) {
            return MempoolAdmissionResult.rejected("Transaccion rechazada por fee rate menor al minimo de relay.");
        }

        Map<String, Transaction> snapshotTransactions = new LinkedHashMap<>(transactions);
        Map<String, Long> snapshotArrival = new LinkedHashMap<>(arrivalOrderById);

        List<Transaction> conflicts = rbfPolicy.findConflicts(tx, transactions.values());
        if (!conflicts.isEmpty()) {
            if (!config.rbfEnabled()) {
                return MempoolAdmissionResult.rejected("Transaccion rechazada por conflicto y RBF desactivado.");
            }
            if (!rbfPolicy.canReplace(tx, conflicts, pool)) {
                return MempoolAdmissionResult.rejected("Transaccion rechazada porque no mejora fee y fee rate del conflicto.");
            }
            for (Transaction conflict : conflicts) {
                transactions.remove(conflict.id());
                arrivalOrderById.remove(conflict.id());
            }
        }

        add(tx);
        List<Transaction> evictedTransactions = applyEvictionIfNeeded(tx, pool);
        if (!transactions.containsKey(tx.id())) {
            transactions.clear();
            transactions.putAll(snapshotTransactions);
            arrivalOrderById.clear();
            arrivalOrderById.putAll(snapshotArrival);
            return MempoolAdmissionResult.rejected("Transaccion rechazada porque no sobrevive a la eviction por bajo fee rate.");
        }

        if (!conflicts.isEmpty()) {
            Transaction replaced = conflicts.size() == 1 ? conflicts.get(0) : null;
            List<Transaction> allRemoved = new ArrayList<>(conflicts);
            allRemoved.addAll(evictedTransactions);
            return MempoolAdmissionResult.acceptedWithReplacement("Transaccion aceptada por RBF con fee rate superior.", allRemoved, replaced);
        }

        if (!evictedTransactions.isEmpty()) {
            return MempoolAdmissionResult.acceptedWithEvictions("Transaccion aceptada y se descartaron transacciones de bajo fee rate.", evictedTransactions);
        }
        return MempoolAdmissionResult.accepted("Transaccion aceptada en mempool.");
    }

    public void remove(Transaction tx) {
        transactions.remove(tx.id());
        arrivalOrderById.remove(tx.id());
    }

    public Collection<Transaction> getAll() {
        return List.copyOf(transactions.values());
    }

    public boolean contains(Transaction tx) {
        return transactions.containsKey(tx.id());
    }

    public int size() {
        return transactions.size();
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    public long virtualSize() {
        long total = 0L;
        for (Transaction tx : transactions.values()) {
            total += TransactionSizeEstimator.virtualSize(tx);
        }
        return total;
    }

    public List<Transaction> asList() {
        return new ArrayList<>(transactions.values());
    }

    private List<Transaction> applyEvictionIfNeeded(Transaction candidate, UTXOPool pool) {
        if (!config.evictionEnabled() || virtualSize() <= config.maxMempoolVBytes()) {
            return List.of();
        }

        List<MempoolEntry> entries = new ArrayList<>();
        for (Transaction current : transactions.values()) {
            entries.add(MempoolEntry.from(current, pool, arrivalOrderById.getOrDefault(current.id(), Long.MAX_VALUE)));
        }

        List<MempoolEntry> evictions = evictionPolicy.chooseEvictions(entries, config.maxMempoolVBytes());
        List<Transaction> evictedTransactions = new ArrayList<>();
        for (MempoolEntry eviction : evictions) {
            Transaction removed = transactions.remove(eviction.transaction().id());
            arrivalOrderById.remove(eviction.transaction().id());
            if (removed != null && !removed.id().equals(candidate.id())) {
                evictedTransactions.add(removed);
            }
        }
        return evictedTransactions;
    }
}
