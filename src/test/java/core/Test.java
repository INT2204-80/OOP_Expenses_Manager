package core;

import java.time.LocalDate;
import java.time.Period;

public class Test {
    public static void main(String[] args) {
        testIncome();
        testExpense();
        testRecurringExpense();
        System.out.println("All tests passed.");
    }

    private static void testIncome() {
        Income income = new Income(1, 500000, LocalDate.of(2026, 7, 20), "Salary payment", "Salary", "Bank", "Employer");
        assertEquals(TransactionType.INCOME, income.getType(), "Income type should be INCOME");
        assertEquals(500000.0, income.getSignedAmount(), "Income signed amount should be positive");
        assertEquals("Employer", income.getSource(), "Income source should be preserved");
        income.setAmount(-100);
    }

    private static void testExpense() {
        Expense expense = new Expense(2, 120000, LocalDate.of(2026, 7, 20), "Groceries", "Food", "Wallet", "Cash");
        assertEquals(TransactionType.EXPENSE, expense.getType(), "Expense type should be EXPENSE");
        assertEquals(-120000.0, expense.getSignedAmount(), "Expense signed amount should be positive");
        assertEquals("Cash", expense.getPaymentMethod(), "Payment method should be preserved");
    }

    private static void testRecurringExpense() {
        RecurringExpense recurringExpense = new RecurringExpense(
            3,
            300000,
            LocalDate.of(2026, 7, 20),
            "Rent",
            "Housing",
            "Bank",
            "Bank Transfer",
            Period.ofMonths(1)
        );
        assertEquals(Period.ofMonths(1), recurringExpense.getPeriod(), "Recurring period should be preserved");
        assertEquals(LocalDate.of(2026, 8, 20), recurringExpense.nextDueDate(), "Next due date should be one period after the date");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " Expected=" + expected + " Actual=" + actual);
        }
    }

    private static void assertEquals(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 1e-9) {
            throw new AssertionError(message + " Expected=" + expected + " Actual=" + actual);
        }
    }
}
