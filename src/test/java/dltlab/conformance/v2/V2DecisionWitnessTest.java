package dltlab.conformance.v2;

import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.ShardManager;
import dltlab.sharding.protocol.CommitAttemptJournal;
import dltlab.sharding.protocol.ProtocolContext;
import dltlab.sharding.protocol.ProtocolException;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.wallet.Wallet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Testigos reales para decidir el alcance de G2, sin modificar el protocolo.
 * No afirma que exista un punto de decisión irrevocable en Java.
 */
public final class V2DecisionWitnessTest {
    private V2DecisionWitnessTest() { }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Uso: V2DecisionWitnessTest DIRECTORIO_SALIDA");
        }
        List<String> rows = new ArrayList<>();
        rows.add("scenario\tresult\tfailure_point\tpartial_debit\tdecision_witness\t"
                + "saw_debit\tsaw_credit\trollback_exact\tstages");
        rows.add(run("normal", null));
        for (var point : ProtocolContext.FailurePoint.values()) {
            rows.add(run("rollback_" + point.name(), point));
        }
        Path output = Path.of(args[0]).resolve("G2P3_TESTIGOS_JAVA.tsv");
        Files.createDirectories(output.getParent());
        Files.write(output, rows, StandardCharsets.UTF_8);
        System.out.println("PASS G2-P3: cinco testigos Java y decision sin testigo irrevocable: " + output);
    }

    private static String run(String scenario, ProtocolContext.FailurePoint point) {
        Wallet owner = new Wallet("g2-p3-" + scenario);
        byte marker = (byte) (point == null ? 0 : point.ordinal() + 1);
        UTXO source = new UTXO(new byte[] {112, 3, 33, marker}, 0);
        ProtocolContext.FailureInjector injector = current -> {
            if (current == point) {
                throw new IllegalStateException("Fallo G2-P3: " + current);
            }
        };
        ShardManager.Setup setup = new ShardManager.Setup(2, 4, 3, injector);
        setup.addInitialUtxo(0, source,
                new Transaction.Output(10_000L, owner.getPublicKey()));
        ShardManager manager = setup.start();
        CrossShardTransfer transfer = new CrossShardTransfer(0, 1, source, 4_000L,
                owner.getPublicKey()).sign(owner.getPrivateKey());
        manager.beginAtomicTransfer(transfer, 4);
        require(manager.deliverAtomicReceipt(transfer.id()), "Fallo al entregar el recibo");
        var before = manager.getSession(transfer.id());
        if (point == null) {
            require(manager.commitAtomicTransfer(transfer.id()), "Commit normal rechazado");
        } else {
            try {
                manager.commitAtomicTransfer(transfer.id());
                throw new AssertionError("La inyeccion no interrumpio el commit: " + point);
            } catch (ProtocolException expected) {
                require(expected.rolledBack(), "Rollback incompleto: " + point);
            }
        }
        V2IntermediateAudit.Input input = new V2IntermediateAudit.Input(
                transfer.id(), before.receipt().receiptId(), transfer.sourceUtxo().key(),
                Base64.getEncoder().encodeToString(before.receipt().recipient().getEncoded()),
                transfer.sourceShardId(), transfer.targetShardId(), transfer.amount(),
                manager.auditFailureCount(), manager.commitAttemptEvents());
        V2IntermediateAudit.Result result = V2IntermediateAudit.evaluate(input);
        boolean commit = point == null;
        require(result.verdict() == (commit
                        ? V2IntermediateAudit.Verdict.TRACE_COMMIT_VALIDADA
                        : V2IntermediateAudit.Verdict.TRACE_ROLLBACK_VALIDADA),
                "Auditoria G2-P2 no valida: " + scenario + " " + result);
        require(!result.durableDecisionWitnessed(),
                "No atribuir una decision irrevocable Java sin evidencia directa");
        require(commit || point.name().equals(result.failurePoint()),
                "El punto de fallo observado es incorrecto");
        boolean partial = point != null && point != ProtocolContext.FailurePoint.AFTER_RECEIPT_CONSUMED;
        require(result.partialApplicationSeen() == (commit || partial),
                "Debito parcial clasificado incorrectamente: " + scenario);
        var stages = result.stages();
        boolean debit = stages.contains(CommitAttemptJournal.Stage.SOURCE_DEBITED);
        boolean credit = stages.contains(CommitAttemptJournal.Stage.DESTINATION_CREDITED);
        boolean restored = !commit &&
                stages.get(stages.size() - 1) == CommitAttemptJournal.Stage.ROLLBACK_COMPLETED;
        require(debit == (commit || partial), "Debito mal identificado");
        require(credit == (commit || point == ProtocolContext.FailurePoint.AFTER_TARGET_CREDIT),
                "Credito mal identificado");
        require(commit || restored, "No consta rollback exacto");
        require(manager.auditFailureCount() == 0L, "Journal incompleto");
        String stagesCsv = String.join(",", stages.stream().map(Enum::name).toList());
        System.out.println("PASS " + scenario + ": " + result.verdict()
                + ", debito=" + debit + ", credito=" + credit);
        return String.join("\t", scenario, result.verdict().name(),
                point == null ? "NONE" : point.name(),
                Boolean.toString(result.partialApplicationSeen()),
                Boolean.toString(result.durableDecisionWitnessed()),
                Boolean.toString(debit), Boolean.toString(credit),
                Boolean.toString(restored), stagesCsv);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
