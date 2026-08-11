package expensemanager.ui.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;

import expensemanager.ui.util.DashboardDialogHelper;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.util.Pair;

public class PeriodFilterManager {
    private LocalDate currentPeriodStart = LocalDate.now().withDayOfMonth(1);
    private LocalDate currentPeriodEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
    private boolean futureOnly = false;

    public LocalDate getStart() {
        return currentPeriodStart;
    }

    public LocalDate getEnd() {
        return currentPeriodEnd;
    }

    public boolean isFutureOnly() {
        return futureOnly;
    }

    public void setFutureOnly(boolean futureOnly) {
        this.futureOnly = futureOnly;
    }

    public void handlePrevPeriod(Runnable onPeriodChanged) {
        long days = ChronoUnit.DAYS.between(currentPeriodStart, currentPeriodEnd) + 1;
        currentPeriodStart = currentPeriodStart.minusDays(days);
        currentPeriodEnd = currentPeriodEnd.minusDays(days);
        if (onPeriodChanged != null) {
            onPeriodChanged.run();
        }
    }

    public void handleNextPeriod(Runnable onPeriodChanged) {
        long days = ChronoUnit.DAYS.between(currentPeriodStart, currentPeriodEnd) + 1;
        currentPeriodStart = currentPeriodStart.plusDays(days);
        currentPeriodEnd = currentPeriodEnd.plusDays(days);
        if (onPeriodChanged != null) {
            onPeriodChanged.run();
        }
    }

    public void handleCustomPeriod(Runnable onPeriodChanged) {
        Optional<Pair<LocalDate, LocalDate>> result =
                DashboardDialogHelper.showCustomPeriodDialog(currentPeriodStart, currentPeriodEnd);

        result.ifPresent(pair -> {
            if (pair.getKey() == null || pair.getValue() == null) {
                return;
            }
            if (pair.getKey().isAfter(pair.getValue())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Ngày bắt đầu không thể lớn hơn ngày kết thúc!");
                alert.showAndWait();
            } else {
                currentPeriodStart = pair.getKey();
                currentPeriodEnd = pair.getValue();
                if (onPeriodChanged != null) {
                    onPeriodChanged.run();
                }
            }
        });
    }

    public void updatePeriodLabels(Label periodLabelOverview, Label periodLabelTrans) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
        String labelText = currentPeriodStart.format(dtf) + " - " + currentPeriodEnd.format(dtf);
        if (periodLabelOverview != null) periodLabelOverview.setText(labelText);
        if (periodLabelTrans != null) periodLabelTrans.setText(labelText);
    }
}