package expensemanager.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
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
 *
 * <p>Truoc khi tach ra, toan bo logic nay (~250 dong) nam thang trong
 * {@code WalletViewController.updateOverviewData()} va
 * {@code createBuckets()}, tron lan voi code dung JavaFX Chart. Vi day
 * la logic thuan tuy (khong phu thuoc UI), viec giu no trong Controller
 * vi pham SRP va khien no khong the unit-test duoc neu khong khoi tao
 * JavaFX Toolkit. Class nay khong import bat ky lop javafx.* nao.
 *
 * <p><b>Luu y ve hanh vi:</b> de giu dung ket qua nhu ban goc, diem bat
 * dau cua {@code balanceByDate} lay ngay cua PHAN TU DAU TIEN trong danh
 * sach giao dich da loc (chua sap xep) tru 1 ngay - dung y het logic cu,
 * ke ca truong hop danh sach dau vao chua duoc sap xep theo ngay (day la
 * hanh vi cu, khong phai loi phat sinh khi refactor).
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
     * Tinh tong thu/chi, phan bo theo danh muc va so du/thu/chi theo ngay
     * tu danh sach giao dich da loc theo bo loc hien tai (period, danh
     * muc, ghi chu, so tien).
     */
    public static OverviewResult compute(List<Transaction> filteredTransactions) {
        OverviewResult r = new OverviewResult();

        if (filteredTransactions.isEmpty()) {
            r.balanceByDate.put(LocalDate.now().minusDays(1), 0.0);
        } else {
            r.balanceByDate.put(filteredTransactions.get(0).getDate().minusDays(1), 0.0);
        }

        double runningBalance = 0;
        List<Transaction> sorted = new ArrayList<>(filteredTransactions);
        sorted.sort(Comparator.comparing(Transaction::getDate));

        for (Transaction t : sorted) {
            LocalDate date = t.getDate();
            double amount = t.getAmount();
            String catName = t.getCategory().getName();

            if (t.getType() == TransactionType.INCOME) {
                r.totalIncome += amount;
                r.incomeByCategory.merge(catName, amount, Double::sum);
                r.incomeByDate.merge(date, amount, Double::sum);
                runningBalance += amount;
            } else {
                r.totalExpense += amount;
                r.expenseByCategory.merge(catName, amount, Double::sum);
                // store expense per-date as POSITIVE value; charts expect positive magnitudes
                r.expenseByDate.merge(date, amount, Double::sum);
                runningBalance -= amount;
            }
            r.balanceByDate.put(date, runningBalance);
        }

        return r;
    }

    /**
     * Tinh so du cuoi ngay cho tung ngay trong khoang [startDate, endDate],
     * bat dau tu so du gan nhat truoc startDate (neu co).
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
            // expenseByDate stores positive amounts; subtract to reduce balance
            currentBal -= expenseByDate.getOrDefault(d, 0.0);
            dailyBalance.put(d, currentBal);
        }
        return dailyBalance;
    }

    /**
     * Gom nhom du lieu theo ngay/tuan/thang de ve bieu do, tuy theo
     * {@link ChartViewMode} nguoi dung dang chon.
     */
    public static List<ChartBucket> createBuckets(
            ChartViewMode mode, LocalDate startDate, LocalDate endDate,
            Map<LocalDate, Double> dailyBalance,
            Map<LocalDate, Double> incomeByDate,
            Map<LocalDate, Double> expenseByDate) {

        List<ChartBucket> buckets = new ArrayList<>();

        if (mode == ChartViewMode.DAYS) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                ChartBucket b = new ChartBucket();
                b.label = d.format(fmt);
                b.tooltipDateRange = d.format(fmt);
                if (!(d.getDayOfMonth() == 1 || d.getDayOfMonth() == 8 || d.getDayOfMonth() == 15
                        || d.getDayOfMonth() == 22 || d.equals(endDate))) {
                    b.label = String.format("%" + d.getDayOfMonth() + "s", "");
                }
                b.balanceAtEnd = dailyBalance.getOrDefault(d, 0.0);
                b.totalIncome = incomeByDate.getOrDefault(d, 0.0);
                b.totalExpense = expenseByDate.getOrDefault(d, 0.0);
                buckets.add(b);
            }
        } else if (mode == ChartViewMode.WEEKS) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            int currentWeek = -1;
            ChartBucket currentBucket = null;
            for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                int weekNum = d.get(weekFields.weekOfMonth());
                if (weekNum != currentWeek) {
                    currentWeek = weekNum;
                    currentBucket = new ChartBucket();
                    currentBucket.label = "Week " + weekNum;
                    currentBucket.tooltipDateRange = "Week " + weekNum;
                    buckets.add(currentBucket);
                }
                currentBucket.balanceAtEnd = dailyBalance.getOrDefault(d, 0.0);
                currentBucket.totalIncome += incomeByDate.getOrDefault(d, 0.0);
                currentBucket.totalExpense += expenseByDate.getOrDefault(d, 0.0);
            }
        } else if (mode == ChartViewMode.MONTHS) {
            ChartBucket bucket = new ChartBucket();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
            bucket.label = startDate.format(fmt);
            bucket.tooltipDateRange = startDate.format(fmt);
            for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                bucket.balanceAtEnd = dailyBalance.getOrDefault(d, 0.0);
                bucket.totalIncome += incomeByDate.getOrDefault(d, 0.0);
                bucket.totalExpense += expenseByDate.getOrDefault(d, 0.0);
            }
            buckets.add(bucket);
        }
        return buckets;
    }
}
