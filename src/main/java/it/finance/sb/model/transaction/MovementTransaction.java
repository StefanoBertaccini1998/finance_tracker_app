package it.finance.sb.model.transaction;

import it.finance.sb.model.account.AbstractAccount;

import java.util.Date;

public class MovementTransaction extends AbstractTransaction {

    private AbstractAccount toAccount;
    private AbstractAccount fromAccount;

    public MovementTransaction(double amount, String reason, Date date, AbstractAccount toAccount, AbstractAccount fromAccount) {
        super(amount, reason, date, TransactionType.MOVEMENT);
        this.toAccount = toAccount;
        this.fromAccount = fromAccount;
    }

    public AbstractAccount getToAccount() {
        return toAccount;
    }

    public void setToAccount(AbstractAccount toAccount) {
        this.toAccount = toAccount;
    }

    public AbstractAccount getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(AbstractAccount fromAccount) {
        this.fromAccount = fromAccount;
    }

    @Override
    public void displayTransaction() {
        System.out.printf("Movement Transaction of %f - from %s - to %s - in %s - reason %s %n",amount,fromAccount.getName(),toAccount.getName(),date,reason);
    }

    @Override
    public void getTotal() {

    }

    @Override
    void modifyTransaction() {

    }
}
