package it.finance.sb.service;

import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.exception.UserLoginException;
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
                                      AccountInterface toAccount, AccountInterface fromAccount)
            throws TransactionOperationException, UserLoginException {
        requireLoggedInUser();

        if (amount <= 0) {
            logger.warning("[TransactionService] Non-positive amount passed.");
            throw new TransactionOperationException("Transaction amount must be greater than 0.");
        }

        if (fromAccount != null && fromAccount.getBalance() < amount) {
            logger.warning("[TransactionService] Insufficient funds in source account.");
            throw new TransactionOperationException("Insufficient funds in source account.");
        }
        validateAccounts(type, toAccount, fromAccount);

        applyAccountUpdates(type, amount, toAccount, fromAccount);

        try {

            AbstractTransaction transaction = TransactionFactory.createTransaction(
                    type, amount, reason, category, date, toAccount, fromAccount);

            InputSanitizer.validate(transaction);
            getCurrentUser().addTransaction(transaction);

            logger.info("[TransactionService] Created transaction: ID=" + transaction.getTransactionId());
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
    public AbstractTransaction delete(AbstractTransaction transaction)
            throws TransactionOperationException, UserLoginException {

        requireLoggedInUser();

        if (transaction == null) {
            throw new TransactionOperationException("Cannot delete a null transaction.");
        }

        try {
            reverseAccountUpdate(transaction);

            TransactionList list = getCurrentUser().getTransactionLists().get(transaction.getType());
            if (list != null) {
                list.remove(transaction);
            }

            logger.info("[TransactionService] Deleted transaction ID=" + transaction.getTransactionId());
            return transaction;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "[TransactionService] Error deleting transaction", e);
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

        double finalAmount = Optional.ofNullable(newAmount).orElse(original.getAmount());
        if (finalAmount <= 0) {
            throw new TransactionOperationException("Modified amount must be greater than 0.");
        }

        String finalCategory = Optional.ofNullable(newCategory).orElse(original.getCategory());
        String finalReason = Optional.ofNullable(newReason).filter(s -> !s.isBlank()).orElse(original.getReason());
        Date finalDate = Optional.ofNullable(newDate).orElse(original.getDate());

        TransactionType type = original.getType();
        AccountInterface finalTo = resolveTargetAccount(original, type, newTo);
        AccountInterface finalFrom = resolveSourceAccount(original, type, newFrom);

        validateAccounts(type, finalTo, finalFrom);

        try {
            //Delete transaction
            delete(original);
            //Create + insert transaction
            AbstractTransaction updated = create(
                    type, finalAmount, finalCategory, finalReason, finalDate, newTo, newFrom);
            logger.info("[TransactionService] Modified transaction ID=" + original.getTransactionId());
            return updated;

        } catch (TransactionOperationException e) {
            logger.warning("[TransactionService] Modification validation failed: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[TransactionService] Failed to modify transaction", e);
            throw new TransactionOperationException("Failed to modify transaction.", e);
        }
    }

    /**
     * Remove transactions for account.
     *
     * @param accountToDelete the account to delete
     */
    public void removeTransactionsForAccount(AccountInterface accountToDelete) {
        getCurrentUser().getTransactionLists().forEach((type, list) -> {
            TransactionIterator iterator = list.iterator();

            while (iterator.hasNext()) {
                AbstractTransaction tx = iterator.next();
                if (isTransactionLinkedToAccount(tx, accountToDelete)) {
                    iterator.remove();
                    logger.info("[TransactionService] Removed transaction ID=" + tx.getTransactionId()
                            + " due to account deletion.");
                }
            }
        });
    }

    /**
     * Display all transactions for the current user (flattened list).
     */
    //TODO move away
    public void displayAllTransactions() {
        User user = getCurrentUser();
        logger.info("[UserService] Showing all transactions for user '" + user.getName() + "'");

        List<AbstractTransaction> transactions = getAllTransactionsFlattened();

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

    /**
     * Gets all transactions.
     *
     * @return the all transactions
     */
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

    private void validateAccounts(TransactionType type, AccountInterface to, AccountInterface from)
            throws TransactionOperationException {

        switch (type) {
            case INCOME -> {
                if (to == null) throw new TransactionOperationException("ToAccount required for INCOME.");
            }
            case EXPENSE -> {
                if (from == null || from.getBalance() <= 0)
                    throw new TransactionOperationException("FromAccount required or insufficient funds.");
            }
            case MOVEMENT -> {
                if (from == null || to == null || from.getBalance() <= 0)
                    throw new TransactionOperationException("Valid source/target required for MOVEMENT.");
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
        if (type == TransactionType.INCOME)
            return Optional.ofNullable(newTo).orElse(((IncomeTransaction) tx).getToAccount());
        if (type == TransactionType.MOVEMENT)
            return Optional.ofNullable(newTo).orElse(((MovementTransaction) tx).getToAccount());
        return null;
    }

    private AccountInterface resolveSourceAccount(AbstractTransaction tx, TransactionType type, AccountInterface newFrom) {
        if (type == TransactionType.EXPENSE)
            return Optional.ofNullable(newFrom).orElse(((ExpenseTransaction) tx).getFromAccount());
        if (type == TransactionType.MOVEMENT)
            return Optional.ofNullable(newFrom).orElse(((MovementTransaction) tx).getFromAccount());
        return null;
    }
}
