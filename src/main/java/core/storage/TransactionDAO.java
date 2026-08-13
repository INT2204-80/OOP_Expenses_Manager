package core.storage;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import core.Category;
import core.TransactionType;
import core.transaction.Expense;
import core.transaction.Income;
import core.transaction.Transaction;
import core.wallet.Wallet;

public class TransactionDAO {
    
    static {
        ensureSchemaColumns();
    }

    private static void ensureSchemaColumns() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            try { stmt.executeUpdate("ALTER TABLE transactions ADD COLUMN is_recurring BOOLEAN DEFAULT FALSE"); } catch (SQLException e) {}
            try { stmt.executeUpdate("ALTER TABLE transactions ADD COLUMN recurring_period VARCHAR(50)"); } catch (SQLException e) {}
            try { stmt.executeUpdate("ALTER TABLE transactions ADD COLUMN passed_periods INT DEFAULT 0"); } catch (SQLException e) {}
            try { stmt.executeUpdate("ALTER TABLE transactions ADD COLUMN recurring_end_date DATE"); } catch (SQLException e) {}
            try { stmt.executeUpdate("ALTER TABLE categories ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE"); } catch (SQLException e) {}
            try { stmt.executeUpdate("ALTER TABLE categories ADD COLUMN icon VARCHAR(255)"); } catch (SQLException e) {}
            try { stmt.executeUpdate("ALTER TABLE categories ADD COLUMN color VARCHAR(255)"); } catch (SQLException e) {}
        } catch (SQLException e) {
            System.err.println("Warning: could not ensure database schema columns: " + e.getMessage());
        }
    }
    
    public int getOrCreateCategoryId(String name, String type) throws SQLException {
        return getOrCreateCategoryId(name, type, null, null);
    }

    public int getOrCreateCategoryId(String name, String type, String icon, String color) throws SQLException {
        String selectSql = "SELECT id FROM categories WHERE name = ? AND transaction_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    if (icon != null || color != null) {
                        String updateSql = "UPDATE categories SET icon = ?, color = ?, is_deleted = FALSE WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, icon != null ? icon : "");
                            updateStmt.setString(2, color != null ? color : "");
                            updateStmt.setInt(3, id);
                            updateStmt.executeUpdate();
                        }
                    }
                    return id;
                }
            }
        }
        
        String insertSql = "INSERT INTO categories (name, transaction_type, icon, color) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.setString(3, icon);
            pstmt.setString(4, color);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public List<Category> getAllCategories() {
        ensureSchemaColumns();

        List<Category> categories = new ArrayList<>();
        String selectSql = "SELECT * FROM categories WHERE is_deleted = FALSE OR is_deleted IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                String typeStr = rs.getString("transaction_type");
                String icon = rs.getString("icon");
                String color = rs.getString("color");
                categories.add(new Category(name, TransactionType.valueOf(typeStr), icon, color));
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
                        String icon = rs.getString("icon");
                        String color = rs.getString("color");
                        categories.add(new Category(name, TransactionType.valueOf(typeStr), icon, color));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error seeding categories: " + e.getMessage());
            }
        }

        return categories;
    }

    public int getCategoryId(String name, String type) throws SQLException {
        String query = "SELECT id FROM categories WHERE name = ? AND transaction_type = ? AND (is_deleted = FALSE OR is_deleted IS NULL)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    public void mergeCategories(List<Category> sources, Category target) throws SQLException {
        int targetId = getCategoryId(target.getName(), target.getType().name());
        if (targetId == -1) return;

        String updateSql = "UPDATE transactions SET category_id = ? WHERE category_id = ?";
        String deleteSql = "UPDATE categories SET is_deleted = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            
            conn.setAutoCommit(false);
            try {
                for (Category src : sources) {
                    int srcId = getCategoryId(src.getName(), src.getType().name());
                    if (srcId != -1 && srcId != targetId) {
                        updateStmt.setInt(1, targetId);
                        updateStmt.setInt(2, srcId);
                        updateStmt.executeUpdate();

                        deleteStmt.setInt(1, srcId);
                        deleteStmt.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void updateCategory(String oldName, String oldType, String newName, String newType, String newIcon, String newColor) throws SQLException {
        int id = getCategoryId(oldName, oldType);
        if (id == -1) return;
        
        String sql = "UPDATE categories SET name = ?, transaction_type = ?, icon = ?, color = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, newType.toUpperCase());
            pstmt.setString(3, newIcon);
            pstmt.setString(4, newColor);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
        }
    }

    public double getTotalAmountForPeriod(String type, java.time.LocalDate startDate, java.time.LocalDate endDate) throws SQLException {
        String sql = "SELECT SUM(amount) as total FROM transactions WHERE transaction_type = ? AND date >= ? AND date <= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
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
        String insertSql = "INSERT INTO transactions (amount, date, note, category_id, wallet_id, transaction_type, source, payment_method, is_recurring, recurring_period, passed_periods, recurring_end_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
             
            int categoryId = getOrCreateCategoryId(
                    t.getCategory().getName(),
                    t.getType().name(),
                    t.getCategory().getIcon(),
                    t.getCategory().getColor());
             
            pstmt.setDouble(1, t.getAmount());
            pstmt.setDate(2, Date.valueOf(t.getDate()));
            pstmt.setString(3, t.getNote());
            if (categoryId != -1) pstmt.setInt(4, categoryId); else pstmt.setNull(4, Types.INTEGER);
            pstmt.setInt(5, walletId);
            pstmt.setString(6, t.getType().name());
            
            if (t instanceof Income) {
                pstmt.setString(7, ((Income) t).getSource());
                pstmt.setNull(8, Types.VARCHAR);
                pstmt.setBoolean(9, false);
                pstmt.setNull(10, Types.VARCHAR);
                pstmt.setInt(11, 0);
                pstmt.setNull(12, Types.DATE);
            } else if (t instanceof core.transaction.RecurringExpense) {
                core.transaction.RecurringExpense re = (core.transaction.RecurringExpense) t;
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setString(8, re.getPaymentMethod());
                pstmt.setBoolean(9, true);
                pstmt.setString(10, re.getPeriod().toString()); // e.g. P1M
                pstmt.setInt(11, re.getPassedPeriods());
                if (re.getEndDate() != null) {
                    pstmt.setDate(12, Date.valueOf(re.getEndDate()));
                } else {
                    pstmt.setNull(12, Types.DATE);
                }
            } else if (t instanceof Expense) {
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setString(8, ((Expense) t).getPaymentMethod());
                pstmt.setBoolean(9, false);
                pstmt.setNull(10, Types.VARCHAR);
                pstmt.setInt(11, 0);
                pstmt.setNull(12, Types.DATE);
            } else {
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.VARCHAR);
                pstmt.setBoolean(9, false);
                pstmt.setNull(10, Types.VARCHAR);
                pstmt.setInt(11, 0);
                pstmt.setNull(12, Types.DATE);
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
        String updateSql = "UPDATE transactions SET amount = ?, date = ?, note = ?, category_id = ?, transaction_type = ?, source = ?, payment_method = ?, is_recurring = ?, recurring_period = ?, passed_periods = ?, recurring_end_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
             
            int categoryId = getOrCreateCategoryId(
                    t.getCategory().getName(),
                    t.getType().name(),
                    t.getCategory().getIcon(),
                    t.getCategory().getColor());
             
            pstmt.setDouble(1, t.getAmount());
            pstmt.setDate(2, Date.valueOf(t.getDate()));
            pstmt.setString(3, t.getNote());
            if (categoryId != -1) pstmt.setInt(4, categoryId); else pstmt.setNull(4, Types.INTEGER);
            pstmt.setString(5, t.getType().name());
            
            if (t instanceof Income) {
                pstmt.setString(6, ((Income) t).getSource());
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setBoolean(8, false);
                pstmt.setNull(9, Types.VARCHAR);
                pstmt.setInt(10, 0);
                pstmt.setNull(11, Types.DATE);
            } else if (t instanceof core.transaction.RecurringExpense) {
                core.transaction.RecurringExpense re = (core.transaction.RecurringExpense) t;
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setString(7, re.getPaymentMethod());
                pstmt.setBoolean(8, true);
                pstmt.setString(9, re.getPeriod().toString());
                pstmt.setInt(10, re.getPassedPeriods());
                if (re.getEndDate() != null) {
                    pstmt.setDate(11, Date.valueOf(re.getEndDate()));
                } else {
                    pstmt.setNull(11, Types.DATE);
                }
            } else if (t instanceof Expense) {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setString(7, ((Expense) t).getPaymentMethod());
                pstmt.setBoolean(8, false);
                pstmt.setNull(9, Types.VARCHAR);
                pstmt.setInt(10, 0);
                pstmt.setNull(11, Types.DATE);
            } else {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setBoolean(8, false);
                pstmt.setNull(9, Types.VARCHAR);
                pstmt.setInt(10, 0);
                pstmt.setNull(11, Types.DATE);
            }
            pstmt.setInt(12, t.getId());
            
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
        String selectSql = "SELECT t.*, c.name AS category_name, c.icon AS category_icon, c.color AS category_color FROM transactions t LEFT JOIN categories c ON t.category_id = c.id WHERE t.wallet_id = ?";
        
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
                    String icon = rs.getString("category_icon");
                    String color = rs.getString("category_color");
                    if (catName == null) catName = "Unknown";
                    if (icon == null) icon = "";
                    if (color == null) color = "";
                    
                    Category cat = new Category(catName, TransactionType.valueOf(typeStr), icon, color);
                    
                    Transaction t;
                    if ("INCOME".equals(typeStr)) {
                        String source = rs.getString("source");
                        t = new Income(id, amount, date, note, cat, wallet, source != null ? source : "Unknown");
                    } else {
                        String method = rs.getString("payment_method");
                        method = method != null ? method : "Cash";
                        boolean isRecurring = rs.getBoolean("is_recurring");
                        if (isRecurring) {
                            String p = rs.getString("recurring_period");
                            java.time.Period period = p != null ? java.time.Period.parse(p) : java.time.Period.ofMonths(1);
                            java.sql.Date endDateDb = rs.getDate("recurring_end_date");
                            java.time.LocalDate endDate = (endDateDb != null) ? endDateDb.toLocalDate() : null;
                            core.transaction.RecurringExpense re = new core.transaction.RecurringExpense(id, amount, date, note, cat, wallet, method, period, endDate);
                            int passedPeriods = rs.getInt("passed_periods");
                            re.setPassedPeriods(passedPeriods);
                            t = re;
                        } else {
                            t = new Expense(id, amount, date, note, cat, wallet, method);
                        }
                    }
                    transactions.add(t);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
        }
        
        processRecurringExpenses(transactions, wallet.getId());
        
        wallet.setBalance(originalBalance); // Restore original balance to prevent double counting
        
        return transactions;
    }

    private void processRecurringExpenses(List<Transaction> transactions, int walletId) {
        List<Transaction> newExpenses = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t instanceof core.transaction.RecurringExpense) {
                core.transaction.RecurringExpense re = (core.transaction.RecurringExpense) t;
                int oldPassed = re.getPassedPeriods();
                re.nextDueDate(); // updates passedPeriods internally (khong vuot qua endDate neu co)
                int newPassed = re.getPassedPeriods();
                
                if (newPassed > oldPassed) {
                    for (int i = oldPassed + 1; i <= newPassed; i++) {
                        java.time.LocalDate generatedDate = re.getDate().plus(re.getPeriod().multipliedBy(i));
                        Expense newExpense = new Expense(0, re.getAmount(), generatedDate, 
                            re.getNote() + " (Auto-generated)", re.getCategory(), re.getWallet(), re.getPaymentMethod());
                        newExpenses.add(newExpense);
                    }
                    // Update the RecurringExpense in DB
                    updateTransaction(re, walletId);
                }
            }
        }
        
        for (Transaction newExp : newExpenses) {
            saveTransaction(newExp, walletId);
            transactions.add(newExp);
        }
    }
}
