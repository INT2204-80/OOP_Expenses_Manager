package core;

public enum Period {
    DAILY("Hàng ngày", 0, 0, 1),
    WEEKLY("Hàng tuần", 0, 0, 7),
    MONTHLY("Hàng tháng", 0, 1, 0),
    YEARLY("Hàng năm", 1, 0, 0);

    private final String displayName;
    private final int years;
    private final int months;
    private final int days;

    Period(String displayName, int years, int months, int days) {
        this.displayName = displayName;
        this.years = years;
        this.months = months;
        this.days = days;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getYears() {
        return years;
    }

    public int getMonths() {
        return months;
    }

    public int getDays() {
        return days;
    }


    public java.time.Period toJavaPeriod() {
        return java.time.Period.of(years, months, days);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
