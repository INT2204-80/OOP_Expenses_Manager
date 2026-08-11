package expensemanager.ui.factory;

import java.time.LocalDate;

import core.Budget;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Dung 1 card ngan sach (Budget) va trang thai rong cho tab Budgets.
 *
 * <p>NOTE cho nguoi review: cay thu muc project cua ban da co san 1 file
 * ten {@code BudgetCardFactory.java} trong {@code ui/factory} (thay o
 * anh chup man hinh). Toi chua co noi dung file do nen tao class nay
 * doc lap - vui long doi chieu/hop nhat 2 file truoc khi dung, tranh
 * trung ten class gay loi bien dich.
 */
public final class BudgetCardFactory {

    private BudgetCardFactory() {}

    public static VBox createBudgetCard(Budget budget, Runnable onDeleteClicked) {
        VBox card = new VBox();
        card.setSpacing(20);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Ng\u00e2n s\u00e1ch > " + budget.getName() + "\n"
                + (budget.getCategory() != null ? budget.getCategory().getName() : "T\u1ea5t c\u1ea3"));
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Button editBtn = new Button("X\u00f3a ng\u00e2n s\u00e1ch");
        editBtn.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-background-radius: 5;");
        editBtn.setOnAction(e -> onDeleteClicked.run());
        header.getChildren().addAll(title, spacer1, editBtn);

        // Stats
        HBox statsBox = new HBox();
        statsBox.setSpacing(15);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.getChildren().addAll(
                createStatCard("Originally Budgeted", String.format("+%,.0f VND", budget.getLimitAmount()), "#4caf50"),
                createStatCard("Chi ti\u00eau g\u1ea7n \u0111\u00e2y", String.format("-%,.0f VND", budget.getCurrentSpent()), "#f44336"),
                createStatCard("Money left",
                        String.format("%s%,.0f VND", budget.getRemainingAmount() >= 0 ? "+" : "", budget.getRemainingAmount()),
                        budget.getRemainingAmount() >= 0 ? "#4caf50" : "#f44336"),
                createStatCard("You can spend",
                        String.format("%,.0f VND/Day", budget.calcDailyAllowance(LocalDate.now())), "#2196f3"));

        // Progress
        VBox progressBox = new VBox();
        progressBox.setSpacing(10);

        Label progressTitle = new Label("Ti\u1ebfn \u0111\u1ed9 ng\u00e2n s\u00e1ch");
        progressTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Label progressDesc = new Label("Keep spending. You can spend "
                + String.format("%,.0f VND", budget.calcDailyAllowance(LocalDate.now()))
                + " each day for the rest of the period.");

        ProgressBar pBar = new ProgressBar(budget.getUsagePercentage() / 100.0);
        pBar.setMaxWidth(Double.MAX_VALUE);
        pBar.setPrefHeight(20);
        pBar.setStyle(budget.isExceed() ? "-fx-accent: #f44336;" : "-fx-accent: #4caf50;");

        HBox datesBox = new HBox();
        Label startDateLbl = new Label(budget.getStartDate() != null ? budget.getStartDate().toString() : "");
        startDateLbl.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 12;");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        Label endDateLbl = new Label(budget.getEndDate() != null ? budget.getEndDate().toString() : "");
        endDateLbl.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 12;");
        datesBox.getChildren().addAll(startDateLbl, spacer2, endDateLbl);

        progressBox.getChildren().addAll(progressTitle, progressDesc, pBar, datesBox);

        card.getChildren().addAll(header, statsBox, progressBox);
        return card;
    }

    public static VBox createEmptyState(Runnable onCreateClicked) {
        VBox emptyBox = new VBox();
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setSpacing(15);
        emptyBox.getStyleClass().add("budget-empty-box");

        Label l1 = new Label("Take control of your expenses and");
        l1.getStyleClass().add("budget-text");
        Label l2 = new Label("save more money with budgets!");
        l2.getStyleClass().add("budget-text");

        Button btn = new Button("Create a New Budget");
        btn.getStyleClass().add("solid-button");
        btn.setOnAction(e -> onCreateClicked.run());
        VBox.setMargin(btn, new Insets(10, 0, 0, 0));

        emptyBox.getChildren().addAll(l1, l2, btn);
        return emptyBox;
    }

    private static VBox createStatCard(String title, String value, String valueColor) {
        VBox box = new VBox();
        box.setSpacing(5);
        box.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 15; -fx-background-radius: 5;");
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);

        Label tLbl = new Label(title);
        tLbl.setStyle("-fx-text-fill: #757575; -fx-font-size: 12;");

        Label vLbl = new Label(value);
        vLbl.setStyle("-fx-text-fill: " + valueColor + "; -fx-font-size: 16; -fx-font-weight: bold;");

        box.getChildren().addAll(tLbl, vLbl);
        return box;
    }
}
