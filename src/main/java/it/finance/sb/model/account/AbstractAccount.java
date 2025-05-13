package it.finance.sb.model.account;

import it.finance.sb.annotation.Sanitize;

/**
 * The type Abstract account.
 */
public abstract class AbstractAccount {
    /**
     * The Account id.
     */
    protected final int accountId;
    private static int idCounter = 0;
    /**
     * The Name.
     */
    @Sanitize(maxLength = 30)
    protected String name;
    /**
     * The Balance.
     */
    protected double balance;

    /**
     * See deposit.
     */
    abstract void seeDeposit();

    /**
     * Instantiates a new Abstract account.
     *
     * @param name    the name
     * @param deposit the deposit
     */
    protected AbstractAccount(String name, double deposit) {
        this.name = name;
        this.balance = deposit;
        this.accountId = ++idCounter;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets balance.
     *
     * @return the balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Sets deposit.
     *
     * @param deposit the deposit
     */
    public void setDeposit(double deposit) {
        this.balance = deposit;
    }

    /**
     * Update.
     *
     * @param amount the amount
     */
    public void update(double amount){
        this.balance += amount;
    }

    /**
     * Gets account id.
     *
     * @return the account id
     */
    public int getAccountId() {
        return accountId;
    }
}
