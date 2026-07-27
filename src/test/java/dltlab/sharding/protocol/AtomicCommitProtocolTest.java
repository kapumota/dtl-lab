package dltlab.sharding.protocol;

import dltlab.crypto.Hashing;
import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.ShardManager;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;

import java.nio.charset.StandardCharsets;

/** Pruebas de integracion del protocolo extraido y de la API anterior. */
public final class AtomicCommitProtocolTest {
    private AtomicCommitProtocolTest() {
    }

    public static void main(String[] args) {
        testExplicitProtocolFlow();
        testLegacyManagerFlow();
        testSingleTerminalDecision();
        System.out.println("Las pruebas del protocolo atomico pasaron correctamente.");
    }

    private static void testExplicitProtocolFlow() {
        Fixture fixture = newFixture(11, ProtocolContext.FailureInjector.none());
        CrossShardProtocol protocol = fixture.manager().getProtocol();

        ProtocolResult delivery = protocol.deliverReceipt(fixture.transfer().id());
        assertTrue(delivery.success(), "La entrega explicita del recibo debe completarse.");
        assertEquals(CrossShardStatus.RECEIPT_DELIVERED, fixture.session().status(),
                "La sesion debe registrar la entrega del recibo.");

        ProtocolResult commit = protocol.commit(fixture.transfer().id());
        assertTrue(commit.success(), "El commit explicito debe completarse.");
        assertEquals(CrossShardStatus.COMMITTED, fixture.session().status(),
                "La sesion debe terminar como COMMITTED.");
        assertCommittedLedger(fixture);
    }

    private static void testLegacyManagerFlow() {
        Fixture fixture = newFixture(12, ProtocolContext.FailureInjector.none());
        assertTrue(fixture.manager().commitAtomicTransfer(fixture.transfer().id()),
                "La API anterior debe delegar y completar el commit.");
        assertEquals(CrossShardStatus.COMMITTED, fixture.session().status(),
                "La compatibilidad debe conservar el estado COMMITTED.");
        assertCommittedLedger(fixture);
    }

    private static void testSingleTerminalDecision() {
        Fixture fixture = newFixture(13, ProtocolContext.FailureInjector.none());
        assertTrue(fixture.manager().commitAtomicTransfer(fixture.transfer().id()),
                "El commit debe completarse una sola vez.");
        long terminalEvents = fixture.session().events().stream()
                .filter(event -> event.nextStatus().isTerminal())
                .count();
        assertEquals(1L, terminalEvents,
                "La traza debe contener una unica decision terminal.");
        assertFalse(fixture.manager().commitAtomicTransfer(fixture.transfer().id()),
                "Un segundo commit debe rechazarse.");
        long terminalEventsAfterRetry = fixture.session().events().stream()
                .filter(event -> event.nextStatus().isTerminal())
                .count();
        assertEquals(1L, terminalEventsAfterRetry,
                "Reintentar el commit no debe agregar otra decision terminal.");
    }

    private static Fixture newFixture(int marker, ProtocolContext.FailureInjector injector) {
        ShardManager manager = new ShardManager(2, 4, 3, injector);
        UTXO sourceUtxo = new UTXO(new byte[]{(byte) marker, 4, 7}, 0);
        manager.getShard(0).getUtxoPool().addUTXO(sourceUtxo,
                new Transaction.Output(10_000L, null));
        CrossShardTransfer transfer = new CrossShardTransfer(0, 1, sourceUtxo, 4_000L, null);
        CrossShardSession session = manager.beginAtomicTransfer(transfer, 4);
        assertEquals(CrossShardStatus.RECEIPT_CREATED, session.status(),
                "El inicio debe dejar la sesion en RECEIPT_CREATED.");
        return new Fixture(manager, sourceUtxo, transfer, session);
    }

    private static void assertCommittedLedger(Fixture fixture) {
        UTXO targetUtxo = new UTXO(Hashing.sha256(("atomic-cross-shard:"
                + fixture.session().receipt().receiptId()).getBytes(StandardCharsets.UTF_8)), 0);
        assertFalse(fixture.manager().getShard(0).getUtxoPool().contains(fixture.sourceUtxo()),
                "El commit debe consumir el UTXO origen.");
        assertFalse(fixture.manager().getShard(0).isLocked(fixture.sourceUtxo().key()),
                "El commit debe liberar el bloqueo del origen.");
        assertTrue(fixture.manager().getShard(1).getUtxoPool().contains(targetUtxo),
                "El commit debe crear el UTXO destino.");
        assertTrue(fixture.manager().getShard(1).getConsumedReceipts()
                        .contains(fixture.session().receipt().receiptId()),
                "El recibo debe quedar consumido una sola vez.");
    }

    private record Fixture(ShardManager manager, UTXO sourceUtxo,
                           CrossShardTransfer transfer, CrossShardSession session) {
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
