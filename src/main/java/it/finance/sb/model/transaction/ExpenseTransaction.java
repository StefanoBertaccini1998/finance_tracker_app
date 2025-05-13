package it.finance.sb.model.transaction;


import it.finance.sb.model.account.AbstractAccount;

import java.util.Date;

/**
 * The type Expense transaction.
 */
public class ExpenseTransaction extends AbstractTransaction {

    private AbstractAccount fromAccount;

    /**
     * Instantiates a new Expense transaction.
     *
     * @param amount      the amount
     * @param category    the category
     * @param reason      the reason
     * @param date        the date
     * @param fromAccount the from account
     */
    public ExpenseTransaction(double amount, String category, String reason, Date date, AbstractAccount fromAccount) {
        super(amount, category, reason, date, TransactionType.EXPENSE);
        this.fromAccount = fromAccount;
    }

    /**
     * Gets from account.
     *
     * @return the from account
     */
    public AbstractAccount getFromAccount() {
        return fromAccount;
    }

    /**
     * Sets from account.
     *
     * @param fromAccount the from account
     */
    public void setFromAccount(AbstractAccount fromAccount) {
        this.fromAccount = fromAccount;
    }

    @Override
    public void displayTransaction() {
        System.out.printf("Expense Transaction of %f - from %s - in %s - reason %s %n", amount, fromAccount.getName(), date, reason);
    }

    @Override
    public double getTotal() {
        return amount;
    }

    @Override
    public String toCsv() {
        return formatCsvLine(fromAccount.getName(), null, category, reason, date.getTime());
    }

    public static ExpenseTransaction fromCsv(String[] fields, AbstractAccount from) throws Exception {
        double amount = Double.parseDouble(fields[2]);
        String category = fields[5];
        String reason = fields[6];
        Date date = new Date(Long.parseLong(fields[7]));
        return new ExpenseTransaction(amount, category, reason, date, from);
    }
}
