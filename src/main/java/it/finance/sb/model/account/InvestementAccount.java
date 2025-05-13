package it.finance.sb.model.account;

/**
 * The type Investement account.
 */
public class InvestementAccount extends AbstractAccount{

    /**
     * Instantiates a new Investement account.
     *
     * @param name    the name
     * @param balance the balance
     */
    public InvestementAccount(String name, double balance) {
        super(name, balance);
    }

    @Override
    void seeDeposit() {
        System.out.printf("The balance of th account %s is of %f",name, balance);
    }
}
