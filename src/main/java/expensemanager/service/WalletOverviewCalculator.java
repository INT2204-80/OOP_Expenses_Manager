package expensemanager.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import core.TransactionType;
import core.transaction.Transaction;

/**
 * Tinh toan tat ca so lieu thong ke cho tab Overview cua mot Wallet:
 * tong thu/chi, phan bo theo danh muc, so du/thu/chi theo ngay, va gom
 * nhom (bucket) theo ngay/tuan/thang cho bieu do.
 */
public final class WalletOverviewCalculator {

    private WalletOverviewCalculator() {}

    public enum ChartViewMode { DAYS, WEEKS, MONTHS }

    /** Ket qua tong hop so lieu cho 1 khoang thoi gian da loc. */
    public static final class OverviewResult {
        public double totalIncome;
        public double totalExpense;
        public final Map<String, Double> incomeByCategory = new LinkedHashMap<>();
        public final Map<String, Double> expenseByCategory = new LinkedHashMap<>();
        public final Map<LocalDate, Double> balanceByDate = new TreeMap<>();
        public final Map<LocalDate, Double> incomeByDate = new TreeMap<>();
        public final Map<LocalDate, Double> expenseByDate = new TreeMap<>();

        public double totalChange() {
            return totalIncome - totalExpense;
        }
    }

    /** 1 diem du lieu tren bieu do (1 ngay / 1 tuan / 1 thang tuy che do xem). */
    public static final class ChartBucket {
        public String label;
        public double balanceAtEnd;
        public double totalIncome;
        public double totalExpense;
        public String tooltipDateRange;
    }

    /**
     * Tinh tong thu/chi, phan bo theo danh muc va so du/thu/chi theo ngay.
     */
    public static OverviewResult compute(List<Transaction> filteredTransactions) {
        OverviewResult r = new OverviewResult();

        if (filteredTransactions == null || filteredTransactions.isEmpty()) {
            r.balanceByDate.put(LocalDate.now().minusDays(1), 0.0);
            return r;
        } else {
            r.balanceByDate.put(filteredTransactions.get(0).getDate().minusDays(1), 0.0);
        }

        double runningBalance = 0;
        List<Transaction> sorted = new ArrayList<>(filteredTransactions);
        sorted.sort(Comparator.comparing(Transaction::getDate));

        for (Transaction t : sorted) {
            if (t == null || t.getDate() == null) {
                continue;
            }

            LocalDate date = t.getDate();
            double amount = Math.abs(t.getAmount());
            String catName = (t.getCategory() != null && t.getCategory().getName() != null) 
                    ? t.getCategory().getName() 
                    : "Khác";

            if (t.getType() == TransactionType.INCOME) {
                r.totalIncome += amount;
                r.incomeByCategory.merge(catName, amount, Double::sum);
                r.incomeByDate.merge(date, amount, Double::sum);
                runningBalance += amount;
            } else {
                r.totalExpense += amount;
                r.expenseByCategory.merge(catName, amount, Double::sum);
                r.expenseByDate.merge(date, amount, Double::sum);
                runningBalance -= amount;
            }
            r.balanceByDate.put(date, runningBalance);
        }

        return r;
    }

    /**
     * Tinh so du cuoi ngay cho tung ngay trong khoang [startDate, endDate].
     */
    public static Map<LocalDate, Double> computeDailyBalance(
            LocalDate startDate, LocalDate endDate,
            Map<LocalDate, Double> balanceByDate,
            Map<LocalDate, Double> incomeByDate,
            Map<LocalDate, Double> expenseByDate) {

        Map<LocalDate, Double> dailyBalance = new TreeMap<>();
        double currentBal = 0;
        for (LocalDate d : balanceByDate.keySet()) {
            if (d.isBefore(startDate)) {
                currentBal = balanceByDate.get(d);
            }
        }
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            currentBal += incomeByDate.getOrDefault(d, 0.0);
            currentBal -= expenseByDate.getOrDefault(d, 0.0);
            dailyBalance.put(d, currentBal);
        }
        return dailyBalance;
    }

    /**
     * Gom nhom du lieu theo ngay/tuan/thang de ve bieu do.
     */
    public static List<ChartBucket> createBuckets(
            ChartViewMode mode, LocalDate startDate, LocalDate endDate,
            Map<LocalDate, Double> dailyBalance,
            Map<LocalDate, Double> incomeByDate,
            Map<LocalDate, Double> expenseByDate) {

        List<ChartBucket> buckets = new ArrayList<>();
        if (startDate == null || endDate == null) {
            return buckets;
        }

        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter fullDateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (mode == ChartViewMode.DAYS) {
            for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                ChartBucket b = new ChartBucket();
                b.label = d.format(dayFmt);
                b.tooltipDateRange = "Ngày " + d.format(fullDateFmt);
                b.balanceAtEnd = dailyBalance.getOrDefault(d, 0.0);
                b.totalIncome = incomeByDate.getOrDefault(d, 0.0);
                b.totalExpense = expenseByDate.getOrDefault(d, 0.0);
                buckets.add(b);
            }

        } else if (mode == ChartViewMode.WEEKS) {
            LocalDate current = startDate;
            int weekIndex = 1;

            while (!current.isAfter(endDate)) {
                LocalDate weekEnd = current.plusDays(6);
                if (weekEnd.isAfter(endDate)) {
                    weekEnd = endDate;
                }

                ChartBucket b = new ChartBucket();
                b.label = "Tuần " + weekIndex;
                b.tooltipDateRange = String.format("Tuần %d (%s - %s)",
                        weekIndex, current.format(dayFmt), weekEnd.format(dayFmt));

                double lastBal = 0;
                for (LocalDate d = current; !d.isAfter(weekEnd); d = d.plusDays(1)) {
                    b.totalIncome += incomeByDate.getOrDefault(d, 0.0);
                    b.totalExpense += expenseByDate.getOrDefault(d, 0.0);
                    if (dailyBalance.containsKey(d)) {
                        lastBal = dailyBalance.get(d);
                    }
                }
                b.balanceAtEnd = lastBal;
                buckets.add(b);

                weekIndex++;
                current = weekEnd.plusDays(1);
            }

        } else if (mode == ChartViewMode.MONTHS) {
            int year = startDate.getYear();
            DateTimeFormatter monthLabelFmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
            
            double runningBal = 0;
            for (Map.Entry<LocalDate, Double> entry : dailyBalance.entrySet()) {
                if (entry.getKey().isBefore(LocalDate.of(year, 1, 1))) {
                    runningBal = entry.getValue();
                }
            }

            for (int m = 1; m <= 12; m++) {
                LocalDate mStart = LocalDate.of(year, m, 1);
                LocalDate mEnd = mStart.withDayOfMonth(mStart.lengthOfMonth());

                ChartBucket b = new ChartBucket();
                b.label = mStart.format(monthLabelFmt);
                b.tooltipDateRange = String.format("Tháng %02d/%d", m, year);

                for (LocalDate d = mStart; !d.isAfter(mEnd); d = d.plusDays(1)) {
                    if (dailyBalance.containsKey(d)) {
                        runningBal = dailyBalance.get(d);
                    }
                    b.totalIncome += incomeByDate.getOrDefault(d, 0.0);
                    b.totalExpense += expenseByDate.getOrDefault(d, 0.0);
                }
                b.balanceAtEnd = runningBal;
                buckets.add(b);
            }
        }

        return buckets;
    }
}