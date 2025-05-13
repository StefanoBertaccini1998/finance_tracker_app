package it.finance.sb.service;

import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.factory.TransactionFactory;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.iterator.TransactionIterator;
import it.finance.sb.model.transaction.*;
import it.finance.sb.model.user.User;
import it.finance.sb.utility.InputSanitizer;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Transaction service.
 */
public class TransactionService {

    private final User user;
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    /**
     * Instantiates a new Transaction service.
     *
     * @param user the user
     */
    public TransactionService(User user) {
        this.user = user;
    }

    /**
     * Create abstract transaction.
     *
     * @param type        the type
     * @param amount      the amount
     * @param category    the category
     * @param reason      the reason
     * @param date        the date
     * @param toAccount   the to account
     * @param fromAccount the from account
     * @return the abstract transaction
     * @throws TransactionOperationException the transaction operation exception
     */
    public AbstractTransaction create(TransactionType type, double amount, String category, String reason, Date date,
                                      AbstractAccount toAccount, AbstractAccount fromAccount) throws TransactionOperationException {
        try {
            if (amount <= 0) {
                logger.warning("Attempted to create transaction with non-positive amount.");
                throw new TransactionOperationException("Transaction amount must be greater than 0.");
            }

            if (fromAccount != null && fromAccount.getBalance() < amount) {
                logger.warning("Insufficient funds in source account.");
                throw new TransactionOperationException("Insufficient funds in source account.");
            }

            switch (type) {
                case INCOME -> {
                    if (toAccount == null) throw new TransactionOperationException("ToAccount is required for INCOME.");
                    toAccount.update(amount);
                }
                case EXPENSE -> {
                    if (fromAccount == null) throw new TransactionOperationException("FromAccount is required for EXPENSE.");
                    fromAccount.update(-amount);
                }
                case MOVEMENT -> {
                    if (toAccount == null || fromAccount == null) {
                        throw new TransactionOperationException("Both accounts required for MOVEMENT.");
                    }
                    if (fromAccount.getBalance() < amount) {
                        throw new TransactionOperationException("Insufficient funds for MOVEMENT.");
                    }
                    toAccount.update(amount);
                    fromAccount.update(-amount);
                }
                default -> throw new TransactionOperationException("Unsupported transaction type: " + type);
            }

            AbstractTransaction transaction = TransactionFactory.createTransaction(type, amount, reason,category, date, toAccount, fromAccount);
            InputSanitizer.validate(transaction);
            user.addTransaction(transaction);

            logger.info("[TransactionService] Created " + type + " transaction: ID=" + transaction.getTransactionId() + ", amount=" + amount);
            return transaction;

        } catch (TransactionOperationException e) {
            logger.log(Level.WARNING, "Transaction creation failed: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error while creating transaction", e);
            throw new TransactionOperationException("Failed to create transaction.", e);
        }
    }

    /**
     * Delete abstract transaction.
     *
     * @param transaction the transaction
     * @return the abstract transaction
     * @throws TransactionOperationException the transaction operation exception
     */
    public AbstractTransaction delete(AbstractTransaction transaction) throws TransactionOperationException {
        if (transaction == null) {
            throw new TransactionOperationException("Cannot delete a null transaction.");
        }

        try {
            double amount = transaction.getAmount();
            TransactionType type = transaction.getType();

            switch (type) {
                case INCOME -> ((IncomeTransaction) transaction).getToAccount().update(-amount);
                case EXPENSE -> ((ExpenseTransaction) transaction).getFromAccount().update(amount);
                case MOVEMENT -> {
                    MovementTransaction m = (MovementTransaction) transaction;
                    m.getFromAccount().update(amount);
                    m.getToAccount().update(-amount);
                }
                default -> throw new TransactionOperationException("Unsupported transaction type: " + type);
            }

            TransactionList list = user.getTransactionLists().get(type);
            if (list != null) list.remove(transaction);

            logger.info("[TransactionService] Deleted transaction ID=" + transaction.getTransactionId() + ", type=" + transaction.getType());
            return transaction;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while deleting transaction: " + e.getMessage(), e);
            throw new TransactionOperationException("Failed to delete transaction.", e);
        }
    }

    /**
     * Modify abstract transaction.
     *
     * @param original    the original
     * @param newAmount   the new amount
     * @param newCategory the new category
     * @param newReason   the new reason
     * @param newDate     the new date
     * @param newTo       the new to
     * @param newFrom     the new from
     * @return the abstract transaction
     * @throws TransactionOperationException the transaction operation exception
     */
    public AbstractTransaction modify(AbstractTransaction original,
                                      double newAmount,
                                      String newCategory,
                                      String newReason,
                                      Date newDate,
                                      AbstractAccount newTo,
                                      AbstractAccount newFrom) throws TransactionOperationException {
        if (original == null || newAmount <= 0) {
            throw new TransactionOperationException("Invalid transaction input.");
        }

        try {
            TransactionType type = original.getType();
            double oldAmount = original.getAmount();

            switch (type) {
                case INCOME -> ((IncomeTransaction) original).getToAccount().update(-oldAmount);
                case EXPENSE -> ((ExpenseTransaction) original).getFromAccount().update(oldAmount);
                case MOVEMENT -> {
                    MovementTransaction m = (MovementTransaction) original;
                    m.getFromAccount().update(oldAmount);
                    m.getToAccount().update(-oldAmount);
                }
            }

            switch (type) {
                case INCOME -> {
                    if (newTo == null) throw new TransactionOperationException("ToAccount required for INCOME.");
                    newTo.update(newAmount);
                }
                case EXPENSE -> {
                    if (newFrom == null || newFrom.getBalance() < newAmount) {
                        throw new TransactionOperationException("Invalid or insufficient funds for EXPENSE.");
                    }
                    newFrom.update(-newAmount);
                }
                case MOVEMENT -> {
                    if (newFrom == null || newTo == null || newFrom.getBalance() < newAmount) {
                        throw new TransactionOperationException("Invalid accounts or funds for MOVEMENT.");
                    }
                    newFrom.update(-newAmount);
                    newTo.update(newAmount);
                }
            }

            AbstractTransaction updated = TransactionFactory.createTransaction(type, newAmount, newCategory, newReason, newDate, newTo, newFrom);
            InputSanitizer.validate(updated);

            TransactionList list = user.getTransactionLists().get(type);
            list.remove(original);
            list.addTransaction(updated);

            logger.info("[TransactionService] Modified transaction ID=" + original.getTransactionId() +
                    " → amount=" + newAmount + ", reason=" + newReason);
            return updated;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error modifying transaction: " + e.getMessage(), e);
            throw new TransactionOperationException("Failed to modify transaction.", e);
        }
    }

    /**
     * Remove transactions for account.
     *
     * @param accountToDelete the account to delete
     */
    public void removeTransactionsForAccount(AbstractAccount accountToDelete) {
        for (TransactionType type : user.getTransactionLists().keySet()) {
            TransactionList list = user.getTransactionLists().get(type);
            TransactionIterator iterator = list.iterator();

            while (iterator.hasNext()) {
                AbstractTransaction tx = iterator.next();
                boolean match = switch (tx.getType()) {
                    case INCOME -> ((IncomeTransaction) tx).getToAccount().equals(accountToDelete);
                    case EXPENSE -> ((ExpenseTransaction) tx).getFromAccount().equals(accountToDelete);
                    case MOVEMENT -> {
                        MovementTransaction m = (MovementTransaction) tx;
                        yield m.getToAccount().equals(accountToDelete)
                                || m.getFromAccount().equals(accountToDelete);
                    }
                };

                if (match) {
                    iterator.remove();
                    logger.info("[TransactionService] Removed transaction ID=" + tx.getTransactionId()
                            + " due to account deletion.");
                }
            }
        }
    }
}
