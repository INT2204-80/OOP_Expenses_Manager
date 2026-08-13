package core.storage;

import core.transaction.Transaction;
import core.wallet.Wallet;
import java.sql.SQLException;
import java.util.List;

public interface ITransactionDAO {
    void saveTransaction(Transaction t, int walletId);
    void deleteTransaction(int transactionId);
    void updateTransaction(Transaction t, int walletId);
    List<Transaction> getTransactionsByWallet(Wallet wallet);
    double getTotalAmountForPeriod(String type, java.time.LocalDate startDate, java.time.LocalDate endDate) throws SQLException;
}
