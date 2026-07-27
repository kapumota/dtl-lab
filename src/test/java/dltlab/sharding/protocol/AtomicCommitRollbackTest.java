package dltlab.sharding.protocol;

import dltlab.crypto.Hashing;
import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.ShardManager;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;

import java.nio.charset.StandardCharsets;

/** Pruebas de rollback ante fallos en cada etapa mutable del commit. */
public final class AtomicCommitRollbackTest {
    private AtomicCommitRollbackTest() {
    }

    public static void main(String[] args) {
        testFailureAfterReceiptConsumed();
        testFailureBetweenDebitAndCredit();
        testFailureDuringTargetCredit();
        testFailureAfterTargetCredit();
        System.out.println("Las pruebas de rollback atomico pasaron correctamente.");
    }

    private static void testFailureAfterReceiptConsumed() {
        assertRollbackAt(ProtocolContext.FailurePoint.AFTER_RECEIPT_CONSUMED, 21);
    }

    private static void testFailureBetweenDebitAndCredit() {
        assertRollbackAt(ProtocolContext.FailurePoint.AFTER_SOURCE_DEBIT, 22);
    }

    private static void testFailureDuringTargetCredit() {
        assertRollbackAt(ProtocolContext.FailurePoint.DURING_TARGET_CREDIT, 23);
    }

    private static void testFailureAfterTargetCredit() {
        assertRollbackAt(ProtocolContext.FailurePoint.AFTER_TARGET_CREDIT, 24);
    }

    private static void assertRollbackAt(ProtocolContext.FailurePoint failurePoint, int marker) {
        ProtocolContext.FailureInjector injector = point -> {
            if (point == failurePoint) {
                throw new IllegalStateException("Fallo inyectado para comprobar rollback.");
            }
        };
        ShardManager manager = new ShardManager(2, 4, 3, injector);
        UTXO sourceUtxo = new UTXO(new byte[]{(byte) marker, 8, 9}, 0);
        Transaction.Output original = new Transaction.Output(10_000L, null);
        manager.getShard(0).getUtxoPool().addUTXO(sourceUtxo, original);
        CrossShardTransfer transfer = new CrossShardTransfer(0, 1, sourceUtxo, 4_000L, null);
        CrossShardSession session = manager.beginAtomicTransfer(transfer, 4);

        ProtocolException error = assertThrows(ProtocolException.class,
                () -> manager.commitAtomicTransfer(transfer.id()),
                "El fallo inyectado debe abortar el commit.");
        assertTrue(error.rolledBack(), "La excepcion debe confirmar que el rollback se completo.");

        UTXO targetUtxo = new UTXO(Hashing.sha256(("atomic-cross-shard:"
                + session.receipt().receiptId()).getBytes(StandardCharsets.UTF_8)), 0);
        UTXO changeUtxo = new UTXO(Hashing.sha256(("cross-shard-change:"
                + session.receipt().receiptId()).getBytes(StandardCharsets.UTF_8)), 0);

        assertTrue(manager.getShard(0).getUtxoPool().contains(sourceUtxo),
                "El rollback debe restaurar el UTXO origen.");
        assertEquals(10_000L, manager.getShard(0).getUtxoPool().getOutput(sourceUtxo).getValue(),
                "El rollback debe restaurar el valor original.");
        assertTrue(manager.getShard(0).isLocked(sourceUtxo.key()),
                "El rollback debe restaurar el bloqueo previo al commit.");
        assertFalse(manager.getShard(0).getUtxoPool().contains(changeUtxo),
                "El rollback debe eliminar el cambio parcial.");
        assertFalse(manager.getShard(1).getUtxoPool().contains(targetUtxo),
                "El rollback debe eliminar el credito parcial.");
        assertFalse(manager.getShard(1).getConsumedReceipts().contains(session.receipt().receiptId()),
                "El rollback debe restaurar el recibo como no consumido.");
        assertEquals(CrossShardStatus.RECEIPT_DELIVERED, session.status(),
                "El rollback debe restaurar el estado previo a la preparacion.");
        assertEquals(0L, session.events().stream()
                        .filter(event -> event.nextStatus().isTerminal()).count(),
                "Un commit fallido no debe dejar una decision terminal.");
    }

    private static <T extends Throwable> T assertThrows(Class<T> expected, Runnable action,
                                                         String message) {
        try {
            action.run();
        } catch (Throwable error) {
            if (expected.isInstance(error)) {
                return expected.cast(error);
            }
            throw new AssertionError(message + " Se obtuvo " + error.getClass().getSimpleName() + ".", error);
        }
        throw new AssertionError(message + " No se produjo la excepcion esperada.");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " Esperado: " + expected + ". Actual: " + actual + ".");
        }
    }
}
