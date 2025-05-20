package it.finance.sb.model.transaction;

import com.fasterxml.jackson.annotation.JsonTypeName;
import it.finance.sb.model.account.AccountInterface;

import java.util.Date;

/**
 * The type Movement transaction.
 */
@JsonTypeName("MovementTransaction")
public class MovementTransaction extends AbstractTransaction {

    private AccountInterface toAccount;
    private AccountInterface fromAccount;

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
    public MovementTransaction(double amount, String category, String reason, Date date, AccountInterface toAccount, AccountInterface fromAccount) {
        super(amount, category, reason, date, TransactionType.MOVEMENT);
        this.toAccount = toAccount;
        this.fromAccount = fromAccount;
    }

    public MovementTransaction(){
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

    /**
     * Gets from account.
     *
     * @return the from account
     */
    public AccountInterface getFromAccount() {
        return fromAccount;
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

}
