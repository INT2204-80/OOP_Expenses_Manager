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

    private WalletDAO walletDAO = new WalletDAO();

    @FXML
    public void initialize() {
        refreshWallets();
    }

    private void refreshWallets() {
        if (walletsContainer != null) {
            walletsContainer.getChildren().clear();
        }
        
        List<Wallet> wallets = walletDAO.getAllWallets();
        
        // If no wallets exist, display a dummy one to match the exact Spendee mockup
        if (wallets.isEmpty()) {
            wallets.add(new CashWallet("Ví tiền mặt", 0.0));
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

        String typeStr = "Cash";
        if (w instanceof BankAccount) {
            typeStr = ((BankAccount)w).getBankName();
        }
        Label typeLbl = new Label(typeStr);
        typeLbl.getStyleClass().add("wallet-type");

        Label balLbl = new Label(String.format("%,.2f VND", w.getBalance()));
        balLbl.getStyleClass().add("wallet-balance");

        details.getChildren().addAll(nameLbl, typeLbl, balLbl);
        VBox.setMargin(details, new Insets(0, 0, 0, 10));

        card.getChildren().addAll(icon, details);

        // Navigation logic: Click on wallet card to open WalletView
        card.setOnMouseClicked(event -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/WalletView.fxml"));
                javafx.scene.Parent root = loader.load();
                WalletViewController controller = loader.getController();
                controller.initData(w);
                
                javafx.stage.Stage stage = (javafx.stage.Stage) card.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root, 1000, 700));
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
        dialog.setHeaderText("Create a new Cash Wallet");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField("Ví tiền mặt");
        name.setPromptText("Wallet Name");
        TextField balance = new TextField("0");
        balance.setPromptText("Initial Balance (VND)");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Balance:"), 0, 1);
        grid.add(balance, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double bal = Double.parseDouble(balance.getText());
                    return new CashWallet(name.getText(), bal);
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
}
