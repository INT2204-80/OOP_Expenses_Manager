package expensemanager.ui.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import core.Category;
import core.transaction.Transaction;
import core.wallet.Wallet;
import expensemanager.service.TransactionService;
import expensemanager.ui.factory.TransactionDialogFactory;
import expensemanager.ui.factory.TransactionRowFactory;
import expensemanager.ui.util.TransactionFilter;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class WalletTransactionManager {
    private static final String ALL_CATEGORIES_SENTINEL = "All categories";
    private final TransactionService transactionService;

    public WalletTransactionManager(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setupFilters(ComboBox<String> filterCategoryCombo, TextField filterNoteField,
                             TextField filterMinAmountField, TextField filterMaxAmountField,
                             List<Category> allCategories, Runnable onFilterChanged) {
        if (filterCategoryCombo != null) {
            filterCategoryCombo.getItems().clear();
            filterCategoryCombo.getItems().add(ALL_CATEGORIES_SENTINEL);
            for (Category cat : allCategories) {
                filterCategoryCombo.getItems().add(cat.getName());
            }
            filterCategoryCombo.getSelectionModel().selectFirst();
            filterCategoryCombo.valueProperty().addListener((obs, o, n) -> onFilterChanged.run());
        }
        if (filterNoteField != null) {
            filterNoteField.textProperty().addListener((obs, o, n) -> onFilterChanged.run());
        }
        if (filterMinAmountField != null) {
            filterMinAmountField.textProperty().addListener((obs, o, n) -> onFilterChanged.run());
        }
        if (filterMaxAmountField != null) {
            filterMaxAmountField.textProperty().addListener((obs, o, n) -> onFilterChanged.run());
        }
    }

    public List<Transaction> getFilteredTransactions(Wallet wallet, PeriodFilterManager periodManager,
                                                      ComboBox<String> filterCategoryCombo,
                                                      TextField filterNoteField,
                                                      TextField filterMinAmountField,
                                                      TextField filterMaxAmountField) {
        if (wallet == null) {
            return new ArrayList<>();
        }
        Double minAmount = parseDoubleOrNull(filterMinAmountField);
        Double maxAmount = parseDoubleOrNull(filterMaxAmountField);
        String selectedCategory = filterCategoryCombo != null ? filterCategoryCombo.getValue() : null;
        String noteKeyword = filterNoteField != null ? filterNoteField.getText() : null;

        return TransactionFilter.create()
                .byPeriod(periodManager.getStart(), periodManager.getEnd())
                .byCategoryName(selectedCategory, ALL_CATEGORIES_SENTINEL)
                .byNoteContains(noteKeyword)
                .byMinAmount(minAmount)
                .byMaxAmount(maxAmount)
                .apply(wallet.getTransactions());
    }

    public void renderTransactions(VBox transactionsListContainer, VBox emptyTransactionsState,
                                   ScrollPane transactionsScrollPane, List<Transaction> filteredTransactions,
                                   List<Category> allCategories, Wallet currentWallet, Runnable refreshCallback) {
        if (transactionsListContainer == null || emptyTransactionsState == null || transactionsScrollPane == null) {
            return;
        }
        transactionsListContainer.getChildren().clear();

        boolean empty = filteredTransactions.isEmpty();
        transactionsScrollPane.setVisible(!empty);
        transactionsScrollPane.setManaged(!empty);
        emptyTransactionsState.setVisible(empty);
        emptyTransactionsState.setManaged(empty);
        if (empty) {
            return;
        }

        for (Transaction t : filteredTransactions) {
            transactionsListContainer.getChildren().add(
                    TransactionRowFactory.createTransactionRow(
                            t,
                            oldT -> handleEditTransaction(oldT, allCategories, currentWallet, refreshCallback),
                            deletedT -> handleDeleteTransaction(deletedT, currentWallet, refreshCallback)
                    )
            );
        }
    }

    public void showAddTransactionDialog(List<Category> allCategories, Wallet currentWallet, Runnable refreshCallback) {
        Dialog<Transaction> dialog = TransactionDialogFactory.createDialog(null, allCategories, currentWallet);
        Optional<Transaction> result = dialog.showAndWait();
        result.ifPresent(transaction -> {
            transactionService.addTransactionAndUpdateWallet(transaction, currentWallet);
            refreshCallback.run();
        });
    }

    public void handleResetFilters(ComboBox<String> filterCategoryCombo, TextField filterNoteField,
                                   TextField filterMinAmountField, TextField filterMaxAmountField, Runnable refreshCallback) {
        if (filterCategoryCombo != null) filterCategoryCombo.getSelectionModel().selectFirst();
        if (filterNoteField != null) filterNoteField.clear();
        if (filterMinAmountField != null) filterMinAmountField.clear();
        if (filterMaxAmountField != null) filterMaxAmountField.clear();
        refreshCallback.run();
    }

    private void handleDeleteTransaction(Transaction t, Wallet currentWallet, Runnable refreshCallback) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa giao dịch này?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            transactionService.deleteTransactionAndUpdateWallet(t, currentWallet);
            refreshCallback.run();
        }
    }

    private void handleEditTransaction(Transaction oldT, List<Category> allCategories, Wallet currentWallet, Runnable refreshCallback) {
        Dialog<Transaction> dialog = TransactionDialogFactory.createDialog(oldT, allCategories, currentWallet);
        Optional<Transaction> result = dialog.showAndWait();
        result.ifPresent(newT -> {
            transactionService.updateTransactionAndUpdateWallet(oldT, newT, currentWallet);
            refreshCallback.run();
        });
    }

    private static Double parseDoubleOrNull(TextField field) {
        if (field == null || field.getText() == null || field.getText().trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}