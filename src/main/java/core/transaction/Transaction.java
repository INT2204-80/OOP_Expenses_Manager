package core.transaction;

import java.time.LocalDate;

import core.Category;
import core.TransactionType;
import core.wallet.Wallet;

public abstract class Transaction {
    private int id;
    private double amount;
    private LocalDate date;
    private String note;
    private Category category;
    private Wallet wallet;
    

    /**
     * co the dung getter/setter cho constructor, tuy nhien nen xem xet lai logic nay
     * @param id
     * @param amount
     * @param date
     * @param note
     * @param category
     * @param wallet
     */
    public Transaction(int id, double amount, LocalDate date, String note, Category category, Wallet wallet) {
        this.id = id;
        this.amount = validateAmount(amount);
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
     * so tien có dấu, income là dương, expense là âm.
     * @return.
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

    /**
     * tranh nguoi dung set amount < 0, vi amount input la duong.
     * <p>So tien luu trong Transaction luon khong am.
     * Dau cua giao dich duoc xu ly boi getSignedAmount().
     *
     * @param amount so tien can kiem tra
     * @return so tien neu hop le
     * @throws IllegalArgumentException neu so tien khong lon hon 0,
     *         la NaN hoac vo cuc
     */
    public static double validateAmount(double amount) {
            if ((amount < 0) || (!Double.isFinite(amount))) {
                throw new IllegalArgumentException("Income amount input cannot be negative");
            }
        return amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = validateAmount(amount);
        //this.amount = amount;// sua doi 23/7
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
}
