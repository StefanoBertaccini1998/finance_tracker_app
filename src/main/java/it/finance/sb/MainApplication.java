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
 * Initializes core services and launches the main controller.
 */
public class MainApplication {

    public static void main(String[] args) throws UserCancelledException {
        Logger logger = LoggerFactory.getSafeLogger(MainApplication.class);

        // === Core services ===
        UserService userService = new UserService();
        FinanceAbstractFactory factory = new DefaultFinanceFactory();
        TransactionService transactionService = new TransactionService(userService,factory);
        AccountService accountService = new AccountService(transactionService,factory);
        MementoService mementoService = new MementoService();
        ImporterI<AbstractTransaction> importer = new CsvImporter(factory);
        WriterI<AbstractTransaction> writer = new CsvWriter<>("TransactionId,Type,Amount,From,To,Category,Reason,Date");
        FileIOService fileIOService = new FileIOService(transactionService, userService, importer, writer);

        // === Launch main CLI menu ===
        MainMenuCliController mainMenu = new MainMenuCliController(
                userService,
                mementoService,
                accountService,
                transactionService,
                fileIOService,
                logger
        );

        mainMenu.run();
    }
}
