package dltlab.conformance.v2;

import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.protocol.CommitAttemptJournal;
import dltlab.verification.LedgerValueOracle;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Auditoria independiente de los estados intermedios que G1 efectivamente observa.
 * NO construye acciones V2, no prueba refinamiento y no atribuye una decision
 * irrevocable al estado interno de Java.
 */
public final class V2IntermediateAudit {
    private V2IntermediateAudit() { }

    public enum Verdict { TRACE_COMMIT_VALIDADA, TRACE_ROLLBACK_VALIDADA, DIVERGENTE, NO_EVALUABLE }

    public record Input(
            String transferId, String receiptId, String sourceUtxoKey,
            String recipientEncoded, int sourceShard, int targetShard,
            long amount, long auditFailures, List<CommitAttemptJournal.Entry> journal
    ) {
        public Input {
            if (transferId == null || transferId.isBlank() || receiptId == null || receiptId.isBlank()
                    || sourceUtxoKey == null || sourceUtxoKey.isBlank()
                    || recipientEncoded == null || recipientEncoded.isBlank()) {
                throw new IllegalArgumentException("Faltan identificadores de transferencia/recibo/UTXO/destinatario.");
            }
            if (sourceShard == targetShard || amount <= 0 || auditFailures < 0) {
                throw new IllegalArgumentException("Parametros de transferencia o auditoria invalidos.");
            }
            journal = List.copyOf(Objects.requireNonNull(journal, "Journal obligatorio."));
        }
    }

    public record Result(Verdict verdict, String detail, String failurePoint,
                         boolean partialApplicationSeen, boolean durableDecisionWitnessed,
                         List<CommitAttemptJournal.Stage> stages) {
        public Result {
            Objects.requireNonNull(verdict);
            Objects.requireNonNull(detail);
            stages = List.copyOf(stages);
        }
    }

    public static Result evaluate(Input input) {
        Objects.requireNonNull(input);
        if (input.auditFailures() != 0) {
            return result(Verdict.NO_EVALUABLE, "La instrumentacion G1 registro fallos.", null, false, List.of());
        }
        List<CommitAttemptJournal.Entry> all = input.journal();
        String attemptId = null;
        for (int i = all.size() - 1; i >= 0; i--) {
            if (input.transferId().equals(all.get(i).transferId())) {
                attemptId = all.get(i).attemptId();
                break;
            }
        }
        if (attemptId == null) {
            return result(Verdict.NO_EVALUABLE, "No hay tentativa observada para la transferencia.", null, false, List.of());
        }
        List<CommitAttemptJournal.Entry> trace = new ArrayList<>();
        for (var entry : all) {
            if (attemptId.equals(entry.attemptId())) {
                if (!input.transferId().equals(entry.transferId())) {
                    return result(Verdict.DIVERGENTE, "Una tentativa mezcla transferencias.", null, false, List.of());
                }
                trace.add(entry);
            }
        }
        var stages = trace.stream().map(CommitAttemptJournal.Entry::stage).toList();
        if (trace.isEmpty() || trace.get(0).stage() != CommitAttemptJournal.Stage.BEGIN) {
            return result(Verdict.DIVERGENTE, "Falta el inicio de la tentativa.", null, false, stages);
        }
        long lastSequence = -1;
        long lastTime = -1;
        long beginningVersion = trace.get(0).sessionVersion();
        for (var entry : trace) {
            CrossShardStatus expectedStatus = switch (entry.stage()) {
                case BEGIN, ROLLBACK_COMPLETED -> CrossShardStatus.RECEIPT_DELIVERED;
                case COMMIT_COMPLETED -> CrossShardStatus.COMMITTED;
                default -> CrossShardStatus.DESTINATION_PREPARED;
            };
            if (entry.sessionStatus() != expectedStatus) {
                return result(Verdict.DIVERGENTE, "Estado de sesion incompatible con etapa " + entry.stage(),
                        null, false, stages);
            }
            if (entry.stage() == CommitAttemptJournal.Stage.ROLLBACK_COMPLETED
                    && entry.sessionVersion() != beginningVersion) {
                return result(Verdict.DIVERGENTE, "La version funcional no regreso al checkpoint.",
                        null, false, stages);
            }
            if (entry.sequence() <= lastSequence || entry.logicalTime() < lastTime) {
                return result(Verdict.DIVERGENTE, "Secuencia o reloj diagnostico no monotono.", null, false, stages);
            }
            lastSequence = entry.sequence();
            lastTime = entry.logicalTime();
        }

        String failure = null;
        List<String> hooks = new ArrayList<>();
        for (var entry : trace) {
            if (entry.stage() == CommitAttemptJournal.Stage.FAILURE_POINT_REACHED) {
                hooks.add(entry.detail());
            }
        }
        boolean committed = stages.get(stages.size() - 1) == CommitAttemptJournal.Stage.COMMIT_COMPLETED;
        boolean rolledBack = stages.get(stages.size() - 1) == CommitAttemptJournal.Stage.ROLLBACK_COMPLETED;
        if (!committed && !rolledBack) {
            return result(Verdict.DIVERGENTE,
                    "Tentativa sin cierre correcto, o rollback fallido/incompleto.", null, false, stages);
        }
        List<String> allHooks = List.of("AFTER_RECEIPT_CONSUMED", "AFTER_SOURCE_DEBIT",
                "DURING_TARGET_CREDIT", "AFTER_TARGET_CREDIT");
        List<String> expectedHooks;
        if (committed) {
            expectedHooks = allHooks;
        } else {
            if (hooks.isEmpty() || !allHooks.contains(hooks.get(hooks.size() - 1))) {
                return result(Verdict.DIVERGENTE, "Falta el punto de fallo registrado.", null, false, stages);
            }
            failure = hooks.get(hooks.size() - 1);
            expectedHooks = allHooks.subList(0, allHooks.indexOf(failure) + 1);
        }
        if (!hooks.equals(expectedHooks)) {
            return result(Verdict.DIVERGENTE, "Orden o identidad de hooks incorrecto: " + hooks,
                    failure, false, stages);
        }
        List<CommitAttemptJournal.Stage> prefix = new ArrayList<>(List.of(
                CommitAttemptJournal.Stage.BEGIN,
                CommitAttemptJournal.Stage.PREPARED,
                CommitAttemptJournal.Stage.RECEIPT_CONSUMED,
                CommitAttemptJournal.Stage.FAILURE_POINT_REACHED));
        if (committed || !"AFTER_RECEIPT_CONSUMED".equals(failure)) {
            prefix.add(CommitAttemptJournal.Stage.SOURCE_DEBITED);
            prefix.add(CommitAttemptJournal.Stage.FAILURE_POINT_REACHED);
        }
        if (committed || (failure != null && ("DURING_TARGET_CREDIT".equals(failure)
                || "AFTER_TARGET_CREDIT".equals(failure)))) {
            prefix.add(CommitAttemptJournal.Stage.FAILURE_POINT_REACHED);
        }
        if (committed || "AFTER_TARGET_CREDIT".equals(failure)) {
            prefix.add(CommitAttemptJournal.Stage.DESTINATION_CREDITED);
            prefix.add(CommitAttemptJournal.Stage.FAILURE_POINT_REACHED);
        }
        if (committed) {
            prefix.add(CommitAttemptJournal.Stage.COMMIT_COMPLETED);
        } else {
            prefix.add(CommitAttemptJournal.Stage.ROLLBACK_STARTED);
            prefix.add(CommitAttemptJournal.Stage.ROLLBACK_COMPLETED);
        }
        if (!stages.equals(prefix)) {
            return result(Verdict.DIVERGENTE, "Secuencia de etapas incompleta, duplicada o fuera de orden.",
                    failure, false, stages);
        }

        try {
            var begin = trace.get(0).ledger();
            validateDomain(begin, input);
            LedgerValueOracle.Snapshot previous = begin;
            LedgerValueOracle.Snapshot afterReceipt = null;
            LedgerValueOracle.Snapshot afterSource = null;
            LedgerValueOracle.Snapshot afterTarget = null;
            boolean partial = false;
            for (int i = 1; i < trace.size(); i++) {
                var entry = trace.get(i);
                var snap = entry.ledger();
                validateDomain(snap, input);
                switch (entry.stage()) {
                    case PREPARED -> require(previous.equals(snap),
                            "Preparacion altero el ledger antes de consumir el recibo.");
                    case RECEIPT_CONSUMED -> {
                        checkReceipt(previous, snap, input);
                        afterReceipt = snap;
                    }
                    case SOURCE_DEBITED -> {
                        require(afterReceipt != null, "Debito sin consumo previo del recibo.");
                        checkSource(previous, snap, input);
                        afterSource = snap;
                        partial = true;
                    }
                    case DESTINATION_CREDITED -> {
                        require(afterSource != null, "Credito sin debito de origen.");
                        checkDestination(previous, snap, input);
                        afterTarget = snap;
                    }
                    case COMMIT_COMPLETED -> {
                        require(afterTarget != null && previous.equals(snap),
                                "Commit completo sin credito o con efectos adicionales.");
                        require(begin.totalValue().equals(snap.totalValue()),
                                "El commit no conserva el valor total.");
                    }
                    case FAILURE_POINT_REACHED, ROLLBACK_STARTED ->
                            require(previous.equals(snap), "Cambio de ledger no registrado en la etapa " + entry.stage());
                    case ROLLBACK_COMPLETED -> require(begin.equals(snap),
                            "El rollback no restauro exactamente UTXO, bloqueos y recibos.");
                    case ROLLBACK_FAILED -> throw new ValidationFailure("El rollback registro fallo.");
                    case BEGIN -> throw new ValidationFailure("Inicio de tentativa duplicado.");
                }
                previous = snap;
            }
            String note = committed
                    ? "Efectos observados coherentes; no hay testigo Java de DecideCommit irrevocable previo al debito."
                    : "Rollback restaurado; efectos parciales permanecen en el diagnostico, no en el ledger final.";
            return result(committed ? Verdict.TRACE_COMMIT_VALIDADA : Verdict.TRACE_ROLLBACK_VALIDADA,
                    note, failure, partial, stages);
        } catch (RuntimeException error) {
            return result(Verdict.DIVERGENTE, error.getMessage() == null ? error.toString() : error.getMessage(),
                    failure, false, stages);
        }
    }

    private static void validateDomain(LedgerValueOracle.Snapshot snapshot, Input input) {
        require(snapshot.utxos().containsKey(input.sourceShard())
                        && snapshot.utxos().containsKey(input.targetShard()),
                "Faltan shards de origen/destino en el snapshot.");
        require(snapshot.utxos().keySet().equals(snapshot.locks().keySet())
                        && snapshot.utxos().keySet().equals(snapshot.consumedReceipts().keySet()),
                "Los shards no coinciden entre estructuras del ledger.");
    }

    private static void checkReceipt(LedgerValueOracle.Snapshot before,
                                     LedgerValueOracle.Snapshot after, Input input) {
        require(before.utxos().equals(after.utxos()) && before.locks().equals(after.locks()),
                "El consumo del recibo modifico UTXO o bloqueos.");
        require(!before.consumedReceipts().get(input.targetShard()).contains(input.receiptId()),
                "El recibo ya estaba consumido antes de la etapa.");
        var expected = new LinkedHashMap<Integer, Set<String>>(before.consumedReceipts());
        var changed = new HashSet<>(expected.get(input.targetShard()));
        changed.add(input.receiptId());
        expected.put(input.targetShard(), Set.copyOf(changed));
        require(expected.equals(after.consumedReceipts()),
                "Consumo de recibo no localizado exclusivamente en destino.");
    }

    private static void checkSource(LedgerValueOracle.Snapshot before,
                                    LedgerValueOracle.Snapshot after, Input input) {
        require(before.consumedReceipts().equals(after.consumedReceipts()),
                "Debito modifico recibos consumidos.");
        var expectedLocks = new LinkedHashMap<Integer, Set<String>>(before.locks());
        var sourceLocks = new HashSet<>(expectedLocks.get(input.sourceShard()));
        require(sourceLocks.remove(input.sourceUtxoKey()), "El UTXO origen no estaba bloqueado.");
        expectedLocks.put(input.sourceShard(), Set.copyOf(sourceLocks));
        require(expectedLocks.equals(after.locks()), "Debito modifico bloqueos distintos del origen.");
        var prior = before.utxos().get(input.sourceShard());
        var post = after.utxos().get(input.sourceShard());
        var original = prior.get(input.sourceUtxoKey());
        require(original != null && original.amount() >= input.amount(), "UTXO origen invalido o sin fondos.");
        var expectedOld = new LinkedHashMap<>(prior);
        expectedOld.remove(input.sourceUtxoKey());
        require(post.entrySet().containsAll(expectedOld.entrySet())
                        && !post.containsKey(input.sourceUtxoKey()),
                "Debito modifico un UTXO ajeno o mantuvo el UTXO consumido.");
        var additions = new LinkedHashMap<>(post);
        expectedOld.keySet().forEach(additions::remove);
        long change = Math.subtractExact(original.amount(), input.amount());
        require(additions.size() == (change == 0 ? 0 : 1), "Cantidad incorrecta de UTXO de cambio.");
        if (change > 0) {
            var output = additions.values().iterator().next();
            require(output.amount() == change
                            && output.recipientEncoded().equals(original.recipientEncoded()),
                    "Importe o propietario incorrecto del UTXO de cambio.");
        }
        for (int shard : before.utxos().keySet()) {
            if (shard != input.sourceShard()) {
                require(before.utxos().get(shard).equals(after.utxos().get(shard)),
                        "El debito modifico otro shard.");
            }
        }
        BigInteger minusAmount = BigInteger.valueOf(input.amount()).negate();
        var delta = LedgerValueOracle.compare(before, after);
        require(delta.shardDeltas().get(input.sourceShard()).equals(minusAmount)
                        && after.totalValue().subtract(before.totalValue()).equals(minusAmount),
                "El debito no refleja el importe neto, incluyendo el cambio.");
    }

    private static void checkDestination(LedgerValueOracle.Snapshot before,
                                         LedgerValueOracle.Snapshot after, Input input) {
        require(before.locks().equals(after.locks())
                        && before.consumedReceipts().equals(after.consumedReceipts()),
                "Credito modifico bloqueos o recibos.");
        for (int shard : before.utxos().keySet()) {
            if (shard != input.targetShard()) {
                require(before.utxos().get(shard).equals(after.utxos().get(shard)),
                        "Credito modifico otro shard.");
            }
        }
        var beforeDst = before.utxos().get(input.targetShard());
        var afterDst = after.utxos().get(input.targetShard());
        require(afterDst.entrySet().containsAll(beforeDst.entrySet()),
                "Credito altero UTXO existentes en destino.");
        var additions = new LinkedHashMap<>(afterDst);
        beforeDst.keySet().forEach(additions::remove);
        require(additions.size() == 1, "El credito no creo exactamente un UTXO.");
        var output = additions.values().iterator().next();
        require(output.amount() == input.amount()
                        && output.recipientEncoded().equals(input.recipientEncoded()),
                "Credito destino incorrecto (importe o destinatario).");
        BigInteger plusAmount = BigInteger.valueOf(input.amount());
        var delta = LedgerValueOracle.compare(before, after);
        require(delta.shardDeltas().get(input.targetShard()).equals(plusAmount)
                        && after.totalValue().subtract(before.totalValue()).equals(plusAmount),
                "Credito destino no suma el importe esperado.");
    }

    private static void require(boolean valid, String detail) {
        if (!valid) throw new ValidationFailure(detail);
    }

    private static Result result(Verdict verdict, String message, String failurePoint,
                                 boolean partial, List<CommitAttemptJournal.Stage> stages) {
        // El journal G1 no contiene una decision Java irrevocable previa al debito.
        return new Result(verdict, message, failurePoint, partial, false, stages);
    }

    private static final class ValidationFailure extends RuntimeException {
        ValidationFailure(String message) { super(message); }
    }
}
