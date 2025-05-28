package it.finance.sb.clicontroller;

import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.user.User;
import it.finance.sb.service.AccountService;
import it.finance.sb.service.FileIOService;
import it.finance.sb.service.MementoService;
import it.finance.sb.service.TransactionService;
import it.finance.sb.service.UserService;
import it.finance.sb.utility.ConsoleStyle;
import it.finance.sb.utility.ConsoleUtils;

import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main CLI controller responsible for coordinating all sub-menus.
 * This controller loads the user, then delegates to account, transaction, and CSV menus.
 * It also allows saving user state before exiting.
 */
public class MainMenuCliController {

    public static final String RETURNING_TO_MAIN_MENU = " Returning to main menu.";

    public static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("emoji.enabled", "false"));
    private static final Logger logger = LoggerFactory.getSafeLogger(MainMenuCliController.class);
    private final UserMenuCliController userMenuCliController;
    private final AccountMenuCliController accountMenuCliController;
    private final TransactionMenuCliController transactionMenuCliController;
    private final CsvMenuCliController csvMenuCliController;
    private final MementoService mementoService;

    private User currentUser;

    /**
     * Constructs the MainMenuCliController with all required components.
     *
     * @param userService        the user service
     * @param mementoService     the snapshot manager
     * @param accountService     the account service
     * @param transactionService the transaction service
     * @param fileIOService      the file import/export service
     */
    public MainMenuCliController(
            UserService userService,
            MementoService mementoService,
            AccountService accountService,
            TransactionService transactionService,
            FileIOService fileIOService
    ) {
        this.mementoService = mementoService;

        this.userMenuCliController = new UserMenuCliController(userService, mementoService);
        this.csvMenuCliController = new CsvMenuCliController(fileIOService);
        this.accountMenuCliController = new AccountMenuCliController(accountService);
        this.transactionMenuCliController = new TransactionMenuCliController(transactionService); // user will be set later
    }


    /**
     * Starts the main application flow: login, menu, exit.
     */
    public void run() {
        try {
            System.out.println(ConsoleStyle.header("Welcome to " + (ENABLED ? "💸":"")+" FinanceTrack!"));
            logger.info("Main menu started");

            // Step 1: Login user
            userMenuCliController.show();
            this.currentUser = userMenuCliController.getCurrentUser();

            // Step 2: Propagate user to services
            transactionMenuCliController.setUser(currentUser);
            accountMenuCliController.setUser(currentUser);
            csvMenuCliController.setUser(currentUser);

            // Step 3: Show main menu
            showMainMenu();

            // Step 4: Exit
            System.out.println(ConsoleStyle.info("Thank you for using FinanceTrack!"));
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back("Exited by user."));
            logger.info("User cancelled operation.");
        } catch (NoSuchElementException | IllegalStateException e) {
            System.out.println(ConsoleStyle.error("Session interrupted. Are you exiting with Ctrl+C?"));
            logger.warning("CLI interrupted: " + e.getMessage());

            // AutoSave
            if (currentUser != null) {
                try {
                    mementoService.saveUser(currentUser);
                    System.out.println(ConsoleStyle.success("Progress auto-saved before shutdown."));
                } catch (Exception ex) {
                    System.out.println(ConsoleStyle.error("Failed to auto-save user: " + ex.getMessage()));
                    logger.severe("Auto-save failed: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("Unexpected fatal error."));
            logger.log(Level.SEVERE, "Unhandled error in MainMenuCliController.run()", e);
        }
    }

    /**
     * Displays the main application menu.
     */
    private void showMainMenu() throws UserCancelledException {
        menuLoop(
                new String[]{
                        "Manage Accounts",
                        "Manage Transactions",
                        "Import/Export CSV",
                        "Save Current User",
                        "Exit"
                },
                this::showAccountMenu,
                this::showTransactionMenu,
                this::showCsvMenu,
                this::saveUser,
                null
        );
    }

    /**
     * Opens the account submenu.
     */
    private void showAccountMenu() {
        try {
            accountMenuCliController.show();
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(RETURNING_TO_MAIN_MENU));
        }
    }

    /**
     * Opens the transaction submenu.
     */
    private void showTransactionMenu() {
        try {
            transactionMenuCliController.show();
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(RETURNING_TO_MAIN_MENU));
        }
    }

    /**
     * Opens the CSV import/export submenu.
     */
    private void showCsvMenu() {
        try {
            csvMenuCliController.show();
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(RETURNING_TO_MAIN_MENU));
        }
    }

    /**
     * Triggers a save of the current user's snapshot.
     */
    private void saveUser() {
        try {
            mementoService.saveUser(currentUser);
            System.out.println(ConsoleStyle.success(" User saved."));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "User save failed", e);
            System.out.println(ConsoleStyle.error(" Failed to save user."));
        }
    }

    /**
     * Generic method for displaying a menu and executing related actions.
     *
     * @param options menu options
     * @param actions corresponding action handlers
     */
    private void menuLoop(String[] options, Runnable... actions) throws UserCancelledException {
        while (true) {
            int choice = ConsoleUtils.showMenu("Main Menu", false, options);
            if (choice == -1 || choice > actions.length || actions[choice - 1] == null) return;
            actions[choice - 1].run();
        }
    }
}
