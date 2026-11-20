package dltlab.verification;

import dltlab.sharding.Shard;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Oráculo de valor UTXO independiente de TLA+ y del cálculo totalValue() del pool.
 * Debe ejecutarse bajo exclusión mutua externa sobre todos los shards capturados.
 * El estado de la sesión Java se compara por separado.
 */
public final class LedgerValueOracle {
    private LedgerValueOracle() { }

    /** El destinatario forma parte de la identidad semántica de la salida. */
    public record OutputState(long amount, String recipientEncoded) {
        public OutputState {
            if (amount < 0) throw new IllegalArgumentException("Valor UTXO negativo");
            if (recipientEncoded == null || recipientEncoded.isBlank()) {
                throw new IllegalArgumentException("Destinatario UTXO ausente");
            }
        }
    }

    public record Snapshot(
            Map<Integer, Map<String, OutputState>> utxos,
            Map<Integer, Set<String>> locks,
            Map<Integer, Set<String>> consumedReceipts,
            BigInteger totalValue
    ) {
        public Snapshot {
            Objects.requireNonNull(utxos, "utxos");
            Objects.requireNonNull(locks, "locks");
            Objects.requireNonNull(consumedReceipts, "consumedReceipts");
            Objects.requireNonNull(totalValue, "totalValue");
            var copiedUtxos = new TreeMap<Integer, Map<String, OutputState>>();
            utxos.forEach((id, values) -> copiedUtxos.put(id, Map.copyOf(values)));
            utxos = Map.copyOf(copiedUtxos);
            var copiedLocks = new TreeMap<Integer, Set<String>>();
            locks.forEach((id, values) -> copiedLocks.put(id, Set.copyOf(values)));
            locks = Map.copyOf(copiedLocks);
            var copiedReceipts = new TreeMap<Integer, Set<String>>();
            consumedReceipts.forEach((id, values) -> copiedReceipts.put(id, Set.copyOf(values)));
            consumedReceipts = Map.copyOf(copiedReceipts);
            if (!utxos.keySet().equals(locks.keySet()) ||
                    !utxos.keySet().equals(consumedReceipts.keySet())) {
                throw new IllegalArgumentException("Snapshots con conjuntos de shards diferentes");
            }
            if (!aggregate(utxos).equals(totalValue)) {
                throw new IllegalArgumentException("El total declarado no coincide con los UTXO");
            }
        }
    }

    public record Comparison(
            boolean conserved,
            boolean restoredExactly,
            BigInteger beforeTotal,
            BigInteger afterTotal,
            Map<Integer, BigInteger> shardDeltas
    ) {
        public Comparison {
            shardDeltas = Map.copyOf(Objects.requireNonNull(shardDeltas));
        }
    }

    public static Snapshot capture(List<Shard> shards) {
        Objects.requireNonNull(shards, "shards");
        var outputs = new TreeMap<Integer, Map<String, OutputState>>();
        var locks = new TreeMap<Integer, Set<String>>();
        var receipts = new TreeMap<Integer, Set<String>>();
        for (Shard shard : shards) {
            Objects.requireNonNull(shard, "shard");
            int id = shard.getId();
            if (outputs.containsKey(id)) {
                throw new IllegalArgumentException("Shard duplicado: " + id);
            }
            var values = new LinkedHashMap<String, OutputState>();
            for (UTXO utxo : shard.getUtxoPool().getAllUTXO()) {
                Transaction.Output output = shard.getUtxoPool().getOutput(utxo);
                if (output == null) throw new IllegalStateException("UTXO sin salida: " + utxo.key());
                PublicKey recipient = Objects.requireNonNull(output.getRecipient(), "Destinatario ausente");
                byte[] bytes = Objects.requireNonNull(recipient.getEncoded(), "Clave no codificable");
                var item = new OutputState(output.getValue(), Base64.getEncoder().encodeToString(bytes));
                if (values.putIfAbsent(utxo.key(), item) != null) {
                    throw new IllegalStateException("Clave UTXO duplicada: " + utxo.key());
                }
            }
            outputs.put(id, Map.copyOf(values));
            locks.put(id, Set.copyOf(shard.getLockedUtxos()));
            receipts.put(id, Set.copyOf(shard.getConsumedReceipts()));
        }
        return new Snapshot(outputs, locks, receipts, aggregate(outputs));
    }

    public static Comparison compare(Snapshot before, Snapshot after) {
        Objects.requireNonNull(before, "before");
        Objects.requireNonNull(after, "after");
        if (!before.utxos().keySet().equals(after.utxos().keySet())) {
            throw new IllegalArgumentException("Conjuntos de shards distintos entre snapshots");
        }
        var deltas = new TreeMap<Integer, BigInteger>();
        for (int id : before.utxos().keySet()) {
            deltas.put(id, sum(after.utxos().get(id)).subtract(sum(before.utxos().get(id))));
        }
        boolean exactly = before.utxos().equals(after.utxos())
                && before.locks().equals(after.locks())
                && before.consumedReceipts().equals(after.consumedReceipts());
        return new Comparison(before.totalValue().equals(after.totalValue()), exactly,
                before.totalValue(), after.totalValue(), deltas);
    }

    private static BigInteger sum(Map<String, OutputState> values) {
        BigInteger total = BigInteger.ZERO;
        for (OutputState state : values.values()) {
            total = total.add(BigInteger.valueOf(state.amount()));
        }
        return total;
    }

    private static BigInteger aggregate(Map<Integer, Map<String, OutputState>> values) {
        BigInteger total = BigInteger.ZERO;
        for (var shardValues : values.values()) total = total.add(sum(shardValues));
        return total;
    }

    public static void requireConservation(Snapshot before, Snapshot after) {
        Comparison result = compare(before, after);
        if (!result.conserved()) {
            throw new AssertionError("Conservación violada: antes=" + result.beforeTotal()
                    + ", después=" + result.afterTotal() + ", deltas=" + result.shardDeltas());
        }
    }

    /** Compara solamente el ledger observable; no sustituye un checkpoint de sesión. */
    public static void requireExactRollback(Snapshot before, Snapshot after) {
        Comparison result = compare(before, after);
        if (!result.restoredExactly()) {
            throw new AssertionError("Rollback incompleto: UTXO/propietarios/bloqueos/recibos no restaurados");
        }
    }
}
