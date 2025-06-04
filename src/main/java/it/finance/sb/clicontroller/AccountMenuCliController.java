package it.finance.sb.clicontroller;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.user.User;
import it.finance.sb.service.AccountService;
import it.finance.sb.utility.AccountPrinter;
import it.finance.sb.utility.ConsoleStyle;
import it.finance.sb.utility.ConsoleUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CLI controller responsible for managing account-related operations.
 * Provides options to view, create, update, and delete accounts.
 * Delegates business logic to AccountService and handles exception shielding.
 */
public class AccountMenuCliController extends MenuCliController {

    public static final String OPERATION_CANCELLED = "Operation cancelled by user.";
    public static final String SESSION_ERROR = "Session error: ";
    public static final String UNEXPECTED_ERROR = "Unexpected error: ";
    private final AccountService accountService;
    private static final Logger logger = LoggerFactory.getSafeLogger(AccountMenuCliController.class);

    /**
     * Constructor to initialize account controller with service dependency.
     *
     * @param accountService the business service for account operations
     */
    public AccountMenuCliController(AccountService accountService) {
        this.accountService = accountService;
    }

    /* Template overrides */
    @Override
    protected String title() {
        return "Account Menu";
    }

    @Override
    protected List<MenuItem> menuItems() {
        return List.of(
                new MenuItem("View Accounts", this::displayAccounts),
                new MenuItem("Create Account", this::createAccount),
                new MenuItem("Update Account", this::updateAccount),
                new MenuItem("Delete Account", this::deleteAccount),
                new MenuItem("Back", this::requestClose)          // no-op exits renderLoop()
        );
    }

    /**
     * Displays all accounts associated with the current user.
     */
    private void displayAccounts() {
        List<AccountInterface> accounts = accountService.getAllAccount();
        AccountPrinter.printAccounts(accounts);
        logger.info(() -> "Displayed " + accounts.size() + " accounts");
    }


    /**
     * Prompts user for account details and creates a new account.
     */
    private void createAccount() {
        logger.info("Started create account flow");
        System.out.println(ConsoleStyle.menuTitle("Create A New Account"));
        try {
            String name = ConsoleUtils.prompt("Account name", false);
            Double balance = ConsoleUtils.promptForDouble("Initial balance", false);
            AccounType type = ConsoleUtils.selectEnum(AccounType.class, "Account Type", false);

            AccountInterface acc = accountService.create(type, name, balance);
            System.out.println(ConsoleStyle.success("Created: " + acc));
            logger.info("Completed create account flow");
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED));
            logger.fine("Account creation cancelled by user.");
        } catch (DataValidationException e) {
            System.out.println(ConsoleStyle.error("Invalid account data: " + e.getMessage()));
            logger.warning("Validation error on account creation: " + e.getMessage());
        } catch (AccountOperationException e) {
            System.out.println(ConsoleStyle.error("Account creation error: " + e.getMessage()));
            logger.warning("Operation error on account creation: " + e.getMessage());
        } catch (UserLoginException e) {
            System.out.println(ConsoleStyle.error(SESSION_ERROR + e.getMessage()));
            logger.warning("Session error on account creation: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error(UNEXPECTED_ERROR + e.getMessage()));
            logger.log(Level.SEVERE, "Unexpected error in createAccount", e);
        }
    }

    /**
     * Prompts user to update an existing account.
     */
    private void updateAccount() {
        logger.info("Started update account flow");
        System.out.println(ConsoleStyle.menuTitle("Select an account to Update"));
        try {
            AccountInterface acc = selectAccount("to update");
            String name = ConsoleUtils.prompt("New name", true);
            Double balance = ConsoleUtils.promptForDouble("New balance", true);
            AccounType type = ConsoleUtils.selectEnum(AccounType.class, "New Type", true);

            accountService.modify(acc, type, name, balance);
            System.out.println(ConsoleStyle.success("Account updated."));
            logger.info("Completed update account flow");
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED));
            logger.fine("Account update cancelled by user.");
        } catch (AccountOperationException e) {
            System.out.println(ConsoleStyle.error("Could not update account: " + e.getMessage()));
            logger.warning("Operation error during account update: " + e.getMessage());
        } catch (UserLoginException e) {
            System.out.println(ConsoleStyle.error(SESSION_ERROR + e.getMessage()));
            logger.warning("Session error during account update: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error(UNEXPECTED_ERROR + e.getMessage()));
            logger.log(Level.SEVERE, "Unexpected error in updateAccount", e);
        }
    }

    /**
     * Prompts user to delete an existing account.
     */
    private void deleteAccount() {
        logger.info("Started delete account flow");
        System.out.println(ConsoleStyle.menuTitle("Select an account to Delete"));
        try {
            AccountInterface acc = selectAccount("to delete");
            accountService.delete(acc);
            System.out.println(ConsoleStyle.success("Account deleted."));
            logger.info("Completed delete account flow");
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED));
            logger.fine("Account deletion cancelled by user.");
        } catch (AccountOperationException e) {
            System.out.println(ConsoleStyle.error("Could not delete account: " + e.getMessage()));
            logger.warning("Operation error during account deletion: " + e.getMessage());
        } catch (UserLoginException e) {
            System.out.println(ConsoleStyle.error(SESSION_ERROR + e.getMessage()));
            logger.warning("Session error during account deletion: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error(UNEXPECTED_ERROR + e.getMessage()));
            logger.log(Level.SEVERE, "Unexpected error in deleteAccount", e);
        }
    }

    /**
     * Prompts user to select an account from the list.
     *
     * @param label context of selection (e.g., "to update")
     * @return the selected account
     * @throws UserCancelledException if user cancels input
     */
    private AccountInterface selectAccount(String label)
            throws UserCancelledException, AccountOperationException {
        List<AccountInterface> list = accountService.getCurrentUser().getAccountList();
        if (list.isEmpty()) throw new AccountOperationException("No accounts available.");
        return ConsoleUtils.selectFromList(list, "account " + label, false);
    }

    /**
     * Sets the current user context.
     *
     * @param user the logged-in user
     */
    public void setUser(User user) {
        this.accountService.setCurrentUser(user);
    }

}
