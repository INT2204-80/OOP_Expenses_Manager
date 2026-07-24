package core.transaction;

import java.time.LocalDate;
import java.time.Period;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import core.wallet.CashWallet;

public class RecurringExpenseTest {

    @Test
    void nextDueDateUsesTheCurrentRecurrenceWindow() {
        LocalDate today = LocalDate.now();
        CashWallet wallet = new CashWallet("Wallet", 1000.0);
        RecurringExpense recurringExpense = new RecurringExpense(
                1,
                50.0,
                today.minusDays(1),
                "Subscription",
                "Bills",
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
        RecurringExpense recurringExpense = new RecurringExpense(
                2,
                40.0,
                today.minusMonths(2),
                "Internet",
                "Bills",
                wallet,
                "Cash",
                Period.ofMonths(1)
        );

        LocalDate nextDueDate = recurringExpense.nextDueDate();

        assertEquals(today, nextDueDate);
        assertTrue(!nextDueDate.isBefore(today));
    }
}
