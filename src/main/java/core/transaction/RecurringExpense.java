package core.transaction;

import java.time.LocalDate;
import java.time.Period;

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
