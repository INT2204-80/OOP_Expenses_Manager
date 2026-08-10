import core.storage.TransactionDAO;
import core.Category;
import java.util.List;

public class TestDB {
    public static void main(String[] args) {
        TransactionDAO dao = new TransactionDAO();
        List<Category> cats = dao.getAllCategories();
        System.out.println("Categories count: " + cats.size());
        for (Category c : cats) {
            System.out.println(c.getName() + " - " + c.getType());
        }
    }
}
