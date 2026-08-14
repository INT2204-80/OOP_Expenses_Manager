package core.wallet;

import core.WalletType;

/**
 * Vi tien mat, cho phep rut tien ma khong tinh phi giao dich.
 */
public class CashWallet extends Wallet {

    /**
     * Khoi tao vi tien mat.
     *
     * @param name ten cua vi
     * @param balance so du ban dau
     * @throws IllegalArgumentException neu so du ban dau khong hop le
     */
    public CashWallet(String name, double balance) {
        super(name, balance);
    }

    public CashWallet(String name, double balance, String currency) {
        super(name, balance, currency);
    }

    /**
     * Rut tien mat khoi vi ma khong tinh phi.
     *
     * @param amount so tien can rut
     * @throws IllegalArgumentException neu so tien khong duong, NaN hoac vo cuc
     * @throws IllegalStateException neu so du khong du
     */
    @Override
    public void withdraw(double amount) {
        super.withdraw(amount);
    }

    /**
     * Lay loai vi tien mat.
     *
     * @return {@link WalletType#CASH}
     */
    @Override
    public WalletType getWalletType() {
        return WalletType.CASH;
    }
}