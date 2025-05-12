package it.finance.sb.model.account;

import it.finance.sb.annotation.Sanitize;

public abstract class AbstractAccount {
    protected final int accountId;
    private static int idCounter = 0;
    @Sanitize(maxLength = 30)
    protected String name;
    protected double balance;

    abstract void seeDeposit();

    protected AbstractAccount(String name, double deposit) {
        this.name = name;
        this.balance = deposit;
        this.accountId = ++idCounter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setDeposit(double deposit) {
        this.balance = deposit;
    }

    public void update(double amount){
        this.balance += amount;
    }

    public int getAccountId() {
        return accountId;
    }
}
