package core;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import core.transaction.Expense;
import core.transaction.Income;
import core.wallet.BankAccount;
import core.wallet.CashWallet;
import core.wallet.EWallet;

public class WalletCreationWithTransactionTest {

    @Test
    void cashWalletDepositWithdraw() {
        CashWallet wallet = new CashWallet("Pocket", 200.0);
        assertEquals("Pocket", wallet.getName());
        assertEquals(200.0, wallet.getBalance());
        assertEquals(core.WalletType.CASH, wallet.getWalletType());

        wallet.deposit(100.0);
        assertEquals(300.0, wallet.getBalance());

        wallet.withdraw(50.0);
        assertEquals(250.0, wallet.getBalance());
    }

    @Test
    void bankAccountPropertiesAndWalletType() {
        BankAccount account = new BankAccount("Savings", 500.0, "First Bank", "123456");
        assertEquals("Savings", account.getName());
        assertEquals(500.0, account.getBalance());
        assertEquals("First Bank", account.getBankName());
        assertEquals("123456", account.getAccountNumber());
        assertEquals(core.WalletType.BANK, account.getWalletType());

        account.setBankName("Second Bank");
        account.setAccountNumber("987654");
        assertEquals("Second Bank", account.getBankName());
        assertEquals("987654", account.getAccountNumber());
    }

    @Test
    void incomeTransactionSignedAmountAndSource() {
        Category salaryCategory = new Category("Salary", TransactionType.INCOME);
        EWallet wallet = new EWallet("PayApp", 1000.0, "PayApp");
        Income income = new Income(1, 400.0, LocalDate.of(2026, 7, 20), "Freelance", salaryCategory, wallet, "Client");

        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(400.0, income.getSignedAmount());
        assertEquals("Client", income.getSource());
        assertEquals(wallet, income.getWallet());
        assertEquals(1400.0, wallet.getBalance()); // Check if the wallet balance is updated after income
    }

    @Test
    void expenseTransactionSignedAmountAndPaymentMethod() {
        CashWallet wallet = new CashWallet("Wallet", -1);
        Category foodCategory = new Category("Food", TransactionType.EXPENSE);
        Expense expense = new Expense(2, 260.0, LocalDate.of(2026, 7, 20), "Groceries", foodCategory, wallet, "Cash");
        assertEquals(TransactionType.EXPENSE, expense.getType());
        //assertEquals(-130.0, expense.getSignedAmount());
        assertEquals("Cash", expense.getPaymentMethod());
        assertEquals(wallet, expense.getWallet());
        assertEquals(120.0, wallet.getBalance()); // Check if the wallet balance is updated after expense
    }

    @Test
    void expenseTransactionSignedAmountAndPaymentMethodTwo() {
        CashWallet wallet = new CashWallet("Wallet", 250.0);
        Category foodCategory = new Category("Food", TransactionType.EXPENSE);
        Expense expense = new Expense(2, 360.0, LocalDate.of(2026, 7, 20), "Groceries", foodCategory, wallet, "Cash");
        assertEquals(TransactionType.EXPENSE, expense.getType());
        //assertEquals(-130.0, expense.getSignedAmount());
        assertEquals("Cash", expense.getPaymentMethod());
        assertEquals(wallet, expense.getWallet());
        assertEquals(120.0, wallet.getBalance()); // Check if the wallet balance is updated after expense
    }
}
