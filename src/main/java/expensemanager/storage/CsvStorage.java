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

public class CsvStorage implements Storage {

    @Override
    public void save(List<Transaction> transactions, String path) throws IOException {
        Path filePath = Path.of(path);
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        List<String> lines = new ArrayList<>();
        lines.add("type,id,amount,date,note,category,walletName,walletType,detail");

        for (Transaction transaction : transactions) {
            String type = transaction.getType() == TransactionType.INCOME ? "INCOME" : "EXPENSE";
            String detail = transaction instanceof Expense
                    ? ((Expense) transaction).getPaymentMethod()
                    : ((Income) transaction).getSource();
            String walletType = transaction.getWallet() != null
                    ? transaction.getWallet().getWalletType().name()
                    : WalletType.CASH.name();

            lines.add(String.join(",",
                    type,
                    String.valueOf(transaction.getId()),
                    String.valueOf(transaction.getAmount()),
                    transaction.getDate() == null ? "" : transaction.getDate().toString(),
                    escape(transaction.getNote()),
                    escape(transaction.getCategory() != null ? transaction.getCategory().getName() : ""),
                    escape(transaction.getWallet() != null ? transaction.getWallet().getName() : ""),
                    walletType,
                    escape(detail)));
        }

        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }

    @Override
    public List<Transaction> load(String path) throws IOException {
        Path filePath = Path.of(path);
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        List<Transaction> transactions = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            transactions.add(parseTransaction(line));
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

        List<String> lines = new ArrayList<>();
        lines.add("#EXPENSE_MANAGER_STATE");
        lines.add("transactions");
        lines.add("type,id,amount,date,note,category,walletName,walletType,detail");
        for (Transaction transaction : transactions) {
            String type = transaction.getType() == TransactionType.INCOME ? "INCOME" : "EXPENSE";
            String detail = transaction instanceof Expense
                    ? ((Expense) transaction).getPaymentMethod()
                    : ((Income) transaction).getSource();
            lines.add(String.join(",",
                    type,
                    String.valueOf(transaction.getId()),
                    String.valueOf(transaction.getAmount()),
                    transaction.getDate() == null ? "" : transaction.getDate().toString(),
                    escape(transaction.getNote()),
                    escape(transaction.getCategory() != null ? transaction.getCategory().getName() : ""),
                    escape(transaction.getWallet() != null ? transaction.getWallet().getName() : ""),
                    transaction.getWallet() != null ? transaction.getWallet().getWalletType().name() : WalletType.CASH.name(),
                    escape(detail)));
        }

        lines.add("wallets");
        lines.add("name,type,balance");
        for (Wallet wallet : wallets) {
            lines.add(String.join(",",
                    escape(wallet.getName()),
                    wallet.getWalletType().name(),
                    String.valueOf(wallet.getBalance())));
        }

        lines.add("categories");
        lines.add("name,type");
        for (Category category : categories) {
            lines.add(String.join(",", escape(category.getName()), category.getType().name()));
        }

        lines.add("budgets");
        lines.add("categoryName,limit,period,startDate,endDate");
        for (Map.Entry<Category, Budget> entry : budgets.entrySet()) {
            Budget budget = entry.getValue();
            lines.add(String.join(",",
                    escape(entry.getKey() != null ? entry.getKey().getName() : ""),
                    String.valueOf(budget.getLimit()),
                    budget.getPeriod() != null ? budget.getPeriod().name() : "",
                    budget.getStartDate() != null ? budget.getStartDate().toString() : "",
                    budget.getEndDate() != null ? budget.getEndDate().toString() : ""));
        }

        Files.write(filePath, lines, StandardCharsets.UTF_8);
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

        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        transactions.clear();
        wallets.clear();
        categories.clear();
        budgets.clear();

        String section = "";
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            if ("transactions".equals(trimmed)) {
                section = "transactions";
                continue;
            }
            if ("wallets".equals(trimmed)) {
                section = "wallets";
                continue;
            }
            if ("categories".equals(trimmed)) {
                section = "categories";
                continue;
            }
            if ("budgets".equals(trimmed)) {
                section = "budgets";
                continue;
            }
            if ("type,id,amount,date,note,category,walletName,walletType,detail".equals(trimmed)) {
                continue;
            }
            if ("name,type,balance".equals(trimmed) || "name,type".equals(trimmed)
                    || "categoryName,limit,period,startDate,endDate".equals(trimmed)) {
                continue;
            }

            switch (section) {
                case "transactions" -> transactions.add(parseTransaction(trimmed));
                case "wallets" -> wallets.add(parseWallet(trimmed));
                case "categories" -> categories.add(parseCategory(trimmed));
                case "budgets" -> {
                    Budget budget = parseBudget(trimmed, categories);
                    if (budget != null) {
                        budgets.put(budget.getCategory(), budget);
                    }
                }
                default -> {
                }
            }
        }
    }

    private Transaction parseTransaction(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 9) {
            throw new IllegalArgumentException("Invalid CSV transaction row: " + line);
        }

        String type = parts[0].trim();
        int id = Integer.parseInt(parts[1].trim());
        double amount = Double.parseDouble(parts[2].trim());
        LocalDate date = parts[3].isBlank() ? null : LocalDate.parse(parts[3].trim());
        String note = unescape(parts[4]);
        String categoryName = unescape(parts[5]);
        String walletName = unescape(parts[6]);
        String walletType = parts[7].trim();
        String detail = unescape(parts[8]);

        Wallet wallet = createWallet(walletName, walletType);
        Category category = new Category(categoryName, type.equals("INCOME") ? TransactionType.INCOME : TransactionType.EXPENSE);

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

    private Wallet parseWallet(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid wallet row: " + line);
        }
        String walletName = unescape(parts[0]);
        String walletType = parts[1].trim();
        double balance = Double.parseDouble(parts[2].trim());
        Wallet wallet = createWallet(walletName, walletType);
        wallet.setBalance(balance);
        return wallet;
    }

    private Category parseCategory(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid category row: " + line);
        }
        String categoryName = unescape(parts[0]);
        TransactionType type = TransactionType.valueOf(parts[1].trim().toUpperCase());
        return new Category(categoryName, type);
    }

    private Budget parseBudget(String line, List<Category> categories) {
        String[] parts = line.split(",", -1);
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid budget row: " + line);
        }
        String categoryName = unescape(parts[0]);
        double limit = Double.parseDouble(parts[1].trim());
        Period period = parts[2].isBlank() ? null : Period.valueOf(parts[2].trim().toUpperCase());
        LocalDate startDate = parts[3].isBlank() ? null : LocalDate.parse(parts[3].trim());
        LocalDate endDate = parts[4].isBlank() ? null : LocalDate.parse(parts[4].trim());

        Category category = categories.stream()
                .filter(existing -> existing != null && existing.getName() != null && existing.getName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElseGet(() -> new Category(categoryName, TransactionType.EXPENSE));

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

    private String escape(String value) {
        return value == null ? "" : value.replace(",", ";");
    }

    private String unescape(String value) {
        return value == null ? "" : value.replace(";", ",");
    }
}
