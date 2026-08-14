package core.transaction;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import core.Category;
import core.wallet.Wallet;

public class RecurringExpense extends Expense {
    private Period period;
    private LocalDate currentDueDate;
    private int passedPeriods = 0;
    /** Ngay ket thuc lap lai (tuy chon). Null nghia la lap lai vo thoi han. */
    private LocalDate endDate;

    public RecurringExpense(int id, double amount, LocalDate date, String note, Category category, Wallet wallet, String paymentMethod, Period period) {
        this(id, amount, date, note, category, wallet, paymentMethod, period, null);
    }

    public RecurringExpense(int id, double amount, LocalDate date, String note, Category category, Wallet wallet, String paymentMethod, Period period, LocalDate endDate) {
        super(id, amount, date, note, category, wallet, paymentMethod);
        if (period == null) {
            throw new IllegalArgumentException("Period cannot be null");
        }
        if (endDate != null && date != null && endDate.isBefore(date)) {
            throw new IllegalArgumentException("Ngay ket thuc khong the truoc ngay bat dau");
        }
        this.period = period;
        this.endDate = endDate;
        this.currentDueDate = date.plus(period);
    }

    /**
     * Tính toán ngày đến hạn tiếp theo dựa trên chu kỳ.
     * Nếu có endDate, sẽ không vượt qua endDate.
     * @return Ngày đến hạn tiếp theo.
     */

    public LocalDate nextDueDate() {
        return nextDueDate(LocalDate.now());
    }

    public LocalDate nextDueDate(LocalDate referenceDate) {
        currentDueDate = getDate().plus(
                period.multipliedBy(passedPeriods + 1));

        while (!currentDueDate.isAfter(referenceDate) && (endDate == null || !currentDueDate.isAfter(endDate))) {
            passedPeriods++;
            currentDueDate = getDate().plus(
                    period.multipliedBy(passedPeriods + 1));
        }

        if (endDate != null && currentDueDate.isAfter(endDate)) {
            return null;
        }

        return currentDueDate;
    }
    public int getPassedPeriods() {
        return passedPeriods;
    }

    /**
     * Tinh danh sach cac ngay den han (occurrence) cua giao dich lap lai nay
     * nam trong khoang [rangeStart, rangeEnd]. Cac ky co ngay den han sau
     * endDate (neu co) se khong duoc tinh vao.
     *
     * <p>Khac voi {@link #nextDueDate()}, ham nay THUAN TUY (khong mutate
     * passedPeriods) - chi dung de du phong hien thi/tinh Overview cho ky
     * dang xem, khong lam thay doi trang thai that cua giao dich.
     *
     * <p>Bat dau tinh tu ky (passedPeriods + 1) vi cac ky <= passedPeriods
     * da duoc chuyen thanh Expense that (qua backfill luc them moi, hoac qua
     * TransactionDAO.processRecurringExpenses() luc load lai tu DB) - tranh
     * trung lap voi giao dich that.
     *
     * @return danh sach ngay den han, sap xep tang dan, rong neu khong co ky nao
     *         roi vao khoang da cho
     */
    public List<LocalDate> getOccurrencesBetween(LocalDate rangeStart, LocalDate rangeEnd) {
        List<LocalDate> result = new ArrayList<>();
        if (rangeStart == null || rangeEnd == null || rangeStart.isAfter(rangeEnd)) {
            return result;
        }

        int k = passedPeriods + 1;
        LocalDate occDate = getDate().plus(period.multipliedBy(k));

        // Gioi han so vong lap de tranh treo neu period bang 0 (khong xay ra
        // trong thuc te vi UI chi cho chon Daily/Weekly/Monthly/Yearly).
        int safety = 0;
        while (!occDate.isAfter(rangeEnd) && safety < 100000) {
            if (endDate != null && occDate.isAfter(endDate)) {
                break;
            }
            if (!occDate.isBefore(rangeStart)) {
                result.add(occDate);
            }
            k++;
            occDate = getDate().plus(period.multipliedBy(k));
            safety++;
        }

        return result;
    }

    public void setPassedPeriods(int passedPeriods) {
        this.passedPeriods = passedPeriods;
    }

    @Override
    public void printInfo() {
        super.printInfo();
        System.out.println("Period: " + period);
        System.out.println("End Date: " + endDate);
        System.out.println("Next Due Date: " + nextDueDate());
    }

    public Period getPeriod() {
        return period;
    }

    /**
     * Thiết lập chu kỳ lặp lại.
     * @param period Chu kỳ mới (không được null).
     */
    
    public void setPeriod(Period period) {
        if (period == null) {
            throw new IllegalArgumentException("Period cannot be null");
        }
        this.period = period;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Dat ngay ket thuc lap lai. Truyen null de lap lai vo thoi han.
     * @param endDate ngay ket thuc, phai khong truoc ngay bat dau neu khac null
     */
    public void setEndDate(LocalDate endDate) {
        if (endDate != null && getDate() != null && endDate.isBefore(getDate())) {
            throw new IllegalArgumentException("Ngay ket thuc khong the truoc ngay bat dau");
        }
        this.endDate = endDate;
    }
}
