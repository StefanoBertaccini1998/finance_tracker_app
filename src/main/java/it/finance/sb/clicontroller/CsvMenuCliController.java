package it.finance.sb.clicontroller;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.FileIOException;
import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.service.FileIOService;
import it.finance.sb.utility.ConsoleStyle;
import it.finance.sb.utility.ConsoleUtils;
import it.finance.sb.utility.TransactionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CLI controller responsible for handling CSV import and export operations.
 * This menu allows users to import transactions from a CSV file or export them to a CSV file.
 * It communicates with the {@link FileIOService} to perform the underlying I/O logic
 * and logs relevant information using {@link Logger}.
 * Provides exception shielding and safe user interactions.
 *
 */
public class CsvMenuCliController implements MenuCliController {

    private final FileIOService fileIOService;
    private final Logger logger;

    /**
     * Constructs a new {@code CsvMenuCliController} with the given services.
     *
     * @param fileIOService the service used to import and export transactions
     * @param logger        the logger for reporting I/O errors and user issues
     */
    public CsvMenuCliController(FileIOService fileIOService, Logger logger) {
        this.fileIOService = fileIOService;
        this.logger = logger;
    }

    /**
     * Displays the CSV menu to the user and routes actions based on the user's selection.
     *
     * @throws UserCancelledException if the user exits the menu
     */
    @Override
    public void show() throws UserCancelledException {
        menuLoop("CSV Menu",
                new String[]{"Import Transactions", "Export Transactions", "Back"},
                this::importTransactions,
                this::exportTransactions,
                null
        );
    }

    /**
     * Handles the import of transactions from a CSV file.
     * Asks the user for the path and additional import options.
     */
    private void importTransactions() {
        try {
            Path path = Path.of(ConsoleUtils.prompt("Enter CSV path", false));
            boolean autoCreate = ConsoleUtils.prompt("Auto-create missing accounts? (y/n)", false).equalsIgnoreCase("y");
            boolean skipErrors = ConsoleUtils.prompt("Skip errors? (y/n)", false).equalsIgnoreCase("y");

            List<AbstractTransaction> imported = fileIOService.importTransactions(path, autoCreate, skipErrors);
            System.out.println(ConsoleStyle.success(" Transactions imported."));
            TransactionPrinter.printTransactions(imported);

        } catch (DataValidationException e) {
            logger.log(Level.SEVERE, "Import failed (validation)", e);
            System.out.println(ConsoleStyle.error(" Failed to import transactions. Validation issue."));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Import failed (IO)", e);
            System.out.println(ConsoleStyle.error(" Failed to import transactions. File error."));
        } catch (UserLoginException e) {
            logger.log(Level.SEVERE, "Import failed (user not logged in)", e);
            System.out.println(ConsoleStyle.error(" User must be logged in."));
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
        } catch (FileIOException e) {
            logger.log(Level.SEVERE, "Import failed, error during file parsing", e);
            System.out.println(ConsoleStyle.error(" Error during file parsing"));
        }
    }

    /**
     * Handles the export of transactions to a user-specified CSV file path.
     */
    private void exportTransactions() {
        try {
            Path path = Path.of(ConsoleUtils.prompt("Enter output CSV path", false));
            fileIOService.exportTransactions(path);
            System.out.println(ConsoleStyle.success(" Exported to: " + path));
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
        } catch (UserLoginException e) {
            logger.log(Level.SEVERE, "Export failed (user not logged in)", e);
            System.out.println(ConsoleStyle.error(" User must be logged in."));
        } catch (FileIOException e) {
            logger.log(Level.SEVERE, "Export failed (file issue)", e);
            System.out.println(ConsoleStyle.error(" Failed to export transactions."));
        }
    }

    /**
     * Displays a generic menu loop to the user and dispatches actions accordingly.
     *
     * @param title   the title of the menu
     * @param options the list of options to show
     * @param actions the actions corresponding to each option
     * @throws UserCancelledException if the user exits the menu
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
}
