package core.storage;

import core.Category;
import java.sql.SQLException;
import java.util.List;

public interface ICategoryDAO {
    int getOrCreateCategoryId(String name, String type) throws SQLException;
    int getOrCreateCategoryId(String name, String type, String icon, String color) throws SQLException;
    List<Category> getAllCategories();
    int getCategoryId(String name, String type) throws SQLException;
    void mergeCategories(List<Category> sources, Category target) throws SQLException;
    void updateCategory(String oldName, String oldType, String newName, String newType, String newIcon, String newColor) throws SQLException;
    void softDeleteCategory(String name, String type) throws SQLException;
    Category getCategoryById(int id) throws SQLException;
}
