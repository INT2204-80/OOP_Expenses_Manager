package expensemanager.services;

import java.time.LocalDate;
import java.util.List;

import core.Budget;
import core.Category;
import core.TransactionType;
import core.transaction.Transaction;

public class BudgetServices {

    public double calculateSpentByCategory(List<Transaction> transactions, Category category, LocalDate startDate, LocalDate endDate) {
        if (category == null || transactions == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("Invalid parameters for budget calculation");
        }

        double spent = 0.0;
        for (Transaction transaction : transactions) {
            if (transaction == null) {
                continue;
            }
            if (transaction.getType() != TransactionType.EXPENSE) {
                continue;
            }
            if (!category.getName().equalsIgnoreCase(transaction.getCategory().getName())) {
                continue;
            }
            LocalDate transactionDate = transaction.getDate();
            if (transactionDate != null && !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate)) {
                spent += transaction.getAmount();
            }
        }
        return spent;
    }

    public boolean isBudgetExceeded(Budget budget, List<Transaction> transactions) {
        if (budget == null || transactions == null) {
            throw new IllegalArgumentException("Invalid budget check parameters");
        }
        double spent = calculateSpentByCategory(transactions, budget.getCategory(), budget.getStartDate(), budget.getEndDate());
        return budget.isExceeded(spent);
    }
}
