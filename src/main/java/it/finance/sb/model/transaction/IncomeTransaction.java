package it.finance.sb.model.transaction;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.model.account.AbstractAccount;

import java.util.Date;
import java.util.Map;

/**
 * The type Income transaction.
 */
public class IncomeTransaction extends AbstractTransaction {

    private AbstractAccount toAccount;

    /**
     * Instantiates a new Income transaction.
     *
     * @param amount    the amount
     * @param category  the category
     * @param reason    the reason
     * @param date      the date
     * @param toAccount the to account
     */
    public IncomeTransaction(double amount, String category, String reason, Date date, AbstractAccount toAccount) {
        super(amount, category, reason, date, TransactionType.INCOME);
        this.toAccount = toAccount;
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

    @Override
    public void displayTransaction() {
        System.out.printf("Income Transaction of %f - to %s - in %s - reason %s %n", amount, toAccount.getName(), date, reason);
    }

    @Override
    public double getTotal() {
        return amount;
    }

    @Override
    public String toCsv() {
        return formatCsvLine(null, toAccount.getName(), category, reason, date.getTime());
    }

    public static IncomeTransaction fromCsv(String[] fields, AbstractAccount to) {
        double amount = Double.parseDouble(fields[2]);
        String category = fields[5];
        String reason = fields[6];
        Date date = new Date(Long.parseLong(fields[7]));
        return new IncomeTransaction(amount, reason, category, date, to);
    }
}
