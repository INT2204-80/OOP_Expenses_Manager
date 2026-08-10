package expensemanager.ui;

import core.wallet.Wallet;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class WalletViewController {

    @FXML
    private Label walletNameTopLabel;

    @FXML
    private Label currentBalanceLabel;

    @FXML
    private Label overviewBalanceLabel;

    @FXML
    private Label overviewChangeLabel;

    @FXML
    private Label overviewExpenseLabel;

    @FXML
    private Label overviewIncomeLabel;

    @FXML private javafx.scene.chart.AreaChart<String, Number> balanceChart;
    @FXML private javafx.scene.chart.StackedBarChart<String, Number> changesChart;
    @FXML private javafx.scene.chart.PieChart incomePieChart;
    @FXML private javafx.scene.chart.PieChart expensePieChart;
    @FXML private javafx.scene.layout.VBox incomeLegendBox;
    @FXML private javafx.scene.layout.VBox expenseLegendBox;

    @FXML private Label balDaysLbl;
    @FXML private Label balWeeksLbl;
    @FXML private Label balMonthsLbl;
    @FXML private Label chgDaysLbl;
    @FXML private Label chgWeeksLbl;
    @FXML private Label chgMonthsLbl;

    public enum ChartViewMode { DAYS, WEEKS, MONTHS }
    private ChartViewMode balanceViewMode = ChartViewMode.DAYS;
    private ChartViewMode changesViewMode = ChartViewMode.DAYS;

    private Wallet currentWallet;

    public void initData(Wallet wallet) {
        this.currentWallet = wallet;
        
        // Load transactions from DB
        core.storage.TransactionDAO transactionDAO = new core.storage.TransactionDAO();
        java.util.List<core.transaction.Transaction> loadedTransactions = transactionDAO.getTransactionsByWallet(wallet);
        wallet.getTransactions().clear();
        for (core.transaction.Transaction t : loadedTransactions) {
            wallet.getTransactions().add(t);
        }
        
        walletNameTopLabel.setText(wallet.getName());
        currentBalanceLabel.setText(String.format("%,.2f VND", wallet.getBalance()));
        if (overviewBalanceLabel != null) {
            overviewBalanceLabel.setText(String.format("%,.2f VND", wallet.getBalance()));
        }
        
        if (categoryTypeCombo != null) {
            categoryTypeCombo.getItems().addAll("Expense", "Income");
            categoryTypeCombo.getSelectionModel().selectFirst();
        }

        loadCategoriesToUI();
        updateOverviewData();
        renderTransactions();
    }
    
    private void renderTransactions() {
        if (transactionsListContainer == null || emptyTransactionsState == null || transactionsScrollPane == null) return;
        
        transactionsListContainer.getChildren().clear();
        
        if (currentWallet.getTransactions().isEmpty()) {
            transactionsScrollPane.setVisible(false);
            transactionsScrollPane.setManaged(false);
            emptyTransactionsState.setVisible(true);
            emptyTransactionsState.setManaged(true);
            return;
        }
        
        transactionsScrollPane.setVisible(true);
        transactionsScrollPane.setManaged(true);
        emptyTransactionsState.setVisible(false);
        emptyTransactionsState.setManaged(false);
        
        for (core.transaction.Transaction t : currentWallet.getTransactions()) {
            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(15);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");
            
            boolean isIncome = t instanceof core.transaction.Income;
            javafx.scene.shape.Circle icon = new javafx.scene.shape.Circle(20, javafx.scene.paint.Color.web(isIncome ? "#2563eb" : "#ef4444"));
            
            javafx.scene.layout.VBox infoBox = new javafx.scene.layout.VBox(5);
            Label catLabel = new Label(t.getCategory().getName());
            catLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
            Label noteLabel = new Label(t.getNote() != null ? t.getNote() : "");
            noteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            Label dateLabel = new Label(t.getDate().toString());
            dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
            infoBox.getChildren().addAll(catLabel, noteLabel, dateLabel);
            
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            Label amountLabel = new Label(String.format("%s%,.0f VND", isIncome ? "+" : "-", t.getAmount()));
            amountLabel.setStyle(String.format("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: %s;", isIncome ? "#2563eb" : "#ef4444"));
            
            Button editBtn = new Button("Sửa");
            editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #3b82f6; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold;");
            editBtn.setOnAction(e -> handleEditTransaction(t));
            
            Button deleteBtn = new Button("Xóa");
            deleteBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold;");
            deleteBtn.setOnAction(e -> handleDeleteTransaction(t));
            
            row.getChildren().addAll(icon, infoBox, spacer, amountLabel, editBtn, deleteBtn);
            transactionsListContainer.getChildren().add(row);
        }
    }
    
    private void handleDeleteTransaction(core.transaction.Transaction t) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa giao dịch này?");
        
        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            core.storage.TransactionDAO dao = new core.storage.TransactionDAO();
            dao.deleteTransaction(t.getId());
            
            if (t instanceof core.transaction.Income) {
                currentWallet.withdraw(t.getAmount());
            } else {
                currentWallet.deposit(t.getAmount());
            }
            
            core.storage.WalletDAO walletDAO = new core.storage.WalletDAO();
            walletDAO.updateBalance(currentWallet.getId(), currentWallet.getBalance());
            
            currentWallet.getTransactions().remove(t);
            updateOverviewData();
            renderTransactions();
            
            currentBalanceLabel.setText(String.format("%,.2f VND", currentWallet.getBalance()));
            if (overviewBalanceLabel != null) overviewBalanceLabel.setText(String.format("%,.2f VND", currentWallet.getBalance()));
        }
    }
    
    private void handleEditTransaction(core.transaction.Transaction oldT) {
        javafx.scene.control.Dialog<core.transaction.Transaction> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Sửa giao dịch");
        
        javafx.scene.control.ButtonType saveButton = new javafx.scene.control.ButtonType("Lưu", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, javafx.scene.control.ButtonType.CANCEL);
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));
        
        grid.add(new Label("Danh mục:"), 0, 0);
        javafx.scene.control.ComboBox<String> categoryCombo = new javafx.scene.control.ComboBox<>();
        for (core.Category cat : allCategories) {
            categoryCombo.getItems().add(cat.getName());
        }
        if (!categoryCombo.getItems().isEmpty()) {
            categoryCombo.getSelectionModel().select(oldT.getCategory().getName());
        }
        grid.add(categoryCombo, 0, 1);
        
        grid.add(new Label("Ngày:"), 1, 0);
        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker(oldT.getDate());
        grid.add(datePicker, 1, 1);
        
        grid.add(new Label("Ghi chú:"), 2, 0);
        javafx.scene.control.TextField noteField = new javafx.scene.control.TextField(oldT.getNote() != null ? oldT.getNote() : "");
        grid.add(noteField, 2, 1);
        
        grid.add(new Label("Số tiền:"), 3, 0);
        javafx.scene.control.TextField amountField = new javafx.scene.control.TextField(String.format("%.0f", oldT.getAmount()));
        grid.add(amountField, 3, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    double newAmount = Double.parseDouble(amountField.getText().replaceAll(",", ""));
                    java.time.LocalDate newDate = datePicker.getValue();
                    String newNote = noteField.getText();
                    String catName = categoryCombo.getValue();
                    
                    core.Category selectedCategory = allCategories.stream()
                        .filter(c -> c.getName().equals(catName))
                        .findFirst()
                        .orElse(new core.Category(catName != null ? catName : "Unknown", core.TransactionType.EXPENSE));
                        
                    core.transaction.Transaction newT;
                    if (selectedCategory.getType() == core.TransactionType.INCOME) {
                        newT = new core.transaction.Income(oldT.getId(), newAmount, newDate, newNote, selectedCategory, currentWallet, catName);
                    } else {
                        newT = new core.transaction.Expense(oldT.getId(), newAmount, newDate, newNote, selectedCategory, currentWallet, catName);
                    }
                    return newT;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
        
        java.util.Optional<core.transaction.Transaction> result = dialog.showAndWait();
        result.ifPresent(newT -> {
            if (oldT instanceof core.transaction.Income) {
                currentWallet.withdraw(oldT.getAmount());
            } else {
                currentWallet.deposit(oldT.getAmount());
            }
            
            currentWallet.getTransactions().remove(oldT);
            
            // newT constructor already updated the wallet balance!
            currentWallet.getTransactions().add(newT);
            
            core.storage.TransactionDAO dao = new core.storage.TransactionDAO();
            dao.updateTransaction(newT, currentWallet.getId());
            
            core.storage.WalletDAO walletDAO = new core.storage.WalletDAO();
            walletDAO.updateBalance(currentWallet.getId(), currentWallet.getBalance());
            
            updateOverviewData();
            renderTransactions();
            
            currentBalanceLabel.setText(String.format("%,.2f VND", currentWallet.getBalance()));
            if (overviewBalanceLabel != null) overviewBalanceLabel.setText(String.format("%,.2f VND", currentWallet.getBalance()));
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

    private void updateToggleStyles(Label active, Label inactive1, Label inactive2) {
        active.getStyleClass().remove("chart-toggle");
        if (!active.getStyleClass().contains("chart-toggle-active")) active.getStyleClass().add("chart-toggle-active");
        
        inactive1.getStyleClass().remove("chart-toggle-active");
        if (!inactive1.getStyleClass().contains("chart-toggle")) inactive1.getStyleClass().add("chart-toggle");
        
        inactive2.getStyleClass().remove("chart-toggle-active");
        if (!inactive2.getStyleClass().contains("chart-toggle")) inactive2.getStyleClass().add("chart-toggle");
    }

    @FXML private void handleBalDays() { balanceViewMode = ChartViewMode.DAYS; updateToggleStyles(balDaysLbl, balWeeksLbl, balMonthsLbl); updateOverviewData(); }
    @FXML private void handleBalWeeks() { balanceViewMode = ChartViewMode.WEEKS; updateToggleStyles(balWeeksLbl, balDaysLbl, balMonthsLbl); updateOverviewData(); }
    @FXML private void handleBalMonths() { balanceViewMode = ChartViewMode.MONTHS; updateToggleStyles(balMonthsLbl, balDaysLbl, balWeeksLbl); updateOverviewData(); }

    @FXML private void handleChgDays() { changesViewMode = ChartViewMode.DAYS; updateToggleStyles(chgDaysLbl, chgWeeksLbl, chgMonthsLbl); updateOverviewData(); }
    @FXML private void handleChgWeeks() { changesViewMode = ChartViewMode.WEEKS; updateToggleStyles(chgWeeksLbl, chgDaysLbl, chgMonthsLbl); updateOverviewData(); }
    @FXML private void handleChgMonths() { changesViewMode = ChartViewMode.MONTHS; updateToggleStyles(chgMonthsLbl, chgDaysLbl, chgWeeksLbl); updateOverviewData(); }

    @FXML private javafx.scene.layout.VBox tabTransactions;
    @FXML private javafx.scene.control.ScrollPane transactionsScrollPane;
    @FXML private javafx.scene.layout.VBox transactionsListContainer;
    @FXML private javafx.scene.layout.VBox emptyTransactionsState;
    @FXML private javafx.scene.layout.VBox tabOverview;
    @FXML private javafx.scene.layout.VBox tabBudgets;
    @FXML private javafx.scene.layout.HBox tabSettings;
    
    @FXML private javafx.scene.layout.VBox menuTransactions;
    @FXML private javafx.scene.layout.VBox menuOverview;
    @FXML private javafx.scene.layout.VBox menuBudgets;
    @FXML private javafx.scene.layout.VBox menuSettings;

    // Inner Settings Fields
    @FXML private Label menuSettingsMain;
    @FXML private Label menuSettingsCat;
    @FXML private javafx.scene.layout.VBox settingsMain;
    @FXML private javafx.scene.layout.VBox settingsCategories;
    @FXML private javafx.scene.control.TextField settingWalletName;
    @FXML private javafx.scene.control.ComboBox<String> categoryTypeCombo;
    @FXML private javafx.scene.control.TextField newCategoryNameField;
    @FXML private javafx.scene.layout.VBox incomeCategoriesContainer;
    @FXML private javafx.scene.layout.VBox expenseCategoriesContainer;
    
    private java.util.List<core.Category> allCategories = new java.util.ArrayList<>();

    private void loadCategoriesToUI() {
        if (incomeCategoriesContainer == null || expenseCategoriesContainer == null) return;
        
        incomeCategoriesContainer.getChildren().clear();
        expenseCategoriesContainer.getChildren().clear();
        allCategories.clear();
        
        core.storage.TransactionDAO dao = new core.storage.TransactionDAO();
        allCategories = dao.getAllCategories();
        
        for (core.Category cat : allCategories) {
            javafx.scene.layout.HBox item = new javafx.scene.layout.HBox(15);
            item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            item.getStyleClass().add("category-list-item");
            
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(12, 
                cat.getType() == core.TransactionType.INCOME ? javafx.scene.paint.Color.web("#3b82f6") : javafx.scene.paint.Color.web("#f472b6"));
            
            Label nameLabel = new Label(cat.getName());
            nameLabel.getStyleClass().add("category-list-name");
            
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            Label countLabel = new Label("0 transactions");
            countLabel.getStyleClass().add("category-list-count");
            
            Label actionLabel = new Label("⚙");
            actionLabel.getStyleClass().add("category-list-action");
            
            Label deleteLabel = new Label("🗑");
            deleteLabel.getStyleClass().add("category-list-action-danger");
            deleteLabel.setCursor(javafx.scene.Cursor.HAND);
            deleteLabel.setOnMouseClicked(e -> {
                try {
                    dao.softDeleteCategory(cat.getName(), cat.getType().name());
                    loadCategoriesToUI();
                    System.out.println("Category deleted: " + cat.getName());
                } catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
            });
            
            item.getChildren().addAll(circle, nameLabel, spacer, countLabel, actionLabel, deleteLabel);
            
            if (cat.getType() == core.TransactionType.INCOME) {
                incomeCategoriesContainer.getChildren().add(item);
            } else {
                expenseCategoriesContainer.getChildren().add(item);
            }
        }
    }

    @FXML
    private void handleCreateCategory() {
        if (newCategoryNameField == null || categoryTypeCombo == null) return;
        
        String name = newCategoryNameField.getText();
        String type = categoryTypeCombo.getValue();
        
        if (name == null || name.trim().isEmpty() || type == null) {
            return;
        }
        
        try {
            core.storage.TransactionDAO dao = new core.storage.TransactionDAO();
            dao.getOrCreateCategoryId(name.trim(), type.toUpperCase());
            newCategoryNameField.clear();
            loadCategoriesToUI();
            System.out.println("Category successfully created: " + name + " (" + type + ")");
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToTransactions() {
        switchTab(tabTransactions, menuTransactions);
    }

    @FXML
    private void switchToOverview() {
        switchTab(tabOverview, menuOverview);
    }

    @FXML
    private void switchToBudgets() {
        switchTab(tabBudgets, menuBudgets);
    }

    @FXML
    private void switchToSettings() {
        switchTab(tabSettings, menuSettings);
        if (settingWalletName != null && currentWallet != null) {
            settingWalletName.setText(currentWallet.getName());
        }
    }

    private void switchTab(javafx.scene.Node tab, javafx.scene.layout.VBox menu) {
        tabTransactions.setVisible(false);
        tabOverview.setVisible(false);
        tabBudgets.setVisible(false);
        tabSettings.setVisible(false);
        tab.setVisible(true);

        menuTransactions.getStyleClass().remove("nav-item-active-container");
        menuTransactions.getStyleClass().add("nav-item-container");
        menuOverview.getStyleClass().remove("nav-item-active-container");
        menuOverview.getStyleClass().add("nav-item-container");
        menuBudgets.getStyleClass().remove("nav-item-active-container");
        menuBudgets.getStyleClass().add("nav-item-container");
        menuSettings.getStyleClass().remove("nav-item-active-container");
        menuSettings.getStyleClass().add("nav-item-container");

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
    private void showAddTransactionDialog() {
        javafx.scene.control.Dialog<core.transaction.Transaction> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Add transaction");
        
        javafx.scene.control.ButtonType addButton = new javafx.scene.control.ButtonType("Add transaction", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, javafx.scene.control.ButtonType.CANCEL);
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));

        grid.add(new Label("Category:"), 0, 0);
        javafx.scene.control.ComboBox<String> categoryCombo = new javafx.scene.control.ComboBox<>();
        for (core.Category cat : allCategories) {
            categoryCombo.getItems().add(cat.getName());
        }
        if (!categoryCombo.getItems().isEmpty()) categoryCombo.getSelectionModel().selectFirst();
        grid.add(categoryCombo, 0, 1);
        
        grid.add(new Label("Date:"), 1, 0);
        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker(java.time.LocalDate.now());
        grid.add(datePicker, 1, 1);
        
        grid.add(new Label("Note:"), 2, 0);
        javafx.scene.control.TextField noteField = new javafx.scene.control.TextField();
        grid.add(noteField, 2, 1);

        grid.add(new Label("Amount:"), 3, 0);
        javafx.scene.control.TextField amountField = new javafx.scene.control.TextField("0");
        grid.add(amountField, 3, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                String catName = categoryCombo.getValue();
                java.time.LocalDate date = datePicker.getValue();
                String note = noteField.getText();
                double amount = 0.0;
                try {
                    amount = Double.parseDouble(amountField.getText());
                } catch (NumberFormatException e) {
                    amount = 0.0;
                }
                
                core.Category selectedCategory = allCategories.stream().filter(c -> c.getName().equals(catName)).findFirst().orElse(new core.Category(catName != null ? catName : "Unknown", core.TransactionType.EXPENSE));
                
                core.transaction.Transaction t;
                if (selectedCategory.getType() == core.TransactionType.INCOME) {
                    t = new core.transaction.Income((int)(Math.random()*10000), amount, date, note, selectedCategory, currentWallet, catName);
                } else {
                    t = new core.transaction.Expense((int)(Math.random()*10000), amount, date, note, selectedCategory, currentWallet, catName);
                }
                return t;
            }
            return null;
        });

        java.util.Optional<core.transaction.Transaction> result = dialog.showAndWait();
        result.ifPresent(transaction -> {
            currentWallet.addTransaction(transaction);
            
            // Database Persistence
            core.storage.TransactionDAO transactionDAO = new core.storage.TransactionDAO();
            transactionDAO.saveTransaction(transaction, currentWallet.getId());
            
            core.storage.WalletDAO walletDAO = new core.storage.WalletDAO();
            walletDAO.updateBalance(currentWallet.getId(), currentWallet.getBalance());
            
            updateOverviewData();
            renderTransactions();
            
            // Refresh wallet balance labels
            currentBalanceLabel.setText(String.format("%,.2f VND", currentWallet.getBalance()));
            if (overviewBalanceLabel != null) {
                overviewBalanceLabel.setText(String.format("%,.2f VND", currentWallet.getBalance()));
            }
        });
    }

    private void updateOverviewData() {
        if (currentWallet == null) return;
        
        double totalIncome = 0;
        double totalExpense = 0;
        
        java.util.Map<String, Double> incomeByCategory = new java.util.HashMap<>();
        java.util.Map<String, Double> expenseByCategory = new java.util.HashMap<>();
        java.util.Map<java.time.LocalDate, Double> balanceByDate = new java.util.TreeMap<>();
        java.util.Map<java.time.LocalDate, Double> incomeByDate = new java.util.TreeMap<>();
        java.util.Map<java.time.LocalDate, Double> expenseByDate = new java.util.TreeMap<>();
        
        // Mock starting balance point (assuming 0 before transactions for demo purposes)
        if (currentWallet.getTransactions().isEmpty()) {
            balanceByDate.put(java.time.LocalDate.now().minusDays(1), 0.0);
        } else {
            balanceByDate.put(currentWallet.getTransactions().get(0).getDate().minusDays(1), 0.0);
        }

        double runningBalance = 0;

        // Sort transactions by date for correct balance tracking
        java.util.List<core.transaction.Transaction> sortedTransactions = new java.util.ArrayList<>(currentWallet.getTransactions());
        sortedTransactions.sort(java.util.Comparator.comparing(core.transaction.Transaction::getDate));
        
        for (core.transaction.Transaction t : sortedTransactions) {
            java.time.LocalDate date = t.getDate();
            double amount = t.getAmount();
            String catName = t.getCategory().getName();
            
            if (t.getType() == core.TransactionType.INCOME) {
                totalIncome += amount;
                incomeByCategory.put(catName, incomeByCategory.getOrDefault(catName, 0.0) + amount);
                incomeByDate.put(date, incomeByDate.getOrDefault(date, 0.0) + amount);
                runningBalance += amount;
            } else {
                totalExpense += amount;
                expenseByCategory.put(catName, expenseByCategory.getOrDefault(catName, 0.0) + amount);
                expenseByDate.put(date, expenseByDate.getOrDefault(date, 0.0) - amount); // negative for bar chart
                runningBalance -= amount;
            }
            balanceByDate.put(date, runningBalance);
        }
        
        double totalChange = totalIncome - totalExpense;

        // Update Labels
        if (overviewIncomeLabel != null) overviewIncomeLabel.setText(String.format("+%,.2f VND", totalIncome));
        if (overviewExpenseLabel != null) overviewExpenseLabel.setText(String.format("-%,.2f VND", totalExpense));
        if (overviewChangeLabel != null) {
            overviewChangeLabel.setText(String.format("%s%,.2f VND", totalChange >= 0 ? "+" : "", totalChange));
            overviewChangeLabel.setStyle(totalChange >= 0 ? "-fx-text-fill: #3b82f6;" : "-fx-text-fill: #ef4444;");
        }

        // Helper formatter for charts
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy");
        javafx.util.StringConverter<Number> formatterVND = new javafx.util.StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                double val = object.doubleValue();
                if (val == 0) return "0.00 VND";
                return String.format("%s%,.2f VND", val > 0 ? "+" : "", val);
            }
            @Override
            public Number fromString(String string) { return null; }
        };

        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate startDate = now.withDayOfMonth(1);
        java.time.LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        
        // Calculate daily balances for the stepped area chart
        java.util.Map<java.time.LocalDate, Double> dailyBalance = new java.util.TreeMap<>();
        double currentBal = 0;
        // First add up everything before startDate if any
        for (java.time.LocalDate d : balanceByDate.keySet()) {
            if (d.isBefore(startDate)) {
                currentBal = balanceByDate.get(d); 
            }
        }
        
        for (java.time.LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            double dailyInc = incomeByDate.getOrDefault(d, 0.0);
            double dailyExp = expenseByDate.getOrDefault(d, 0.0); // negative
            currentBal += dailyInc;
            currentBal += dailyExp;
            dailyBalance.put(d, currentBal);
        }

        java.util.List<ChartBucket> balBuckets = createBuckets(balanceViewMode, startDate, endDate, dailyBalance, incomeByDate, expenseByDate);
        java.util.List<ChartBucket> chgBuckets = createBuckets(changesViewMode, startDate, endDate, dailyBalance, incomeByDate, expenseByDate);

        // Update Balance Chart
        if (balanceChart != null) {
            if (balanceChart.getYAxis() instanceof javafx.scene.chart.NumberAxis) {
                ((javafx.scene.chart.NumberAxis) balanceChart.getYAxis()).setTickLabelFormatter(formatterVND);
            }
            javafx.scene.chart.XYChart.Series<String, Number> balanceSeries;
            if (balanceChart.getData().isEmpty()) {
                balanceSeries = new javafx.scene.chart.XYChart.Series<>();
                balanceChart.getData().add(balanceSeries);
            } else {
                balanceSeries = balanceChart.getData().get(0);
                balanceSeries.getData().clear();
            }
            
            for (ChartBucket bucket : balBuckets) {
                double balVal = bucket.balanceAtEnd;
                javafx.scene.chart.XYChart.Data<String, Number> balData = new javafx.scene.chart.XYChart.Data<>(bucket.label, balVal);
                balanceSeries.getData().add(balData);
                
                balData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip();
                        tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                        javafx.scene.layout.VBox tbox = new javafx.scene.layout.VBox(5);
                        tbox.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-background-radius: 4px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); -fx-border-color: #e2e8f0; -fx-border-radius: 4px;");
                        javafx.scene.control.Label dLbl = new javafx.scene.control.Label(bucket.tooltipDateRange);
                        dLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
                        javafx.scene.control.Label bLbl = new javafx.scene.control.Label(String.format("Balance: %,.2f VND", balVal));
                        bLbl.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                        tbox.getChildren().addAll(dLbl, bLbl);
                        tooltip.setGraphic(tbox);
                        javafx.scene.control.Tooltip.install(newNode, tooltip);
                    }
                });
            }
        }

        // Update Changes Chart
        if (changesChart != null) {
            if (changesChart.getYAxis() instanceof javafx.scene.chart.NumberAxis) {
                ((javafx.scene.chart.NumberAxis) changesChart.getYAxis()).setTickLabelFormatter(formatterVND);
            }
            javafx.scene.chart.XYChart.Series<String, Number> incomeSeries;
            javafx.scene.chart.XYChart.Series<String, Number> expenseSeries;
            
            if (changesChart.getData().size() >= 2) {
                incomeSeries = changesChart.getData().get(0);
                expenseSeries = changesChart.getData().get(1);
                incomeSeries.getData().clear();
                expenseSeries.getData().clear();
            } else {
                incomeSeries = new javafx.scene.chart.XYChart.Series<>();
                expenseSeries = new javafx.scene.chart.XYChart.Series<>();
                changesChart.getData().addAll(incomeSeries, expenseSeries);
            }

            for (ChartBucket bucket : chgBuckets) {
                double incVal = bucket.totalIncome;
                double expVal = bucket.totalExpense;
                
                javafx.scene.chart.XYChart.Data<String, Number> incData = new javafx.scene.chart.XYChart.Data<>(bucket.label, incVal);
                javafx.scene.chart.XYChart.Data<String, Number> expData = new javafx.scene.chart.XYChart.Data<>(bucket.label, expVal);
                
                incomeSeries.getData().add(incData);
                expenseSeries.getData().add(expData);
                
                javafx.beans.value.ChangeListener<javafx.scene.Node> nodeListener = (obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip();
                        tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                        javafx.scene.layout.VBox tbox = new javafx.scene.layout.VBox(5);
                        tbox.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-background-radius: 4px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); -fx-border-color: #e2e8f0; -fx-border-radius: 4px;");
                        javafx.scene.control.Label dLbl = new javafx.scene.control.Label(bucket.tooltipDateRange);
                        dLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
                        javafx.scene.control.Label iLbl = new javafx.scene.control.Label(String.format("Income: +%,.2f VND", incVal));
                        iLbl.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                        javafx.scene.control.Label eLbl = new javafx.scene.control.Label(String.format("Expense: %,.2f VND", expVal));
                        eLbl.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                        tbox.getChildren().addAll(dLbl, iLbl, eLbl);
                        tooltip.setGraphic(tbox);
                        javafx.scene.control.Tooltip.install(newNode, tooltip);
                    }
                };
                
                incData.nodeProperty().addListener(nodeListener);
                expData.nodeProperty().addListener(nodeListener);
            }
        }

        // Update Pie Charts
        if (incomePieChart != null) {
            incomePieChart.getData().clear();
            for (java.util.Map.Entry<String, Double> entry : incomeByCategory.entrySet()) {
                incomePieChart.getData().add(new javafx.scene.chart.PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }
        if (expensePieChart != null) {
            expensePieChart.getData().clear();
            for (java.util.Map.Entry<String, Double> entry : expenseByCategory.entrySet()) {
                expensePieChart.getData().add(new javafx.scene.chart.PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }

        String[] incomeColors = {"#2563eb", "#60a5fa", "#1d4ed8"};
        updateLegend(incomeLegendBox, incomeByCategory, incomeColors, true);

        String[] expenseColors = {"#ef4444", "#f59e0b", "#f97316", "#ec4899"};
        updateLegend(expenseLegendBox, expenseByCategory, expenseColors, false);
    }

    private void updateLegend(javafx.scene.layout.VBox legendBox, java.util.Map<String, Double> data, String[] colors, boolean isIncome) {
        if (legendBox == null) return;
        legendBox.getChildren().clear();
        int i = 0;
        for (java.util.Map.Entry<String, Double> entry : data.entrySet()) {
            String color = colors[i % colors.length];
            
            javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox();
            hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            hbox.setSpacing(10);
            
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(8, javafx.scene.paint.Color.web(color));
            
            javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(entry.getKey());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568; -fx-font-size: 13px;");
            
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            javafx.scene.control.Label amountLabel = new javafx.scene.control.Label(String.format("%s%,.2f VND", isIncome ? "+" : "-", entry.getValue()));
            amountLabel.setStyle(String.format("-fx-font-weight: bold; -fx-text-fill: %s; -fx-font-size: 13px;", isIncome ? "#2563eb" : "#ef4444"));
            
            hbox.getChildren().addAll(circle, nameLabel, spacer, amountLabel);
            legendBox.getChildren().add(hbox);
            i++;
        }
    }

    public static class ChartBucket {
        public String label;
        public double balanceAtEnd;
        public double totalIncome;
        public double totalExpense;
        public String tooltipDateRange;
    }

    private java.util.List<ChartBucket> createBuckets(ChartViewMode mode, java.time.LocalDate startDate, java.time.LocalDate endDate, 
                                                     java.util.Map<java.time.LocalDate, Double> dailyBalance,
                                                     java.util.Map<java.time.LocalDate, Double> incomeByDate,
                                                     java.util.Map<java.time.LocalDate, Double> expenseByDate) {
        java.util.List<ChartBucket> buckets = new java.util.ArrayList<>();
        
        if (mode == ChartViewMode.DAYS) {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy");
            for (java.time.LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                ChartBucket b = new ChartBucket();
                b.label = d.format(fmt);
                b.tooltipDateRange = d.format(fmt);
                if (!(d.getDayOfMonth() == 1 || d.getDayOfMonth() == 8 || d.getDayOfMonth() == 15 || d.getDayOfMonth() == 22 || d.equals(endDate))) {
                    b.label = String.format("%" + d.getDayOfMonth() + "s", ""); // spaces
                }
                b.balanceAtEnd = dailyBalance.getOrDefault(d, 0.0);
                b.totalIncome = incomeByDate.getOrDefault(d, 0.0);
                b.totalExpense = expenseByDate.getOrDefault(d, 0.0);
                buckets.add(b);
            }
        } else if (mode == ChartViewMode.WEEKS) {
            java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault());
            int currentWeek = -1;
            ChartBucket currentBucket = null;
            for (java.time.LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                int weekNum = d.get(weekFields.weekOfMonth());
                if (weekNum != currentWeek) {
                    currentWeek = weekNum;
                    currentBucket = new ChartBucket();
                    currentBucket.label = "Week " + weekNum;
                    currentBucket.tooltipDateRange = "Week " + weekNum;
                    buckets.add(currentBucket);
                }
                currentBucket.balanceAtEnd = dailyBalance.getOrDefault(d, 0.0); // gets overwritten, so it takes the last day's balance
                currentBucket.totalIncome += incomeByDate.getOrDefault(d, 0.0);
                currentBucket.totalExpense += expenseByDate.getOrDefault(d, 0.0);
            }
        } else if (mode == ChartViewMode.MONTHS) {
            ChartBucket bucket = new ChartBucket();
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy");
            bucket.label = startDate.format(fmt);
            bucket.tooltipDateRange = startDate.format(fmt);
            for (java.time.LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                bucket.balanceAtEnd = dailyBalance.getOrDefault(d, 0.0); // gets the last day
                bucket.totalIncome += incomeByDate.getOrDefault(d, 0.0);
                bucket.totalExpense += expenseByDate.getOrDefault(d, 0.0);
            }
            buckets.add(bucket);
        }
        return buckets;
    }
}

