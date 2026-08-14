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
        processRecurringExpenses(transactions, wallet);
        return transactions;
    }

    private void processRecurringExpenses(List<Transaction> transactions, Wallet wallet) {
        List<Transaction> newExpenses = new ArrayList<>();
        boolean hasUpdates = false;

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
                            re.getNote() + " (Auto-generated)", re.getCategory(), wallet, re.getPaymentMethod());
                        newExpenses.add(newExpense);
                        applyToWallet(newExpense, wallet);
                    }
                    // Update the RecurringExpense in DB
                    transactionDAO.updateTransaction(re, wallet.getId());
                    hasUpdates = true;
                }
            }
        }
        
        for (Transaction newExp : newExpenses) {
            transactionDAO.saveTransaction(newExp, wallet.getId());
            transactions.add(newExp);
        }

        if (hasUpdates) {
            walletDAO.updateBalance(wallet.getId(), wallet.getBalance());
        }
    }

    private void applyToWallet(Transaction t, Wallet wallet) {
        if (t instanceof Income) {
            wallet.deposit(t.getAmount());
        } else {
            wallet.withdraw(t.getAmount());
        }
    }

    private void revertFromWallet(Transaction t, Wallet wallet) {
        if (t instanceof Income) {
            wallet.withdraw(t.getAmount());
        } else {
            wallet.deposit(t.getAmount());
        }
    }

    /**
     * Thêm giao dịch mới, tự động cập nhật số dư ví và lưu Database
     */
    public void addTransactionAndUpdateWallet(Transaction t, Wallet wallet) {
        applyToWallet(t, wallet);
        wallet.addTransaction(t); 
        transactionDAO.saveTransaction(t, wallet.getId());
        walletDAO.updateBalance(wallet.getId(), wallet.getBalance());
    }

    /**
     * Thêm một RecurringExpense với backfill
     */
    public void addRecurringExpenseWithBackfill(RecurringExpense re, Wallet wallet) {
        int passed = re.getPassedPeriods();

        applyToWallet(re, wallet);
        wallet.addTransaction(re);
        transactionDAO.saveTransaction(re, wallet.getId());

        for (int i = 1; i <= passed; i++) {
            LocalDate generatedDate = re.getDate().plus(re.getPeriod().multipliedBy(i));
            Expense backfillExpense = new Expense(0, re.getAmount(), generatedDate,
                    re.getNote() + " (Auto-generated)", re.getCategory(), wallet, re.getPaymentMethod());
            applyToWallet(backfillExpense, wallet);
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

        revertFromWallet(oldT, wallet);
        wallet.getTransactions().remove(oldT);
        
        applyToWallet(newT, wallet);
        wallet.getTransactions().add(newT);

        transactionDAO.updateTransaction(newT, wallet.getId());
        walletDAO.updateBalance(wallet.getId(), wallet.getBalance());
    }

    /**
     * Xóa giao dịch, tự động hoàn tiền lại vào ví và xóa khỏi Database
     */
    public void deleteTransactionAndUpdateWallet(Transaction t, Wallet wallet) {
        transactionDAO.deleteTransaction(t.getId());

        revertFromWallet(t, wallet);
        
        wallet.getTransactions().remove(t);
        walletDAO.updateBalance(wallet.getId(), wallet.getBalance());
    }
}