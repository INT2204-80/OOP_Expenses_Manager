package core.storage;

import core.Budget;
import core.Category;
import core.Period;
import core.TransactionType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO implements IBudgetDAO {

    static {
        // Automatically create the budgets table if it doesn't exist
        String createTableSql = "CREATE TABLE IF NOT EXISTS budgets (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "limit_amount DOUBLE NOT NULL," +
                "current_spent DOUBLE DEFAULT 0.0," +
                "category_name VARCHAR(255)," +
                "period VARCHAR(50) NOT NULL," +
                "start_date DATE NOT NULL," +
                "end_date DATE," +
                "wallet_id INT" +
                ")";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
            // Ensure wallet_id column exists if table was already created
            try { stmt.executeUpdate("ALTER TABLE budgets ADD COLUMN wallet_id INT"); } catch (SQLException e) {}
        } catch (SQLException e) {
            System.err.println("Error initializing budgets table: " + e.getMessage());
        }
    }

    public void addBudget(Budget budget, int walletId) {
        String sql = "INSERT INTO budgets (name, limit_amount, current_spent, category_name, period, start_date, end_date, wallet_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, budget.getName());
            pstmt.setDouble(2, budget.getLimitAmount());
            pstmt.setDouble(3, budget.getCurrentSpent());
            if (budget.getCategory() != null) {
                pstmt.setString(4, budget.getCategory().getName());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }
            pstmt.setString(5, budget.getPeriod().name());
            pstmt.setDate(6, Date.valueOf(budget.getStartDate()));
            if (budget.getEndDate() != null) {
                pstmt.setDate(7, Date.valueOf(budget.getEndDate()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            pstmt.setInt(8, walletId);

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    budget.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding budget: " + e.getMessage());
        }
    }

    public void updateBudget(Budget budget) {
        String sql = "UPDATE budgets SET name = ?, limit_amount = ?, current_spent = ?, category_name = ?, period = ?, start_date = ?, end_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, budget.getName());
            pstmt.setDouble(2, budget.getLimitAmount());
            pstmt.setDouble(3, budget.getCurrentSpent());
            if (budget.getCategory() != null) {
                pstmt.setString(4, budget.getCategory().getName());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }
            pstmt.setString(5, budget.getPeriod().name());
            pstmt.setDate(6, Date.valueOf(budget.getStartDate()));
            if (budget.getEndDate() != null) {
                pstmt.setDate(7, Date.valueOf(budget.getEndDate()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            pstmt.setInt(8, budget.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating budget: " + e.getMessage());
        }
    }

    public void deleteBudget(int budgetId) {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, budgetId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting budget: " + e.getMessage());
        }
    }

    public List<Budget> getAllBudgets() {
        return getBudgetsByWallet(-1);
    }
    
    public List<Budget> getBudgetsByWallet(int walletId) {
        List<Budget> budgets = new ArrayList<>();
        String sql;
        if (walletId == -1) {
            sql = "SELECT * FROM budgets";
        } else {
            sql = "SELECT * FROM budgets WHERE wallet_id = ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (walletId != -1) {
                pstmt.setInt(1, walletId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    double limitAmount = rs.getDouble("limit_amount");
                    double currentSpent = rs.getDouble("current_spent");
                    String categoryName = rs.getString("category_name");
                    String periodStr = rs.getString("period");
                    LocalDate startDate = rs.getDate("start_date").toLocalDate();
                    Date endDateDb = rs.getDate("end_date");
                    LocalDate endDate = (endDateDb != null) ? endDateDb.toLocalDate() : null;

                    Category category = null;
                    if (categoryName != null) {
                        category = new Category(categoryName, TransactionType.EXPENSE);
                    }

                    Period period = Period.valueOf(periodStr);

                    Budget budget = new Budget(id, name, limitAmount, currentSpent, category, period, startDate, endDate);
                    budgets.add(budget);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving budgets: " + e.getMessage());
        }
        return budgets;
    }
}
