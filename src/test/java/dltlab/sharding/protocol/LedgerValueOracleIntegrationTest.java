package dltlab.sharding.protocol;

import dltlab.crypto.Hashing;
import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.ShardManager;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.verification.LedgerValueOracle;
import dltlab.wallet.Wallet;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Prueba propuesta de integración con el protocolo real (no ejecutada aquí). */
public final class LedgerValueOracleIntegrationTest {
    private LedgerValueOracleIntegrationTest() { }

    public static void main(String[] args) {
        checkCommitConservation();
        for (ProtocolContext.FailurePoint point : ProtocolContext.FailurePoint.values()) {
            checkRollback(point);
        }
        checkRecipientMutationDetected();
        System.out.println("PASS: integración del oráculo con commit y rollback");
    }

    private static void checkCommitConservation() {
        Wallet owner = new Wallet("propietario-commit");
        UTXO source = new UTXO(new byte[]{61, 1, 2}, 0);
        var setup = new ShardManager.Setup(2, 4, 3, ProtocolContext.FailureInjector.none());
        setup.addInitialUtxo(0, source, new Transaction.Output(10_000L, owner.getPublicKey()));
        ShardManager manager = setup.start();
        var transfer = new CrossShardTransfer(0, 1, source, 4_000L,
                owner.getPublicKey()).sign(owner.getPrivateKey());
        manager.beginAtomicTransfer(transfer, 4);
        var before = LedgerValueOracle.capture(List.of(manager.getShard(0), manager.getShard(1)));
        check(manager.commitAtomicTransfer(transfer.id()), "El commit debe completarse");
        var after = LedgerValueOracle.capture(List.of(manager.getShard(0), manager.getShard(1)));
        LedgerValueOracle.requireConservation(before, after);
        check(LedgerValueOracle.compare(before, after).shardDeltas().get(0).longValueExact() == -4_000L,
                "Débito neto del origen esperado");
        check(LedgerValueOracle.compare(before, after).shardDeltas().get(1).longValueExact() == 4_000L,
                "Crédito del destino esperado");
        check(manager.getSession(transfer.id()).status() == CrossShardStatus.COMMITTED,
                "Estado terminal esperado");
    }

    private static void checkRollback(ProtocolContext.FailurePoint point) {
        Wallet owner = new Wallet("propietario-rollback-" + point);
        byte marker = (byte) (80 + point.ordinal());
        UTXO source = new UTXO(new byte[]{marker, 3, 4}, 0);
        ProtocolContext.FailureInjector injector = p -> {
            if (p == point) throw new IllegalStateException("Fallo inyectado " + point);
        };
        var setup = new ShardManager.Setup(2, 4, 3, injector);
        setup.addInitialUtxo(0, source, new Transaction.Output(10_000L, owner.getPublicKey()));
        ShardManager manager = setup.start();
        var transfer = new CrossShardTransfer(0, 1, source, 4_000L,
                owner.getPublicKey()).sign(owner.getPrivateKey());
        CrossShardSession session = manager.beginAtomicTransfer(transfer, 4);
        // Fijar el estado previo al commit después de entregar el recibo:
        // commit() también entrega automáticamente el recibo antes de tomar su checkpoint.
        check(manager.deliverAtomicReceipt(transfer.id()),
                "El recibo debe entregarse antes de capturar la historia");
        // Consultar la sesion viva mediante una nueva vista despues de entregar el recibo.
        var eventHistory = manager.getSession(transfer.id()).events();
        var before = LedgerValueOracle.capture(List.of(manager.getShard(0), manager.getShard(1)));
        try {
            manager.commitAtomicTransfer(transfer.id());
            throw new AssertionError("Se esperaba fallo inyectado para " + point);
        } catch (ProtocolException failure) {
            check(failure.rolledBack(), "El rollback debe estar completo: " + point);
        }
        var after = LedgerValueOracle.capture(List.of(manager.getShard(0), manager.getShard(1)));
        LedgerValueOracle.requireConservation(before, after);
        LedgerValueOracle.requireExactRollback(before, after);
        check(manager.getSession(transfer.id()).events().equals(eventHistory),
                "La historia funcional debe regresar a su checkpoint");
        check(manager.getSession(transfer.id()).status() == CrossShardStatus.RECEIPT_DELIVERED,
                "Estado restaurado antes del commit");
    }

    private static void checkRecipientMutationDetected() {
        Wallet first = new Wallet("titular-original");
        Wallet second = new Wallet("titular-diferente");
        UTXO source = new UTXO(new byte[]{98, 7, 6}, 0);
        var setup = new ShardManager.Setup(2, 4, 3, ProtocolContext.FailureInjector.none());
        setup.addInitialUtxo(0, source, new Transaction.Output(75L, first.getPublicKey()));
        ShardManager manager = setup.start();
        // getShard() devuelve un snapshot defensivo, no una referencia al ledger vivo.
        // La perturbacion es un fixture de sensibilidad del oraculo, no un mutante Java.
        var detachedSource = manager.getShard(0);
        var detachedDestination = manager.getShard(1);
        var before = LedgerValueOracle.capture(List.of(detachedSource, detachedDestination));
        detachedSource.getUtxoPool().addUTXO(source,
                new Transaction.Output(75L, second.getPublicKey()));
        var after = LedgerValueOracle.capture(List.of(detachedSource, detachedDestination));
        check(LedgerValueOracle.compare(before, after).conserved(), "El importe no cambió");
        check(!LedgerValueOracle.compare(before, after).restoredExactly(),
                "Cambiar solo el destinatario debe invalidar el rollback exacto");
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
