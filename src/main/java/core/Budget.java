package core;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import core.transaction.Expense;
import core.transaction.Transaction;

/**
 * Lop quan ly ngan sach cho 1 danh muc chi tieu hoac toan bo ung dung 
 */

public class Budget {
    private int id;
    private String name;
    private double limitAmount;
    private double currentSpent;
    private Category category;
    private Period period;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Khoi tao ngan sach theo ngay bat dau va ngay ket thuc cu the
     */
    public Budget(int id, String name, double limitAmount, Category category, LocalDate startDate, LocalDate endDate) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten ngan sach khong duoc de trong");
        }

        if (limitAmount <= 0 || !Double.isFinite(limitAmount)) {
            throw new IllegalArgumentException("Han muc ngan sach phai la so duong hop le");
        }

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngay ket thuc khong the truoc ngay bat dau");
        }

        this.id = id;
        this.name = name.trim();
        this.limitAmount = limitAmount;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.currentSpent = 0.0;
        this.period = Period.MONTHLY;
    }

    /**
     * Khoi tao ngan sach theo chu ky (Period) tu dong tinh endDate
     */
    public Budget(int id, String name, double limitAmount, Category category, Period period, LocalDate startDate) {
        this(id, name, limitAmount, category, startDate, 
            (startDate != null && period != null) ? startDate.plus(period.toJavaPeriod()).minusDays(1) : null);

        this.period = period;
    }

    /**
     * Khoi tao ngan sach day du cac tham so (bao gom so tien da chi)
     */
    public Budget(int id, String name, double limitAmount, double currentSpent, Category category, Period period, LocalDate startDate, LocalDate endDate) {
        this(id, name, limitAmount, category, startDate, endDate);
        this.period = period;
        setSpentAmount(currentSpent);
    }

    /**
     * Kiem tra ngan sach da bi vuot han muc hay chua
     * @return true neu so tien da chi vuot qua han muc 
     */
    public boolean isExceed() {
        return currentSpent > limitAmount;
    }

    /**
     * Lay so tien con lai co the chi tieu trong han muc
     * @return so tien con lai (limit - spent). Tra ve 0.0 neu vuot qua han muc
     */
    public double getRemainingAmount() {
        double remaining = limitAmount - currentSpent;
        return Math.max(0.0, remaining);
    }

    /**
     * Tinh ty le phan tram han muc ngan sach da su dung
     * @return ty le % 
     */
    public double getUsagePercentage() {
        if (limitAmount == 0) {
            return 0.0;
        }
        return (currentSpent / limitAmount) * 100;
    }

    /**
     * Tinh han muc chi tieu trung binh moi ngay cho den khi het han ngan sach
     * @param currentDate ngay hien tai can tinh
     * @return so tien co the chi moi ngay. Tra ve 0.0 neu qua han hoac da vuot han muc
     */
    public double calcDailyAllowance(LocalDate currentDate) {
        if (currentDate == null || isExceed()) {
            return 0.0;
        }

        LocalDate targetEndDate = (this.endDate != null)
            ? this.endDate
            : currentDate.withDayOfMonth(currentDate.lengthOfMonth());

        if (currentDate.isAfter(targetEndDate)) {
            return 0.0;
        }

        long daysRemaining = ChronoUnit.DAYS.between(currentDate, targetEndDate) + 1;

        if (daysRemaining <= 0) {
            return 0.0;
        }

        return getRemainingAmount() / daysRemaining;
    }

    /**
     * Kiem tra xem giao dich co khop voi ngan sach nay khong
     * @param transaction giao dich can kiem tra
     * @return true neu giao dich thuoc pham vi ngan sach
     */
    public boolean appliesTo(Transaction transaction) {
        if (transaction == null || !(transaction instanceof Expense)) {
            return false;
        }

        // Kiem tra danh muc
        if (this.category != null) {
            if (transaction.getCategory() == null || 
                !this.category.getName().equalsIgnoreCase(transaction.getCategory().getName())) {
                    return false;
            }
        }

        // Kiem tra khoang thoi gian
        LocalDate txDate = transaction.getDate();
        if (txDate != null) {
            if (startDate != null && txDate.isBefore(startDate)) {
                return false;
            }
            if (endDate != null && txDate.isAfter(endDate)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tu dong tinh toan va cap nhat so tien da chi (currentSpent) tu danh sach giao dich
     * @param transactions danh sach giao dich trong he thong
     */
    public void updateSpentFromTransactions(List<Transaction> transactions) {
        if (transactions == null) {
            return;
        }

        double totalSpent = 0.0;

        for (Transaction tx : transactions) {
            if (appliesTo(tx)) {
                totalSpent += tx.getAmount();
            }
        }

        this.currentSpent = totalSpent;
    }

    /**
     * Cong them 1 khoan tien vua chi vao ngan sach
     * @param amount so tien chi them
     */
    public void addSpent(double amount) {
        if (amount < 0 || !Double.isFinite(amount)) {
            throw new IllegalArgumentException("So tien chi tieu phai lon hon 0");
        }

        this.currentSpent += amount;
    }

    /**
     * Reset so tien da chi ve 0.0
     */
    public void resetSpent() {
        this.currentSpent = 0.0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten ngan sach khong duoc de trong");
        }
        this.name = name.trim();
    }

    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) {
        if (limitAmount <= 0 || !Double.isFinite(limitAmount)) {
            throw new IllegalArgumentException("Han muc phai la so duong hop le");
        }
        this.limitAmount = limitAmount;
    }

    public double getCurrentSpent() { return currentSpent; }
    public void setSpentAmount(double currentSpent) {
        if (currentSpent < 0 || !Double.isFinite(currentSpent)) {
            throw new IllegalArgumentException("So tien da chi khong the la so am");
        }
        this.currentSpent = currentSpent;
    }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Period getPeriod() { return period; }
    public void setPeriod(Period period) { this.period = period; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    @Override
    public String toString() {
        return String.format("Budget[ID=%d, Name='%s', Limit=%.2f, Spent=%.2f, Exceed=%s]", id, name, limitAmount, currentSpent, isExceed() ? "YES" : "NO");    
    }
}

