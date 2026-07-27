package dltlab.sharding;

import dltlab.transaction.UTXOPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Fragmento del ledger con su propio UTXO pool, validadores y recibos consumidos. */
public class Shard {
    private final int id;
    private final UTXOPool utxoPool = new UTXOPool();
    private final Set<String> lockedUtxos = new HashSet<>();
    private final Set<String> consumedReceipts = new HashSet<>();
    private final List<ShardValidator> validators = new ArrayList<>();

    public Shard(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public UTXOPool getUtxoPool() {
        return utxoPool;
    }

    public boolean lockUtxo(String utxoKey) {
        return lockedUtxos.add(utxoKey);
    }

    public boolean unlockUtxo(String utxoKey) {
        return lockedUtxos.remove(utxoKey);
    }

    public boolean isLocked(String utxoKey) {
        return lockedUtxos.contains(utxoKey);
    }

    public Set<String> getLockedUtxos() {
        return Collections.unmodifiableSet(lockedUtxos);
    }

    public boolean markReceiptConsumed(String receiptId) {
        return consumedReceipts.add(receiptId);
    }

    /** Restaura un recibo como no consumido durante un rollback atomico. */
    public boolean unmarkReceiptConsumed(String receiptId) {
        return consumedReceipts.remove(receiptId);
    }

    public Set<String> getConsumedReceipts() {
        return Collections.unmodifiableSet(consumedReceipts);
    }

    public void addValidator(ShardValidator validator) {
        validators.add(validator);
    }

    public List<ShardValidator> getValidators() {
        return Collections.unmodifiableList(validators);
    }

    public int onlineValidators() {
        int count = 0;
        for (ShardValidator validator : validators) {
            if (validator.online()) count++;
        }
        return count;
    }

    public int approvingValidators() {
        int count = 0;
        for (ShardValidator validator : validators) {
            if (validator.approves()) count++;
        }
        return count;
    }
}
