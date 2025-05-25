package it.finance.sb.factory;

import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionBuilder;
import it.finance.sb.model.transaction.TransactionType;

import java.util.Date;

/**
 * MovementTransactionCreator builds MOVEMENT transactions using the TransactionBuilder.
 * Both 'from' and 'to' accounts are required for this transaction type.
 */
public class MovementTransactionCreator implements TransactionCreator {
    @Override
    public AbstractTransaction create(double amount, String category, String reason, Date date, AccountInterface to, AccountInterface from) {
        return new TransactionBuilder()
                .type(TransactionType.MOVEMENT)
                .amount(amount)
                .category(category)
                .reason(reason)
                .date(date)
                .from(from)
                .to(to)
                .build();
    }
}