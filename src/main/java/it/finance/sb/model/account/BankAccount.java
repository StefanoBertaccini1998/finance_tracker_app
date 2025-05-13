package it.finance.sb.model.account;

/**
 * The type Bank account.
 */
public class BankAccount extends AbstractAccount{

    private double interestRate;

    /**
     * Instantiates a new Bank account.
     *
     * @param name    the name
     * @param balance the balance
     */
    public BankAccount(String name, double balance) {
        super(name, balance);
    }

    @Override
    void seeDeposit() {
        System.out.printf("The balance of th account %s is of %f",name, balance);
    }

    /**
     * Gets interest rate.
     *
     * @return the interest rate
     */
    public double getInterestRate() {
        return interestRate;
    }

    /**
     * Sets interest rate.
     *
     * @param interestRate the interest rate
     */
    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }
}
