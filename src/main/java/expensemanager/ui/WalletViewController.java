package expensemanager.ui;

import core.wallet.Wallet;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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

    private Wallet currentWallet;

    public void initData(Wallet wallet) {
        this.currentWallet = wallet;
        walletNameTopLabel.setText(wallet.getName());
        currentBalanceLabel.setText(String.format("%,.2f VND", wallet.getBalance()));
        if (overviewBalanceLabel != null) {
            overviewBalanceLabel.setText(String.format("%,.2f VND", wallet.getBalance()));
        }
        
        if (categoryTypeCombo != null) {
            categoryTypeCombo.getItems().addAll("Expense", "Income");
            categoryTypeCombo.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleBackToDashboard(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) walletNameTopLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private javafx.scene.layout.VBox tabTransactions;
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
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Add transaction");
        
        javafx.scene.control.ButtonType addButton = new javafx.scene.control.ButtonType("Add transaction", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, javafx.scene.control.ButtonType.CANCEL);
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));

        grid.add(new Label("Category:"), 0, 0);
        javafx.scene.control.ComboBox<String> categoryCombo = new javafx.scene.control.ComboBox<>();
        categoryCombo.getItems().addAll("Lương", "Kinh doanh", "Đồ ăn & Đồ uống", "Mua sắm", "Di chuyển");
        categoryCombo.getSelectionModel().selectFirst();
        grid.add(categoryCombo, 0, 1);
        
        grid.add(new Label("Date:"), 1, 0);
        grid.add(new javafx.scene.control.DatePicker(java.time.LocalDate.now()), 1, 1);
        
        grid.add(new Label("Note:"), 2, 0);
        grid.add(new javafx.scene.control.TextField(), 2, 1);

        grid.add(new Label("Amount:"), 3, 0);
        javafx.scene.control.TextField amountField = new javafx.scene.control.TextField("0.00");
        grid.add(amountField, 3, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }
}
