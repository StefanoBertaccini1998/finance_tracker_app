package it.finance.sb.service;

import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.factory.TransactionFactory;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.iterator.TransactionIterator;
import it.finance.sb.model.transaction.*;
import it.finance.sb.model.user.User;
import it.finance.sb.utility.InputSanitizer;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Transaction service.
 */
public class TransactionService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    /**
     * Instantiates a new Transaction service.
     */
    public TransactionService() {
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
                                      AccountInterface toAccount, AccountInterface fromAccount) throws TransactionOperationException {
        requireLoggedInUser();
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
                    if (fromAccount == null)
                        throw new TransactionOperationException("FromAccount is required for EXPENSE.");
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

            AbstractTransaction transaction = TransactionFactory.createTransaction(type, amount, reason, category, date, toAccount, fromAccount);
            InputSanitizer.validate(transaction);
            getCurrentUser().addTransaction(transaction);

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
        requireLoggedInUser();
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

            TransactionList list = getCurrentUser().getTransactionLists().get(type);
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
                                      Double newAmount,
                                      String newCategory,
                                      String newReason,
                                      Date newDate,
                                      AccountInterface newTo,
                                      AccountInterface newFrom) throws TransactionOperationException {
        if (original == null) {
            throw new TransactionOperationException("Original transaction is null.");
        }

        try {
            //Determine final values
            double finalAmount = (newAmount != null) ? newAmount : original.getAmount();
            if (finalAmount <= 0) {
                throw new TransactionOperationException("Amount must be greater than 0.");
            }

            String finalCategory = (newCategory != null) ? newCategory : original.getCategory();
            String finalReason = (newReason != null && !newReason.isEmpty()) ? newReason : original.getReason();
            Date finalDate = (newDate != null) ? newDate : original.getDate();

            TransactionType type = original.getType();

            // Use existing accounts if null
            if (type == TransactionType.INCOME && newTo == null)
                newTo = ((IncomeTransaction) original).getToAccount();

            if (type == TransactionType.EXPENSE && newFrom == null)
                newFrom = ((ExpenseTransaction) original).getFromAccount();

            if (type == TransactionType.MOVEMENT) {
                MovementTransaction m = (MovementTransaction) original;
                if (newFrom == null) newFrom = m.getFromAccount();
                if (newTo == null) newTo = m.getToAccount();
            }

            //VALIDATE account states BEFORE changing anything
            switch (type) {
                case INCOME -> {
                    if (newTo == null) throw new TransactionOperationException("ToAccount required for INCOME.");
                }
                case EXPENSE -> {
                    if (newFrom == null || newFrom.getBalance() < finalAmount)
                        throw new TransactionOperationException("FromAccount invalid or insufficient funds.");
                }
                case MOVEMENT -> {
                    if (newFrom == null || newTo == null || newFrom.getBalance() < finalAmount)
                        throw new TransactionOperationException("Invalid accounts or insufficient funds for MOVEMENT.");
                }
            }

            //Reverse original transaction
            switch (type) {
                case INCOME -> ((IncomeTransaction) original).getToAccount().update(-original.getAmount());
                case EXPENSE -> ((ExpenseTransaction) original).getFromAccount().update(original.getAmount());
                case MOVEMENT -> {
                    MovementTransaction m = (MovementTransaction) original;
                    m.getFromAccount().update(original.getAmount());
                    m.getToAccount().update(-original.getAmount());
                }
            }

            //Apply new effect
            switch (type) {
                case INCOME -> newTo.update(finalAmount);
                case EXPENSE -> newFrom.update(-finalAmount);
                case MOVEMENT -> {
                    newFrom.update(-finalAmount);
                    newTo.update(finalAmount);
                }
            }

            //Create + insert transaction
            AbstractTransaction updated = TransactionFactory.createTransaction(type, finalAmount, finalCategory, finalReason, finalDate, newTo, newFrom);
            InputSanitizer.validate(updated);

            TransactionList list = getCurrentUser().getTransactionLists().get(type);
            list.remove(original);
            list.addTransaction(updated);

            logger.info("[TransactionService] Modified transaction ID=" + original.getTransactionId() +
                    " → amount=" + finalAmount + ", reason=" + finalReason);
            return updated;

        } catch (TransactionOperationException e) {
            logger.warning("Validation failed during modify: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error while modifying transaction", e);
            throw new TransactionOperationException("Failed to modify transaction.", e);
        }
    }

    /**
     * Remove transactions for account.
     *
     * @param accountToDelete the account to delete
     */
    public void removeTransactionsForAccount(AccountInterface accountToDelete) {
        for (TransactionType type : getCurrentUser().getTransactionLists().keySet()) {
            TransactionList list = getCurrentUser().getTransactionLists().get(type);
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

    /**
     * Display all transactions for the current user (flattened list).
     */
    public void displayAllTransactions() {
        User user = getCurrentUser();
        logger.info("[UserService] Showing all transactions for user '" + user.getName() + "'");

        List<AbstractTransaction> transactions = user.getAllTransactionsFlattened();

        if (transactions.isEmpty()) {
            System.out.println("⚠️ No transactions found.");
            return;
        }

        System.out.println("\n📋 All Transactions:");
        for (AbstractTransaction tx : transactions) {
            System.out.printf("  ➤ ID: %-4d | 💰 Amount: %-8.2f | 📌 Category: %-12s | 📃 Reason: %-20s | 📅 Date: %s | Type: %s\n",
                    tx.getTransactionId(),
                    tx.getAmount(),
                    tx.getCategory(),
                    tx.getReason(),
                    tx.getDate(),
                    tx.getType().name()
            );
        }
    }
}
