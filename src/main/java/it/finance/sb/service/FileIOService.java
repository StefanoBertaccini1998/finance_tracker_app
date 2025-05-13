package it.finance.sb.service;

import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.io.CsvTransactionImporter;
import it.finance.sb.io.CsvWriter;
import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.user.User;
import it.finance.sb.utility.InputSanitizer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FileIOService {

    private final User user;
    private static final Logger logger = Logger.getLogger(FileIOService.class.getName());

    public FileIOService(User user) {
        this.user = user;
    }

    /**
     * Imports a list of transactions from a CSV file and adds them to the user.
     */
    public void importTransactions(Path filePath, boolean autoCreateAccounts, boolean skipErrors) throws TransactionOperationException {
        try {
            // Map<AccountId, Account>
            Map<String, AbstractAccount> accountMap = user.getAccountList().stream()
                    .collect(Collectors.toMap(AbstractAccount::getName, Function.identity()));
            List<String> errors = new ArrayList<>();

            List<AbstractTransaction> transactions = CsvTransactionImporter.importTransactions(filePath, accountMap, autoCreateAccounts, skipErrors, errors);

            for (AbstractTransaction transaction : transactions) {

                // Add categories (if not present)
                if (transaction.getCategory() != null && !user.isCategoryAllowed(transaction.getCategory())) {
                    user.addCategory(transaction.getCategory());
                }
                // Add to user's transaction map
                user.addTransaction(transaction);
            }

            if (!errors.isEmpty()) {
                logger.warning("[FileIOService] Some lines failed: \n" + String.join("\n", errors));
            }

            logger.info("[FileIOService] Successfully imported " + transactions.size() + " transactions from CSV.");

        } catch (Exception e) {
            logger.warning("[FileIOService] Import failed: " + e.getMessage());
            throw new TransactionOperationException("Failed to import transactions: " + e.getMessage(), e);
        }
    }

    /**
     * Exports a transaction list from the user to a CSV file.
     */
    public void exportTransactions(Path path) throws TransactionOperationException {
        try {
            CsvWriter<AbstractTransaction> writer = new CsvWriter<>();
            List<AbstractTransaction> allTxs = user.getAllTransactions()
                    .values()
                    .stream()
                    .flatMap(List::stream)
                    .toList();

            writer.writeToFile(allTxs, path);

            logger.info("[FileIOService] Exported " + allTxs.size() + " transactions to file: " + path);

        } catch (Exception e) {
            logger.severe("[FileIOService] Export failed: " + e.getMessage());
            throw new TransactionOperationException("Failed to export transactions.", e);
        }
    }
}
