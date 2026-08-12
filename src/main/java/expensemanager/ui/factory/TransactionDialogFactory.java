package expensemanager.ui.factory;

import java.time.LocalDate;
import java.util.List;

import core.Category;
import core.TransactionType;
import core.transaction.Expense;
import core.transaction.Income;
import core.transaction.RecurringExpense;
import core.transaction.Transaction;
import core.wallet.Wallet;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class TransactionDialogFactory {

    public static Dialog<Transaction> createDialog(Transaction oldT, List<Category> categories, Wallet wallet) {
        Dialog<Transaction> dialog = new Dialog<>();
        ButtonType actionBtnType = new ButtonType(oldT == null ? "Thêm" : "Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(actionBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> categoryCombo = new ComboBox<>();
        categories.forEach(cat -> categoryCombo.getItems().add(cat.getName()));

        DatePicker datePicker = new DatePicker(oldT != null ? oldT.getDate() : LocalDate.now());
        TextField noteField = new TextField(oldT != null && oldT.getNote() != null ? oldT.getNote() : "");
        TextField amountField = new TextField(oldT != null ? String.format("%.0f", oldT.getAmount()) : "0");

        CheckBox recurringCheck = new CheckBox("Lặp lại");
        ComboBox<String> periodCombo = new ComboBox<>();
        periodCombo.getItems().addAll("Hàng ngày", "Hàng tuần", "Hàng tháng", "Hàng năm");
        periodCombo.getSelectionModel().select("Hàng tháng");
        periodCombo.setDisable(true);

        Label endDateLabel = new Label("Ngày kết thúc (tuỳ chọn):");
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Không giới hạn");
        endDatePicker.setDisable(true);

        // Hàm cập nhật trạng thái Disable cho checkbox Lặp lại dựa vào loại Danh mục
        Runnable updateRecurringState = () -> {
            String selectedCatName = categoryCombo.getValue();
            Category cat = categories.stream()
                    .filter(c -> c.getName().equals(selectedCatName))
                    .findFirst()
                    .orElse(null);

            if (cat != null && cat.getType() == TransactionType.INCOME) {
                recurringCheck.setSelected(false);
                recurringCheck.setDisable(true);
                periodCombo.setDisable(true);
                endDatePicker.setDisable(true);
            } else {
                recurringCheck.setDisable(false);
                periodCombo.setDisable(!recurringCheck.isSelected());
                endDatePicker.setDisable(!recurringCheck.isSelected());
            }
        };

        categoryCombo.valueProperty().addListener((obs, o, n) -> updateRecurringState.run());

        if (oldT != null) {
            categoryCombo.getSelectionModel().select(oldT.getCategory().getName());
            if (oldT instanceof RecurringExpense) {
                recurringCheck.setSelected(true);
                // BUG FIX: truoc day periodCombo khong duoc dong bo lai theo chu ky
                // that da luu cua oldT, nen luc Sua luon hien mac dinh "Hang thang"
                // du chu ky that la gi. Phai anh xa nguoc java.time.Period -> core.Period
                // roi chon dung item trong periodCombo.
                RecurringExpense oldRe = (RecurringExpense) oldT;
                java.time.Period savedPeriod = oldRe.getPeriod();
                String displayName = mapToDisplayName(savedPeriod);
                if (displayName != null) {
                    periodCombo.getSelectionModel().select(displayName);
                }
                if (oldRe.getEndDate() != null) {
                    endDatePicker.setValue(oldRe.getEndDate());
                }
            }
        } else if (!categoryCombo.getItems().isEmpty()) {
            categoryCombo.getSelectionModel().selectFirst();
        }
        
        updateRecurringState.run();

        recurringCheck.setOnAction(e -> {
            periodCombo.setDisable(!recurringCheck.isSelected());
            endDatePicker.setDisable(!recurringCheck.isSelected());
            if (!recurringCheck.isSelected()) {
                endDatePicker.setValue(null);
            }
        });

        grid.add(new Label("Danh mục:"), 0, 0); grid.add(categoryCombo, 1, 0);
        grid.add(new Label("Ngày:"), 0, 1);      grid.add(datePicker, 1, 1);
        grid.add(new Label("Ghi chú:"), 0, 2);   grid.add(noteField, 1, 2);
        grid.add(new Label("Số tiền:"), 0, 3);  grid.add(amountField, 1, 3);
        grid.add(recurringCheck, 0, 4);          grid.add(periodCombo, 1, 4);
        grid.add(endDateLabel, 0, 5);            grid.add(endDatePicker, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Validation - Kiểm tra hợp lệ dữ liệu trước khi lưu
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(actionBtnType);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                double amt = Double.parseDouble(amountField.getText().replaceAll(",", ""));
                if (amt <= 0) {
                    showError("Số tiền phải lớn hơn 0!");
                    event.consume();
                    return;
                }

                String catName = categoryCombo.getValue();
                Category cat = categories.stream()
                        .filter(c -> c.getName().equals(catName))
                        .findFirst()
                        .orElse(new Category(catName, TransactionType.EXPENSE));

                // Kiểm tra nếu là Chi tiêu thì không được vượt quá số dư hiện tại
                if (cat.getType() == TransactionType.EXPENSE && wallet != null) {
                    double currentBal = wallet.getBalance();
                    // Nếu là sửa giao dịch cũ, tính toán số dư hoàn trả giả định
                    if (oldT != null) {
                        currentBal += (oldT instanceof Income) ? -oldT.getAmount() : oldT.getAmount();
                    }
                    if (amt > currentBal) {
                        showError("Số tiền chi tiêu vượt quá số dư ví khả dụng!");
                        event.consume();
                        return;
                    }
                }

                // Kiểm tra Ngày kết thúc (nếu có nhập) không được trước Ngày bắt đầu
                if (recurringCheck.isSelected() && endDatePicker.getValue() != null
                        && datePicker.getValue() != null
                        && endDatePicker.getValue().isBefore(datePicker.getValue())) {
                    showError("Ngày kết thúc không được trước ngày bắt đầu!");
                    event.consume();
                }
            } catch (NumberFormatException e) {
                showError("Số tiền không hợp lệ!");
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == actionBtnType) {
                double amount = Double.parseDouble(amountField.getText().replaceAll(",", ""));
                LocalDate date = datePicker.getValue();
                String note = noteField.getText();
                String catName = categoryCombo.getValue();
                Category cat = categories.stream()
                        .filter(c -> c.getName().equals(catName))
                        .findFirst()
                        .orElse(new Category(catName, TransactionType.EXPENSE));

                int id = oldT != null ? oldT.getId() : (int)(Math.random() * 10000);

                if (cat.getType() == TransactionType.INCOME) {
                    return new Income(id, amount, date, note, cat, wallet, catName);
                } else if (recurringCheck.isSelected()) {
                    core.Period corePeriod = mapToCorePeriod(periodCombo.getValue());
                    java.time.Period p = corePeriod.toJavaPeriod();
                    LocalDate endDate = endDatePicker.getValue();
                    return new RecurringExpense(id, amount, date, note, cat, wallet, catName, p, endDate);
                } else {
                    return new Expense(id, amount, date, note, cat, wallet, catName);
                }
            }
            return null;
        });

        return dialog;
    }

    /**
     * Anh xa ngay ngoc lai: java.time.Period (da luu trong RecurringExpense/DB)
     * sang ten hien thi trong periodCombo, de dialog Sua hien dung chu ky that
     * thay vi mac dinh "Hang thang".
     *
     * @return ten hien thi khop, hoac null neu khong khop voi Period nao trong core.Period
     */
    private static String mapToDisplayName(java.time.Period savedPeriod) {
        if (savedPeriod == null) {
            return null;
        }
        for (core.Period p : core.Period.values()) {
            if (p.toJavaPeriod().equals(savedPeriod)) {
                return p.getDisplayName();
            }
        }
        return null;
    }

    /**
     * Anh xa ten hien thi trong periodCombo ("Hang ngay", "Hang tuan", ...)
     * sang core.Period tuong ung. Mac dinh MONTHLY neu khong khop (an toan).
     */
    private static core.Period mapToCorePeriod(String displayName) {
        if (displayName != null) {
            for (core.Period p : core.Period.values()) {
                if (p.getDisplayName().equals(displayName)) {
                    return p;
                }
            }
        }
        return core.Period.MONTHLY;
    }

    private static void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
