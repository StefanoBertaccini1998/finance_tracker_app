package it.finance.sb.factory;

import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.ExpenseTransaction;

import java.util.Date;

/**
 * The type Expense transaction creator.
 */
public class ExpenseTransactionCreator implements TransactionCreator {
    @Override
    public AbstractTransaction create(double amount, String category, String reason, Date date, AccountInterface to, AccountInterface from) {
        return new ExpenseTransaction(amount,category, reason, date, from);
    }
}
