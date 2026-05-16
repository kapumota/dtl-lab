package dltlab.security;

import dltlab.app.DemoData;
import dltlab.blockchain.BlockChain;
import dltlab.transaction.Transaction;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXO;
import dltlab.transaction.UTXOPool;
import dltlab.wallet.Wallet;

/** Ataque: una wallet firma una entrada que pertenece a otra llave publica. */
public class InvalidSignatureAttack implements Attack {
    @Override
    public AttackResult run() {
        Wallet owner = new Wallet("dueno");
        Wallet attacker = new Wallet("atacante");
        Wallet recipient = new Wallet("receptor");
        BlockChain chain = DemoData.createChainWithGenesis(owner);
        UTXOPool pool = chain.getMaxHeightUTXOPool();
        UTXO coinbase = DemoData.firstUtxo(pool);
        Transaction.Output output = pool.getOutput(coinbase);

        Transaction forged = new Transaction();
        forged.addInput(coinbase.getTxHash(), coinbase.getOutputIndex());
        forged.addOutput(output.getValue() - 1000L, recipient.getPublicKey());
        forged.signInput(0, attacker.getPrivateKey());
        forged.finalizeTransaction();

        boolean valid = new TxValidator(pool).isValidTx(forged);
        return new AttackResult("Firma invalida", !valid,
                "El atacante firmo con una llave privada distinta. Transaccion valida: " + valid + ".");
    }
}
