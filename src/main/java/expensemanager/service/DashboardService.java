package expensemanager.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import core.storage.TransactionDAO;
import core.storage.WalletDAO;
import core.wallet.CashWallet;
import core.wallet.Wallet;

public class DashboardService {
    private final core.storage.IWalletDAO walletDAO = new core.storage.WalletDAO();
    private final core.storage.ITransactionDAO transactionDAO;

    public DashboardService() {
        core.storage.ICategoryDAO catDao = new core.storage.CategoryDAO();
        this.transactionDAO = new core.storage.TransactionDAO(catDao);
    }

    public List<Wallet> getOrInitWallets() {
        List<Wallet> wallets = walletDAO.getAllWallets();
        if (wallets.isEmpty()) {
            Wallet defaultWallet = new CashWallet("Ví tiền mặt", 0.0);
            walletDAO.addWallet(defaultWallet);
            wallets = walletDAO.getAllWallets();
        }
        return wallets;
    }

    public void addWallet(Wallet wallet) {
        walletDAO.addWallet(wallet);
    }

    public void deleteWallet(int walletId) {
        walletDAO.deleteWallet(walletId);
    }

    public double calculateTotalBalance(List<Wallet> wallets) {
        return wallets.stream().mapToDouble(Wallet::getBalance).sum();
    }

    public OverviewData getOverviewData(LocalDate start, LocalDate end) {
        try {
            double income = transactionDAO.getTotalAmountForPeriod("INCOME", start, end);
            double expense = transactionDAO.getTotalAmountForPeriod("EXPENSE", start, end);
            return new OverviewData(income, expense, income - expense);
        } catch (SQLException e) {
            e.printStackTrace();
            return new OverviewData(0, 0, 0);
        }
    }

    // DTO mang dữ liệu tổng quan
    public record OverviewData(double income, double expense, double netChange) {}
}