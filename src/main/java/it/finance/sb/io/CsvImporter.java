package it.finance.sb.io;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.factory.FinanceAbstractFactory;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The type Csv importer.
 */
public class CsvImporter implements ImporterI<AbstractTransaction> {

    private static final Logger logger = LoggerFactory.getSafeLogger(CsvImporter.class);
    private final List<AccountInterface> newlyCreatedAccounts = new ArrayList<>();
    private static final String EXPECTED_HEADER = "TransactionId,Type,Amount,From,To,Category,Reason,Date";

    private final FinanceAbstractFactory factory;

    public CsvImporter(FinanceAbstractFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<AbstractTransaction> importFrom(Path inputFile,
                                                Map<String, AccountInterface> accountMap,
                                                boolean autoCreateMissingAccounts,
                                                boolean skipBadLines,
                                                List<String> errorLog) throws IOException, DataValidationException {
        logger.info(() -> "Starting import from CSV: " + inputFile);
        if (!Files.exists(inputFile) || !Files.isRegularFile(inputFile)) {
            throw new IOException("Input file not found or invalid.");
        }

        newlyCreatedAccounts.clear();
        List<AbstractTransaction> transactions = new ArrayList<>();
        List<String> localErrors = new ArrayList<>();
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
                    transactions.add(transaction);
                    logger.fine(() -> "Parsed transaction: " + transaction);
                } catch (Exception e) {
                    String msg = "[Line " + lineNum + "] " + e.getMessage();
                    logger.warning("Skipped line " + lineNum + ": " + e.getMessage());
                    localErrors.add(msg);
                }
            }
        }

        // Aggiunta all'errorLog esterno (se fornito)
        if (errorLog != null) errorLog.addAll(localErrors);

        // Se skipBadLines == false e ci sono errori, fallisce
        if (!skipBadLines && !localErrors.isEmpty()) {
            String summary = "Import failed. Invalid lines:\n" + String.join("\n", localErrors);
            throw new DataValidationException(summary);
        }

        logger.info(() -> "Completed import. Total parsed: " + transactions.size());
        return transactions;
    }

    private AbstractTransaction parseLine(String line,
                                          int lineNum,
                                          Map<String, AccountInterface> accountMap,
                                          boolean autoCreate) throws DataValidationException, TransactionOperationException {

        String[] fields = line.split(",", -1);
        if (fields.length < 8) {
            throw new DataValidationException("Line " + lineNum + ": too few fields. Expected 8 fields.");
        }

        String typeStr = fields[1].trim();
        String amountStr = fields[2].trim();
        String fromName = fields[3].trim();
        String toName = fields[4].trim();
        String category = fields[5].trim().isEmpty() ? "Uncategorized" : fields[5].trim();
        String reason = fields[6].trim();
        String dateStr = fields[7].trim();

        // Mandatory field check
        if (typeStr.isEmpty()) {
            throw new DataValidationException("Line " + lineNum + ": missing transaction type.");
        }
        if (amountStr.isEmpty()) {
            throw new DataValidationException("Line " + lineNum + ": missing amount.");
        }
        if (dateStr.isEmpty()) {
            throw new DataValidationException("Line " + lineNum + ": missing date.");
        }

        TransactionType type;
        double amount;
        Date date;

        try {
            type = TransactionType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new DataValidationException("Line " + lineNum + ": invalid transaction type: '" + typeStr + "'");
        }

        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            throw new DataValidationException("Line " + lineNum + ": invalid amount: '" + amountStr + "'");
        }

        try {
            date = new Date(Long.parseLong(dateStr));
        } catch (NumberFormatException e) {
            throw new DataValidationException("Line " + lineNum + ": invalid date format: '" + dateStr + "'");
        }

        AccountInterface from = resolveAccount(accountMap, fromName, autoCreate);
        AccountInterface to = resolveAccount(accountMap, toName, autoCreate);

        validateRequiredAccounts(type, from, to, lineNum);

        return switch (type) {
            case INCOME -> factory.createIncome(amount, category, reason, date, to);
            case EXPENSE -> factory.createExpense(amount, category, reason, date, from);
            case MOVEMENT -> factory.createMovement(amount, category, reason, date, to, from);
        };
    }

    private void validateRequiredAccounts(TransactionType type,
                                          AccountInterface from,
                                          AccountInterface to,
                                          int lineNum) throws DataValidationException {
        switch (type) {
            case INCOME -> {
                if (to == null)
                    throw new DataValidationException("Line " + lineNum + ": missing destination account for INCOME");
            }
            case EXPENSE -> {
                if (from == null)
                    throw new DataValidationException("Line " + lineNum + ": missing source account for EXPENSE");
            }
            case MOVEMENT -> {
                if (from == null)
                    throw new DataValidationException("Line " + lineNum + ": missing source account for MOVEMENT");
                if (to == null)
                    throw new DataValidationException("Line " + lineNum + ": missing destination account for MOVEMENT");
                if (from.equals(to))
                    throw new DataValidationException("Line " + lineNum + ": source and destination accounts must be different");
            }
        }
    }

    private AccountInterface resolveAccount(Map<String, AccountInterface> accountMap,
                                            String name,
                                            boolean autoCreate) throws DataValidationException {
        if (name.isBlank()) return null;

        AccountInterface account = accountMap.get(name);
        if (account == null && autoCreate) {
            account = factory.createAccount(AccounType.BANK, name, 0.00);
            accountMap.put(name, account);
            newlyCreatedAccounts.add(account);
        }
        return account;
    }

    public List<AccountInterface> getNewlyCreatedAccounts() {
        return newlyCreatedAccounts;
    }
}
