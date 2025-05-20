package it.finance.sb.service;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.factory.TransactionFactory;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.iterator.TransactionIterator;
import it.finance.sb.model.transaction.*;
import it.finance.sb.utility.InputSanitizer;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer responsible for managing creation, modification,
 * deletion, and validation of user transactions.
 */
public class TransactionService extends BaseService {

    private final Logger logger = LoggerFactory.getInstance().getLogger(TransactionService.class);
    private final UserService userService;

    public TransactionService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates and registers a new transaction.
     *
     * @param type        Transaction type
     * @param amount      Amount
     * @param category    Category
     * @param reason      Reason
     * @param date        Date
     * @param toAccount   Target account (may be null)
     * @param fromAccount Source account (may be null)
     * @return Created AbstractTransaction
     * @throws TransactionOperationException Business validation errors
     * @throws DataValidationException       Annotation-level validation
     * @throws UserLoginException            If user is not logged in
     */
    public AbstractTransaction create(TransactionType type, double amount, String category, String reason, Date date,
                                      AccountInterface toAccount, AccountInterface fromAccount)
            throws TransactionOperationException, UserLoginException, DataValidationException {

        requireLoggedInUser();

        // Basic validation
        if (amount <= 0) {
            logger.warning("Rejected transaction with non-positive amount: " + amount);
            throw new TransactionOperationException("Amount must be greater than 0.");
        }

        if (fromAccount != null && fromAccount.getBalance() < amount) {
            logger.warning("Insufficient funds in source account: " + fromAccount.getName());
            throw new TransactionOperationException("Insufficient funds.");
        }

        validateAccounts(type, toAccount, fromAccount);
        applyAccountUpdates(type, amount, toAccount, fromAccount);

        try {
            AbstractTransaction transaction = TransactionFactory.createTransaction(
                    type, amount, category, reason, date, toAccount, fromAccount
            );

            InputSanitizer.validate(transaction);
            getCurrentUser().addTransaction(transaction);
            userService.addCategory(category);

            logger.info("Created transaction ID=" + transaction.getTransactionId() + " for user: " + getCurrentUser().getName());
            return transaction;

        } catch (DataValidationException e) {
            logger.log(Level.WARNING, "Sanitization failed: " + e.getMessage(), e);
            throw e;

        } catch (TransactionOperationException e) {
            logger.log(Level.WARNING, "Business rule violation: " + e.getMessage());
            throw e;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error creating transaction", e);
            throw new TransactionOperationException("Unexpected failure while creating transaction.", e);
        }
    }

    /**
     * Deletes an existing transaction and reverts account balances.
     */
    public AbstractTransaction delete(AbstractTransaction transaction)
            throws TransactionOperationException, UserLoginException {

        requireLoggedInUser();

        if (transaction == null) {
            logger.warning("Attempted to delete null transaction.");
            throw new TransactionOperationException("Transaction cannot be null.");
        }

        try {
            reverseAccountUpdate(transaction);
            getCurrentUser()
                    .getTransactionLists()
                    .getOrDefault(transaction.getType(), new TransactionList())
                    .remove(transaction);

            logger.info("Deleted transaction ID=" + transaction.getTransactionId());
            return transaction;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting transaction", e);
            throw new TransactionOperationException("Failed to delete transaction.", e);
        }
    }

    /**
     * Modifies an existing transaction by deleting and recreating it.
     */
    public AbstractTransaction modify(AbstractTransaction original,
                                      Double newAmount,
                                      String newCategory,
                                      String newReason,
                                      Date newDate,
                                      AccountInterface newTo,
                                      AccountInterface newFrom)
            throws TransactionOperationException, UserLoginException {

        if (original == null) throw new TransactionOperationException("Original transaction is null.");

        double finalAmount = Optional.ofNullable(newAmount).orElse(original.getAmount());
        if (finalAmount <= 0) throw new TransactionOperationException("Amount must be positive.");

        String finalCategory = Optional.ofNullable(newCategory).orElse(original.getCategory());
        String finalReason = Optional.ofNullable(newReason).orElse(original.getReason());
        Date finalDate = Optional.ofNullable(newDate).orElse(original.getDate());

        TransactionType type = original.getType();
        AccountInterface finalTo = resolveTargetAccount(original, type, newTo);
        AccountInterface finalFrom = resolveSourceAccount(original, type, newFrom);

        validateAccounts(type, finalTo, finalFrom);

        try {
            delete(original);
            AbstractTransaction updated = create(type, finalAmount, finalCategory, finalReason, finalDate, finalTo, finalFrom);

            logger.info("Transaction modified: OldID=" + original.getTransactionId() +
                    ", NewID=" + updated.getTransactionId());

            return updated;

        } catch (TransactionOperationException | UserLoginException e) {
            logger.log(Level.WARNING, "Failed to modify transaction: " + e.getMessage());
            throw e;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error modifying transaction", e);
            throw new TransactionOperationException("Failed to modify transaction.", e);
        }
    }

    /**
     * Removes all transactions involving a specific account (used on account deletion).
     */
    public void removeTransactionsForAccount(AccountInterface accountToDelete) {
        getCurrentUser().getTransactionLists().forEach((type, list) -> {
            TransactionIterator iterator = list.iterator();
            while (iterator.hasNext()) {
                AbstractTransaction tx = iterator.next();
                if (isTransactionLinkedToAccount(tx, accountToDelete)) {
                    iterator.remove();
                    logger.info("Removed transaction ID=" + tx.getTransactionId() +
                            " due to deletion of account: " + accountToDelete.getName());
                }
            }
        });
    }

    public List<AbstractTransaction> getAllTransactionsFlattened() {
        List<AbstractTransaction> all = new ArrayList<>();
        for (TransactionList txList : getCurrentUser().getTransactionLists().values()) {
            all.addAll(txList.getFlattenedTransactions());
        }
        return all;
    }

    public List<AbstractTransaction> getTransactionsByCategory(String category) {
        return getAllTransactionsFlattened().stream()
                .filter(tx -> tx.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    // ======================= Internal Helpers =======================

    private void validateAccounts(TransactionType type, AccountInterface to, AccountInterface from)
            throws TransactionOperationException {
        switch (type) {
            case INCOME -> {
                if (to == null) throw new TransactionOperationException("To account is required for INCOME.");
            }
            case EXPENSE -> {
                if (from == null) throw new TransactionOperationException("From account is required for EXPENSE.");
            }
            case MOVEMENT -> {
                if (to == null || from == null) throw new TransactionOperationException("Both accounts required for MOVEMENT.");
            }
        }
    }

    private void applyAccountUpdates(TransactionType type, double amount, AccountInterface to, AccountInterface from) {
        switch (type) {
            case INCOME -> to.update(amount);
            case EXPENSE -> from.update(-amount);
            case MOVEMENT -> {
                from.update(-amount);
                to.update(amount);
            }
        }
    }

    private void reverseAccountUpdate(AbstractTransaction tx) {
        double amount = tx.getAmount();
        switch (tx.getType()) {
            case INCOME -> ((IncomeTransaction) tx).getToAccount().update(-amount);
            case EXPENSE -> ((ExpenseTransaction) tx).getFromAccount().update(amount);
            case MOVEMENT -> {
                MovementTransaction m = (MovementTransaction) tx;
                m.getFromAccount().update(amount);
                m.getToAccount().update(-amount);
            }
        }
    }

    private boolean isTransactionLinkedToAccount(AbstractTransaction tx, AccountInterface account) {
        return switch (tx.getType()) {
            case INCOME -> ((IncomeTransaction) tx).getToAccount().equals(account);
            case EXPENSE -> ((ExpenseTransaction) tx).getFromAccount().equals(account);
            case MOVEMENT -> {
                MovementTransaction m = (MovementTransaction) tx;
                yield m.getFromAccount().equals(account) || m.getToAccount().equals(account);
            }
        };
    }

    private AccountInterface resolveTargetAccount(AbstractTransaction tx, TransactionType type, AccountInterface newTo) {
        return switch (type) {
            case INCOME -> Optional.ofNullable(newTo).orElse(((IncomeTransaction) tx).getToAccount());
            case MOVEMENT -> Optional.ofNullable(newTo).orElse(((MovementTransaction) tx).getToAccount());
            default -> null;
        };
    }

    private AccountInterface resolveSourceAccount(AbstractTransaction tx, TransactionType type, AccountInterface newFrom) {
        return switch (type) {
            case EXPENSE -> Optional.ofNullable(newFrom).orElse(((ExpenseTransaction) tx).getFromAccount());
            case MOVEMENT -> Optional.ofNullable(newFrom).orElse(((MovementTransaction) tx).getFromAccount());
            default -> null;
        };
    }
}
