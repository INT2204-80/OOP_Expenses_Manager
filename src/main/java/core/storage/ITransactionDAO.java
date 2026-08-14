package core.storage;

import core.transaction.Transaction;
import core.wallet.Wallet;
import java.sql.SQLException;
import java.util.List;

public interface ITransactionDAO {
    void saveTransaction(Transaction t, int walletId);
    void saveTransaction(java.sql.Connection conn, Transaction t, int walletId) throws SQLException;
    void deleteTransaction(int transactionId);
    void deleteTransaction(java.sql.Connection conn, int transactionId) throws SQLException;
    void updateTransaction(Transaction t, int walletId);
    void updateTransaction(java.sql.Connection conn, Transaction t, int walletId) throws SQLException;
    List<Transaction> getTransactionsByWallet(Wallet wallet);
    double getTotalAmountForPeriod(String type, java.time.LocalDate startDate, java.time.LocalDate endDate) throws SQLException;
}
