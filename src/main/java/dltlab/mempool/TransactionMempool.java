package dltlab.mempool;

import dltlab.transaction.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Mempool global: transacciones pendientes antes de ser minadas. */
public class TransactionMempool {
    private final Map<String, Transaction> transactions = new LinkedHashMap<>();

    public void add(Transaction tx) {
        transactions.put(tx.id(), tx);
    }

    public void remove(Transaction tx) {
        transactions.remove(tx.id());
    }

    public Collection<Transaction> getAll() {
        return List.copyOf(transactions.values());
    }

    public int size() {
        return transactions.size();
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    public List<Transaction> asList() {
        return new ArrayList<>(transactions.values());
    }
}
