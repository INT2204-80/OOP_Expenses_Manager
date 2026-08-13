package expensemanager.service;

import java.util.List;

import core.Budget;
import core.Category;
import core.storage.IBudgetDAO;
import core.storage.ICategoryDAO;

public class BudgetService {
    private final IBudgetDAO budgetDAO;
    private final ICategoryDAO categoryDAO;

    public BudgetService(IBudgetDAO budgetDAO, ICategoryDAO categoryDAO) {
        this.budgetDAO = budgetDAO;
        this.categoryDAO = categoryDAO;
    }

    public List<Category> fetchAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public void removeBudget(int budgetId) {
        budgetDAO.deleteBudget(budgetId);
    }

    public void updateBudget(Budget budget) {
        budgetDAO.updateBudget(budget);
    }

    public void addBudget(Budget budget, int walletId) {
        budgetDAO.addBudget(budget, walletId);
    }

    public List<Budget> getBudgetsByWallet(int walletId) {
        return budgetDAO.getBudgetsByWallet(walletId);
    }
}
