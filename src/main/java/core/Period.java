package core;

import java.time.LocalDate;

public enum Period {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    public static LocalDate calculatedEndDate(LocalDate startDate, Period period) {
        return switch (period) {
            case DAILY -> startDate.plusDays(1);
            case WEEKLY -> startDate.plusWeeks(1);
            case MONTHLY -> startDate.plusMonths(1);
            case YEARLY -> startDate.plusYears(1);
        };
    }
}
