package expensemanager.services;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import core.Budget;
import core.Category;
import core.TransactionType;
import core.transaction.Expense;
import core.transaction.Transaction;
import core.wallet.CashWallet;

public class BudgetServicesTest {

    @Test
    void calculatesExpensesForSameCategoryAndChecksBudgetExceeded() {
        CashWallet wallet = new CashWallet("Main", 1000.0);
        Category food = new Category("Food", TransactionType.EXPENSE);
        Category transport = new Category("Transport", TransactionType.EXPENSE);
        Budget budget = new Budget(food, 100.0, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        List<Transaction> transactions = List.of(
                new Expense(1, 60.0, LocalDate.of(2026, 1, 10), "Lunch", food, wallet, "Cash"),
                new Expense(2, 30.0, LocalDate.of(2026, 1, 12), "Coffee", food, wallet, "Cash"),
                new Expense(3, 40.0, LocalDate.of(2026, 1, 15), "Taxi", transport, wallet, "Cash")
        );

        BudgetServices services = new BudgetServices();
        double spent = services.calculateSpentByCategory(transactions, food, budget.getStartDate(), budget.getEndDate());

        assertEquals(90.0, spent, 0.001);
        assertFalse(services.isBudgetExceeded(budget, transactions));

        Budget exceededBudget = new Budget(food, 80.0, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        assertTrue(services.isBudgetExceeded(exceededBudget, transactions));
    }
}
