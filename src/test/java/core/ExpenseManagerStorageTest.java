package core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import core.transaction.Expense;
import core.transaction.Income;
import core.transaction.Transaction;
import core.wallet.CashWallet;
import expensemanager.ExpenseManager;
import expensemanager.storage.CsvStorage;
import expensemanager.storage.JsonStorage;
import expensemanager.storage.Storage;

public class ExpenseManagerStorageTest {

    @Test
    void budgetReportsWhetherLimitIsExceeded() {
        Category food = new Category("Food", TransactionType.EXPENSE);
        Budget budget = new Budget(food, 500.0, Period.MONTHLY);

        assertFalse(budget.isExceeded(400.0));
        assertTrue(budget.isExceeded(600.0));
    }

    @Test
    void expenseManagerAggregatesMonthlySummaryAndCategoryStatistics() {
        CashWallet wallet = new CashWallet("Main", 1000.0);
        Category food = new Category("Food", TransactionType.EXPENSE);
        Category salary = new Category("Salary", TransactionType.INCOME);
        ExpenseManager manager = new ExpenseManager(new CsvStorage());

        manager.getWallets().add(wallet);
        manager.getCategories().add(food);
        manager.getCategories().add(salary);

        manager.addTransaction(new Expense(1, 50.0, LocalDate.of(2026, 7, 10), "Lunch", food, wallet, "Cash"));
        manager.addTransaction(new Income(2, 200.0, LocalDate.of(2026, 7, 11), "Salary", salary, wallet, "Company"));

        Map<String, Double> summary = manager.monthlySummary(2026, 7);
        assertEquals(150.0, summary.get("net"), 0.001);
        assertEquals(50.0, summary.get("expenses"), 0.001);
        assertEquals(200.0, summary.get("income"), 0.001);

        Map<Category, Double> stats = manager.statisticsByCategory();
        assertEquals(50.0, stats.get(food), 0.001);
        assertEquals(200.0, stats.get(salary), 0.001);
    }

    @Test
    void storageImplementationsPersistAndLoadTransactions() throws Exception {
        CashWallet wallet = new CashWallet("Main", 1000.0);
        Category food = new Category("Food", TransactionType.EXPENSE);
        Category salary = new Category("Salary", TransactionType.INCOME);
        Transaction expense = new Expense(1, 40.0, LocalDate.of(2026, 7, 12), "Dinner", food, wallet, "Cash");
        Transaction income = new Income(2, 300.0, LocalDate.of(2026, 7, 13), "Salary", salary, wallet, "Company");

        Path csvPath = Files.createTempFile("expenses", ".csv");
        Storage csvStorage = new CsvStorage();
        csvStorage.save(List.of(expense, income), csvPath.toString());
        List<Transaction> loadedCsv = csvStorage.load(csvPath.toString());
        assertEquals(2, loadedCsv.size());
        assertEquals(TransactionType.EXPENSE, loadedCsv.get(0).getType());

        Path jsonPath = Files.createTempFile("expenses", ".json");
        Storage jsonStorage = new JsonStorage();
        jsonStorage.save(List.of(expense, income), jsonPath.toString());
        List<Transaction> loadedJson = jsonStorage.load(jsonPath.toString());
        assertEquals(2, loadedJson.size());
        assertEquals(TransactionType.INCOME, loadedJson.get(1).getType());
    }
}
