package core.storage;

import core.Budget;
import java.util.List;

public interface IBudgetDAO {
    void addBudget(Budget budget, int walletId);
    void updateBudget(Budget budget);
    void deleteBudget(int budgetId);
    List<Budget> getAllBudgets();
    List<Budget> getBudgetsByWallet(int walletId);
}
