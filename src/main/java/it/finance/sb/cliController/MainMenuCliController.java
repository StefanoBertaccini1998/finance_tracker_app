package it.finance.sb.cliController;

import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.model.user.User;
import it.finance.sb.service.AccountService;
import it.finance.sb.service.FileIOService;
import it.finance.sb.service.MementoService;
import it.finance.sb.service.TransactionService;
import it.finance.sb.service.UserService;
import it.finance.sb.utility.ConsoleStyle;
import it.finance.sb.utility.ConsoleUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main CLI controller responsible for coordinating all sub-menus.
 * This controller loads the user, then delegates to account, transaction, and CSV menus.
 * It also allows saving user state before exiting.
 */
public class MainMenuCliController {

    private final Logger logger;
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
     * @param logger             the logging utility
     */
    public MainMenuCliController(
            UserService userService,
            MementoService mementoService,
            AccountService accountService,
            TransactionService transactionService,
            FileIOService fileIOService,
            Logger logger
    ) {
        this.logger = logger;
        this.mementoService = mementoService;

        this.userMenuCliController = new UserMenuCliController(userService, mementoService, logger);
        this.csvMenuCliController = new CsvMenuCliController(fileIOService, logger);
        this.accountMenuCliController = new AccountMenuCliController(accountService);
        this.transactionMenuCliController = new TransactionMenuCliController(transactionService, null); // user will be set later
    }

    /**
     * Starts the main application flow: login, menu, exit.
     */
    public void run() throws UserCancelledException {
        System.out.println(ConsoleStyle.header("Welcome to 💸 FinanceTrack!"));

        // Step 1: Login user
        userMenuCliController.show();
        this.currentUser = userMenuCliController.getCurrentUser();

        // Propagate user to transaction/account services
        transactionMenuCliController.setUser(currentUser);

        // Step 2: Show main menu
        showMainMenu();

        // Step 3: Exit
        System.out.println(ConsoleStyle.info("Thank you for using FinanceTrack!"));
    }

    /**
     * Displays the main application menu.
     */
    private void showMainMenu() throws UserCancelledException {
        menuLoop("Main Menu",
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
            System.out.println(ConsoleStyle.back(" Returning to main menu."));
        }
    }

    /**
     * Opens the transaction submenu.
     */
    private void showTransactionMenu() {
        try {
            transactionMenuCliController.show();
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Returning to main menu."));
        }
    }

    /**
     * Opens the CSV import/export submenu.
     */
    private void showCsvMenu() {
        try {
            csvMenuCliController.show();
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Returning to main menu."));
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
     * @param title   menu title
     * @param options menu options
     * @param actions corresponding action handlers
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
