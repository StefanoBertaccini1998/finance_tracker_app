package it.finance.sb.factory;

import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.IncomeTransaction;

import java.util.Date;

/**
 * The type Income transaction creator.
 */
public class IncomeTransactionCreator implements TransactionCreator {
    @Override
    public AbstractTransaction create(double amount,String category, String reason, Date date, AbstractAccount to, AbstractAccount from) {
        return new IncomeTransaction(amount,category, reason, date, to);
    }
}
