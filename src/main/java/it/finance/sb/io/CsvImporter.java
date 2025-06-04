package it.finance.sb.io;

import it.finance.sb.exception.CsvParseException;
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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * CsvImporter reads a CSV file containing financial transactions and converts each line into
 * an AbstractTransaction object. It supports multi-threaded parsing for performance and
 * automatic account creation when needed.
 */
public class CsvImporter implements ImporterI<AbstractTransaction> {

    private static final Logger logger = LoggerFactory.getSafeLogger(CsvImporter.class);
    // List of accounts created during import, must be thread-safe
    private final List<AccountInterface> newlyCreatedAccounts = Collections.synchronizedList(new ArrayList<>());
    private static final String EXPECTED_HEADER = "TransactionId,Type,Amount,From,To,Category,Reason,Date";

    private final FinanceAbstractFactory factory;

    public CsvImporter(FinanceAbstractFactory factory) {
        this.factory = factory;
    }

    /**
     * Imports transactions from a CSV file. Each line is parsed in a separate thread.
     * Handles errors gracefully and optionally creates missing accounts.
     *
     * @param inputFile                 Path to the CSV file
     * @param accountMap                Map of existing accounts
     * @param autoCreateMissingAccounts Flag to create accounts if not found
     * @param skipBadLines              Flag to skip lines with parsing errors
     * @param errorLog                  Optional list to collect error messages
     * @return List of parsed transactions
     * @throws IOException             if file reading fails or parsing threads fail
     * @throws DataValidationException if errors are found and skipping is disabled
     */
    @Override
    public List<AbstractTransaction> importFrom(Path inputFile,
                                                Map<String, AccountInterface> accountMap,
                                                boolean autoCreateMissingAccounts,
                                                boolean skipBadLines,
                                                List<String> errorLog) throws IOException, DataValidationException, CsvParseException {
        logger.info(() -> "Starting import from CSV: " + inputFile);

        // Verify file existence and type
        if (!Files.exists(inputFile) || !Files.isRegularFile(inputFile)) {
            throw new IOException("Input file not found or invalid.");
        }

        newlyCreatedAccounts.clear();
        List<AbstractTransaction> transactions = Collections.synchronizedList(new ArrayList<>());
        List<String> localErrors = Collections.synchronizedList(new ArrayList<>());

        // Create thread pool with a size equal to number of CPU cores
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
                // Check CSV header
                String header = reader.readLine();
                if (header == null || !header.strip().equalsIgnoreCase(EXPECTED_HEADER)) {
                    throw new IOException("Invalid or missing CSV header. Expected: " + EXPECTED_HEADER);
                }

                List<Future<?>> futures = new ArrayList<>();
                AtomicInteger lineNum = new AtomicInteger(1);
                String line;

                // Read and dispatch each line to a thread
                while ((line = reader.readLine()) != null) {
                    final String currentLine = line;
                    final int currentLineNum = lineNum.incrementAndGet();
                    if (currentLine.trim().isEmpty()) continue;

                    // Each line is parsed in its own task
                    futures.add(executor.submit(() -> {
                        try {
                            AbstractTransaction tx = parseLine(currentLine, currentLineNum, accountMap, autoCreateMissingAccounts);
                            transactions.add(tx);
                            logger.fine(() -> "Parsed transaction: " + tx);
                        } catch (Exception e) {
                            String msg = "[Line " + currentLineNum + "] " + e.getMessage();
                            logger.warning("Skipped line " + currentLineNum + ": " + e.getMessage());
                            localErrors.add(msg);
                        }
                    }));
                }

                // Ensure all parsing tasks complete
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (Exception e) {
                        handleThreadException(e);
                    }
                }
            }
        }

        // Append local parsing errors to external log if provided
        if (errorLog != null) errorLog.addAll(localErrors);

        // Fail if not skipping bad lines and any errors were encountered
        if (!skipBadLines && !localErrors.isEmpty()) {
            throw new CsvParseException("Import failed. Invalid lines:\n" + String.join("\n", localErrors));
        }

        logger.info(() -> "Completed import. Total parsed: " + transactions.size());
        return transactions;
    }


    /**
     * Parses a single line of CSV into an AbstractTransaction.
     * Validates fields, resolves or creates accounts, and constructs the appropriate transaction type.
     *
     * @param line       the raw CSV line
     * @param lineNum    the line number (for error messages)
     * @param accountMap the existing account map to match source/destination
     * @param autoCreate flag to auto-create accounts if missing
     * @return the parsed AbstractTransaction
     * @throws DataValidationException       if any validation fails (type, amount, date, etc.)
     * @throws TransactionOperationException if creation logic fails
     */
    private AbstractTransaction parseLine(String line,
                                          int lineNum,
                                          Map<String, AccountInterface> accountMap,
                                          boolean autoCreate) throws DataValidationException, TransactionOperationException, CsvParseException {

        String[] fields = line.split(",", -1);
        if (fields.length < 8) {
            throw new CsvParseException("Line " + lineNum + ": too few fields. Expected 8 fields.");
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
            throw new CsvParseException("Line " + lineNum + ": missing transaction type.");
        }
        if (amountStr.isEmpty()) {
            throw new CsvParseException("Line " + lineNum + ": missing amount.");
        }
        if (dateStr.isEmpty()) {
            throw new CsvParseException("Line " + lineNum + ": missing date.");
        }

        TransactionType type;
        double amount;
        Date date;

        try {
            type = TransactionType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new CsvParseException("Line " + lineNum + ": invalid transaction type: '" + typeStr + "'");
        }

        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            throw new CsvParseException("Line " + lineNum + ": invalid amount: '" + amountStr + "'");
        }

        try {
            date = new Date(Long.parseLong(dateStr));
        } catch (NumberFormatException e) {
            throw new CsvParseException("Line " + lineNum + ": invalid date format: '" + dateStr + "'");
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
                                          int lineNum) throws CsvParseException {
        switch (type) {
            case INCOME -> {
                if (to == null)
                    throw new CsvParseException("Line " + lineNum + ": missing destination account for INCOME");
            }
            case EXPENSE -> {
                if (from == null)
                    throw new CsvParseException("Line " + lineNum + ": missing source account for EXPENSE");
            }
            case MOVEMENT -> {
                if (from == null)
                    throw new CsvParseException("Line " + lineNum + ": missing source account for MOVEMENT");
                if (to == null)
                    throw new CsvParseException("Line " + lineNum + ": missing destination account for MOVEMENT");
                if (from.equals(to))
                    throw new CsvParseException("Line " + lineNum + ": source and destination accounts must be different");
            }
        }
    }


    /**
     * Shared lock only inside this CsvImporter instance
     */
    private final Object accountLock = new Object();

    /**
     * Resolves an account by name. Creates a new account if not found and autoCreate is enabled.
     * Synchronized to prevent concurrent creation of the same account.
     */
    private AccountInterface resolveAccount(Map<String, AccountInterface> accounts,
                                            String name,
                                            boolean autoCreate) throws DataValidationException {
        if (name.isBlank()) return null;

        // Fast, un-synchronised read
        AccountInterface acc = accounts.get(name);
        if (acc != null || !autoCreate) return acc;

        /* ---------- critical section ---------- */
        synchronized (accountLock) {
            // Someone else might have created it while we were waiting
            acc = accounts.get(name);
            if (acc != null) return acc;

            // Safe to create now; factory may throw a checked exception
            acc = factory.createAccount(AccounType.BANK, name, 0.00);
            accounts.put(name, acc);
            newlyCreatedAccounts.add(acc);
            return acc;
        }
    }

    public List<AccountInterface> getNewlyCreatedAccounts() {
        return newlyCreatedAccounts;
    }


    /**
     * Handles exceptions thrown during multi-threaded parsing.
     * Restores interrupted state and wraps causes into IOException.
     */
    private void handleThreadException(Exception e) throws IOException {
        if (e instanceof InterruptedException ie) {
            logger.warning("CSV import thread interrupted: " + ie.getMessage());
            Thread.currentThread().interrupt();
            throw new IOException("CSV import was interrupted.", ie);

        } else if (e instanceof ExecutionException ee) {       // <-- change back
            Throwable cause = ee.getCause();
            if (cause instanceof CsvParseException cpe)
                throw new IOException("Validation error during CSV import: "
                        + cpe.getMessage(), cpe);
            else
                throw new IOException("Unexpected error during CSV import: "
                        + cause.getMessage(), cause);

        } else {
            throw new IOException("Unhandled error during thread execution: "
                    + e.getMessage(), e);
        }
    }
}
