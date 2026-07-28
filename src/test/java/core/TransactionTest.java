package core.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import core.TransactionType;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Kiem thu logic cua cac lop giao dich.
 */
class TransactionTest {

    /**
     * Kiem tra giao dich chap nhan so tien duong.
     */
    @Test
    void transactionAcceptsPositiveAmount() {
        Income income = new Income(
                1,
                100_000,
                LocalDate.now(),
                "Luong",
                "Thu nhap",
                "Ngan hang",
                "Cong ty");

        assertEquals(100_000, income.getAmount());
        assertEquals(TransactionType.INCOME, income.getType());
    }

    /**
     * Kiem tra constructor tu choi so tien am.
     */
    @Test
    void transactionRejectsNegativeAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Income(
                        1,
                        -100,
                        LocalDate.now(),
                        "Luong",
                        "Thu nhap",
                        "Ngan hang",
                        "Cong ty"));
    }


    /**
     * Kiem tra giao dich tu choi NaN va vo cuc.
     */
    @Test
    void transactionRejectsNonFiniteAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Transaction.validateAmount(Double.NaN));

        assertThrows(
                IllegalArgumentException.class,
                () -> Transaction.validateAmount(
                        Double.POSITIVE_INFINITY));

        assertThrows(
                IllegalArgumentException.class,
                () -> Transaction.validateAmount(
                        Double.NEGATIVE_INFINITY));
    }

    /**
     * Kiem tra setter khong cap nhat khi so tien khong hop le.
     */
    @Test
    void setAmountKeepsOldValueWhenNewValueIsInvalid() {
        Income income = new Income(
                1,
                100_000,
                LocalDate.now(),
                "Luong",
                "Thu nhap",
                "Ngan hang",
                "Cong ty");

        assertThrows(
                IllegalArgumentException.class,
                () -> income.setAmount(-100));

        assertEquals(100_000, income.getAmount());
    }

    /**
     * Kiem tra so tien co dau cua thu va chi.
     */
    @Test
    void signedAmountDependsOnTransactionType() {
        Income income = new Income(
                1,
                100_000,
                LocalDate.now(),
                "Luong",
                "Thu nhap",
                "Ngan hang",
                "Cong ty");

        Expense expense = new Expense(
                2,
                50_000,
                LocalDate.now(),
                "Mua hang",
                "Chi tieu",
                "Tien mat",
                "Cash");

        assertEquals(100_000, income.getSignedAmount());
        assertEquals(-50_000, expense.getSignedAmount());
    }
}