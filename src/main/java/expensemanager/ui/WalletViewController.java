package expensemanager.ui;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import core.Budget;
import core.Category;
import core.TransactionType;
import core.storage.BudgetDAO;
import core.storage.TransactionDAO;
import core.storage.WalletDAO;
import core.transaction.Income;
import core.transaction.RecurringExpense;
import core.transaction.Transaction;
import core.wallet.Wallet;
import expensemanager.service.WalletOverviewCalculator;
import expensemanager.service.WalletOverviewCalculator.ChartBucket;
import expensemanager.service.WalletOverviewCalculator.ChartViewMode;
import expensemanager.service.WalletOverviewCalculator.OverviewResult;
import expensemanager.ui.factory.BudgetCardFactory;
import expensemanager.ui.factory.BudgetDialogFactory;
import expensemanager.ui.factory.CategoryDialogFactory;
import expensemanager.ui.factory.CategoryRowFactory;
import expensemanager.ui.factory.TransactionDialogFactory;
import expensemanager.ui.factory.TransactionRowFactory;
import expensemanager.ui.util.ColorPalette;
import expensemanager.ui.util.DashboardDialogHelper;
import expensemanager.ui.util.MoneyFormat;
import expensemanager.ui.util.TransactionFilter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.util.StringConverter;

public class WalletViewController {

    // ===== FXML: header/overview labels =====
    @FXML private Label walletNameTopLabel;
    @FXML private Label currentBalanceLabel;
    @FXML private Label overviewBalanceLabel;
    @FXML private Label overviewChangeLabel;
    @FXML private Label overviewExpenseLabel;
    @FXML private Label overviewIncomeLabel;
    // Transaction tab summary labels
    @FXML private Label transChangeLabel;
    @FXML private Label transExpenseLabel;
    @FXML private Label transIncomeLabel;

    // ===== FXML: charts =====
    @FXML private AreaChart<String, Number> balanceChart;
    @FXML private StackedBarChart<String, Number> changesChart;
    @FXML private PieChart incomePieChart;
    @FXML private PieChart expensePieChart;
    @FXML private VBox incomeLegendBox;
    @FXML private VBox expenseLegendBox;

    @FXML private Label balDaysLbl;
    @FXML private Label balWeeksLbl;
    @FXML private Label balMonthsLbl;
    @FXML private Label chgDaysLbl;
    @FXML private Label chgWeeksLbl;
    @FXML private Label chgMonthsLbl;

    private ChartViewMode balanceViewMode = ChartViewMode.DAYS;
    private ChartViewMode changesViewMode = ChartViewMode.DAYS;

    // ===== FXML: tabs =====
    @FXML private VBox tabTransactions;
    @FXML private ScrollPane transactionsScrollPane;
    @FXML private VBox transactionsListContainer;
    @FXML private VBox emptyTransactionsState;
    @FXML private VBox tabOverview;
    @FXML private VBox tabBudgets;
    @FXML private VBox budgetsContainer;
    @FXML private HBox tabSettings;

    // ===== FXML: transaction filters =====
    @FXML private ComboBox<String> filterCategoryCombo;
    @FXML private TextField filterNoteField;
    @FXML private TextField filterMinAmountField;
    @FXML private TextField filterMaxAmountField;
    @FXML private Label resetFiltersLabel;
    @FXML private javafx.scene.control.Button futureToggleBtn;
    private static final String ALL_CATEGORIES_SENTINEL = "All categories";
    private boolean showFuture = false;

    // ===== FXML: side menu =====
    @FXML private VBox menuTransactions;
    @FXML private VBox menuOverview;
    @FXML private VBox menuBudgets;
    @FXML private VBox menuSettings;

    // ===== FXML: settings =====
    @FXML private Label menuSettingsMain;
    @FXML private Label menuSettingsCat;
    @FXML private VBox settingsMain;
    @FXML private VBox settingsCategories;
    @FXML private TextField settingWalletName;
    @FXML private ComboBox<String> categoryTypeCombo;
    @FXML private ComboBox<String> categoryIconCombo;
    @FXML private ComboBox<String> categoryColorCombo;
    @FXML private TextField newCategoryNameField;
    @FXML private VBox incomeCategoriesContainer;
    @FXML private VBox expenseCategoriesContainer;

    @FXML private Label periodLabelOverview;
    @FXML private Label periodLabelTrans;

    private Wallet currentWallet;
    private List<Category> allCategories = new ArrayList<>();
    private Set<Category> selectedCategories = new HashSet<>();

    private LocalDate currentPeriodStart = LocalDate.now().withDayOfMonth(1);
    private LocalDate currentPeriodEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

    public void initData(Wallet wallet) {
        this.currentWallet = wallet;

        TransactionDAO transactionDAO = new TransactionDAO();
        List<Transaction> loadedTransactions = transactionDAO.getTransactionsByWallet(wallet);
        wallet.getTransactions().clear();
        wallet.getTransactions().addAll(loadedTransactions);

        walletNameTopLabel.setText(wallet.getName());
        refreshBalanceLabels();

        setupCategoryCreationCombos();
        loadCategoriesToUI();
        setupFilters();

        updateOverviewData();
        renderTransactions();
    }

    private void setupCategoryCreationCombos() {
        if (categoryIconCombo != null) {
            categoryIconCombo.getItems().addAll(ColorPalette.CATEGORY_ICONS);
            categoryIconCombo.setCellFactory(ColorPalette.iconCellFactory());
            categoryIconCombo.setButtonCell(ColorPalette.iconCellFactory().call(null));
            categoryIconCombo.getSelectionModel().selectFirst();
        }
        if (categoryColorCombo != null) {
            categoryColorCombo.getItems().addAll(ColorPalette.COLOR_NAMES);
            categoryColorCombo.setCellFactory(ColorPalette.colorCellFactory());
            categoryColorCombo.setButtonCell(ColorPalette.colorCellFactory().call(null));
            categoryColorCombo.getSelectionModel().selectFirst();
        }
        if (categoryTypeCombo != null) {
            categoryTypeCombo.getItems().addAll("Expense", "Income");
            categoryTypeCombo.getSelectionModel().selectFirst();
        }
    }

    private void setupFilters() {
        if (filterCategoryCombo != null) {
            filterCategoryCombo.getItems().clear();
            filterCategoryCombo.getItems().add(ALL_CATEGORIES_SENTINEL);
            for (Category cat : allCategories) {
                filterCategoryCombo.getItems().add(cat.getName());
            }
            filterCategoryCombo.getSelectionModel().selectFirst();
            filterCategoryCombo.valueProperty().addListener((obs, o, n) -> refreshTransactionsView());
        }
        if (filterNoteField != null) {
            filterNoteField.textProperty().addListener((obs, o, n) -> refreshTransactionsView());
        }
        if (filterMinAmountField != null) {
            filterMinAmountField.textProperty().addListener((obs, o, n) -> refreshTransactionsView());
        }
        if (filterMaxAmountField != null) {
            filterMaxAmountField.textProperty().addListener((obs, o, n) -> refreshTransactionsView());
        }
    }

    private void refreshTransactionsView() {
        updateOverviewData();
        renderTransactions();
    }

    private void refreshBalanceLabels() {
        if (currentWallet == null) return;

        // Exclude future-dated transactions from the "current" balance display
        java.time.LocalDate today = java.time.LocalDate.now();
        double futureEffect = 0.0;
        for (Transaction t : currentWallet.getTransactions()) {
            if (t.getDate() != null && t.getDate().isAfter(today)) {
                futureEffect += (t.getType() == TransactionType.INCOME) ? t.getAmount() : -t.getAmount();
            }
        }

        double displayedBalance = currentWallet.getBalance() - futureEffect;
        currentBalanceLabel.setText(MoneyFormat.format(displayedBalance));
        if (overviewBalanceLabel != null) {
            overviewBalanceLabel.setText(MoneyFormat.format(displayedBalance));
        }
    }

    private List<Transaction> getFilteredTransactions() {
        if (currentWallet == null) {
            return new ArrayList<>();
        }
        Double minAmount = parseDoubleOrNull(filterMinAmountField);
        Double maxAmount = parseDoubleOrNull(filterMaxAmountField);
        String selectedCategory = filterCategoryCombo != null ? filterCategoryCombo.getValue() : null;
        String noteKeyword = filterNoteField != null ? filterNoteField.getText() : null;

        TransactionFilter filter = TransactionFilter.create()
                .byCategoryName(selectedCategory, ALL_CATEGORIES_SENTINEL)
                .byNoteContains(noteKeyword)
                .byMinAmount(minAmount)
                .byMaxAmount(maxAmount);

        java.time.LocalDate today = java.time.LocalDate.now();
        if (showFuture) {
            // Show only transactions with date after today
            filter.and(t -> t.getDate() != null && t.getDate().isAfter(today));
        } else {
            // Default: filter by the current period
            filter.byPeriod(currentPeriodStart, currentPeriodEnd);
        }

        return filter.apply(currentWallet.getTransactions());
    }

    @FXML
    private void handleToggleFuture() {
        showFuture = !showFuture;
        if (futureToggleBtn != null) {
            if (showFuture) {
                if (!futureToggleBtn.getStyleClass().contains("toggle-button-active")) {
                    futureToggleBtn.getStyleClass().add("toggle-button-active");
                }
            } else {
                futureToggleBtn.getStyleClass().remove("toggle-button-active");
            }
        }
        refreshTransactionsView();
    }

    private static Double parseDoubleOrNull(TextField field) {
        if (field == null || field.getText() == null || field.getText().trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void renderTransactions() {
        if (transactionsListContainer == null || emptyTransactionsState == null || transactionsScrollPane == null) {
            return;
        }
        transactionsListContainer.getChildren().clear();

        List<Transaction> filtered = getFilteredTransactions();
        boolean empty = filtered.isEmpty();
        transactionsScrollPane.setVisible(!empty);
        transactionsScrollPane.setManaged(!empty);
        emptyTransactionsState.setVisible(empty);
        emptyTransactionsState.setManaged(empty);
        if (empty) {
            return;
        }

        for (Transaction t : filtered) {
            transactionsListContainer.getChildren().add(
                    TransactionRowFactory.createTransactionRow(t, this::handleEditTransaction, this::handleDeleteTransaction));
        }
    }

    private void handleDeleteTransaction(Transaction t) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("X\u00e1c nh\u1eadn x\u00f3a");
        alert.setHeaderText("B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n x\u00f3a giao d\u1ecbch n\u00e0y?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new TransactionDAO().deleteTransaction(t.getId());

            if (t instanceof Income) {
                currentWallet.withdraw(t.getAmount());
            } else {
                currentWallet.deposit(t.getAmount());
            }
            new WalletDAO().updateBalance(currentWallet.getId(), currentWallet.getBalance());
            currentWallet.getTransactions().remove(t);

            refreshTransactionsView();
            refreshBalanceLabels();
        }
    }

    /**
     * Them hoac sua 1 giao dich, dung chung {@link TransactionDialogFactory}
     * da co san trong project (truoc day WalletViewController tu viet lai
     * y het dialog nay 2 lan - 1 lan cho "them", 1 lan cho "sua").
     *
     * <p>Luu y: TransactionDialogFactory hien luon tao RecurringExpense voi
     * chu ky co dinh la Period.ofMonths(1) bat ke gia tri periodCombo nguoi
     * dung chon (xem TransactionDialogFactory.createDialog) - day la hanh
     * vi co san, khong phai loi phat sinh o day. Toi giu lai
     * {@code passedPeriods} cua giao dich lap cu (neu co) de khong mat du
     * lieu khi sua.
     */

    private void handleEditTransaction(Transaction oldT) {
        Dialog<Transaction> dialog = TransactionDialogFactory.createDialog(oldT, allCategories, currentWallet);
        Optional<Transaction> result = dialog.showAndWait();
        result.ifPresent(newT -> {
            if (newT instanceof RecurringExpense && oldT instanceof RecurringExpense) {
                ((RecurringExpense) newT).setPassedPeriods(((RecurringExpense) oldT).getPassedPeriods());
            }

            if (oldT instanceof Income) {
                currentWallet.withdraw(oldT.getAmount());
            } else {
                currentWallet.deposit(oldT.getAmount());
            }
            currentWallet.getTransactions().remove(oldT);
            currentWallet.getTransactions().add(newT); // constructor cua newT da cap nhat so du vi

            new TransactionDAO().updateTransaction(newT, currentWallet.getId());
            new WalletDAO().updateBalance(currentWallet.getId(), currentWallet.getBalance());

            refreshTransactionsView();
            refreshBalanceLabels();
        });
    }

    @FXML
    private void showAddTransactionDialog() {
        Dialog<Transaction> dialog = TransactionDialogFactory.createDialog(null, allCategories, currentWallet);
        Optional<Transaction> result = dialog.showAndWait();
        result.ifPresent(transaction -> {
            currentWallet.addTransaction(transaction);

            new TransactionDAO().saveTransaction(transaction, currentWallet.getId());
            new WalletDAO().updateBalance(currentWallet.getId(), currentWallet.getBalance());

            refreshTransactionsView();
            refreshBalanceLabels();
        });
    }

    @FXML
    private void handleBackToDashboard(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) walletNameTopLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePrevPeriod() {
        long days = java.time.temporal.ChronoUnit.DAYS.between(currentPeriodStart, currentPeriodEnd) + 1;
        currentPeriodStart = currentPeriodStart.minusDays(days);
        currentPeriodEnd = currentPeriodEnd.minusDays(days);
        refreshTransactionsView();
    }

    @FXML
    public void handleNextPeriod() {
        long days = java.time.temporal.ChronoUnit.DAYS.between(currentPeriodStart, currentPeriodEnd) + 1;
        currentPeriodStart = currentPeriodStart.plusDays(days);
        currentPeriodEnd = currentPeriodEnd.plusDays(days);
        refreshTransactionsView();
    }

    @FXML
    public void handleCustomPeriod() {
        Optional<Pair<LocalDate, LocalDate>> result =
                DashboardDialogHelper.showCustomPeriodDialog(currentPeriodStart, currentPeriodEnd);

        result.ifPresent(pair -> {
            if (pair.getKey() == null || pair.getValue() == null) {
                return;
            }
            if (pair.getKey().isAfter(pair.getValue())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("L\u1ed7i");
                alert.setHeaderText("Ng\u00e0y b\u1eaft \u0111\u1ea7u kh\u00f4ng th\u1ec3 l\u1edbn h\u01a1n ng\u00e0y k\u1ebft th\u00fac!");
                alert.showAndWait();
            } else {
                currentPeriodStart = pair.getKey();
                currentPeriodEnd = pair.getValue();
                refreshTransactionsView();
            }
        });
    }

    @FXML
    private void handleResetFilters() {
        if (filterCategoryCombo != null) filterCategoryCombo.getSelectionModel().selectFirst();
        if (filterNoteField != null) filterNoteField.clear();
        if (filterMinAmountField != null) filterMinAmountField.clear();
        if (filterMaxAmountField != null) filterMaxAmountField.clear();
        refreshTransactionsView();
    }

    private void updatePeriodLabels() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
        String labelText = currentPeriodStart.format(dtf) + " - " + currentPeriodEnd.format(dtf);
        if (periodLabelOverview != null) periodLabelOverview.setText(labelText);
        if (periodLabelTrans != null) periodLabelTrans.setText(labelText);
    }

 
    private void updateOverviewData() {
        if (currentWallet == null) {
            return;
        }
        updatePeriodLabels();

        OverviewResult overview = WalletOverviewCalculator.compute(getFilteredTransactions());

        if (overviewIncomeLabel != null) overviewIncomeLabel.setText(String.format("+%,.2f VND", overview.totalIncome));
        if (overviewExpenseLabel != null) overviewExpenseLabel.setText(String.format("-%,.2f VND", overview.totalExpense));
        if (overviewChangeLabel != null) {
            double change = overview.totalChange();
            overviewChangeLabel.setText(String.format("%s%,.2f VND", change >= 0 ? "+" : "", change));
            overviewChangeLabel.setStyle(change >= 0 ? "-fx-text-fill: #3b82f6;" : "-fx-text-fill: #ef4444;");
        }

        // Also populate the Transactions tab summary cards (they live in the Transactions tab FXML)
        if (transIncomeLabel != null) transIncomeLabel.setText(String.format("+%,.2f VND", overview.totalIncome));
        if (transExpenseLabel != null) transExpenseLabel.setText(String.format("-%,.2f VND", overview.totalExpense));
        if (transChangeLabel != null) {
            double change = overview.totalChange();
            transChangeLabel.setText(String.format("%s%,.2f VND", change >= 0 ? "+" : "", change));
            transChangeLabel.setStyle(change >= 0 ? "-fx-text-fill: #3b82f6;" : "-fx-text-fill: #ef4444;");
        }

        StringConverter<Number> formatterVND = new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                double val = object.doubleValue();
                if (val == 0) return "0.00 VND";
                return String.format("%s%,.2f VND", val > 0 ? "+" : "", val);
            }
            @Override
            public Number fromString(String string) { return null; }
        };

        Map<LocalDate, Double> dailyBalance = WalletOverviewCalculator.computeDailyBalance(
                currentPeriodStart, currentPeriodEnd, overview.balanceByDate, overview.incomeByDate, overview.expenseByDate);

        List<ChartBucket> balBuckets = WalletOverviewCalculator.createBuckets(
                balanceViewMode, currentPeriodStart, currentPeriodEnd, dailyBalance, overview.incomeByDate, overview.expenseByDate);
        List<ChartBucket> chgBuckets = WalletOverviewCalculator.createBuckets(
                changesViewMode, currentPeriodStart, currentPeriodEnd, dailyBalance, overview.incomeByDate, overview.expenseByDate);

        populateBalanceChart(balBuckets, formatterVND);
        populateChangesChart(chgBuckets, formatterVND);
        populatePieCharts(overview);

        updateLegend(incomeLegendBox, overview.incomeByCategory, new String[]{"#2563eb", "#60a5fa", "#1d4ed8"}, true);
        updateLegend(expenseLegendBox, overview.expenseByCategory, new String[]{"#ef4444", "#f59e0b", "#f97316", "#ec4899"}, false);
    }

    private void populateBalanceChart(List<ChartBucket> balBuckets, StringConverter<Number> formatterVND) {
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

    private Tooltip buildBalanceTooltip(ChartBucket bucket, double balVal) {
        Tooltip tooltip = new Tooltip();
        tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        VBox tbox = new VBox(5);
        tbox.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-background-radius: 4px; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 4px;");
        Label dLbl = new Label(bucket.tooltipDateRange);
        dLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label bLbl = new Label(String.format("Balance: %,.2f VND", balVal));
        bLbl.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
        tbox.getChildren().addAll(dLbl, bLbl);
        tooltip.setGraphic(tbox);
        return tooltip;
    }

    private void populateChangesChart(List<ChartBucket> chgBuckets, StringConverter<Number> formatterVND) {
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

    private Tooltip buildChangesTooltip(ChartBucket bucket, double incVal, double expVal) {
        Tooltip tooltip = new Tooltip();
        tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        VBox tbox = new VBox(5);
        tbox.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-background-radius: 4px; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 4px;");
        Label dLbl = new Label(bucket.tooltipDateRange);
        dLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label iLbl = new Label(String.format("Income: +%,.2f VND", incVal));
        iLbl.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
        Label eLbl = new Label(String.format("Expense: -%,.2f VND", expVal));
        eLbl.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        tbox.getChildren().addAll(dLbl, iLbl, eLbl);
        tooltip.setGraphic(tbox);
        return tooltip;
    }

    private void populatePieCharts(OverviewResult overview) {
        if (incomePieChart != null) {
            incomePieChart.getData().clear();
            overview.incomeByCategory.forEach((name, val) -> incomePieChart.getData().add(new PieChart.Data(name, val)));
        }
        if (expensePieChart != null) {
            expensePieChart.getData().clear();
            overview.expenseByCategory.forEach((name, val) -> expensePieChart.getData().add(new PieChart.Data(name, val)));
        }
    }

    private void updateLegend(VBox legendBox, Map<String, Double> data, String[] colors, boolean isIncome) {
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

    @FXML private void handleBalDays() { balanceViewMode = ChartViewMode.DAYS; updateToggleStyles(balDaysLbl, balWeeksLbl, balMonthsLbl); updateOverviewData(); }
    @FXML private void handleBalWeeks() { balanceViewMode = ChartViewMode.WEEKS; updateToggleStyles(balWeeksLbl, balDaysLbl, balMonthsLbl); updateOverviewData(); }
    @FXML private void handleBalMonths() { balanceViewMode = ChartViewMode.MONTHS; updateToggleStyles(balMonthsLbl, balDaysLbl, balWeeksLbl); updateOverviewData(); }
    @FXML private void handleChgDays() { changesViewMode = ChartViewMode.DAYS; updateToggleStyles(chgDaysLbl, chgWeeksLbl, chgMonthsLbl); updateOverviewData(); }
    @FXML private void handleChgWeeks() { changesViewMode = ChartViewMode.WEEKS; updateToggleStyles(chgWeeksLbl, chgDaysLbl, chgMonthsLbl); updateOverviewData(); }
    @FXML private void handleChgMonths() { changesViewMode = ChartViewMode.MONTHS; updateToggleStyles(chgMonthsLbl, chgDaysLbl, chgWeeksLbl); updateOverviewData(); }

    private void updateToggleStyles(Label active, Label inactive1, Label inactive2) {
        active.getStyleClass().remove("chart-toggle");
        if (!active.getStyleClass().contains("chart-toggle-active")) active.getStyleClass().add("chart-toggle-active");

        inactive1.getStyleClass().remove("chart-toggle-active");
        if (!inactive1.getStyleClass().contains("chart-toggle")) inactive1.getStyleClass().add("chart-toggle");

        inactive2.getStyleClass().remove("chart-toggle-active");
        if (!inactive2.getStyleClass().contains("chart-toggle")) inactive2.getStyleClass().add("chart-toggle");
    }

    @FXML private void switchToTransactions() { switchTab(tabTransactions, menuTransactions); }
    @FXML private void switchToOverview() { switchTab(tabOverview, menuOverview); }

    @FXML
    private void switchToBudgets() {
        switchTab(tabBudgets, menuBudgets);
        renderBudgets();
    }

    @FXML
    private void switchToSettings() {
        switchTab(tabSettings, menuSettings);
        if (settingWalletName != null && currentWallet != null) {
            settingWalletName.setText(currentWallet.getName());
        }
    }

    private void switchTab(Node tab, VBox menu) {
        tabTransactions.setVisible(false);
        tabOverview.setVisible(false);
        tabBudgets.setVisible(false);
        tabSettings.setVisible(false);
        tab.setVisible(true);

        for (VBox m : List.of(menuTransactions, menuOverview, menuBudgets, menuSettings)) {
            m.getStyleClass().remove("nav-item-active-container");
            m.getStyleClass().add("nav-item-container");
        }
        menu.getStyleClass().remove("nav-item-container");
        menu.getStyleClass().add("nav-item-active-container");
    }

    @FXML
    private void switchToSettingsMain() {
        settingsMain.setVisible(true);
        settingsCategories.setVisible(false);
        menuSettingsMain.getStyleClass().add("settings-menu-item-active");
        menuSettingsCat.getStyleClass().remove("settings-menu-item-active");
    }

    @FXML
    private void switchToSettingsCat() {
        settingsMain.setVisible(false);
        settingsCategories.setVisible(true);
        menuSettingsCat.getStyleClass().add("settings-menu-item-active");
        menuSettingsMain.getStyleClass().remove("settings-menu-item-active");
    }

    private void loadCategoriesToUI() {
        if (incomeCategoriesContainer == null || expenseCategoriesContainer == null) {
            return;
        }
        incomeCategoriesContainer.getChildren().clear();
        expenseCategoriesContainer.getChildren().clear();
        allCategories.clear();
        selectedCategories.clear();

        allCategories = new TransactionDAO().getAllCategories();

        for (Category cat : allCategories) {
            HBox row = CategoryRowFactory.createCategoryRow(
                    cat,
                    (c, selected) -> {
                        if (selected) selectedCategories.add(c); else selectedCategories.remove(c);
                    },
                    this::showEditCategoryDialog,
                    this::deleteCategorySoft);

            if (cat.getType() == TransactionType.INCOME) {
                incomeCategoriesContainer.getChildren().add(row);
            } else {
                expenseCategoriesContainer.getChildren().add(row);
            }
        }
    }

    private void deleteCategorySoft(Category cat) {
        try {
            new TransactionDAO().softDeleteCategory(cat.getName(), cat.getType().name());
            loadCategoriesToUI();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteCategories() {
        if (selectedCategories.isEmpty()) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("X\u00f3a danh m\u1ee5c");
        alert.setHeaderText("B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n x\u00f3a " + selectedCategories.size() + " danh m\u1ee5c \u0111\u00e3 ch\u1ecdn?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            TransactionDAO dao = new TransactionDAO();
            for (Category cat : selectedCategories) {
                try {
                    dao.softDeleteCategory(cat.getName(), cat.getType().name());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            selectedCategories.clear();
            loadCategoriesToUI();
        }
    }

    @FXML
    private void handleMergeCategories() {
        if (selectedCategories.size() < 2) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("C\u1ea7n ch\u1ecdn \u00edt nh\u1ea5t 2 danh m\u1ee5c \u0111\u1ec3 g\u1ed9p!");
            alert.showAndWait();
            return;
        }

        List<Category> choices = new ArrayList<>(selectedCategories);
        ChoiceDialog<Category> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("G\u1ed9p danh m\u1ee5c");
        dialog.setHeaderText("Ch\u1ecdn danh m\u1ee5c \u0110\u00cdCH (danh m\u1ee5c s\u1ebd \u0111\u01b0\u1ee3c gi\u1eef l\u1ea1i):");
        dialog.setContentText("Danh m\u1ee5c \u0111\u00edch:");

        dialog.showAndWait().ifPresent(target -> {
            List<Category> sources = new ArrayList<>(selectedCategories);
            sources.remove(target);

            try {
                new TransactionDAO().mergeCategories(sources, target);
                selectedCategories.clear();
                loadCategoriesToUI();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("C\u00f3 l\u1ed7i x\u1ea3y ra khi g\u1ed9p danh m\u1ee5c!");
                errorAlert.showAndWait();
            }
        });
    }

    private void showEditCategoryDialog(Category cat) {
        CategoryDialogFactory.createEditDialog(cat).showAndWait().ifPresent(result -> {
            try {
                new TransactionDAO().updateCategory(
                        cat.getName(), cat.getType().name(),
                        result.newName, result.newType, result.newIcon, result.newColor);
                loadCategoriesToUI();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    private void handleCreateCategory() {
        if (newCategoryNameField == null || categoryTypeCombo == null) {
            return;
        }
        String name = newCategoryNameField.getText();
        String type = categoryTypeCombo.getValue();
        String icon = categoryIconCombo != null ? categoryIconCombo.getValue() : null;
        String color = categoryColorCombo != null ? categoryColorCombo.getValue() : null;

        if (name == null || name.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "T\u00ean danh m\u1ee5c kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng!", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (type == null) {
            return;
        }

        try {
            new TransactionDAO().getOrCreateCategoryId(name.trim(), type.toUpperCase(), icon, color);
            newCategoryNameField.clear();
            loadCategoriesToUI();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowAddBudgetDialog() {
        Dialog<Budget> dialog = BudgetDialogFactory.createAddBudgetDialog(allCategories);
        dialog.showAndWait().ifPresent(budget -> {
            new BudgetDAO().addBudget(budget, currentWallet.getId());
            renderBudgets();
        });
    }

    private void renderBudgets() {
        if (budgetsContainer == null || currentWallet == null) {
            return;
        }
        budgetsContainer.getChildren().clear();

        BudgetDAO dao = new BudgetDAO();
        List<Budget> budgets = dao.getBudgetsByWallet(currentWallet.getId());

        if (budgets.isEmpty()) {
            budgetsContainer.getChildren().add(BudgetCardFactory.createEmptyState(this::handleShowAddBudgetDialog));
            return;
        }

        List<Transaction> walletTx = currentWallet.getTransactions();
        for (Budget budget : budgets) {
            budget.updateSpentFromTransactions(walletTx);
            VBox card = BudgetCardFactory.createBudgetCard(budget, () -> {
                dao.deleteBudget(budget.getId());
                renderBudgets();
            });
            budgetsContainer.getChildren().add(card);
        }
    }
}
