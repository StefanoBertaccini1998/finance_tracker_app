package it.finance.sb.io;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.transaction.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CsvTransactionImporter {

    public static List<AbstractTransaction> importTransactions(Path inputFile, Map<Integer, AbstractAccount> accountMap) throws Exception {
        if (!Files.exists(inputFile) || !Files.isRegularFile(inputFile)) {
            throw new IOException("Input file is invalid or not found.");
        }

        List<AbstractTransaction> transactions = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 5) throw new DataValidationException("Malformed CSV row: " + line);

                TransactionType type = TransactionType.valueOf(fields[4]);
                int accountId = Integer.parseInt(fields[5]);
                AbstractAccount account = accountMap.get(accountId);
                if (account == null) throw new DataValidationException("Account ID " + accountId + " not found.");

                switch (type) {
                    case INCOME -> transactions.add(IncomeTransaction.fromCsv(fields, account));
                    case EXPENSE -> transactions.add(ExpenseTransaction.fromCsv(fields, account));
                    case MOVEMENT -> {
                        int fromId = Integer.parseInt(fields[5]);
                        int toId = Integer.parseInt(fields[6]);
                        AbstractAccount from = accountMap.get(fromId);
                        AbstractAccount to = accountMap.get(toId);
                        if (from == null || to == null) throw new DataValidationException("Movement accounts not found.");
                        transactions.add(MovementTransaction.fromCsv(fields, from, to));
                    }
                    default -> throw new DataValidationException("Unsupported type: " + type);
                }
            }
        }

        return transactions;
    }
}
