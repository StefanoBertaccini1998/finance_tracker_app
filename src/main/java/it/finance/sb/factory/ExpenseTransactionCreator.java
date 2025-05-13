package it.finance.sb.factory;

import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.ExpenseTransaction;
import it.finance.sb.model.transaction.IncomeTransaction;

import java.util.Date;

/**
 * The type Expense transaction creator.
 */
public class ExpenseTransactionCreator implements TransactionCreator {
    @Override
    public AbstractTransaction create(double amount,String category, String reason, Date date, AbstractAccount to, AbstractAccount from) {
        return new ExpenseTransaction(amount,category, reason, date, from);
    }
}
