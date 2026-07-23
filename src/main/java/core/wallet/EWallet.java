package core.wallet;

import core.WalletType;

public class EWallet extends Wallet {
    private String provider;

    public EWallet(String name, double balance , String provider) {
        super(name, balance);
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public WalletType getWalletType() {
        return WalletType.EWALLET;
    }

}
