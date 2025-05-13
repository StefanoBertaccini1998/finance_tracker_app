package it.finance.sb.model.transaction;

import it.finance.sb.model.account.AbstractAccount;

import java.util.Date;

/**
 * The type Movement transaction.
 */
public class MovementTransaction extends AbstractTransaction {

    private AbstractAccount toAccount;
    private AbstractAccount fromAccount;

    /**
     * Instantiates a new Movement transaction.
     *
     * @param amount      the amount
     * @param category    the category
     * @param reason      the reason
     * @param date        the date
     * @param toAccount   the to account
     * @param fromAccount the from account
     */
    public MovementTransaction(double amount, String category, String reason, Date date, AbstractAccount toAccount, AbstractAccount fromAccount) {
        super(amount, category, reason, date, TransactionType.MOVEMENT);
        this.toAccount = toAccount;
        this.fromAccount = fromAccount;
    }

    /**
     * Gets to account.
     *
     * @return the to account
     */
    public AbstractAccount getToAccount() {
        return toAccount;
    }

    /**
     * Sets to account.
     *
     * @param toAccount the to account
     */
    public void setToAccount(AbstractAccount toAccount) {
        this.toAccount = toAccount;
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
        System.out.printf("Movement Transaction of %f - from %s - to %s - in %s - reason %s %n", amount, fromAccount.getName(), toAccount.getName(), date, reason);
    }

    @Override
    public double getTotal() {

        return amount;
    }

    @Override
    public String toCsv() {
        return formatCsvLine(fromAccount.getName(), toAccount.getName(), category, reason, date.getTime());
    }

    public static MovementTransaction fromCsv(String[] fields, AbstractAccount to, AbstractAccount from) {
        double amount = Double.parseDouble(fields[2]);
        String category = fields[5];
        String reason = fields[6];
        Date date = new Date(Long.parseLong(fields[7]));
        return new MovementTransaction(amount, category, reason, date, to, from);
    }

}
