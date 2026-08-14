package expensemanager.ui.factory;

import java.util.function.Consumer;

import core.transaction.Income;
import core.transaction.PendingRecurringOccurrence;
import core.transaction.RecurringExpense;
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

    /**
     * Giu lai overload cu de khong pha code goi hien co (che do hien thi binh thuong).
     */
    public static HBox createTransactionRow(Transaction t, Consumer<Transaction> onEdit, Consumer<Transaction> onDelete) {
        return createTransactionRow(t, onEdit, onDelete, false);
    }

    /**
     * @param futureRecurringDisplay neu true VA t la RecurringExpense: an ngay khoi tao,
     *                                thay bang ky han tiep theo (nextDueDate()). Dung cho
     *                                khoi "Dinh ky sap toi" trong tab Future.
     */
    public static HBox createTransactionRow(Transaction t, Consumer<Transaction> onEdit, Consumer<Transaction> onDelete,
                                             boolean futureRecurringDisplay) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        boolean isIncome = t instanceof Income;
        boolean isPending = t instanceof PendingRecurringOccurrence;
        Color fallback = Color.web(isIncome ? "#2563eb" : "#ef4444");
        Color fxColor = ColorPalette.resolve(t.getCategory().getColor(), fallback);

        StackPane iconPane = new StackPane();
        iconPane.getChildren().add(new Circle(20, fxColor));
        if (t.getCategory().getIcon() != null && !t.getCategory().getIcon().isEmpty()) {
            Label iconLabel = new Label(t.getCategory().getIcon());
            iconLabel.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif; -fx-text-fill: white; -fx-font-size: 18px;");
            iconPane.getChildren().add(iconLabel);
        }

        VBox infoBox = new VBox(5);
        Label catLabel = new Label(t.getCategory().getName());
        catLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        Label noteLabel = new Label(t.getNote() != null ? t.getNote() : "");
        noteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        Label dateLabel;
        if (futureRecurringDisplay && t instanceof RecurringExpense) {
            // Giao dich lap lai trong khoi "Dinh ky sap toi": khong hien ngay khoi tao,
            // chi hien ky han tiep theo.
            RecurringExpense re = (RecurringExpense) t;
            java.time.LocalDate nextDate = re.nextDueDate();
            if (nextDate != null) {
                dateLabel = new Label("K\u1ef3 ti\u1ebfp theo: " + nextDate.toString());
            } else {
                dateLabel = new Label("\u0110\u00e3 k\u1ebft th\u00fac");
            }
        } else if (isPending) {
            // Occurrence ao cua giao dich lap lai, chua toi han trong ky dang xem.
            dateLabel = new Label("D\u1ef1 ki\u1ebfn: " + t.getDate().toString());
        } else {
            dateLabel = new Label(t.getDate().toString());
        }
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        infoBox.getChildren().addAll(catLabel, noteLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = new Label(String.format("%s%,.0f VND", isIncome ? "+" : "-", t.getAmount()));
        amountLabel.setStyle(String.format(
                "-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: %s;",
                isIncome ? "#2563eb" : "#ef4444"));

        if (isPending) {
            // Occurrence ao: khong phai giao dich that trong DB, khong cho Sua/Xoa.
            row.setStyle("-fx-background-color: #fffbeb; -fx-padding: 15; -fx-background-radius: 8; "
                    + "-fx-border-color: #fcd34d; -fx-border-width: 1; -fx-border-style: dashed; -fx-border-radius: 8;");

            Label pendingBadge = new Label("Pending");
            pendingBadge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-font-size: 10px; "
                    + "-fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");

            row.getChildren().addAll(iconPane, infoBox, spacer, amountLabel, pendingBadge);
        } else {
            Button editBtn = new Button("S\u1eeda");
            editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #3b82f6; -fx-cursor: hand; "
                    + "-fx-font-size: 12px; -fx-font-weight: bold;");
            editBtn.setOnAction(e -> onEdit.accept(t));

            Button deleteBtn = new Button("X\u00f3a");
            deleteBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-cursor: hand; "
                    + "-fx-font-size: 12px; -fx-font-weight: bold;");
            deleteBtn.setOnAction(e -> onDelete.accept(t));

            row.getChildren().addAll(iconPane, infoBox, spacer, amountLabel, editBtn, deleteBtn);
        }

        return row;
    }
}
