package it.finance.sb.clicontroller;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.user.User;
import it.finance.sb.service.AccountService;
import it.finance.sb.utility.AccountPrinter;
import it.finance.sb.utility.ConsoleStyle;
import it.finance.sb.utility.ConsoleUtils;

import java.util.List;

/**
 * CLI controller responsible for managing account-related operations.
 * Provides options to view, create, update, and delete accounts using {@link ConsoleUtils}.
 * Delegates account logic to {@link AccountService} and applies exception shielding.
 */
public class AccountMenuCliController implements MenuCliController {

    public static final String OPERATION_CANCELLED = "Operation cancelled by user.";
    public static final String SESSION_ERROR = "Session error: ";
    public static final String UNEXPECTED_ERROR = "Unexpected error: ";
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
        List<AccountInterface> accounts = accountService.getAllAccount();
        AccountPrinter.printAccounts(accounts);
    }

    /**
     * Prompts the user to create a new account.
     * Applies validation and prints success/error messages.
     */
    private void createAccount() {
        System.out.println(ConsoleStyle.menuTitle("Create A New Account"));
        try {
            String name = ConsoleUtils.prompt("Account name", false);
            Double balance = ConsoleUtils.promptForDouble("Initial balance", false);
            AccounType type = ConsoleUtils.selectEnum(AccounType.class, "Account Type", false);

            AccountInterface acc = accountService.create(type, name, balance);
            System.out.println(ConsoleStyle.success("Created: " + acc));

        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED));
        } catch (DataValidationException e) {
            System.out.println(ConsoleStyle.error("Invalid account data: " + e.getMessage()));
        } catch (AccountOperationException e) {
            System.out.println(ConsoleStyle.error("Account creation error: " + e.getMessage()));
        } catch (UserLoginException e) {
            System.out.println(ConsoleStyle.error(SESSION_ERROR + e.getMessage()));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error(UNEXPECTED_ERROR + e.getMessage()));
        }
    }

    /**
     * Updates an existing account selected by the user.
     */
    private void updateAccount() {
        System.out.println(ConsoleStyle.menuTitle("Select an account to Update"));
        try {
            AccountInterface acc = selectAccount("to update");
            String name = ConsoleUtils.prompt("New name", true);
            Double balance = ConsoleUtils.promptForDouble("New balance", true);
            AccounType type = ConsoleUtils.selectEnum(AccounType.class, "New Type", true);

            accountService.modify(acc, type, name, balance);
            System.out.println(ConsoleStyle.success("Account updated."));

        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED));
        } catch (DataValidationException e) {
            System.out.println(ConsoleStyle.error("Validation error: " + e.getMessage()));
        } catch (AccountOperationException e) {
            System.out.println(ConsoleStyle.error("Could not update account: " + e.getMessage()));
        } catch (UserLoginException e) {
            System.out.println(ConsoleStyle.error(SESSION_ERROR + e.getMessage()));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error(UNEXPECTED_ERROR + e.getMessage()));
        }
    }

    /**
     * Deletes an existing account selected by the user.
     */
    private void deleteAccount() {
        System.out.println(ConsoleStyle.menuTitle("Select an account to Update Delete"));
        try {
            AccountInterface acc = selectAccount("to delete");
            accountService.delete(acc);
            System.out.println(ConsoleStyle.success("Account deleted."));

        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED));
        } catch (AccountOperationException e) {
            System.out.println(ConsoleStyle.error("Could not delete account: " + e.getMessage()));
        } catch (UserLoginException e) {
            System.out.println(ConsoleStyle.error(SESSION_ERROR + e.getMessage()));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error(UNEXPECTED_ERROR + e.getMessage()));
        }
    }

    /**
     * Prompts the user to select an account by index.
     *
     * @param label the selection context (e.g., "to update")
     * @return the selected {@link AccountInterface}
     * @throws UserCancelledException if user cancels or inputs "back"
     */
    private AccountInterface selectAccount(String label) throws UserCancelledException, AccountOperationException {
        var list = accountService.getCurrentUser().getAccountList();
        if (list.isEmpty()) {
            throw new AccountOperationException("No accounts available.");
        }

        for (int i = 0; i < list.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, list.get(i));
        }

        while (true) {
            String input = ConsoleUtils.prompt("Enter account " + label, false);
            if (input == null || input.equalsIgnoreCase("back")) throw new UserCancelledException();

            try {
                int index = Integer.parseInt(input.trim()) - 1;
                if (index >= 0 && index < list.size()) return list.get(index);
                System.out.println(ConsoleStyle.error("Index out of range."));
            } catch (NumberFormatException e) {
                System.out.println(ConsoleStyle.error("Invalid index."));
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
        while (true) {
            int choice = ConsoleUtils.showMenu(title, false, options);
            if (choice == -1 || choice > actions.length || actions[choice - 1] == null) return;
            actions[choice - 1].run();
        }
    }

    public void setUser(User user) {
        this.accountService.setCurrentUser(user);
    }
}
