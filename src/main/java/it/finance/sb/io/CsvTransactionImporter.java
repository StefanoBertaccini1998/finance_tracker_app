package it.finance.sb.io;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.account.BankAccount;
import it.finance.sb.model.transaction.*;
import it.finance.sb.utility.InputSanitizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CsvTransactionImporter {

    public static List<AbstractTransaction> importTransactions(
            Path inputFile,
            Map<String, AbstractAccount> accountMap,
            boolean autoCreateMissingAccounts,
            boolean skipBadLines,
            List<String> errorLog
    ) throws IOException {
        if (!Files.exists(inputFile) || !Files.isRegularFile(inputFile)) {
            throw new IOException("Input file not found or invalid.");
        }

        List<AbstractTransaction> transactions = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            String line;
            int lineNum = 0;

            String expectedHeader = "TransactionId,Type,Amount,From,To,Category,Reason,Date";

            //Validate Header
            String header = reader.readLine();
            lineNum++;

            if (header == null || !header.strip().equalsIgnoreCase(expectedHeader)) {
                throw new IOException("Invalid or missing CSV header at line 1. Expected: " + expectedHeader);
            }

            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    if (line.trim().isEmpty()) continue; // skip blank lines
                    String[] fields = line.split(",", -1); // preserve empty columns

                    if (fields.length < 8) throw new DataValidationException("Line " + lineNum + ": too few fields.");

                    TransactionType type = TransactionType.valueOf(fields[1].trim());
                    double amount = Double.parseDouble(fields[2].trim());
                    String fromName = fields[3].trim();
                    String toName = fields[4].trim();
                    String category = fields[5].trim().isEmpty() ? "Uncategorized" : fields[5].trim();
                    String reason = fields[6].trim();
                    Date date = new Date(Long.parseLong(fields[7].trim()));

                    AbstractAccount from = fromName.isEmpty() ? null : accountMap.get(fromName);
                    AbstractAccount to = toName.isEmpty() ? null : accountMap.get(toName);

                    // Auto-create if enabled
                    if (from == null && !fromName.isEmpty() && autoCreateMissingAccounts) {
                        from = new BankAccount(fromName, 0);
                        accountMap.put(fromName, from);
                    }
                    if (to == null && !toName.isEmpty() && autoCreateMissingAccounts) {
                        to = new BankAccount(toName, 0);
                        accountMap.put(toName, to);
                    }

                    if (amount <= 0) {
                        throw new DataValidationException("Transaction amount can't be NEGATIVE. Found: " + amount);
                    }

                    // Fail if still null when needed
                    if (type == TransactionType.INCOME && to == null)
                        throw new DataValidationException("Missing destination account for INCOME");
                    if (type == TransactionType.EXPENSE && from == null)
                        throw new DataValidationException("Missing source account for EXPENSE");
                    if (type == TransactionType.MOVEMENT && (from == null || to == null))
                        throw new DataValidationException("Missing accounts for MOVEMENT");

                    AbstractTransaction tx = switch (type) {
                        case INCOME -> new IncomeTransaction(amount, category, reason, date, to);
                        case EXPENSE -> new ExpenseTransaction(amount, category, reason, date, from);
                        case MOVEMENT -> new MovementTransaction(amount, category, reason, date, to, from);
                    };
                    InputSanitizer.validate(tx);
                    transactions.add(tx);

                } catch (Exception e) {
                    String error = "[Line " + lineNum + "] " + e.getMessage();
                    if (errorLog != null) errorLog.add(error);
                    if (!skipBadLines) {
                        throw new DataValidationException(error, e);
                    }
                }
            }
        }
        return transactions;
    }
}
