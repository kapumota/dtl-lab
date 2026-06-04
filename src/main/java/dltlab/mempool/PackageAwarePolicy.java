package dltlab.mempool;

import dltlab.transaction.FeeCalculator;
import dltlab.transaction.FeeRate;
import dltlab.transaction.Transaction;
import dltlab.transaction.TransactionSizeEstimator;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXO;
import dltlab.transaction.UTXOPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Politica package-aware: construye paquetes ancestro a descendiente y compara su fee rate total.
 *
 * Una transaccion hija puede pagar un fee alto, pero ser invalida hasta que su padre entre en el
 * mismo bloque. Esta politica detecta esas dependencias y permite estudiar CPFP.
 */
public class PackageAwarePolicy implements MempoolPolicy {
    @Override
    public String name() {
        return "Paquetes con dependencias";
    }

    @Override
    public List<Transaction> select(Collection<Transaction> candidates, UTXOPool pool, int maxCount) {
        return selectInternal(candidates, pool, maxCount, Long.MAX_VALUE);
    }

    public List<Transaction> selectByVirtualSize(Collection<Transaction> candidates, UTXOPool pool, long maxBlockVBytes) {
        if (maxBlockVBytes <= 0) {
            throw new IllegalArgumentException("La capacidad del bloque debe ser positiva.");
        }
        return selectInternal(candidates, pool, Integer.MAX_VALUE, maxBlockVBytes);
    }

    private List<Transaction> selectInternal(Collection<Transaction> candidates,
                                             UTXOPool pool,
                                             int maxCount,
                                             long maxBlockVBytes) {
        List<Transaction> mempool = new ArrayList<>(candidates);
        Map<String, Transaction> byId = indexById(mempool);
        UTXOPool workingPool = new UTXOPool(pool);
        List<Transaction> selected = new ArrayList<>();
        Set<String> selectedIds = new HashSet<>();
        long usedVBytes = 0L;

        while (selected.size() < maxCount && usedVBytes < maxBlockVBytes) {
            int remainingSlots = maxCount - selected.size();
            long remainingVBytes = maxBlockVBytes - usedVBytes;
            Optional<PackageCandidate> best = findBestPackage(mempool, byId, workingPool, selectedIds, remainingSlots, remainingVBytes);
            if (best.isEmpty()) {
                break;
            }

            TxValidator validator = new TxValidator(workingPool);
            for (Transaction tx : best.get().orderedTransactions()) {
                if (!selectedIds.contains(tx.id()) && validator.isValidTx(tx)) {
                    validator.applyTransaction(tx);
                    selected.add(tx);
                    selectedIds.add(tx.id());
                    usedVBytes += TransactionSizeEstimator.virtualSize(tx);
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
                                                       int remainingSlots,
                                                       long remainingVBytes) {
        List<PackageCandidate> packages = new ArrayList<>();
        for (Transaction tx : mempool) {
            if (selectedIds.contains(tx.id())) {
                continue;
            }
            PackageCandidate candidate = buildPackage(tx, byId, pool, selectedIds, remainingSlots, remainingVBytes);
            if (candidate.valid()) {
                packages.add(candidate);
            }
        }

        return packages.stream()
                .max(Comparator
                        .comparing(PackageCandidate::feeRate)
                        .thenComparingLong(PackageCandidate::totalFee)
                        .thenComparingInt(PackageCandidate::size));
    }

    private PackageCandidate buildPackage(Transaction target,
                                          Map<String, Transaction> byId,
                                          UTXOPool basePool,
                                          Set<String> selectedIds,
                                          int remainingSlots,
                                          long remainingVBytes) {
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
        long totalVBytes = 0L;
        for (Transaction tx : ordered.values()) {
            long txVBytes = TransactionSizeEstimator.virtualSize(tx);
            if (totalVBytes + txVBytes > remainingVBytes) {
                return PackageCandidate.invalid();
            }
            if (!validator.isValidTx(tx)) {
                return PackageCandidate.invalid();
            }
            long fee = FeeCalculator.fee(tx, validator.getUtxoPool());
            if (fee == Long.MIN_VALUE) {
                return PackageCandidate.invalid();
            }
            totalFee += fee;
            totalVBytes += txVBytes;
            validator.applyTransaction(tx);
        }
        return new PackageCandidate(new ArrayList<>(ordered.values()), totalFee, totalVBytes, true);
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
            return false;
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

    private record PackageCandidate(List<Transaction> orderedTransactions, long totalFee, long totalVBytes, boolean valid) {
        static PackageCandidate invalid() {
            return new PackageCandidate(List.of(), Long.MIN_VALUE, 1L, false);
        }

        int size() {
            return orderedTransactions.size();
        }

        FeeRate feeRate() {
            return new FeeRate(totalFee, Math.max(1L, totalVBytes));
        }
    }
}
