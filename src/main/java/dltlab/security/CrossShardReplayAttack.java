package dltlab.security;

import dltlab.crypto.Hashing;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.Receipt;
import dltlab.sharding.ShardManager;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.wallet.Wallet;

/** Ataque: reutilizar el mismo recibo cross-shard dos veces. */
public class CrossShardReplayAttack implements Attack {
    @Override
    public AttackResult run() {
        Wallet alice = new Wallet("alice");
        Wallet bob = new Wallet("bob");
        ShardManager.Setup setup = new ShardManager.Setup(2);
        UTXO source = new UTXO(Hashing.sha256("origen".getBytes()), 0);
        setup.addInitialUtxo(0, source, new Transaction.Output(5000L, alice.getPublicKey()));
        ShardManager manager = setup.start();

        CrossShardTransfer transfer = new CrossShardTransfer(0, 1, source, 3000L, bob.getPublicKey()).sign(alice.getPrivateKey());
        Receipt receipt = manager.lockAndCreateReceipt(transfer);
        boolean first = manager.commitReceipt(receipt);
        boolean replay = manager.commitReceipt(receipt);

        return new AttackResult("Replay cross-shard", first && !replay,
                "Primer commit aceptado: " + first + ". Segundo commit con el mismo recibo aceptado: " + replay + ".");
    }
}
