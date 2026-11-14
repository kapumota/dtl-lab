package dltlab.conformance.v2;

import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.ShardManager;
import dltlab.sharding.protocol.CommitAttemptJournal;
import dltlab.sharding.protocol.ProtocolContext;
import dltlab.sharding.protocol.ProtocolException;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.verification.LedgerValueOracle;
import dltlab.wallet.Wallet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Prueba G2-P0: clasificacion de limites, NO replay TLC. */
public final class V2BoundaryProjectionTest {
    private V2BoundaryProjectionTest() { }

    public static void main(String[] args) {
        successfulCommit();
        for (ProtocolContext.FailurePoint point : ProtocolContext.FailurePoint.values()) {
            injectedFailure(point);
        }
        insufficientSourceQuorum();
        System.out.println("PASS G2-P0: commit, cuatro rollbacks y rechazo no representable; TLC pendiente");
    }

    private static void successfulCommit() {
        var owner = new Wallet("g2-p0-commit");
        var source = new UTXO(new byte[]{91, 31, 1}, 0);
        var setup = new ShardManager.Setup(2, 4, 3, ProtocolContext.FailureInjector.none());
        setup.addInitialUtxo(0, source, new Transaction.Output(10_000L, owner.getPublicKey()));
        var manager = setup.start();
        var transfer = new CrossShardTransfer(0, 1, source, 4_000L,
                owner.getPublicKey()).sign(owner.getPrivateKey());
        manager.beginAtomicTransfer(transfer, 4);
        check(manager.deliverAtomicReceipt(transfer.id()), "La entrega debe completarse");
        CrossShardSession beforeSession = manager.getSession(transfer.id());
        var beforeLedger = LedgerValueOracle.capture(manager.getShards());
        var beforeObs = manager.receiptObservations();
        check(manager.commitAtomicTransfer(transfer.id()), "El commit debe completarse");
        var afterSession = manager.getSession(transfer.id());
        var afterLedger = LedgerValueOracle.capture(manager.getShards());
        var afterObs = manager.receiptObservations();
        var audit = manager.commitAttemptEvents();
        var input = new V2BoundaryProjection.Input(beforeSession, afterSession,
                beforeLedger, afterLedger, audit, beforeObs, afterObs,
                manager.getQuorum(), manager.auditFailureCount());
        var result = V2BoundaryProjection.classify(input);
        check(result.verdict() == V2BoundaryProjection.Verdict.REPLAY_PENDIENTE_TLC,
                "Commit candidato a TLC: " + result.explanation());
        check(result.candidateActions().size() == 4 + afterSession.targetApprovals(),
                "Cantidad incorrecta de acciones candidatas");
        check(result.candidateActions().get(0).equals("Prepare(t, 1)"), "Debe iniciar Prepare");
        check(result.candidateActions().contains("DecideCommit(t)"), "Falta decision");
        check(result.candidateActions().get(result.candidateActions().size() - 1)
                .equals("ApplyDst(t)"), "Debe terminar ApplyDst");
        check(result.diagnosticObservationsChanged(), "Observaciones deben evolucionar");
        System.out.println("PASS G2-P0: commit normal => candidato a replay TLC");

        // Mutacion adversarial: cambia propietario sin cambiar importe ni total.
        Map<Integer, Map<String, LedgerValueOracle.OutputState>> tampered = new LinkedHashMap<>();
        for (var entry : afterLedger.utxos().entrySet()) tampered.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
        String targetKey = tampered.get(1).keySet().iterator().next();
        var previous = tampered.get(1).get(targetKey);
        tampered.get(1).put(targetKey,
                new LedgerValueOracle.OutputState(previous.amount(), "ZmFsc28="));
        var fakeAfter = new LedgerValueOracle.Snapshot(tampered,
                afterLedger.locks(), afterLedger.consumedReceipts(), afterLedger.totalValue());
        var alteredJournal = new ArrayList<>(audit);
        int lastIndex = alteredJournal.size() - 1;
        var finalEntry = alteredJournal.get(lastIndex);
        alteredJournal.set(lastIndex, new CommitAttemptJournal.Entry(
                finalEntry.sequence(), finalEntry.attemptId(), finalEntry.transferId(),
                finalEntry.logicalTime(), finalEntry.stage(), finalEntry.sessionStatus(),
                finalEntry.sessionVersion(), fakeAfter, finalEntry.detail()));
        var altered = V2BoundaryProjection.classify(new V2BoundaryProjection.Input(
                beforeSession, afterSession, beforeLedger, fakeAfter, alteredJournal,
                beforeObs, afterObs, manager.getQuorum(), 0));
        check(altered.verdict() == V2BoundaryProjection.Verdict.DIVERGENTE,
                "Debe detectar propietario alterado aunque conserve valor");
        var unreliable = V2BoundaryProjection.classify(new V2BoundaryProjection.Input(
                beforeSession, afterSession, beforeLedger, afterLedger, audit,
                beforeObs, afterObs, manager.getQuorum(), 1));
        check(unreliable.verdict() == V2BoundaryProjection.Verdict.NO_EVALUADO,
                "Un fallo del journal invalida la clasificacion");
        System.out.println("PASS G2-P0: mutacion de destinatario y fallo de auditoria detectados");
    }

    private static void injectedFailure(ProtocolContext.FailurePoint target) {
        var owner = new Wallet("g2-p0-" + target.name());
        var source = new UTXO(new byte[]{92, (byte) target.ordinal(), 1}, 0);
        var injector = (ProtocolContext.FailureInjector) point -> {
            if (point == target) throw new IllegalStateException("Fallo deliberado: " + target);
        };
        var setup = new ShardManager.Setup(2, 4, 3, injector);
        setup.addInitialUtxo(0, source, new Transaction.Output(10_000L, owner.getPublicKey()));
        var manager = setup.start();
        var transfer = new CrossShardTransfer(0, 1, source, 4_000L,
                owner.getPublicKey()).sign(owner.getPrivateKey());
        manager.beginAtomicTransfer(transfer, 4);
        check(manager.deliverAtomicReceipt(transfer.id()), "La entrega debe completarse");
        var beforeSession = manager.getSession(transfer.id());
        var beforeLedger = LedgerValueOracle.capture(manager.getShards());
        var beforeObs = manager.receiptObservations();
        try {
            manager.commitAtomicTransfer(transfer.id());
            throw new AssertionError("El punto de falla no interrumpio commit: " + target);
        } catch (ProtocolException expected) {
            check(expected.rolledBack(), "Rollback fallido en: " + target);
        }
        var result = V2BoundaryProjection.classify(new V2BoundaryProjection.Input(
                beforeSession, manager.getSession(transfer.id()),
                beforeLedger, LedgerValueOracle.capture(manager.getShards()),
                manager.commitAttemptEvents(), beforeObs, manager.receiptObservations(),
                manager.getQuorum(), manager.auditFailureCount()));
        check(result.verdict() == V2BoundaryProjection.Verdict.STUTTER_ESTADO_NEGOCIO,
                "Rollback debe ser stutter de negocio: " + target + " : " + result.explanation());
        check(result.businessRestored(), "El estado de negocio debe restaurarse");
        check(result.diagnosticObservationsChanged(),
                "Las observaciones de tentativa deben permanecer aunque el negocio se restaure");
        System.out.println("PASS G2-P0: " + target + " => stutter solo de negocio; diagnostico cambia");
    }

    private static void insufficientSourceQuorum() {
        var owner = new Wallet("g2-p0-noquorum");
        var source = new UTXO(new byte[]{93, 31, 1}, 0);
        var setup = new ShardManager.Setup(2, 4, 3, ProtocolContext.FailureInjector.none());
        setup.addInitialUtxo(0, source, new Transaction.Output(10_000L, owner.getPublicKey()));
        var manager = setup.start();
        manager.setShardOnline(0, false);
        var transfer = new CrossShardTransfer(0, 1, source, 4_000L,
                owner.getPublicKey()).sign(owner.getPrivateKey());
        CrossShardSession session = manager.beginAtomicTransfer(transfer, 4);
        var ledger = LedgerValueOracle.capture(manager.getShards());
        var obs = manager.receiptObservations();
        var result = V2BoundaryProjection.classify(new V2BoundaryProjection.Input(
                session, manager.getSession(transfer.id()), ledger, ledger,
                manager.commitAttemptEvents(), obs, obs, manager.getQuorum(), manager.auditFailureCount()));
        check(result.verdict() == V2BoundaryProjection.Verdict.NO_REPRESENTABLE,
                "No debe inventar Lock para quorum insuficiente");
        System.out.println("PASS G2-P0: rechazo previo al bloqueo => no representable en el piloto");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
