package expensemanager.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import expensemanager.service.WalletOverviewCalculator.ChartBucket;
import expensemanager.service.WalletOverviewCalculator.OverviewResult;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
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
import javafx.util.Duration;
import javafx.util.StringConverter;

public class WalletChartHelper {

    public static void populateBalanceChart(AreaChart<String, Number> balanceChart, List<ChartBucket> balBuckets, StringConverter<Number> formatterVND) {
        if (balanceChart == null) return;

        balanceChart.setAnimated(false);
        balanceChart.getData().clear();

        // Định cấu hình Trục X (Nhãn ngày/tuần/tháng)
        if (balanceChart.getXAxis() instanceof CategoryAxis) {
            configureXAxis((CategoryAxis) balanceChart.getXAxis(), extractLabels(balBuckets));
        }

        // Định cấu hình Trục Y
        if (balanceChart.getYAxis() instanceof NumberAxis) {
            NumberAxis yAxis = (NumberAxis) balanceChart.getYAxis();
            yAxis.setAnimated(false);
            configureYAxisScale(yAxis, balBuckets, true, formatterVND);
        }

        if (balBuckets == null || balBuckets.isEmpty()) return;

        XYChart.Series<String, Number> balanceSeries = new XYChart.Series<>();
        balanceSeries.setName("Balance");

        for (ChartBucket bucket : balBuckets) {
            double balVal = bucket.balanceAtEnd;
            XYChart.Data<String, Number> balData = new XYChart.Data<>(bucket.label, balVal);
            balanceSeries.getData().add(balData);

            balData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    setupSymbolHoverEffect(newNode);
                    Tooltip.install(newNode, buildBalanceTooltip(bucket, balVal));
                }
            });
        }

        balanceChart.getData().add(balanceSeries);
    }

    public static void populateChangesChart(StackedBarChart<String, Number> changesChart, List<ChartBucket> chgBuckets, StringConverter<Number> formatterVND) {
        if (changesChart == null) return;

        changesChart.setAnimated(false);
        changesChart.getData().clear();

        int bucketCount = chgBuckets != null ? chgBuckets.size() : 0;

        // Định cấu hình Trục X
        if (changesChart.getXAxis() instanceof CategoryAxis) {
            configureXAxis((CategoryAxis) changesChart.getXAxis(), extractLabels(chgBuckets));
        }

        // FIX LỖI CỘT PHÌNH TO: Tự động chỉnh khoảng cách categoryGap dựa theo số lượng phần tử
        if (bucketCount <= 5) {
            changesChart.setCategoryGap(80); // Rất ít cột (xem theo Tuần) -> Tạo khoảng trống lớn để cột thon gọn
        } else if (bucketCount <= 12) {
            changesChart.setCategoryGap(20); // Xem theo Tháng (12 cột)
        } else if (bucketCount <= 20) {
            changesChart.setCategoryGap(20);
        } else {
            changesChart.setCategoryGap(6);  // Xem theo Ngày (31 cột)
        }

        // Định cấu hình Trục Y
        if (changesChart.getYAxis() instanceof NumberAxis) {
            NumberAxis yAxis = (NumberAxis) changesChart.getYAxis();
            yAxis.setAnimated(false);
            configureYAxisScale(yAxis, chgBuckets, false, formatterVND);
        }

        if (chgBuckets == null || chgBuckets.isEmpty()) return;

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expense");

        for (ChartBucket bucket : chgBuckets) {
            double incVal = bucket.totalIncome;
            double expVal = bucket.totalExpense;

            XYChart.Data<String, Number> incData = new XYChart.Data<>(bucket.label, incVal);
            XYChart.Data<String, Number> expData = new XYChart.Data<>(bucket.label, expVal);

            incomeSeries.getData().add(incData);
            expenseSeries.getData().add(expData);

            incData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    setupBarHoverEffect(newNode);
                    Tooltip.install(newNode, buildChangesTooltip(bucket, incVal, expVal));
                }
            });

            expData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    setupBarHoverEffect(newNode);
                    Tooltip.install(newNode, buildChangesTooltip(bucket, incVal, expVal));
                }
            });
        }

        changesChart.getData().addAll(incomeSeries, expenseSeries);
    }

    public static void populatePieCharts(PieChart incomePieChart, PieChart expensePieChart, OverviewResult overview) {
        if (incomePieChart != null) {
            incomePieChart.setAnimated(false);
            incomePieChart.getData().clear();
            if (overview != null && overview.incomeByCategory != null) {
                overview.incomeByCategory.forEach((name, val) -> {
                    if (val > 0) {
                        PieChart.Data slice = new PieChart.Data(name, val);
                        incomePieChart.getData().add(slice);
                        slice.nodeProperty().addListener((obs, oldNode, newNode) -> {
                            if (newNode != null) {
                                setupSymbolHoverEffect(newNode);
                                Tooltip.install(newNode, createPieTooltip(name, val, true));
                            }
                        });
                    }
                });
            }
        }

        if (expensePieChart != null) {
            expensePieChart.setAnimated(false);
            expensePieChart.getData().clear();
            if (overview != null && overview.expenseByCategory != null) {
                overview.expenseByCategory.forEach((name, val) -> {
                    if (val > 0) {
                        PieChart.Data slice = new PieChart.Data(name, val);
                        expensePieChart.getData().add(slice);
                        slice.nodeProperty().addListener((obs, oldNode, newNode) -> {
                            if (newNode != null) {
                                setupSymbolHoverEffect(newNode);
                                Tooltip.install(newNode, createPieTooltip(name, val, false));
                            }
                        });
                    }
                });
            }
        }
    }

    public static void updateLegend(VBox legendBox, Map<String, Double> data, String[] colors, boolean isIncome) {
        if (legendBox == null) return;
        legendBox.getChildren().clear();

        if (data == null || data.isEmpty()) {
            Label emptyLabel = new Label("Chưa có dữ liệu");
            emptyLabel.setStyle("-fx-text-fill: #a0aec0; -fx-font-style: italic;");
            legendBox.getChildren().add(emptyLabel);
            return;
        }

        int i = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            String color = colors[i % colors.length];

            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setSpacing(10);

            Circle circle = new Circle(6, Color.web(color));

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

    /** Lay danh sach nhan (label) theo dung thu tu tu danh sach bucket. */
    private static List<String> extractLabels(List<ChartBucket> buckets) {
        List<String> labels = new ArrayList<>();
        if (buckets != null) {
            for (ChartBucket b : buckets) {
                labels.add(b.label);
            }
        }
        return labels;
    }

    // --- Helper Cấu hình Trục X (CategoryAxis) ---
    /**
     * BUG FIX: truoc day ham nay chi goi xAxis.getCategories().clear() roi de
     * autoRanging=true tu suy category tu du lieu series duoc them vao sau.
     * Khi so luong category giam manh (VD: tu 31 ngay xuong con 12 thang),
     * CategoryAxis cua JavaFX khong lam moi dung layout/kich thuoc cac cot,
     * khien cot bi tinh sai vi tri/kich thuoc va khong hien thi duoc (du truc Y
     * van scale dung theo du lieu that). Fix: tat autoRanging va set cung danh
     * sach category dung thu tu truoc khi du lieu series duoc them vao.
     */
    private static void configureXAxis(CategoryAxis xAxis, List<String> labels) {
        xAxis.setAutoRanging(false);
        xAxis.getCategories().clear();
        if (labels != null && !labels.isEmpty()) {
            xAxis.getCategories().setAll(labels);
        }
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(true);

        int itemCount = labels != null ? labels.size() : 0;
        // Nếu chọn xem theo ngày (nhiều hơn 15 cột) -> Xoay nghiêng 45 độ cho dễ đọc
        if (itemCount > 15) {
            xAxis.setTickLabelRotation(-45);
        } else {
            xAxis.setTickLabelRotation(0);
        }
    }

    // --- Helper Cấu hình Trục Y ---
    private static void configureYAxisScale(NumberAxis yAxis, List<ChartBucket> buckets, boolean isBalanceChart, StringConverter<Number> formatterVND) {
        yAxis.setTickLabelFormatter(formatterVND);
        yAxis.setAutoRanging(false);

        double min = 0;
        double max = 0;

        if (buckets != null && !buckets.isEmpty()) {
            for (ChartBucket bucket : buckets) {
                if (isBalanceChart) {
                    min = Math.min(min, bucket.balanceAtEnd);
                    max = Math.max(max, bucket.balanceAtEnd);
                } else {
                    double total = bucket.totalIncome + bucket.totalExpense;
                    max = Math.max(max, total);
                }
            }
        }

        if (Math.abs(max - min) < 0.0001) {
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(1000000);
            yAxis.setTickUnit(250000);
            return;
        }

        double range = max - min;
        double paddedMax = max + (range * 0.15);
        double paddedMin = min < 0 ? min - (range * 0.15) : 0;

        double roughTick = (paddedMax - paddedMin) / 4.0;
        double niceTick = computeNiceTickUnit(roughTick);

        yAxis.setLowerBound(Math.floor(paddedMin / niceTick) * niceTick);
        yAxis.setUpperBound(Math.ceil(paddedMax / niceTick) * niceTick);
        yAxis.setTickUnit(niceTick);
    }

    private static double computeNiceTickUnit(double rough) {
        if (rough <= 0) return 100000;
        double exponent = Math.floor(Math.log10(rough));
        double fraction = rough / Math.pow(10, exponent);
        double niceFraction;

        if (fraction < 1.5) niceFraction = 1;
        else if (fraction < 3) niceFraction = 2;
        else if (fraction < 7) niceFraction = 5;
        else niceFraction = 10;

        return niceFraction * Math.pow(10, exponent);
    }

    private static void setupBarHoverEffect(Node node) {
        node.setStyle("-fx-cursor: hand;");
        node.setOnMouseEntered(e -> node.setOpacity(0.75));
        node.setOnMouseExited(e -> node.setOpacity(1.0));
    }

    private static void setupSymbolHoverEffect(Node node) {
        node.setStyle("-fx-cursor: hand;");
        node.setOnMouseEntered(e -> {
            node.setScaleX(1.15);
            node.setScaleY(1.15);
        });
        node.setOnMouseExited(e -> {
            node.setScaleX(1.0);
            node.setScaleY(1.0);
        });
    }

    private static Tooltip buildBalanceTooltip(ChartBucket bucket, double balVal) {
        Tooltip tooltip = createStyledTooltip();
        VBox tbox = createTooltipBaseContainer();
        
        Label dLbl = new Label(bucket.tooltipDateRange != null ? bucket.tooltipDateRange : bucket.label);
        dLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label bLbl = new Label(String.format("Số dư: %,.2f VND", balVal));
        bLbl.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");

        tbox.getChildren().addAll(dLbl, bLbl);
        tooltip.setGraphic(tbox);
        return tooltip;
    }

    private static Tooltip buildChangesTooltip(ChartBucket bucket, double incVal, double expVal) {
        Tooltip tooltip = createStyledTooltip();
        VBox tbox = createTooltipBaseContainer();

        Label dLbl = new Label(bucket.tooltipDateRange != null ? bucket.tooltipDateRange : bucket.label);
        dLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label iLbl = new Label(String.format("Thu nhập: +%,.2f VND", incVal));
        iLbl.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
        Label eLbl = new Label(String.format("Chi tiêu: -%,.2f VND", expVal));
        eLbl.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");

        tbox.getChildren().addAll(dLbl, iLbl, eLbl);
        tooltip.setGraphic(tbox);
        return tooltip;
    }

    private static Tooltip createPieTooltip(String categoryName, double amount, boolean isIncome) {
        Tooltip tooltip = createStyledTooltip();
        VBox tbox = createTooltipBaseContainer();

        Label nameLbl = new Label(categoryName);
        nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label amtLbl = new Label(String.format("%s%,.2f VND", isIncome ? "+" : "-", amount));
        amtLbl.setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold;", isIncome ? "#2563eb" : "#ef4444"));

        tbox.getChildren().addAll(nameLbl, amtLbl);
        tooltip.setGraphic(tbox);
        return tooltip;
    }

    private static Tooltip createStyledTooltip() {
        Tooltip tooltip = new Tooltip();
        tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        tooltip.setShowDelay(Duration.millis(50));
        return tooltip;
    }

    private static VBox createTooltipBaseContainer() {
        VBox tbox = new VBox(4);
        tbox.setStyle("-fx-background-color: white; -fx-padding: 8px 12px; -fx-background-radius: 6px; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 2); "
                + "-fx-border-color: #cbd5e1; -fx-border-radius: 6px;");
        return tbox;
    }
}
