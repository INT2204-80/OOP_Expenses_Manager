package core;

public abstract class Wallet {
    private String name;
    private double balance;

    public Wallet(String name, double balance) {
        this.name = name;
        setBalance(balance);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("Số dư ví không được phép là số âm!");
        }
        this.balance = balance;
    }

    public void deposit(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Số tiền nạp vào phải lớn hơn 0!");
        }
        this.balance += amount;
    }

    public abstract void withdraw(double amount);

    public abstract TransactionType getWalletType();
}
