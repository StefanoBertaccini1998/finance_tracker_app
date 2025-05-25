package it.finance.sb.cliController;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.service.AccountService;
import it.finance.sb.utility.ConsoleStyle;
import it.finance.sb.utility.ConsoleUtils;

/**
 * CLI controller responsible for managing account-related operations.
 * Provides options to view, create, update, and delete accounts using {@link ConsoleUtils}.
 * Delegates account logic to {@link AccountService} and applies exception shielding.
 */
public class AccountMenuCliController implements MenuCliController {

    private final AccountService accountService;

    /**
     * Constructs the controller with the required service.
     *
     * @param accountService the account business service
     */
    public AccountMenuCliController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Displays the account menu.
     */
    @Override
    public void show() throws UserCancelledException {
        menuLoop("Account Menu",
                new String[]{"View Accounts", "Create Account", "Update Account", "Delete Account", "Back"},
                this::displayAccounts,
                this::createAccount,
                this::updateAccount,
                this::deleteAccount,
                null
        );
    }

    /**
     * Displays all current user's accounts.
     */
    private void displayAccounts() {
        accountService.displayAllAccount();
    }

    /**
     * Prompts the user to create a new account.
     * Applies validation and prints success/error messages.
     */
    private void createAccount() {
        try {
            String name = ConsoleUtils.prompt("Account name", false);
            Double balance = ConsoleUtils.promptForDouble("Initial balance", false);
            AccounType type = ConsoleUtils.selectEnum(AccounType.class, "Account Type", false);

            AccountInterface acc = accountService.create(type, name, balance);
            System.out.println(ConsoleStyle.success(" Created: " + acc));

        } catch (DataValidationException e) {
            System.out.println(ConsoleStyle.error(" Check account details: " + e.getMessage()));
        } catch (AccountOperationException | UserLoginException e) {
            System.out.println(ConsoleStyle.error(" " + e.getMessage()));
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
        }
    }

    /**
     * Updates an existing account selected by the user.
     */
    private void updateAccount() {
        try {
            AccountInterface acc = selectAccount("to update");
            String name = ConsoleUtils.prompt("New name (blank to skip)", true);
            AccounType type = ConsoleUtils.selectEnum(AccounType.class, "New Type", true);
            Double balance = ConsoleUtils.promptForDouble("New balance (blank to skip)", true);

            accountService.modify(acc, type, name, balance);
            System.out.println(ConsoleStyle.success(" Account updated."));

        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
        } catch (AccountOperationException | UserLoginException | DataValidationException e) {
            System.out.println(ConsoleStyle.error(" Could not update account: " + e.getMessage()));
        }
    }

    /**
     * Deletes an existing account selected by the user.
     */
    private void deleteAccount() {
        try {
            AccountInterface acc = selectAccount("to delete");
            accountService.delete(acc);
            System.out.println(ConsoleStyle.success(" Account deleted."));

        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
        } catch (AccountOperationException | UserLoginException e) {
            System.out.println(ConsoleStyle.error(" " + e.getMessage()));
        }
    }

    /**
     * Prompts the user to select an account by index.
     *
     * @param label the selection context (e.g., "to update")
     * @return the selected {@link AccountInterface}
     * @throws UserCancelledException if user cancels or inputs "back"
     */
    private AccountInterface selectAccount(String label) throws UserCancelledException {
        var list = accountService.getCurrentUser().getAccountList();
        if (list.isEmpty()) {
            System.out.println(ConsoleStyle.warning(" No accounts available."));
            throw new UserCancelledException();
        }

        for (int i = 0; i < list.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, list.get(i));
        }

        while (true) {
            String input = ConsoleUtils.prompt("Enter account " + label + " index (or 'back')", false);
            if (input == null || input.equalsIgnoreCase("back")) throw new UserCancelledException();

            try {
                int index = Integer.parseInt(input.trim()) - 1;
                if (index >= 0 && index < list.size()) return list.get(index);
                System.out.println(ConsoleStyle.error(" Index out of range."));
            } catch (NumberFormatException e) {
                System.out.println(ConsoleStyle.error(" Invalid index."));
            }
        }
    }

    /**
     * General-purpose menu loop for CLI controllers.
     *
     * @param title   menu title
     * @param options displayed options
     * @param actions corresponding actions
     */
    private void menuLoop(String title, String[] options, Runnable... actions) throws UserCancelledException {
        boolean running = true;
        while (running) {
            int choice = ConsoleUtils.showMenu(title, false, options);
            if (choice == -1 || choice > actions.length || actions[choice - 1] == null) return;

            actions[choice - 1].run();
        }
    }
}
