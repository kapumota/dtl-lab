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
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Prueba integrada G2-P2 sobre el protocolo real, sin modificarlo. */
public final class V2IntermediateAuditTest {
    private V2IntermediateAuditTest() { }

    public static void main(String[] args) throws Exception {
        commitAndNegativeControls();
        for (ProtocolContext.FailurePoint point : ProtocolContext.FailurePoint.values()) {
            rollback(point);
        }
        contention(0);
        contention(1);
        System.out.println("PASS G2-P2: estados intermedios, negativos y contencion real (sin refinamiento demostrado)");
    }

    private static void commitAndNegativeControls() {
        var owner = new Wallet("g2-p2-commit");
        var source = new UTXO(new byte[]{112, 2, 0}, 0);
        var setup = new ShardManager.Setup(2, 4, 3);
        setup.addInitialUtxo(0, source, new Transaction.Output(10_000L, owner.getPublicKey()));
        var manager = setup.start();
        var transfer = new CrossShardTransfer(0, 1, source, 4_000L,
                owner.getPublicKey()).sign(owner.getPrivateKey());
        manager.beginAtomicTransfer(transfer, 4);
        require(manager.deliverAtomicReceipt(transfer.id()), "Recibo no entregado.");
        CrossShardSession session = manager.getSession(transfer.id());
        require(manager.commitAtomicTransfer(transfer.id()), "Commit normal rechazado.");
        var input = input(manager, session);
        var accepted = V2IntermediateAudit.evaluate(input);
        require(accepted.verdict() == V2IntermediateAudit.Verdict.TRACE_COMMIT_VALIDADA,
                "Commit no supero auditoria interna: " + accepted);
        require(!accepted.durableDecisionWitnessed(), "Se atribuyo falsamente una decision Java irrevocable.");
        System.out.println("PASS G2-P2: commit intermedio con consumo, cambio y credito correctos");

        var entries = new ArrayList<>(input.journal());
        entries.removeIf(e -> e.stage() == CommitAttemptJournal.Stage.SOURCE_DEBITED);
        reject(copy(input, 0, entries), "omision de debito");

        entries = new ArrayList<>(input.journal());
        int i = indexOf(entries, CommitAttemptJournal.Stage.FAILURE_POINT_REACHED);
        var old = entries.get(i);
        entries.set(i, replace(old, old.ledger(), "DURING_TARGET_CREDIT"));
        reject(copy(input, 0, entries), "hook fuera de orden");

        entries = new ArrayList<>(input.journal());
        int targetIndex = indexOf(entries, CommitAttemptJournal.Stage.DESTINATION_CREDITED);
        var dest = entries.get(targetIndex);
        var alteredUtxos = new LinkedHashMap<Integer, Map<String, LedgerValueOracle.OutputState>>();
        dest.ledger().utxos().forEach((shard, outputs) -> alteredUtxos.put(shard, new LinkedHashMap<>(outputs)));
        String createdKey = alteredUtxos.get(1).keySet().iterator().next();
        var original = alteredUtxos.get(1).get(createdKey);
        alteredUtxos.get(1).put(createdKey,
                new LedgerValueOracle.OutputState(original.amount(), "YWR2ZXJzYXJpby1mYWxzby1wcmludA=="));
        var alteredLedger = new LedgerValueOracle.Snapshot(alteredUtxos,
                dest.ledger().locks(), dest.ledger().consumedReceipts(), dest.ledger().totalValue());
        entries.set(targetIndex, replace(dest, alteredLedger, dest.detail()));
        reject(copy(input, 0, entries), "destinatario equivocado aunque se conserve el total");
        require(V2IntermediateAudit.evaluate(copy(input, 1, input.journal())).verdict()
                == V2IntermediateAudit.Verdict.NO_EVALUABLE, "No se detuvo ante fallo de auditoria.");
        System.out.println("PASS G2-P2: negativos, etapas omitidas, hooks y destinatario");
    }

    private static void rollback(ProtocolContext.FailurePoint point) {
        var owner = new Wallet("g2-p2-" + point);
        var source = new UTXO(new byte[]{112, 3, (byte) point.ordinal()}, 0);
        ProtocolContext.FailureInjector injector = p -> {
            if (p == point) throw new IllegalStateException("Fallo inyectado: " + point);
        };
        var setup = new ShardManager.Setup(2, 4, 3, injector);
        setup.addInitialUtxo(0, source, new Transaction.Output(10_000L, owner.getPublicKey()));
        var manager = setup.start();
        var transfer = new CrossShardTransfer(0, 1, source, 4_000L,
                owner.getPublicKey()).sign(owner.getPrivateKey());
        manager.beginAtomicTransfer(transfer, 4);
        require(manager.deliverAtomicReceipt(transfer.id()), "Recibo no entregado.");
        var session = manager.getSession(transfer.id());
        try {
            manager.commitAtomicTransfer(transfer.id());
            throw new AssertionError("No se activo el punto de fallo: " + point);
        } catch (ProtocolException expected) {
            require(expected.rolledBack(), "Rollback fallido: " + point);
        }
        var input = input(manager, session);
        var result = V2IntermediateAudit.evaluate(input);
        require(result.verdict() == V2IntermediateAudit.Verdict.TRACE_ROLLBACK_VALIDADA,
                "Rollback G2-P2 incorrecto: " + point + " => " + result);
        require(point.name().equals(result.failurePoint()), "Fallo no identificado en el journal.");
        require(result.partialApplicationSeen() == (point != ProtocolContext.FailurePoint.AFTER_RECEIPT_CONSUMED),
                "Aplicacion parcial debito-credito mal identificada.");
        System.out.println("PASS G2-P2: " + point + " => historial parcial y rollback exacto");

        if (point == ProtocolContext.FailurePoint.AFTER_SOURCE_DEBIT) {
            var entries = new ArrayList<>(input.journal());
            int index = indexOf(entries, CommitAttemptJournal.Stage.ROLLBACK_COMPLETED);
            var old = entries.get(index);
            var previous = entries.get(index - 1);
            entries.set(index, replace(old, previous.ledger(), old.detail()));
            reject(copy(input, 0, entries), "rollback residual alterado");
            System.out.println("PASS G2-P2: negativo rollback residual");
        }
    }

    /** Competencia real entre dos solicitudes, con barrera de inicio y acceso concurrente al gestor. */
    private static void contention(int variant) throws Exception {
        var owner = new Wallet("g2-p2-contention-" + variant);
        var source = new UTXO(new byte[]{112, 4, (byte) variant}, 0);
        var setup = new ShardManager.Setup(2, 4, 3);
        setup.addInitialUtxo(0, source, new Transaction.Output(10_000L, owner.getPublicKey()));
        var manager = setup.start();
        var before = LedgerValueOracle.capture(manager.getShards());
        var left = new CrossShardTransfer(0, 1, source, 4_000L,
                owner.getPublicKey()).sign(owner.getPrivateKey());
        var right = new CrossShardTransfer(0, 1, source, 3_000L,
                owner.getPublicKey()).sign(owner.getPrivateKey());
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> a = executor.submit(() -> beginCompeting(manager, left, ready, start));
            Future<Boolean> b = executor.submit(() -> beginCompeting(manager, right, ready, start));
            require(ready.await(15, TimeUnit.SECONDS), "Los dos hilos no llegaron a la barrera.");
            start.countDown();
            boolean first = a.get(30, TimeUnit.SECONDS);
            boolean second = b.get(30, TimeUnit.SECONDS);
            require(first != second, "Dos transferencias aceptadas o ambas rechazadas sobre el mismo UTXO.");
            var winner = first ? left : right;
            var loser = first ? right : left;
            require(manager.getSession(winner.id()) != null && manager.getSession(loser.id()) == null,
                    "Sesiones incoherentes despues de competencia sobre el mismo UTXO.");
            require(manager.deliverAtomicReceipt(winner.id()), "No se entrego recibo ganador.");
            require(manager.commitAtomicTransfer(winner.id()), "No se confirmo la transferencia ganadora.");
            LedgerValueOracle.requireConservation(before, LedgerValueOracle.capture(manager.getShards()));
            require(manager.getShard(1).getConsumedReceipts().size() == 1,
                    "No hay exactamente un recibo confirmado.");
            require(manager.auditFailureCount() == 0, "Se perdio evidencia diagnostica.");
            System.out.println("PASS G2-P2: contencion concurrente mismo UTXO variante=" + variant);
        } finally {
            executor.shutdownNow();
        }
    }

    private static boolean beginCompeting(ShardManager manager, CrossShardTransfer transfer,
                                          CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        require(start.await(15, TimeUnit.SECONDS), "Barrera de salida agotada.");
        try {
            manager.beginAtomicTransfer(transfer, 4);
            return true;
        } catch (ProtocolException expected) {
            return false;
        }
    }

    private static V2IntermediateAudit.Input input(ShardManager manager, CrossShardSession session) {
        var transfer = session.transfer();
        return new V2IntermediateAudit.Input(
                transfer.id(), session.receipt().receiptId(), transfer.sourceUtxo().key(),
                Base64.getEncoder().encodeToString(session.receipt().recipient().getEncoded()),
                transfer.sourceShardId(), transfer.targetShardId(), transfer.amount(),
                manager.auditFailureCount(), manager.commitAttemptEvents());
    }

    private static V2IntermediateAudit.Input copy(V2IntermediateAudit.Input old, long failures,
                                                   List<CommitAttemptJournal.Entry> entries) {
        return new V2IntermediateAudit.Input(old.transferId(), old.receiptId(), old.sourceUtxoKey(),
                old.recipientEncoded(), old.sourceShard(), old.targetShard(), old.amount(), failures, entries);
    }

    private static CommitAttemptJournal.Entry replace(CommitAttemptJournal.Entry old,
                        LedgerValueOracle.Snapshot ledger, String detail) {
        return new CommitAttemptJournal.Entry(old.sequence(), old.attemptId(), old.transferId(),
                old.logicalTime(), old.stage(), old.sessionStatus(), old.sessionVersion(), ledger, detail);
    }

    private static int indexOf(List<CommitAttemptJournal.Entry> list,
                               CommitAttemptJournal.Stage stage) {
        for (int i = 0; i < list.size(); i++) if (list.get(i).stage() == stage) return i;
        throw new AssertionError("Etapa requerida ausente: " + stage);
    }

    private static void reject(V2IntermediateAudit.Input input, String label) {
        var result = V2IntermediateAudit.evaluate(input);
        require(result.verdict() == V2IntermediateAudit.Verdict.DIVERGENTE,
                "El detector acepto una traza alterada: " + label + " => " + result);
    }

    private static void require(boolean condition, String detail) {
        if (!condition) throw new AssertionError(detail);
    }
}
