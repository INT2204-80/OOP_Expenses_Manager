package expensemanager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Scanner;

import core.Budget;
import core.Category;
import core.Period;
import core.TransactionType;
import core.transaction.Expense;
import core.transaction.Income;
import core.transaction.Transaction;
import core.wallet.BankAccount;
import core.wallet.CashWallet;
import core.wallet.EWallet;
import core.wallet.Wallet;
import expensemanager.storage.CsvStorage;
import expensemanager.storage.Storage;

public class ConsoleView {
    private final ExpenseManager manager;
    private final Scanner scanner;
    private final Storage storage;

    public ConsoleView() {
        this.storage = new CsvStorage();
        this.manager = new ExpenseManager(storage);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== Expense Manager Console ===");
        System.out.println("1. Add wallet");
        System.out.println("2. Add category");
        System.out.println("3. Add transaction");
        System.out.println("4. Add budget");
        System.out.println("5. View transactions");
        System.out.println("6. View summary");
        System.out.println("7. Save to file");
        System.out.println("8. Load from file");
        System.out.println("0. Exit");

        boolean running = true;
        while (running) {
            System.out.print("\nChoose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> addWallet();
                    case "2" -> addCategory();
                    case "3" -> addTransaction();
                    case "4" -> addBudget();
                    case "5" -> viewTransactions();
                    case "6" -> viewSummary();
                    case "7" -> saveToFile();
                    case "8" -> loadFromFile();
                    case "0" -> {
                        running = false;
                        System.out.println("Goodbye!");
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void addWallet() {
        System.out.print("Wallet name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Initial balance: ");
        double balance = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Type (CASH/BANK/EWALLET): ");
        String type = scanner.nextLine().trim().toUpperCase();

        Wallet wallet = switch (type) {
            case "BANK" -> new BankAccount(name, balance, "Bank", "0000");
            case "EWALLET" -> new EWallet(name, balance, "Provider");
            default -> new CashWallet(name, balance);
        };
        try {
            manager.addWallet(wallet);
            System.out.println("Wallet added.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Error adding wallet: " + ex.getMessage());
        }
    }

    private void addCategory() {
        System.out.print("Category name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Type (INCOME/EXPENSE): ");
        String type = scanner.nextLine().trim().toUpperCase();
        Category category = new Category(name, TransactionType.valueOf(type));
        manager.getCategories().add(category);
        System.out.println("Category added.");
    }

    private void addTransaction() {
        if (manager.getWallets().isEmpty()) {
            System.out.println("Please add a wallet first.");
            return;
        }
        if (manager.getCategories().isEmpty()) {
            System.out.println("Please add a category first.");
            return;
        }

        System.out.print("Transaction ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Amount: ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Date (yyyy-mm-dd): ");
        LocalDate date = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Note: ");
        String note = scanner.nextLine().trim();
        System.out.print("Category name: ");
        String categoryName = scanner.nextLine().trim();
        System.out.print("Wallet name: ");
        String walletName = scanner.nextLine().trim();
        System.out.print("Type (INCOME/EXPENSE): ");
        String type = scanner.nextLine().trim().toUpperCase();
        System.out.print("Detail/source or payment method: ");
        String detail = scanner.nextLine().trim();

        Category category = findCategory(categoryName);
        Wallet wallet = findWallet(walletName);
        if (category == null || wallet == null) {
            System.out.println("Category or wallet not found.");
            return;
        }

        Transaction transaction = switch (type) {
            case "INCOME" -> new Income(id, amount, date, note, category, wallet, detail);
            case "EXPENSE" -> new Expense(id, amount, date, note, category, wallet, detail);
            default -> throw new IllegalArgumentException("Invalid transaction type");
        };

        manager.addTransaction(transaction);
        System.out.println("Transaction added.");
    }

    private void addBudget() {
        if (manager.getCategories().isEmpty()) {
            System.out.println("Please add an expense category first.");
            return;
        }

        System.out.print("Category name: ");
        String categoryName = scanner.nextLine().trim();
        Category category = findCategory(categoryName);
        if (category == null) {
            System.out.println("Category not found.");
            return;
        }
        if (category.getType() != TransactionType.EXPENSE) {
            System.out.println("Budget can only be created for expense categories.");
            return;
        }

        System.out.print("Limit: ");
        double limit = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Period (DAILY/WEEKLY/MONTHLY/YEARLY): ");
        String period = scanner.nextLine().trim().toUpperCase();

        Budget budget = new Budget(category, limit, Period.valueOf(period));
        manager.addBudget(category, budget);
        System.out.println("Budget added.");
    }

    private void viewTransactions() {
        if (manager.getTransactions().isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        for (Transaction transaction : manager.getTransactions()) {
            System.out.println("- " + transaction.getId() + " | " + transaction.getType() + " | " + transaction.getAmount() + " | " + transaction.getDate() + " | " + transaction.getNote());
        }
    }

    private void viewSummary() {
        System.out.print("Year: ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Month: ");
        int month = Integer.parseInt(scanner.nextLine().trim());
        Map<String, Double> summary = manager.monthlySummary(year, month);
        System.out.println("Income: " + summary.get("income"));
        System.out.println("Expenses: " + summary.get("expenses"));
        System.out.println("Net: " + summary.get("net"));
    }

    private void saveToFile() {
        System.out.print("File path: ");
        String path = scanner.nextLine().trim();
        try {
            manager.saveToStorage(path);
            System.out.println("Saved successfully.");
        } catch (IOException ex) {
            System.out.println("Save failed: " + ex.getMessage());
        }
    }

    private void loadFromFile() {
        System.out.print("File path: ");
        String path = scanner.nextLine().trim();
        try {
            manager.loadFromStorage(path);
            System.out.println("Loaded successfully.");
        } catch (IOException ex) {
            System.out.println("Load failed: " + ex.getMessage());
        }
    }

    private Category findCategory(String name) {
        return manager.getCategories().stream()
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private Wallet findWallet(String name) {
        return manager.getWallets().stream()
                .filter(wallet -> wallet.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
