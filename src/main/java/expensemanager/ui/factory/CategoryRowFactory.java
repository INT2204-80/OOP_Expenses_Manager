package expensemanager.ui.factory;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import core.Category;
import core.TransactionType;
import expensemanager.ui.util.ColorPalette;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Dung 1 dong hien thi danh muc trong tab Settings &gt; Categories.
 *
 * <p>Tach ra tu vong lap trong {@code WalletViewController.loadCategoriesToUI()}.
 * Luu y: nhan "0 transactions" duoc giu nguyen dung nhu ban goc (khong
 * thay bang so giao dich thuc te) vi ban goc chua bao gio cap nhat gia
 * tri nay - day la 1 tinh nang con dang do dang, khong phai loi do
 * refactor gay ra.
 */
public final class CategoryRowFactory {

    private CategoryRowFactory() {}

    public static HBox createCategoryRow(
            Category cat,
            BiConsumer<Category, Boolean> onToggleSelected,
            Consumer<Category> onEdit,
            Consumer<Category> onDelete) {

        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("category-list-item");

        CheckBox checkBox = new CheckBox();
        checkBox.setOnAction(e -> onToggleSelected.accept(cat, checkBox.isSelected()));

        Color fallback = cat.getType() == TransactionType.INCOME
                ? Color.web("#3b82f6") : Color.web("#f472b6");
        Color fxColor = ColorPalette.resolve(cat.getColor(), fallback);

        StackPane iconPane = new StackPane();
        iconPane.getChildren().add(new Circle(15, fxColor));
        if (cat.getIcon() != null && !cat.getIcon().isEmpty()) {
            Label iconLabel = new Label(cat.getIcon());
            iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            iconPane.getChildren().add(iconLabel);
        }

        Label nameLabel = new Label(cat.getName());
        nameLabel.getStyleClass().add("category-list-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLabel = new Label("0 transactions");
        countLabel.getStyleClass().add("category-list-count");

        Label actionLabel = new Label("\u2699");
        actionLabel.getStyleClass().add("category-list-action");
        actionLabel.setCursor(Cursor.HAND);
        actionLabel.setOnMouseClicked(e -> onEdit.accept(cat));

        Label deleteLabel = new Label("\uD83D\uDDD1");
        deleteLabel.getStyleClass().add("category-list-action-danger");
        deleteLabel.setCursor(Cursor.HAND);
        deleteLabel.setOnMouseClicked(e -> onDelete.accept(cat));

        item.getChildren().addAll(checkBox, iconPane, nameLabel, spacer, countLabel, actionLabel, deleteLabel);
        return item;
    }
}
