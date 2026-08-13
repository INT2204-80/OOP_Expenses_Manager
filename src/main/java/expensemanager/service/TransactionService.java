package expensemanager.service;

import java.time.LocalDate;
import java.util.List;

import core.storage.TransactionDAO;
import core.storage.WalletDAO;
import core.transaction.Expense;
import core.transaction.Income;
import core.transaction.RecurringExpense;
import core.transaction.Transaction;
import core.wallet.Wallet;

public class TransactionService {
    
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final WalletDAO walletDAO = new WalletDAO();

    /**
     * Lấy danh sách toàn bộ giao dịch của một ví
     */
    public List<Transaction> getTransactionsByWallet(Wallet wallet) {
        return transactionDAO.getTransactionsByWallet(wallet);
    }

    /**
     * Thêm giao dịch mới, tự động cập nhật số dư ví và lưu Database
     */
    public void addTransactionAndUpdateWallet(Transaction t, Wallet wallet) {
        wallet.addTransaction(t); // Hàm này bên trong Wallet có thể đã xử lý cộng/trừ số dư
        
        transactionDAO.saveTransaction(t, wallet.getId());
        walletDAO.updateBalance(wallet.getId(), wallet.getBalance());
    }

    /**
     * Them mot RecurringExpense moi VOI ngay khoi tao trong qua khu, kem backfill
     * (sinh them) cac giao dich Expense tuong ung so chu ky da qua (re.getPassedPeriods()).
     *
     * <p>Luu y: khoan tien cho ban than RecurringExpense (1 lan dau) da duoc tru vao
     * wallet ngay trong constructor cua Expense (lop cha). Ham nay chi can tru them
     * cho tung ky da qua (moi ky mot Expense rieng, danh dau "(Auto-generated)" giong
     * co che processRecurringExpenses() ben TransactionDAO khi load lai tu DB).
     *
     * <p>re.getPassedPeriods() phai duoc tinh san (goi re.nextDueDate() truoc do) boi
     * noi goi ham nay, vi day la noi hien Alert xac nhan voi nguoi dung truoc khi goi.
     */
    public void addRecurringExpenseWithBackfill(RecurringExpense re, Wallet wallet) {
        int passed = re.getPassedPeriods();

        wallet.addTransaction(re);
        transactionDAO.saveTransaction(re, wallet.getId());

        for (int i = 1; i <= passed; i++) {
            LocalDate generatedDate = re.getDate().plus(re.getPeriod().multipliedBy(i));
            Expense backfillExpense = new Expense(0, re.getAmount(), generatedDate,
                    re.getNote() + " (Auto-generated)", re.getCategory(), wallet, re.getPaymentMethod());
            // Constructor cua Expense da tu tru tien vao wallet
            wallet.addTransaction(backfillExpense);
            transactionDAO.saveTransaction(backfillExpense, wallet.getId());
        }

        walletDAO.updateBalance(wallet.getId(), wallet.getBalance());
    }

    /**
     * Cập nhật giao dịch (Sửa), tự động tính toán lại số dư chênh lệch và lưu Database
     */
    public void updateTransactionAndUpdateWallet(Transaction oldT, Transaction newT, Wallet wallet) {
        // Giữ lại số kỳ đã qua nếu là giao dịch lặp lại
        if (newT instanceof RecurringExpense && oldT instanceof RecurringExpense) {
            ((RecurringExpense) newT).setPassedPeriods(((RecurringExpense) oldT).getPassedPeriods());
        }

        // 1. Hoàn tác (Revert) số tiền của giao dịch cũ
        if (oldT instanceof Income) {
            wallet.withdraw(oldT.getAmount());
        } else {
            wallet.deposit(oldT.getAmount());
        }
        wallet.getTransactions().remove(oldT);
        
        // 2. Thêm giao dịch mới vào (Constructor của newT hoặc addTransaction sẽ cập nhật lại số dư)
        wallet.getTransactions().add(newT);

        // 3. Cập nhật xuống Database
        transactionDAO.updateTransaction(newT, wallet.getId());
        walletDAO.updateBalance(wallet.getId(), wallet.getBalance());
    }

    /**
     * Xóa giao dịch, tự động hoàn tiền lại vào ví và xóa khỏi Database
     */
    public void deleteTransactionAndUpdateWallet(Transaction t, Wallet wallet) {
        transactionDAO.deleteTransaction(t.getId());

        // Hoàn tác số tiền
        if (t instanceof Income) {
            wallet.withdraw(t.getAmount());
        } else {
            wallet.deposit(t.getAmount());
        }
        
        wallet.getTransactions().remove(t);
        walletDAO.updateBalance(wallet.getId(), wallet.getBalance());
    }
}