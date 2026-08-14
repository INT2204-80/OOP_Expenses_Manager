package core.storage;

import core.Category;
import core.TransactionType;
import core.transaction.Expense;
import core.transaction.Income;
import core.transaction.RecurringExpense;
import core.transaction.Transaction;
import core.wallet.BankAccount;
import core.wallet.CashWallet;
import core.wallet.EWallet;
import core.wallet.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kiem thu chuc nang luu va nap giao dich cua {@link CsvStorage}.
 */
class CsvStorageTest {
    private static final double DELTA = 0.001;

    @TempDir
    Path tempDirectory;

    /**
     * Kiem tra luu va nap du ba loai giao dich cung thong tin rieng.
     *
     * @throws IOException neu thao tac file that bai
     */
    @Test
    void savesAndLoadsAllSupportedTransactionTypes() throws IOException {
        EWallet incomeWallet = new EWallet(
                "Vi Momo",
                1_000,
                "Momo");
        CashWallet expenseWallet = new CashWallet(
                "Tien mat",
                1_000);
        BankAccount recurringWallet = new BankAccount(
                "Tai khoan VCB",
                5_000,
                "VCB",
                "123456");

        Income income = new Income(
                1,
                400,
                LocalDate.of(2026, 7, 20),
                "Luong",
                new Category("Luong", TransactionType.INCOME),
                incomeWallet,
                "Cong ty");
        Expense expense = new Expense(
                2,
                150,
                LocalDate.of(2026, 7, 21),
                "An trua",
                new Category("An uong", TransactionType.EXPENSE),
                expenseWallet,
                "Cash");
        RecurringExpense recurringExpense = new RecurringExpense(
                3,
                1_000,
                LocalDate.of(2026, 7, 1),
                "Tien nha",
                new Category("Nha o", TransactionType.EXPENSE),
                recurringWallet,
                "Bank Transfer",
                Period.ofMonths(1));

        Path file = tempDirectory.resolve("transactions.csv");
        CsvStorage storage = new CsvStorage();
        storage.save(
                List.of(income, expense, recurringExpense),
                file.toString());

        List<Transaction> loaded = storage.load(file.toString());

        assertEquals(3, loaded.size());

        Income loadedIncome = assertInstanceOf(
                Income.class,
                loaded.get(0));
        assertEquals(1, loadedIncome.getId());
        assertEquals(400, loadedIncome.getAmount(), DELTA);
        assertEquals(LocalDate.of(2026, 7, 20), loadedIncome.getDate());
        assertEquals("Luong", loadedIncome.getNote());
        assertEquals("Luong", loadedIncome.getCategory().getName());
        assertEquals(
                TransactionType.INCOME,
                loadedIncome.getCategory().getType());
        assertEquals("Cong ty", loadedIncome.getSource());
        EWallet loadedEWallet = assertInstanceOf(
                EWallet.class,
                loadedIncome.getWallet());
        assertEquals("Vi Momo", loadedEWallet.getName());
        assertEquals("Momo", loadedEWallet.getProvider());
        assertEquals(1_000, loadedEWallet.getBalance(), DELTA);

        Expense loadedExpense = assertInstanceOf(
                Expense.class,
                loaded.get(1));
        assertEquals("Cash", loadedExpense.getPaymentMethod());
        assertEquals("An uong", loadedExpense.getCategory().getName());
        assertEquals(
                1_000,
                loadedExpense.getWallet().getBalance(),
                DELTA);

        RecurringExpense loadedRecurring = assertInstanceOf(
                RecurringExpense.class,
                loaded.get(2));
        assertEquals(
                Period.ofMonths(1),
                loadedRecurring.getPeriod());
        assertEquals(
                "Bank Transfer",
                loadedRecurring.getPaymentMethod());
        BankAccount loadedBankAccount = assertInstanceOf(
                BankAccount.class,
                loadedRecurring.getWallet());
        assertEquals("VCB", loadedBankAccount.getBankName());
        assertEquals("123456", loadedBankAccount.getAccountNumber());
        assertEquals(5_000, loadedBankAccount.getBalance(), DELTA);
    }

    /**
     * Kiem tra ky tu CSV dac biet va moi quan he dung chung vi.
     *
     * @throws IOException neu thao tac file that bai
     */
    @Test
    void preservesSpecialCharactersAndSharedWallet() throws IOException {
        CashWallet wallet = new CashWallet("Vi, chung", 1_000);
        Income income = new Income(
                10,
                200,
                LocalDate.of(2026, 7, 30),
                "Thu, \"thuong\" thang 7",
                new Category("Luong, thuong", TransactionType.INCOME),
                wallet,
                "Cong ty, \"ABC\"");
        Expense expense = new Expense(
                11,
                100,
                LocalDate.of(2026, 7, 31),
                "Mua, \"do\"",
                new Category("Mua sam", TransactionType.EXPENSE),
                wallet,
                "Tien mat");

        Path file = tempDirectory.resolve("special.csv");
        CsvStorage storage = new CsvStorage();
        storage.save(List.of(income, expense), file.toString());

        List<Transaction> loaded = storage.load(file.toString());
        Income loadedIncome = assertInstanceOf(
                Income.class,
                loaded.get(0));
        Expense loadedExpense = assertInstanceOf(
                Expense.class,
                loaded.get(1));

        assertEquals(
                "Thu, \"thuong\" thang 7",
                loadedIncome.getNote());
        assertEquals("Cong ty, \"ABC\"", loadedIncome.getSource());
        assertEquals("Mua, \"do\"", loadedExpense.getNote());
        assertSame(loadedIncome.getWallet(), loadedExpense.getWallet());
        assertEquals(
                1_000,
                loadedIncome.getWallet().getBalance(),
                DELTA);
    }

    /**
     * Kiem tra file chua ton tai tra ve danh sach rong.
     *
     * @throws IOException neu duong dan khong hop le
     */
    @Test
    void returnsEmptyListWhenFileDoesNotExist() throws IOException {
        Path missingFile = tempDirectory.resolve("missing.csv");

        List<Transaction> loaded = new CsvStorage().load(
                missingFile.toString());

        assertTrue(loaded.isEmpty());
    }

    /**
     * Kiem tra save ghi de noi dung cu thay vi noi them ban ghi.
     *
     * @throws IOException neu thao tac file that bai
     */
    @Test
    void saveOverwritesExistingFile() throws IOException {
        CashWallet wallet = new CashWallet("Tien mat", 1_000);
        Income income = new Income(
                20,
                100,
                LocalDate.of(2026, 7, 31),
                "Thu nhap",
                new Category("Luong", TransactionType.INCOME),
                wallet,
                "Cong ty");
        Path file = tempDirectory.resolve("overwrite.csv");
        CsvStorage storage = new CsvStorage();

        storage.save(List.of(income), file.toString());
        storage.save(List.of(), file.toString());

        assertTrue(storage.load(file.toString()).isEmpty());
    }

    /**
     * Kiem tra du lieu CSV sai dinh dang duoc bao bang IOException.
     *
     * @throws IOException neu khong the tao file kiem thu
     */
    @Test
    void rejectsMalformedCsvData() throws IOException {
        String header = "recordType,id,amount,date,note,categoryName,"
                + "categoryType,walletType,walletName,walletBalance,"
                + "bankName,accountNumber,provider,source,"
                + "paymentMethod,period";
        String invalidRecord = "INCOME,abc,100,2026-07-31,Luong,"
                + "Luong,INCOME,CASH,Tien mat,1000,,,,Cong ty,,";
        Path file = tempDirectory.resolve("invalid.csv");
        Files.writeString(
                file,
                header + System.lineSeparator() + invalidRecord,
                StandardCharsets.UTF_8);

        IOException exception = assertThrows(
                IOException.class,
                () -> new CsvStorage().load(file.toString()));

        assertTrue(exception.getMessage().contains("line 2"));
        assertInstanceOf(NumberFormatException.class, exception.getCause());
    }
}