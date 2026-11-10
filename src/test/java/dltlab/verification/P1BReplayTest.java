package dltlab.verification;

import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.Receipt;
import dltlab.sharding.ShardManager;
import dltlab.sharding.protocol.ProtocolContext;
import dltlab.sharding.protocol.ProtocolException;
import dltlab.sharding.protocol.ReceiptFaultFixture;
import dltlab.sharding.protocol.ReceiptObservation;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.wallet.Wallet;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static dltlab.sharding.protocol.ReceiptObservation.Kind.*;

public final class P1BReplayTest {
    private static final Wallet OWNER = new Wallet("propietario");
    private static final UTXO SOURCE = new UTXO(new byte[]{1, 9}, 0);

    public static void main(String[] args) {
        validAndReplay();
        rollbackAndRetry();
        bypassAndAdministration();
        for (var evidence : List.of(ReceiptFaultFixture.confirmedDuplicate(false, false),
                ReceiptFaultFixture.confirmedDuplicate(true, false),
                ReceiptFaultFixture.confirmedDuplicate(true, true))) {
            check(!NoReceiptReplayInvariant.checkObservations(evidence).passed(),
                    "El oraculo debe detectar efectos confirmados duplicados.");
        }
        System.out.println("PASS P1-B");
    }

    private static void validAndReplay() {
        ShardManager manager = manager(ProtocolContext.FailureInjector.none());
        var transfer = signedTransfer();
        Receipt receipt = manager.lockAndCreateReceipt(transfer);
        Receipt altered = new Receipt("alterado", receipt.transferId(), receipt.sourceShardId(),
                receipt.targetShardId(), receipt.sourceUtxoKey(), receipt.amount(), receipt.recipient());
        check(!manager.commitReceipt(altered) && manager.getShard(1).getUtxoPool().size() == 0,
                "ID alterado rechazado sin credito.");
        check(manager.commitReceipt(receipt) && !manager.commitReceipt(receipt),
                "Debe haber exactamente un commit.");
        check(manager.getShard(0).getUtxoPool().totalValue() == 60
                && manager.getShard(1).getUtxoPool().totalValue() == 40, "Conservacion de saldo.");
        check(kinds(manager).equals(List.of(ISSUED, REJECTED, DELIVERED, ATTEMPT, DEBIT, CREDIT,
                CONFIRMED, REJECTED)), "Ciclo observable del recibo.");
        check(invariant(manager), "El replay rechazado no es un doble efecto.");
        reject(() -> manager.receiptObservations().clear());
    }

    private static void rollbackAndRetry() {
        AtomicBoolean fail = new AtomicBoolean(true);
        ShardManager manager = manager(point -> {
            if (point == ProtocolContext.FailurePoint.AFTER_TARGET_CREDIT && fail.getAndSet(false)) {
                throw new IllegalStateException("Fallo despues del credito.");
            }
        });
        var transfer = signedTransfer();
        manager.beginAtomicTransfer(transfer, 3);
        try {
            manager.commitAtomicTransfer(transfer.id());
            throw new AssertionError("Se esperaba un rollback.");
        } catch (ProtocolException error) {
            check(error.rolledBack(), "El rollback debe completarse.");
        }
        check(invariant(manager) && manager.getShard(0).getUtxoPool().contains(SOURCE)
                && manager.getShard(1).getUtxoPool().size() == 0, "El credito temporal se revierte.");
        check(manager.commitAtomicTransfer(transfer.id()) && invariant(manager),
                "El segundo intento puede confirmar tras rollback.");
        check(kinds(manager).equals(List.of(ISSUED, DELIVERED, ATTEMPT, DEBIT, CREDIT, ROLLED_BACK,
                ATTEMPT, DEBIT, CREDIT, CONFIRMED)), "El historial debe preservar ambos intentos.");
    }

    private static void bypassAndAdministration() {
        ShardManager[] ref = new ShardManager[1];
        var transfer = signedTransfer();
        ShardManager manager = manager(point -> {
            if (point == ProtocolContext.FailurePoint.AFTER_TARGET_CREDIT) {
                reject(() -> ref[0].advanceClockTo(50));
                reject(() -> ref[0].setShardOnline(1, false));
                reject(() -> ref[0].getProtocol().abort(transfer.id(), "Reentrada."));
                reject(() -> ref[0].getShards());
            }
        });
        ref[0] = manager;
        reject(() -> manager.getProtocol().begin(
                new CrossShardTransfer(0, 1, SOURCE, 40, OWNER.getPublicKey()), 3));
        check(manager.getSessions().isEmpty(), "La API directa tambien exige firma.");
        var session = manager.beginAtomicTransfer(transfer, 3);
        session.markAborted("Solo copia.");
        var source = manager.getShard(0);
        source.unlockUtxo(SOURCE.key());
        source.getUtxoPool().removeUTXO(SOURCE);
        manager.getShard(1).markReceiptConsumed(session.receipt().receiptId());
        check(manager.getShard(0).isLocked(SOURCE.key())
                && manager.getShard(0).getUtxoPool().contains(SOURCE)
                && manager.getShard(1).getConsumedReceipts().isEmpty()
                && manager.getSession(transfer.id()).status() == CrossShardStatus.RECEIPT_CREATED,
                "Las instantaneas no alteran estado vivo.");
        check(manager.getProtocol().commit(transfer.id()).success() && manager.getCurrentRound() == 0,
                "El hook no puede cambiar reloj ni abortar durante el commit.");
        manager.setValidatorOnline(1, 0, false);
        check(manager.getShard(1).onlineValidators() == 3, "Cambio administrativo autorizado.");

        ShardManager noQuorum = manager(ProtocolContext.FailureInjector.none());
        noQuorum.setShardOnline(1, false);
        Receipt receipt = noQuorum.lockAndCreateReceipt(transfer);
        check(!noQuorum.getProtocol().commitReceipt(receipt)
                && noQuorum.getShard(1).getUtxoPool().size() == 0, "El adaptador aplica quorum.");
        ShardManager expired = manager(ProtocolContext.FailureInjector.none());
        expired.beginAtomicTransfer(transfer, 1);
        expired.advanceClockTo(2);
        check(!expired.getProtocol().commit(transfer.id()).success()
                && expired.getShard(1).getUtxoPool().size() == 0, "La API directa aplica timeout.");
    }

    private static ShardManager manager(ProtocolContext.FailureInjector hook) {
        var setup = new ShardManager.Setup(2, 4, 3, hook);
        setup.addInitialUtxo(0, SOURCE, new Transaction.Output(100, OWNER.getPublicKey()));
        return setup.start();
    }

    private static CrossShardTransfer signedTransfer() {
        return new CrossShardTransfer(0, 1, SOURCE, 40, OWNER.getPublicKey()).sign(OWNER.getPrivateKey());
    }

    private static List<ReceiptObservation.Kind> kinds(ShardManager manager) {
        return manager.receiptObservations().stream().map(ReceiptObservation::kind).toList();
    }

    private static boolean invariant(ShardManager manager) {
        return new NoReceiptReplayInvariant().check(new LedgerState(null, manager)).passed();
    }

    private static void reject(Runnable operation) {
        try {
            operation.run();
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException expected) {
            return;
        }
        throw new AssertionError("La operacion debia rechazarse.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
