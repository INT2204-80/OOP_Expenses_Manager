package expensemanager.ui;

import java.io.IOException;
import java.util.List;

import core.Budget;
import core.storage.BudgetDAO;
import core.storage.TransactionDAO;
import expensemanager.service.BudgetService;
import expensemanager.ui.factory.BudgetCardFactory;
import expensemanager.ui.factory.BudgetDialogFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class BudgetsViewController {

    @FXML
    private FlowPane budgetsContainer;

    private final BudgetService budgetService;

    // Injection dependencies (chuẩn DIP)
    public BudgetsViewController() {
        this.budgetService = new BudgetService(new BudgetDAO(), new TransactionDAO());
    }

    @FXML
    public void initialize() {
        budgetService.prepareAndRecalculate();
        loadBudgets();
    }

    private void loadBudgets() {
        budgetsContainer.getChildren().clear();

        List<Budget> budgets = budgetService.fetchAllBudgets();

        // 1. Tạo các thẻ Ngân sách
        for (Budget budget : budgets) {
            VBox card = BudgetCardFactory.createBudgetCard(budget, () -> handleDeleteBudget(budget));
            budgetsContainer.getChildren().add(card);
        }

        // 2. Tạo thẻ Placeholder "+"
        VBox placeholder = BudgetCardFactory.createEmptyState(this::showAddBudgetDialog);
        budgetsContainer.getChildren().add(placeholder);
    }

    @FXML
    private void showAddBudgetDialog() {
        BudgetDialogFactory.createAddBudgetDialog(budgetService.fetchAllCategories())
                .showAndWait()
                .ifPresent(budget -> {
                    budgetService.saveNewBudget(budget);
                    loadBudgets();
                });
    }

    private void handleDeleteBudget(Budget budget) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this budget?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                budgetService.removeBudget(budget.getId());
                loadBudgets();
            }
        });
    }

    @FXML
    private void handleNavigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = budgetsContainer.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Unable to load dashboard view.");
            errorAlert.setHeaderText(null);
            errorAlert.showAndWait();
        }
    }
}