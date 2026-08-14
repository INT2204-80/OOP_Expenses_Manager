package expensemanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.Period;

import org.junit.jupiter.api.Test;

import core.Category;
import core.TransactionType;
import core.transaction.Expense;
import core.transaction.RecurringExpense;
import core.wallet.CashWallet;
import core.wallet.Wallet;
import core.storage.ITransactionDAO;

public class TransactionServiceTest {

    private TransactionService createService() {
        return new TransactionService(new DummyTransactionDAO(), null);
    }

    private Wallet createWalletWithBalance(double balance) {
        return new CashWallet("Test Wallet", balance);
    }

    private Category getTestCategory() {
        return new Category("Test", TransactionType.EXPENSE);
    }

    @Test
    void insufficientBackfillIsAtomic() {
        TransactionService service = createService();
        Wallet wallet = createWalletWithBalance(150.0);
        
        RecurringExpense re = new RecurringExpense(1, 100.0, LocalDate.now().minusMonths(2), 
                "Sub", getTestCategory(), wallet, "Cash", Period.ofMonths(1));
        re.nextDueDate(); // updates passedPeriods to 2
        
        assertThrows(IllegalStateException.class, () -> service.addRecurringExpenseWithBackfill(re, wallet));
        
        assertEquals(150.0, wallet.getBalance(), 0.001);
        assertEquals(0, wallet.getTransactions().size());
    }

    @Test
    void insufficientUpdateIsAtomic() {
        TransactionService service = createService();
        Wallet wallet = createWalletWithBalance(50.0);
        
        Expense oldExp = new Expense(1, 20.0, LocalDate.now(), "Small", getTestCategory(), wallet, "Cash");
        wallet.addTransaction(oldExp);
        
        Expense newExp = new Expense(1, 100.0, LocalDate.now(), "Large", getTestCategory(), wallet, "Cash");
        
        // This will throw RuntimeException caused by IllegalStateException
        Exception e = assertThrows(RuntimeException.class, () -> service.updateTransactionAndUpdateWallet(oldExp, newExp, wallet));
        assertEquals(IllegalStateException.class, e.getCause().getClass());
        
        assertEquals(50.0, wallet.getBalance(), 0.001);
        assertEquals(1, wallet.getTransactions().size());
        assertEquals(oldExp, wallet.getTransactions().get(0));
    }

    @Test
    void insufficientRecurringProcessingIsAtomic() {
        TransactionService service = createService();
        Wallet wallet = createWalletWithBalance(150.0);
        
        RecurringExpense re = new RecurringExpense(1, 100.0, LocalDate.now().minusMonths(2), 
                "Sub", getTestCategory(), wallet, "Cash", Period.ofMonths(1));
        re.setPassedPeriods(0); // assume db loaded 0
        
        wallet.addTransaction(re);
        
        // When we call getTransactionsByWallet, DummyTransactionDAO returns the wallet's transactions
        Exception e = assertThrows(RuntimeException.class, () -> service.getTransactionsByWallet(wallet));
        assertEquals(IllegalStateException.class, e.getCause().getClass());
        
        // Balance should remain unchanged, list should be unchanged, passedPeriods should revert
        assertEquals(150.0, wallet.getBalance(), 0.001);
        assertEquals(1, wallet.getTransactions().size());
        assertEquals(0, ((RecurringExpense) wallet.getTransactions().get(0)).getPassedPeriods());
    }

    // A simple mock for ITransactionDAO to test getTransactionsByWallet
    private static class DummyTransactionDAO implements ITransactionDAO {
        @Override
        public java.util.List<core.transaction.Transaction> getTransactionsByWallet(Wallet wallet) {
            // Return a copy of the transactions currently in the wallet
            return new java.util.ArrayList<>(wallet.getTransactions());
        }
        @Override public double getTotalAmountForPeriod(String t, LocalDate s, LocalDate e) { return 0; }
        @Override public void saveTransaction(core.transaction.Transaction t, int w) {}
        @Override public void saveTransaction(java.sql.Connection c, core.transaction.Transaction t, int w) {}
        @Override public void deleteTransaction(int t) {}
        @Override public void deleteTransaction(java.sql.Connection c, int t) {}
        @Override public void updateTransaction(core.transaction.Transaction t, int w) {}
        @Override public void updateTransaction(java.sql.Connection c, core.transaction.Transaction t, int w) {}
    }
}
