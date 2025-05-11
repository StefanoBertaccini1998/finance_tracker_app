package it.finance.sb.model.account;

public class CashAccount extends AbstractAccount{

    public CashAccount(String name, double balance) {
        super(name, balance);
    }

    @Override
    void seeDeposit() {
        System.out.printf("The balance of th account %s is of %f",name, balance);
    }
}
