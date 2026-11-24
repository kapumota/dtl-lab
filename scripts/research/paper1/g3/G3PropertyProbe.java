package dltlab.research.paper1.g3;

import dltlab.conformance.v2.V2IntermediateAudit;
import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.ShardManager;
import dltlab.sharding.protocol.ProtocolContext;
import dltlab.sharding.protocol.ProtocolException;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.verification.LedgerValueOracle;
import dltlab.wallet.Wallet;

import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Oráculo adicional sobre transacciones Java reales, fuera del journal G1. */
public final class G3PropertyProbe {
    private G3PropertyProbe() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Uso: G3PropertyProbe beneficiario|rollback|contencion");
        }
        switch (args[0]) {
            case "beneficiario" -> beneficiarioDistinto();
            case "rollback" -> rollbackCreditoParcial();
            case "contencion" -> contencionReal();
            default -> throw new IllegalArgumentException("Escenario no registrado: " + args[0]);
        }
        System.out.println("G3_PROPIEDAD_PASS:" + args[0]);
    }

    private static void beneficiarioDistinto() {
        Wallet titular = new Wallet("g3-titular");
        Wallet beneficiario = new Wallet("g3-beneficiario-distinto");
        UTXO fuente = new UTXO(new byte[] {44, 15, 21}, 0);
        var setup = new ShardManager.Setup(2, 4, 3);
        setup.addInitialUtxo(0, fuente, new Transaction.Output(10_000L, titular.getPublicKey()));
        ShardManager manager = setup.start();
        var transfer = new CrossShardTransfer(0, 1, fuente, 4_000L,
                beneficiario.getPublicKey()).sign(titular.getPrivateKey());
        CrossShardSession session = manager.beginAtomicTransfer(transfer, 4);
        validar(manager.deliverAtomicReceipt(transfer.id()), "No se entregó el recibo");
        var previo = LedgerValueOracle.capture(manager.getShards());
        validar(manager.commitAtomicTransfer(transfer.id()), "El commit fue rechazado");
        var posterior = LedgerValueOracle.capture(manager.getShards());
        var g2 = V2IntermediateAudit.evaluate(new V2IntermediateAudit.Input(
                transfer.id(), session.receipt().receiptId(), fuente.key(),
                Base64.getEncoder().encodeToString(beneficiario.getPublicKey().getEncoded()),
                0, 1, 4_000L, manager.auditFailureCount(), manager.commitAttemptEvents()));
        System.out.println("G3_G2_VEREDICTO:" + g2.verdict());

        var origen = posterior.utxos().get(0);
        var destino = posterior.utxos().get(1);
        String llaveTitular = Base64.getEncoder().encodeToString(titular.getPublicKey().getEncoded());
        String llaveBeneficiario = Base64.getEncoder().encodeToString(beneficiario.getPublicKey().getEncoded());
        validar(!origen.containsKey(fuente.key()), "No se consumió el UTXO de origen");
        validar(origen.size() == 1, "Cantidad incorrecta de salidas de cambio");
        validar(destino.size() == 1, "Cantidad incorrecta de salidas en destino");
        var cambio = origen.values().iterator().next();
        var credito = destino.values().iterator().next();
        validar(cambio.amount() == 6_000L, "Importe incorrecto del cambio");
        validar(cambio.recipientEncoded().equals(llaveTitular), "El cambio pertenece a otra clave");
        validar(credito.amount() == 4_000L, "Importe incorrecto del crédito");
        validar(credito.recipientEncoded().equals(llaveBeneficiario), "Se acreditó al destinatario equivocado");
        validar(posterior.consumedReceipts().get(1).contains(session.receipt().receiptId()),
                "El recibo correcto no fue consumido en el shard destino");
        validar(!posterior.locks().get(0).contains(fuente.key()), "Quedó un bloqueo residual");
        LedgerValueOracle.requireConservation(previo, posterior);
        validar(g2.verdict() == V2IntermediateAudit.Verdict.TRACE_COMMIT_VALIDADA,
                "G2 divergió respecto del commit real");
    }

    private static void rollbackCreditoParcial() {
        Wallet titular = new Wallet("g3-rollback-titular");
        UTXO fuente = new UTXO(new byte[] {44, 16, 22}, 0);
        ProtocolContext.FailureInjector inyector = punto -> {
            if (punto == ProtocolContext.FailurePoint.AFTER_TARGET_CREDIT) {
                throw new IllegalStateException("Fallo controlado después del crédito");
            }
        };
        var setup = new ShardManager.Setup(2, 4, 3, inyector);
        setup.addInitialUtxo(0, fuente, new Transaction.Output(10_000L, titular.getPublicKey()));
        ShardManager manager = setup.start();
        var transfer = new CrossShardTransfer(0, 1, fuente, 4_000L,
                titular.getPublicKey()).sign(titular.getPrivateKey());
        CrossShardSession sesion = manager.beginAtomicTransfer(transfer, 4);
        validar(manager.deliverAtomicReceipt(transfer.id()), "Entrega del recibo fallida");
        var previo = LedgerValueOracle.capture(manager.getShards());
        boolean restaurado = false;
        try {
            manager.commitAtomicTransfer(transfer.id());
        } catch (ProtocolException error) {
            restaurado = error.rolledBack();
            System.out.println("G3_ROLLBACK_DECLARADO:" + restaurado);
        }
        var posterior = LedgerValueOracle.capture(manager.getShards());
        var g2 = V2IntermediateAudit.evaluate(new V2IntermediateAudit.Input(
                transfer.id(), sesion.receipt().receiptId(), fuente.key(),
                Base64.getEncoder().encodeToString(titular.getPublicKey().getEncoded()),
                0, 1, 4_000L, manager.auditFailureCount(), manager.commitAttemptEvents()));
        System.out.println("G3_G2_VEREDICTO:" + g2.verdict());
        LedgerValueOracle.requireExactRollback(previo, posterior);
        validar(restaurado, "No se confirmó el rollback completo");
        validar(manager.getSession(transfer.id()).status() == CrossShardStatus.RECEIPT_DELIVERED,
                "Sesión no restaurada al checkpoint");
        validar(g2.verdict() == V2IntermediateAudit.Verdict.TRACE_ROLLBACK_VALIDADA,
                "G2 no validó el rollback real");
    }

    private static void contencionReal() throws Exception {
        Wallet titular = new Wallet("g3-contencion-titular");
        UTXO fuente = new UTXO(new byte[] {44, 17, 23}, 0);
        var setup = new ShardManager.Setup(2, 4, 3);
        setup.addInitialUtxo(0, fuente, new Transaction.Output(10_000L, titular.getPublicKey()));
        ShardManager manager = setup.start();
        var primera = new CrossShardTransfer(0, 1, fuente, 4_000L,
                titular.getPublicKey()).sign(titular.getPrivateKey());
        var segunda = new CrossShardTransfer(0, 1, fuente, 3_000L,
                titular.getPublicKey()).sign(titular.getPrivateKey());
        CountDownLatch lista = new CountDownLatch(2);
        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> uno = hilos.submit(() -> iniciar(manager, primera, lista, salida));
            Future<Boolean> dos = hilos.submit(() -> iniciar(manager, segunda, lista, salida));
            validar(lista.await(15, TimeUnit.SECONDS), "Los hilos no llegaron a la barrera");
            salida.countDown();
            boolean a = uno.get(30, TimeUnit.SECONDS);
            boolean b = dos.get(30, TimeUnit.SECONDS);
            validar(a != b, "Dos sesiones compitieron con éxito por el mismo UTXO");
            validar(manager.getSessions().size() == 1, "Debe existir una sola sesión de transferencia");
            var vencedora = a ? primera : segunda;
            validar(manager.commitAtomicTransfer(vencedora.id()), "No se completó la ganadora");
            var ledger = LedgerValueOracle.capture(manager.getShards());
            validar(ledger.consumedReceipts().get(1).size() == 1,
                    "Debe consumirse exactamente un recibo");
            validar(ledger.totalValue().longValueExact() == 10_000L,
                    "Valor neto incorrecto después de la contención");
        } finally {
            salida.countDown();
            hilos.shutdownNow();
        }
    }

    private static boolean iniciar(ShardManager manager, CrossShardTransfer t,
                                   CountDownLatch lista, CountDownLatch salida) throws Exception {
        lista.countDown();
        validar(salida.await(15, TimeUnit.SECONDS), "Barrera agotada");
        try {
            manager.beginAtomicTransfer(t, 4);
            return true;
        } catch (ProtocolException expected) {
            return false;
        }
    }

    private static void validar(boolean valor, String mensaje) {
        if (!valor) throw new AssertionError(mensaje);
    }
}
