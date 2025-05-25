package it.finance.sb.cliController;

import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.exception.UserCancelledException;
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

/**
 * CLI controller for managing user transactions.
 * Provides a text-based menu to view, create, update, and delete transactions.
 * Interacts with {@link TransactionService} to perform the required operations
 * and uses {@link ConsoleUtils} to prompt user input.
 * Transaction types are dynamically handled via {@link TransactionType}.
 *
 */
public class TransactionMenuCliController implements MenuCliController {

    private final TransactionService transactionService;
    private User user;

    /**
     * Constructs the transaction menu controller.
     *
     * @param transactionService the service for performing transaction operations
     * @param user               the currently logged-in user
     */
    public TransactionMenuCliController(TransactionService transactionService, User user) {
        this.transactionService = transactionService;
    }

    /**
     * Displays the main menu for managing transactions.
     *
     * @throws UserCancelledException if the user cancels the interaction
     */
    @Override
    public void show() throws UserCancelledException {
        menuLoop("Transaction Menu",
                new String[]{
                        "View Transactions",
                        "Create Transaction",
                        "Update Transaction",
                        "Delete Transaction",
                        "Back"
                },
                this::displayAllTransactions,
                this::createTransaction,
                this::updateTransaction,
                this::deleteTransaction,
                null
        );
    }

    /**
     * Handles the creation of a new transaction.
     * Prompts the user for type, amount, category, reason, and accounts.
     */
    private void createTransaction() {
        try {
            if (user.getAccountList().isEmpty()) {
                throw new TransactionOperationException("At least one account is required.");
            }

            TransactionType type = ConsoleUtils.selectEnum(TransactionType.class, "Transaction Type", false);
            Double amount = ConsoleUtils.promptForDouble("Amount", false);
            String category = ConsoleUtils.selectOrCreateCategory(user.getSortedCategories(), false);
            String reason = ConsoleUtils.prompt("Reason", false);
            AccountInterface[] acc = getTransactionAccounts(type);

            transactionService.create(type, amount, category, reason, new Date(), acc[0], acc[1]);
            System.out.println(ConsoleStyle.success("✅ Transaction created."));

        } catch (Exception e) {
            System.out.println(ConsoleStyle.error(" " + e.getMessage()));
        }
    }

    /**
     * Handles updating an existing transaction.
     * Allows optional modification of amount, category, reason, and accounts.
     */
    private void updateTransaction() {
        try {
            AbstractTransaction tx = selectFromList(transactionService.getAllTransactionsFlattened(), "to update");
            Double amount = ConsoleUtils.promptForDouble("New amount (blank to skip)", true);
            String category = ConsoleUtils.selectOrCreateCategory(user.getSortedCategories(), true);
            String reason = ConsoleUtils.prompt("New reason (blank to skip)", true);
            AccountInterface[] acc = getTransactionAccounts(tx.getType());

            transactionService.modify(tx, amount, category, reason, new Date(), acc[0], acc[1]);
            System.out.println(ConsoleStyle.success(" Transaction updated."));

        } catch (Exception e) {
            System.out.println(ConsoleStyle.error(" " + e.getMessage()));
        }
    }

    /**
     * Deletes a transaction selected by the user.
     */
    private void deleteTransaction() {
        try {
            AbstractTransaction tx = selectFromList(transactionService.getAllTransactionsFlattened(), "to delete");
            transactionService.delete(tx);
            System.out.println(ConsoleStyle.success(" Transaction deleted."));

        } catch (Exception e) {
            System.out.println(ConsoleStyle.error(" " + e.getMessage()));
        }
    }

    /**
     * Displays all transactions currently associated with the user.
     */
    private void displayAllTransactions() {
        List<AbstractTransaction> list = transactionService.getAllTransactionsFlattened();
        if (list.isEmpty()) {
            System.out.println(ConsoleStyle.warning(" No transactions available."));
        } else {
            TransactionPrinter.printTransactions(list);
        }
    }

    /**
     * Prompts the user to select source/destination accounts based on transaction type.
     *
     * @param type the type of transaction
     * @return an array with [to, from] accounts in position 0 and 1
     * @throws UserCancelledException if the user cancels the operation
     */
    private AccountInterface[] getTransactionAccounts(TransactionType type) throws UserCancelledException {
        AccountInterface from = null, to = null;
        switch (type) {
            case INCOME -> to = selectFromList(user.getAccountList(), "destination account");
            case EXPENSE -> from = selectFromList(user.getAccountList(), "source account");
            case MOVEMENT -> {
                from = selectFromList(user.getAccountList(), "source account");
                to = selectFromList(user.getAccountList(), "destination account");
            }
        }
        return new AccountInterface[]{to, from};
    }

    /**
     * Displays a list of items and prompts the user to select one.
     *
     * @param list  the list of items
     * @param label a textual description shown to the user
     * @param <T>   the type of items in the list
     * @return the selected item
     * @throws UserCancelledException if the user types 'back'
     */
    private <T> T selectFromList(List<T> list, String label) throws UserCancelledException {
        if (list == null || list.isEmpty()) {
            System.out.println(ConsoleStyle.warning(" No items available."));
            throw new UserCancelledException();
        }

        for (int i = 0; i < list.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, list.get(i).toString());
        }

        while (true) {
            String input = ConsoleUtils.prompt("Enter " + label + " index (or 'back')", false);
            if (input == null || input.equalsIgnoreCase("back")) throw new UserCancelledException();

            try {
                int index = Integer.parseInt(input.trim()) - 1;
                if (index >= 0 && index < list.size()) return list.get(index);
                else System.out.println(ConsoleStyle.error(" Index out of range."));
            } catch (NumberFormatException e) {
                System.out.println(ConsoleStyle.error(" Invalid index."));
            }
        }
    }

    /**
     * Generic method for displaying a menu loop.
     *
     * @param title   the menu title
     * @param options the options to display
     * @param actions the actions corresponding to each menu entry
     * @throws UserCancelledException if the user cancels or exits
     */
    private void menuLoop(String title, String[] options, Runnable... actions) throws UserCancelledException {
        boolean running = true;
        while (running) {
            int choice = ConsoleUtils.showMenu(title, false, options);
            if (choice == -1) return;
            if (choice > actions.length || actions[choice - 1] == null) {
                running = false;
            } else {
                actions[choice - 1].run();
            }
        }
    }

    public void setUser(User user) {
        this.user = user;
    }
}
