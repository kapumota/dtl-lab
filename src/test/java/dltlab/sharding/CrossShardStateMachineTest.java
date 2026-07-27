package dltlab.sharding;

import dltlab.sharding.protocol.ProtocolAction;
import dltlab.sharding.protocol.ProtocolEvent;
import dltlab.transaction.UTXO;

import java.util.List;

/** Pruebas del recorrido permitido de la maquina de estados cross-shard. */
public final class CrossShardStateMachineTest {
    private CrossShardStateMachineTest() {
    }

    public static void main(String[] args) {
        testSuccessfulStatePath();
        testEventsAreImmutable();
        System.out.println("Las pruebas de la maquina de estados cross-shard pasaron correctamente.");
    }

    private static void testSuccessfulStatePath() {
        CrossShardSession session = newSession(1);
        assertEquals(CrossShardStatus.CREATED, session.status(),
                "Una sesion nueva debe iniciar en CREATED.");
        assertTrue(session.canTransitionTo(CrossShardStatus.SOURCE_LOCKED),
                "CREATED debe permitir el bloqueo del origen.");
        assertFalse(session.canTransitionTo(CrossShardStatus.COMMITTED),
                "CREATED no debe permitir un commit directo.");

        session.markSourceLocked(0L);
        session.markReceiptCreated(0L);
        session.markReceiptDelivered(1L);
        session.markDestinationPrepared(3, 4, 1L);
        session.markCommitted(3, 4, 2L);

        assertEquals(CrossShardStatus.COMMITTED, session.status(),
                "El recorrido valido debe terminar en COMMITTED.");
        assertTrue(session.isTerminal(), "COMMITTED debe ser un estado terminal.");
        assertEquals(3, session.targetApprovals(),
                "La sesion debe conservar las aprobaciones del destino.");
        assertEquals(4, session.targetValidators(),
                "La sesion debe conservar la cantidad de validadores del destino.");
        assertEquals(5L, session.stateVersion(),
                "Cinco transiciones deben incrementar la version del estado.");

        List<ProtocolEvent> events = session.events();
        assertEquals(6, events.size(),
                "La traza debe incluir la creacion y cinco transiciones.");
        ProtocolEvent first = events.get(0);
        assertTrue(first.isInitial(), "El primer evento debe representar la creacion de la sesion.");
        assertEquals(null, first.previousStatus(),
                "El evento inicial no debe tener un estado anterior.");
        assertEquals(CrossShardStatus.CREATED, first.nextStatus(),
                "El evento inicial debe terminar en CREATED.");

        ProtocolEvent last = events.get(events.size() - 1);
        assertEquals(5L, last.sequence(), "La secuencia final debe ser monotona.");
        assertEquals(ProtocolAction.COMMIT_DESTINATION, last.action(),
                "La ultima accion debe ser COMMIT_DESTINATION.");
        assertEquals(CrossShardStatus.DESTINATION_PREPARED, last.previousStatus(),
                "El commit debe partir de DESTINATION_PREPARED.");
        assertEquals(CrossShardStatus.COMMITTED, last.nextStatus(),
                "El commit debe terminar en COMMITTED.");
    }

    private static void testEventsAreImmutable() {
        CrossShardSession session = newSession(2);
        assertThrows(UnsupportedOperationException.class,
                () -> session.events().add(session.events().get(0)),
                "La lista publica de eventos debe ser inmutable.");
    }

    private static CrossShardSession newSession(int marker) {
        UTXO source = new UTXO(new byte[]{(byte) marker, 2, 3}, 0);
        CrossShardTransfer transfer = new CrossShardTransfer(0, 1, source, 1000L, null);
        Receipt receipt = new Receipt("recibo-" + marker, transfer.id(), 0, 1,
                source.key(), 1000L, null);
        return new CrossShardSession(transfer, receipt, 0, 5, 3, 4);
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
