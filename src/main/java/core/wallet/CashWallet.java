package core.wallet;

import core.WalletType;

public class CashWallet extends Wallet {
    public CashWallet(String name, double balance) {
        super(name, balance);
    }

    @Override
    public WalletType getWalletType() {
        return WalletType.CASH;
    }
    
}
