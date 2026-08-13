package expensemanager.ui.controllers;

import java.io.IOException;
import java.util.List;

import core.Budget;
import core.Category;
import core.storage.BudgetDAO;
import core.storage.TransactionDAO;
import core.storage.WalletDAO;
import core.transaction.Transaction;
import core.wallet.Wallet;
import expensemanager.service.BudgetService;
import expensemanager.ui.factory.BudgetCardFactory;
import expensemanager.ui.factory.BudgetDialogFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class BudgetsViewController {

    @FXML
    private FlowPane budgetsContainer;

    private final BudgetService budgetService;
    private final BudgetDAO budgetDAO;
    private final TransactionDAO transactionDAO;
    private final WalletDAO walletDAO;

    // Injection dependencies (chuẩn DIP)
    public BudgetsViewController() {
        this.budgetDAO = new BudgetDAO();
        this.transactionDAO = new TransactionDAO();
        this.walletDAO = new WalletDAO();
        this.budgetService = new BudgetService(this.budgetDAO, this.transactionDAO);
    }

    @FXML
    public void initialize() {
        loadBudgets();
    }

    /**
     * Gộp budget của TẤT CẢ các ví để hiển thị ở Dashboard, nhưng tính "spent"
     * riêng theo từng ví (dùng đúng transactions của ví đó), không dùng SQL
     * không lọc wallet_id và không ghi đè xuống DB.
     */
    private void loadBudgets() {
        budgetsContainer.getChildren().clear();

        List<Wallet> wallets = walletDAO.getAllWallets();
        boolean hasAnyBudget = false;

        for (Wallet wallet : wallets) {
            List<Budget> walletBudgets = budgetDAO.getBudgetsByWallet(wallet.getId());
            if (walletBudgets.isEmpty()) {
                continue;
            }

            List<Transaction> walletTx = transactionDAO.getTransactionsByWallet(wallet);

            for (Budget budget : walletBudgets) {
                // Chỉ tính trong bộ nhớ để hiển thị, KHÔNG updateBudget() xuống DB
                budget.updateSpentFromTransactions(walletTx);

                VBox card = BudgetCardFactory.createBudgetCard(
                        budget,
                        wallet.getName(),
                        () -> handleEditBudget(budget),
                        () -> handleDeleteBudget(budget));
                budgetsContainer.getChildren().add(card);
                hasAnyBudget = true;
            }
        }

        if (!hasAnyBudget) {
            Label emptyLabel = new Label("Chưa có ngân sách nào. Hãy tạo ngân sách từ bên trong từng ví.");
            budgetsContainer.getChildren().add(emptyLabel);
        }
    }

    private void handleEditBudget(Budget budget) {
        List<Category> allCategories = budgetService.fetchAllCategories();
        Dialog<Budget> dialog = BudgetDialogFactory.createEditBudgetDialog(budget, allCategories);
        dialog.showAndWait().ifPresent(updatedBudget -> {
            budgetService.updateBudget(updatedBudget);
            loadBudgets();
        });
    }

    private void handleDeleteBudget(Budget budget) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this budget?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                budgetService.removeBudget(budget.getId());
                loadBudgets();
            }
        });
    }

    @FXML
    private void handleNavigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = budgetsContainer.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Unable to load dashboard view.");
            errorAlert.setHeaderText(null);
            errorAlert.showAndWait();
        }
    }
}
