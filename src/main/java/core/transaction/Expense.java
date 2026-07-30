package core.transaction;

import java.time.LocalDate;

import core.TransactionType;
import core.wallet.Wallet;

public class Expense extends Transaction {
    private String paymentMethod;

    public Expense(int id, double amount, LocalDate date, String note, String category, Wallet wallet, String paymentMethod) {
        super(id, validateAmount(amount), date, note, category, wallet);
        double validatedAmount = validateAmount(amount);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if (validatedAmount > wallet.getBalance()) {
            throw new IllegalArgumentException("Expense amount cannot exceed wallet balance");
        }
        wallet.withdraw(validatedAmount);
        this.paymentMethod = paymentMethod;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.EXPENSE;
    }

    @Override
    public double getSignedAmount() {
        return -getAmount();
    }

    @Override
    public void printInfo() {
        super.printInfo();
        System.out.println("Payment Method: " + paymentMethod);
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
