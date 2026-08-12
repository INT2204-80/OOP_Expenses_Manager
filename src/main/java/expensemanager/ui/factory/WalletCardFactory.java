package expensemanager.ui.factory;

import java.util.function.Consumer;

import core.wallet.BankAccount;
import core.wallet.EWallet;
import core.wallet.Wallet;
import expensemanager.ui.util.MoneyFormat;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class WalletCardFactory {

    public static HBox createWalletCard(Wallet w, Consumer<Wallet> onDelete, Consumer<Wallet> onClick) {
        if (w == null) {
            return new HBox();
        }
        
        HBox card = new HBox(15);
        card.getStyleClass().add("wallet-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(300);

        // SVG Icon
        SVGPath icon = new SVGPath();
        if (w instanceof BankAccount) {
            icon.setContent("M4 10h3v7H4zm6.5 0h3v7h-3zM2 19h20v3H2zm15-9h3v7h-3zm-15-4 10-5 10 5v2H2z");
            icon.setFill(Color.web("#a0aec0"));
        } else {
            icon.setContent("M21 7.28V5c0-1.1-.9-2-2-2H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2v-2.28A2 2 0 0 0 22 15V9a2 2 0 0 0-1-1.72zM20 9v6h-7V9h7zM5 19V5h14v2h-6c-1.1 0-2 .9-2 2v6c0 1.1.9 2 2 2h6v2H5z");
            icon.setFill(Color.web("#8B5A2B"));
        }
        icon.setScaleX(1.2);
        icon.setScaleY(1.2);

        // Details
        VBox details = new VBox();
        details.setAlignment(Pos.CENTER_LEFT);

        String walletName = (w.getName() != null) ? w.getName() : "Ví không tên";
        Label nameLbl = new Label(walletName);
        nameLbl.getStyleClass().add("wallet-name");

        String typeStr = switch (w) {
            case BankAccount ba -> "Bank Account (" + (ba.getBankName() != null ? ba.getBankName() : "") + ")";
            case EWallet ew    -> "E-Wallet (" + (ew.getProvider() != null ? ew.getProvider() : "") + ")";
            default            -> "Cash Wallet";
        };

        Label typeLbl = new Label(typeStr);
        typeLbl.getStyleClass().add("wallet-type");
        typeLbl.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 11px;");

        Label balLbl = new Label(MoneyFormat.format(w.getBalance()));
        balLbl.getStyleClass().add("wallet-balance");

        details.getChildren().addAll(nameLbl, typeLbl, balLbl);
        VBox.setMargin(details, new Insets(0, 0, 0, 10));

        // Delete Button
        Button deleteBtn = new Button("\uD83D\uDDD1");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-font-size: 16px;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this wallet?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) onDelete.accept(w);
            });
            e.consume();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(icon, details, spacer, deleteBtn);
        card.setStyle("-fx-cursor: hand;");
        
        // BẮT SỰ KIỆN CLICK AN TOÀN + IN LOG KIỂM TRA
        card.setOnMouseClicked(e -> {
            if (onClick != null) {
                System.out.println(" Đã click vào ví: " + w.getName());
                onClick.accept(w);
            } else {
                System.err.println("Callback onClick bị null!");
            }
        });

        return card;
    }
}