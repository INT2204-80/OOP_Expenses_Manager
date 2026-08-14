package core.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import core.wallet.BankAccount;
import core.wallet.CashWallet;
import core.wallet.EWallet;
import core.wallet.Wallet;

public class WalletDAO implements IWalletDAO {

    public List<Wallet> getAllWallets() {
        List<Wallet> wallets = new ArrayList<>();
        String sql = "SELECT * FROM wallets";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double balance = rs.getDouble("balance");
                String type = rs.getString("wallet_type");
                String currency = rs.getString("currency");

                Wallet wallet = null;
                if ("CASH".equals(type)) {
                    wallet = new CashWallet(name, balance, currency);
                } else if ("BANK".equals(type)) {
                    String bankName = rs.getString("bank_name");
                    String accNum = rs.getString("account_number");
                    wallet = new BankAccount(name, balance, currency, bankName, accNum);
                } else if ("EWALLET".equals(type)) {
                    String provider = rs.getString("provider");
                    wallet = new EWallet(name, balance, currency, provider);
                }
                
                if (wallet != null) {
                    wallet.setId(id);
                    wallets.add(wallet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching wallets", e);
        }

        return wallets;
    }

    public void addWallet(Wallet wallet) {
        String sql = "INSERT INTO wallets (name, balance, wallet_type, bank_name, account_number, provider, currency) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, wallet.getName());
            pstmt.setDouble(2, wallet.getBalance());
            pstmt.setString(3, wallet.getWalletType().name());

            if (wallet instanceof BankAccount) {
                BankAccount ba = (BankAccount) wallet;
                pstmt.setString(4, ba.getBankName());
                pstmt.setString(5, ba.getAccountNumber());
                pstmt.setNull(6, Types.VARCHAR);
            } else if (wallet instanceof EWallet) {
                EWallet ew = (EWallet) wallet;
                pstmt.setNull(4, Types.VARCHAR);
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setString(6, ew.getProvider());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setNull(6, Types.VARCHAR);
            }
            pstmt.setString(7, wallet.getCurrency());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Database error adding wallet", e);
        }
    }
    public void updateWallet(int walletId, String newName, double newBalance, String newCurrency) {
        String sql = "UPDATE wallets SET name = ?, balance = ?, currency = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, newName);
            pstmt.setDouble(2, newBalance);
            pstmt.setString(3, newCurrency);
            pstmt.setInt(4, walletId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating wallet", e);
        }
    }

    public void updateBalance(int walletId, double newBalance) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            updateBalance(conn, walletId, newBalance);
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating wallet balance", e);
        }
    }

    public void updateBalance(Connection conn, int walletId, double newBalance) throws SQLException {
        String sql = "UPDATE wallets SET balance = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, walletId);
            pstmt.executeUpdate();
        }
    }

    public void updateWallet(Wallet wallet) {
        String sql = "UPDATE wallets SET name = ?, balance = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, wallet.getName());
            pstmt.setDouble(2, wallet.getBalance());
            pstmt.setInt(3, wallet.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating wallet", e);
        }
    }

    public void deleteWallet(int walletId) {
        // Also delete associated budgets and transactions to avoid orphans
        String deleteBudgetSql = "DELETE FROM budgets WHERE wallet_id = ?";
        String deleteTxSql = "DELETE FROM transactions WHERE wallet_id = ?";
        String deleteWalletSql = "DELETE FROM wallets WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt0 = conn.prepareStatement(deleteBudgetSql);
             PreparedStatement pstmt1 = conn.prepareStatement(deleteTxSql);
             PreparedStatement pstmt2 = conn.prepareStatement(deleteWalletSql)) {
             
            // Start transaction
            conn.setAutoCommit(false);
            
            pstmt0.setInt(1, walletId);
            pstmt0.executeUpdate();

            pstmt1.setInt(1, walletId);
            pstmt1.executeUpdate();
            
            pstmt2.setInt(1, walletId);
            pstmt2.executeUpdate();
            
            conn.commit();
            conn.setAutoCommit(true);
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error deleting wallet", e);
        }
    }
}
