package expensemanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import core.Budget;
import core.Category;
import core.TransactionType;
import core.transaction.Transaction;
import core.wallet.CashWallet;
import core.wallet.Wallet;
import expensemanager.storage.Storage;

public class ExpenseManager {
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<Wallet> wallets = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private final Map<Category, Budget> budgets = new LinkedHashMap<>();
    private final Storage storage;

    public ExpenseManager(Storage storage) {
        this.storage = storage;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public List<Wallet> getWallets() {
        return wallets;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Map<Category, Budget> getBudgets() {
        return budgets;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public boolean removeTransaction(int id) {
        return transactions.removeIf(transaction -> transaction.getId() == id);
    }

    public boolean updateTransaction(int id, Transaction updatedTransaction) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId() == id) {
                transactions.set(i, updatedTransaction);
                return true;
            }
        }
        return false;
    }

    public Transaction findTransaction(int id) {
        return transactions.stream()
                .filter(transaction -> transaction.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public void addBudget(Category category, Budget budget) {
        budgets.put(category, budget);
    }

    public Map<String, Double> monthlySummary(int year, int month) {
        double income = 0.0;
        double expenses = 0.0;

        for (Transaction transaction : transactions) {
            if (transaction.getDate() == null) {
                continue;
            }
            if (transaction.getDate().getYear() != year || transaction.getDate().getMonthValue() != month) {
                continue;
            }
            if (transaction.getType() == TransactionType.INCOME) {
                income += transaction.getAmount();
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                expenses += transaction.getAmount();
            }
        }

        Map<String, Double> summary = new LinkedHashMap<>();
        summary.put("income", income);
        summary.put("expenses", expenses);
        summary.put("net", income - expenses);
        return summary;
    }

    public Map<Category, Double> statisticsByCategory() {
        Map<Category, Double> statistics = new LinkedHashMap<>();
        for (Transaction transaction : transactions) {
            Category category = resolveCategory(transaction.getCategory());
            if (category == null) {
                continue;
            }
            statistics.merge(category, transaction.getAmount(), Double::sum);
        }
        return statistics;
    }

    public void saveToStorage(String path) throws IOException {
        if (storage != null) {
            storage.saveState(transactions, wallets, categories, budgets, path);
        }
    }

    public List<Transaction> loadFromStorage(String path) throws IOException {
        if (storage == null) {
            return List.of();
        }
        storage.loadState(path, transactions, wallets, categories, budgets);
        return transactions;
    }

    private Category resolveCategory(Category category) {
        String categoryName = category != null ? category.getName() : null;
        if (categoryName == null) {
            return null;
        }
        for (Category cat : categories) {
            if (cat != null && cat.getName() != null && cat.getName().equalsIgnoreCase(categoryName)) {
                return category;
            }
        }
        return null;
    }

    public Storage getStorage() {
        return storage;
    }

    public void addWallet(Wallet wallet) {
        wallets.add(wallet);
    }

    public Wallet findWalletByName(String name) {
        return wallets.stream()
                .filter(wallet -> wallet.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> new CashWallet(name, 0.0));
    }
}
