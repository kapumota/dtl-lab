package dltlab.sharding;

import java.security.PublicKey;

/** Prueba educativa de que un shard origen bloqueo valor para un shard destino. */
public record Receipt(String receiptId, String transferId, int sourceShardId, int targetShardId,
                      String sourceUtxoKey, long amount, PublicKey recipient) {}
