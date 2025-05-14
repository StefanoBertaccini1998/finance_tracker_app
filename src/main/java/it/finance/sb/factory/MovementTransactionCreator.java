package it.finance.sb.factory;

import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.MovementTransaction;

import java.util.Date;

/**
 * The type Movement transaction creator.
 */
public class MovementTransactionCreator implements TransactionCreator {
    @Override
    public AbstractTransaction create(double amount, String category, String reason, Date date, AccountInterface to, AccountInterface from) {
        return new MovementTransaction(amount, category, reason, date, to, from);
    }
}
