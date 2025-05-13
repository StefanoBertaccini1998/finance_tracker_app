package it.finance.sb.model.transaction;

import it.finance.sb.model.account.AbstractAccount;

import java.util.Date;

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
        return String.join(",", String.valueOf(transactionId), String.valueOf(amount),
                reason, String.valueOf(date.getTime()), type.name(), toAccount.getAccountId() + "", category);
    }

    public static IncomeTransaction fromCsv(String[] fields, AbstractAccount to) throws Exception {
        double amount = Double.parseDouble(fields[1]);
        String reason = fields[2];
        Date date = new Date(Long.parseLong(fields[3]));
        String category = fields.length > 6 ? fields[6] : "Uncategorized";
        return new IncomeTransaction(amount, category, reason, date, to);
    }
}
