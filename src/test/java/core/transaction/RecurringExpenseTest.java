package core.transaction;

import java.time.LocalDate;
import java.time.Period;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import core.Category;
import core.TransactionType;
import core.wallet.CashWallet;

public class RecurringExpenseTest {

    @Test
    void nextDueDateUsesTheCurrentRecurrenceWindow() {
        LocalDate today = LocalDate.now();
        CashWallet wallet = new CashWallet("Wallet", 1000.0);
        Category category = new Category("Subscription", TransactionType.EXPENSE);
        RecurringExpense recurringExpense = new RecurringExpense(
                1,
                50.0,
                today.minusDays(1),
                "Subscription",
                category,
                wallet,
                "Cash",
                Period.ofDays(7)
        );

        LocalDate nextDueDate = recurringExpense.nextDueDate();

        assertEquals(today.plusDays(6), nextDueDate);
        assertTrue(!nextDueDate.isBefore(today));
    }

    @Test
    void nextDueDateMovesForwardWhenTheStoredDueDateIsAlreadyPast() {
        LocalDate today = LocalDate.now();
        CashWallet wallet = new CashWallet("Wallet", 1000.0);
        Category category = new Category("Internet", TransactionType.EXPENSE);
        RecurringExpense recurringExpense = new RecurringExpense(
                2,
                40.0,
                today.minusMonths(2),
                "Internet",
                category,
                wallet,
                "Cash",
                Period.ofMonths(1)
        );

        LocalDate nextDueDate = recurringExpense.nextDueDate();

        assertEquals(recurringExpense.getDate().plusMonths(3), nextDueDate);
        assertTrue(nextDueDate.isAfter(today));
    }

    private RecurringExpense createRecurringExpense(Period period) {
        return new RecurringExpense(
                1,
                50.0,
                LocalDate.now(),
                "Subscription",
                new Category("Subscription", TransactionType.EXPENSE),
                new CashWallet("Wallet", 1000.0),
                "Cash",
                period
        );
    }

    @Test
    void constructorRejectsInvalidPeriods() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> createRecurringExpense(Period.ZERO));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> createRecurringExpense(Period.ofDays(-1)));
    }

    @Test
    void setPassedPeriodsRejectsNegativeValues() {
        RecurringExpense recurringExpense = createRecurringExpense(Period.ofDays(7));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> recurringExpense.setPassedPeriods(-1));
    }
}
