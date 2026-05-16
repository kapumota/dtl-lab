package dltlab.sharding;

import dltlab.crypto.Hashing;
import dltlab.transaction.UTXO;

import java.security.PublicKey;

/** Solicitud para mover valor entre shards. */
public class CrossShardTransfer {
    private final String id;
    private final int sourceShardId;
    private final int targetShardId;
    private final UTXO sourceUtxo;
    private final long amount;
    private final PublicKey recipient;

    public CrossShardTransfer(int sourceShardId, int targetShardId, UTXO sourceUtxo, long amount, PublicKey recipient) {
        this.sourceShardId = sourceShardId;
        this.targetShardId = targetShardId;
        this.sourceUtxo = sourceUtxo;
        this.amount = amount;
        this.recipient = recipient;
        this.id = Hashing.hex(Hashing.sha256((sourceShardId + ":" + targetShardId + ":" + sourceUtxo.key() + ":" + amount).getBytes()));
    }

    public String id() { return id; }
    public int sourceShardId() { return sourceShardId; }
    public int targetShardId() { return targetShardId; }
    public UTXO sourceUtxo() { return sourceUtxo; }
    public long amount() { return amount; }
    public PublicKey recipient() { return recipient; }
}
