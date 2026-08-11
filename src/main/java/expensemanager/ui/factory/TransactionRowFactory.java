package expensemanager.ui.factory;

import java.util.function.Consumer;

import core.transaction.Income;
import core.transaction.Transaction;
import expensemanager.ui.util.ColorPalette;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Dung 1 dong hien thi giao dich trong danh sach Transactions.
 *
 * <p>Truoc day logic nay nam thang trong vong lap cua
 * {@code WalletViewController.renderTransactions()} (~60 dong), khien
 * controller vua phai render UI vua giu callback sua/xoa. Tach ra day de
 * controller chi con truyen callback vao, giong pattern da co san cua
 * {@link WalletCardFactory}.
 */
public final class TransactionRowFactory {

    private TransactionRowFactory() {}

    public static HBox createTransactionRow(Transaction t, Consumer<Transaction> onEdit, Consumer<Transaction> onDelete) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        boolean isIncome = t instanceof Income;
        Color fallback = Color.web(isIncome ? "#2563eb" : "#ef4444");
        Color fxColor = ColorPalette.resolve(t.getCategory().getColor(), fallback);

        StackPane iconPane = new StackPane();
        iconPane.getChildren().add(new Circle(20, fxColor));
        if (t.getCategory().getIcon() != null && !t.getCategory().getIcon().isEmpty()) {
            Label iconLabel = new Label(t.getCategory().getIcon());
            iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
            iconPane.getChildren().add(iconLabel);
        }

        VBox infoBox = new VBox(5);
        Label catLabel = new Label(t.getCategory().getName());
        catLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        Label noteLabel = new Label(t.getNote() != null ? t.getNote() : "");
        noteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        Label dateLabel = new Label(t.getDate().toString());
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        infoBox.getChildren().addAll(catLabel, noteLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = new Label(String.format("%s%,.0f VND", isIncome ? "+" : "-", t.getAmount()));
        amountLabel.setStyle(String.format(
                "-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: %s;",
                isIncome ? "#2563eb" : "#ef4444"));

        Button editBtn = new Button("S\u1eeda");
        editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #3b82f6; -fx-cursor: hand; "
                + "-fx-font-size: 12px; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> onEdit.accept(t));

        Button deleteBtn = new Button("X\u00f3a");
        deleteBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-cursor: hand; "
                + "-fx-font-size: 12px; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> onDelete.accept(t));

        row.getChildren().addAll(iconPane, infoBox, spacer, amountLabel, editBtn, deleteBtn);
        return row;
    }
}
