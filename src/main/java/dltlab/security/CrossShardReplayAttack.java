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
        ShardManager manager = new ShardManager(2);
        UTXO source = new UTXO(Hashing.sha256("origen".getBytes()), 0);
        manager.getShard(0).getUtxoPool().addUTXO(source, new Transaction.Output(5000L, alice.getPublicKey()));

        CrossShardTransfer transfer = new CrossShardTransfer(0, 1, source, 3000L, bob.getPublicKey());
        Receipt receipt = manager.lockAndCreateReceipt(transfer);
        boolean first = manager.commitReceipt(receipt);
        boolean replay = manager.commitReceipt(receipt);

        return new AttackResult("Replay cross-shard", first && !replay,
                "Primer commit aceptado: " + first + ". Segundo commit con el mismo recibo aceptado: " + replay + ".");
    }
}
