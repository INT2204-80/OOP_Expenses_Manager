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

    public RecurringExpense(int id, double amount, LocalDate date, String note, Category category, Wallet wallet, String paymentMethod, Period period) {
        super(id, amount, date, note, category, wallet, paymentMethod);
        if (period == null) {
            throw new IllegalArgumentException("Period cannot be null");
        }
        this.period = period;
        this.currentDueDate = date.plus(period);
    }

    /**
     * GOAL : Chỉnh sửa khi muốn tính toán ngày đến hạn tiếp theo dựa trên ngày hiện tại và chu kỳ định kỳ.
     * @return
     */

    public LocalDate nextDueDate() {
        LocalDate today = LocalDate.now();

        currentDueDate = getDate().plus(
                period.multipliedBy(passedPeriods + 1));

        while (today.isAfter(currentDueDate)) {
            passedPeriods++;
            currentDueDate = getDate().plus(
                    period.multipliedBy(passedPeriods + 1));
        }

        return currentDueDate;
    }
    public int getPassedPeriods() {
        return passedPeriods;
    }

    /**
     * Tinh danh sach cac ngay den han (occurrence) cua giao dich lap lai nay
     * nam trong khoang [rangeStart, rangeEnd].
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
        System.out.println("Next Due Date: " + nextDueDate());
    }

    public Period getPeriod() {
        return period;
    }

    /**
     * Sets the period for the recurring expense. The period must be a non-null value.
     * @param period
     */
    
    public void setPeriod(Period period) {
        if (period == null) {
            throw new IllegalArgumentException("Period cannot be null");
        }
        this.period = period;
    }
}
