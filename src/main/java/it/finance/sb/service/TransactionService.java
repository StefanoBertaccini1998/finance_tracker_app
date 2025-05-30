package it.finance.sb.service;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.factory.FinanceAbstractFactory;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.iterator.TransactionIterator;
import it.finance.sb.model.transaction.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * TransactionService handles the business logic related to creating, modifying,
 * deleting, and retrieving transactions. It enforces account integrity,
 * transaction validation, and category tracking.
 */
public class TransactionService extends BaseService {

    private final Logger logger = LoggerFactory.getSafeLogger(TransactionService.class);
    private final UserService userService;
    private final FinanceAbstractFactory factory;

    public TransactionService(UserService userService, FinanceAbstractFactory factory) {
        this.userService = userService;
        this.factory = factory;
    }

    /**
     * Creates and registers a validated transaction.
     */
    public AbstractTransaction create(TransactionType type, double amount, String category, String reason, Date date,
                                      AccountInterface toAccount, AccountInterface fromAccount)
            throws TransactionOperationException, UserLoginException, DataValidationException {

        requireLoggedInUser();

        if (amount <= 0) {
            logger.warning(() -> "Rejected transaction with non-positive amount: " + amount);
            throw new DataValidationException("Amount must be greater than 0.");
        }
        if (fromAccount != null && fromAccount.getBalance() < amount) {
            logger.warning("Insufficient funds in source account: " + fromAccount.getName());
            throw new TransactionOperationException("Insufficient funds.");
        }
        //Check account reliability
        validateAccounts(type, toAccount, fromAccount);
        //account are controlled before
        applyAccountUpdates(type, amount, toAccount, fromAccount);

        try {
            AbstractTransaction transaction = switch (type) {
                case INCOME -> factory.createIncome(amount, category, reason, date, toAccount);
                case EXPENSE -> factory.createExpense(amount, category, reason, date, fromAccount);
                case MOVEMENT -> factory.createMovement(amount, category, reason, date, toAccount, fromAccount);
            };

            getCurrentUser().addTransaction(transaction);
            userService.addCategory(category);

            logger.info("Created transaction ID=" + transaction.getTransactionId() + " for user: " + getCurrentUser().getName());
            return transaction;

        } catch (DataValidationException e) {
            throw new TransactionOperationException("Failed to create transaction.", e);
        }
    }

    /**
     * Deletes a transaction and rolls back its effects.
     */
    public AbstractTransaction delete(AbstractTransaction transaction)
            throws TransactionOperationException, UserLoginException {

        requireLoggedInUser();

        if (transaction == null) {
            throw new TransactionOperationException("Transaction cannot be null.");
        }

        try {
            reverseAccountUpdate(transaction);
            getCurrentUser().getTransactionLists()
                    .getOrDefault(transaction.getType(), new TransactionList())
                    .remove(transaction);

            logger.info("Deleted transaction ID=" + transaction.getTransactionId());
            return transaction;
        } catch (Exception e) {
            throw new TransactionOperationException("Unexpected failure while deleting transaction", e);
        }
    }

    /**
     * Modifies a transaction by deleting and re-creating it.
     */
    public AbstractTransaction modify(AbstractTransaction original, Double newAmount, String newCategory,
                                      String newReason, Date newDate,
                                      AccountInterface newTo, AccountInterface newFrom)
            throws TransactionOperationException, UserLoginException {

        requireLoggedInUser();
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
        } catch (UserLoginException | DataValidationException e) {
            throw new TransactionOperationException("Failed to modify transaction.", e);
        }
    }

    /**
     * Removes all transactions that involve a given account.
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

    /**
     * Returns all transactions across all types.
     */
    public List<AbstractTransaction> getAllTransactionsFlattened() {
        return getCurrentUser().getTransactionLists().values().stream()
                .flatMap(list -> list.getFlattenedTransactions().stream())
                .toList();
    }

    /**
     * Filters transactions by category (case-insensitive).
     */
    public List<AbstractTransaction> getTransactionsByCategory(String category) {
        return getAllTransactionsFlattened().stream()
                .filter(tx -> tx.getCategory() != null && tx.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    /**
     * Filters transactions by type.
     */
    public List<AbstractTransaction> getTransactionsByType(TransactionType type) {
        return getAllTransactionsFlattened().stream()
                .filter(tx -> tx.getType() == type)
                .toList();
    }

    /**
     * Filters transactions whose reason contains a keyword (case-insensitive).
     */
    public List<AbstractTransaction> getTransactionsByReasonContains(String keyword) {
        return getAllTransactionsFlattened().stream()
                .filter(tx -> keyword != null && tx.getReason() != null && tx.getReason().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }


    // === Internal helpers ===

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
                if (to == null || from == null) {
                    throw new TransactionOperationException("Both accounts required for MOVEMENT.");
                }
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
