package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.Receipt;

/** Contrato ejecutable del protocolo atomico cross-shard. */
public interface CrossShardProtocol {
    ProtocolResult begin(CrossShardTransfer transfer, int timeoutRounds);

    ProtocolResult deliverReceipt(String transferId);

    ProtocolResult commit(String transferId);

    ProtocolResult abort(String transferId, String reason);

    ProtocolResult timeout(String transferId);

    Receipt lockAndCreateReceipt(CrossShardTransfer transfer);

    boolean commitReceipt(Receipt receipt);
}
