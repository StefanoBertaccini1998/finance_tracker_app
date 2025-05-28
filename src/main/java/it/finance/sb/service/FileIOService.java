package it.finance.sb.service;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.FileIOException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.io.CsvImporter;
import it.finance.sb.io.ImporterI;
import it.finance.sb.io.WriterI;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.utility.InputSanitizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * FileIOService handles import and export of user transactions.
 * Applies SRP, OCP (via pluggable ImporterI/WriterI), and exception shielding.
 */
public class FileIOService extends BaseService {

    private static final Logger logger = LoggerFactory.getSafeLogger(FileIOService.class);

    private final TransactionService transactionService;
    private final UserService userService;
    private final ImporterI<AbstractTransaction> transactionImporter;
    private final WriterI<AbstractTransaction> transactionWriter;

    public FileIOService(TransactionService transactionService,
                         UserService userService,
                         ImporterI<AbstractTransaction> transactionImporter,
                         WriterI<AbstractTransaction> transactionWriter) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.transactionImporter = transactionImporter;
        this.transactionWriter = transactionWriter;
    }

    /**
     * Imports validated transactions from a CSV and updates user context.
     * Supports error recovery and dynamic account creation.
     *
     * @param filePath           path to CSV file
     * @param autoCreateAccounts allow creation of missing accounts
     * @param skipErrors         continue on bad lines
     * @return list of successfully imported transactions
     */
    public List<AbstractTransaction> importTransactions(Path filePath, boolean autoCreateAccounts, boolean skipErrors)
            throws UserLoginException, DataValidationException, IOException {

        requireLoggedInUser();

        Map<String, AccountInterface> accountMap = buildAccountLookup();
        List<String> errorLog = new ArrayList<>();

        try {
            List<AbstractTransaction> imported = transactionImporter.importFrom(
                    filePath, accountMap, autoCreateAccounts, skipErrors, errorLog
            );

            if (autoCreateAccounts && transactionImporter instanceof CsvImporter importerImpl) {
                handleAutoCreatedAccounts(importerImpl.getNewlyCreatedAccounts());
            }

            for (AbstractTransaction tx : imported) {
                safelyAddTransaction(tx, errorLog);
            }

            if (!errorLog.isEmpty()) {
                logger.warning(() -> "Some entries failed:\n" + String.join("\n", errorLog));
            }

            logger.info(() -> "Imported " + imported.size() + " transactions from: " + filePath);
            return imported;

        } catch (DataValidationException | IOException e) {
            throw new FileIOException("Failed to import: " + e.getMessage(), e, errorLog);
        } catch (Exception e) {
            throw new FileIOException("Unexpected error during import", e, errorLog);
        }
    }

    /**
     * Exports all current user's transactions to a given path.
     */
    public void exportTransactions(Path outputPath) throws FileIOException, UserLoginException {
        requireLoggedInUser();

        try {
            List<AbstractTransaction> allTxs = transactionService.getAllTransactionsFlattened();
            transactionWriter.exportToFile(allTxs, outputPath);
            logger.info(() -> "Exported " + allTxs.size() + " transactions to: " + outputPath);
        } catch (Exception e) {
            throw new FileIOException("Failed to export transactions.", e);
        }
    }

    /**
     * Maps user's accounts by name for importer resolution.
     */
    private Map<String, AccountInterface> buildAccountLookup() {
        return getCurrentUser().getAccountList().stream()
                .collect(Collectors.toMap(AccountInterface::getName, Function.identity()));
    }

    /**
     * Adds a category to the user if missing.
     */
    private void updateUserCategoryIfNeeded(AbstractTransaction tx) throws UserLoginException {
        String category = tx.getCategory();
        if (category != null && !category.isBlank() && !getCurrentUser().isCategoryAllowed(category)) {
            userService.addCategory(category);
            logger.info(() -> "Added new category during import: " + category);
        }
    }

    /**
     * Validates and adds auto-created accounts.
     */
    private void handleAutoCreatedAccounts(List<AccountInterface> accounts) {
        for (AccountInterface account : accounts) {
            try {
                InputSanitizer.validate(account);
                getCurrentUser().addAccount(account);
                logger.info("Auto-added account from import: " + account.getName());
            } catch (Exception e) {
                logger.warning("Failed to add auto-created account: " + e.getMessage());
            }
        }
    }

    /**
     * Validates and adds a transaction, logging failures.
     */
    private void safelyAddTransaction(AbstractTransaction tx, List<String> errorLog) {
        try {
            InputSanitizer.validate(tx);
            updateUserCategoryIfNeeded(tx);
            getCurrentUser().addTransaction(tx);
        } catch (Exception e) {
            errorLog.add("❌ Skipped invalid transaction: " + e.getMessage());
            logger.warning("Skipped malformed transaction: " + e.getMessage());
        }
    }
}
