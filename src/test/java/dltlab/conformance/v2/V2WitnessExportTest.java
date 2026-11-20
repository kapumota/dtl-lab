package dltlab.conformance.v2;

import dltlab.sharding.ShardManager;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.protocol.ProtocolContext;
import dltlab.sharding.protocol.ProtocolException;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.verification.LedgerValueOracle;
import dltlab.wallet.Wallet;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Exporta testigos de operaciones reales para un replay acotado en TLC V2.2.
 * NO emite un veredicto de conformidad: solo selecciona entradas ya validadas por G2-P0.
 */
public final class V2WitnessExportTest {
    private static final String HEADER = String.join("\t", List.of(
            "scenario", "kind", "transfer_id_b64", "utxo_key_b64", "amount",
            "quorum", "votes", "source_shard", "target_shard", "before_status",
            "after_status", "verdict", "business_restored", "diagnostics_changed",
            "audit_failures", "delta_source", "delta_target"));

    private V2WitnessExportTest() { }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Uso: V2WitnessExportTest DIRECTORIO_DE_EVIDENCIA");
        }
        Path directory = Path.of(args[0]);
        Files.createDirectories(directory);
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        lines.add(run("normal", null));
        for (ProtocolContext.FailurePoint point : ProtocolContext.FailurePoint.values()) {
            lines.add(run("rollback_" + point.name(), point));
        }
        Path output = directory.resolve("testigos_java_v2.tsv");
        Files.writeString(output, String.join("\n", lines) + "\n", StandardCharsets.UTF_8);
        System.out.println("PASS: exportados 5 testigos Java para replay TLC: " + output);
    }

    private static String run(String scenario, ProtocolContext.FailurePoint failure) {
        var owner = new Wallet("g2-p1-" + scenario);
        var source = new UTXO(new byte[]{109, 3, (byte) (failure == null ? 7 : failure.ordinal())}, 0);
        ProtocolContext.FailureInjector injector = failure == null
                ? ProtocolContext.FailureInjector.none()
                : point -> {
                    if (point == failure) throw new IllegalStateException("Fallo deliberado: " + failure);
                };
        var setup = new ShardManager.Setup(2, 4, 3, injector);
        setup.addInitialUtxo(0, source, new Transaction.Output(10_000L, owner.getPublicKey()));
        ShardManager manager = setup.start();
        CrossShardTransfer transfer = new CrossShardTransfer(0, 1, source, 4_000L,
                owner.getPublicKey()).sign(owner.getPrivateKey());
        manager.beginAtomicTransfer(transfer, 4);
        require(manager.deliverAtomicReceipt(transfer.id()), "Recibo no entregado");
        CrossShardSession before = manager.getSession(transfer.id());
        var ledgerBefore = LedgerValueOracle.capture(manager.getShards());
        var obsBefore = manager.receiptObservations();
        if (failure == null) {
            require(manager.commitAtomicTransfer(transfer.id()), "Commit no confirmado");
        } else {
            boolean observedFailure = false;
            try {
                manager.commitAtomicTransfer(transfer.id());
            } catch (ProtocolException expected) {
                observedFailure = true;
                require(expected.rolledBack(), "Rollback incompleto: " + failure);
            }
            require(observedFailure, "No se activo el fallo: " + failure);
        }
        var after = manager.getSession(transfer.id());
        var ledgerAfter = LedgerValueOracle.capture(manager.getShards());
        var result = V2BoundaryProjection.classify(new V2BoundaryProjection.Input(
                before, after, ledgerBefore, ledgerAfter, manager.commitAttemptEvents(),
                obsBefore, manager.receiptObservations(), manager.getQuorum(),
                manager.auditFailureCount()));
        var expected = failure == null
                ? V2BoundaryProjection.Verdict.REPLAY_PENDIENTE_TLC
                : V2BoundaryProjection.Verdict.STUTTER_ESTADO_NEGOCIO;
        require(result.verdict() == expected, "Verdicto G2-P0 inesperado: " + result);
        require(manager.auditFailureCount() == 0, "Instrumentacion G1 con fallos");
        require(before.status() == CrossShardStatus.RECEIPT_DELIVERED,
                "La frontera Java no se encuentra en RECEIPT_DELIVERED");
        require(after.status() == (failure == null
                ? CrossShardStatus.COMMITTED : CrossShardStatus.RECEIPT_DELIVERED),
                "Estado final Java distinto del previsto");
        require(result.diagnosticObservationsChanged(), "No hay evidencia diagnostica");
        require(failure == null || result.businessRestored(), "El estado de negocio no se restauro");
        require(failure != null || after.targetApprovals() >= manager.getQuorum(),
                "Commit confirmado sin quorum de destino");
        long approvals = failure == null ? after.targetApprovals() : 0L;
        String transferId = encode(transfer.id());
        String sourceKey = encode(source.key());
        return String.join("\t", List.of(
                scenario, failure == null ? "COMMIT" : "ROLLBACK",
                transferId, sourceKey, Long.toString(transfer.amount()),
                Integer.toString(manager.getQuorum()), Long.toString(approvals),
                Integer.toString(transfer.sourceShardId()),
                Integer.toString(transfer.targetShardId()),
                before.status().name(), after.status().name(), result.verdict().name(),
                Boolean.toString(result.businessRestored()),
                Boolean.toString(result.diagnosticObservationsChanged()),
                Long.toString(manager.auditFailureCount()),
                delta(result.deltas().getOrDefault(transfer.sourceShardId(), BigInteger.ZERO)),
                delta(result.deltas().getOrDefault(transfer.targetShardId(), BigInteger.ZERO))));
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String delta(BigInteger value) { return value.toString(); }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
