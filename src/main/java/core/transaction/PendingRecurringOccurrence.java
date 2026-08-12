package core.transaction;

import java.time.LocalDate;

import core.TransactionType;

/**
 * Dai dien cho MOT lan xuat hien (occurrence) trong tuong lai cua mot
 * {@link RecurringExpense}, chua toi han (ngay xuat hien > hom nay).
 *
 * <p><b>Quan trong:</b> lop nay ke thua thang tu {@link Transaction}, KHONG
 * qua {@link Expense}. Ly do: constructor cua {@code Expense} tu dong goi
 * {@code wallet.withdraw(...)} ngay khi khoi tao - neu dung Expense de bieu
 * dien mot occurrence "pending" thi se vo tinh tru tien that vao vi ngay
 * lap tuc, sai voi yeu cau "chi hien pending, chua toi han thi khong tru
 * vao balance". Vi vay lop nay tu dinh nghia getType()/getSignedAmount()
 * ma khong dung toi wallet.withdraw()/deposit().
 *
 * <p>Doi tuong nay KHONG duoc luu vao DB va KHONG duoc them vao
 * {@code wallet.getTransactions()} (danh sach that). No chi duoc tao ra
 * tam thoi (trong bo nho) de:
 * <ul>
 *   <li>Hien thi the "Pending" trong danh sach Transactions khi xem theo ky;</li>
 *   <li>Duoc WalletOverviewCalculator.compute() cong vao total income/expense/change
 *       cua ky dang xem (vi compute() doc theo getType()/getAmount(), khong
 *       phan biet Expense that hay occurrence ao).</li>
 * </ul>
 * id luon la 0 vi khong ton tai trong DB. Khong ho tro Sua/Xoa (xu ly o tang UI).
 */
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
