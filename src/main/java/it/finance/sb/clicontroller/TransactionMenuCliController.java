package it.finance.sb.clicontroller;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.User;
import it.finance.sb.service.TransactionService;
import it.finance.sb.utility.ConsoleStyle;
import it.finance.sb.utility.ConsoleUtils;
import it.finance.sb.utility.TransactionPrinter;

import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CLI controller for managing user transactions.
 * Provides a text-based menu to view, create, update, and delete transactions.
 * Interacts with {@link TransactionService} to perform the required operations
 * and uses {@link ConsoleUtils} to prompt user input.
 * Transaction types are dynamically handled via {@link TransactionType}.
 */
public class TransactionMenuCliController extends MenuCliController {

    public static final String OPERATION_CANCELLED_BY_USER = "Operation cancelled by user.";
    public static final String UNEXPECTED_ERROR = "Unexpected error: ";
    public static final String SESSION_ERROR = "Session error: ";
    public static final String NO_TRANSACTIONS_AVAILABLE = "No transactions available.";
    private final TransactionService transactionService;
    private User user;
    private static final Logger logger = LoggerFactory.getSafeLogger(TransactionMenuCliController.class);

    /**
     * Constructs the transaction menu controller.
     *
     * @param transactionService the service for performing transaction operations
     */
    public TransactionMenuCliController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /* Template overrides */
    @Override
    protected String title() {
        return "Transaction Menu";
    }

    @Override
    protected List<MenuItem> menuItems() {
        return List.of(
                new MenuItem("View Transactions", this::displayAllTransactions),
                new MenuItem("Create Transaction", this::createTransaction),
                new MenuItem("Update Transaction", this::updateTransaction),
                new MenuItem("Delete Transaction", this::deleteTransaction),
                new MenuItem("Back", this::requestClose)         // exit loop
        );
    }

    /**
     * Prompts the user to input data and creates a new transaction.
     * Validates user input and handles all possible exceptions.
     */
    private void createTransaction() {
        logger.info("Started create transaction flow");
        try {
            if (user.getAccountList().isEmpty()) {
                throw new TransactionOperationException("At least one account is required.");
            }

            // Prompt transaction fields
            TransactionType type = ConsoleUtils.selectEnum(TransactionType.class, "Transaction Type", false);
            AccountInterface[] acc = getTransactionAccounts(type);
            Double amount = ConsoleUtils.promptForDouble("Amount", false);
            String category = ConsoleUtils.selectOrCreateCategory(user.getSortedCategories(), false);
            String reason = ConsoleUtils.prompt("Reason", false);

            // Create the transaction
            transactionService.create(type, amount, category, reason, new Date(), acc[0], acc[1]);
            System.out.println(ConsoleStyle.success("Transaction created."));
            logger.info(() -> "Transaction created successfully: " + type + " for amount " + amount);
            logger.info("Completed create transaction flow");
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED_BY_USER));
        } catch (DataValidationException e) {
            System.out.println(ConsoleStyle.error("Validation failed: " + e.getMessage()));
            logger.warning("Validation failed during transaction creation: " + e.getMessage());
        } catch (TransactionOperationException e) {
            System.out.println(ConsoleStyle.error("Transaction error: " + e.getMessage()));
            logger.warning("Transaction error: " + e.getMessage());
        } catch (UserLoginException e) {
            System.out.println(ConsoleStyle.error("User session error: " + e.getMessage()));
            logger.warning("User session error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error(UNEXPECTED_ERROR + e.getMessage()));
            logger.log(Level.SEVERE, "Unexpected error in createTransaction", e);
        }
    }

    /**
     * Allows user to modify an existing transaction.
     * User can selectively update fields like amount, category, reason.
     */
    private void updateTransaction() {
        System.out.println(ConsoleStyle.menuTitle("Update Transaction"));
        logger.info("Started update transaction flow");

        List<AbstractTransaction> all = transactionService.getAllTransactionsFlattened();
        if (all.isEmpty()) {                                 // ← guard
            System.out.println(ConsoleStyle.warning(NO_TRANSACTIONS_AVAILABLE));
            return;
        }
        try {
            AbstractTransaction tx = ConsoleUtils.selectFromList(
                    transactionService.getAllTransactionsFlattened(), "transaction to update", false);

            // Prompt for new values
            Double amount = ConsoleUtils.promptForDouble("New amount (blank to skip)", true);
            String category = ConsoleUtils.selectOrCreateCategory(user.getSortedCategories(), true);
            String reason = ConsoleUtils.prompt("New reason (blank to skip)", true);
            AccountInterface[] acc = getTransactionAccounts(tx.getType());

            transactionService.modify(tx, amount, category, reason, new Date(), acc[0], acc[1]);
            System.out.println(ConsoleStyle.success("Transaction updated."));
            logger.info("Transaction updated: " + tx.getTransactionId());
            logger.info("Completed update transaction flow");
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED_BY_USER));
            logger.fine("Transaction update cancelled by user.");
        } catch (TransactionOperationException e) {
            System.out.println(ConsoleStyle.error("Could not update transaction: " + e.getMessage()));
            logger.warning("Update failed: " + e.getMessage());
        } catch (UserLoginException e) {
            System.out.println(ConsoleStyle.error(SESSION_ERROR + e.getMessage()));
            logger.warning(SESSION_ERROR + e.getMessage());
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error(UNEXPECTED_ERROR + e.getMessage()));
            logger.log(Level.SEVERE, "Unexpected error in updateTransaction", e);
        }
    }

    /**
     * Deletes a user-selected transaction.
     * Provides confirmation and logging.
     */
    private void deleteTransaction() {
        System.out.println(ConsoleStyle.menuTitle("Delete Transaction"));
        logger.info("Started delete transaction flow");

        List<AbstractTransaction> all = transactionService.getAllTransactionsFlattened();
        if (all.isEmpty()) {                                 // ← guard
            System.out.println(ConsoleStyle.warning(NO_TRANSACTIONS_AVAILABLE));
            return;
        }
        try {
            AbstractTransaction tx = ConsoleUtils.selectFromList(
                    transactionService.getAllTransactionsFlattened(), "transaction to delete", false);
            transactionService.delete(tx);
            System.out.println(ConsoleStyle.success("Transaction deleted."));
            logger.info("Transaction deleted: " + tx.getTransactionId());
            logger.info("Completed delete transaction flow");
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED_BY_USER));
            logger.fine("Transaction deletion cancelled by user.");
        } catch (TransactionOperationException e) {
            System.out.println(ConsoleStyle.error("Could not delete transaction: " + e.getMessage()));
            logger.warning("Deletion failed: " + e.getMessage());
        } catch (UserLoginException e) {
            System.out.println(ConsoleStyle.error(SESSION_ERROR + e.getMessage()));
            logger.warning(SESSION_ERROR + e.getMessage());
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error(UNEXPECTED_ERROR + e.getMessage()));
            logger.log(Level.SEVERE, "Unexpected error in deleteTransaction", e);
        }
    }

    /**
     * Displays all transactions currently associated with the user.
     */
    private void displayAllTransactions() {
        try {
            logger.info("Started display transaction flow");
            List<AbstractTransaction> list =
                    transactionService.getAllTransactionsFlattened();

            if (list.isEmpty()) {
                System.out.println(ConsoleStyle.warning(NO_TRANSACTIONS_AVAILABLE));
                return;
            }

            TransactionPrinter.printTransactions(list);

            while (true) {
                int c = ConsoleUtils.showMenu("Filter by:", true,
                        "Type", "Category", "Reason", "Back");
                switch (c) {
                    case 1 -> filterByType();
                    case 2 -> filterByCategory();
                    case 3 -> filterByReason();
                    default -> {
                        return;
                    }        // Back
                }
            }
        } catch (UserCancelledException ignore) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED_BY_USER));
        }
    }

    private void filterByType() {
        logger.info("Filtering transaction by type");
        try {
            TransactionType type = ConsoleUtils.selectEnum(TransactionType.class, "Transaction Type", false);
            List<AbstractTransaction> filtered = transactionService.getTransactionsByType(type);
            TransactionPrinter.printTransactions(filtered);
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED_BY_USER));
        }

    }

    private void filterByCategory() {
        logger.info("Filtering transaction by category");
        try {
            String category = ConsoleUtils.selectOrCreateCategory(user.getSortedCategories(), false);
            List<AbstractTransaction> filtered = transactionService.getTransactionsByCategory(category);
            TransactionPrinter.printTransactions(filtered);
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED_BY_USER));
        }

    }

    private void filterByReason() {
        logger.info("Filtering transaction by semantic reason");
        try {
            String input = ConsoleUtils.prompt("Enter keyword to search in reasons:", false);
            List<AbstractTransaction> filtered = transactionService.getTransactionsByReasonContains(input);
            TransactionPrinter.printTransactions(filtered);
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED_BY_USER));
        }
    }

    /**
     * Prompts the user to select source/destination accounts based on transaction type.
     *
     * @param type the type of transaction
     * @return an array with [to, from] accounts in position 0 and 1
     * @throws UserCancelledException if the user cancels the operation
     */
    private AccountInterface[] getTransactionAccounts(TransactionType type) throws UserCancelledException, TransactionOperationException {
        logger.info("Getting accounts for transaction");
        AccountInterface from = null;
        AccountInterface to = null;
        List<AccountInterface> accounts = user.getAccountList();
        switch (type) {
            case INCOME -> to = ConsoleUtils.selectFromList(user.getAccountList(), "destination account", false);
            case EXPENSE -> from = ConsoleUtils.selectFromList(user.getAccountList(), "source account", false);
            case MOVEMENT -> {
                if (accounts.size() < 2)
                    throw new TransactionOperationException("At least two accounts are required.");

                AccountInterface src = ConsoleUtils.selectFromList(accounts, "source account", false);

                Predicate<AccountInterface> skipSrc = a -> a.equals(src);
                AccountInterface dst = ConsoleUtils.selectFromList(
                        accounts, "destination account", false, skipSrc, Object::toString);

                from = src;
                to = dst;
            }
        }
        logger.info("Accounts got for transaction");
        return new AccountInterface[]{to, from};
    }


    /**
     * Sets the current user context for the controller.
     * This is required before performing any transaction actions.
     *
     * @param user the logged-in user
     */
    public void setUser(User user) {
        this.user = user;
        transactionService.setCurrentUser(user);
    }
}
