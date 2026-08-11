package expensemanager.ui.controllers;

import java.util.List;

import core.Budget;
import core.Category;
import core.storage.BudgetDAO;
import core.transaction.Transaction;
import core.wallet.Wallet;
import expensemanager.service.BudgetService;
import expensemanager.ui.factory.BudgetCardFactory;
import expensemanager.ui.factory.BudgetDialogFactory;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;

public class WalletBudgetManager {

    private final BudgetDAO budgetDAO;
    private final BudgetService budgetService;

    public WalletBudgetManager(BudgetDAO budgetDAO, BudgetService budgetService) {
        this.budgetDAO = budgetDAO;
        this.budgetService = budgetService;
    }

    public void handleShowAddBudgetDialog(List<Category> allCategories, Wallet currentWallet, VBox budgetsContainer) {
        Dialog<Budget> dialog = BudgetDialogFactory.createAddBudgetDialog(allCategories);
        dialog.showAndWait().ifPresent(budget -> {
            budgetDAO.addBudget(budget, currentWallet.getId());
            renderBudgets(budgetsContainer, currentWallet, allCategories);
        });
    }

    public void renderBudgets(VBox budgetsContainer, Wallet currentWallet, List<Category> allCategories) {
        if (budgetsContainer == null || currentWallet == null) {
            return;
        }
        budgetsContainer.getChildren().clear();

        List<Budget> budgets = budgetDAO.getBudgetsByWallet(currentWallet.getId());

        if (budgets.isEmpty()) {
            budgetsContainer.getChildren().add(BudgetCardFactory.createEmptyState(
                    () -> handleShowAddBudgetDialog(allCategories, currentWallet, budgetsContainer)));
            return;
        }

        List<Transaction> walletTx = currentWallet.getTransactions();
        for (Budget budget : budgets) {
            budget.updateSpentFromTransactions(walletTx);
            VBox card = BudgetCardFactory.createBudgetCard(budget, () -> {
                budgetService.removeBudget(budget.getId());
                renderBudgets(budgetsContainer, currentWallet, allCategories);
            });
            budgetsContainer.getChildren().add(card);
        }
    }
}