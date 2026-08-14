package expensemanager.service;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import core.storage.DatabaseConnection;
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
                    hasUpdates = true;
                }
            }
        }
        
        if (!hasUpdates && newExpenses.isEmpty()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Transaction t : transactions) {
                    if (t instanceof RecurringExpense) {
                        RecurringExpense re = (RecurringExpense) t;
                        transactionDAO.updateTransaction(conn, re, wallet.getId());
                    }
                }
                
                for (Transaction newExp : newExpenses) {
                    transactionDAO.saveTransaction(conn, newExp, wallet.getId());
                    transactions.add(newExp);
                }

                walletDAO.updateBalance(conn, wallet.getId(), wallet.getBalance());
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Transaction failed", e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Database connection error", e);
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
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transactionDAO.saveTransaction(conn, t, wallet.getId());
                walletDAO.updateBalance(conn, wallet.getId(), wallet.getBalance());
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                revertFromWallet(t, wallet);
                wallet.getTransactions().remove(t);
                throw new RuntimeException("Transaction failed", e);
            }
        } catch (Exception e) {
            revertFromWallet(t, wallet);
            wallet.getTransactions().remove(t);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Thêm một RecurringExpense với backfill
     */
    public void addRecurringExpenseWithBackfill(RecurringExpense re, Wallet wallet) {
        int passed = re.getPassedPeriods();

        applyToWallet(re, wallet);
        wallet.addTransaction(re);

        List<Expense> backfillExpenses = new ArrayList<>();
        for (int i = 1; i <= passed; i++) {
            LocalDate generatedDate = re.getDate().plus(re.getPeriod().multipliedBy(i));
            Expense backfillExpense = new Expense(0, re.getAmount(), generatedDate,
                    re.getNote() + " (Auto-generated)", re.getCategory(), wallet, re.getPaymentMethod());
            applyToWallet(backfillExpense, wallet);
            wallet.addTransaction(backfillExpense);
            backfillExpenses.add(backfillExpense);
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transactionDAO.saveTransaction(conn, re, wallet.getId());
                for (Expense ex : backfillExpenses) {
                    transactionDAO.saveTransaction(conn, ex, wallet.getId());
                }
                walletDAO.updateBalance(conn, wallet.getId(), wallet.getBalance());
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                // revert memory
                for (Expense ex : backfillExpenses) {
                    revertFromWallet(ex, wallet);
                    wallet.getTransactions().remove(ex);
                }
                revertFromWallet(re, wallet);
                wallet.getTransactions().remove(re);
                throw new RuntimeException("Transaction failed", e);
            }
        } catch (Exception e) {
            for (Expense ex : backfillExpenses) {
                revertFromWallet(ex, wallet);
                wallet.getTransactions().remove(ex);
            }
            revertFromWallet(re, wallet);
            wallet.getTransactions().remove(re);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Cập nhật giao dịch (Sửa), tự động tính toán lại số dư chênh lệch và lưu Database
     */
    public void updateTransactionAndUpdateWallet(Transaction oldT, Transaction newT, Wallet wallet) {
        if (newT instanceof RecurringExpense && oldT instanceof RecurringExpense) {
            ((RecurringExpense) newT).setPassedPeriods(((RecurringExpense) oldT).getPassedPeriods());
        }

        revertFromWallet(oldT, wallet);
        int oldIndex = wallet.getTransactions().indexOf(oldT);
        wallet.getTransactions().remove(oldT);
        
        applyToWallet(newT, wallet);
        wallet.getTransactions().add(newT);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transactionDAO.updateTransaction(conn, newT, wallet.getId());
                walletDAO.updateBalance(conn, wallet.getId(), wallet.getBalance());
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                // revert memory
                revertFromWallet(newT, wallet);
                wallet.getTransactions().remove(newT);
                applyToWallet(oldT, wallet);
                if (oldIndex >= 0 && oldIndex <= wallet.getTransactions().size()) {
                    wallet.getTransactions().add(oldIndex, oldT);
                } else {
                    wallet.getTransactions().add(oldT);
                }
                throw new RuntimeException("Transaction failed", e);
            }
        } catch (Exception e) {
            revertFromWallet(newT, wallet);
            wallet.getTransactions().remove(newT);
            applyToWallet(oldT, wallet);
            if (oldIndex >= 0 && oldIndex <= wallet.getTransactions().size()) {
                wallet.getTransactions().add(oldIndex, oldT);
            } else {
                wallet.getTransactions().add(oldT);
            }
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Xóa giao dịch, tự động hoàn tiền lại vào ví và xóa khỏi Database
     */
    public void deleteTransactionAndUpdateWallet(Transaction t, Wallet wallet) {
        revertFromWallet(t, wallet);
        int oldIndex = wallet.getTransactions().indexOf(t);
        wallet.getTransactions().remove(t);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transactionDAO.deleteTransaction(conn, t.getId());
                walletDAO.updateBalance(conn, wallet.getId(), wallet.getBalance());
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                // revert memory
                applyToWallet(t, wallet);
                if (oldIndex >= 0 && oldIndex <= wallet.getTransactions().size()) {
                    wallet.getTransactions().add(oldIndex, t);
                } else {
                    wallet.getTransactions().add(t);
                }
                throw new RuntimeException("Transaction failed", e);
            }
        } catch (Exception e) {
            applyToWallet(t, wallet);
            if (oldIndex >= 0 && oldIndex <= wallet.getTransactions().size()) {
                wallet.getTransactions().add(oldIndex, t);
            } else {
                wallet.getTransactions().add(t);
            }
            throw new RuntimeException("Database error", e);
        }
    }
}