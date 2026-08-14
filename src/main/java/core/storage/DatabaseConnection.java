package core.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/expense_manager";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    static {
        // Run migration to add new columns if missing
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             java.sql.Statement stmt = conn.createStatement()) {
            
            // wallets
            try { stmt.execute("ALTER TABLE wallets ADD COLUMN currency VARCHAR(10) DEFAULT 'VND'"); } catch (Exception e) {}
            
            // categories
            try { stmt.execute("ALTER TABLE categories ADD COLUMN icon VARCHAR(255)"); } catch (Exception e) {}
            try { stmt.execute("ALTER TABLE categories ADD COLUMN color VARCHAR(255)"); } catch (Exception e) {}
            try { stmt.execute("ALTER TABLE categories ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE"); } catch (Exception e) {}

            // transactions
            try { stmt.execute("ALTER TABLE transactions ADD COLUMN is_recurring BOOLEAN DEFAULT FALSE"); } catch (Exception e) {}
            try { stmt.execute("ALTER TABLE transactions ADD COLUMN recurring_period VARCHAR(50)"); } catch (Exception e) {}
            try { stmt.execute("ALTER TABLE transactions ADD COLUMN passed_periods INT DEFAULT 0"); } catch (Exception e) {}
            try { stmt.execute("ALTER TABLE transactions ADD COLUMN recurring_end_date DATE"); } catch (Exception e) {}

            // budgets
            try { stmt.execute("ALTER TABLE budgets ADD COLUMN category_id INT"); } catch (Exception e) {}
            // Migrate category_name to category_id
            try {
                stmt.execute("UPDATE budgets b JOIN categories c ON b.category_name = c.name SET b.category_id = c.id WHERE b.category_id IS NULL AND b.category_name IS NOT NULL");
            } catch (Exception e) {}
            try { stmt.execute("ALTER TABLE budgets DROP COLUMN category_name"); } catch (Exception e) {}
            
            System.out.println("Migrated DB: Schema updated.");
        } catch (Exception e) {
            System.err.println("Database migration failed: " + e.getMessage());
        }
    }
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
    }
}
