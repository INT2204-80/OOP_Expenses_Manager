package expensemanager.ui;

import core.Budget;
import core.Category;
import core.Period;
import core.storage.BudgetDAO;
import core.storage.TransactionDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class BudgetsViewController {

    @FXML
    private FlowPane budgetsContainer;

    private BudgetDAO budgetDAO;
    private TransactionDAO transactionDAO;
    
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    public void initialize() {
        budgetDAO = new BudgetDAO();
        transactionDAO = new TransactionDAO();
        
        // Recalculate spent amounts based on current transactions
        budgetDAO.recalculateAllBudgetsSpentAmount();
        
        loadBudgets();
    }

    @FXML
    private void handleNavigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = budgetsContainer.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBudgets() {
        // Clear all except the "Create a New Budget" placeholder which should be the last or first.
        // Wait, since we need the placeholder to always be there, we can just clear and re-add it.
        budgetsContainer.getChildren().clear();
        
        List<Budget> budgets = budgetDAO.getAllBudgets();
        
        for (Budget budget : budgets) {
            VBox card = createBudgetCard(budget);
            budgetsContainer.getChildren().add(card);
        }
        
        // Add placeholder
        VBox placeholder = createPlaceholderCard();
        budgetsContainer.getChildren().add(placeholder);
    }

    private VBox createPlaceholderCard() {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll("budget-card", "add-budget-card");
        card.setOnMouseClicked(e -> showAddBudgetDialog());
        
        Label text = new Label("Take control of your expenses and\nsave more money with budgets!");
        text.getStyleClass().add("add-budget-text");
        text.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        text.setWrapText(true);
        
        Button btn = new Button("Create a New Budget");
        btn.getStyleClass().add("btn-primary");
        btn.setOnAction(e -> showAddBudgetDialog());
        
        card.getChildren().addAll(text, btn);
        return card;
    }

    private VBox createBudgetCard(Budget budget) {
        VBox card = new VBox(10);
        card.getStyleClass().add("budget-card");
        
        Label nameLabel = new Label(budget.getName());
        nameLabel.getStyleClass().add("budget-card-title");
        
        Label targetLabel = new Label("All wallets"); // Hardcoded as per discussion
        targetLabel.getStyleClass().add("budget-card-subtitle");
        
        VBox headerBox = new VBox(2, nameLabel, targetLabel);
        
        double left = budget.getRemainingAmount();
        boolean exceed = budget.isExceed();
        
        String curr = "VND";
        core.storage.WalletDAO wDao = new core.storage.WalletDAO();
        java.util.List<core.wallet.Wallet> ws = wDao.getAllWallets();
        if (!ws.isEmpty()) curr = ws.get(0).getCurrency();
        
        Label leftLabel = new Label(String.format("%,.2f %s %s", 
                exceed ? (budget.getCurrentSpent() - budget.getLimitAmount()) : left, curr,
                exceed ? "exceeded" : "left"));
        leftLabel.getStyleClass().add(exceed ? "budget-amount-left-exceed" : "budget-amount-left");
        
        Label fromLabel = new Label(String.format("From %,.2f %s", budget.getLimitAmount(), curr));
        fromLabel.getStyleClass().add("budget-amount-total");
        
        VBox amountBox = new VBox(2, leftLabel, fromLabel);
        
        // Progress bar
        ProgressBar progressBar = new ProgressBar(budget.getUsagePercentage() / 100.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().add(exceed ? "budget-progress-bar-exceed" : "budget-progress-bar");
        
        // Footer: Percentage and dates
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        
        Label percentLabel = new Label(String.format("%.1f%%", budget.getUsagePercentage()));
        percentLabel.getStyleClass().add("budget-percentage");
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        Label startDateLabel = new Label(budget.getStartDate().format(DATE_FORMATTER));
        startDateLabel.getStyleClass().add("budget-date");
        
        Region spacer2 = new Region();
        spacer2.setMinWidth(10);
        
        Label endDateLabel = new Label(budget.getEndDate() != null ? budget.getEndDate().format(DATE_FORMATTER) : "No end date");
        endDateLabel.getStyleClass().add("budget-date");
        
        footer.getChildren().addAll(percentLabel, spacer1, startDateLabel, spacer2, endDateLabel);
        
        // Spacers to push footer down
        Region vSpacer = new Region();
        VBox.setVgrow(vSpacer, Priority.ALWAYS);
        
        card.getChildren().addAll(headerBox, amountBox, progressBar, vSpacer, footer);
        
        // Allow deleting budget on right click or double click (simple implementation: double click)
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this budget?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        budgetDAO.deleteBudget(budget.getId());
                        loadBudgets();
                    }
                });
            }
        });
        
        return card;
    }

    @FXML
    private void showAddBudgetDialog() {
        Dialog<Budget> dialog = new Dialog<>();
        dialog.setTitle("Add New Budget");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Modify OK button to look like "Create a Budget"
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Create a Budget");
        okButton.getStyleClass().add("btn-primary");
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);
        
        // General Info
        Label generalInfoTitle = new Label("💰 General Info");
        generalInfoTitle.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
        
        GridPane generalGrid = new GridPane();
        generalGrid.setHgap(15);
        generalGrid.setVgap(10);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Budget Name");
        
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        
        ComboBox<String> currencyBox = new ComboBox<>();
        currencyBox.getItems().add("Vietnamese đồng");
        currencyBox.getSelectionModel().selectFirst();
        currencyBox.setDisable(true);
        
        generalGrid.add(new Label("Budget Name"), 0, 0);
        generalGrid.add(nameField, 0, 1, 2, 1);
        generalGrid.add(new Label("Amount"), 0, 2);
        generalGrid.add(amountField, 0, 3);
        generalGrid.add(new Label("Currency"), 1, 2);
        generalGrid.add(currencyBox, 1, 3);
        
        VBox generalBox = new VBox(10, generalInfoTitle, generalGrid);
        
        // Budget Filter
        Label filterTitle = new Label("⚡ Budget Filter");
        filterTitle.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
        
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().add("All categories");
        
        List<Category> allCategories = transactionDAO.getAllCategories();
        for (Category c : allCategories) {
            categoryBox.getItems().add(c.getName());
        }
        categoryBox.getSelectionModel().selectFirst();
        categoryBox.setMaxWidth(Double.MAX_VALUE);
        
        VBox filterBox = new VBox(10, filterTitle, new Label("Budgeted for"), categoryBox);
        
        // Budget Period
        Label periodTitle = new Label("📅 Budget Period");
        periodTitle.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
        
        ToggleGroup periodGroup = new ToggleGroup();
        HBox periodBoxH = new HBox(5);
        
        for (Period p : Period.values()) {
            ToggleButton tb = new ToggleButton(p.name().substring(0, 1) + p.name().substring(1).toLowerCase());
            tb.setToggleGroup(periodGroup);
            tb.setUserData(p);
            tb.setStyle("-fx-background-radius: 15; -fx-padding: 5 12;");
            if (p == Period.MONTHLY) tb.setSelected(true);
            periodBoxH.getChildren().add(tb);
        }
        
        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        
        VBox periodBox = new VBox(10, periodTitle, new Label("Recurrence"), periodBoxH, new Label("Start date"), startDatePicker);
        
        content.getChildren().addAll(generalBox, filterBox, periodBox);
        dialogPane.setContent(content);
        
        // Request focus on name field
        Platform.runLater(nameField::requestFocus);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    String name = nameField.getText();
                    double amount = Double.parseDouble(amountField.getText());
                    String catName = categoryBox.getValue();
                    
                    Category category = null;
                    if (!"All categories".equals(catName)) {
                        category = new Category(catName, core.TransactionType.EXPENSE);
                    }
                    
                    Period period = (Period) periodGroup.getSelectedToggle().getUserData();
                    LocalDate startDate = startDatePicker.getValue();
                    LocalDate endDate = startDate.plus(period.toJavaPeriod()).minusDays(1);
                    if (period == Period.ONCE) {
                        endDate = startDate.withDayOfMonth(startDate.lengthOfMonth()); // simple fallback
                    }
                    
                    return new Budget(0, name, amount, category, period, startDate); // period constructor handles endDate
                } catch (NumberFormatException e) {
                    // In a real app, show validation error
                    return null;
                }
            }
            return null;
        });
        
        Optional<Budget> result = dialog.showAndWait();
        result.ifPresent(budget -> {
            budgetDAO.addBudget(budget, -1);
            // After adding, recalculate to fetch existing transactions if any
            budgetDAO.recalculateBudgetSpentAmount(budget);
            budgetDAO.updateBudget(budget);
            loadBudgets();
        });
    }
}

