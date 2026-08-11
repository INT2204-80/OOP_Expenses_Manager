package core.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/expense_manager";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    static {
        // Run migration to add currency if missing
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE wallets ADD COLUMN currency VARCHAR(10) DEFAULT 'VND'");
            System.out.println("Migrated DB: Added currency column to wallets.");
        } catch (Exception e) {
            // Probably already exists
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
