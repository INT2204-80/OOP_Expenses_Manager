package core.wallet;

import core.WalletType;

/**
 * Vi dien tu cua cac nha cung cap nhu Momo hoac ZaloPay.
 */
public class EWallet extends Wallet {
    private String provider;

    /**
     * Khoi tao vi dien tu chua co nha cung cap.
     *
     * @param name ten cua vi
     * @param balance so du ban dau
     * @throws IllegalArgumentException neu so du ban dau khong hop le
     */
    public EWallet(String name, double balance) {
        super(name, balance);
    }

    /**
     * Khoi tao vi dien tu voi nha cung cap.
     *
     * @param name ten cua vi
     * @param balance so du ban dau
     * @param provider nha cung cap vi dien tu
     * @throws IllegalArgumentException neu so du ban dau khong hop le
     */
    public EWallet(String name, double balance, String provider) {
        super(name, balance);
        this.provider = provider;
    }

    /**
     * Lay nha cung cap vi dien tu.
     *
     * @return nha cung cap vi dien tu
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Cap nhat nha cung cap vi dien tu.
     *
     * @param provider nha cung cap moi
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Rut tien khoi vi dien tu ma khong tinh phi.
     *
     * @param amount so tien can rut
     * @throws IllegalArgumentException neu so tien khong duong, NaN hoac vo cuc
     * @throws IllegalStateException neu so du khong du
     */

    /**
     * Lay loai vi dien tu.
     *
     * @return {@link WalletType#EWALLET}
     */
    @Override
    public WalletType getWalletType() {
        return WalletType.EWALLET;
    }
}