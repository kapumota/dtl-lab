package dltlab.sharding;

import dltlab.sharding.protocol.InvalidTransitionException;
import dltlab.transaction.UTXO;

/** Pruebas de irreversibilidad para los estados terminales. */
public final class TerminalStateTest {
    private TerminalStateTest() {
    }

    public static void main(String[] args) {
        testCommittedCannotAbort();
        testTimedOutCannotCommit();
        testDoubleCommitFails();
        testEveryTerminalStateRejectsChanges();
        System.out.println("Las pruebas de estados terminales pasaron correctamente.");
    }

    private static void testCommittedCannotAbort() {
        CrossShardSession session = committedSession(10);
        assertThrows(InvalidTransitionException.class,
                () -> session.markAborted("Abort posterior al commit.", 3L),
                "COMMITTED -> ABORTED debe fallar.");
    }

    private static void testTimedOutCannotCommit() {
        CrossShardSession session = receiptCreatedSession(11);
        session.markTimedOut("La transferencia alcanzo el timeout.", 5L);
        assertThrows(InvalidTransitionException.class,
                () -> session.markCommitted(3, 4, 6L),
                "TIMED_OUT -> COMMITTED debe fallar.");
    }

    private static void testDoubleCommitFails() {
        CrossShardSession session = committedSession(12);
        assertThrows(InvalidTransitionException.class,
                () -> session.markCommitted(3, 4, 4L),
                "Un segundo commit debe fallar.");
    }

    private static void testEveryTerminalStateRejectsChanges() {
        CrossShardSession aborted = receiptCreatedSession(13);
        aborted.markAborted("Abort de prueba.", 2L);
        assertThrows(InvalidTransitionException.class,
                () -> aborted.markFailedValidation("Cambio posterior al abort.", 3L),
                "ABORTED no debe permitir nuevas transiciones.");

        CrossShardSession failed = newSession(14);
        failed.markFailedValidation("Fallo de quorum en origen.", 0L);
        assertThrows(InvalidTransitionException.class,
                () -> failed.markSourceLocked(1L),
                "FAILED_VALIDATION no debe permitir nuevas transiciones.");
    }

    private static CrossShardSession committedSession(int marker) {
        CrossShardSession session = receiptCreatedSession(marker);
        session.markReceiptDelivered(1L);
        session.markDestinationPrepared(3, 4, 1L);
        session.markCommitted(3, 4, 2L);
        return session;
    }

    private static CrossShardSession receiptCreatedSession(int marker) {
        CrossShardSession session = newSession(marker);
        session.markSourceLocked(0L);
        session.markReceiptCreated(0L);
        return session;
    }

    private static CrossShardSession newSession(int marker) {
        UTXO source = new UTXO(new byte[]{(byte) marker, 4, 5}, 0);
        CrossShardTransfer transfer = new CrossShardTransfer(0, 1, source, 1000L, null);
        Receipt receipt = new Receipt("recibo-terminal-" + marker, transfer.id(), 0, 1,
                source.key(), 1000L, null);
        return new CrossShardSession(transfer, receipt, 0, 5, 3, 4);
    }

    private static void assertThrows(Class<? extends Throwable> expected, Runnable action,
                                     String message) {
        try {
            action.run();
        } catch (Throwable error) {
            if (expected.isInstance(error)) {
                return;
            }
            throw new AssertionError(message + " Se obtuvo " + error.getClass().getSimpleName() + ".", error);
        }
        throw new AssertionError(message + " No se produjo la excepcion esperada.");
    }
}
