package it.finance.sb.service;

import it.finance.sb.exception.FileIOException;
import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.io.CsvTransactionImporter;
import it.finance.sb.io.CsvWriter;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FileIOService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(FileIOService.class);

    private final TransactionService transactionService;

    public FileIOService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Imports a list of transactions from a CSV file and adds them to the user.
     */
    public void importTransactions(Path filePath, boolean autoCreateAccounts, boolean skipErrors) throws FileIOException, UserLoginException {
        requireLoggedInUser();
        try {
            // Map<AccountId, Account>
            Map<String, AccountInterface> accountMap = getCurrentUser().getAccountList().stream()
                    .collect(Collectors.toMap(AccountInterface::getName, Function.identity()));
            List<String> errors = new ArrayList<>();

            List<AbstractTransaction> transactions = CsvTransactionImporter.importTransactions(filePath, accountMap, autoCreateAccounts, skipErrors, errors);

            for (AbstractTransaction transaction : transactions) {

                // Add categories (if not present)
                if (transaction.getCategory() != null && !getCurrentUser().isCategoryAllowed(transaction.getCategory())) {
                    getCurrentUser().addCategory(transaction.getCategory());
                }
                // Add to user's transaction map
                getCurrentUser().addTransaction(transaction);
            }

            if (!errors.isEmpty()) {
                logger.warning("[FileIOService] Some lines failed: \n" + String.join("\n", errors));
            }

            logger.info("[FileIOService] Successfully imported " + transactions.size() + " transactions from CSV.");

        } catch (Exception e) {
            logger.warning("[FileIOService] Import failed: " + e.getMessage());
            throw new FileIOException("Failed to import transactions: " + e.getMessage(), e);
        }
    }

    /**
     * Exports a transaction list from the user to a CSV file.
     */
    public void exportTransactions(Path path) throws FileIOException, UserLoginException {
        requireLoggedInUser();
        try {
            CsvWriter<AbstractTransaction> writer = new CsvWriter<>();
            List<AbstractTransaction> allTxs = transactionService.getAllTransactionsFlattened();
            writer.writeToFile(allTxs, path);

            logger.info("[FileIOService] Exported " + allTxs.size() + " transactions to file: " + path);

        } catch (Exception e) {
            logger.severe("[FileIOService] Export failed: " + e.getMessage());
            throw new FileIOException("Failed to export transactions.", e);
        }
    }
}
