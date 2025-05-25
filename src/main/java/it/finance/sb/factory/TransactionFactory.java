package it.finance.sb.factory;

import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * TransactionFactory is responsible for delegating transaction creation
 * to the correct TransactionCreator implementation based on the given type.
 * It applies the Factory and Strategy design patterns.
 */
public class TransactionFactory implements TransactionAbstractFactory{

    private static final Logger logger = LoggerFactory.getInstance().getLogger(TransactionFactory.class);

    /**
     * Registry of creators mapped by transaction type.
     * Uses Strategy pattern to delegate creation logic.
     */
    private static final Map<TransactionType, TransactionCreator> creators = new EnumMap<>(TransactionType.class);

    static {
        creators.put(TransactionType.INCOME, new IncomeTransactionCreator());
        creators.put(TransactionType.EXPENSE, new ExpenseTransactionCreator());
        creators.put(TransactionType.MOVEMENT, new MovementTransactionCreator());
    }

    /**
     * Creates a transaction of the specified type and applies validation.
     *
     * @param type   Type of the transaction
     * @param amount Transaction amount
     * @param category Category label
     * @param reason Optional reason or description
     * @param date Date of transaction
     * @param to Account credited (if applicable)
     * @param from Account debited (if applicable)
     * @return A validated AbstractTransaction instance
     * @throws TransactionOperationException if the type is unsupported
     */
    public static AbstractTransaction createTransaction(
            TransactionType type,
            double amount,
            String category,
            String reason,
            Date date,
            AccountInterface to,
            AccountInterface from
    ) throws TransactionOperationException {

        TransactionCreator creator = creators.get(type);
        if (creator == null) {
            logger.severe("Unsupported transaction type: " + type);
            throw new TransactionOperationException("Unsupported transaction type: " + type);
        }

        AbstractTransaction transaction = creator.create(amount, category, reason, date, to, from);

        logger.info("Transaction created and validated: Type=" + type + ", Amount=" + amount);
        return transaction;
    }

    /**
     * Allows manual registration of a custom TransactionCreator.
     * Useful for extending the system dynamically or during testing.
     *
     * @param type Transaction type to associate
     * @param creator Creator implementation
     */
    public static void registerCreator(TransactionType type, TransactionCreator creator) {
        creators.put(type, creator);
        logger.info("Custom TransactionCreator registered for type: " + type);
    }

    /**
     * Clears all registered creators.
     * Mainly used for testing purposes.
     */
    public static void clearCreators() {
        creators.clear();
        logger.warning("TransactionFactory creators map cleared.");
    }

    @Override
    public AbstractTransaction createIncome(double amount, String category, String reason, Date date, AccountInterface to) {
        try {
            return createTransaction(TransactionType.INCOME, amount, category, reason, date, to, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create income transaction", e);
        }
    }

    @Override
    public AbstractTransaction createExpense(double amount, String category, String reason, Date date, AccountInterface from) {
        try {
            return createTransaction(TransactionType.EXPENSE, amount, category, reason, date, null, from);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create expense transaction", e);
        }
    }

    @Override
    public AbstractTransaction createMovement(double amount, String category, String reason, Date date, AccountInterface to, AccountInterface from) {
        try {
            return createTransaction(TransactionType.MOVEMENT, amount, category, reason, date, to, from);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create movement transaction", e);
        }
    }
}
