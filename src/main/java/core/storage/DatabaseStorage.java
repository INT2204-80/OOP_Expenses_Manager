package core.storage;

import core.Category;
import core.TransactionType;
import core.transaction.Expense;
import core.transaction.Income;
import core.transaction.Transaction;
import core.wallet.CashWallet;
import core.wallet.Wallet;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseStorage implements Storage {

    @Override
    public void save(List<Transaction> transactions, String path) throws IOException {
        String insertSql = "INSERT INTO transactions (amount, date, note, transaction_type, source, payment_method) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
             
            for (Transaction t : transactions) {
                pstmt.setDouble(1, t.getAmount());
                pstmt.setDate(2, Date.valueOf(t.getDate()));
                pstmt.setString(3, t.getNote());
                pstmt.setString(4, t.getType().name());
                
                if (t instanceof Income) {
                    pstmt.setString(5, ((Income) t).getSource());
                    pstmt.setString(6, null);
                } else if (t instanceof Expense) {
                    pstmt.setString(5, null);
                    pstmt.setString(6, ((Expense) t).getPaymentMethod());
                } else {
                    pstmt.setString(5, null);
                    pstmt.setString(6, null);
                }
                
                // Note: Simplified logic. A full implementation would also insert/lookup 
                // Categories and Wallets to maintain referential integrity.
                
                pstmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            throw new IOException("Database save error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> load(String path) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        String selectSql = "SELECT * FROM transactions";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {
             
            while (rs.next()) {
                int id = rs.getInt("id");
                double amount = rs.getDouble("amount");
                LocalDate date = rs.getDate("date").toLocalDate();
                String note = rs.getString("note");
                String typeStr = rs.getString("transaction_type");
                
                // Note: We use dummy Category and Wallet for simplification here.
                // A full implementation would load them from the DB based on IDs.
                Category cat = new Category("Imported", TransactionType.valueOf(typeStr));
                Wallet wallet = new CashWallet("Main Wallet", 0);
                
                Transaction t;
                if ("INCOME".equals(typeStr)) {
                    String source = rs.getString("source");
                    t = new Income(id, amount, date, note, cat, wallet, source != null ? source : "Unknown");
                } else {
                    String method = rs.getString("payment_method");
                    t = new Expense(id, amount, date, note, cat, wallet, method != null ? method : "Cash");
                }
                transactions.add(t);
            }
            
        } catch (SQLException e) {
            throw new IOException("Database load error: " + e.getMessage(), e);
        }
        
        return transactions;
    }
}
