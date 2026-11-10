package dltlab.security;

import dltlab.crypto.Hashing;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.ShardManager;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.wallet.Wallet;

/** Ataque/fallo: el shard destino queda offline y la transferencia debe expirar sin perder fondos. */
public class CrossShardTimeoutAttack implements Attack {
    @Override
    public AttackResult run() {
        Wallet alice = new Wallet("alice");
        Wallet bob = new Wallet("bob");
        ShardManager.Setup setup = new ShardManager.Setup(2, 4, 3);
        UTXO source = new UTXO(Hashing.sha256("timeout-origen".getBytes()), 0);
        setup.addInitialUtxo(0, source, new Transaction.Output(5000L, alice.getPublicKey()));
        ShardManager manager = setup.start();

        CrossShardTransfer transfer = new CrossShardTransfer(0, 1, source, 3000L, bob.getPublicKey()).sign(alice.getPrivateKey());
        manager.beginAtomicTransfer(transfer, 1);
        manager.setShardOnline(1, false);
        // El destino no produce commit; el protocolo debe expirar la sesion y liberar el UTXO origen.
        boolean committed = false;
        manager.advanceRounds(2);

        boolean defenseWorked = !committed
                && manager.getSession(transfer.id()).status() == CrossShardStatus.TIMED_OUT
                && !manager.getShard(0).isLocked(source.key())
                && manager.getShard(0).getUtxoPool().contains(source)
                && manager.getShard(1).getUtxoPool().size() == 0;

        return new AttackResult("Timeout cross-shard", defenseWorked,
                "Destino offline. Commit aceptado: " + committed
                        + ". Estado final: " + manager.getSession(transfer.id()).status()
                        + ". UTXO origen bloqueado: " + manager.getShard(0).isLocked(source.key()) + ".");
    }
}
