package core;

import java.time.LocalDate;

public class Expense extends Transaction {
    private String paymentMethod;

    public Expense(int id, double amount, LocalDate date, String note, String category, String wallet, String paymentMethod) {
        super(id, validateAmount(amount), date, note, category, wallet);
        this.paymentMethod = paymentMethod;
    }

    /**
     * tranh nguoi dung set amount < 0, vi amount input la duong.
     * @param amount
     * @return
     */

    public static double validateAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Expense amount input cannot be negative");
        }
        return amount;
    }

    @Override
    public void setAmount(double amount) {
        amount = validateAmount(amount);
        super.setAmount(amount);
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
