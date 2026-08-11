package expensemanager.ui.util;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import core.transaction.Transaction;

/**
 * Thay thế cho getFilteredTransactions() trong controller gốc.
 * <p>
 * Bản gốc gộp period + category + note + min/max amount vào MỘT method,
 * nên mỗi lần thêm tiêu chí lọc mới (vd. lọc theo ví, theo loại giao dịch)
 * là phải sửa lại method đó — vi phạm OCP. Ở đây mỗi tiêu chí là 1
 * Predicate độc lập, có thể cộng dồn (and) tuỳ ý, và test riêng lẻ được
 * mà không cần khởi tạo JavaFX UI.
 */
public final class TransactionFilter {

    private Predicate<Transaction> predicate = t -> true;

    public static TransactionFilter create() {
        return new TransactionFilter();
    }

    public TransactionFilter byPeriod(LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            predicate = predicate.and(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end));
        }
        return this;
    }

    public TransactionFilter byFutureMode(LocalDate referenceDate, boolean futureOnly) {
        if (futureOnly) {
            LocalDate today = referenceDate != null ? referenceDate : LocalDate.now();
            predicate = predicate.and(t -> t.getDate() != null && t.getDate().isAfter(today));
        }
        return this;
    }

    public TransactionFilter byCategoryName(String categoryName, String allCategoriesSentinel) {
        if (categoryName != null && !categoryName.equals(allCategoriesSentinel)) {
            predicate = predicate.and(t -> t.getCategory().getName().equals(categoryName));
        }
        return this;
    }

    public TransactionFilter byNoteContains(String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            String needle = keyword.trim().toLowerCase();
            predicate = predicate.and(t -> t.getNote() != null && t.getNote().toLowerCase().contains(needle));
        }
        return this;
    }

    public TransactionFilter byMinAmount(Double min) {
        if (min != null) {
            predicate = predicate.and(t -> t.getAmount() >= min);
        }
        return this;
    }

    public TransactionFilter byMaxAmount(Double max) {
        if (max != null) {
            predicate = predicate.and(t -> t.getAmount() <= max);
        }
        return this;
    }

    /** Cho phép gắn thêm tiêu chí tuỳ ý mà không cần sửa class này. */
    public TransactionFilter and(Predicate<Transaction> extra) {
        predicate = predicate.and(extra);
        return this;
    }

    public List<Transaction> apply(List<Transaction> source) {
        return source.stream().filter(predicate).collect(Collectors.toList());
    }
}
