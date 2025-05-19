package it.finance.sb.factory;

import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.ExpenseTransaction;
import it.finance.sb.model.transaction.TransactionBuilder;
import it.finance.sb.model.transaction.TransactionType;

import java.util.Date;

/**
 * The type Expense transaction creator.
 */
public class ExpenseTransactionCreator implements TransactionCreator {
    @Override
    public AbstractTransaction create(double amount, String category, String reason, Date date, AccountInterface to, AccountInterface from) {
        TransactionBuilder builder = new TransactionBuilder()
                .type(TransactionType.EXPENSE)
                .amount(amount)
                .category(category)
                .reason(reason)
                .date(date)
                .from(from);

        return builder.build();


    }
}
