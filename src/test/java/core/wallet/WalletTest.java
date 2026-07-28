package core.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import core.WalletType;
import org.junit.jupiter.api.Test;

/**
 * Kiem thu logic nap, rut va quan ly thong tin cua cac loai vi.
 */
class WalletTest {
    private static final double DELTA = 0.001;

    /**
     * Kiem tra khoi tao va cap nhat ten vi.
     */
    @Test
    void walletNameCanBeUpdated() {
        CashWallet wallet = new CashWallet("Tien mat", 10_000);

        assertEquals("Tien mat", wallet.getName());

        wallet.setName("Tien mat ca nhan");

        assertEquals("Tien mat ca nhan", wallet.getName());
    }

    /**
     * Kiem tra nap tien hop le lam tang so du.
     */
    @Test
    void depositIncreasesBalance() {
        CashWallet wallet = new CashWallet("Tien mat", 10_000);

        wallet.deposit(2_500);

        assertEquals(12_500, wallet.getBalance(), DELTA);
    }

    /**
     * Kiem tra vi tu choi so du ban dau khong hop le.
     */
    @Test
    void walletRejectsInvalidInitialBalance() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CashWallet("So du am", -1));

        assertThrows(
                IllegalArgumentException.class,
                () -> new CashWallet("So du NaN", Double.NaN));

        assertThrows(
                IllegalArgumentException.class,
                () -> new CashWallet("So du vo cuc", Double.POSITIVE_INFINITY));
    }

    /**
     * Kiem tra nap tien khong hop le khong lam thay doi so du.
     */
    @Test
    void walletRejectsInvalidDeposit() {
        CashWallet wallet = new CashWallet("Tien mat", 10_000);

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.deposit(-1));

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.deposit(Double.NaN));

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.deposit(Double.POSITIVE_INFINITY));

        assertEquals(10_000, wallet.getBalance(), DELTA);
    }

    /**
     * Kiem tra cap nhat so du khong hop le bi tu choi.
     */
    @Test
    void walletRejectsInvalidBalanceUpdate() {
        CashWallet wallet = new CashWallet("Tien mat", 10_000);

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.setBalance(-1));

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.setBalance(Double.NaN));

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.setBalance(Double.NEGATIVE_INFINITY));

        assertEquals(10_000, wallet.getBalance(), DELTA);
    }

    /**
     * Kiem tra vi tien mat rut tien khong mat phi.
     */
    @Test
    void cashWalletWithdrawsWithoutFee() {
        CashWallet wallet = new CashWallet("Tien mat", 10_000);

        wallet.withdraw(2_500);

        assertEquals(7_500, wallet.getBalance(), DELTA);
        assertEquals(WalletType.CASH, wallet.getWalletType());
    }

    /**
     * Kiem tra vi tien mat tu choi giao dich rut khong hop le.
     */
    @Test
    void cashWalletRejectsInvalidWithdrawal() {
        CashWallet wallet = new CashWallet("Tien mat", 10_000);

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.withdraw(-1));

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.withdraw(0));

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.withdraw(Double.NaN));

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.withdraw(Double.POSITIVE_INFINITY));

        assertThrows(
                IllegalStateException.class,
                () -> wallet.withdraw(10_001));

        assertEquals(10_000, wallet.getBalance(), DELTA);
    }

    /**
     * Kiem tra tai khoan ngan hang rut tien khong mat phi.
     */
    @Test
    void bankAccountWithdrawsWithoutFee() {
        BankAccount account =
                new BankAccount("Tai khoan", 10_000, "VCB", "123456");

        account.withdraw(3_000);

        assertEquals(7_000, account.getBalance(), DELTA);
        assertEquals(WalletType.BANK, account.getWalletType());
    }

    /**
     * Kiem tra tai khoan ngan hang co the rut het so du.
     */
    @Test
    void bankAccountCanWithdrawEntireBalance() {
        BankAccount account =
                new BankAccount("Tai khoan", 10_000, "VCB", "123456");

        account.withdraw(10_000);

        assertEquals(0, account.getBalance(), DELTA);
    }

    /**
     * Kiem tra tai khoan ngan hang tu choi rut qua so du.
     */
    @Test
    void bankAccountRejectsWithdrawalOverBalance() {
        BankAccount account =
                new BankAccount("Tai khoan", 10_000, "VCB", "123456");

        assertThrows(
                IllegalStateException.class,
                () -> account.withdraw(10_001));

        assertEquals(10_000, account.getBalance(), DELTA);
    }

    /**
     * Kiem tra getter va setter cua thong tin ngan hang.
     */
    @Test
    void bankAccountInformationCanBeUpdated() {
        BankAccount account =
                new BankAccount("Tai khoan", 10_000, "VCB", "123456");

        assertEquals("VCB", account.getBankName());
        assertEquals("123456", account.getAccountNumber());

        account.setBankName("BIDV");
        account.setAccountNumber("987654");

        assertEquals("BIDV", account.getBankName());
        assertEquals("987654", account.getAccountNumber());
    }

    /**
     * Kiem tra vi dien tu rut tien khong mat phi.
     */
    @Test
    void eWalletWithdrawsWithoutFee() {
        EWallet wallet = new EWallet("Vi Momo", 10_000, "Momo");

        wallet.withdraw(2_500);

        assertEquals(7_500, wallet.getBalance(), DELTA);
        assertEquals("Momo", wallet.getProvider());
        assertEquals(WalletType.EWALLET, wallet.getWalletType());
    }

    /**
     * Kiem tra nha cung cap vi dien tu co the duoc cap nhat.
     */
    @Test
    void eWalletProviderCanBeUpdated() {
        EWallet wallet = new EWallet("Vi dien tu", 10_000);

        assertNull(wallet.getProvider());

        wallet.setProvider("ZaloPay");

        assertEquals("ZaloPay", wallet.getProvider());
    }

    /**
     * Kiem tra vi dien tu tu choi rut qua so du.
     */
    @Test
    void eWalletRejectsWithdrawalOverBalance() {
        EWallet wallet = new EWallet("Vi Momo", 10_000, "Momo");

        assertThrows(
                IllegalStateException.class,
                () -> wallet.withdraw(10_001));

        assertEquals(10_000, wallet.getBalance(), DELTA);
    }
}