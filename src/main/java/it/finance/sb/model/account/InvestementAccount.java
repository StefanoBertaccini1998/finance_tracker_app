package it.finance.sb.model.account;

public class InvestementAccount extends AbstractAccount{

    public InvestementAccount(String name, double balance) {
        super(name, balance);
    }

    @Override
    void seeDeposit() {
        System.out.printf("The balance of th account %s is of %f",name, balance);
    }
}
