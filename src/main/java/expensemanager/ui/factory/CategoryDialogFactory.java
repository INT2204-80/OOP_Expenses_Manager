package expensemanager.ui.factory;

import core.Category;
import expensemanager.ui.util.ColorPalette;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Dung dialog "Chinh sua danh muc".
 *
 * <p>Ban goc trong WalletViewController.showEditCategoryDialog() tu goi
 * thang TransactionDAO.updateCategory(...) ngay ben trong
 * resultConverter cua dialog - vi pham SRP (dialog vua lo nhap lieu vua
 * lo persist du lieu) va vi pham DIP (UI factory phu thuoc truc tiep
 * vao lop truy cap CSDL). Theo dung pattern da co san cua
 * {@link TransactionDialogFactory} va {@link BudgetDialogFactory}: class
 * nay chi tra ve du lieu nguoi dung nhap, viec goi DAO va refresh UI
 * do controller (noi goi dialog) dam nhiem.
 */
public final class CategoryDialogFactory {

    private CategoryDialogFactory() {}

    /** Ket qua nguoi dung nhap trong dialog sua danh muc. */
    public static final class CategoryEditResult {
        public final String newName;
        public final String newType;
        public final String newIcon;
        public final String newColor;

        public CategoryEditResult(String newName, String newType, String newIcon, String newColor) {
            this.newName = newName;
            this.newType = newType;
            this.newIcon = newIcon;
            this.newColor = newColor;
        }
    }

    public static Dialog<CategoryEditResult> createEditDialog(Category cat) {
        Dialog<CategoryEditResult> dialog = new Dialog<>();
        dialog.setTitle("Ch\u1ec9nh s\u1eeda danh m\u1ee5c");

        ButtonType saveButtonType = new ButtonType("L\u01b0u", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField nameField = new TextField(cat.getName());

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Expense", "Income");
        String typeStr = cat.getType().name().substring(0, 1).toUpperCase()
                + cat.getType().name().substring(1).toLowerCase();
        typeCombo.setValue(typeStr);

        ComboBox<String> iconCombo = new ComboBox<>();
        iconCombo.getItems().addAll(ColorPalette.CATEGORY_ICONS);
        iconCombo.setCellFactory(ColorPalette.iconCellFactory());
        iconCombo.setButtonCell(ColorPalette.iconCellFactory().call(null));
        iconCombo.setValue(cat.getIcon());

        ComboBox<String> colorCombo = new ComboBox<>();
        colorCombo.getItems().addAll(ColorPalette.COLOR_NAMES);
        colorCombo.setCellFactory(ColorPalette.colorCellFactory());
        colorCombo.setButtonCell(ColorPalette.colorCellFactory().call(null));
        colorCombo.setValue(cat.getColor());

        grid.add(new Label("T\u00ean danh m\u1ee5c:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Lo\u1ea1i:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Icon:"), 0, 2);
        grid.add(iconCombo, 1, 2);
        grid.add(new Label("M\u00e0u s\u1eafc:"), 0, 3);
        grid.add(colorCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String newName = nameField.getText().trim();
                if (newName.isEmpty()) {
                    return null;
                }
                return new CategoryEditResult(newName, typeCombo.getValue(), iconCombo.getValue(), colorCombo.getValue());
            }
            return null;
        });

        return dialog;
    }
}
