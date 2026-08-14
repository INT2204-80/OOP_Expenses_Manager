package core.storage;

import core.wallet.Wallet;
import java.util.List;

public interface IWalletDAO {
    List<Wallet> getAllWallets();
    void addWallet(Wallet wallet);
    void updateWallet(int walletId, String newName, double newBalance, String newCurrency);
    void updateBalance(int walletId, double newBalance);
    void updateBalance(java.sql.Connection conn, int walletId, double newBalance) throws java.sql.SQLException;
    void updateWallet(Wallet wallet);
    void deleteWallet(int walletId);
}
