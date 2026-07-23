package core.transaction;

import java.time.LocalDate;
import java.time.Period;

public class RecurringExpense extends Expense {
    private Period period;

    public RecurringExpense(int id, double amount, LocalDate date, String note, String category, String wallet, String paymentMethod, Period period) {
        super(id, validateAmount(amount), date, note, category, wallet, paymentMethod);
        this.period = period;
    }

    /**
     * GOAL : Chỉnh sửa khi muốn tính toán ngày đến hạn tiếp theo dựa trên ngày hiện tại và chu kỳ định kỳ.
     * Hàm dưới mới chỉ tính toán ngày đến hạn tiếp theo dựa trên ngày giao dịch và chu kỳ định kỳ.
     * @return
     */
    public LocalDate nextDueDate() {
        return getDate().plus(period);
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
