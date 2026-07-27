package dltlab.sharding;

import dltlab.sharding.protocol.InvalidTransitionException;
import dltlab.sharding.protocol.ProtocolAction;
import dltlab.transaction.UTXO;

/** Pruebas de transiciones prohibidas y validacion del tiempo logico. */
public final class InvalidTransitionTest {
    private InvalidTransitionTest() {
    }

    public static void main(String[] args) {
        testCommitWithoutReceiptFails();
        testTimeoutBeforeLockFails();
        testReceiptBeforeLockFails();
        testWrongActionFails();
        testLogicalTimeCannotGoBackwards();
        System.out.println("Las pruebas de transiciones invalidas pasaron correctamente.");
    }

    private static void testCommitWithoutReceiptFails() {
        CrossShardSession session = newSession(20);
        assertThrows(InvalidTransitionException.class,
                () -> session.transitionTo(CrossShardStatus.COMMITTED, 0L,
                        ProtocolAction.COMMIT_DESTINATION,
                        "Intento de commit sin crear ni entregar el recibo."),
                "El commit sin recibo creado debe fallar.");
    }

    private static void testTimeoutBeforeLockFails() {
        CrossShardSession session = newSession(21);
        assertThrows(InvalidTransitionException.class,
                () -> session.markTimedOut("Timeout antes del bloqueo.", 1L),
                "El timeout antes del bloqueo debe fallar.");
    }

    private static void testReceiptBeforeLockFails() {
        CrossShardSession session = newSession(22);
        assertThrows(InvalidTransitionException.class,
                () -> session.markReceiptCreated(0L),
                "No se debe crear un recibo antes de bloquear el origen.");
    }

    private static void testWrongActionFails() {
        CrossShardSession session = newSession(23);
        session.markSourceLocked(0L);
        assertThrows(InvalidTransitionException.class,
                () -> session.transitionTo(CrossShardStatus.RECEIPT_CREATED, 0L,
                        ProtocolAction.DELIVER_RECEIPT,
                        "Accion incorrecta para crear el recibo."),
                "La accion debe corresponder con la transicion solicitada.");
    }

    private static void testLogicalTimeCannotGoBackwards() {
        CrossShardSession session = newSession(24);
        session.markSourceLocked(2L);
        assertThrows(IllegalArgumentException.class,
                () -> session.markReceiptCreated(1L),
                "El tiempo logico no debe retroceder.");
    }

    private static CrossShardSession newSession(int marker) {
        UTXO source = new UTXO(new byte[]{(byte) marker, 6, 7}, 0);
        CrossShardTransfer transfer = new CrossShardTransfer(0, 1, source, 1000L, null);
        Receipt receipt = new Receipt("recibo-invalido-" + marker, transfer.id(), 0, 1,
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
