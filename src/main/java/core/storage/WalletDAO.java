package core.storage;

import core.WalletType;
import core.wallet.BankAccount;
import core.wallet.CashWallet;
import core.wallet.EWallet;
import core.wallet.Wallet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WalletDAO {

    public List<Wallet> getAllWallets() {
        List<Wallet> wallets = new ArrayList<>();
        String sql = "SELECT * FROM wallets";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("name");
                double balance = rs.getDouble("balance");
                String type = rs.getString("wallet_type");

                if ("CASH".equals(type)) {
                    wallets.add(new CashWallet(name, balance));
                } else if ("BANK".equals(type)) {
                    String bankName = rs.getString("bank_name");
                    String accNum = rs.getString("account_number");
                    wallets.add(new BankAccount(name, balance, bankName, accNum));
                } else if ("EWALLET".equals(type)) {
                    String provider = rs.getString("provider");
                    wallets.add(new EWallet(name, balance, provider));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching wallets: " + e.getMessage());
        }

        return wallets;
    }

    public void addWallet(Wallet wallet) {
        String sql = "INSERT INTO wallets (name, balance, wallet_type, bank_name, account_number, provider) VALUES (?, ?, ?, ?, ?, ?)";

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

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding wallet: " + e.getMessage());
        }
    }
}
