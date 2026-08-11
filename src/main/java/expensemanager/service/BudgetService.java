package expensemanager.service;

import java.util.List;

import core.Budget;
import core.Category;
import core.storage.BudgetDAO;
import core.storage.TransactionDAO;

public class BudgetService {
    private final BudgetDAO budgetDAO;
    private final TransactionDAO transactionDAO;

    public BudgetService(BudgetDAO budgetDAO, TransactionDAO transactionDAO) {
        this.budgetDAO = budgetDAO;
        this.transactionDAO = transactionDAO;
    }

    public void prepareAndRecalculate() {
        budgetDAO.recalculateAllBudgetsSpentAmount();
    }

    public List<Budget> fetchAllBudgets() {
        return budgetDAO.getAllBudgets();
    }

    public List<Category> fetchAllCategories() {
        return transactionDAO.getAllCategories();
    }

    public void saveNewBudget(Budget budget) {
        budgetDAO.addBudget(budget, -1);
        budgetDAO.recalculateBudgetSpentAmount(budget);
        budgetDAO.updateBudget(budget);
    }

    public void removeBudget(int budgetId) {
        budgetDAO.deleteBudget(budgetId);
    }
}