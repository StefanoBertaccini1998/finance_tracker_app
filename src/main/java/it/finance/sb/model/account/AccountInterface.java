package it.finance.sb.model.account;

public interface AccountInterface {
    String getName();
    double getBalance();
    int getAccountId();
    AccounType getType();
    void setType(AccounType type);
    void update(double amount);
    void setDeposit(double deposit);
    void setName(String name);
}
