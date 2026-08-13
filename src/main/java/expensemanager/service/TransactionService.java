package expensemanager.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import core.storage.ITransactionDAO;
import core.storage.IWalletDAO;
import core.transaction.Expense;
import core.transaction.Income;
import core.transaction.RecurringExpense;
import core.transaction.Transaction;
import core.wallet.Wallet;

public class TransactionService {
    
    private final ITransactionDAO transactionDAO;
    private final IWalletDAO walletDAO;

    public TransactionService(ITransactionDAO transactionDAO, IWalletDAO walletDAO) {
        this.transactionDAO = transactionDAO;
        this.walletDAO = walletDAO;
    }

    /**
     * Lấy danh sách toàn bộ giao dịch của một ví
     */
    public List<Transaction> getTransactionsByWallet(Wallet wallet) {
        List<Transaction> transactions = transactionDAO.getTransactionsByWallet(wallet);
        processRecurringExpenses(transactions, wallet.getId());
        return transactions;
    }

    private void processRecurringExpenses(List<Transaction> transactions, int walletId) {
        List<Transaction> newExpenses = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t instanceof RecurringExpense) {
                RecurringExpense re = (RecurringExpense) t;
                int oldPassed = re.getPassedPeriods();
                re.nextDueDate(); // updates passedPeriods internally
                int newPassed = re.getPassedPeriods();
                
                if (newPassed > oldPassed) {
                    for (int i = oldPassed + 1; i <= newPassed; i++) {
                        LocalDate generatedDate = re.getDate().plus(re.getPeriod().multipliedBy(i));
                        Expense newExpense = new Expense(0, re.getAmount(), generatedDate, 
                            re.getNote() + " (Auto-generated)", re.getCategory(), re.getWallet(), re.getPaymentMethod());
                        newExpenses.add(newExpense);
                    }
                    // Update the RecurringExpense in DB
                    transactionDAO.updateTransaction(re, walletId);
                }
            }
        }
        
        for (Transaction newExp : newExpenses) {
            transactionDAO.saveTransaction(newExp, walletId);
            transactions.add(newExp);
        }
    }

    /**
     * Thêm giao dịch mới, tự động cập nhật số dư ví và lưu Database
     */
    public void addTransactionAndUpdateWallet(Transaction t, Wallet wallet) {
        wallet.addTransaction(t); 
        transactionDAO.saveTransaction(t, wallet.getId());
        walletDAO.updateBalance(wallet.getId(), wallet.getBalance());
    }

    /**
     * Thêm một RecurringExpense với backfill
     */
    public void addRecurringExpenseWithBackfill(RecurringExpense re, Wallet wallet) {
        int passed = re.getPassedPeriods();

        wallet.addTransaction(re);
        transactionDAO.saveTransaction(re, wallet.getId());

        for (int i = 1; i <= passed; i++) {
            LocalDate generatedDate = re.getDate().plus(re.getPeriod().multipliedBy(i));
            Expense backfillExpense = new Expense(0, re.getAmount(), generatedDate,
                    re.getNote() + " (Auto-generated)", re.getCategory(), wallet, re.getPaymentMethod());
            wallet.addTransaction(backfillExpense);
            transactionDAO.saveTransaction(backfillExpense, wallet.getId());
        }

        walletDAO.updateBalance(wallet.getId(), wallet.getBalance());
    }

    /**
     * Cập nhật giao dịch (Sửa), tự động tính toán lại số dư chênh lệch và lưu Database
     */
    public void updateTransactionAndUpdateWallet(Transaction oldT, Transaction newT, Wallet wallet) {
        if (newT instanceof RecurringExpense && oldT instanceof RecurringExpense) {
            ((RecurringExpense) newT).setPassedPeriods(((RecurringExpense) oldT).getPassedPeriods());
        }

        if (oldT instanceof Income) {
            wallet.withdraw(oldT.getAmount());
        } else {
            wallet.deposit(oldT.getAmount());
        }
        wallet.getTransactions().remove(oldT);
        
        wallet.getTransactions().add(newT);

        transactionDAO.updateTransaction(newT, wallet.getId());
        walletDAO.updateBalance(wallet.getId(), wallet.getBalance());
    }

    /**
     * Xóa giao dịch, tự động hoàn tiền lại vào ví và xóa khỏi Database
     */
    public void deleteTransactionAndUpdateWallet(Transaction t, Wallet wallet) {
        transactionDAO.deleteTransaction(t.getId());

        if (t instanceof Income) {
            wallet.withdraw(t.getAmount());
        } else {
            wallet.deposit(t.getAmount());
        }
        
        wallet.getTransactions().remove(t);
        walletDAO.updateBalance(wallet.getId(), wallet.getBalance());
    }
}