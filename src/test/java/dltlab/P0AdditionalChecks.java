package dltlab;

import dltlab.blockchain.Block;
import dltlab.blockchain.BlockChain;
import dltlab.crypto.Hashing;
import dltlab.crypto.SignatureUtils;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.Receipt;
import dltlab.sharding.ShardManager;
import dltlab.transaction.FeeCalculator;
import dltlab.transaction.Transaction;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXO;
import dltlab.transaction.UTXOPool;
import dltlab.wallet.Wallet;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static dltlab.P0RegressionTest.*;

final class P0AdditionalChecks {
    private static final Wallet OWNER = new Wallet("propietario");
    private static final Wallet OTHER = new Wallet("otro");
    private static final UTXO SOURCE = new UTXO(new byte[]{1, 2, 3}, 0);
    private static final UTXO SECOND = new UTXO(new byte[]{4, 5, 6}, 0);

    private P0AdditionalChecks() {}

    static void run() {
        signedReceipt();
        exactAmounts();
        coinbase();
    }

    private static void signedReceipt() {
        ShardManager.Setup setup = new ShardManager.Setup(2);
        setup.addInitialUtxo(0, SOURCE, new Transaction.Output(100, OWNER.getPublicKey()));
        ShardManager manager = setup.start();
        var unsigned = new CrossShardTransfer(0, 1, SOURCE, 30, OTHER.getPublicKey());
        var before = ledger(manager);
        reject(() -> manager.lockAndCreateReceipt(unsigned));
        reject(() -> manager.getProtocol().begin(unsigned.sign(OTHER.getPrivateKey()), 3));
        byte[] signature = SignatureUtils.sign(OWNER.getPrivateKey(), unsigned.signingBytes());
        for (var modified : List.of(
                new CrossShardTransfer(0, 1, SOURCE, 31, OTHER.getPublicKey(), signature),
                new CrossShardTransfer(0, 1, SOURCE, 30, OWNER.getPublicKey(), signature),
                new CrossShardTransfer(0, 1, SECOND, 30, OTHER.getPublicKey(), signature),
                new CrossShardTransfer(1, 0, SOURCE, 30, OTHER.getPublicKey(), signature))) {
            reject(() -> manager.beginAtomicTransfer(modified, 3));
        }
        check(before.equals(ledger(manager)) && manager.getSessions().isEmpty(), "Rechazos sin efectos.");

        var authorized = new CrossShardTransfer(0, 1, SOURCE, 30, OTHER.getPublicKey(), signature);
        String oldId = Hashing.hex(Hashing.sha256(("0:1:" + SOURCE.key() + ":30")
                .getBytes(StandardCharsets.UTF_8)));
        check(authorized.id().equals(unsigned.id()) && authorized.id().startsWith("v2:")
                && !authorized.id().equals(oldId), "Identidad canonica y versionada.");
        Receipt receipt = manager.lockAndCreateReceipt(authorized);
        var pending = ledger(manager);
        Receipt altered = new Receipt(receipt.receiptId(), receipt.transferId(), 0, 1,
                SOURCE.key(), 31, OTHER.getPublicKey());
        check(!manager.commitReceipt(altered) && !manager.commitAtomicTransfer(oldId)
                && pending.equals(ledger(manager)), "Recibo alterado o ID antiguo modificaron el ledger.");

        manager.getShard(0).getUtxoPool().removeUTXO(SOURCE);
        manager.getShard(0).unlockUtxo(SOURCE.key());
        manager.getShard(1).markReceiptConsumed(receipt.receiptId());
        manager.getSession(authorized.id()).markAborted("Copia local.");
        try {
            setup.addInitialUtxo(1, SECOND, new Transaction.Output(1, OTHER.getPublicKey()));
            throw new AssertionError("La carga inicial debe quedar cerrada.");
        } catch (IllegalStateException expected) {
            // El gestor ya fue entregado y la capacidad de carga ya no puede usarse.
        }
        check(pending.equals(ledger(manager))
                && manager.getSession(authorized.id()).status() == CrossShardStatus.RECEIPT_CREATED,
                "M09: las copias y la carga inicial no deben alterar una sesion activa.");

        check(manager.commitReceipt(receipt), "Se debe acreditar el recibo emitido.");
        check(!manager.getShard(0).getUtxoPool().contains(SOURCE)
                && manager.getShard(0).getUtxoPool().totalValue() == 70
                && manager.getShard(1).getUtxoPool().totalValue() == 30,
                "El debito, el cambio y el credito deben conservar el valor.");
        var committed = ledger(manager);
        check(!manager.commitReceipt(receipt) && committed.equals(ledger(manager)), "Replay sin efectos.");
    }

    private static void exactAmounts() {
        UTXOPool pool = new UTXOPool();
        pool.addUTXO(SOURCE, new Transaction.Output(Long.MAX_VALUE, OWNER.getPublicKey()));
        pool.addUTXO(SECOND, new Transaction.Output(1, OWNER.getPublicKey()));
        TxValidator validator = new TxValidator(pool);
        var before = contents(pool);
        for (Transaction invalid : List.of(new Transaction(), outputs(0), spend(outputs(), SOURCE),
                spend(outputs(-1), SOURCE), spend(outputs(Long.MAX_VALUE, Long.MAX_VALUE), SOURCE),
                spend(outputs(1), SOURCE, SECOND), spend(outputs(2), SECOND))) {
            check(!validator.isValidTx(invalid), "Se acepto una transaccion monetariamente invalida.");
            reject(() -> validator.applyTransaction(invalid));
            check(before.equals(contents(validator.getUtxoPool())), "La aplicacion invalida altero el ledger.");
        }
        check(FeeCalculator.fee(spend(outputs(Long.MAX_VALUE, 1), SOURCE), pool) == Long.MIN_VALUE,
                "El calculo de fee debe rechazar el overflow.");
        var valid = spend(outputs(Long.MAX_VALUE - 1, 1), SOURCE);
        check(FeeCalculator.fee(valid, pool) == 0 && validator.isValidTx(valid), "Limite valido.");
        validator.applyTransaction(valid);
        check(!validator.getUtxoPool().contains(SOURCE) && validator.getUtxoPool().size() == 3,
                "El gasto valido debe consumir la entrada y crear sus dos salidas.");
        reject(() -> OWNER.createSpend(SECOND, pool.getOutput(SECOND), OTHER.getPublicKey(),
                Long.MIN_VALUE, 0));
    }

    private static Transaction spend(Transaction tx, UTXO... inputs) {
        for (UTXO input : inputs) tx.addInput(input.getTxHash(), input.getOutputIndex());
        for (int i = 0; i < inputs.length; i++) tx.signInput(i, OWNER.getPrivateKey());
        return tx;
    }

    private static void coinbase() {
        Block genesis = new Block(null, OWNER.getPublicKey(), List.of(), 1);
        BlockChain chain = new BlockChain(genesis);
        var before = contents(chain.getMaxHeightUTXOPool());
        Block block = new Block(genesis.getHash(), OTHER.getPublicKey(), List.of(), 2);
        try {
            block.getCoinbase().addOutput(Long.MAX_VALUE, OTHER.getPublicKey());
            throw new AssertionError("Una coinbase comprometida no debe aceptar salidas adicionales.");
        } catch (IllegalStateException expected) {
            check(chain.getMaxHeight() == 1 && chain.getKnownBlocks().size() == 1
                    && before.equals(contents(chain.getMaxHeightUTXOPool())),
                    "La mutacion rechazada debe conservar la cadena.");
        }
        check(chain.addBlock(block),
                "Una coinbase dedicada con recompensa fija debe seguir siendo valida.");
    }

    private static void reject(Runnable operation) {
        try {
            operation.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Se esperaba un rechazo sin mutaciones.");
    }
}
