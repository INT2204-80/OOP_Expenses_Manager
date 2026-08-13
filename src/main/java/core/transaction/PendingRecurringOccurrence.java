package core.transaction;

import java.time.LocalDate;

import core.TransactionType;

public class PendingRecurringOccurrence extends Transaction {

    private final RecurringExpense source;

    public PendingRecurringOccurrence(RecurringExpense source, LocalDate occurrenceDate) {
        super(0, source.getAmount(), occurrenceDate, source.getNote(), source.getCategory(), source.getWallet());
        this.source = source;
    }

    /**
     * RecurringExpense goc da sinh ra occurrence nay (dung neu can tham chieu nguoc,
     * vi du de biet chu ky/paymentMethod).
     */
    public RecurringExpense getSource() {
        return source;
    }

    @Override
    public TransactionType getType() {
        // Hien tai RecurringExpense chi duoc tao cho danh muc EXPENSE
        // (xem TransactionDialogFactory: checkbox "Lap lai" bi disable voi danh muc INCOME).
        return TransactionType.EXPENSE;
    }

    @Override
    public double getSignedAmount() {
        return -getAmount();
    }
}
