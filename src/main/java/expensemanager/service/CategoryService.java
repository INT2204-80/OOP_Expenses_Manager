package expensemanager.service;

import core.Category;
import core.storage.ICategoryDAO;

import java.sql.SQLException;
import java.util.List;

public class CategoryService {
    private final ICategoryDAO categoryDAO;

    public CategoryService(ICategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public int createCategory(String name, String type, String icon, String color) throws SQLException {
        return categoryDAO.getOrCreateCategoryId(name, type, icon, color);
    }

    public void deleteCategories(List<Category> categories) throws SQLException {
        for (Category cat : categories) {
            categoryDAO.softDeleteCategory(cat.getName(), cat.getType().name());
        }
    }

    public void mergeCategories(List<Category> sources, Category target) throws SQLException {
        categoryDAO.mergeCategories(sources, target);
    }

    public void updateCategory(String oldName, String oldType, String newName, String newType, String newIcon, String newColor) throws SQLException {
        categoryDAO.updateCategory(oldName, oldType, newName, newType, newIcon, newColor);
    }
}
