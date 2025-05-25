package it.finance.sb.model.transaction;

import com.fasterxml.jackson.annotation.JsonTypeName;
import it.finance.sb.annotation.Sanitize;
import it.finance.sb.model.account.AccountInterface;

import java.util.Date;

/**
 * The type Income transaction.
 */
@JsonTypeName("IncomeTransaction")
public class IncomeTransaction extends AbstractTransaction {
    @Sanitize(nonNull = true)
    private AccountInterface toAccount;

    /**
     * Instantiates a new Income transaction.
     *
     * @param amount    the amount
     * @param category  the category
     * @param reason    the reason
     * @param date      the date
     * @param toAccount the to account
     */
    public IncomeTransaction(double amount, String category, String reason, Date date, AccountInterface toAccount) {
        super(amount, category, reason, date, TransactionType.INCOME);
        this.toAccount = toAccount;
    }

    public IncomeTransaction(){
        super();
    }

    /**
     * Gets to account.
     *
     * @return the to account
     */
    public AccountInterface getToAccount() {
        return toAccount;
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

}
