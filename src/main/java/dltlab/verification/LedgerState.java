package dltlab.verification;

import dltlab.blockchain.BlockChain;
import dltlab.sharding.ShardManager;

/** Estado observable que revisan los invariantes. */
public record LedgerState(BlockChain blockChain, ShardManager shardManager) {}
