package expensemanager.ui.util;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

/**
 * Nguon duy nhat cho viec anh xa ten mau (String, vd. "Blue") sang
 * {@link Color}, va cac cell factory dung chung cho ComboBox chon icon/mau.
 *
 * <p>Truoc khi tach class nay, logic switch-case tren cung mot bo 11 mau
 * (blue/red/green/yellow/purple/orange/pink/teal/indigo/cyan/gray) bi
 * lap lai 4 lan trong WalletViewController (initData, renderTransactions,
 * loadCategoriesToUI, showEditCategoryDialog), va 2 cell-factory (icon,
 * mau) bi lap lai y het nhau 2 lan. Vi pham DRY/SRP ro rang: moi lan can
 * them 1 mau moi phai sua o ca 4 noi.
 */
public final class ColorPalette {

    private ColorPalette() {}

    /** Danh sach ten mau hien thi trong ComboBox chon mau danh muc. */
    public static final String[] COLOR_NAMES = {
            "Blue", "Red", "Green", "Yellow", "Purple", "Orange",
            "Pink", "Teal", "Indigo", "Cyan", "Gray"
    };

    /** Danh sach icon (emoji) hien thi trong ComboBox chon icon danh muc. */
    public static final String[] CATEGORY_ICONS = {
            "\uD83D\uDCB0", "\uD83C\uDF74", "\uD83D\uDE97", "\uD83D\uDECD\uFE0F",
            "\uD83C\uDFE0", "\uD83C\uDFAE", "\uD83C\uDFE5", "\uD83D\uDCDA",
            "\u2708\uFE0F", "\uD83C\uDFAC", "\uD83D\uDC57", "\uD83D\uDC3E",
            "\uD83D\uDCF1", "\uD83C\uDF81"
    };

    /**
     * Quy doi ten mau (khong phan biet hoa/thuong) sang {@link Color}.
     *
     * @param colorName ten mau, co the null
     * @param fallback  mau tra ve neu colorName null/khong khop bat ky muc nao
     * @return mau tuong ung, hoac fallback
     */
    public static Color resolve(String colorName, Color fallback) {
        if (colorName == null) {
            return fallback;
        }
        switch (colorName.toLowerCase()) {
            case "blue":   return Color.web("#3b82f6");
            case "red":    return Color.web("#ef4444");
            case "green":  return Color.web("#10b981");
            case "yellow": return Color.web("#f59e0b");
            case "purple": return Color.web("#8b5cf6");
            case "orange": return Color.web("#f97316");
            case "pink":   return Color.web("#ec4899");
            case "teal":   return Color.web("#14b8a6");
            case "indigo": return Color.web("#6366f1");
            case "cyan":   return Color.web("#06b6d4");
            case "gray":   return Color.web("#64748b");
            default:       return fallback;
        }
    }

    /** Cell factory dung chung cho moi ComboBox&lt;String&gt; chon mau (chi ve 1 vong tron mau). */
    public static Callback<ListView<String>, ListCell<String>> colorCellFactory() {
        return param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(new Circle(7, resolve(item, Color.GRAY)));
                    setText(item);
                }
            }
        };
    }

    /** Cell factory dung chung cho moi ComboBox&lt;String&gt; chon icon (emoji). */
    public static Callback<ListView<String>, ListCell<String>> iconCellFactory() {
        return param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    Label iconLabel = new Label(item);
                    iconLabel.setStyle(
                            "-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif; "
                                    + "-fx-font-size: 16px; -fx-text-fill: #1e293b;");
                    setGraphic(iconLabel);
                }
            }
        };
    }
}
