package expensemanager.ui.controllers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.Category;
import core.TransactionType;
import core.storage.TransactionDAO;
import expensemanager.service.BudgetService;
import expensemanager.ui.factory.CategoryDialogFactory;
import expensemanager.ui.factory.CategoryRowFactory;
import expensemanager.ui.util.ColorPalette;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class WalletCategoryManager {

    private final BudgetService budgetService;
    private final TransactionDAO transactionDAO;

    private final List<Category> allCategories = new ArrayList<>();
    private final Set<Category> selectedCategories = new HashSet<>();

    public WalletCategoryManager(BudgetService budgetService, TransactionDAO transactionDAO) {
        this.budgetService = budgetService;
        this.transactionDAO = transactionDAO;
    }

    public List<Category> getAllCategories() {
        return allCategories;
    }

    public void setupCategoryCreationCombos(ComboBox<String> categoryTypeCombo,
                                            ComboBox<String> categoryIconCombo,
                                            ComboBox<String> categoryColorCombo) {
        if (categoryIconCombo != null) {
            categoryIconCombo.getItems().clear();
            categoryIconCombo.getItems().addAll(ColorPalette.CATEGORY_ICONS);
            categoryIconCombo.setCellFactory(ColorPalette.iconCellFactory());
            categoryIconCombo.setButtonCell(ColorPalette.iconCellFactory().call(null));
            categoryIconCombo.getSelectionModel().selectFirst();
        }
        if (categoryColorCombo != null) {
            categoryColorCombo.getItems().clear();
            categoryColorCombo.getItems().addAll(ColorPalette.COLOR_NAMES);
            categoryColorCombo.setCellFactory(ColorPalette.colorCellFactory());
            categoryColorCombo.setButtonCell(ColorPalette.colorCellFactory().call(null));
            categoryColorCombo.getSelectionModel().selectFirst();
        }
        if (categoryTypeCombo != null) {
            categoryTypeCombo.getItems().clear();
            categoryTypeCombo.getItems().addAll("Expense", "Income");
            categoryTypeCombo.getSelectionModel().selectFirst();
        }
    }

    public void loadCategoriesToUI(VBox incomeCategoriesContainer, VBox expenseCategoriesContainer, Runnable refreshFilters) {
        if (incomeCategoriesContainer == null || expenseCategoriesContainer == null) {
            return;
        }
        incomeCategoriesContainer.getChildren().clear();
        expenseCategoriesContainer.getChildren().clear();
        allCategories.clear();
        selectedCategories.clear();

        allCategories.addAll(budgetService.fetchAllCategories());

        for (Category cat : allCategories) {
            HBox row = CategoryRowFactory.createCategoryRow(
                    cat,
                    (c, selected) -> {
                        if (selected) selectedCategories.add(c); else selectedCategories.remove(c);
                    },
                    c -> showEditCategoryDialog(c, incomeCategoriesContainer, expenseCategoriesContainer, refreshFilters),
                    c -> deleteCategorySoft(c, incomeCategoriesContainer, expenseCategoriesContainer, refreshFilters));

            if (cat.getType() == TransactionType.INCOME) {
                incomeCategoriesContainer.getChildren().add(row);
            } else {
                expenseCategoriesContainer.getChildren().add(row);
            }
        }

        if (refreshFilters != null) {
            refreshFilters.run();
        }
    }

    public void handleCreateCategory(TextField newCategoryNameField, ComboBox<String> categoryTypeCombo,
                                     ComboBox<String> categoryIconCombo, ComboBox<String> categoryColorCombo,
                                     VBox incomeCategoriesContainer, VBox expenseCategoriesContainer, Runnable refreshFilters) {
        if (newCategoryNameField == null || categoryTypeCombo == null) {
            return;
        }
        String name = newCategoryNameField.getText();
        String type = categoryTypeCombo.getValue();
        String icon = categoryIconCombo != null ? categoryIconCombo.getValue() : null;
        String color = categoryColorCombo != null ? categoryColorCombo.getValue() : null;

        if (name == null || name.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Tên danh mục không được để trống!", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (type == null) {
            return;
        }

        try {
            transactionDAO.getOrCreateCategoryId(name.trim(), type.toUpperCase(), icon, color);
            newCategoryNameField.clear();
            loadCategoriesToUI(incomeCategoriesContainer, expenseCategoriesContainer, refreshFilters);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleDeleteCategories(VBox incomeCategoriesContainer, VBox expenseCategoriesContainer, Runnable refreshFilters) {
        if (selectedCategories.isEmpty()) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xóa danh mục");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa " + selectedCategories.size() + " danh mục đã chọn?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            for (Category cat : selectedCategories) {
                try {
                    transactionDAO.softDeleteCategory(cat.getName(), cat.getType().name());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            selectedCategories.clear();
            loadCategoriesToUI(incomeCategoriesContainer, expenseCategoriesContainer, refreshFilters);
        }
    }

    public void handleMergeCategories(VBox incomeCategoriesContainer, VBox expenseCategoriesContainer, Runnable refreshFilters) {
        if (selectedCategories.size() < 2) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Cần chọn ít nhất 2 danh mục để gộp!");
            alert.showAndWait();
            return;
        }

        List<Category> choices = new ArrayList<>(selectedCategories);
        ChoiceDialog<Category> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Gộp danh mục");
        dialog.setHeaderText("Chọn danh mục ĐÍCH (danh mục sẽ được giữ lại):");
        dialog.setContentText("Danh mục đích:");

        dialog.showAndWait().ifPresent(target -> {
            List<Category> sources = new ArrayList<>(selectedCategories);
            sources.remove(target);

            try {
                transactionDAO.mergeCategories(sources, target);
                selectedCategories.clear();
                loadCategoriesToUI(incomeCategoriesContainer, expenseCategoriesContainer, refreshFilters);
            } catch (SQLException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Có lỗi xảy ra khi gộp danh mục!");
                errorAlert.showAndWait();
            }
        });
    }

    private void deleteCategorySoft(Category cat, VBox incomeContainer, VBox expenseContainer, Runnable refreshFilters) {
        try {
            transactionDAO.softDeleteCategory(cat.getName(), cat.getType().name());
            loadCategoriesToUI(incomeContainer, expenseContainer, refreshFilters);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showEditCategoryDialog(Category cat, VBox incomeContainer, VBox expenseContainer, Runnable refreshFilters) {
        CategoryDialogFactory.createEditDialog(cat).showAndWait().ifPresent(result -> {
            try {
                transactionDAO.updateCategory(
                        cat.getName(), cat.getType().name(),
                        result.newName, result.newType, result.newIcon, result.newColor);
                loadCategoriesToUI(incomeContainer, expenseContainer, refreshFilters);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }
}