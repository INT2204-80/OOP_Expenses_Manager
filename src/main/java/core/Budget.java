package core;

import java.time.LocalDate;


public class Budget {
    private Category category;
    private double limit = 0.0;
    private Period period;
    private LocalDate startDate;
    private LocalDate endDate;

    public Budget(Category category, double limit, Period period) {
        if (category == null || limit < 0 || period == null) {
            throw new IllegalArgumentException("Invalid budget parameters");
        } else if (category.getType() != TransactionType.EXPENSE) {
            throw new IllegalArgumentException("Budget can only be set for expense categories");
        } else {
            this.category = category;
            this.limit = limit;
            this.period = period;
            this.startDate = LocalDate.now();
            this.endDate = Period.calculatedEndDate(startDate, period);
        }
        
    }

    public Budget(Category category, double limit, LocalDate startDate , LocalDate endDate) {
        if (category == null || limit < 0 || startDate == null || endDate == null) {
            throw new IllegalArgumentException("Invalid budget parameters");
        } else if (category.getType() != TransactionType.EXPENSE) {
            throw new IllegalArgumentException("Budget can only be set for expense categories");
        } else if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        } else {
            this.category = category;
            this.limit = limit;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public boolean isExceeded(double spent) {
        return spent > limit;
    }
}
