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

public class TransactionDAO implements ITransactionDAO {
    
    private ICategoryDAO categoryDAO;

    public TransactionDAO() {
        this.categoryDAO = new CategoryDAO(); // In real DI, this would be injected
    }
    
    public TransactionDAO(ICategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    @Override
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

    @Override
    public void saveTransaction(Transaction t, int walletId) {
        String insertSql = "INSERT INTO transactions (amount, date, note, category_id, wallet_id, transaction_type, source, payment_method, is_recurring, recurring_period, passed_periods, recurring_end_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
             
            int categoryId = categoryDAO.getOrCreateCategoryId(
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
            throw new RuntimeException("Database error saving transaction", e);
        }
    }

    @Override
    public void deleteTransaction(int transactionId) {
        String deleteSql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setInt(1, transactionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error deleting transaction", e);
        }
    }

    @Override
    public void updateTransaction(Transaction t, int walletId) {
        String updateSql = "UPDATE transactions SET amount = ?, date = ?, note = ?, category_id = ?, transaction_type = ?, source = ?, payment_method = ?, is_recurring = ?, recurring_period = ?, passed_periods = ?, recurring_end_date = ?, wallet_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
             
            int categoryId = categoryDAO.getOrCreateCategoryId(
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
            pstmt.setInt(12, walletId);
            pstmt.setInt(13, t.getId());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating transaction", e);
        }
    }

    @Override
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
            throw new RuntimeException("Database error loading transactions", e);
        }
        
        wallet.setBalance(originalBalance); // Restore original balance to prevent double counting
        
        return transactions;
    }
}
