package core.wallet;

import core.WalletType;

public class BankAccount extends Wallet {
    private String bankName;
    private String accountNumber;

    public BankAccount(String name, double balance, String bankName, String accountNumber) {
        super(name, balance);
        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public WalletType getWalletType() {
        return WalletType.BANK;
    }
    
}
