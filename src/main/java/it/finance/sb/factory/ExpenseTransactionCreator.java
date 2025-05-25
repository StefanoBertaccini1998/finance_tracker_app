package it.finance.sb.factory;

import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionBuilder;
import it.finance.sb.model.transaction.TransactionType;

import java.util.Date;

/**
 * ExpenseTransactionCreator builds EXPENSE transactions using the TransactionBuilder.
 * Only 'from' account is required for this transaction type.
 */
public class ExpenseTransactionCreator implements TransactionCreator {
    @Override
    public AbstractTransaction create(double amount, String category, String reason, Date date, AccountInterface to, AccountInterface from) {
        return new TransactionBuilder()
                .type(TransactionType.EXPENSE)
                .amount(amount)
                .category(category)
                .reason(reason)
                .date(date)
                .from(from)
                .build();
    }
}