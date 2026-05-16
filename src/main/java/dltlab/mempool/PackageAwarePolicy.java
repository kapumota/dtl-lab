package dltlab.mempool;

import dltlab.transaction.FeeCalculator;
import dltlab.transaction.Transaction;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXO;
import dltlab.transaction.UTXOPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Politica package-aware: construye paquetes ancestro -> descendiente y compara el fee total del paquete.
 *
 * La idea educativa es mostrar por que un minero no debe mirar solo transacciones aisladas. Una
 * transaccion hija puede pagar un fee alto, pero ser invalida hasta que su transaccion padre sea
 * incluida en el mismo bloque. Esta politica detecta esas dependencias dentro de la mempool.
 */
public class PackageAwarePolicy implements MempoolPolicy {
    @Override
    public String name() {
        return "Paquetes con dependencias";
    }

    @Override
    public List<Transaction> select(Collection<Transaction> candidates, UTXOPool pool, int maxCount) {
        List<Transaction> mempool = new ArrayList<>(candidates);
        Map<String, Transaction> byId = indexById(mempool);
        UTXOPool workingPool = new UTXOPool(pool);
        List<Transaction> selected = new ArrayList<>();
        Set<String> selectedIds = new HashSet<>();

        while (selected.size() < maxCount) {
            Optional<PackageCandidate> best = findBestPackage(mempool, byId, workingPool, selectedIds, maxCount - selected.size());
            if (best.isEmpty()) {
                break;
            }

            TxValidator validator = new TxValidator(workingPool);
            for (Transaction tx : best.get().orderedTransactions()) {
                if (!selectedIds.contains(tx.id()) && validator.isValidTx(tx)) {
                    validator.applyTransaction(tx);
                    selected.add(tx);
                    selectedIds.add(tx.id());
                }
            }
            workingPool = validator.getUtxoPool();
        }

        return selected;
    }

    private Optional<PackageCandidate> findBestPackage(List<Transaction> mempool,
                                                       Map<String, Transaction> byId,
                                                       UTXOPool pool,
                                                       Set<String> selectedIds,
                                                       int remainingSlots) {
        List<PackageCandidate> packages = new ArrayList<>();
        for (Transaction tx : mempool) {
            if (selectedIds.contains(tx.id())) {
                continue;
            }
            PackageCandidate candidate = buildPackage(tx, byId, pool, selectedIds, remainingSlots);
            if (candidate.valid()) {
                packages.add(candidate);
            }
        }

        return packages.stream()
                .max(Comparator
                        .comparingLong(PackageCandidate::totalFee)
                        .thenComparingDouble(PackageCandidate::feePerTx)
                        .thenComparingInt(PackageCandidate::size));
    }

    private PackageCandidate buildPackage(Transaction target,
                                          Map<String, Transaction> byId,
                                          UTXOPool basePool,
                                          Set<String> selectedIds,
                                          int remainingSlots) {
        LinkedHashMap<String, Transaction> ordered = new LinkedHashMap<>();
        Set<String> visiting = new HashSet<>();
        if (!collectAncestors(target, byId, basePool, selectedIds, visiting, ordered)) {
            return PackageCandidate.invalid();
        }
        if (ordered.size() > remainingSlots) {
            return PackageCandidate.invalid();
        }

        TxValidator validator = new TxValidator(basePool);
        long totalFee = 0L;
        for (Transaction tx : ordered.values()) {
            if (!validator.isValidTx(tx)) {
                return PackageCandidate.invalid();
            }
            long fee = FeeCalculator.fee(tx, validator.getUtxoPool());
            if (fee == Long.MIN_VALUE) {
                return PackageCandidate.invalid();
            }
            totalFee += fee;
            validator.applyTransaction(tx);
        }
        return new PackageCandidate(new ArrayList<>(ordered.values()), totalFee, true);
    }

    private boolean collectAncestors(Transaction tx,
                                     Map<String, Transaction> byId,
                                     UTXOPool basePool,
                                     Set<String> selectedIds,
                                     Set<String> visiting,
                                     LinkedHashMap<String, Transaction> ordered) {
        if (selectedIds.contains(tx.id()) || ordered.containsKey(tx.id())) {
            return true;
        }
        if (!visiting.add(tx.id())) {
            return false; // Ciclo artificial en la mempool.
        }

        for (Transaction.Input input : tx.getInputs()) {
            UTXO inputUtxo = new UTXO(input.getPreviousTxHash(), input.getOutputIndex());
            if (basePool.contains(inputUtxo)) {
                continue;
            }
            Transaction parent = byId.get(hexKey(input.getPreviousTxHash()));
            if (parent == null) {
                visiting.remove(tx.id());
                return false;
            }
            if (input.getOutputIndex() < 0 || input.getOutputIndex() >= parent.getOutputs().size()) {
                visiting.remove(tx.id());
                return false;
            }
            if (!collectAncestors(parent, byId, basePool, selectedIds, visiting, ordered)) {
                visiting.remove(tx.id());
                return false;
            }
        }

        ordered.put(tx.id(), tx);
        visiting.remove(tx.id());
        return true;
    }

    private Map<String, Transaction> indexById(Collection<Transaction> txs) {
        Map<String, Transaction> result = new HashMap<>();
        for (Transaction tx : txs) {
            result.put(tx.id(), tx);
        }
        return result;
    }

    private String hexKey(byte[] hash) {
        return dltlab.crypto.Hashing.hex(hash);
    }

    private record PackageCandidate(List<Transaction> orderedTransactions, long totalFee, boolean valid) {
        static PackageCandidate invalid() {
            return new PackageCandidate(List.of(), Long.MIN_VALUE, false);
        }

        int size() {
            return orderedTransactions.size();
        }

        double feePerTx() {
            return orderedTransactions.isEmpty() ? 0.0 : (double) totalFee / orderedTransactions.size();
        }
    }
}
