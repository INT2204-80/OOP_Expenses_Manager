package expensemanager.ui.util;

import java.util.List;
import java.util.Map;

import expensemanager.service.WalletOverviewCalculator.ChartBucket;
import expensemanager.service.WalletOverviewCalculator.OverviewResult;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.StringConverter;

public class WalletChartHelper {

    public static void populateBalanceChart(AreaChart<String, Number> balanceChart, List<ChartBucket> balBuckets, StringConverter<Number> formatterVND) {
        if (balanceChart == null) {
            return;
        }
        if (balanceChart.getYAxis() instanceof NumberAxis) {
            ((NumberAxis) balanceChart.getYAxis()).setTickLabelFormatter(formatterVND);
        }
        
        XYChart.Series<String, Number> balanceSeries;
        if (balanceChart.getData().isEmpty()) {
            balanceSeries = new XYChart.Series<>();
            balanceChart.getData().add(balanceSeries);
        } else {
            balanceSeries = balanceChart.getData().get(0);
            balanceSeries.getData().clear();
        }

        for (ChartBucket bucket : balBuckets) {
            double balVal = bucket.balanceAtEnd;
            XYChart.Data<String, Number> balData = new XYChart.Data<>(bucket.label, balVal);
            balanceSeries.getData().add(balData);

            balData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, buildBalanceTooltip(bucket, balVal));
                }
            });
        }
    }

    public static void populateChangesChart(StackedBarChart<String, Number> changesChart, List<ChartBucket> chgBuckets, StringConverter<Number> formatterVND) {
        if (changesChart == null) {
            return;
        }
        if (changesChart.getYAxis() instanceof NumberAxis) {
            ((NumberAxis) changesChart.getYAxis()).setTickLabelFormatter(formatterVND);
        }
        
        XYChart.Series<String, Number> incomeSeries;
        XYChart.Series<String, Number> expenseSeries;
        
        if (changesChart.getData().size() >= 2) {
            incomeSeries = changesChart.getData().get(0);
            expenseSeries = changesChart.getData().get(1);
            incomeSeries.getData().clear();
            expenseSeries.getData().clear();
        } else {
            incomeSeries = new XYChart.Series<>();
            expenseSeries = new XYChart.Series<>();
            changesChart.getData().addAll(incomeSeries, expenseSeries);
        }

        for (ChartBucket bucket : chgBuckets) {
            double incVal = bucket.totalIncome;
            double expVal = bucket.totalExpense;

            XYChart.Data<String, Number> incData = new XYChart.Data<>(bucket.label, incVal);
            XYChart.Data<String, Number> expData = new XYChart.Data<>(bucket.label, expVal);
            incomeSeries.getData().add(incData);
            expenseSeries.getData().add(expData);

            javafx.beans.value.ChangeListener<Node> nodeListener = (obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, buildChangesTooltip(bucket, incVal, expVal));
                }
            };
            incData.nodeProperty().addListener(nodeListener);
            expData.nodeProperty().addListener(nodeListener);
        }
    }

    public static void populatePieCharts(PieChart incomePieChart, PieChart expensePieChart, OverviewResult overview) {
        if (incomePieChart != null) {
            incomePieChart.getData().clear();
            overview.incomeByCategory.forEach((name, val) -> incomePieChart.getData().add(new PieChart.Data(name, val)));
        }
        if (expensePieChart != null) {
            expensePieChart.getData().clear();
            overview.expenseByCategory.forEach((name, val) -> expensePieChart.getData().add(new PieChart.Data(name, val)));
        }
    }

    public static void updateLegend(VBox legendBox, Map<String, Double> data, String[] colors, boolean isIncome) {
        if (legendBox == null) {
            return;
        }
        legendBox.getChildren().clear();
        int i = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            String color = colors[i % colors.length];

            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setSpacing(10);

            Circle circle = new Circle(8, Color.web(color));

            Label nameLabel = new Label(entry.getKey());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568; -fx-font-size: 13px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label amountLabel = new Label(String.format("%s%,.2f VND", isIncome ? "+" : "-", entry.getValue()));
            amountLabel.setStyle(String.format("-fx-font-weight: bold; -fx-text-fill: %s; -fx-font-size: 13px;", isIncome ? "#2563eb" : "#ef4444"));

            hbox.getChildren().addAll(circle, nameLabel, spacer, amountLabel);
            legendBox.getChildren().add(hbox);
            i++;
        }
    }

    // --- Private Helper Methods (Tooltip Builders) ---

    private static Tooltip buildBalanceTooltip(ChartBucket bucket, double balVal) {
        Tooltip tooltip = new Tooltip();
        tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        
        VBox tbox = createTooltipBaseContainer();
        Label dLbl = new Label(bucket.tooltipDateRange);
        dLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label bLbl = new Label(String.format("Balance: %,.2f VND", balVal));
        bLbl.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
        
        tbox.getChildren().addAll(dLbl, bLbl);
        tooltip.setGraphic(tbox);
        return tooltip;
    }

    private static Tooltip buildChangesTooltip(ChartBucket bucket, double incVal, double expVal) {
        Tooltip tooltip = new Tooltip();
        tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        
        VBox tbox = createTooltipBaseContainer();
        Label dLbl = new Label(bucket.tooltipDateRange);
        dLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label iLbl = new Label(String.format("Income: +%,.2f VND", incVal));
        iLbl.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
        Label eLbl = new Label(String.format("Expense: %,.2f VND", expVal));
        eLbl.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        
        tbox.getChildren().addAll(dLbl, iLbl, eLbl);
        tooltip.setGraphic(tbox);
        return tooltip;
    }

    private static VBox createTooltipBaseContainer() {
        VBox tbox = new VBox(5);
        tbox.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-background-radius: 4px; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 4px;");
        return tbox;
    }
}