package dltlab.security;

import dltlab.app.DemoData;
import dltlab.blockchain.BlockChain;
import dltlab.transaction.Transaction;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXO;
import dltlab.transaction.UTXOPool;
import dltlab.wallet.Wallet;

/** Ataque: dos transacciones intentan gastar el mismo UTXO. */
public class DoubleSpendAttack implements Attack {
    @Override
    public AttackResult run() {
        Wallet miner = new Wallet("minero");
        Wallet alice = new Wallet("alice");
        Wallet bob = new Wallet("bob");
        BlockChain chain = DemoData.createChainWithGenesis(miner);
        UTXOPool pool = chain.getMaxHeightUTXOPool();
        UTXO coinbase = DemoData.firstUtxo(pool);
        Transaction.Output output = pool.getOutput(coinbase);

        Transaction tx1 = miner.createSpend(coinbase, output, alice.getPublicKey(), 10_000_000L, 1000L);
        Transaction tx2 = miner.createSpend(coinbase, output, bob.getPublicKey(), 10_000_000L, 2000L);

        TxValidator validator = new TxValidator(pool);
        Transaction[] accepted = validator.handleTxs(new Transaction[]{tx1, tx2});
        boolean defenseWorked = accepted.length == 1;
        return new AttackResult("Doble gasto", defenseWorked,
                "Se enviaron dos transacciones que gastan el mismo UTXO. Aceptadas: " + accepted.length + ".");
    }
}
