package it.finance.sb.clicontroller;

import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.user.User;
import it.finance.sb.service.*;
import it.finance.sb.utility.ConsoleStyle;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main CLI controller responsible for coordinating all sub-menus.
 * This controller loads the user, then delegates to account, transaction, and CSV menus.
 * It also allows saving user state before exiting.
 */
public class MainMenuCliController extends MenuCliController {

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

    /* ─── Template overrides ─── */
    @Override
    protected void preMenu() {
        System.out.println(ConsoleStyle.header(
                "Welcome to " + (ENABLED ? "💸" : "") + " FinanceTrack!"));
        logger.info("Main menu started");
    }

    @Override
    protected void postMenu() {
        System.out.println(ConsoleStyle.info("Thank you for using FinanceTrack!"));
    }

    @Override
    protected String title() {
        return "Main Menu";
    }

    @Override
    protected List<MenuItem> menuItems() {
        return List.of(
                new MenuItem("Manage Accounts", this::showAccountMenu),
                new MenuItem("Manage Transactions", this::showTransactionMenu),
                new MenuItem("Import/Export CSV", this::showCsvMenu),
                new MenuItem("Save Current User", this::saveUser),
                new MenuItem("Exit", this::requestClose)     // no-op exits loop
        );
    }

    /**
     * Starts the main application flow: login, menu, exit.
     */
    public void run() {
        try {
            // Login
            userMenuCliController.display();
            currentUser = userMenuCliController.getCurrentUser();

            if (currentUser == null) {                    // user picked “Close”
                System.out.println(ConsoleStyle.back("Closing the application"));
                return;                                   // skip main menu entirely
            }

            // Wire user into sub-controllers
            accountMenuCliController.setUser(currentUser);
            transactionMenuCliController.setUser(currentUser);
            csvMenuCliController.setUser(currentUser);

            // Enter main loop
            this.display();                                  // Template-Method
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back("Closing the application"));
            logger.info("User cancelled operation.");
        } catch (NoSuchElementException | IllegalStateException e) {
            handleInterruption(e);
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("Unexpected fatal error."));
            logger.log(Level.SEVERE, "Unhandled error in MainMenuCliController.run()", e);
        }
    }

    /**
     * Opens the account submenu.
     */
    private void showAccountMenu() {
        try {
            accountMenuCliController.display();
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(RETURNING_TO_MAIN_MENU));
        }
    }

    /**
     * Opens the transaction submenu.
     */
    private void showTransactionMenu() {
        try {
            transactionMenuCliController.display();
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(RETURNING_TO_MAIN_MENU));
        }
    }

    /**
     * Opens the CSV import/export submenu.
     */
    private void showCsvMenu() {
        try {
            csvMenuCliController.display();
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
     * Handles an asynchronous console interruption (Ctrl-C, broken pipe, etc.).
     * Shows a friendly message, logs the incident, and attempts an auto-save.
     */
    private void handleInterruption(RuntimeException e) {
        System.out.println(ConsoleStyle.error(
                "Session interrupted. Are you exiting with Ctrl+C?"));
        logger.warning("CLI interrupted: " + e.getMessage());

        if (currentUser != null) {
            try {
                mementoService.saveUser(currentUser);
                System.out.println(ConsoleStyle.success(
                        "Progress auto-saved before shutdown."));
            } catch (Exception ex) {
                System.out.println(ConsoleStyle.error(
                        "Failed to auto-save user: " + ex.getMessage()));
                logger.log(Level.SEVERE, "Auto-save failed", ex);
            }
        }
    }

}
