package expensemanager.ui.controllers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import core.transaction.Transaction;
import core.wallet.Wallet;
import expensemanager.service.WalletOverviewCalculator;
import expensemanager.service.WalletOverviewCalculator.ChartBucket;
import expensemanager.service.WalletOverviewCalculator.ChartViewMode;
import expensemanager.service.WalletOverviewCalculator.OverviewResult;
import expensemanager.ui.util.WalletChartHelper;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class WalletOverviewTabController {

    private ChartViewMode balanceViewMode = ChartViewMode.DAYS;
    private ChartViewMode changesViewMode = ChartViewMode.DAYS;

    public void setBalanceViewMode(ChartViewMode mode) { this.balanceViewMode = mode; }
    public void setChangesViewMode(ChartViewMode mode) { this.changesViewMode = mode; }

    public void updateOverviewData(Wallet currentWallet, PeriodFilterManager periodManager,
                                   List<Transaction> filteredTransactions,
                                   Label overviewIncomeLabel, Label overviewExpenseLabel, Label overviewChangeLabel,
                                   Label periodLabelOverview, Label periodLabelTrans,
                                   AreaChart<String, Number> balanceChart, StackedBarChart<String, Number> changesChart,
                                   PieChart incomePieChart, PieChart expensePieChart,
                                   VBox incomeLegendBox, VBox expenseLegendBox) {
        if (currentWallet == null) {
            return;
        }

        if (periodManager != null) {
            periodManager.updatePeriodLabels(periodLabelOverview, periodLabelTrans);
        }

        List<Transaction> safeTransactions = filteredTransactions != null ? filteredTransactions : Collections.emptyList();
        OverviewResult overview = WalletOverviewCalculator.compute(safeTransactions);

        if (overviewIncomeLabel != null) overviewIncomeLabel.setText(String.format("+%,.2f VND", overview.totalIncome));
        if (overviewExpenseLabel != null) overviewExpenseLabel.setText(String.format("-%,.2f VND", overview.totalExpense));
        if (overviewChangeLabel != null) {
            double change = overview.totalChange();
            overviewChangeLabel.setText(String.format("%s%,.2f VND", change >= 0 ? "+" : "", change));
            overviewChangeLabel.setStyle(change >= 0 ? "-fx-text-fill: #3b82f6;" : "-fx-text-fill: #ef4444;");
        }

        StringConverter<Number> formatterVND = new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                if (object == null) return "0 VND";
                double val = object.doubleValue();
                if (Math.abs(val) < 0.001) return "0 VND";
                return String.format("%,.0f VND", val);
            }
            @Override
            public Number fromString(String string) { return null; }
        };

        LocalDate startDate = periodManager != null ? periodManager.getStart() : LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = periodManager != null ? periodManager.getEnd() : LocalDate.now();

        Map<LocalDate, Double> dailyBalance = WalletOverviewCalculator.computeDailyBalance(
                startDate, endDate, overview.balanceByDate, overview.incomeByDate, overview.expenseByDate);

        List<ChartBucket> balBuckets = WalletOverviewCalculator.createBuckets(
                balanceViewMode, startDate, endDate, dailyBalance, overview.incomeByDate, overview.expenseByDate);
        List<ChartBucket> chgBuckets = WalletOverviewCalculator.createBuckets(
                changesViewMode, startDate, endDate, dailyBalance, overview.incomeByDate, overview.expenseByDate);

        WalletChartHelper.populateBalanceChart(balanceChart, balBuckets, formatterVND);
        WalletChartHelper.populateChangesChart(changesChart, chgBuckets, formatterVND);
        WalletChartHelper.populatePieCharts(incomePieChart, expensePieChart, overview);

        WalletChartHelper.updateLegend(incomeLegendBox, overview.incomeByCategory, new String[]{"#2563eb", "#60a5fa", "#1d4ed8"}, true);
        WalletChartHelper.updateLegend(expenseLegendBox, overview.expenseByCategory, new String[]{"#ef4444", "#f59e0b", "#f97316", "#ec4899"}, false);
    }

    public void updateToggleStyles(Label active, Label inactive1, Label inactive2) {
        if (active != null) {
            active.getStyleClass().remove("chart-toggle");
            if (!active.getStyleClass().contains("chart-toggle-active")) active.getStyleClass().add("chart-toggle-active");
        }

        if (inactive1 != null) {
            inactive1.getStyleClass().remove("chart-toggle-active");
            if (!inactive1.getStyleClass().contains("chart-toggle")) inactive1.getStyleClass().add("chart-toggle");
        }

        if (inactive2 != null) {
            inactive2.getStyleClass().remove("chart-toggle-active");
            if (!inactive2.getStyleClass().contains("chart-toggle")) inactive2.getStyleClass().add("chart-toggle");
        }
    }
    
}