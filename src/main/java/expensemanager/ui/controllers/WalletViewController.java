package expensemanager.ui.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import core.storage.BudgetDAO;
import core.storage.TransactionDAO;
import core.transaction.Transaction;
import core.wallet.Wallet;
import expensemanager.service.BudgetService;
import expensemanager.service.TransactionService;
import expensemanager.service.WalletOverviewCalculator.ChartViewMode;
import expensemanager.ui.util.MoneyFormat;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class WalletViewController {

    // ===== Sub-managers =====
    private final TransactionService transactionService;
    private final TransactionDAO transactionDAO;
    private final BudgetDAO budgetDAO;
    private final BudgetService budgetService;

    private final PeriodFilterManager periodManager;
    private final WalletTransactionManager transactionManager;
    private final WalletOverviewTabController overviewTabController;
    private final WalletCategoryManager categoryManager;
    private final WalletBudgetManager budgetManager;

    // Constructors
    public WalletViewController() {
        this.transactionDAO = new TransactionDAO();
        this.budgetDAO = new BudgetDAO();
        this.transactionService = new TransactionService();
        this.budgetService = new BudgetService(this.budgetDAO, this.transactionDAO);

        this.periodManager = new PeriodFilterManager();
        this.transactionManager = new WalletTransactionManager(this.transactionService);
        this.overviewTabController = new WalletOverviewTabController();
        this.categoryManager = new WalletCategoryManager(this.budgetService, this.transactionDAO);
        this.budgetManager = new WalletBudgetManager(this.budgetDAO, this.budgetService);
    }

    public WalletViewController(TransactionService transactionService, TransactionDAO transactionDAO,
                                BudgetDAO budgetDAO, BudgetService budgetService) {
        this.transactionDAO = transactionDAO;
        this.budgetDAO = budgetDAO;
        this.transactionService = transactionService;
        this.budgetService = budgetService;

        this.periodManager = new PeriodFilterManager();
        this.transactionManager = new WalletTransactionManager(this.transactionService);
        this.overviewTabController = new WalletOverviewTabController();
        this.categoryManager = new WalletCategoryManager(this.budgetService, this.transactionDAO);
        this.budgetManager = new WalletBudgetManager(this.budgetDAO, this.budgetService);
    }

    // ===== FXML Labels & Charts =====
    @FXML private Label walletNameTopLabel;
    @FXML private Label currentBalanceLabel;
    @FXML private Label overviewBalanceLabel;
    @FXML private Label overviewChangeLabel;
    @FXML private Label overviewExpenseLabel;
    @FXML private Label overviewIncomeLabel;

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

    // ===== FXML Tabs & Layout =====
    @FXML private VBox tabTransactions;
    @FXML private ScrollPane transactionsScrollPane;
    @FXML private VBox transactionsListContainer;
    @FXML private VBox emptyTransactionsState;
    @FXML private VBox tabOverview;
    @FXML private VBox tabBudgets;
    @FXML private VBox budgetsContainer;
    @FXML private HBox tabSettings;

    // ===== FXML Filters =====
    @FXML private ComboBox<String> filterCategoryCombo;
    @FXML private TextField filterNoteField;
    @FXML private TextField filterMinAmountField;
    @FXML private TextField filterMaxAmountField;

    // ===== FXML Side Menu & Settings =====
    @FXML private VBox menuTransactions;
    @FXML private VBox menuOverview;
    @FXML private VBox menuBudgets;
    @FXML private VBox menuSettings;
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
    @FXML private TextField settingInitialBalance;
    @FXML private TextField settingCurrency;

    @FXML private Label periodLabelOverview;
    @FXML private Label periodLabelTrans;
    @FXML private Button futureToggleButton;

    private Wallet currentWallet;

    // =========================================================================
    // INITIALIZATION
    // =========================================================================
    public void initData(Wallet wallet) {
        this.currentWallet = wallet;
        reloadTransactionsFromStore();

        walletNameTopLabel.setText(wallet.getName());
        refreshBalanceLabels();

        categoryManager.setupCategoryCreationCombos(categoryTypeCombo, categoryIconCombo, categoryColorCombo);
        categoryManager.loadCategoriesToUI(incomeCategoriesContainer, expenseCategoriesContainer, this::setupFilters);

        refreshTransactionsView();
    }

    private void setupFilters() {
        transactionManager.setupFilters(filterCategoryCombo, filterNoteField, filterMinAmountField,
                filterMaxAmountField, categoryManager.getAllCategories(), this::refreshTransactionsView);
    }

    private void reloadTransactionsFromStore() {
        if (currentWallet == null) {
            return;
        }
        List<Transaction> loadedTransactions = transactionService.getTransactionsByWallet(currentWallet);
        currentWallet.getTransactions().clear();
        currentWallet.getTransactions().addAll(loadedTransactions);
    }

    private void refreshTransactionsView() {
        reloadTransactionsFromStore();
        updateOverviewData();
        List<Transaction> filtered = transactionManager.getFilteredTransactions(
                currentWallet, periodManager, filterCategoryCombo, filterNoteField, filterMinAmountField, filterMaxAmountField);

        transactionManager.renderTransactions(
                transactionsListContainer, emptyTransactionsState, transactionsScrollPane,
                filtered, categoryManager.getAllCategories(), currentWallet, () -> {
                    refreshTransactionsView();
                    refreshBalanceLabels();
                });
    }

    private void refreshBalanceLabels() {
        double displayBalance = calculateDisplayBalance(currentWallet);
        currentBalanceLabel.setText(MoneyFormat.format(displayBalance));
        if (overviewBalanceLabel != null) {
            overviewBalanceLabel.setText(MoneyFormat.format(displayBalance));
        }
    }

    private double calculateDisplayBalance(Wallet wallet) {
        if (wallet == null) {
            return 0.0;
        }
        double balance = wallet.getBalance();
        if (wallet.getTransactions() == null) {
            return balance;
        }
        for (Transaction transaction : wallet.getTransactions()) {
            if (transaction != null && transaction.getDate() != null && transaction.getDate().isAfter(java.time.LocalDate.now())) {
                balance -= transaction.getSignedAmount();
            }
        }
        return balance;
    }

    private void updateOverviewData() {
        List<Transaction> filtered = transactionManager.getFilteredTransactions(
                currentWallet, periodManager, filterCategoryCombo, filterNoteField, filterMinAmountField, filterMaxAmountField);

        overviewTabController.updateOverviewData(
                currentWallet, periodManager, filtered,
                overviewIncomeLabel, overviewExpenseLabel, overviewChangeLabel,
                periodLabelOverview, periodLabelTrans,
                balanceChart, changesChart, incomePieChart, expensePieChart,
                incomeLegendBox, expenseLegendBox);
    }

    // =========================================================================
    // ACTION HANDLERS (FORWARD TO MANAGERS)
    // =========================================================================
    @FXML public void handlePrevPeriod() { periodManager.handlePrevPeriod(this::refreshTransactionsView); }
    @FXML public void handleNextPeriod() { periodManager.handleNextPeriod(this::refreshTransactionsView); }
    @FXML public void handleCustomPeriod() { periodManager.handleCustomPeriod(this::refreshTransactionsView); }

    @FXML private void handleFutureToggle() {
        periodManager.setFutureOnly(!periodManager.isFutureOnly());
        if (futureToggleButton != null) {
            futureToggleButton.getStyleClass().remove("toggle-button-active");
            if (periodManager.isFutureOnly()) {
                futureToggleButton.getStyleClass().add("toggle-button-active");
            }
        }
        refreshTransactionsView();
    }

    @FXML private void showAddTransactionDialog() {
        transactionManager.showAddTransactionDialog(categoryManager.getAllCategories(), currentWallet, () -> {
            refreshTransactionsView();
            refreshBalanceLabels();
        });
    }

    @FXML private void handleResetFilters() {
        transactionManager.handleResetFilters(filterCategoryCombo, filterNoteField, filterMinAmountField, filterMaxAmountField, this::refreshTransactionsView);
    }

    @FXML private void handleBalDays() { overviewTabController.setBalanceViewMode(ChartViewMode.DAYS); overviewTabController.updateToggleStyles(balDaysLbl, balWeeksLbl, balMonthsLbl); updateOverviewData(); }
    @FXML private void handleBalWeeks() { overviewTabController.setBalanceViewMode(ChartViewMode.WEEKS); overviewTabController.updateToggleStyles(balWeeksLbl, balDaysLbl, balMonthsLbl); updateOverviewData(); }
    @FXML private void handleBalMonths() { overviewTabController.setBalanceViewMode(ChartViewMode.MONTHS); overviewTabController.updateToggleStyles(balMonthsLbl, balDaysLbl, balWeeksLbl); updateOverviewData(); }
    @FXML private void handleChgDays() { overviewTabController.setChangesViewMode(ChartViewMode.DAYS); overviewTabController.updateToggleStyles(chgDaysLbl, chgWeeksLbl, chgMonthsLbl); updateOverviewData(); }
    @FXML private void handleChgWeeks() { overviewTabController.setChangesViewMode(ChartViewMode.WEEKS); overviewTabController.updateToggleStyles(chgWeeksLbl, chgDaysLbl, chgMonthsLbl); updateOverviewData(); }
    @FXML private void handleChgMonths() { overviewTabController.setChangesViewMode(ChartViewMode.MONTHS); overviewTabController.updateToggleStyles(chgMonthsLbl, chgDaysLbl, chgMonthsLbl); updateOverviewData(); }

    @FXML private void handleCreateCategory() {
        categoryManager.handleCreateCategory(newCategoryNameField, categoryTypeCombo, categoryIconCombo, categoryColorCombo, incomeCategoriesContainer, expenseCategoriesContainer, this::setupFilters);
    }

    @FXML private void handleDeleteCategories() {
        categoryManager.handleDeleteCategories(incomeCategoriesContainer, expenseCategoriesContainer, this::setupFilters);
    }

    @FXML private void handleMergeCategories() {
        categoryManager.handleMergeCategories(incomeCategoriesContainer, expenseCategoriesContainer, this::setupFilters);
    }

    @FXML private void handleShowAddBudgetDialog() {
        budgetManager.handleShowAddBudgetDialog(categoryManager.getAllCategories(), currentWallet, budgetsContainer);
    }

    // =========================================================================
    // NAVIGATION & TAB SWITCHING
    // =========================================================================
    @FXML private void switchToTransactions() { switchTab(tabTransactions, menuTransactions); }
    @FXML private void switchToOverview() { switchTab(tabOverview, menuOverview); }

    @FXML
    private void switchToBudgets() {
        switchTab(tabBudgets, menuBudgets);
        budgetManager.renderBudgets(budgetsContainer, currentWallet, categoryManager.getAllCategories());
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
    private void handleDeleteWallet(MouseEvent event) {
        // 1. Tạo Alert xác nhận
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Confirmation");
        alert.setContentText("Are you sure you want to delete this wallet?");

        // 2. Đổi các nút bấm mặc định thành Yes và No
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        // 3. Hiển thị dialog và xử lý kết quả khi bấm
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            
            // TODO: Gọi hàm xóa Ví khỏi Database/Danh sách ví của bạn ở đây
            // ví dụ: walletService.deleteWallet(currentWallet);

            // 4. Xóa thành công -> Quay trở lại màn hình Dashboard
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Lỗi khi quay về Dashboard: " + e.getMessage());
            }
        }
    }
    @FXML
    private void handleUpdateWalletSettings() {
        String newName = settingWalletName.getText().trim();
        String initialBalanceStr = settingInitialBalance.getText().trim();
        String currency = settingCurrency.getText().trim();

        // Kiểm tra tên ví không được trống
        if (newName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên ví không được để trống!");
            return;
        }

        try {
            double initialBalance = Double.parseDouble(initialBalanceStr);

            // TODO: Cập nhật thông tin Ví vào Database / Model của bạn
            // currentWallet.setName(newName);
            // currentWallet.setInitialBalance(initialBalance);
            // currentWallet.setCurrency(currency);
            // walletService.updateWallet(currentWallet);

            // Cập nhật lại tên ví trên thanh Tiêu đề (Navigation Top) ngay lập tức
            if (walletNameTopLabel != null) {
                walletNameTopLabel.setText(newName);
            }

            // Hiển thị thông báo thành công
            showAlert(Alert.AlertType.INFORMATION, "Success", "Wallet settings updated successfully!");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Số dư ban đầu phải là một số hợp lệ!");
        }
    }

    // Hàm tiện ích hiển thị thông báo Alert
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}