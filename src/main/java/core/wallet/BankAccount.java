package core.wallet;

import core.WalletType;

/**
 * Tai khoan ngan hang cho phep rut tien ma khong tinh phi giao dich.
 */
public class BankAccount extends Wallet {
    private String bankName;
    private String accountNumber;

    /**
     * Khoi tao tai khoan ngan hang.
     *
     * @param name ten hien thi cua tai khoan
     * @param balance so du ban dau
     * @param bankName ten ngan hang
     * @param accountNumber so tai khoan
     * @throws IllegalArgumentException neu so du ban dau khong hop le
     */
    public BankAccount(
            String name,
            double balance,
            String bankName,
            String accountNumber) {
        super(name, balance);
        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }

    /**
     * Lay ten ngan hang.
     *
     * @return ten ngan hang
     */
    public String getBankName() {
        return bankName;
    }

    /**
     * Cap nhat ten ngan hang.
     *
     * @param bankName ten ngan hang moi
     */
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    /**
     * Lay so tai khoan.
     *
     * @return so tai khoan
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Cap nhat so tai khoan.
     *
     * @param accountNumber so tai khoan moi
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * Rut tien khoi tai khoan ma khong tinh phi giao dich.
     *
     * @param amount so tien can rut
     * @throws IllegalArgumentException neu so tien khong duong, NaN hoac vo cuc
     * @throws IllegalStateException neu so du khong du
     */
    @Override
    public void withdraw(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException(
                    "Withdrawal amount must be positive and finite");
        }

        if (amount > getBalance()) {
            throw new IllegalStateException("Insufficient balance");
        }

        setBalance(getBalance() - amount);
    }

    /**
     * Lay loai tai khoan ngan hang.
     *
     * @return {@link WalletType#BANK}
     */
    @Override
    public WalletType getWalletType() {
        return WalletType.BANK;
    }
}