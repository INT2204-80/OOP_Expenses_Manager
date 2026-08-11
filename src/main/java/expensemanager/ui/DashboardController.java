package expensemanager.ui;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import core.wallet.Wallet;
import expensemanager.service.DashboardService;
import expensemanager.ui.factory.WalletCardFactory;
import expensemanager.ui.util.DashboardDialogHelper;
import expensemanager.ui.util.MoneyFormat;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class DashboardController {

    @FXML private HBox walletsContainer;
    @FXML private Label totalBalanceLabel;
    @FXML private Label totalPeriodChangeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label periodLabel;

    private LocalDate currentPeriodStart = LocalDate.now().withDayOfMonth(1);
    private LocalDate currentPeriodEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    public void initialize() {
        refreshWallets();
        refreshOverview();
    }
    
    private void refreshWallets() {
        if (walletsContainer != null) {
            walletsContainer.getChildren().clear();
        }

        List<Wallet> wallets = dashboardService.getOrInitWallets();
        double totalBalance = dashboardService.calculateTotalBalance(wallets);

        for (Wallet w : wallets) {
            if (walletsContainer != null) {
                HBox card = WalletCardFactory.createWalletCard(
                        w,
                        deletedWallet -> {
                            dashboardService.deleteWallet(deletedWallet.getId());
                            refreshWallets();
                        },
                        clickedWallet -> openWalletView(clickedWallet)
                );
                walletsContainer.getChildren().add(card);
            }
        }

        if (totalBalanceLabel != null) {
            totalBalanceLabel.setText(MoneyFormat.format(totalBalance));
        }
    }

    private void refreshOverview() {
        if (periodLabel != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
            periodLabel.setText(currentPeriodStart.format(dtf) + " - " + currentPeriodEnd.format(dtf));
        }

        DashboardService.OverviewData overview = dashboardService.getOverviewData(currentPeriodStart, currentPeriodEnd);

        if (totalIncomeLabel != null) totalIncomeLabel.setText(MoneyFormat.format(overview.income()));
        if (totalExpensesLabel != null) totalExpensesLabel.setText(MoneyFormat.format(overview.expense()));
        if (totalPeriodChangeLabel != null) {
            totalPeriodChangeLabel.setText(MoneyFormat.format(overview.netChange()));
            String color = overview.netChange() < 0 ? "#ef4444" : "#3b82f6";
            totalPeriodChangeLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        }
    }

    @FXML
    public void handleAddWallet() {
        DashboardDialogHelper.showAddWalletDialog().ifPresent(wallet -> {
            dashboardService.addWallet(wallet);
            refreshWallets();
        });
    }

    @FXML
    public void handlePrevPeriod() {
        shiftPeriod(-1);
    }

    @FXML
    public void handleNextPeriod() {
        shiftPeriod(1);
    }

    private void shiftPeriod(int direction) {
        long days = ChronoUnit.DAYS.between(currentPeriodStart, currentPeriodEnd) + 1;
        currentPeriodStart = currentPeriodStart.plusDays(direction * days);
        currentPeriodEnd = currentPeriodEnd.plusDays(direction * days);
        refreshOverview();
    }

    @FXML
    public void handleCustomPeriod() {
        DashboardDialogHelper.showCustomPeriodDialog(currentPeriodStart, currentPeriodEnd).ifPresent(pair -> {
            if (pair.getKey() != null && pair.getValue() != null) {
                if (pair.getKey().isAfter(pair.getValue())) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ngày bắt đầu không thể lớn hơn ngày kết thúc!");
                    alert.showAndWait();
                } else {
                    currentPeriodStart = pair.getKey();
                    currentPeriodEnd = pair.getValue();
                    refreshOverview();
                }
            }
        });
    }

    private void openWalletView(Wallet wallet) {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/fxml/WalletView.fxml");
            
            // 1. Kiểm tra xem file FXML có tồn tại đúng đường dẫn không
            if (fxmlUrl == null) {
                showErrorAlert("Lỗi đường dẫn", "Không tìm thấy file /fxml/WalletView.fxml!\nHãy kiểm tra lại tên file trong thư mục resources/fxml/");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // 2. Kiểm tra Controller
            WalletViewController controller = loader.getController();
            if (controller == null) {
                showErrorAlert("Lỗi Controller", "File WalletView.fxml chưa khai báo fx:controller=\"expensemanager.ui.WalletViewController\"");
                return;
            }

            // 3. Truyền dữ liệu và đổi giao diện
            controller.initData(wallet);
            walletsContainer.getScene().setRoot(root);

        } catch (Exception e) {
            // Bắt TẤT CẢ các lỗi (bao gồm NullPointerException, LoadException...)
            e.printStackTrace();
            showErrorAlert("Lỗi mở WalletView", "Chi tiết lỗi: " + e.toString());
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleNavigateToBudgets() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BudgetsView.fxml"));
            Parent root = loader.load();
            walletsContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleConnectBank() {
    // Gọi thẳng sang handleAddWallet vì dialog mới đã hỗ trợ chọn loại "Bank Account"
        handleAddWallet();
    }
    
}