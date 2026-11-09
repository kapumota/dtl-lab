package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.Shard;
import dltlab.sharding.ShardManager;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.wallet.Wallet;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/** Comprueba vistas conjuntas del gestor bajo commit y rollback coordinados. */
public final class P1CIsolationTest {
    private static final Wallet OWNER = new Wallet("propietario");
    private static final UTXO SOURCE = new UTXO(new byte[]{7, 1, 3}, 0);
    private static final long TIMEOUT_SECONDS = 10;

    private P1CIsolationTest() {}

    public static void main(String[] args) throws Exception {
        for (var point : ProtocolContext.FailurePoint.values()) {
            for (boolean rollback : new boolean[]{false, true}) {
                for (boolean direct : new boolean[]{false, true}) {
                    jointSnapshot(point, rollback, direct);
                }
            }
        }
        independentReads();
        System.out.println("PASS P1-C: 16 intercalados conjuntos y lecturas individuales.");
    }

    private static void jointSnapshot(ProtocolContext.FailurePoint point, boolean rollback,
                                      boolean direct) throws Exception {
        CountDownLatch stopped = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch readRequested = new CountDownLatch(1);
        ShardManager manager = manager(observed -> {
            if (observed == point) {
                stopped.countDown();
                await(release);
                if (rollback) {
                    throw new IllegalStateException("Fallo controlado para comprobar la vista restaurada.");
                }
            }
        });
        var transfer = transfer();
        manager.beginAtomicTransfer(transfer, 3);
        var before = manager.getShards();
        assertView(before, false);

        FutureTask<Boolean> write = new FutureTask<>(() -> {
            try {
                boolean success = direct ? manager.getProtocol().commit(transfer.id()).success()
                        : manager.commitAtomicTransfer(transfer.id());
                check(!rollback && success, "Resultado inesperado del commit.");
                return true;
            } catch (ProtocolException error) {
                check(rollback && error.rolledBack(), "El fallo debe completar el rollback.");
                return false;
            }
        });
        FutureTask<List<Shard>> read = new FutureTask<>(() -> {
            readRequested.countDown();
            return manager.getShards();
        });
        Thread writer = new Thread(write, "p1c-escritor");
        Thread reader = new Thread(read, "p1c-lector");
        writer.setDaemon(true);
        reader.setDaemon(true);
        try {
            writer.start();
            await(stopped);
            reader.start();
            await(readRequested);
            assertBlockedOnWriter(reader, writer, read);
            check(!read.isDone(), "La vista conjunta termino mientras el commit estaba detenido.");
        } finally {
            release.countDown();
        }
        check(write.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) == !rollback, "Resultado del escritor.");
        var snapshot = read.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        writer.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
        reader.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
        check(!writer.isAlive() && !reader.isAlive(), "Los hilos deben terminar.");
        assertView(snapshot, !rollback);
        assertView(before, false);
        check(manager.getSession(transfer.id()).status()
                        == (rollback ? CrossShardStatus.RECEIPT_DELIVERED : CrossShardStatus.COMMITTED),
                "La sesion debe reflejar el resultado coherente.");
        snapshot.get(0).getUtxoPool().removeUTXO(SOURCE);
        snapshot.get(0).unlockUtxo(SOURCE.key());
        assertView(manager.getShards(), !rollback);
        System.out.println("PASS " + point + " rollback=" + rollback + " protocoloDirecto=" + direct);
    }

    private static void assertBlockedOnWriter(Thread reader, Thread writer, FutureTask<?> result) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
        var threads = ManagementFactory.getThreadMXBean();
        while (System.nanoTime() < deadline) {
            var info = threads.getThreadInfo(reader.getId());
            if (info != null && info.getThreadState() == Thread.State.BLOCKED
                    && info.getLockOwnerId() == writer.getId()) {
                return;
            }
            check(!result.isDone(), "El lector accedio al ledger antes de liberar al escritor.");
            Thread.yield();
        }
        throw new AssertionError("No se observo al lector esperando el monitor del escritor.");
    }

    private static void independentReads() throws Exception {
        ShardManager manager = manager(ProtocolContext.FailureInjector.none());
        var transfer = transfer();
        manager.beginAtomicTransfer(transfer, 3);
        CountDownLatch sourceRead = new CountDownLatch(1);
        CountDownLatch committed = new CountDownLatch(1);
        var jointBefore = manager.getShards();
        FutureTask<Long> reads = new FutureTask<>(() -> {
            long source = manager.getShard(0).getUtxoPool().totalValue();
            sourceRead.countDown();
            await(committed);
            long target = manager.getShard(1).getUtxoPool().totalValue();
            return Math.addExact(source, target);
        });
        FutureTask<Boolean> write = new FutureTask<>(() -> {
            await(sourceRead);
            try {
                return manager.commitAtomicTransfer(transfer.id());
            } finally {
                committed.countDown();
            }
        });
        Thread reader = new Thread(reads, "p1c-lecturas-individuales");
        Thread writer = new Thread(write, "p1c-commit-entre-lecturas");
        reader.setDaemon(true);
        writer.setDaemon(true);
        reader.start();
        writer.start();
        check(write.get(TIMEOUT_SECONDS, TimeUnit.SECONDS), "El commit intermedio debe completarse.");
        check(reads.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) == 140,
                "Las consultas individuales deben reflejar los dos instantes coordinados.");
        reader.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
        writer.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
        check(!writer.isAlive() && !reader.isAlive(), "Los hilos deben terminar.");
        assertView(jointBefore, false);
        assertView(manager.getShards(), true);
        System.out.println("PASS lecturas individuales atraviesan commit: suma=140; vistas conjuntas=100.");
    }

    private static void assertView(List<Shard> shards, boolean committed) {
        var source = shards.get(0);
        var target = shards.get(1);
        check(shards.size() == 2 && source.getId() == 0 && target.getId() == 1, "Topologia.");
        check(source.getUtxoPool().totalValue() == (committed ? 60 : 100)
                && target.getUtxoPool().totalValue() == (committed ? 40 : 0), "Saldos de la vista.");
        check(source.getUtxoPool().contains(SOURCE) != committed
                && source.isLocked(SOURCE.key()) != committed, "Consumo y bloqueo del origen.");
        check(source.getUtxoPool().size() == 1 && target.getUtxoPool().size() == (committed ? 1 : 0),
                "UTXO origen, cambio y destino.");
        check(target.getConsumedReceipts().size() == (committed ? 1 : 0), "Recibo definitivo.");
    }

    private static ShardManager manager(ProtocolContext.FailureInjector hook) {
        var setup = new ShardManager.Setup(2, 4, 3, hook);
        setup.addInitialUtxo(0, SOURCE, new Transaction.Output(100, OWNER.getPublicKey()));
        return setup.start();
    }

    private static CrossShardTransfer transfer() {
        return new CrossShardTransfer(0, 1, SOURCE, 40, OWNER.getPublicKey()).sign(OWNER.getPrivateKey());
    }

    private static void await(CountDownLatch latch) {
        try {
            check(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Se agoto la espera de coordinacion.");
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Coordinacion interrumpida.", error);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
