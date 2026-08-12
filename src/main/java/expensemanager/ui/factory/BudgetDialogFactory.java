package expensemanager.ui.factory;

import java.time.LocalDate;
import java.util.List;

import core.Budget;
import core.Category;
import core.Period;
import core.TransactionType;
import javafx.application.Platform;
import javafx.geometry.Insets; // Import toàn bộ các layout như VBox, HBox, GridPane...
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class BudgetDialogFactory {

    /** So ky tu toi da cho ten ngan sach, tranh nhap qua dai lam vo layout. */
    private static final int MAX_NAME_LENGTH = 100;
    /** Han muc toi da hop ly (10 ty VND) de tranh nhap nham qua nhieu so 0. */
    private static final double MAX_AMOUNT = 10_000_000_000.0;

    public static Dialog<Budget> createAddBudgetDialog(List<Category> availableCategories) {
        return buildDialog(null, availableCategories);
    }

    /**
     * Dialog sua ngan sach da co san. Cac truong duoc dien san du lieu cu,
     * giu nguyen id cua budget khi tra ve ket qua de goi BudgetDAO#updateBudget.
     */
    public static Dialog<Budget> createEditBudgetDialog(Budget existingBudget, List<Category> availableCategories) {
        if (existingBudget == null) {
            throw new IllegalArgumentException("existingBudget khong duoc null");
        }
        return buildDialog(existingBudget, availableCategories);
    }

    private static Dialog<Budget> buildDialog(Budget existingBudget, List<Category> availableCategories) {
        boolean isEdit = existingBudget != null;

        Dialog<Budget> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Budget" : "Add New Budget");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText(isEdit ? "Save Changes" : "Create a Budget");
        okButton.getStyleClass().add("btn-primary");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);

        // --- General Info Section ---
        Label generalInfoTitle = new Label("💰 General Info");
        generalInfoTitle.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");

        GridPane generalGrid = new GridPane();
        generalGrid.setHgap(15);
        generalGrid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("Budget Name");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        ComboBox<String> currencyBox = new ComboBox<>();
        currencyBox.getItems().add("Vietnamese đồng");
        currencyBox.getSelectionModel().selectFirst();
        currencyBox.setDisable(true);

        generalGrid.add(new Label("Budget Name"), 0, 0);
        generalGrid.add(nameField, 0, 1, 2, 1);
        generalGrid.add(new Label("Amount"), 0, 2);
        generalGrid.add(amountField, 0, 3);
        generalGrid.add(new Label("Currency"), 1, 2);
        generalGrid.add(currencyBox, 1, 3);

        VBox generalBox = new VBox(10, generalInfoTitle, generalGrid);

        // --- Filter Section ---
        Label filterTitle = new Label("⚡ Budget Filter");
        filterTitle.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().add("All categories");
        availableCategories.forEach(c -> categoryBox.getItems().add(c.getName()));
        categoryBox.getSelectionModel().selectFirst();
        categoryBox.setMaxWidth(Double.MAX_VALUE);

        VBox filterBox = new VBox(10, filterTitle, new Label("Budgeted for"), categoryBox);

        // --- Period Section ---
        Label periodTitle = new Label("📅 Budget Period");
        periodTitle.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");

        ToggleGroup periodGroup = new ToggleGroup();
        javafx.scene.layout.HBox periodBoxH = new javafx.scene.layout.HBox(5);

        for (Period p : Period.values()) {
            ToggleButton tb = new ToggleButton(p.name().substring(0, 1) + p.name().substring(1).toLowerCase());
            tb.setToggleGroup(periodGroup);
            tb.setUserData(p);
            tb.setStyle("-fx-background-radius: 15; -fx-padding: 5 12;");
            periodBoxH.getChildren().add(tb);
        }

        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        VBox periodBox = new VBox(10, periodTitle, new Label("Recurrence"), periodBoxH, new Label("Start date"), startDatePicker);

        content.getChildren().addAll(generalBox, filterBox, periodBox);
        dialogPane.setContent(content);

        // --- Dien san du lieu khi o che do Sua ---
        if (isEdit) {
            nameField.setText(existingBudget.getName());
            amountField.setText(String.valueOf(existingBudget.getLimitAmount()));

            if (existingBudget.getCategory() != null
                    && categoryBox.getItems().contains(existingBudget.getCategory().getName())) {
                categoryBox.getSelectionModel().select(existingBudget.getCategory().getName());
            } else {
                categoryBox.getSelectionModel().selectFirst();
            }

            for (javafx.scene.Node node : periodBoxH.getChildren()) {
                if (node instanceof ToggleButton tb && tb.getUserData() == existingBudget.getPeriod()) {
                    tb.setSelected(true);
                }
            }

            if (existingBudget.getStartDate() != null) {
                startDatePicker.setValue(existingBudget.getStartDate());
            }
        } else {
            // Mac dinh MONTHLY khi tao moi
            for (javafx.scene.Node node : periodBoxH.getChildren()) {
                if (node instanceof ToggleButton tb && tb.getUserData() == Period.MONTHLY) {
                    tb.setSelected(true);
                }
            }
        }

        Platform.runLater(nameField::requestFocus);

        // --- Input Validation Event Filter ---
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            String amountStr = amountField.getText() == null ? "" : amountField.getText().replaceAll(",", "").trim();

            if (name.isEmpty()) {
                showErrorAlert("Tên ngân sách không được để trống!");
                event.consume();
                return;
            }

            if (name.length() > MAX_NAME_LENGTH) {
                showErrorAlert("Tên ngân sách không được vượt quá " + MAX_NAME_LENGTH + " ký tự!");
                event.consume();
                return;
            }

            if (amountStr.isEmpty()) {
                showErrorAlert("Vui lòng nhập số tiền hạn mức!");
                event.consume();
                return;
            }

            double amt;
            try {
                amt = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                showErrorAlert("Số tiền nhập vào không hợp lệ! Chỉ được nhập số.");
                event.consume();
                return;
            }

            if (!Double.isFinite(amt)) {
                showErrorAlert("Số tiền nhập vào không hợp lệ!");
                event.consume();
                return;
            }

            if (amt <= 0) {
                showErrorAlert("Số tiền hạn mức phải lớn hơn 0!");
                event.consume();
                return;
            }

            if (amt > MAX_AMOUNT) {
                showErrorAlert(String.format("Số tiền hạn mức không được vượt quá %,.0f VND!", MAX_AMOUNT));
                event.consume();
                return;
            }

            if (periodGroup.getSelectedToggle() == null) {
                showErrorAlert("Vui lòng chọn chu kỳ ngân sách (Budget Period)!");
                event.consume();
                return;
            }

            if (startDatePicker.getValue() == null) {
                showErrorAlert("Vui lòng chọn ngày bắt đầu!");
                event.consume();
                return;
            }
        });

        // --- Convert Result ---
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String name = nameField.getText().trim();
                double amount = Double.parseDouble(amountField.getText().replaceAll(",", "").trim());
                String catName = categoryBox.getValue();

                Category category = null;
                if (!"All categories".equals(catName)) {
                    category = new Category(catName, TransactionType.EXPENSE);
                }

                Period period = (Period) periodGroup.getSelectedToggle().getUserData();
                LocalDate startDate = startDatePicker.getValue();

                Budget result = new Budget(isEdit ? existingBudget.getId() : 0, name, amount, category, period, startDate);
                if (isEdit) {
                    // Giu lai so tien da chi cu, vi day la sua thong tin ngan sach
                    // chu khong phai reset lai qua trinh chi tieu.
                    result.setSpentAmount(existingBudget.getCurrentSpent());
                }
                return result;
            }
            return null;
        });

        return dialog;
    }

    private static void showErrorAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
