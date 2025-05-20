package it.finance.sb.io;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.factory.AccountFactory;
import it.finance.sb.factory.TransactionFactory;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.utility.InputSanitizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The type Csv importer.
 */
public class CsvImporter implements CsvImporterI<AbstractTransaction> {

    private static final Logger logger = LoggerFactory.getInstance().getLogger(CsvImporter.class);
    private final List<AccountInterface> newlyCreatedAccounts = new ArrayList<>();
    private static final String EXPECTED_HEADER = "TransactionId,Type,Amount,From,To,Category,Reason,Date";

    @Override
    public List<AbstractTransaction> importFrom(Path inputFile,
                                                Map<String, ?> referenceMap,
                                                boolean autoCreateMissingAccounts,
                                                boolean skipBadLines,
                                                List<String> errorLog) throws IOException, DataValidationException {
        logger.info("Starting import from CSV: " + inputFile);
        if (!Files.exists(inputFile) || !Files.isRegularFile(inputFile)) {
            throw new IOException("Input file not found or invalid.");
        }

        Map<String, AccountInterface> accountMap = castAccountMap(referenceMap);
        newlyCreatedAccounts.clear();
        List<AbstractTransaction> transactions = new ArrayList<>();
        int lineNum = 0;

        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            // Validate header
            String header = reader.readLine();
            lineNum++;
            if (header == null || !header.strip().equalsIgnoreCase(EXPECTED_HEADER)) {
                throw new IOException("Invalid or missing CSV header. Expected: " + EXPECTED_HEADER);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                try {
                    AbstractTransaction transaction = parseLine(line, lineNum, accountMap, autoCreateMissingAccounts);
                    InputSanitizer.validate(transaction);
                    transactions.add(transaction);
                    logger.fine("Parsed transaction: " + transaction);
                } catch (Exception e) {
                    String msg = "[Line " + lineNum + "] " + e.getMessage();
                    if (errorLog != null) errorLog.add(msg);
                    logger.warning("Skipped line " + lineNum + ": " + e.getMessage());
                    if (!skipBadLines) {
                        throw new DataValidationException(msg, e);
                    }
                }
            }
        }
        logger.info("Completed import. Total parsed: " + transactions.size());
        return transactions;
    }

    private AbstractTransaction parseLine(String line,
                                          int lineNum,
                                          Map<String, AccountInterface> accountMap,
                                          boolean autoCreate) throws DataValidationException, AccountOperationException, TransactionOperationException {

        String[] fields = line.split(",", -1);
        if (fields.length < 8) throw new DataValidationException("Line " + lineNum + ": too few fields.");

        TransactionType type = TransactionType.valueOf(fields[1].trim());
        double amount = Double.parseDouble(fields[2].trim());
        String fromName = fields[3].trim();
        String toName = fields[4].trim();
        String category = fields[5].trim().isEmpty() ? "Uncategorized" : fields[5].trim();
        String reason = fields[6].trim();
        Date date = new Date(Long.parseLong(fields[7].trim()));

        AccountInterface from = resolveAccount(accountMap, fromName, autoCreate);
        AccountInterface to = resolveAccount(accountMap, toName, autoCreate);

        validateRequiredAccounts(type, from, to);

        return TransactionFactory.createTransaction(type, amount, category, reason, date, to, from);
    }

    private void validateRequiredAccounts(TransactionType type,
                                          AccountInterface from,
                                          AccountInterface to) throws DataValidationException {
        switch (type) {
            case INCOME -> {
                if (to == null) throw new DataValidationException("Missing destination account for INCOME");
            }
            case EXPENSE -> {
                if (from == null) throw new DataValidationException("Missing source account for EXPENSE");
            }
            case MOVEMENT -> {
                if (from == null || to == null)
                    throw new DataValidationException("Missing accounts for MOVEMENT");
            }
        }
    }

    private AccountInterface resolveAccount(Map<String, AccountInterface> accountMap,
                                            String name,
                                            boolean autoCreate) throws AccountOperationException {
        if (name.isBlank()) return null;

        AccountInterface account = accountMap.get(name);
        if (account == null && autoCreate) {
            account = AccountFactory.createAccount(AccounType.BANK, name, 0.01);
            accountMap.put(name, account);
            newlyCreatedAccounts.add(account);
        }
        return account;
    }

    private Map<String, AccountInterface> castAccountMap(Map<String, ?> rawMap) {
        return rawMap.entrySet().stream()
                .filter(e -> e.getValue() instanceof AccountInterface)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (AccountInterface) e.getValue()));
    }

    public List<AccountInterface> getNewlyCreatedAccounts() {
        return newlyCreatedAccounts;
    }
}
