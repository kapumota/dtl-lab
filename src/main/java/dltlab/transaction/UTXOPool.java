package dltlab.transaction;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Conjunto actual de salidas no gastadas. */
public class UTXOPool {
    private final Map<UTXO, Transaction.Output> pool = new LinkedHashMap<>();

    public UTXOPool() {}

    public UTXOPool(UTXOPool other) {
        this.pool.putAll(other.pool);
    }

    public void addUTXO(UTXO utxo, Transaction.Output output) {
        pool.put(utxo, output);
    }

    public void removeUTXO(UTXO utxo) {
        pool.remove(utxo);
    }

    public boolean contains(UTXO utxo) {
        return pool.containsKey(utxo);
    }

    public Transaction.Output getOutput(UTXO utxo) {
        return pool.get(utxo);
    }

    public Set<UTXO> getAllUTXO() {
        return Collections.unmodifiableSet(pool.keySet());
    }

    public Collection<Transaction.Output> getAllOutputs() {
        return Collections.unmodifiableCollection(pool.values());
    }

    public int size() {
        return pool.size();
    }

    public long totalValue() {
        long total = 0L;
        for (Transaction.Output output : pool.values()) {
            total += output.getValue();
        }
        return total;
    }
}
