package expensemanager.ui.util;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import core.transaction.Transaction;

public final class TransactionFilter {

    private Predicate<Transaction> predicate = t -> true;

    public static TransactionFilter create() {
        return new TransactionFilter();
    }

    public TransactionFilter byPeriod(LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            predicate = predicate.and(t -> t != null && t.getDate() != null && !t.getDate().isBefore(start) && !t.getDate().isAfter(end));
        }
        return this;
    }

    public TransactionFilter byFutureMode(LocalDate referenceDate, boolean futureOnly) {
        if (futureOnly) {
            LocalDate today = referenceDate != null ? referenceDate : LocalDate.now();
            predicate = predicate.and(t -> t != null && t.getDate() != null && t.getDate().isAfter(today));
        }
        return this;
    }

    public TransactionFilter byCategoryName(String categoryName, String allCategoriesSentinel) {
        if (categoryName != null && !categoryName.equals(allCategoriesSentinel)) {
            predicate = predicate.and(t -> t != null && t.getCategory() != null && categoryName.equals(t.getCategory().getName()));
        }
        return this;
    }

    public TransactionFilter byNoteContains(String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            String needle = keyword.trim().toLowerCase();
            predicate = predicate.and(t -> t != null && t.getNote() != null && t.getNote().toLowerCase().contains(needle));
        }
        return this;
    }

    public TransactionFilter byMinAmount(Double min) {
        if (min != null) {
            predicate = predicate.and(t -> t != null && t.getAmount() >= min);
        }
        return this;
    }

    public TransactionFilter byMaxAmount(Double max) {
        if (max != null) {
            predicate = predicate.and(t -> t != null && t.getAmount() <= max);
        }
        return this;
    }

    public TransactionFilter and(Predicate<Transaction> extra) {
        predicate = predicate.and(extra);
        return this;
    }

    public List<Transaction> apply(List<Transaction> source) {
        if (source == null) {
            return List.of();
        }
        return source.stream().filter(predicate).collect(Collectors.toList());
    }
}