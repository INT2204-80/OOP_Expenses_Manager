package core;

import java.time.LocalDate;

public class Income extends Transaction {
    private String source;

    public Income(int id, double amount, LocalDate date, String note, String category, String wallet, String source) {
        super(id, validateAmount(amount), date, note, category, wallet);
        this.source = source;
    }

    /**
     * tranh nguoi dung set amount < 0, vi amount input la duong.
     * @param amount
     * @return
     */
    public static double validateAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Income amount input cannot be negative");
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
        return TransactionType.INCOME;
    }

    @Override
    public double getSignedAmount() {
        return getAmount();
    }

    @Override
    public void printInfo() {
        super.printInfo();
        System.out.println("Source: " + source);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
