package core;

import java.time.LocalDate;

public abstract class Transaction {
    private int id;
    private double amount;
    private LocalDate date;
    private String note;
    private String category;
    private String wallet;
    

    /**
     * co the dung getter/setter cho constructor, tuy nhien nen xem xet lai logic nay
     * @param id
     * @param amount
     * @param date
     * @param note
     * @param category
     * @param wallet
     */
    public Transaction(int id, double amount, LocalDate date, String note, String category, String wallet) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.category = category;
        this.wallet = wallet;
    }
    /**
     * income / expense .
     * @return
     */

    public abstract TransactionType getType();

    /**
     * so tien có dấu, income là dương, expense là âm
     * @return
     */

    public abstract double getSignedAmount();

    public void printInfo() {
        System.out.println("ID: " + id);
        System.out.println("Amount: " + amount);
        System.out.println("Date: " + date);
        System.out.println("Note: " + note);
        System.out.println("Category: " + category);
        System.out.println("Wallet: " + wallet);
        System.out.println("Type: " + getType());
        System.out.println("Signed Amount: " + getSignedAmount());
    }

    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the transaction. The ID must be a non-negative integer.
     * Chinh ID , Chua chac co kha nang nguoi dung duoc set ID hay khong, nen can xem xet lai logic nay
     * @param id
     */

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getWallet() {
        return wallet;
    }

    public void setWallet(String wallet) {
        this.wallet = wallet;
    }

    public abstract TransactionType getType();

    public abstract double getSignedAmount();
}
