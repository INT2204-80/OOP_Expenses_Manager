package expensemanager.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import core.Budget;
import core.Category;
import core.Period;
import core.TransactionType;
import core.WalletType;
import core.transaction.Expense;
import core.transaction.Income;
import core.transaction.Transaction;
import core.wallet.BankAccount;
import core.wallet.CashWallet;
import core.wallet.EWallet;
import core.wallet.Wallet;

public class JsonStorage implements Storage {

    @Override
    public void save(List<Transaction> transactions, String path) throws IOException {
        Path filePath = Path.of(path);
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            if (i > 0) {
                builder.append(",");
            }
            builder.append("{\"type\":\"")
                    .append(transaction.getType() == TransactionType.INCOME ? "INCOME" : "EXPENSE")
                    .append("\",\"id\":")
                    .append(transaction.getId())
                    .append(",\"amount\":")
                    .append(transaction.getAmount())
                    .append(",\"date\":\"")
                    .append(transaction.getDate() == null ? "" : transaction.getDate())
                    .append("\",\"note\":\"")
                    .append(escape(transaction.getNote()))
                    .append("\",\"category\":\"")
                    .append(escape(transaction.getCategory() != null ? transaction.getCategory().getName() : ""))
                    .append("\",\"walletName\":\"")
                    .append(escape(transaction.getWallet() != null ? transaction.getWallet().getName() : ""))
                    .append("\",\"walletType\":\"")
                    .append(transaction.getWallet() != null ? transaction.getWallet().getWalletType().name() : WalletType.CASH.name())
                    .append("\",\"detail\":\"")
                    .append(transaction instanceof Expense ? escape(((Expense) transaction).getPaymentMethod()) : escape(((Income) transaction).getSource()))
                    .append("\"}");
        }
        builder.append("]");
        Files.writeString(filePath, builder.toString(), StandardCharsets.UTF_8);
    }

    @Override
    public List<Transaction> load(String path) throws IOException {
        Path filePath = Path.of(path);
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        if (content == null || content.isBlank()) {
            return new ArrayList<>();
        }

        List<Transaction> transactions = new ArrayList<>();
        String trimmed = content.trim();
        if (trimmed.length() < 2) {
            return transactions;
        }

        String body = trimmed.substring(1, trimmed.length() - 1);
        if (body.isBlank()) {
            return transactions;
        }

        String[] entries = body.split("\\},\\{");
        for (String entry : entries) {
            String normalized = entry.replace("{", "").replace("}", "");
            if (normalized.isBlank()) {
                continue;
            }
            transactions.add(parseTransaction(normalized));
        }

        return transactions;
    }

    @Override
    public void saveState(List<Transaction> transactions, List<Wallet> wallets, List<Category> categories,
            Map<Category, Budget> budgets, String path) throws IOException {
        Path filePath = Path.of(path);
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("{\"transactions\":[");
        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            if (i > 0) {
                builder.append(",");
            }
            builder.append("{\"type\":\"")
                    .append(transaction.getType() == TransactionType.INCOME ? "INCOME" : "EXPENSE")
                    .append("\",\"id\":")
                    .append(transaction.getId())
                    .append(",\"amount\":")
                    .append(transaction.getAmount())
                    .append(",\"date\":\"")
                    .append(transaction.getDate() == null ? "" : transaction.getDate())
                    .append("\",\"note\":\"")
                    .append(escape(transaction.getNote()))
                    .append("\",\"category\":\"")
                    .append(escape(transaction.getCategory() != null ? transaction.getCategory().getName() : ""))
                    .append("\",\"walletName\":\"")
                    .append(escape(transaction.getWallet() != null ? transaction.getWallet().getName() : ""))
                    .append("\",\"walletType\":\"")
                    .append(transaction.getWallet() != null ? transaction.getWallet().getWalletType().name() : WalletType.CASH.name())
                    .append("\",\"detail\":\"")
                    .append(transaction instanceof Expense ? escape(((Expense) transaction).getPaymentMethod()) : escape(((Income) transaction).getSource()))
                    .append("\"}");
        }
        builder.append("],\"wallets\":[");
        for (int i = 0; i < wallets.size(); i++) {
            Wallet wallet = wallets.get(i);
            if (i > 0) {
                builder.append(",");
            }
            builder.append("{\"name\":\"").append(escape(wallet.getName())).append("\",\"type\":\"")
                    .append(wallet.getWalletType().name()).append("\",\"balance\":")
                    .append(wallet.getBalance()).append("}");
        }
        builder.append("],\"categories\":[");
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            if (i > 0) {
                builder.append(",");
            }
            builder.append("{\"name\":\"").append(escape(category.getName())).append("\",\"type\":\"")
                    .append(category.getType().name()).append("\"}");
        }
        builder.append("],\"budgets\":[");
        int budgetIndex = 0;
        for (Map.Entry<Category, Budget> entry : budgets.entrySet()) {
            if (budgetIndex > 0) {
                builder.append(",");
            }
            Budget budget = entry.getValue();
            builder.append("{\"category\":\"").append(escape(entry.getKey() != null ? entry.getKey().getName() : ""))
                    .append("\",\"limit\":").append(budget.getLimit())
                    .append(",\"period\":\"").append(budget.getPeriod() != null ? budget.getPeriod().name() : "")
                    .append("\",\"startDate\":\"").append(budget.getStartDate() != null ? budget.getStartDate() : "")
                    .append("\",\"endDate\":\"").append(budget.getEndDate() != null ? budget.getEndDate() : "")
                    .append("\"}");
            budgetIndex++;
        }
        builder.append("]}");
        Files.writeString(filePath, builder.toString(), StandardCharsets.UTF_8);
    }

    @Override
    public void loadState(String path, List<Transaction> transactions, List<Wallet> wallets,
            List<Category> categories, Map<Category, Budget> budgets) throws IOException {
        Path filePath = Path.of(path);
        if (!Files.exists(filePath)) {
            transactions.clear();
            wallets.clear();
            categories.clear();
            budgets.clear();
            return;
        }

        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        transactions.clear();
        wallets.clear();
        categories.clear();
        budgets.clear();

        if (content == null || content.isBlank()) {
            return;
        }

        for (String entry : extractArray(content, "transactions")) {
            if (!entry.isBlank()) {
                transactions.add(parseTransaction(entry));
            }
        }
        for (String entry : extractArray(content, "wallets")) {
            if (!entry.isBlank()) {
                wallets.add(parseWallet(entry));
            }
        }
        for (String entry : extractArray(content, "categories")) {
            if (!entry.isBlank()) {
                categories.add(parseCategory(entry));
            }
        }
        for (String entry : extractArray(content, "budgets")) {
            if (!entry.isBlank()) {
                Budget budget = parseBudget(entry, categories);
                if (budget != null) {
                    budgets.put(budget.getCategory(), budget);
                }
            }
        }
    }

    private Transaction parseTransaction(String entry) {
        String[] parts = entry.split(",");
        String type = null;
        int id = 0;
        double amount = 0.0;
        LocalDate date = null;
        String note = "";
        String categoryName = "";
        String walletName = "";
        String walletType = "CASH";
        String detail = "";

        for (String part : parts) {
            String[] kv = part.split(":", 2);
            if (kv.length != 2) {
                continue;
            }
            String key = kv[0].replace("\"", "").trim();
            String value = kv[1].replace("\"", "").trim();
            switch (key) {
                case "type" -> type = value;
                case "id" -> id = Integer.parseInt(value);
                case "amount" -> amount = Double.parseDouble(value);
                case "date" -> date = value.isBlank() ? null : LocalDate.parse(value);
                case "note" -> note = unescape(value);
                case "category" -> categoryName = unescape(value);
                case "walletName" -> walletName = unescape(value);
                case "walletType" -> walletType = value;
                case "detail" -> detail = unescape(value);
                default -> {
                }
            }
        }

        Wallet wallet = createWallet(walletName, walletType);
        TransactionType resolvedType = "INCOME".equalsIgnoreCase(type) ? TransactionType.INCOME : TransactionType.EXPENSE;
        Category category = new Category(categoryName, resolvedType);

        if ("INCOME".equalsIgnoreCase(type)) {
            return new Income(id, amount, date, note, category, wallet, detail);
        }
        return new Expense(id, amount, date, note, category, wallet, detail);
    }

    private Wallet createWallet(String walletName, String walletType) {
        WalletType type = WalletType.valueOf(walletType.toUpperCase());
        return switch (type) {
            case BANK -> new BankAccount(walletName, 0.0, "Bank", "0000");
            case EWALLET -> new EWallet(walletName, 0.0, "Provider");
            case CASH -> new CashWallet(walletName, 0.0);
        };
    }

    private Wallet parseWallet(String entry) {
        String[] parts = entry.split(",");
        String name = "";
        String walletType = "CASH";
        double balance = 0.0;
        for (String part : parts) {
            String[] kv = part.split(":", 2);
            if (kv.length != 2) {
                continue;
            }
            String key = kv[0].replace("\"", "").trim();
            String value = kv[1].replace("\"", "").trim();
            switch (key) {
                case "name" -> name = unescape(value);
                case "type" -> walletType = value;
                case "balance" -> balance = Double.parseDouble(value);
                default -> {
                }
            }
        }
        Wallet wallet = createWallet(name, walletType);
        wallet.setBalance(balance);
        return wallet;
    }

    private Category parseCategory(String entry) {
        String[] parts = entry.split(",");
        String name = "";
        String type = "EXPENSE";
        for (String part : parts) {
            String[] kv = part.split(":", 2);
            if (kv.length != 2) {
                continue;
            }
            String key = kv[0].replace("\"", "").trim();
            String value = kv[1].replace("\"", "").trim();
            switch (key) {
                case "name" -> name = unescape(value);
                case "type" -> type = value;
                default -> {
                }
            }
        }
        return new Category(name, TransactionType.valueOf(type.toUpperCase()));
    }

    private Budget parseBudget(String entry, List<Category> categories) {
        String[] parts = entry.split(",");
        String categoryName = "";
        double limit = 0.0;
        Period period = null;
        LocalDate startDate = null;
        LocalDate endDate = null;
        for (String part : parts) {
            String[] kv = part.split(":", 2);
            if (kv.length != 2) {
                continue;
            }
            String key = kv[0].replace("\"", "").trim();
            String value = kv[1].replace("\"", "").trim();
            switch (key) {
                case "category" -> categoryName = unescape(value);
                case "limit" -> limit = Double.parseDouble(value);
                case "period" -> period = value.isBlank() ? null : Period.valueOf(value.toUpperCase());
                case "startDate" -> startDate = value.isBlank() ? null : LocalDate.parse(value);
                case "endDate" -> endDate = value.isBlank() ? null : LocalDate.parse(value);
                default -> {
                }
            }
        }
        final String categoryNameValue = categoryName;
        Category category = categories.stream()
                .filter(existing -> existing != null && existing.getName() != null && existing.getName().equalsIgnoreCase(categoryNameValue))
                .findFirst()
                .orElseGet(() -> new Category(categoryNameValue, TransactionType.EXPENSE));
        if (period != null) {
            Budget budget = new Budget(category, limit, period);
            budget.setStartDate(startDate != null ? startDate : budget.getStartDate());
            budget.setEndDate(endDate != null ? endDate : budget.getEndDate());
            return budget;
        }
        if (startDate != null && endDate != null) {
            return new Budget(category, limit, startDate, endDate);
        }
        return null;
    }

    private List<String> extractArray(String content, String key) {
        String marker = "\"" + key + "\":[";
        int start = content.indexOf(marker);
        if (start < 0) {
            return List.of();
        }
        start += marker.length();
        int end = content.indexOf("]", start);
        if (end < 0) {
            return List.of();
        }
        String body = content.substring(start, end);
        if (body.isBlank()) {
            return List.of();
        }
        return List.of(body.split("\\},\\{"));
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\"", "\\\"");
    }

    private String unescape(String value) {
        return value == null ? "" : value.replace("\\\"", "\"");
    }
}
