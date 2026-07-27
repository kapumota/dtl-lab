package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardStatus;

/** Resultado comun de una operacion del protocolo cross-shard. */
public interface ProtocolResult {
    String transferId();
    boolean success();
    CrossShardStatus status();
    String message();
}
