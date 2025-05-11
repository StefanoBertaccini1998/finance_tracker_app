package it.finance.sb.model.transaction;

import it.finance.sb.model.account.AbstractAccount;

import java.util.Date;

public class IncomeTransaction extends AbstractTransaction {

    private AbstractAccount toAccount;
    public IncomeTransaction(double amount, String reason, Date date,AbstractAccount toAccount) {
        super(amount, reason, date,TransactionType.INCOME);
        this.toAccount = toAccount;
    }

    public AbstractAccount getToAccount() {
        return toAccount;
    }

    public void setToAccount(AbstractAccount toAccount) {
        this.toAccount = toAccount;
    }

    @Override
    public void displayTransaction() {
        System.out.printf("Income Transaction of %f - to %s - in %s - reason %s %n",amount,toAccount.getName(),date,reason);
    }

    @Override
    public void getTotal() {

    }

    @Override
    void modifyTransaction() {

    }
}
