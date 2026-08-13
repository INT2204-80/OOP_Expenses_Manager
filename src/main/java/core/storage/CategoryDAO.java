package core.storage;

import core.Category;
import core.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO implements ICategoryDAO {

    public CategoryDAO() {
        ensureSchemaColumns();
    }

    private void ensureSchemaColumns() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            try { stmt.executeUpdate("ALTER TABLE categories ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE"); } catch (SQLException e) {}
            try { stmt.executeUpdate("ALTER TABLE categories ADD COLUMN icon VARCHAR(255)"); } catch (SQLException e) {}
            try { stmt.executeUpdate("ALTER TABLE categories ADD COLUMN color VARCHAR(255)"); } catch (SQLException e) {}
        } catch (SQLException e) {
            System.err.println("Warning: could not ensure categories schema columns: " + e.getMessage());
        }
    }

    @Override
    public int getOrCreateCategoryId(String name, String type) throws SQLException {
        return getOrCreateCategoryId(name, type, null, null);
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void softDeleteCategory(String name, String type) throws SQLException {
        String updateSql = "UPDATE categories SET is_deleted = TRUE WHERE name = ? AND transaction_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.executeUpdate();
        }
    }
}
