package dltlab.wallet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Registro pequeno para crear wallets de ejemplo en las demos. */
public class WalletRegistry {
    private final List<Wallet> wallets = new ArrayList<>();

    public Wallet create(String label) {
        Wallet wallet = new Wallet(label);
        wallets.add(wallet);
        return wallet;
    }

    public List<Wallet> getWallets() {
        return Collections.unmodifiableList(wallets);
    }
}
