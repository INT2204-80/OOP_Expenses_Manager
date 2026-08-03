package core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import core.transaction.Expense;
import core.transaction.Income;
import core.transaction.Transaction;
import core.wallet.CashWallet;

public class BudgetTest {
    @Test
    void testBudgetInitialization() {
        Category foodCategory = new Category("Food", TransactionType.EXPENSE);
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        Budget budget = new Budget(1, "Ăn uống tháng 8", 5000000.0, foodCategory, start, end);

        assertEquals(1, budget.getId());
        assertEquals("Ăn uống tháng 8", budget.getName());
        assertEquals(5000000.0, budget.getLimitAmount());
        assertEquals(0.0, budget.getCurrentSpent());
        assertEquals(5000000.0, budget.getRemainingAmount());
        assertEquals(0.0, budget.getUsagePercentage(), 0.001);
        assertFalse(budget.isExceed());
    }

    @Test
    void testAddSpentAndIsExceed() {
        Category foodCategory = new Category("Food", TransactionType.EXPENSE);
        Budget budget = new Budget(1, "Food", 1000000.0, foodCategory, Period.MONTHLY, LocalDate.of(2026, 8, 1));

        budget.addSpent(600000.0);
        assertEquals(600000.0, budget.getCurrentSpent());
        assertEquals(400000.0, budget.getRemainingAmount());
        assertEquals(60.0, budget.getUsagePercentage(), 0.001);
        assertFalse(budget.isExceed());

        budget.addSpent(500000.0);
        assertEquals(1100000.0, budget.getCurrentSpent());
        assertEquals(0.0, budget.getRemainingAmount()); // Remaining không âm (tối thiểu 0)
        assertEquals(110.0, budget.getUsagePercentage(), 0.001);
        assertTrue(budget.isExceed());
    }

    @Test
    void testCalcDailyAllowance() {
        Category transport = new Category("Transport", TransactionType.EXPENSE);
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 10); // Tổng 10 ngày
        Budget budget = new Budget(2, "Đi lại", 1000000.0, transport, start, end);

        // Ngày 1/8 (còn 10 ngày từ 1/8 -> 10/8): 1,000,000 / 10 = 100,000
        assertEquals(100000.0, budget.calcDailyAllowance(LocalDate.of(2026, 8, 1)), 0.01);

        // Đã tiêu 400,000, còn lại 600,000
        budget.addSpent(400000.0);
        // Ngày 5/8 (còn 6 ngày từ 5/8 -> 10/8): 600,000 / 6 = 100,000
        assertEquals(100000.0, budget.calcDailyAllowance(LocalDate.of(2026, 8, 5)), 0.01);
    }

    @Test
    void testUpdateSpentFromTransactions() {
        CashWallet wallet = new CashWallet("Tiền mặt", 10000000.0);
        Category food = new Category("Ăn uống", TransactionType.EXPENSE);
        Category entertainment = new Category("Giải trí", TransactionType.EXPENSE);

        Budget foodBudget = new Budget(1, "Ngân sách Ăn uống", 2000000.0, food, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Expense(101, 150000.0, LocalDate.of(2026, 8, 2), "Ăn trưa", food, wallet, "Cash"));
        transactions.add(new Expense(102, 350000.0, LocalDate.of(2026, 8, 5), "Ăn tối", food, wallet, "Cash"));
        transactions.add(new Expense(103, 500000.0, LocalDate.of(2026, 8, 6), "Xem phim", entertainment, wallet, "Cash")); // Khác category
        transactions.add(new Income(104, 1000000.0, LocalDate.of(2026, 8, 1), "Lương", food, wallet, "Cong ty")); // Income không tính vào spent

        foodBudget.updateSpentFromTransactions(transactions);

        assertEquals(500000.0, foodBudget.getCurrentSpent());
        assertEquals(1500000.0, foodBudget.getRemainingAmount());
        assertFalse(foodBudget.isExceed());
    }

    @Test
    void testResetSpent() {
        Category shopping = new Category("Shopping", TransactionType.EXPENSE);
        Budget budget = new Budget(3, "Mua sắm", 3000000.0, shopping, Period.MONTHLY, LocalDate.of(2026, 8, 1));

        budget.addSpent(1500000.0);
        assertEquals(1500000.0, budget.getCurrentSpent());

        budget.resetSpent();
        assertEquals(0.0, budget.getCurrentSpent());
        assertEquals(3000000.0, budget.getRemainingAmount());
        assertFalse(budget.isExceed());
    }

    @Test
    void testInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Budget(1, "", 1000.0, null, LocalDate.now(), LocalDate.now());
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Budget(1, "Test", -500.0, null, LocalDate.now(), LocalDate.now());
        });
    }
}
