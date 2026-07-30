package core.wallet;

import core.WalletType;

/**
 * Lop truu tuong dai dien cho mot vi hoac tai khoan tien.
 *
 * <p>Lop nay quan ly ten vi, so du va dinh nghia cac hanh vi chung
 * nhu nap tien, rut tien va lay loai vi.
 */
public abstract class Wallet {
    private String name;
    private double balance = 0.0;

    /**
     * Khoi tao mot vi voi ten va so du ban dau.
     *
     * @param name ten cua vi
     * @param balance so du ban dau
     * @throws IllegalArgumentException neu so du am, NaN hoac vo cuc
     */
    public Wallet(String name, double balance) {
        this.name = name;
        this.balance = validateAmount(balance);
    }

    /**
     * Lay ten cua vi.
     *
     * @return ten cua vi
     */
    public String getName() {
        return name;
    }

    /**
     * Cap nhat ten cua vi.
     *
     * @param name ten moi cua vi
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Lay so du hien tai.
     *
     * @return so du hien tai
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Kiem tra mot gia tri tien te hop le.
     *
     * @param amount gia tri can kiem tra
     * @return gia tri neu hop le
     * @throws IllegalArgumentException neu gia tri am, NaN hoac vo cuc
     */
    public static double validateAmount(double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException(
                    "Amount cannot be negative, NaN or infinite");
        }
        return amount;
    }

    /**
     * Cap nhat so du cua vi.
     *
     * @param balance so du moi
     * @return so du sau khi cap nhat
     * @throws IllegalArgumentException neu so du am, NaN hoac vo cuc
     */
    public double setBalance(double balance) {
        this.balance = validateAmount(balance);
        return this.balance;
    }

    /**
     * Nap tien vao vi.
     *
     * @param amount so tien can nap
     * @throws IllegalArgumentException neu so tien am, NaN hoac vo cuc
     */
    public void deposit(double amount) {
        amount = validateAmount(amount);
        setBalance(getBalance() + amount);
    }

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
     * Lay loai vi.
     *
     * @return loai vi tuong ung
     */
    public abstract WalletType getWalletType();
}