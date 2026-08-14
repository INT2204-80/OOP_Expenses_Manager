package core.transaction;

import java.time.LocalDate;

import core.Category;
import core.TransactionType;
import core.wallet.Wallet;

public class Income extends Transaction {
    private String source;

    public Income(int id, double amount, LocalDate date, String note, Category category, Wallet wallet, String source) {
        super(id, amount, date, note, category, wallet);
        this.source = source;
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
