package core.storage;

import core.Category;
import core.TransactionType;
import core.WalletType;
import core.transaction.Expense;
import core.transaction.Income;
import core.transaction.RecurringExpense;
import core.transaction.Transaction;
import core.wallet.BankAccount;
import core.wallet.CashWallet;
import core.wallet.EWallet;
import core.wallet.Wallet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Luu va nap danh sach giao dich bang file CSV.
 *
 * <p>Moi dong CSV luu du thong tin giao dich, danh muc va vi. Lop nay
 * ho tro Income, Expense va RecurringExpense.
 */
public class CsvStorage implements Storage {
    private static final String HEADER = String.join(",",
            "recordType",
            "id",
            "amount",
            "date",
            "note",
            "categoryName",
            "categoryType",
            "categoryIcon",
            "categoryColor",
            "walletType",
            "walletName",
            "walletBalance",
            "currency",
            "bankName",
            "accountNumber",
            "provider",
            "source",
            "paymentMethod",
            "period",
            "passedPeriods",
            "recurringEndDate");

    private static final int COLUMN_COUNT = 21;

    /**
     * Luu danh sach giao dich xuong file CSV bang UTF-8.
     *
     * @param transactions danh sach giao dich can luu
     * @param path duong dan file CSV
     * @throws IOException neu khong the ghi file hoac du lieu khong hop le
     */
    @Override
    public void save(List<Transaction> transactions, String path)
            throws IOException {
        if (transactions == null) {
            throw new IllegalArgumentException(
                    "Transaction list cannot be null");
        }

        Path file = toPath(path);
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                file,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            writer.write(HEADER);
            writer.newLine();

            for (int index = 0; index < transactions.size(); index++) {
                Transaction transaction = transactions.get(index);
                if (transaction == null) {
                    throw new IOException(
                            "Transaction at index " + index + " is null");
                }
                writer.write(toCsvLine(transaction));
                writer.newLine();
            }
        }
    }

    /**
     * Nap danh sach giao dich tu file CSV bang UTF-8.
     *
     * @param path duong dan file CSV
     * @return danh sach giao dich da nap
     * @throws IOException neu file hoac du lieu CSV khong hop le
     */
    @Override
    public List<Transaction> load(String path) throws IOException {
        Path file = toPath(path);
        if (Files.notExists(file)) {
            return new ArrayList<>();
        }
        if (!Files.isRegularFile(file)) {
            throw new IOException("CSV path is not a file: " + path);
        }

        List<Transaction> transactions = new ArrayList<>();
        Map<String, Wallet> loadedWallets = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(
                file,
                StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return transactions;
            }
            // Allow legacy 16-column header
            int expectedCols = COLUMN_COUNT;
            if (header.split(",").length == 16) {
                expectedCols = 16;
            } else if (!HEADER.equals(header)) {
                throw new IOException("Invalid CSV header");
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                transactions.add(fromCsvLine(
                        line,
                        lineNumber,
                        loadedWallets,
                        expectedCols));
            }
        }

        return transactions;
    }

    private String toCsvLine(Transaction transaction) throws IOException {
        Category category = transaction.getCategory();
        Wallet wallet = transaction.getWallet();
        if (transaction.getDate() == null
                || category == null
                || wallet == null) {
            throw new IOException(
                    "Transaction date, category and wallet cannot be null");
        }

        String recordType;
        String source = "";
        String paymentMethod = "";
        String period = "";
        String passedPeriods = "0";
        String recurringEndDate = "";

        if (transaction instanceof RecurringExpense recurringExpense) {
            recordType = "RECURRING_EXPENSE";
            paymentMethod = text(recurringExpense.getPaymentMethod());
            if (recurringExpense.getPeriod() == null) {
                throw new IOException(
                        "Recurring expense period cannot be null");
            }
            period = recurringExpense.getPeriod().toString();
            passedPeriods = Integer.toString(recurringExpense.getPassedPeriods());
            if (recurringExpense.getEndDate() != null) {
                recurringEndDate = recurringExpense.getEndDate().toString();
            }
        } else if (transaction instanceof Income income) {
            recordType = "INCOME";
            source = text(income.getSource());
        } else if (transaction instanceof Expense expense) {
            recordType = "EXPENSE";
            paymentMethod = text(expense.getPaymentMethod());
        } else {
            throw new IOException(
                    "Unsupported transaction class: "
                            + transaction.getClass().getName());
        }

        String bankName = "";
        String accountNumber = "";
        String provider = "";
        if (wallet instanceof BankAccount bankAccount) {
            bankName = text(bankAccount.getBankName());
            accountNumber = text(bankAccount.getAccountNumber());
        } else if (wallet instanceof EWallet eWallet) {
            provider = text(eWallet.getProvider());
        } else if (!(wallet instanceof CashWallet)) {
            throw new IOException(
                    "Unsupported wallet class: "
                            + wallet.getClass().getName());
        }

        List<String> values = List.of(
                recordType,
                Integer.toString(transaction.getId()),
                Double.toString(transaction.getAmount()),
                transaction.getDate().toString(),
                text(transaction.getNote()),
                category.getName(),
                category.getType().name(),
                text(category.getIcon()),
                text(category.getColor()),
                wallet.getWalletType().name(),
                text(wallet.getName()),
                Double.toString(wallet.getBalance()),
                text(wallet.getCurrency()),
                bankName,
                accountNumber,
                provider,
                source,
                paymentMethod,
                period,
                passedPeriods,
                recurringEndDate);

        StringJoiner result = new StringJoiner(",");
        for (String value : values) {
            result.add(escape(value));
        }
        return result.toString();
    }

    private Transaction fromCsvLine(
            String line,
            int lineNumber,
            Map<String, Wallet> loadedWallets,
            int expectedCols) throws IOException {
        try {
            List<String> columns = parseLine(line);
            if (columns.size() != expectedCols) {
                throw new IllegalArgumentException(
                        "Expected " + expectedCols + " columns but got " + columns.size());
            }

            String recordType = columns.get(0);
            int id = Integer.parseInt(columns.get(1));
            double amount = Double.parseDouble(columns.get(2));
            LocalDate date = LocalDate.parse(columns.get(3));
            String note = columns.get(4);
            String categoryName = columns.get(5);
            TransactionType categoryType = TransactionType.valueOf(
                    columns.get(6));
                    
            String categoryIcon = "";
            String categoryColor = "";
            WalletType walletType;
            String walletName;
            double walletBalance;
            String currency = "VND";
            String bankName;
            String accountNumber;
            String provider;
            String source;
            String paymentMethod;
            String periodText;
            int passedPeriods = 0;
            String recurringEndDateStr = "";

            if (expectedCols == 16) {
                walletType = WalletType.valueOf(columns.get(7));
                walletName = columns.get(8);
                walletBalance = Double.parseDouble(columns.get(9));
                bankName = columns.get(10);
                accountNumber = columns.get(11);
                provider = columns.get(12);
                source = columns.get(13);
                paymentMethod = columns.get(14);
                periodText = columns.get(15);
            } else {
                categoryIcon = columns.get(7);
                categoryColor = columns.get(8);
                walletType = WalletType.valueOf(columns.get(9));
                walletName = columns.get(10);
                walletBalance = Double.parseDouble(columns.get(11));
                currency = columns.get(12);
                if (currency.isBlank()) currency = "VND";
                bankName = columns.get(13);
                accountNumber = columns.get(14);
                provider = columns.get(15);
                source = columns.get(16);
                paymentMethod = columns.get(17);
                periodText = columns.get(18);
                passedPeriods = columns.get(19).isBlank() ? 0 : Integer.parseInt(columns.get(19));
                recurringEndDateStr = columns.get(20);
            }

            if (id < 0) {
                throw new IllegalArgumentException("ID cannot be negative");
            }
            Transaction.validateAmount(amount);
            Wallet.validateAmount(walletBalance);
            validateRecordType(recordType, categoryType);

            Category category = new Category(categoryName, categoryType, categoryIcon, categoryColor);
            String walletKey = walletKey(
                    walletType,
                    walletName,
                    accountNumber,
                    provider);
            Wallet wallet = loadedWallets.get(walletKey);
            if (wallet == null) {
                wallet = createWallet(
                        walletType,
                        walletName,
                        walletBalance,
                        currency,
                        bankName,
                        accountNumber,
                        provider);
                loadedWallets.put(walletKey, wallet);
            } else if (Double.compare(
                    wallet.getBalance(), walletBalance) != 0) {
                throw new IllegalArgumentException(
                        "Inconsistent balance for the same wallet");
            }

            return createTransaction(
                    recordType,
                    id,
                    amount,
                    date,
                    note,
                    category,
                    wallet,
                    source,
                    paymentMethod,
                    periodText,
                    passedPeriods,
                    recurringEndDateStr);
        } catch (IllegalArgumentException exception) {
            throw new IOException(
                    "Invalid CSV data at line " + lineNumber,
                    exception);
        }
    }

    private Transaction createTransaction(
            String recordType,
            int id,
            double amount,
            LocalDate date,
            String note,
            Category category,
            Wallet realWallet,
            String source,
            String paymentMethod,
            String periodText,
            int passedPeriods,
            String recurringEndDateStr) {

        Transaction transaction = switch (recordType) {
            case "INCOME" -> new Income(
                    id,
                    amount,
                    date,
                    note,
                    category,
                    realWallet,
                    source);
            case "EXPENSE" -> new Expense(
                    id,
                    amount,
                    date,
                    note,
                    category,
                    realWallet,
                    paymentMethod);
            case "RECURRING_EXPENSE" -> {
                if (periodText.isBlank()) {
                    throw new IllegalArgumentException(
                            "Recurring period cannot be empty");
                }
                LocalDate endDate = recurringEndDateStr.isBlank() ? null : LocalDate.parse(recurringEndDateStr);
                RecurringExpense re = new RecurringExpense(
                        id,
                        amount,
                        date,
                        note,
                        category,
                        realWallet,
                        paymentMethod,
                        Period.parse(periodText),
                        endDate);
                re.setPassedPeriods(passedPeriods);
                yield re;
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported transaction type: " + recordType);
        };

        return transaction;
    }

    private Wallet createWallet(
            WalletType walletType,
            String name,
            double balance,
            String currency,
            String bankName,
            String accountNumber,
            String provider) {
        return switch (walletType) {
            case CASH -> new CashWallet(name, balance, currency);
            case BANK -> new BankAccount(
                    name,
                    balance,
                    currency,
                    bankName,
                    accountNumber);
            case EWALLET -> new EWallet(name, balance, currency, provider);
        };
    }

    private void validateRecordType(
            String recordType,
            TransactionType categoryType) {
        TransactionType expectedType;
        if ("INCOME".equals(recordType)) {
            expectedType = TransactionType.INCOME;
        } else if ("EXPENSE".equals(recordType)
                || "RECURRING_EXPENSE".equals(recordType)) {
            expectedType = TransactionType.EXPENSE;
        } else {
            throw new IllegalArgumentException(
                    "Unsupported transaction type: " + recordType);
        }

        if (categoryType != expectedType) {
            throw new IllegalArgumentException(
                    "Category type does not match transaction type");
        }
    }

    private String walletKey(
            WalletType type,
            String name,
            String accountNumber,
            String provider) {
        return switch (type) {
            case CASH -> type + "|" + name;
            case BANK -> type + "|" + accountNumber;
            case EWALLET -> type + "|" + provider + "|" + name;
        };
    }

    private List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (current == '"') {
                if (quoted
                        && index + 1 < line.length()
                        && line.charAt(index + 1) == '"') {
                    value.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }

        if (quoted) {
            throw new IllegalArgumentException("Unclosed quoted field");
        }
        values.add(value.toString());
        return values;
    }

    private String escape(String value) throws IOException {
        String safeValue = text(value);
        if (safeValue.indexOf('\n') >= 0
                || safeValue.indexOf('\r') >= 0) {
            throw new IOException(
                    "CSV text fields cannot contain line breaks");
        }
        if (safeValue.indexOf(',') >= 0
                || safeValue.indexOf('"') >= 0) {
            return '"'
                    + safeValue.replace("\"", "\"\"")
                    + '"';
        }
        return safeValue;
    }

    private Path toPath(String path) throws IOException {
        if (path == null || path.isBlank()) {
            throw new IOException("CSV path cannot be empty");
        }
        try {
            return Path.of(path);
        } catch (InvalidPathException exception) {
            throw new IOException("Invalid CSV path: " + path, exception);
        }
    }

    private String text(String value) {
        return value == null ? "" : value;
    }
}