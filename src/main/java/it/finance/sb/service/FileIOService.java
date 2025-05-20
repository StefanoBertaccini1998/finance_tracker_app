package it.finance.sb.service;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.FileIOException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.io.CsvImporterI;
import it.finance.sb.io.CsvWriter;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.utility.InputSanitizer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FileIOService extends BaseService {

    private static final Logger logger = LoggerFactory.getInstance().getLogger(FileIOService.class);

    private final TransactionService transactionService;
    private final UserService userService;
    private final CsvImporterI<AbstractTransaction> transactionImporter;
    private final CsvWriter<AbstractTransaction> transactionWriter;

    public FileIOService(TransactionService transactionService,
                         UserService userService,
                         CsvImporterI<AbstractTransaction> transactionImporter,
                         CsvWriter<AbstractTransaction> transactionWriter) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.transactionImporter = transactionImporter;
        this.transactionWriter = transactionWriter;
    }

    /**
     * Imports a list of transactions from a CSV file and adds them to the user.
     *
     * @return the imported transaction list
     */
    public List<AbstractTransaction> importTransactions(Path filePath, boolean autoCreateAccounts, boolean skipErrors)
            throws FileIOException, UserLoginException, DataValidationException {

        requireLoggedInUser();

        Map<String, AccountInterface> accountMap = buildAccountLookup();
        List<String> errorLog = new ArrayList<>();

        try {
            List<AbstractTransaction> imported = transactionImporter.importFrom(
                    filePath, accountMap, autoCreateAccounts, skipErrors, errorLog
            );

            for (AbstractTransaction tx : imported) {
                try {
                    InputSanitizer.validate(tx);
                    updateUserCategoryIfNeeded(tx);
                    getCurrentUser().addTransaction(tx);
                } catch (Exception e) {
                    errorLog.add("❌ Skipped invalid transaction: " + e.getMessage());
                    logger.warning("[FileIOService] Skipped malformed transaction: " + e.getMessage());
                }
            }

            if (!errorLog.isEmpty()) {
                logger.warning("[FileIOService] Some entries failed:\n" + String.join("\n", errorLog));
            }

            logger.info("[FileIOService] Imported " + imported.size() + " transactions from: " + filePath);
            return imported;
        } catch (DataValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[FileIOService] Failed to import: " + e.getMessage(), e);
            throw new FileIOException("Failed to import transactions.", e);
        }
    }

    /**
     * Exports all transactions of current user to CSV.
     */
    public void exportTransactions(Path outputPath) throws FileIOException, UserLoginException {
        requireLoggedInUser();

        try {
            List<AbstractTransaction> allTxs = transactionService.getAllTransactionsFlattened();

            transactionWriter.exportToFile(allTxs, outputPath);

            logger.info("[FileIOService] Exported " + allTxs.size() + " transactions to: " + outputPath);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[FileIOService] Export failed: " + e.getMessage(), e);
            throw new FileIOException("Failed to export transactions.", e);
        }
    }

    /**
     * Build account lookup map from user's account list
     */
    private Map<String, AccountInterface> buildAccountLookup() {
        return getCurrentUser().getAccountList().stream()
                .collect(Collectors.toMap(AccountInterface::getName, Function.identity()));
    }

    /**
     * Auto-add category if not present for the user
     */
    private void updateUserCategoryIfNeeded(AbstractTransaction tx) throws UserLoginException {
        String category = tx.getCategory();
        if (category != null && !category.isBlank() && !getCurrentUser().isCategoryAllowed(category)) {
            userService.addCategory(category);
            logger.info("[FileIOService] Added new category during import: " + category);
        }
    }
}
