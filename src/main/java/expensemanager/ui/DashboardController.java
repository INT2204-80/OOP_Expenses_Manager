package expensemanager.ui;

import core.storage.WalletDAO;
import core.wallet.BankAccount;
import core.wallet.CashWallet;
import core.wallet.Wallet;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class DashboardController {

    @FXML private HBox walletsContainer;
    
    @FXML private Label totalBalanceLabel;
    @FXML private Label totalPeriodChangeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label periodLabel;
    
    private java.time.LocalDate currentPeriodStart = java.time.LocalDate.now().withDayOfMonth(1);
    private java.time.LocalDate currentPeriodEnd = java.time.LocalDate.now().withDayOfMonth(java.time.LocalDate.now().lengthOfMonth());

    private WalletDAO walletDAO = new WalletDAO();

    @FXML
    public void initialize() {
        refreshWallets();
        refreshOverview();
    }

    private void refreshWallets() {
        if (walletsContainer != null) {
            walletsContainer.getChildren().clear();
        }
        
        List<Wallet> wallets = walletDAO.getAllWallets();
        
        // If no wallets exist, display a dummy one to match the exact Spendee mockup
        if (wallets.isEmpty()) {
            Wallet defaultWallet = new CashWallet("VÃ­ tiá»n máº·t", 0.0);
            walletDAO.addWallet(defaultWallet);
            wallets = walletDAO.getAllWallets();
        }

        double totalBalance = 0;

        for (Wallet w : wallets) {
            totalBalance += w.getBalance();
            if (walletsContainer != null) {
                HBox card = createWalletCard(w);
                walletsContainer.getChildren().add(card);
            }
        }

        if (totalBalanceLabel != null) {
            totalBalanceLabel.setText(String.format("%,.2f VND", totalBalance));
        }
    }

    private HBox createWalletCard(Wallet w) {
        HBox card = new HBox(15);
        card.getStyleClass().add("wallet-card");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setPrefWidth(300);

        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        if (w instanceof BankAccount) {
            icon.setContent("M4 10h3v7H4zm6.5 0h3v7h-3zM2 19h20v3H2zm15-9h3v7h-3zm-15-4 10-5 10 5v2H2z"); // Bank SVG
            icon.setFill(javafx.scene.paint.Color.web("#a0aec0"));
        } else {
            icon.setContent("M21 7.28V5c0-1.1-.9-2-2-2H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2v-2.28A2 2 0 0 0 22 15V9a2 2 0 0 0-1-1.72zM20 9v6h-7V9h7zM5 19V5h14v2h-6c-1.1 0-2 .9-2 2v6c0 1.1.9 2 2 2h6v2H5z"); // Wallet SVG
            icon.setFill(javafx.scene.paint.Color.web("#8B5A2B")); // Brown wallet color
        }
        icon.setScaleX(1.2);
        icon.setScaleY(1.2);

        VBox details = new VBox();
        details.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLbl = new Label(w.getName());
        nameLbl.getStyleClass().add("wallet-name");

        String typeStr = "Cash Wallet";
        if (w instanceof BankAccount) {
            BankAccount ba = (BankAccount) w;
            typeStr = "Bank Account (" + (ba.getBankName() != null ? ba.getBankName() : "") + ")";
        } else if (w instanceof core.wallet.EWallet) {
            core.wallet.EWallet ew = (core.wallet.EWallet) w;
            typeStr = "E-Wallet (" + (ew.getProvider() != null ? ew.getProvider() : "") + ")";
        }
        Label typeLbl = new Label(typeStr);
        typeLbl.getStyleClass().add("wallet-type");
        typeLbl.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 11px;");

        Label balLbl = new Label(String.format("%,.2f VND", w.getBalance()));
        balLbl.getStyleClass().add("wallet-balance");

        details.getChildren().addAll(nameLbl, typeLbl, balLbl);
        VBox.setMargin(details, new Insets(0, 0, 0, 10));

        // Delete button
        Button deleteBtn = new Button("\uD83D\uDDD1");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-font-size: 16px;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this wallet and all its transactions?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    walletDAO.deleteWallet(w.getId());
                    refreshWallets();
                }
            });
            e.consume(); // Prevent triggering the card click
        });

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        card.getChildren().addAll(icon, details, spacer, deleteBtn);

        // Navigation logic: Click on wallet card to open WalletView
        card.setOnMouseClicked(event -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/WalletView.fxml"));
                javafx.scene.Parent root = loader.load();
                WalletViewController controller = loader.getController();
                controller.initData(w);
                
                javafx.stage.Stage stage = (javafx.stage.Stage) card.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        card.setStyle("-fx-cursor: hand;");

        return card;
    }

    @FXML
    public void handleAddWallet() {
        Dialog<Wallet> dialog = new Dialog<>();
        dialog.setTitle("Add New Wallet");
        dialog.setHeaderText("Create a new Wallet");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 50, 10, 10));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Cash Wallet", "Bank Account", "E-Wallet");
        typeCombo.setValue("Cash Wallet");
        typeCombo.setStyle("-fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField name = new TextField();
        name.setPromptText("Wallet Name (e.g. My Bank, Momo...)");
        TextField balance = new TextField("0");
        balance.setPromptText("Initial Balance (VND)");
        
        TextField bankName = new TextField();
        bankName.setPromptText("Bank Name");
        TextField accNumber = new TextField();
        accNumber.setPromptText("Account Number");
        
        TextField provider = new TextField();
        provider.setPromptText("Provider (Momo, ZaloPay, etc.)");

        Label bankNameLbl = new Label("Bank Name:");
        Label accNumLbl = new Label("Account Num:");
        Label providerLbl = new Label("Provider:");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Balance:"), 0, 1);
        grid.add(balance, 1, 1);
        
        grid.add(bankNameLbl, 0, 2);
        grid.add(bankName, 1, 2);
        grid.add(accNumLbl, 0, 3);
        grid.add(accNumber, 1, 3);
        
        grid.add(providerLbl, 0, 4);
        grid.add(provider, 1, 4);

        Runnable updateVisibility = () -> {
            String sel = typeCombo.getValue();
            boolean isBank = "Bank Account".equals(sel);
            boolean isEWallet = "E-Wallet".equals(sel);

            bankNameLbl.setVisible(isBank); bankNameLbl.setManaged(isBank);
            bankName.setVisible(isBank); bankName.setManaged(isBank);
            accNumLbl.setVisible(isBank); accNumLbl.setManaged(isBank);
            accNumber.setVisible(isBank); accNumber.setManaged(isBank);

            providerLbl.setVisible(isEWallet); providerLbl.setManaged(isEWallet);
            provider.setVisible(isEWallet); provider.setManaged(isEWallet);
            
            if (dialog.getDialogPane().getScene() != null && dialog.getDialogPane().getScene().getWindow() != null) {
                dialog.getDialogPane().getScene().getWindow().sizeToScene();
            }
        };

        typeCombo.setOnAction(e -> updateVisibility.run());
        updateVisibility.run();

        content.getChildren().addAll(new Label("Wallet Type:"), typeCombo, grid);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double bal = Double.parseDouble(balance.getText());
                    String type = typeCombo.getValue();
                    if ("Bank Account".equals(type)) {
                        return new core.wallet.BankAccount(name.getText(), bal, bankName.getText(), accNumber.getText());
                    } else if ("E-Wallet".equals(type)) {
                        return new core.wallet.EWallet(name.getText(), bal, provider.getText());
                    } else {
                        return new core.wallet.CashWallet(name.getText(), bal);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid balance format.");
                    return null;
                }
            }
            return null;
        });

        Optional<Wallet> result = dialog.showAndWait();
        result.ifPresent(wallet -> {
            walletDAO.addWallet(wallet);
            refreshWallets();
        });
    }

    @FXML
    public void handlePrevPeriod() {
        long days = java.time.temporal.ChronoUnit.DAYS.between(currentPeriodStart, currentPeriodEnd) + 1;
        currentPeriodStart = currentPeriodStart.minusDays(days);
        currentPeriodEnd = currentPeriodEnd.minusDays(days);
        refreshOverview();
    }

    

    @FXML
    public void handleNextPeriod() {
        long days = java.time.temporal.ChronoUnit.DAYS.between(currentPeriodStart, currentPeriodEnd) + 1;
        currentPeriodStart = currentPeriodStart.plusDays(days);
        currentPeriodEnd = currentPeriodEnd.plusDays(days);
        refreshOverview();
    }

            @FXML
    public void handleCustomPeriod() {
        javafx.scene.control.Dialog<javafx.util.Pair<java.time.LocalDate, java.time.LocalDate>> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Chọn thời gian");
        dialog.setHeaderText("Chọn khoảng thời gian muốn xem");

        javafx.scene.control.ButtonType okButtonType = new javafx.scene.control.ButtonType("Xác nhận", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
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
                    alert.setHeaderText("Ngày bắt dầu không thể lớn hơn ngày kẽt thúc!");
                    alert.showAndWait();
                } else {
                    currentPeriodStart = pair.getKey();
                    currentPeriodEnd = pair.getValue();
                    refreshOverview();
                }
            }
        });
    }

    private void refreshOverview() {
        java.time.LocalDate endDate = currentPeriodEnd;
        
        if (periodLabel != null) {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy", java.util.Locale.ENGLISH);
            periodLabel.setText(currentPeriodStart.format(dtf) + " - " + endDate.format(dtf));
        }

        core.storage.TransactionDAO dao = new core.storage.TransactionDAO();
        try {
            double income = dao.getTotalAmountForPeriod("INCOME", currentPeriodStart, endDate);
            double expense = dao.getTotalAmountForPeriod("EXPENSE", currentPeriodStart, endDate);
            double change = income - expense;

            if (totalIncomeLabel != null) totalIncomeLabel.setText(String.format("%,.2f VND", income));
            if (totalExpensesLabel != null) totalExpensesLabel.setText(String.format("%,.2f VND", expense));
            if (totalPeriodChangeLabel != null) {
                totalPeriodChangeLabel.setText(String.format("%,.2f VND", change));
                if (change < 0) {
                    totalPeriodChangeLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 24px; -fx-font-weight: bold;");
                } else {
                    totalPeriodChangeLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 24px; -fx-font-weight: bold;");
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleConnectBank() {
        Dialog<Wallet> dialog = new Dialog<>();
        dialog.setTitle("Connect Bank Account");
        dialog.setHeaderText("Add a new Bank Account");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Wallet Name");
        TextField balance = new TextField();
        balance.setPromptText("Initial Balance (VND)");
        TextField bankName = new TextField();
        bankName.setPromptText("Bank Name (e.g. VCB)");
        TextField accNum = new TextField();
        accNum.setPromptText("Account Number");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Balance:"), 0, 1);
        grid.add(balance, 1, 1);
        grid.add(new Label("Bank Name:"), 0, 2);
        grid.add(bankName, 1, 2);
        grid.add(new Label("Account No:"), 0, 3);
        grid.add(accNum, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double bal = Double.parseDouble(balance.getText());
                    return new BankAccount(name.getText(), bal, bankName.getText(), accNum.getText());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid balance format.");
                    return null;
                }
            }
            return null;
        });

        Optional<Wallet> result = dialog.showAndWait();
        result.ifPresent(wallet -> {
            walletDAO.addWallet(wallet);
            refreshWallets();
        });
    }
    
    @FXML
    private void handleNavigateToBudgets() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/BudgetsView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Get current stage and set new scene
            javafx.scene.Scene scene = walletsContainer.getScene();
            scene.setRoot(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}




