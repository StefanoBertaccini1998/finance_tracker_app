package it.finance.sb;

import it.finance.sb.clicontroller.MainMenuCliController;
import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.factory.DefaultFinanceFactory;
import it.finance.sb.factory.FinanceAbstractFactory;
import it.finance.sb.io.CsvImporter;
import it.finance.sb.io.CsvWriter;
import it.finance.sb.io.ImporterI;
import it.finance.sb.io.WriterI;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.service.*;

import java.util.logging.Logger;

/**
 * Main entry point of the FinanceTrack CLI application.
 * <p>
 * This class is responsible for initializing all core components and services,
 * including user management, transaction handling, account management,
 * CSV import/export logic, memento state handling, and logging. Once the setup
 * is complete, it delegates control to the main CLI menu controller.
 * <p>
 * The design applies several design patterns such as Factory, Memento,
 * and Interface Segregation via custom IO interfaces.
 */
public class MainApplication {

    /**
     * Launches the FinanceTrack CLI application.
     *
     * @param args CLI arguments (not used in this application)
     */
    public static void main(String[] args) {
        // Initialize a safe logger instance for the application
        Logger logger = LoggerFactory.getSafeLogger(MainApplication.class);
        logger.info("Starting application...");

        // Instantiate core domain services and dependencies
        UserService userService = new UserService();
        FinanceAbstractFactory factory = new DefaultFinanceFactory();
        TransactionService transactionService = new TransactionService(userService, factory);
        AccountService accountService = new AccountService(transactionService, factory);
        MementoService mementoService = new MementoService();

        // Configure CSV importer and writer with appropriate headers
        ImporterI<AbstractTransaction> importer = new CsvImporter(factory);
        WriterI<AbstractTransaction> writer = new CsvWriter<>(
                "TransactionId,Type,Amount,From,To,Category,Reason,Date"
        );

        // Setup file I/O service combining importer and writer
        FileIOService fileIOService = new FileIOService(
                transactionService,
                userService,
                importer,
                writer
        );

        // Launch the main CLI menu controller to handle user input
        MainMenuCliController mainMenu = new MainMenuCliController(
                userService,
                mementoService,
                accountService,
                transactionService,
                fileIOService
        );
        logger.info("Application configured correctly.");
        mainMenu.run();
        logger.info("Closing application.");
    }
}
