package core.wallet;

import core.WalletType;

public abstract class Wallet {
    private String name;
    private double balance = 0.0;


    public Wallet(String name, double balance) {
        this.name = name;
        this.balance = validateAmount(balance);
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

    //validate balance >= 0, neu < 0 thi throw exception

    public static double validateAmount(double amount) {
        try {
            if (amount < 0) {
                throw new IllegalArgumentException("Amount input cannot be negative");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            // You can choose to handle the exception differently, e.g., return a default value or rethrow it.
        }
        return amount;
    } 

    public double setBalance(double balance) {
        this.balance = validateAmount(balance);
        return this.balance;
    }

    public void deposit(double amount) {
        amount = validateAmount(amount);
        this.balance += amount;
    }

    public void withdraw(double amount) {
        amount = validateAmount(amount);
        try {
            if (amount > balance) {
                throw new IllegalArgumentException("Insufficient balance in wallet.");
            }
            this.balance -= amount;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            // You can choose to handle the exception differently, e.g., return a default value or rethrow it.
        }
    }

    public abstract WalletType getWalletType();
}
