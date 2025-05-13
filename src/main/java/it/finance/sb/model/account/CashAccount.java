package it.finance.sb.model.account;

/**
 * The type Cash account.
 */
public class CashAccount extends AbstractAccount{

    /**
     * Instantiates a new Cash account.
     *
     * @param name    the name
     * @param balance the balance
     */
    public CashAccount(String name, double balance) {
        super(name, balance);
    }

    @Override
    void seeDeposit() {
        System.out.printf("The balance of th account %s is of %f",name, balance);
    }
}
