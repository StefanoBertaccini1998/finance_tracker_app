package it.finance.sb.factory;

import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.transaction.AbstractTransaction;

import java.util.Date;

/**
 * The interface Transaction creator.
 */
public interface TransactionCreator {
    /**
     * Create abstract transaction.
     *
     * @param amount   the amount
     * @param category the category
     * @param reason   the reason
     * @param date     the date
     * @param to       the to
     * @param from     the from
     * @return the abstract transaction
     */
    AbstractTransaction create(double amount,String category, String reason, Date date, AbstractAccount to, AbstractAccount from);
}