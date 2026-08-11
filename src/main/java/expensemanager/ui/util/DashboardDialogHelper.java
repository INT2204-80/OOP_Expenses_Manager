package expensemanager.ui.util;

import java.time.LocalDate;
import java.util.Optional;

import core.wallet.BankAccount;
import core.wallet.CashWallet;
import core.wallet.EWallet;
import core.wallet.Wallet;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Pair;

public class DashboardDialogHelper {

    public static Optional<Wallet> showAddWalletDialog() {
        Dialog<Wallet> dialog = new Dialog<>();
        dialog.setTitle("Add New Wallet");
        dialog.setHeaderText("Create a new Wallet");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 20, 20, 20));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Cash Wallet", "Bank Account", "E-Wallet");
        typeCombo.setValue("Cash Wallet");
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(10); 
        grid.setVgap(12);

        // 1. Cố định kích thước cột để nhãn chữ không bị cắt (VD: "Bank Name:")
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100); 
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setMinWidth(200);
        grid.getColumnConstraints().addAll(col1, col2);

        TextField name = new TextField();
        name.setPromptText("e.g. My Cash, VCB...");
        TextField balance = new TextField("0");
        balance.setPromptText("Initial Balance");
        TextField bankName = new TextField();
        bankName.setPromptText("Bank Name");
        TextField accNumber = new TextField();
        accNumber.setPromptText("Account Number");
        TextField provider = new TextField();
        provider.setPromptText("Provider (Momo, ZaloPay...)");

        Label bankNameLbl = new Label("Bank Name:");
        Label accNumLbl = new Label("Account Num:");
        Label providerLbl = new Label("Provider:");

        grid.add(new Label("Name:"), 0, 0); grid.add(name, 1, 0);
        grid.add(new Label("Balance:"), 0, 1); grid.add(balance, 1, 1);
        grid.add(bankNameLbl, 0, 2); grid.add(bankName, 1, 2);
        grid.add(accNumLbl, 0, 3); grid.add(accNumber, 1, 3);
        grid.add(providerLbl, 0, 4); grid.add(provider, 1, 4);

        Runnable updateVisibility = () -> {
            boolean isBank = "Bank Account".equals(typeCombo.getValue());
            boolean isEWallet = "E-Wallet".equals(typeCombo.getValue());
            
            bankNameLbl.setVisible(isBank); bankNameLbl.setManaged(isBank);
            bankName.setVisible(isBank); bankName.setManaged(isBank);
            accNumLbl.setVisible(isBank); accNumLbl.setManaged(isBank);
            accNumber.setVisible(isBank); accNumber.setManaged(isBank);
            providerLbl.setVisible(isEWallet); providerLbl.setManaged(isEWallet);
            provider.setVisible(isEWallet); provider.setManaged(isEWallet);

            // 2. Tự động tính toán lại chiều cao Dialog khi ẩn/hiện các ô nhập liệu
            Platform.runLater(() -> {
                if (dialog.getDialogPane().getScene() != null) {
                    Window window = dialog.getDialogPane().getScene().getWindow();
                    if (window != null) {
                        window.sizeToScene();
                    }
                }
            });
        };

        typeCombo.setOnAction(e -> updateVisibility.run());
        updateVisibility.run();

        content.getChildren().addAll(new Label("Wallet Type:"), typeCombo, grid);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == saveButtonType) {
                try {
                    double bal = Double.parseDouble(balance.getText());
                    String type = typeCombo.getValue();
                    if ("Bank Account".equals(type)) return new BankAccount(name.getText(), bal, bankName.getText(), accNumber.getText());
                    if ("E-Wallet".equals(type)) return new EWallet(name.getText(), bal, provider.getText());
                    return new CashWallet(name.getText(), bal);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public static Optional<Pair<LocalDate, LocalDate>> showCustomPeriodDialog(LocalDate start, LocalDate end) {
        Dialog<Pair<LocalDate, LocalDate>> dialog = new Dialog<>();
        dialog.setTitle("Chọn thời gian");
        dialog.setHeaderText("Chọn khoảng thời gian muốn xem");

        ButtonType okButtonType = new ButtonType("Xác nhận", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        DatePicker startDatePicker = new DatePicker(start);
        DatePicker endDatePicker = new DatePicker(end);

        grid.add(new Label("Từ ngày:"), 0, 0); grid.add(startDatePicker, 1, 0);
        grid.add(new Label("Đến ngày:"), 0, 1); grid.add(endDatePicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> btn == okButtonType ? new Pair<>(startDatePicker.getValue(), endDatePicker.getValue()) : null);

        return dialog.showAndWait();
    }
}