package core.storage;

import core.Category;
import core.TransactionType;
import core.transaction.Expense;
import core.transaction.Income;
import core.transaction.Transaction;
import core.wallet.Wallet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    
    public int getOrCreateCategoryId(String name, String type) throws SQLException {
        String selectSql = "SELECT id FROM categories WHERE name = ? AND transaction_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        
        String insertSql = "INSERT INTO categories (name, transaction_type) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public List<Category> getAllCategories() {
        // Ensure is_deleted column exists
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE categories ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE");
        } catch (SQLException e) {
            // Column already exists, ignore
        }

        List<Category> categories = new ArrayList<>();
        String selectSql = "SELECT * FROM categories WHERE is_deleted = FALSE OR is_deleted IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                String typeStr = rs.getString("transaction_type");
                categories.add(new Category(name, TransactionType.valueOf(typeStr)));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
        }
        
        // Seed default categories if none exist and no error occurred
        if (categories.isEmpty()) {
            try {
                getOrCreateCategoryId("Lương", "INCOME");
                getOrCreateCategoryId("Tiền thưởng", "INCOME");
                getOrCreateCategoryId("Ăn uống", "EXPENSE");
                getOrCreateCategoryId("Mua sắm", "EXPENSE");
                getOrCreateCategoryId("Đi lại", "EXPENSE");
                getOrCreateCategoryId("Giải trí", "EXPENSE");
                
                // Fetch again after seeding
                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(selectSql)) {
                    while (rs.next()) {
                        String name = rs.getString("name");
                        String typeStr = rs.getString("transaction_type");
                        categories.add(new Category(name, TransactionType.valueOf(typeStr)));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error seeding categories: " + e.getMessage());
            }
        }

        return categories;
    }

    public void softDeleteCategory(String name, String type) throws SQLException {
        String updateSql = "UPDATE categories SET is_deleted = TRUE WHERE name = ? AND transaction_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.executeUpdate();
        }
    }

    public void saveTransaction(Transaction t, int walletId) {
        String insertSql = "INSERT INTO transactions (amount, date, note, category_id, wallet_id, transaction_type, source, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
             
            int categoryId = getOrCreateCategoryId(t.getCategory().getName(), t.getType().name());
             
            pstmt.setDouble(1, t.getAmount());
            pstmt.setDate(2, Date.valueOf(t.getDate()));
            pstmt.setString(3, t.getNote());
            if (categoryId != -1) pstmt.setInt(4, categoryId); else pstmt.setNull(4, Types.INTEGER);
            pstmt.setInt(5, walletId);
            pstmt.setString(6, t.getType().name());
            
            if (t instanceof Income) {
                pstmt.setString(7, ((Income) t).getSource());
                pstmt.setNull(8, Types.VARCHAR);
            } else if (t instanceof Expense) {
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setString(8, ((Expense) t).getPaymentMethod());
            } else {
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.VARCHAR);
            }
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    t.setId(generatedKeys.getInt(1));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error saving transaction: " + e.getMessage());
        }
    }

    public void deleteTransaction(int transactionId) {
        String deleteSql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setInt(1, transactionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
        }
    }

    public void updateTransaction(Transaction t, int walletId) {
        String updateSql = "UPDATE transactions SET amount = ?, date = ?, note = ?, category_id = ?, transaction_type = ?, source = ?, payment_method = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
             
            int categoryId = getOrCreateCategoryId(t.getCategory().getName(), t.getType().name());
             
            pstmt.setDouble(1, t.getAmount());
            pstmt.setDate(2, Date.valueOf(t.getDate()));
            pstmt.setString(3, t.getNote());
            if (categoryId != -1) pstmt.setInt(4, categoryId); else pstmt.setNull(4, Types.INTEGER);
            pstmt.setString(5, t.getType().name());
            
            if (t instanceof Income) {
                pstmt.setString(6, ((Income) t).getSource());
                pstmt.setNull(7, Types.VARCHAR);
            } else if (t instanceof Expense) {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setString(7, ((Expense) t).getPaymentMethod());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setNull(7, Types.VARCHAR);
            }
            pstmt.setInt(8, t.getId());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    t.setId(generatedKeys.getInt(1));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
        }
    }

    public List<Transaction> getTransactionsByWallet(Wallet wallet) {
        List<Transaction> transactions = new ArrayList<>();
        String selectSql = "SELECT t.*, c.name AS category_name FROM transactions t LEFT JOIN categories c ON t.category_id = c.id WHERE t.wallet_id = ?";
        
        double originalBalance = wallet.getBalance(); // Save original balance
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
             
            pstmt.setInt(1, wallet.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    double amount = rs.getDouble("amount");
                    java.time.LocalDate date = rs.getDate("date").toLocalDate();
                    String note = rs.getString("note");
                    String typeStr = rs.getString("transaction_type");
                    String catName = rs.getString("category_name");
                    if (catName == null) catName = "Unknown";
                    
                    Category cat = new Category(catName, TransactionType.valueOf(typeStr));
                    
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
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
        }
        
        wallet.setBalance(originalBalance); // Restore original balance to prevent double counting
        
        return transactions;
    }
}
