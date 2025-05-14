package it.finance.sb.factory;

import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.*;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

/**
 * Abstract full Factory with Strategy pattern to create specific type of Transaction
 */
public class TransactionFactory {
    private static final Map<TransactionType, TransactionCreator> creators = new EnumMap<>(TransactionType.class);

    static {
        creators.put(TransactionType.INCOME, new IncomeTransactionCreator());
        creators.put(TransactionType.EXPENSE, new ExpenseTransactionCreator());
        creators.put(TransactionType.MOVEMENT, new MovementTransactionCreator());
    }

    /**
     * Method that create a transaction of the type passed
     *
     * @param type     Type of the transaction
     * @param amount   Amount of the transaction
     * @param category Category of the transaction
     * @param reason   Reason of the transaction
     * @param date     Date of the transaction
     * @param to       Account where the transaction will add amount
     * @param from     Account where the transaction will subtract amount
     * @return the instance of the class AbstractTransaction
     *
     */
    public static AbstractTransaction createTransaction(TransactionType type, double amount, String category, String reason, Date date, AccountInterface to, AccountInterface from) throws TransactionOperationException {
        TransactionCreator creator = creators.get(type);
        if (creator == null) throw new TransactionOperationException("Unsupported transaction type: " + type);
        return creator.create(amount, category, reason, date, to, from);
    }
}