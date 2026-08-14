package expensemanager.ui.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.util.stream.Collectors;

import core.Category;
import core.transaction.PendingRecurringOccurrence;
import core.transaction.RecurringExpense;
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
import javafx.scene.control.Label;
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
        boolean futureOnly = periodManager.isFutureOnly();

        List<Transaction> source = new ArrayList<>(wallet.getTransactions());

        if (!futureOnly) {
            // Bo sung cac lan xuat hien (occurrence) "pending" trong tuong lai cua
            // giao dich lap lai, roi vao khoang [start, end] dang xem theo ky.
            // Day la giao dich AO (khong luu DB, khong tru wallet that) - chi de:
            // (1) hien "Pending" trong danh sach; (2) cong vao total Overview cua ky do.
            LocalDate today = LocalDate.now();
            LocalDate start = periodManager.getStart();
            LocalDate end = periodManager.getEnd();
            for (Transaction t : wallet.getTransactions()) {
                if (t instanceof RecurringExpense) {
                    RecurringExpense re = (RecurringExpense) t;
                    for (LocalDate occDate : re.getOccurrencesBetween(start, end)) {
                        if (occDate.isAfter(today)) {
                            source.add(new PendingRecurringOccurrence(re, occDate));
                        }
                    }
                }
            }
        }

        TransactionFilter filter = TransactionFilter.create()
                .byFutureMode(LocalDate.now(), futureOnly);

        if (!futureOnly) {
            filter.byPeriod(periodManager.getStart(), periodManager.getEnd());
        }

        return filter
                .byCategoryName(selectedCategory, ALL_CATEGORIES_SENTINEL)
                .byNoteContains(noteKeyword)
                .byMinAmount(minAmount)
                .byMaxAmount(maxAmount)
                .apply(source);
    }

    public void renderTransactions(VBox transactionsListContainer, VBox emptyTransactionsState,
                                   ScrollPane transactionsScrollPane, List<Transaction> filteredTransactions,
                                   List<Category> allCategories, Wallet currentWallet, boolean futureOnly,
                                   Runnable refreshCallback) {
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

        if (futureOnly) {
            // Tach thanh 2 khoi rieng: "Dinh ky sap toi" (RecurringExpense) va
            // "Giao dich sap toi" (giao dich thuong, mot lan, ngay trong tuong lai).
            List<Transaction> recurring = filteredTransactions.stream()
                    .filter(t -> t instanceof RecurringExpense)
                    .collect(Collectors.toList());
            List<Transaction> nonRecurring = filteredTransactions.stream()
                    .filter(t -> !(t instanceof RecurringExpense))
                    .collect(Collectors.toList());

            if (!recurring.isEmpty()) {
                transactionsListContainer.getChildren().add(createSectionHeader("Định kỳ sắp tới"));
                addRows(transactionsListContainer, recurring, allCategories, currentWallet, refreshCallback, true);
            }
            if (!nonRecurring.isEmpty()) {
                transactionsListContainer.getChildren().add(createSectionHeader("Giao dịch sắp tới"));
                addRows(transactionsListContainer, nonRecurring, allCategories, currentWallet, refreshCallback, false);
            }
        } else {
            addRows(transactionsListContainer, filteredTransactions, allCategories, currentWallet, refreshCallback, false);
        }
    }

    private void addRows(VBox container, List<Transaction> transactions, List<Category> allCategories,
                         Wallet currentWallet, Runnable refreshCallback, boolean futureRecurringDisplay) {
        for (Transaction t : transactions) {
            container.getChildren().add(
                    TransactionRowFactory.createTransactionRow(
                            t,
                            oldT -> handleEditTransaction(oldT, allCategories, currentWallet, refreshCallback),
                            deletedT -> handleDeleteTransaction(deletedT, currentWallet, refreshCallback),
                            futureRecurringDisplay
                    )
            );
        }
    }

    private Label createSectionHeader(String text) {
        Label header = new Label(text);
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #64748b; -fx-padding: 10 0 4 4;");
        return header;
    }

    public void showAddTransactionDialog(List<Category> allCategories, Wallet currentWallet, Runnable refreshCallback) {
        Dialog<Transaction> dialog = TransactionDialogFactory.createDialog(null, allCategories, currentWallet);
        Optional<Transaction> result = dialog.showAndWait();
        result.ifPresent(transaction -> {
            if (transaction instanceof RecurringExpense) {
                handleAddRecurringWithBackfill((RecurringExpense) transaction, currentWallet, refreshCallback);
            } else {
                transactionService.addTransactionAndUpdateWallet(transaction, currentWallet);
                refreshCallback.run();
            }
        });
    }

    /**
     * Xử lý thêm RecurringExpense mới.
     * Constructor không làm thay đổi số dư ví.
     * Việc trừ tiền được thực hiện tập trung trong TransactionService.
     * Nếu ngày khởi tạo ở quá khứ và đã qua X chu kỳ, hỏi xác nhận người dùng trước khi backfill X giao dịch tương ứng.
     */
    private void handleAddRecurringWithBackfill(RecurringExpense re, Wallet currentWallet, Runnable refreshCallback) {
        re.nextDueDate(); // tinh toan lai passedPeriods dua tren ngay hien tai
        int passed = re.getPassedPeriods();

        if (passed <= 0) {
            transactionService.addTransactionAndUpdateWallet(re, currentWallet);
            refreshCallback.run();
            return;
        }

        int totalOccurrences = passed + 1;
        double totalRequired = re.getAmount() * totalOccurrences;
        
        if (!Double.isFinite(totalRequired) || totalRequired > currentWallet.getBalance()) {
            showError("Số dư ví không đủ để tạo " + totalOccurrences + " giao dịch!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận giao dịch định kỳ trong quá khứ");
        alert.setHeaderText("Ngày khởi tạo đã qua " + passed + " chu kỳ");
        alert.setContentText("Hệ thống sẽ thêm " + passed
                + " giao dịch tương ứng với các chu kỳ đã qua và trừ thẳng vào ví. Bạn có muốn tiếp tục?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> confirmResult = alert.showAndWait();
        if (confirmResult.isPresent() && confirmResult.get() == ButtonType.YES) {
            transactionService.addRecurringExpenseWithBackfill(re, currentWallet);
            refreshCallback.run();
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setHeaderText(null);
        alert.showAndWait();
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