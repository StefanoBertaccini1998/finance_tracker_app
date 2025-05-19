package it.finance.sb.factory;

import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.IncomeTransaction;
import it.finance.sb.model.transaction.TransactionBuilder;
import it.finance.sb.model.transaction.TransactionType;

import java.util.Date;

/**
 * The type Income transaction creator.
 */
public class IncomeTransactionCreator implements TransactionCreator {
    @Override
    public AbstractTransaction create(double amount, String category, String reason, Date date, AccountInterface to, AccountInterface from) {
        TransactionBuilder builder = new TransactionBuilder()
                .type(TransactionType.INCOME)
                .amount(amount)
                .category(category)
                .reason(reason)
                .date(date)
                .to(to);
        return builder.build();

    }
}
