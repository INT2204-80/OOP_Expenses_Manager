package core;

import java.time.LocalDate;

public class Income extends Transaction {
    private String source;

    public Income(int id, double amount, LocalDate date, String note, String category, String wallet, String source) {
        super(id, amount, date, note, category, wallet);
        this.source = source;
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
