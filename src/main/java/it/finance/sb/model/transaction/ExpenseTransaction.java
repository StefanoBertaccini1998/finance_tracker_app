package it.finance.sb.model.transaction;


import com.fasterxml.jackson.annotation.JsonTypeName;
import it.finance.sb.model.account.AccountInterface;

import java.util.Date;

/**
 * The type Expense transaction.
 */
@JsonTypeName("ExpenseTransaction")
public class ExpenseTransaction extends AbstractTransaction {

    private AccountInterface fromAccount;

    /**
     * Instantiates a new Expense transaction.
     *
     * @param amount      the amount
     * @param category    the category
     * @param reason      the reason
     * @param date        the date
     * @param fromAccount the from account
     */
    public ExpenseTransaction(double amount, String category, String reason, Date date, AccountInterface fromAccount) {
        super(amount, category, reason, date, TransactionType.EXPENSE);
        this.fromAccount = fromAccount;
    }

    public ExpenseTransaction(){
        super();
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
        System.out.printf("Expense Transaction of %f - from %s - in %s - reason %s %n", amount, fromAccount.getName(), date, reason);
    }

    @Override
    public double getTotal() {
        return amount;
    }

    @Override
    public String[] toCsv() {
        return new String[]{
                String.valueOf(transactionId),
                type.name(),
                String.valueOf(amount),
                fromAccount == null ? "" : fromAccount.getName(),
                "",
                category    == null ? "" : category,
                reason      == null ? "" : reason,
                String.valueOf(date.getTime())
        };
    }

    @Override
    public String toString() {
        return String.format("Income: %.2f %s - %s (%s)",
                amount,
                fromAccount.getName(),
                category,
                date
        );
    }
}
