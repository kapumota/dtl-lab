package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.ShardManager;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.verification.LedgerValueOracle;
import dltlab.wallet.Wallet;

import java.util.List;

/** Integracion G1: commit, cuatro fallos y estado parcial registrado. */
public final class CommitAuditIntegrationTest {
    private CommitAuditIntegrationTest() { }

    public static void main(String[] args) {
        verifySuccessfulCommit();
        for (ProtocolContext.FailurePoint point : ProtocolContext.FailurePoint.values()) {
            verifyInjectedRollback(point);
        }
        System.out.println("PASS: G1 integracion real de commit y rollback (5 escenarios)");
    }

    private static void verifySuccessfulCommit() {
        Wallet owner = new Wallet("g1-success");
        UTXO source = new UTXO(new byte[]{111, 9, 1}, 0);
        var setup = new ShardManager.Setup(2, 4, 3, ProtocolContext.FailureInjector.none());
        setup.addInitialUtxo(0, source,
                new Transaction.Output(10_000L, owner.getPublicKey()));
        ShardManager manager = setup.start();
        var transfer = new CrossShardTransfer(0, 1, source, 4_000L,
                owner.getPublicKey()).sign(owner.getPrivateKey());
        manager.beginAtomicTransfer(transfer, 4);
        check(manager.deliverAtomicReceipt(transfer.id()), "Debe entregarse recibo");
        var before = LedgerValueOracle.capture(manager.getShards());
        check(manager.commitAtomicTransfer(transfer.id()), "Commit debe completar");
        var after = LedgerValueOracle.capture(manager.getShards());

        LedgerValueOracle.requireConservation(before, after);
        var compare = LedgerValueOracle.compare(before, after);
        check(compare.shardDeltas().get(0).longValueExact() == -4_000L,
                "Variacion origen incorrecta");
        check(compare.shardDeltas().get(1).longValueExact() == 4_000L,
                "Variacion destino incorrecta");
        check(after.utxos().get(0).size() == 1,
                "Debe quedar una salida de cambio en el origen");
        check(after.utxos().get(1).size() == 1,
                "Debe existir una salida destino");

        var audit = manager.commitAttemptEvents();
        assertStages(audit, List.of(
                CommitAttemptJournal.Stage.BEGIN,
                CommitAttemptJournal.Stage.PREPARED,
                CommitAttemptJournal.Stage.RECEIPT_CONSUMED,
                CommitAttemptJournal.Stage.SOURCE_DEBITED,
                CommitAttemptJournal.Stage.FAILURE_POINT_REACHED,
                CommitAttemptJournal.Stage.FAILURE_POINT_REACHED,
                CommitAttemptJournal.Stage.DESTINATION_CREDITED,
                CommitAttemptJournal.Stage.FAILURE_POINT_REACHED,
                CommitAttemptJournal.Stage.COMMIT_COMPLETED
        ), false);
        // Cada uno de los cuatro puntos de falla se ejecuta tambien en el caso valido.
        check(audit.stream().filter(e -> e.stage()
                == CommitAttemptJournal.Stage.FAILURE_POINT_REACHED).count() == 4,
                "Cuatro puntos de falla observables");
        var partial = entryOf(audit, CommitAttemptJournal.Stage.SOURCE_DEBITED);
        check(partial.ledger().totalValue().longValueExact() == 6_000L,
                "El estado intermedio debe mostrar debito sin credito de destino");
        var credited = entryOf(audit, CommitAttemptJournal.Stage.DESTINATION_CREDITED);
        check(credited.ledger().totalValue().longValueExact() == 10_000L,
                "Credito debe restaurar la conservacion del valor");
        check(manager.auditFailureCount() == 0L, "No debe fallar la instrumentacion");
    }

    private static void verifyInjectedRollback(ProtocolContext.FailurePoint selected) {
        Wallet owner = new Wallet("g1-failure-" + selected.name());
        UTXO source = new UTXO(new byte[]{(byte) (120 + selected.ordinal()), 9, 2}, 0);
        ProtocolContext.FailureInjector injector = point -> {
            if (point == selected) {
                throw new IllegalStateException("Fallo controlado en " + point.name());
            }
        };
        var setup = new ShardManager.Setup(2, 4, 3, injector);
        setup.addInitialUtxo(0, source,
                new Transaction.Output(10_000L, owner.getPublicKey()));
        ShardManager manager = setup.start();
        var transfer = new CrossShardTransfer(0, 1, source, 4_000L,
                owner.getPublicKey()).sign(owner.getPrivateKey());
        CrossShardSession started = manager.beginAtomicTransfer(transfer, 4);
        check(manager.deliverAtomicReceipt(transfer.id()), "Debe entregarse recibo");
        var beforeHistory = manager.getSession(transfer.id()).events();
        var before = LedgerValueOracle.capture(manager.getShards());
        try {
            manager.commitAtomicTransfer(transfer.id());
            throw new AssertionError("Fallo inyectado no detectado: " + selected);
        } catch (ProtocolException expected) {
            check(expected.rolledBack(), "Rollback debe restaurar: " + selected);
        }
        var after = LedgerValueOracle.capture(manager.getShards());
        LedgerValueOracle.requireConservation(before, after);
        LedgerValueOracle.requireExactRollback(before, after);
        check(manager.getSession(transfer.id()).status() == CrossShardStatus.RECEIPT_DELIVERED,
                "Debe restaurarse el estado de sesion");
        check(manager.getSession(transfer.id()).events().equals(beforeHistory),
                "La historia funcional se restaura desde checkpoint");

        var audit = manager.commitAttemptEvents();
        check(!audit.isEmpty(), "Debe existir evidencia del intento fallido");
        var first = audit.get(0);
        check(first.stage() == CommitAttemptJournal.Stage.BEGIN,
                "Debe haber evento inicial");
        check(first.ledger().equals(before), "Snapshot inicial inexacto");
        check(audit.stream().anyMatch(e -> e.stage()
                == CommitAttemptJournal.Stage.FAILURE_POINT_REACHED
                && e.detail().equals(selected.name())),
                "Punto de falla no registrado: " + selected);
        var last = audit.get(audit.size() - 1);
        check(last.stage() == CommitAttemptJournal.Stage.ROLLBACK_COMPLETED,
                "Debe registrarse rollback completo");
        check(last.ledger().equals(before), "Snapshot final debe coincidir exactamente");
        check(last.sessionStatus() == CrossShardStatus.RECEIPT_DELIVERED,
                "El journal conserva el estado posterior a restauracion");
        check(audit.stream().noneMatch(e -> e.stage()
                == CommitAttemptJournal.Stage.COMMIT_COMPLETED),
                "No debe declararse commit si hubo rollback");
        check(manager.auditFailureCount() == 0L, "No debe fallar la instrumentacion");
        check(started.receipt().transferId().equals(transfer.id()),
                "Consistencia de sesion");

        if (selected == ProtocolContext.FailurePoint.AFTER_SOURCE_DEBIT
                || selected == ProtocolContext.FailurePoint.DURING_TARGET_CREDIT
                || selected == ProtocolContext.FailurePoint.AFTER_TARGET_CREDIT) {
            check(entryOf(audit, CommitAttemptJournal.Stage.SOURCE_DEBITED)
                            .ledger().totalValue().longValueExact() == 6_000L,
                    "Debe observarse el estado parcial de origen");
        }
        if (selected == ProtocolContext.FailurePoint.AFTER_TARGET_CREDIT) {
            check(entryOf(audit, CommitAttemptJournal.Stage.DESTINATION_CREDITED)
                            .ledger().totalValue().longValueExact() == 10_000L,
                    "Antes de rollback, el credito debe estar aplicado");
        }
    }

    private static CommitAttemptJournal.Entry entryOf(
            List<CommitAttemptJournal.Entry> audit, CommitAttemptJournal.Stage stage) {
        return audit.stream().filter(e -> e.stage() == stage).findFirst()
                .orElseThrow(() -> new AssertionError("Falta etapa: " + stage));
    }

    private static void assertStages(List<CommitAttemptJournal.Entry> audit,
                                     List<CommitAttemptJournal.Stage> expected,
                                     boolean strict) {
        check(!audit.isEmpty(), "Journal vacio");
        for (int index = 0; index < audit.size(); index++) {
            check(audit.get(index).sequence() == index, "Secuencia no contigua");
        }
        if (strict) {
            check(audit.stream().map(CommitAttemptJournal.Entry::stage).toList()
                    .equals(expected), "Orden de etapas inesperado");
        } else {
            for (CommitAttemptJournal.Stage required : expected) {
                check(audit.stream().anyMatch(e -> e.stage() == required),
                        "Falta etapa esperada: " + required);
            }
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
