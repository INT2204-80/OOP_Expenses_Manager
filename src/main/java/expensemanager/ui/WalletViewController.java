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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import core.storage.WalletDAO;
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
    
    // Period state
    private java.time.LocalDate currentPeriodStart = java.time.LocalDate.now().withDayOfMonth(1);
    private java.time.LocalDate currentPeriodEnd = java.time.LocalDate.now().withDayOfMonth(java.time.LocalDate.now().lengthOfMonth());
    
    @FXML private Label periodLabelOverview;
    @FXML private Label periodLabelTrans;
    @FXML private Label transChangeLabel;
    @FXML private Label transExpenseLabel;
    @FXML private Label transIncomeLabel;
    
    @FXML private Label deleteWalletLabel;

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
        String curr = wallet.getCurrency();
        currentBalanceLabel.setText(String.format("%,.2f %s", wallet.getBalance(), curr));
        if (overviewBalanceLabel != null) {
            overviewBalanceLabel.setText(String.format("%,.2f %s", wallet.getBalance(), curr));
        }
        
        if (categoryIconCombo != null) {
            categoryIconCombo.getItems().addAll("\uD83D\uDCB0", "\uD83C\uDF74", "\uD83D\uDE97", "\uD83D\uDECD\uFE0F", "\uD83C\uDFE0", "\uD83C\uDFAE", "\uD83C\uDFE5", "\uD83D\uDCDA", "\u2708\uFE0F", "\uD83C\uDFAC", "\uD83D\uDC57", "\uD83D\uDC3E", "\uD83D\uDCF1", "\uD83C\uDF81");
            
            javafx.util.Callback<javafx.scene.control.ListView<String>, javafx.scene.control.ListCell<String>> iconCellFactory = param -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(null);
                        javafx.scene.control.Label iconLabel = new javafx.scene.control.Label(item);
                        iconLabel.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif; -fx-font-size: 16px; -fx-text-fill: #1e293b;");
                        setGraphic(iconLabel);
                    }
                }
            };
            categoryIconCombo.setCellFactory(iconCellFactory);
            categoryIconCombo.setButtonCell(iconCellFactory.call(null));
            categoryIconCombo.getSelectionModel().selectFirst();
        }
        if (categoryColorCombo != null) {
            categoryColorCombo.getItems().addAll("Blue", "Red", "Green", "Yellow", "Purple", "Orange", "Pink", "Teal", "Indigo", "Cyan", "Gray");
            
            javafx.util.Callback<javafx.scene.control.ListView<String>, javafx.scene.control.ListCell<String>> colorCellFactory = param -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(7);
                        switch (item.toLowerCase()) {
                            case "blue": circle.setFill(javafx.scene.paint.Color.web("#3b82f6")); break;
                            case "red": circle.setFill(javafx.scene.paint.Color.web("#ef4444")); break;
                            case "green": circle.setFill(javafx.scene.paint.Color.web("#10b981")); break;
                            case "yellow": circle.setFill(javafx.scene.paint.Color.web("#f59e0b")); break;
                            case "purple": circle.setFill(javafx.scene.paint.Color.web("#8b5cf6")); break;
                            case "orange": circle.setFill(javafx.scene.paint.Color.web("#f97316")); break;
                            case "pink": circle.setFill(javafx.scene.paint.Color.web("#ec4899")); break;
                            case "teal": circle.setFill(javafx.scene.paint.Color.web("#14b8a6")); break;
                            case "indigo": circle.setFill(javafx.scene.paint.Color.web("#6366f1")); break;
                            case "cyan": circle.setFill(javafx.scene.paint.Color.web("#06b6d4")); break;
                            case "gray": circle.setFill(javafx.scene.paint.Color.web("#64748b")); break;
                            default: circle.setFill(javafx.scene.paint.Color.GRAY); break;
                        }
                        setGraphic(circle);
                        setText(item);
                    }
                }
            };
            categoryColorCombo.setCellFactory(colorCellFactory);
            categoryColorCombo.setButtonCell(colorCellFactory.call(null));
            categoryColorCombo.getSelectionModel().selectFirst();
        }

        if (categoryTypeCombo != null) {
            categoryTypeCombo.getItems().addAll("Expense", "Income");
            categoryTypeCombo.getSelectionModel().selectFirst();
        }

        loadCategoriesToUI();
        
        // Initialize filters
        if (filterCategoryCombo != null) {
            filterCategoryCombo.getItems().clear();
            filterCategoryCombo.getItems().add("All categories");
            for (core.Category cat : allCategories) {
                filterCategoryCombo.getItems().add(cat.getName());
            }
            filterCategoryCombo.getSelectionModel().selectFirst();
            filterCategoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateOverviewData();
                renderTransactions();
            });
        }
        if (filterNoteField != null) {
            filterNoteField.textProperty().addListener((obs, oldVal, newVal) -> {
                updateOverviewData();
                renderTransactions();
            });
        }
        if (filterMinAmountField != null) {
            filterMinAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
                updateOverviewData();
                renderTransactions();
            });
        }
        if (filterMaxAmountField != null) {
            filterMaxAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
                updateOverviewData();
                renderTransactions();
            });
        }
        
        updateOverviewData();
        renderTransactions();
    }
    
    private void renderTransactions() {
        if (transactionsListContainer == null || emptyTransactionsState == null || transactionsScrollPane == null) return;
        
        transactionsListContainer.getChildren().clear();
        
        java.util.List<core.transaction.Transaction> filteredTransactions = getFilteredTransactions();
        
        if (filteredTransactions.isEmpty()) {
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
        
        for (core.transaction.Transaction t : filteredTransactions) {
            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(15);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");
            
            boolean isIncome = t instanceof core.transaction.Income;
              
            String cStr = t.getCategory().getColor();
            javafx.scene.paint.Color fxColor;
            if (cStr != null) {
                switch (cStr.toLowerCase()) {
                    case "blue": fxColor = javafx.scene.paint.Color.web("#3b82f6"); break;
                    case "red": fxColor = javafx.scene.paint.Color.web("#ef4444"); break;
                    case "green": fxColor = javafx.scene.paint.Color.web("#10b981"); break;
                    case "yellow": fxColor = javafx.scene.paint.Color.web("#f59e0b"); break;
                    case "purple": fxColor = javafx.scene.paint.Color.web("#8b5cf6"); break;
                    case "orange": fxColor = javafx.scene.paint.Color.web("#f97316"); break;
                    case "pink": fxColor = javafx.scene.paint.Color.web("#ec4899"); break;
                    case "teal": fxColor = javafx.scene.paint.Color.web("#14b8a6"); break;
                    case "indigo": fxColor = javafx.scene.paint.Color.web("#6366f1"); break;
                    case "cyan": fxColor = javafx.scene.paint.Color.web("#06b6d4"); break;
                    case "gray": fxColor = javafx.scene.paint.Color.web("#64748b"); break;
                    default: fxColor = javafx.scene.paint.Color.web(isIncome ? "#2563eb" : "#ef4444");
                }
            } else {
                fxColor = javafx.scene.paint.Color.web(isIncome ? "#2563eb" : "#ef4444");
            }
            
            javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
            javafx.scene.shape.Circle icon = new javafx.scene.shape.Circle(20, fxColor);
            iconPane.getChildren().add(icon);
            if (t.getCategory().getIcon() != null && !t.getCategory().getIcon().isEmpty()) {
                Label iconLabel = new Label(t.getCategory().getIcon());
                iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
                iconPane.getChildren().add(iconLabel);
            }
            
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
            
            String curr = currentWallet != null ? currentWallet.getCurrency() : "VND";
            Label amountLabel = new Label(String.format("%s%,.0f %s", isIncome ? "+" : "-", t.getAmount(), curr));
            amountLabel.setStyle(String.format("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: %s;", isIncome ? "#2563eb" : "#ef4444"));
            
            Button editBtn = new Button("Sửa");
            editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #3b82f6; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold;");
            editBtn.setOnAction(e -> handleEditTransaction(t));
            
            Button deleteBtn = new Button("Xóa");
            deleteBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold;");
            deleteBtn.setOnAction(e -> handleDeleteTransaction(t));
            
            row.getChildren().addAll(iconPane, infoBox, spacer, amountLabel, editBtn, deleteBtn);
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
            
            String curr = currentWallet.getCurrency();
            currentBalanceLabel.setText(String.format("%,.2f %s", currentWallet.getBalance(), curr));
            if (overviewBalanceLabel != null) overviewBalanceLabel.setText(String.format("%,.2f %s", currentWallet.getBalance(), curr));
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
        categoryCombo.getItems().add("--- CHI TIÊU (EXPENSE) ---");
        for (core.Category cat : allCategories) {
            if (cat.getType() == core.TransactionType.EXPENSE) categoryCombo.getItems().add(cat.getName());
        }
        categoryCombo.getItems().add("--- THU NHẬP (INCOME) ---");
        for (core.Category cat : allCategories) {
            if (cat.getType() == core.TransactionType.INCOME) categoryCombo.getItems().add(cat.getName());
        }
        categoryCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("---")) {
                        setDisable(true);
                        setStyle("-fx-font-weight: bold; -fx-text-fill: gray;");
                    } else {
                        setDisable(false);
                        setStyle("");
                    }
                }
            }
        });
        categoryCombo.getSelectionModel().select(oldT.getCategory().getName());
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
        
        javafx.scene.control.CheckBox recurringCheck = new javafx.scene.control.CheckBox("Lặp lại (Recurring)");
        grid.add(recurringCheck, 0, 2);
        
        javafx.scene.control.ComboBox<String> periodCombo = new javafx.scene.control.ComboBox<>();
        periodCombo.getItems().addAll("Hàng ngày", "Hàng tuần", "Hàng tháng", "Hàng năm");
        periodCombo.getSelectionModel().select("Hàng tháng");
        periodCombo.setDisable(true);
        grid.add(periodCombo, 1, 2);
        
        categoryCombo.setOnAction(e -> {
            String catName = categoryCombo.getValue();
            if (catName != null && catName.startsWith("---")) return;
            core.Category selectedCategory = allCategories.stream().filter(c -> c.getName().equals(catName)).findFirst().orElse(null);
            if (selectedCategory != null && selectedCategory.getType() == core.TransactionType.INCOME) {
                recurringCheck.setDisable(true);
                recurringCheck.setSelected(false);
                periodCombo.setDisable(true);
            } else {
                recurringCheck.setDisable(false);
                periodCombo.setDisable(!recurringCheck.isSelected());
            }
        });

        if (oldT instanceof core.transaction.RecurringExpense) {
            recurringCheck.setSelected(true);
            periodCombo.setDisable(false);
            core.transaction.RecurringExpense re = (core.transaction.RecurringExpense) oldT;
            if (re.getPeriod().equals(java.time.Period.ofDays(1))) periodCombo.getSelectionModel().select("Hàng ngày");
            else if (re.getPeriod().equals(java.time.Period.ofWeeks(1))) periodCombo.getSelectionModel().select("Hàng tuần");
            else if (re.getPeriod().equals(java.time.Period.ofYears(1))) periodCombo.getSelectionModel().select("Hàng năm");
            else periodCombo.getSelectionModel().select("Hàng tháng");
        } else if (oldT.getCategory().getType() == core.TransactionType.INCOME) {
            recurringCheck.setDisable(true);
        }
        
        recurringCheck.setOnAction(e -> {
            periodCombo.setDisable(!recurringCheck.isSelected());
        });
        
        dialog.getDialogPane().setContent(grid);
        
        final javafx.scene.control.Button editOkBtn = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(saveButton);
        editOkBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                double amt = Double.parseDouble(amountField.getText().replaceAll(",", ""));
                if (amt <= 0) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "S\u1ed1 ti\u1ec1n ph\u1ea3i l\u1edbn h\u01a1n 0", javafx.scene.control.ButtonType.OK);
                    alert.showAndWait();
                    event.consume();
                    return;
                }
                if (amt > 1000000000000L) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "S\u1ed1 ti\u1ec1n qu\u00e1 l\u1edbn", javafx.scene.control.ButtonType.OK);
                    alert.showAndWait();
                    event.consume();
                    return;
                }
                String catName = categoryCombo.getValue();
                if (catName == null || catName.startsWith("---")) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Vui lòng chọn một danh mục hợp lệ", javafx.scene.control.ButtonType.OK);
                    alert.showAndWait();
                    event.consume();
                    return;
                }
                core.Category cat = allCategories.stream().filter(c -> c.getName().equals(catName)).findFirst().orElse(null);
                
                double simulatedBalance = currentWallet.getBalance();
                if (oldT instanceof core.transaction.Income) {
                    simulatedBalance -= oldT.getAmount();
                } else {
                    simulatedBalance += oldT.getAmount();
                }

                if (cat != null && cat.getType() == core.TransactionType.EXPENSE && amt > simulatedBalance) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "S\u1ed1 ti\u1ec1n chi ti\u00eau kh\u00f4ng \u0111\u01b0\u1ee3c v\u01b0\u1ee3t qu\u00e1 s\u1ed1 d\u01b0 v\u00ed", javafx.scene.control.ButtonType.OK);
                    alert.showAndWait();
                    event.consume();
                    return;
                }
            } catch (NumberFormatException e) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "S\u1ed1 ti\u1ec1n kh\u00f4ng h\u1ee3p l\u1ec7", javafx.scene.control.ButtonType.OK);
                alert.showAndWait();
                event.consume();
            }
        });

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
                        if (recurringCheck.isSelected()) {
                            java.time.Period p;
                            String selP = periodCombo.getValue();
                            if ("Hàng ngày".equals(selP)) p = java.time.Period.ofDays(1);
                            else if ("Hàng tuần".equals(selP)) p = java.time.Period.ofWeeks(1);
                            else if ("Hàng năm".equals(selP)) p = java.time.Period.ofYears(1);
                            else p = java.time.Period.ofMonths(1);
                            
                            core.transaction.RecurringExpense newRe = new core.transaction.RecurringExpense(oldT.getId(), newAmount, newDate, newNote, selectedCategory, currentWallet, catName, p);
                            if (oldT instanceof core.transaction.RecurringExpense) {
                                newRe.setPassedPeriods(((core.transaction.RecurringExpense)oldT).getPassedPeriods());
                            }
                            newT = newRe;
                        } else {
                            newT = new core.transaction.Expense(oldT.getId(), newAmount, newDate, newNote, selectedCategory, currentWallet, catName);
                        }
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
            
            String curr = currentWallet.getCurrency();
            currentBalanceLabel.setText(String.format("%,.2f %s", currentWallet.getBalance(), curr));
            if (overviewBalanceLabel != null) overviewBalanceLabel.setText(String.format("%,.2f %s", currentWallet.getBalance(), curr));
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
    @FXML private javafx.scene.layout.VBox budgetsContainer;
    @FXML private javafx.scene.layout.HBox tabSettings;
    
    // Filter controls
    @FXML private javafx.scene.control.ComboBox<String> filterCategoryCombo;
    @FXML private javafx.scene.control.TextField filterNoteField;
    @FXML private javafx.scene.control.TextField filterMinAmountField;
    @FXML private javafx.scene.control.TextField filterMaxAmountField;
    @FXML private javafx.scene.control.Label resetFiltersLabel;
    
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
    @FXML private javafx.scene.control.TextField settingInitialBalance;
    @FXML private javafx.scene.control.ComboBox<String> settingCurrency;
    @FXML private javafx.scene.control.ComboBox<String> categoryTypeCombo;
    @FXML private javafx.scene.control.ComboBox<String> categoryIconCombo;
    @FXML private javafx.scene.control.ComboBox<String> categoryColorCombo;
    @FXML private javafx.scene.control.TextField newCategoryNameField;
    @FXML private javafx.scene.layout.VBox incomeCategoriesContainer;
    @FXML private javafx.scene.layout.VBox expenseCategoriesContainer;
    
    private java.util.List<core.Category> allCategories = new java.util.ArrayList<>();
    private java.util.Set<core.Category> selectedCategories = new java.util.HashSet<>();

    private void loadCategoriesToUI() {
        if (incomeCategoriesContainer == null || expenseCategoriesContainer == null) return;
        
        incomeCategoriesContainer.getChildren().clear();
        expenseCategoriesContainer.getChildren().clear();
        allCategories.clear();
        selectedCategories.clear();
        
        core.storage.TransactionDAO dao = new core.storage.TransactionDAO();
        allCategories = dao.getAllCategories();
        
        for (core.Category cat : allCategories) {
            javafx.scene.layout.HBox item = new javafx.scene.layout.HBox(15);
            item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            item.getStyleClass().add("category-list-item");
            
            javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox();
            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    selectedCategories.add(cat);
                } else {
                    selectedCategories.remove(cat);
                }
            });
            
            String cStr = cat.getColor();
            javafx.scene.paint.Color fxColor;
            if (cStr != null) {
                switch (cStr.toLowerCase()) {
                    case "blue": fxColor = javafx.scene.paint.Color.web("#3b82f6"); break;
                    case "red": fxColor = javafx.scene.paint.Color.web("#ef4444"); break;
                    case "green": fxColor = javafx.scene.paint.Color.web("#10b981"); break;
                    case "yellow": fxColor = javafx.scene.paint.Color.web("#f59e0b"); break;
                    case "purple": fxColor = javafx.scene.paint.Color.web("#8b5cf6"); break;
                    case "orange": fxColor = javafx.scene.paint.Color.web("#f97316"); break;
                    case "pink": fxColor = javafx.scene.paint.Color.web("#ec4899"); break;
                    case "teal": fxColor = javafx.scene.paint.Color.web("#14b8a6"); break;
                    case "indigo": fxColor = javafx.scene.paint.Color.web("#6366f1"); break;
                    case "cyan": fxColor = javafx.scene.paint.Color.web("#06b6d4"); break;
                    case "gray": fxColor = javafx.scene.paint.Color.web("#64748b"); break;
                    default: fxColor = cat.getType() == core.TransactionType.INCOME ? javafx.scene.paint.Color.web("#3b82f6") : javafx.scene.paint.Color.web("#f472b6");
                }
            } else {
                fxColor = cat.getType() == core.TransactionType.INCOME ? javafx.scene.paint.Color.web("#3b82f6") : javafx.scene.paint.Color.web("#f472b6");
            }
            
            javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(15, fxColor);
            iconPane.getChildren().add(circle);
            if (cat.getIcon() != null && !cat.getIcon().isEmpty()) {
                Label iconLabel = new Label(cat.getIcon());
                iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                iconPane.getChildren().add(iconLabel);
            }
            
            Label nameLabel = new Label(cat.getName());
            nameLabel.getStyleClass().add("category-list-name");
            
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            Label countLabel = new Label("0 transactions");
            countLabel.getStyleClass().add("category-list-count");
            
            Label actionLabel = new Label("\u2699");
            actionLabel.getStyleClass().add("category-list-action");
            actionLabel.setCursor(javafx.scene.Cursor.HAND);
            actionLabel.setOnMouseClicked(e -> showEditCategoryDialog(cat));
            
            Label deleteLabel = new Label("\uD83D\uDDD1");
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
            
            item.getChildren().addAll(checkBox, iconPane, nameLabel, spacer, countLabel, actionLabel, deleteLabel);
            
            if (cat.getType() == core.TransactionType.INCOME) {
                incomeCategoriesContainer.getChildren().add(item);
            } else {
                expenseCategoriesContainer.getChildren().add(item);
            }
        }
    }

    @FXML
    private void handleDeleteCategories() {
        if (selectedCategories.isEmpty()) return;
        
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xóa danh mục");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa " + selectedCategories.size() + " danh mục đã chọn?");
        
        if (alert.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK) {
            core.storage.TransactionDAO dao = new core.storage.TransactionDAO();
            for (core.Category cat : selectedCategories) {
                try {
                    dao.softDeleteCategory(cat.getName(), cat.getType().name());
                } catch (java.sql.SQLException e) {
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
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setHeaderText("Cần chọn ít nhất 2 danh mục để gộp!");
            alert.showAndWait();
            return;
        }

        java.util.List<core.Category> choices = new java.util.ArrayList<>(selectedCategories);
        javafx.scene.control.ChoiceDialog<core.Category> dialog = new javafx.scene.control.ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Gộp danh mục");
        dialog.setHeaderText("Chọn danh mục ĐÍCH (danh mục sẽ được giữ lại):");
        dialog.setContentText("Danh mục đích:");

        java.util.Optional<core.Category> result = dialog.showAndWait();
        if (result.isPresent()) {
            core.Category target = result.get();
            java.util.List<core.Category> sources = new java.util.ArrayList<>(selectedCategories);
            sources.remove(target); // Remove target from sources list

            core.storage.TransactionDAO dao = new core.storage.TransactionDAO();
            try {
                dao.mergeCategories(sources, target);
                selectedCategories.clear();
                loadCategoriesToUI();
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
                javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Có lỗi xảy ra khi gộp danh mục!");
                errorAlert.showAndWait();
            }
        }
    }

    @FXML
    private void showEditCategoryDialog(core.Category cat) {
        javafx.scene.control.Dialog<core.Category> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Chỉnh sửa danh mục");
        
        javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("Lưu", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CANCEL);
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));

        javafx.scene.control.TextField nameField = new javafx.scene.control.TextField(cat.getName());
        
        javafx.scene.control.ComboBox<String> typeCombo = new javafx.scene.control.ComboBox<>();
        typeCombo.getItems().addAll("Expense", "Income");
        String typeStr = cat.getType().name().substring(0, 1).toUpperCase() + cat.getType().name().substring(1).toLowerCase();
        typeCombo.setValue(typeStr);
        
        javafx.scene.control.ComboBox<String> iconCombo = new javafx.scene.control.ComboBox<>();
        iconCombo.getItems().addAll("\uD83D\uDCB0", "\uD83C\uDF74", "\uD83D\uDE97", "\uD83D\uDECD\uFE0F", "\uD83C\uDFE0", "\uD83C\uDFAE", "\uD83C\uDFE5", "\uD83D\uDCDA", "\u2708\uFE0F", "\uD83C\uDFAC", "\uD83D\uDC57", "\uD83D\uDC3E", "\uD83D\uDCF1", "\uD83C\uDF81");
        
        javafx.util.Callback<javafx.scene.control.ListView<String>, javafx.scene.control.ListCell<String>> iconCellFactory = param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    javafx.scene.control.Label iconLabel = new javafx.scene.control.Label(item);
                    iconLabel.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif; -fx-font-size: 16px; -fx-text-fill: #1e293b;");
                    setGraphic(iconLabel);
                }
            }
        };
        iconCombo.setCellFactory(iconCellFactory);
        iconCombo.setButtonCell(iconCellFactory.call(null));
        iconCombo.setValue(cat.getIcon());
        
        javafx.scene.control.ComboBox<String> colorCombo = new javafx.scene.control.ComboBox<>();
        colorCombo.getItems().addAll("Blue", "Red", "Green", "Yellow", "Purple", "Orange", "Pink", "Teal", "Indigo", "Cyan", "Gray");
        
        javafx.util.Callback<javafx.scene.control.ListView<String>, javafx.scene.control.ListCell<String>> colorCellFactory = param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(7);
                    switch (item.toLowerCase()) {
                        case "blue": circle.setFill(javafx.scene.paint.Color.web("#3b82f6")); break;
                        case "red": circle.setFill(javafx.scene.paint.Color.web("#ef4444")); break;
                        case "green": circle.setFill(javafx.scene.paint.Color.web("#10b981")); break;
                        case "yellow": circle.setFill(javafx.scene.paint.Color.web("#f59e0b")); break;
                        case "purple": circle.setFill(javafx.scene.paint.Color.web("#8b5cf6")); break;
                        case "orange": circle.setFill(javafx.scene.paint.Color.web("#f97316")); break;
                        case "pink": circle.setFill(javafx.scene.paint.Color.web("#ec4899")); break;
                        case "teal": circle.setFill(javafx.scene.paint.Color.web("#14b8a6")); break;
                        case "indigo": circle.setFill(javafx.scene.paint.Color.web("#6366f1")); break;
                        case "cyan": circle.setFill(javafx.scene.paint.Color.web("#06b6d4")); break;
                        case "gray": circle.setFill(javafx.scene.paint.Color.web("#64748b")); break;
                        default: circle.setFill(javafx.scene.paint.Color.GRAY); break;
                    }
                    setGraphic(circle);
                    setText(item);
                }
            }
        };
        colorCombo.setCellFactory(colorCellFactory);
        colorCombo.setButtonCell(colorCellFactory.call(null));
        colorCombo.setValue(cat.getColor());

        grid.add(new javafx.scene.control.Label("Tên danh mục:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new javafx.scene.control.Label("Loại:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new javafx.scene.control.Label("Icon:"), 0, 2);
        grid.add(iconCombo, 1, 2);
        grid.add(new javafx.scene.control.Label("Màu sắc:"), 0, 3);
        grid.add(colorCombo, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String newName = nameField.getText().trim();
                if (newName.isEmpty()) return null; // Validation
                core.storage.TransactionDAO dao = new core.storage.TransactionDAO();
                try {
                    dao.updateCategory(
                        cat.getName(), cat.getType().name(),
                        newName, typeCombo.getValue(),
                        iconCombo.getValue(), colorCombo.getValue()
                    );
                    loadCategoriesToUI();
                } catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }

    @FXML
    private void handleCreateCategory() {
        if (newCategoryNameField == null || categoryTypeCombo == null) return;
        
        String name = newCategoryNameField.getText();
        String type = categoryTypeCombo.getValue();
        String icon = categoryIconCombo != null ? categoryIconCombo.getValue() : null;
        String color = categoryColorCombo != null ? categoryColorCombo.getValue() : null;
        
        if (name == null || name.trim().isEmpty()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "T\u00ean danh m\u1ee5c kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng!", javafx.scene.control.ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (type == null) {
            return;
        }
        
        try {
            core.storage.TransactionDAO dao = new core.storage.TransactionDAO();
            dao.getOrCreateCategoryId(name.trim(), type.toUpperCase(), icon, color);
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
        renderBudgets();
    }

    @FXML
    private void switchToSettings() {
        switchTab(tabSettings, menuSettings);
        if (settingWalletName != null && currentWallet != null) {
            settingWalletName.setText(currentWallet.getName());
            if (settingInitialBalance != null) {
                double netTx = 0;
                if (currentWallet.getTransactions() != null) {
                    for (core.transaction.Transaction t : currentWallet.getTransactions()) {
                        if (t.getType() == core.TransactionType.INCOME) {
                            netTx += t.getAmount();
                        } else if (t.getType() == core.TransactionType.EXPENSE) {
                            netTx -= t.getAmount();
                        }
                    }
                }
                double initialBalance = currentWallet.getBalance() - netTx;
                settingInitialBalance.setText(String.format("%.0f", initialBalance));
            }
            if (settingCurrency != null) {
                if (settingCurrency.getItems().isEmpty()) {
                    settingCurrency.getItems().addAll("VND", "USD");
                }
                settingCurrency.setValue(currentWallet.getCurrency());
            }
        }
    }

    @FXML
    private void handleUpdateWalletSettings() {
        if (currentWallet == null) return;
        
        String newName = settingWalletName.getText();
        if (newName == null || newName.trim().isEmpty()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Tên ví không được để trống!", javafx.scene.control.ButtonType.OK);
            alert.showAndWait();
            return;
        }
        
        double newInitialBalance;
        try {
            newInitialBalance = Double.parseDouble(settingInitialBalance.getText().replaceAll(",", ""));
        } catch (NumberFormatException e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Số dư không hợp lệ!", javafx.scene.control.ButtonType.OK);
            alert.showAndWait();
            return;
        }
        
        double netTx = 0;
        if (currentWallet.getTransactions() != null) {
            for (core.transaction.Transaction t : currentWallet.getTransactions()) {
                if (t.getType() == core.TransactionType.INCOME) {
                    netTx += t.getAmount();
                } else if (t.getType() == core.TransactionType.EXPENSE) {
                    netTx -= t.getAmount();
                }
            }
        }
        double newCurrentBalance = newInitialBalance + netTx;
        
        if (newCurrentBalance < 0) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Thay đổi này sẽ làm số dư hiện tại của ví bị âm!", javafx.scene.control.ButtonType.OK);
            alert.showAndWait();
            return;
        }
        
        core.storage.WalletDAO dao = new core.storage.WalletDAO();
        
        String newCurrency = settingCurrency != null && settingCurrency.getValue() != null ? settingCurrency.getValue() : currentWallet.getCurrency();
        
        dao.updateWallet(currentWallet.getId(), newName.trim(), newCurrentBalance, newCurrency);
        
        currentWallet.setName(newName.trim());
        currentWallet.setBalance(newCurrentBalance);
        currentWallet.setCurrency(newCurrency);
        
        if (walletNameTopLabel != null) walletNameTopLabel.setText(currentWallet.getName());
        if (currentBalanceLabel != null) currentBalanceLabel.setText(String.format("%,.2f %s", currentWallet.getBalance(), currentWallet.getCurrency()));
        if (overviewBalanceLabel != null) overviewBalanceLabel.setText(String.format("%,.2f %s", currentWallet.getBalance(), currentWallet.getCurrency()));
        updateOverviewData();
        
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Cập nhật thông tin ví thành công!", javafx.scene.control.ButtonType.OK);
        alert.showAndWait();
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
        categoryCombo.getItems().add("--- CHI TIÊU (EXPENSE) ---");
        for (core.Category cat : allCategories) {
            if (cat.getType() == core.TransactionType.EXPENSE) {
                categoryCombo.getItems().add(cat.getName());
            }
        }
        categoryCombo.getItems().add("--- THU NHẬP (INCOME) ---");
        for (core.Category cat : allCategories) {
            if (cat.getType() == core.TransactionType.INCOME) {
                categoryCombo.getItems().add(cat.getName());
            }
        }
        categoryCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("---")) {
                        setDisable(true);
                        setStyle("-fx-font-weight: bold; -fx-text-fill: gray;");
                    } else {
                        setDisable(false);
                        setStyle("");
                    }
                }
            }
        });
        if (categoryCombo.getItems().size() > 1) {
            categoryCombo.getSelectionModel().select(1);
        }
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
        
        javafx.scene.control.CheckBox recurringCheck = new javafx.scene.control.CheckBox("Lặp lại (Recurring)");
        grid.add(recurringCheck, 0, 2);
        
        javafx.scene.control.ComboBox<String> periodCombo = new javafx.scene.control.ComboBox<>();
        periodCombo.getItems().addAll("Hàng ngày", "Hàng tuần", "Hàng tháng", "Hàng năm");
        periodCombo.getSelectionModel().select("Hàng tháng");
        periodCombo.setDisable(true);
        grid.add(periodCombo, 1, 2);
        
        recurringCheck.setOnAction(e -> {
            periodCombo.setDisable(!recurringCheck.isSelected());
        });
        
        categoryCombo.setOnAction(e -> {
            String catName = categoryCombo.getValue();
            if (catName != null && catName.startsWith("---")) return;
            core.Category selectedCategory = allCategories.stream().filter(c -> c.getName().equals(catName)).findFirst().orElse(null);
            if (selectedCategory != null && selectedCategory.getType() == core.TransactionType.INCOME) {
                recurringCheck.setDisable(true);
                recurringCheck.setSelected(false);
                periodCombo.setDisable(true);
            } else {
                recurringCheck.setDisable(false);
                periodCombo.setDisable(!recurringCheck.isSelected());
            }
        });

        dialog.getDialogPane().setContent(grid);

        final javafx.scene.control.Button okBtn = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(addButton);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                double amt = Double.parseDouble(amountField.getText().replaceAll(",", ""));
                if (amt <= 0) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "S\u1ed1 ti\u1ec1n ph\u1ea3i l\u1edbn h\u01a1n 0", javafx.scene.control.ButtonType.OK);
                    alert.showAndWait();
                    event.consume();
                    return;
                }
                if (amt > 1000000000000L) { // 1000 billion max
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "S\u1ed1 ti\u1ec1n qu\u00e1 l\u1edbn", javafx.scene.control.ButtonType.OK);
                    alert.showAndWait();
                    event.consume();
                    return;
                }
                String catName = categoryCombo.getValue();
                if (catName == null || catName.startsWith("---")) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Vui lòng chọn một danh mục hợp lệ", javafx.scene.control.ButtonType.OK);
                    alert.showAndWait();
                    event.consume();
                    return;
                }
                core.Category cat = allCategories.stream().filter(c -> c.getName().equals(catName)).findFirst().orElse(null);
                if (cat != null && cat.getType() == core.TransactionType.EXPENSE && amt > currentWallet.getBalance()) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "S\u1ed1 ti\u1ec1n chi ti\u00eau kh\u00f4ng \u0111\u01b0\u1ee3c v\u01b0\u1ee3t qu\u00e1 s\u1ed1 d\u01b0 v\u00ed", javafx.scene.control.ButtonType.OK);
                    alert.showAndWait();
                    event.consume();
                    return;
                }
            } catch (NumberFormatException e) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "S\u1ed1 ti\u1ec1n kh\u00f4ng h\u1ee3p l\u1ec7", javafx.scene.control.ButtonType.OK);
                alert.showAndWait();
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                String catName = categoryCombo.getValue();
                java.time.LocalDate date = datePicker.getValue();
                String note = noteField.getText();
                double amount = 0.0;
                try {
                    amount = Double.parseDouble(amountField.getText().replaceAll(",", ""));
                } catch (NumberFormatException e) {
                    amount = 0.0;
                }
                
                core.Category selectedCategory = allCategories.stream().filter(c -> c.getName().equals(catName)).findFirst().orElse(new core.Category(catName != null ? catName : "Unknown", core.TransactionType.EXPENSE));
                
                core.transaction.Transaction t;
                if (selectedCategory.getType() == core.TransactionType.INCOME) {
                    t = new core.transaction.Income((int)(Math.random()*10000), amount, date, note, selectedCategory, currentWallet, catName);
                } else {
                    if (recurringCheck.isSelected()) {
                        java.time.Period p;
                        String selP = periodCombo.getValue();
                        if ("Hàng ngày".equals(selP)) p = java.time.Period.ofDays(1);
                        else if ("Hàng tuần".equals(selP)) p = java.time.Period.ofWeeks(1);
                        else if ("Hàng năm".equals(selP)) p = java.time.Period.ofYears(1);
                        else p = java.time.Period.ofMonths(1);
                        t = new core.transaction.RecurringExpense((int)(Math.random()*10000), amount, date, note, selectedCategory, currentWallet, catName, p);
                    } else {
                        t = new core.transaction.Expense((int)(Math.random()*10000), amount, date, note, selectedCategory, currentWallet, catName);
                    }
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
            String curr = currentWallet.getCurrency();
            currentBalanceLabel.setText(String.format("%,.2f %s", currentWallet.getBalance(), curr));
            if (overviewBalanceLabel != null) {
                overviewBalanceLabel.setText(String.format("%,.2f %s", currentWallet.getBalance(), curr));
            }
        });
    }

    private java.util.List<core.transaction.Transaction> getFilteredTransactions() {
        if (currentWallet == null) return new java.util.ArrayList<>();
        
        java.util.stream.Stream<core.transaction.Transaction> stream = currentWallet.getTransactions().stream();
        
        // Apply period filter
        stream = stream.filter(t -> !t.getDate().isBefore(currentPeriodStart) && !t.getDate().isAfter(currentPeriodEnd));
        
        // Apply category filter
        if (filterCategoryCombo != null && filterCategoryCombo.getValue() != null && !filterCategoryCombo.getValue().equals("All categories")) {
            String selectedCat = filterCategoryCombo.getValue();
            stream = stream.filter(t -> t.getCategory().getName().equals(selectedCat));
        }
        
        // Apply note filter
        if (filterNoteField != null && !filterNoteField.getText().trim().isEmpty()) {
            String keyword = filterNoteField.getText().trim().toLowerCase();
            stream = stream.filter(t -> t.getNote() != null && t.getNote().toLowerCase().contains(keyword));
        }
        
        // Apply amount filters
        if (filterMinAmountField != null && !filterMinAmountField.getText().trim().isEmpty()) {
            try {
                double minAmt = Double.parseDouble(filterMinAmountField.getText().trim());
                stream = stream.filter(t -> t.getAmount() >= minAmt);
            } catch (NumberFormatException ignored) {}
        }
        if (filterMaxAmountField != null && !filterMaxAmountField.getText().trim().isEmpty()) {
            try {
                double maxAmt = Double.parseDouble(filterMaxAmountField.getText().trim());
                stream = stream.filter(t -> t.getAmount() <= maxAmt);
            } catch (NumberFormatException ignored) {}
        }
        
        return stream.collect(java.util.stream.Collectors.toList());
    }

    @FXML
    public void handlePrevPeriod() {
        long days = java.time.temporal.ChronoUnit.DAYS.between(currentPeriodStart, currentPeriodEnd) + 1;
        currentPeriodStart = currentPeriodStart.minusDays(days);
        currentPeriodEnd = currentPeriodEnd.minusDays(days);
        updateOverviewData();
        renderTransactions();
    }

    @FXML
    public void handleNextPeriod() {
        long days = java.time.temporal.ChronoUnit.DAYS.between(currentPeriodStart, currentPeriodEnd) + 1;
        currentPeriodStart = currentPeriodStart.plusDays(days);
        currentPeriodEnd = currentPeriodEnd.plusDays(days);
        updateOverviewData();
        renderTransactions();
    }

    @FXML
    public void handleCustomPeriod() {
        javafx.scene.control.Dialog<javafx.util.Pair<java.time.LocalDate, java.time.LocalDate>> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Chọn thời gian");
        dialog.setHeaderText("Chọn khoảng thời gian muốn xem");

        javafx.scene.control.ButtonType okButtonType = new javafx.scene.control.ButtonType("OK", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));

        javafx.scene.control.DatePicker startDatePicker = new javafx.scene.control.DatePicker(currentPeriodStart);
        javafx.scene.control.DatePicker endDatePicker = new javafx.scene.control.DatePicker(currentPeriodEnd);

        grid.add(new javafx.scene.control.Label("Từ ngày:"), 0, 0);
        grid.add(startDatePicker, 1, 0);
        grid.add(new javafx.scene.control.Label("Đến ngày:"), 0, 1);
        grid.add(endDatePicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new javafx.util.Pair<>(startDatePicker.getValue(), endDatePicker.getValue());
            }
            return null;
        });

        java.util.Optional<javafx.util.Pair<java.time.LocalDate, java.time.LocalDate>> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            if (pair.getKey() != null && pair.getValue() != null) {
                if (pair.getKey().isAfter(pair.getValue())) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi");
                    alert.setHeaderText("Ngày bắt đầu không thể lớn hơn ngày kết thúc!");
                    alert.showAndWait();
                } else {
                    currentPeriodStart = pair.getKey();
                    currentPeriodEnd = pair.getValue();
                    updateOverviewData();
                    renderTransactions();
                }
            }
        });
    }

    @FXML
    private void handleResetFilters() {
        if (filterCategoryCombo != null) filterCategoryCombo.getSelectionModel().selectFirst();
        if (filterNoteField != null) filterNoteField.clear();
        if (filterMinAmountField != null) filterMinAmountField.clear();
        if (filterMaxAmountField != null) filterMaxAmountField.clear();
        updateOverviewData();
        renderTransactions();
    }

    private void updatePeriodLabels() {
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy", java.util.Locale.ENGLISH);
        String labelText = currentPeriodStart.format(dtf) + " - " + currentPeriodEnd.format(dtf);
        if (periodLabelOverview != null) periodLabelOverview.setText(labelText);
        if (periodLabelTrans != null) periodLabelTrans.setText(labelText);
    }

    private void updateOverviewData() {
        if (currentWallet == null) return;
        
        updatePeriodLabels();

        
        double totalIncome = 0;
        double totalExpense = 0;
        
        java.util.Map<String, Double> incomeByCategory = new java.util.HashMap<>();
        java.util.Map<String, Double> expenseByCategory = new java.util.HashMap<>();
        java.util.Map<java.time.LocalDate, Double> balanceByDate = new java.util.TreeMap<>();
        java.util.Map<java.time.LocalDate, Double> incomeByDate = new java.util.TreeMap<>();
        java.util.Map<java.time.LocalDate, Double> expenseByDate = new java.util.TreeMap<>();
        
        java.util.List<core.transaction.Transaction> filteredTransactions = getFilteredTransactions();
        
        // Mock starting balance point (assuming 0 before transactions for demo purposes)
        if (filteredTransactions.isEmpty()) {
            balanceByDate.put(java.time.LocalDate.now().minusDays(1), 0.0);
        } else {
            balanceByDate.put(filteredTransactions.get(0).getDate().minusDays(1), 0.0);
        }

        double runningBalance = 0;

        // Sort transactions by date for correct balance tracking
        java.util.List<core.transaction.Transaction> sortedTransactions = new java.util.ArrayList<>(filteredTransactions);
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
        String curr = currentWallet != null ? currentWallet.getCurrency() : "VND";
        if (overviewIncomeLabel != null) overviewIncomeLabel.setText(String.format("+%,.2f %s", totalIncome, curr));
        if (overviewExpenseLabel != null) overviewExpenseLabel.setText(String.format("-%,.2f %s", totalExpense, curr));
        if (overviewChangeLabel != null) {
            overviewChangeLabel.setText(String.format("%s%,.2f %s", totalChange >= 0 ? "+" : "", totalChange, curr));
            overviewChangeLabel.setStyle(totalChange >= 0 ? "-fx-text-fill: #3b82f6;" : "-fx-text-fill: #ef4444;");
        }
        if (transIncomeLabel != null) transIncomeLabel.setText(String.format("+%,.2f %s", totalIncome, curr));
        if (transExpenseLabel != null) transExpenseLabel.setText(String.format("-%,.2f %s", totalExpense, curr));
        if (transChangeLabel != null) {
            transChangeLabel.setText(String.format("%s%,.2f %s", totalChange >= 0 ? "+" : "", totalChange, curr));
            transChangeLabel.setStyle(totalChange >= 0 ? "-fx-text-fill: #3b82f6;" : "-fx-text-fill: #ef4444;");
        }

        // Helper formatter for charts
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy");
        javafx.util.StringConverter<Number> formatterVND = new javafx.util.StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                if (object == null) return "0 " + curr;
                double val = object.doubleValue();
                if (val == 0) return "0.00 " + curr;
                return String.format("%s%,.2f %s", val > 0 ? "+" : "", val, curr);
            }
            @Override
            public Number fromString(String string) { return null; }
        };

        java.time.LocalDate startDate = currentPeriodStart;
        java.time.LocalDate endDate = currentPeriodEnd;
        
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
                        javafx.scene.control.Label bLbl = new javafx.scene.control.Label(String.format("Balance: %,.2f %s", balVal, curr));
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
                        javafx.scene.control.Label iLbl = new javafx.scene.control.Label(String.format("Income: +%,.2f %s", incVal, curr));
                        iLbl.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                        javafx.scene.control.Label eLbl = new javafx.scene.control.Label(String.format("Expense: %,.2f %s", expVal, curr));
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
            
            String curr = currentWallet != null ? currentWallet.getCurrency() : "VND";
            javafx.scene.control.Label amountLabel = new javafx.scene.control.Label(String.format("%s%,.2f %s", isIncome ? "+" : "-", entry.getValue(), curr));
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

    @FXML
    private void handleShowAddBudgetDialog() {
        javafx.scene.control.Dialog<core.Budget> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Create a New Budget");
        
        javafx.scene.control.ButtonType addButton = new javafx.scene.control.ButtonType("Create", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, javafx.scene.control.ButtonType.CANCEL);
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));
        
        grid.add(new Label("Tên ngân sách:"), 0, 0);
        javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();
        grid.add(nameField, 1, 0);

        String curr = currentWallet != null ? currentWallet.getCurrency() : "VND";
        grid.add(new Label("Hạn mức (" + curr + "):"), 0, 1);
        javafx.scene.control.TextField amountField = new javafx.scene.control.TextField("0");
        grid.add(amountField, 1, 1);
        
        grid.add(new Label("Danh mục:"), 0, 2);
        javafx.scene.control.ComboBox<String> categoryCombo = new javafx.scene.control.ComboBox<>();
        categoryCombo.getItems().add("Tất cả (All)");
        for (core.Category cat : allCategories) {
            if (cat.getType() == core.TransactionType.EXPENSE) {
                categoryCombo.getItems().add(cat.getName());
            }
        }
        categoryCombo.getSelectionModel().selectFirst();
        grid.add(categoryCombo, 1, 2);
        
        grid.add(new Label("Chu kỳ:"), 0, 3);
        javafx.scene.control.ComboBox<String> periodCombo = new javafx.scene.control.ComboBox<>();
        periodCombo.getItems().addAll("MONTHLY", "WEEKLY", "YEARLY");
        periodCombo.getSelectionModel().select("MONTHLY");
        grid.add(periodCombo, 1, 3);
        
        grid.add(new Label("Ngày bắt đầu:"), 0, 4);
        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker(java.time.LocalDate.now());
        grid.add(datePicker, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        final javafx.scene.control.Button okBtn = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(addButton);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                double amt = Double.parseDouble(amountField.getText().replaceAll(",", ""));
                if (amt <= 0) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "S\u1ed1 ti\u1ec1n ph\u1ea3i l\u1edbn h\u01a1n 0", javafx.scene.control.ButtonType.OK);
                    alert.showAndWait();
                    event.consume();
                }
                if (nameField.getText().trim().isEmpty()) {
                    event.consume();
                }
            } catch (Exception e) {
                event.consume();
            }
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                try {
                    String name = nameField.getText().trim();
                    double amount = Double.parseDouble(amountField.getText().replaceAll(",", ""));
                    String catName = categoryCombo.getValue();
                    core.Category selectedCategory = null;
                    if (!"Tất cả (All)".equals(catName)) {
                        selectedCategory = allCategories.stream().filter(c -> c.getName().equals(catName)).findFirst().orElse(null);
                    }
                    core.Period period = core.Period.valueOf(periodCombo.getValue());
                    java.time.LocalDate startDate = datePicker.getValue();
                    
                    return new core.Budget(0, name, amount, selectedCategory, period, startDate);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });
        
        java.util.Optional<core.Budget> result = dialog.showAndWait();
        result.ifPresent(budget -> {
            core.storage.BudgetDAO dao = new core.storage.BudgetDAO();
            dao.addBudget(budget, currentWallet.getId());
            renderBudgets();
        });
    }

    private void renderBudgets() {
        if (budgetsContainer == null || currentWallet == null) return;
        
        budgetsContainer.getChildren().clear();
        
        core.storage.BudgetDAO dao = new core.storage.BudgetDAO();
        java.util.List<core.Budget> budgets = dao.getBudgetsByWallet(currentWallet.getId());
        
        if (budgets.isEmpty()) {
            javafx.scene.layout.VBox emptyBox = new javafx.scene.layout.VBox();
            emptyBox.setAlignment(javafx.geometry.Pos.CENTER);
            emptyBox.setSpacing(15);
            emptyBox.getStyleClass().add("budget-empty-box");
            
            Label l1 = new Label("Take control of your expenses and");
            l1.getStyleClass().add("budget-text");
            Label l2 = new Label("save more money with budgets!");
            l2.getStyleClass().add("budget-text");
            
            javafx.scene.control.Button btn = new javafx.scene.control.Button("Create a New Budget");
            btn.getStyleClass().add("solid-button");
            btn.setOnAction(e -> handleShowAddBudgetDialog());
            javafx.scene.layout.VBox.setMargin(btn, new javafx.geometry.Insets(10, 0, 0, 0));
            
            emptyBox.getChildren().addAll(l1, l2, btn);
            budgetsContainer.getChildren().add(emptyBox);
            return;
        }
        
        java.util.List<core.transaction.Transaction> walletTx = currentWallet.getTransactions();
        
        for (core.Budget budget : budgets) {
            budget.updateSpentFromTransactions(walletTx);
            
            javafx.scene.layout.VBox card = new javafx.scene.layout.VBox();
            card.setSpacing(25);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 25; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 5); -fx-border-color: #e2e8f0; -fx-border-radius: 16; -fx-border-width: 1;");
            
            // Header
            javafx.scene.layout.HBox header = new javafx.scene.layout.HBox();
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            javafx.scene.layout.VBox titleBox = new javafx.scene.layout.VBox(5);
            Label nameLabel = new Label(budget.getName());
            nameLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            
            String catName = budget.getCategory() != null ? budget.getCategory().getName() : "All Categories";
            Label catLabel = new Label("Category: " + catName);
            catLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-font-weight: 500;");
            titleBox.getChildren().addAll(nameLabel, catLabel);
            
            javafx.scene.layout.Region spacer1 = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer1, javafx.scene.layout.Priority.ALWAYS);
            
            javafx.scene.control.Button editBtn = new javafx.scene.control.Button("Delete Budget");
            editBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand; -fx-border-color: #fee2e2; -fx-border-radius: 8;");
            editBtn.setOnAction(e -> {
                dao.deleteBudget(budget.getId());
                renderBudgets();
            });
            header.getChildren().addAll(titleBox, spacer1, editBtn);
            
            // Stats Grid
            javafx.scene.layout.HBox statsBox = new javafx.scene.layout.HBox();
            statsBox.setSpacing(15);
            statsBox.setAlignment(javafx.geometry.Pos.CENTER);
            
            String curr = currentWallet != null ? currentWallet.getCurrency() : "VND";
            statsBox.getChildren().addAll(
                createStatCard("Originally Budgeted", String.format("%,.0f %s", budget.getLimitAmount(), curr), "#10b981", "#ecfdf5"),
                createStatCard("Spent", String.format("%,.0f %s", budget.getCurrentSpent(), curr), "#ef4444", "#fef2f2"),
                createStatCard("Left", String.format("%,.0f %s", budget.getRemainingAmount(), curr), budget.getRemainingAmount() >= 0 ? "#10b981" : "#ef4444", budget.getRemainingAmount() >= 0 ? "#ecfdf5" : "#fef2f2"),
                createStatCard("Daily Allowance", String.format("%,.0f %s", budget.calcDailyAllowance(java.time.LocalDate.now()), curr), "#3b82f6", "#eff6ff")
            );
            
            // Progress Section
            javafx.scene.layout.VBox progressBox = new javafx.scene.layout.VBox();
            progressBox.setSpacing(12);
            
            javafx.scene.layout.HBox progressTitleBox = new javafx.scene.layout.HBox();
            Label progressTitle = new Label("Budget Progress");
            progressTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #334155;");
            javafx.scene.layout.Region pSpacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(pSpacer, javafx.scene.layout.Priority.ALWAYS);
            Label percentageLbl = new Label(String.format("%.1f%%", budget.getUsagePercentage()));
            percentageLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + (budget.isExceed() ? "#ef4444" : "#10b981") + ";");
            progressTitleBox.getChildren().addAll(progressTitle, pSpacer, percentageLbl);
            
            javafx.scene.control.ProgressBar pBar = new javafx.scene.control.ProgressBar(budget.getUsagePercentage() / 100.0);
            pBar.setMaxWidth(Double.MAX_VALUE);
            pBar.setPrefHeight(12);
            pBar.setStyle("-fx-accent: " + (budget.isExceed() ? "#ef4444" : "#10b981") + "; -fx-control-inner-background: #f1f5f9;");
            
            javafx.scene.layout.HBox datesBox = new javafx.scene.layout.HBox();
            Label startDateLbl = new Label(budget.getStartDate() != null ? budget.getStartDate().toString() : "");
            startDateLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: bold;");
            javafx.scene.layout.Region spacer2 = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);
            Label endDateLbl = new Label(budget.getEndDate() != null ? budget.getEndDate().toString() : "");
            endDateLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: bold;");
            datesBox.getChildren().addAll(startDateLbl, spacer2, endDateLbl);
            
            progressBox.getChildren().addAll(progressTitleBox, pBar, datesBox);
            
            card.getChildren().addAll(header, statsBox, progressBox);
            budgetsContainer.getChildren().add(card);
        }
    }

    private javafx.scene.layout.VBox createStatCard(String title, String value, String valueColor, String bgColor) {
        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox();
        box.setSpacing(8);
        box.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 16; -fx-background-radius: 12; -fx-border-color: " + valueColor + "40; -fx-border-radius: 12; -fx-border-width: 1;");
        javafx.scene.layout.HBox.setHgrow(box, javafx.scene.layout.Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label tLbl = new Label(title);
        tLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-font-weight: 600;");
        
        Label vLbl = new Label(value);
        vLbl.setStyle("-fx-text-fill: " + valueColor + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        box.getChildren().addAll(tLbl, vLbl);
        return box;
    }

    @FXML
    private void handleDeleteWallet(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xóa ví");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa ví này không?");
        alert.setContentText("Toàn bộ các giao dịch nằm trong ví đó cũng sẽ bị xóa vĩnh viễn không thể khôi phục.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            WalletDAO dao = new WalletDAO();
            dao.deleteWallet(currentWallet.getId());
            
            // Navigate back to Dashboard
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) deleteWalletLabel.getScene().getWindow();
                stage.setScene(new Scene(root, 1024, 768));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


