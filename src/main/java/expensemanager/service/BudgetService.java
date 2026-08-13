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

    public List<Category> fetchAllCategories() {
        return transactionDAO.getAllCategories();
    }

    public void removeBudget(int budgetId) {
        budgetDAO.deleteBudget(budgetId);
    }

    public void updateBudget(Budget budget) {
        budgetDAO.updateBudget(budget);
    }
}
