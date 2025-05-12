package it.finance.sb.model.transaction;


import it.finance.sb.model.account.AbstractAccount;

import java.util.Date;

public class ExpenseTransaction extends AbstractTransaction {

    private AbstractAccount fromAccount;
    public ExpenseTransaction(double amount, String category, String reason, Date date, AbstractAccount fromAccount) {
        super(amount, category, reason, date,TransactionType.EXPENSE);
        this.fromAccount = fromAccount;
    }

    public AbstractAccount getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(AbstractAccount fromAccount) {
        this.fromAccount = fromAccount;
    }

    @Override
    public void displayTransaction() {
        System.out.printf("Expense Transaction of %f - from %s - in %s - reason %s %n",amount,fromAccount.getName(),date,reason);
    }

    @Override
    public double getTotal() {

        return amount;
    }

}
