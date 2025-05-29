package it.finance.sb.clicontroller;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.FileIOException;
import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.user.User;
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
 */
public class CsvMenuCliController extends MenuCliController {

    private final FileIOService fileIOService;
    private static final Logger logger = LoggerFactory.getSafeLogger(CsvMenuCliController.class);


    /**
     * Constructs a new {@code CsvMenuCliController} with the given services.
     *
     * @param fileIOService the service used to import and export transactions
     */
    public CsvMenuCliController(FileIOService fileIOService) {
        this.fileIOService = fileIOService;
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
        logger.info("Started import transaction flow");
        try {
            //Prompt for path -> auto create account -> skip error
            Path path = Path.of(ConsoleUtils.prompt("Enter CSV path", false));
            boolean autoCreate = ConsoleUtils.prompt("Auto-create missing accounts? (y/n)", false).equalsIgnoreCase("y");
            boolean skipErrors = ConsoleUtils.prompt("Skip errors? (y/n)", false).equalsIgnoreCase("y");

            //Prompt for path -> auto create account -> skip error
            List<AbstractTransaction> imported = fileIOService.importTransactions(path, autoCreate, skipErrors);
            System.out.println(ConsoleStyle.success("Transactions imported successfully."));
            //Show imported transaction
            TransactionPrinter.printTransactions(imported);
            logger.info("Completed import transaction flow");
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back("Import cancelled by user."));
        } catch (UserLoginException e) {
            logger.log(Level.SEVERE, "Import failed: no user logged in", e);
            System.out.println(ConsoleStyle.error("Please log in before importing transactions."));
        } catch (FileIOException e) {
            logger.log(Level.SEVERE, "Import failed during file parsing", e);
            System.out.println(ConsoleStyle.error("The file format appears invalid. Please check the CSV content and structure."));

            //Catch the error line from the custom exception
            List<String> errors = e.getErrorLog();
            if (errors != null && !errors.isEmpty()) {
                System.out.println(ConsoleStyle.warning("Details of rows skipped or failed before interruption:"));
                for (String lineError : errors) {
                    System.out.println(ConsoleStyle.info(" - " + lineError));
                }
            }
        } catch (DataValidationException e) {
            logger.log(Level.SEVERE, "Import failed due to invalid data", e);
            System.out.println(ConsoleStyle.error("One or more rows contain invalid or incomplete data. Please review your CSV."));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Import failed due to IO issue", e);
            System.out.println(ConsoleStyle.error("The file could not be read. Check the path and ensure the file exists."));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected import error", e);
            System.out.println(ConsoleStyle.error("An unexpected error occurred during import. Please try again."));
        }
    }

    /**
     * Handles the export of transactions to a user-specified CSV file path.
     */
    private void exportTransactions() {
        logger.info("Started export transaction flow");
        try {
            Path path = Path.of(ConsoleUtils.prompt("Enter output CSV file path", false));
            fileIOService.exportTransactions(path);
            System.out.println(ConsoleStyle.success("Transactions successfully exported to: " + path));
            logger.info("Completed export transaction flow");
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back("Export cancelled by user."));
        } catch (UserLoginException e) {
            logger.log(Level.SEVERE, "Export failed: no user logged in", e);
            System.out.println(ConsoleStyle.error("Please log in before exporting transactions."));
        } catch (FileIOException e) {
            logger.log(Level.SEVERE, "Export failed due to file access issue", e);
            System.out.println(ConsoleStyle.error("Failed to write the CSV file. Check file permissions or path validity."));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected export error", e);
            System.out.println(ConsoleStyle.error("An unexpected error occurred during export. Please try again."));
        }
    }

    /**
     * Sets the current user context for the controller.
     * This is required before performing any import/export actions.
     *
     * @param user the logged-in user
     */
    public void setUser(User user) {
        this.fileIOService.setCurrentUser(user);
    }
}
