package expensemanager.ui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import core.Category;
import core.TransactionType;
import core.transaction.Expense;
import core.transaction.Transaction;
import core.wallet.Wallet;
import core.WalletType;

class TransactionFilterTest {

    @Test
    void futureOnlyShouldIncludeOnlyTransactionsAfterToday() {
        Wallet wallet = new Wallet("Test wallet", 1000) {
            @Override
            public WalletType getWalletType() {
                return WalletType.CASH;
            }
        };

        Category category = new Category("Food", TransactionType.EXPENSE, "🍔", "red");
        Transaction past = new Expense(1, 10, LocalDate.now().minusDays(1), "past", category, wallet, "Cash");
        Transaction today = new Expense(2, 20, LocalDate.now(), "today", category, wallet, "Cash");
        Transaction future = new Expense(3, 30, LocalDate.now().plusDays(2), "future", category, wallet, "Cash");

        List<Transaction> filtered = TransactionFilter.create()
                .byFutureMode(LocalDate.now(), true)
                .apply(List.of(past, today, future));

        assertEquals(List.of(future), filtered);
    }
}
