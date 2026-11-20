package dltlab.conformance.v2;

import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.protocol.CommitAttemptJournal;
import dltlab.sharding.protocol.ReceiptObservation;
import dltlab.verification.LedgerValueOracle;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Proyeccion preliminar de limites de operacion Java hacia el modelo TLA+ V2.2.
 * NO ejecuta TLC y NO declara conformidad. No modifica los mapeadores V1.
 *
 * La proyeccion de negocio excluye deliberadamente el journal y las
 * observaciones append-only. Se conserva un indicador de su variacion.
 */
public final class V2BoundaryProjection {
    private V2BoundaryProjection() { }

    public enum Verdict {
        REPLAY_PENDIENTE_TLC,
        STUTTER_ESTADO_NEGOCIO,
        DIVERGENTE,
        NO_REPRESENTABLE,
        NO_EVALUADO
    }

    public record Result(
            Verdict verdict,
            String explanation,
            List<String> candidateActions,
            Map<Integer, BigInteger> deltas,
            boolean businessRestored,
            boolean diagnosticObservationsChanged
    ) {
        public Result {
            Objects.requireNonNull(verdict);
            Objects.requireNonNull(explanation);
            candidateActions = List.copyOf(candidateActions);
            deltas = Map.copyOf(deltas);
        }
    }

    /** Entrada explicitamente tipada: evita inferir eventos de cadenas o textos de log. */
    public record Input(
            CrossShardSession beforeSession,
            CrossShardSession afterSession,
            LedgerValueOracle.Snapshot beforeLedger,
            LedgerValueOracle.Snapshot afterLedger,
            List<CommitAttemptJournal.Entry> journal,
            List<ReceiptObservation> observationsBefore,
            List<ReceiptObservation> observationsAfter,
            int quorum,
            long auditFailures
    ) {
        public Input {
            Objects.requireNonNull(beforeSession);
            Objects.requireNonNull(afterSession);
            Objects.requireNonNull(beforeLedger);
            Objects.requireNonNull(afterLedger);
            journal = List.copyOf(Objects.requireNonNull(journal));
            observationsBefore = List.copyOf(Objects.requireNonNull(observationsBefore));
            observationsAfter = List.copyOf(Objects.requireNonNull(observationsAfter));
            if (quorum < 1 || auditFailures < 0) throw new IllegalArgumentException("Parametros G2 invalidos");
        }
    }

    public static Result classify(Input input) {
        Objects.requireNonNull(input);
        var before = input.beforeSession();
        var after = input.afterSession();
        boolean obsChanged = !input.observationsBefore().equals(input.observationsAfter());
        boolean sessionRestored = sameFunctionalSession(before, after);
        LedgerValueOracle.Comparison diff;
        try {
            diff = LedgerValueOracle.compare(input.beforeLedger(), input.afterLedger());
        } catch (RuntimeException error) {
            return answer(Verdict.DIVERGENTE, "Snapshots de negocio incompatibles: " + error.getMessage(),
                    List.of(), Map.of(), false, obsChanged);
        }
        boolean businessRestored = diff.restoredExactly() && sessionRestored;
        Map<Integer, BigInteger> deltas = diff.shardDeltas();
        if (input.auditFailures() != 0) {
            return answer(Verdict.NO_EVALUADO, "La instrumentacion G1 registro fallos; proyeccion no fiable.",
                    List.of(), deltas, businessRestored, obsChanged);
        }
        if (!before.transfer().id().equals(after.transfer().id())
                || !before.receipt().equals(after.receipt())) {
            return answer(Verdict.DIVERGENTE, "Identidad de sesion o recibo alterada.",
                    List.of(), deltas, businessRestored, obsChanged);
        }
        if (before.status() != CrossShardStatus.RECEIPT_DELIVERED) {
            return answer(Verdict.NO_REPRESENTABLE,
                    "Esta version piloto exige el limite previo RECEIPT_DELIVERED; no inventa acciones Lock/Prepare.",
                    List.of(), deltas, businessRestored, obsChanged);
        }
        List<CommitAttemptJournal.Entry> currentAttempt = latestAttempt(input.journal(), before.transfer().id());
        if (currentAttempt.isEmpty()) {
            return answer(Verdict.NO_EVALUADO, "No existen eventos G1 para esta tentativa.",
                    List.of(), deltas, businessRestored, obsChanged);
        }
        CommitAttemptJournal.Entry first = currentAttempt.get(0);
        CommitAttemptJournal.Entry last = currentAttempt.get(currentAttempt.size() - 1);
        if (first.stage() != CommitAttemptJournal.Stage.BEGIN
                || !first.ledger().equals(input.beforeLedger())) {
            return answer(Verdict.DIVERGENTE, "La evidencia inicial no coincide con el limite previo.",
                    List.of(), deltas, businessRestored, obsChanged);
        }
        if (!last.ledger().equals(input.afterLedger())) {
            return answer(Verdict.DIVERGENTE, "La evidencia final del journal no coincide con el limite posterior.",
                    List.of(), deltas, businessRestored, obsChanged);
        }
        if (after.status() == CrossShardStatus.COMMITTED) {
            String issue = verifyCommitted(input, currentAttempt, diff);
            if (issue != null) {
                return answer(Verdict.DIVERGENTE, issue, List.of(), deltas, businessRestored, obsChanged);
            }
            List<String> actions = new ArrayList<>();
            actions.add("Prepare(t, 1)");
            for (int i = 1; i <= after.targetApprovals(); i++) {
                actions.add("Vote(t, v" + i + ")");
            }
            actions.add("DecideCommit(t)");
            actions.add("ApplySrc(t)");
            actions.add("ApplyDst(t)");
            return answer(Verdict.REPLAY_PENDIENTE_TLC,
                    "Secuencia candidata contigua desde un estado V2 previamente Locked; requiere TLC y funcion de abstraccion total.",
                    actions, deltas, false, obsChanged);
        }
        if (last.stage() == CommitAttemptJournal.Stage.ROLLBACK_COMPLETED
                && businessRestored && ordered(currentAttempt, List.of(
                    CommitAttemptJournal.Stage.BEGIN,
                    CommitAttemptJournal.Stage.ROLLBACK_STARTED,
                    CommitAttemptJournal.Stage.ROLLBACK_COMPLETED
                ))) {
            return answer(Verdict.STUTTER_ESTADO_NEGOCIO,
                    "Stutter solo en la proyeccion UTXO/bloqueos/recibos/sesion; observaciones diagnosticas excluidas.",
                    List.of("Stutter"), deltas, true, obsChanged);
        }
        if (last.stage() == CommitAttemptJournal.Stage.ROLLBACK_FAILED
                || (last.stage() == CommitAttemptJournal.Stage.ROLLBACK_COMPLETED && !businessRestored)) {
            return answer(Verdict.DIVERGENTE,
                    "Rollback fallido o estado de negocio residual; prohibido proyectar como Stutter.",
                    List.of(), deltas, false, obsChanged);
        }
        return answer(Verdict.NO_REPRESENTABLE,
                "Resultado fuera del piloto: falta una accion abstracta justificada para el estado final " + after.status(),
                List.of(), deltas, businessRestored, obsChanged);
    }

    private static String verifyCommitted(Input input, List<CommitAttemptJournal.Entry> audit,
                                          LedgerValueOracle.Comparison delta) {
        var beforeSession = input.beforeSession();
        var afterSession = input.afterSession();
        var transfer = beforeSession.transfer();
        int src = transfer.sourceShardId();
        int dst = transfer.targetShardId();
        String sourceKey = transfer.sourceUtxo().key();
        String receiptId = beforeSession.receipt().receiptId();
        var pre = input.beforeLedger();
        var post = input.afterLedger();
        if (!delta.conserved()) return "Conservacion global violada.";
        if (afterSession.targetApprovals() < input.quorum()) return "Decision de commit sin quorum.";
        if (afterSession.events().size() <= beforeSession.events().size()) return "Falta transicion funcional de commit.";
        if (!delta.shardDeltas().getOrDefault(src, BigInteger.ZERO)
                .equals(BigInteger.valueOf(-transfer.amount()))
                || !delta.shardDeltas().getOrDefault(dst, BigInteger.ZERO)
                .equals(BigInteger.valueOf(transfer.amount()))) {
            return "El delta neto por shard no coincide con el importe de la transferencia.";
        }
        for (var entry : delta.shardDeltas().entrySet()) {
            if (entry.getKey() != src && entry.getKey() != dst && entry.getValue().signum() != 0) {
                return "Se modifico un tercer shard.";
            }
        }
        if (!pre.utxos().containsKey(src) || !pre.utxos().containsKey(dst)) {
            return "Faltan shards en el snapshot previo.";
        }
        var sourceBefore = pre.utxos().get(src);
        var sourceAfter = post.utxos().get(src);
        var targetBefore = pre.utxos().get(dst);
        var targetAfter = post.utxos().get(dst);
        var original = sourceBefore.get(sourceKey);
        if (original == null || sourceAfter.containsKey(sourceKey)) {
            return "El UTXO de origen no fue consumido como se esperaba.";
        }
        if (!pre.locks().get(src).contains(sourceKey) || post.locks().get(src).contains(sourceKey)) {
            return "Bloqueo UTXO no liberado correctamente.";
        }
        Set<String> expectedLocks = new HashSet<>(pre.locks().get(src));
        expectedLocks.remove(sourceKey);
        if (!expectedLocks.equals(post.locks().get(src))) return "Otros bloqueos de origen cambiaron.";
        if (pre.consumedReceipts().get(dst).contains(receiptId)
                || !post.consumedReceipts().get(dst).contains(receiptId)) {
            return "Consumo del recibo no comprobado.";
        }
        Set<String> expectedReceipts = new HashSet<>(pre.consumedReceipts().get(dst));
        expectedReceipts.add(receiptId);
        if (!expectedReceipts.equals(post.consumedReceipts().get(dst))) {
            return "Se modificaron otros recibos consumidos.";
        }
        Map<String, LedgerValueOracle.OutputState> unchangedSrc = new LinkedHashMap<>(sourceBefore);
        unchangedSrc.remove(sourceKey);
        Map<String, LedgerValueOracle.OutputState> newSrc = new LinkedHashMap<>(sourceAfter);
        unchangedSrc.keySet().forEach(newSrc::remove);
        Map<String, LedgerValueOracle.OutputState> unchangedDst = new LinkedHashMap<>(targetBefore);
        Map<String, LedgerValueOracle.OutputState> newDst = new LinkedHashMap<>(targetAfter);
        unchangedDst.keySet().forEach(newDst::remove);
        if (!sourceAfter.entrySet().containsAll(unchangedSrc.entrySet())
                || !targetAfter.entrySet().containsAll(targetBefore.entrySet())) {
            return "UTXO ajenos a la transferencia fueron alterados.";
        }
        long change;
        try {
            change = Math.subtractExact(original.amount(), transfer.amount());
        } catch (ArithmeticException error) {
            return "Importe de cambio fuera del dominio long.";
        }
        if (change < 0 || newSrc.size() != (change == 0 ? 0 : 1)) return "Cambio UTXO incorrecto.";
        if (change > 0) {
            var item = newSrc.values().iterator().next();
            if (item.amount() != change || !item.recipientEncoded().equals(original.recipientEncoded())) {
                return "El UTXO de cambio tiene valor o propietario incorrecto.";
            }
        }
        if (newDst.size() != 1) return "El nuevo UTXO destino no es unico.";
        var received = newDst.values().iterator().next();
        String recipient = Base64.getEncoder().encodeToString(beforeSession.receipt().recipient().getEncoded());
        if (received.amount() != transfer.amount() || !received.recipientEncoded().equals(recipient)) {
            return "Importe o destinatario del UTXO destino incorrecto.";
        }
        for (int shard : pre.utxos().keySet()) {
            if (shard != src && shard != dst && (!pre.utxos().get(shard).equals(post.utxos().get(shard))
                    || !pre.locks().get(shard).equals(post.locks().get(shard))
                    || !pre.consumedReceipts().get(shard).equals(post.consumedReceipts().get(shard)))) {
                return "Un shard ajeno fue modificado.";
            }
        }
        if (!pre.locks().get(dst).equals(post.locks().get(dst))
                || !pre.consumedReceipts().get(src).equals(post.consumedReceipts().get(src))) {
            return "Bloqueos o recibos de un shard equivocado cambiaron.";
        }
        if (!ordered(audit, List.of(
                CommitAttemptJournal.Stage.BEGIN,
                CommitAttemptJournal.Stage.PREPARED,
                CommitAttemptJournal.Stage.RECEIPT_CONSUMED,
                CommitAttemptJournal.Stage.SOURCE_DEBITED,
                CommitAttemptJournal.Stage.DESTINATION_CREDITED,
                CommitAttemptJournal.Stage.COMMIT_COMPLETED
        )) || audit.get(audit.size() - 1).stage() != CommitAttemptJournal.Stage.COMMIT_COMPLETED) {
            return "La secuencia de eventos del commit es incompleta o desordenada.";
        }
        return null;
    }

    private static boolean sameFunctionalSession(CrossShardSession a, CrossShardSession b) {
        return a.status() == b.status()
                && a.events().equals(b.events())
                && a.reason().equals(b.reason())
                && a.targetApprovals() == b.targetApprovals()
                && a.targetValidators() == b.targetValidators()
                && a.lastLogicalTime() == b.lastLogicalTime()
                && a.stateVersion() == b.stateVersion();
    }

    private static List<CommitAttemptJournal.Entry> latestAttempt(List<CommitAttemptJournal.Entry> entries,
                                                                    String transferId) {
        String attemptId = null;
        for (int i = entries.size() - 1; i >= 0; i--) {
            var item = entries.get(i);
            if (item.transferId().equals(transferId)) {
                attemptId = item.attemptId();
                break;
            }
        }
        if (attemptId == null) return List.of();
        String selected = attemptId;
        return entries.stream().filter(e -> e.transferId().equals(transferId)
                && e.attemptId().equals(selected)).toList();
    }

    private static boolean ordered(List<CommitAttemptJournal.Entry> audit,
                                   List<CommitAttemptJournal.Stage> required) {
        int last = -1;
        for (var stage : required) {
            int next = -1;
            for (int i = last + 1; i < audit.size(); i++) {
                if (audit.get(i).stage() == stage) { next = i; break; }
            }
            if (next < 0) return false;
            last = next;
        }
        return true;
    }

    private static Result answer(Verdict verdict, String explanation, List<String> actions,
                                 Map<Integer, BigInteger> deltas, boolean restored,
                                 boolean observationsChanged) {
        return new Result(verdict, explanation, actions, deltas, restored, observationsChanged);
    }
}
