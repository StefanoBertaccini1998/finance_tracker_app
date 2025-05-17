package it.finance.sb.service;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.exception.FileIOException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.factory.AccountFactory;
import it.finance.sb.io.CsvTransactionImporter;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.IncomeTransaction;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import it.finance.sb.utility.InputSanitizer;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileIOServiceTest {

    @Test
    void testCsvParsingWithMixedValidLines_NoFile() throws Exception {
        User user = new User("NoFileUser", 30, Gender.OTHER);
        AccountInterface mainAcc = AccountFactory.createAccount(AccounType.BANK, "MainAcc", 1000);
        user.addAccount(mainAcc);

        // Account map used for resolving names
        Map<String, AccountInterface> accountMap = new HashMap<>();
        accountMap.put(mainAcc.getName(), mainAcc);

        List<String> csvLines = List.of(
                "TransactionId,Type,Amount,From,To,Category,Reason,Date",
                "1,INCOME,1000,,MainAcc,Salary,Monthly pay," + System.currentTimeMillis(),                  // ✅ OK
                "2,EXPENSE,200,MainAcc,,Food,Groceries," + System.currentTimeMillis(),                      // ✅ OK
                "3,MOVEMENT,300,MainAcc,Savings,Transfer,Move to savings," + System.currentTimeMillis(),    // ✅ Account Savings created
                "4,INCOME,500,UnknownAcc,,Gift,Gift income," + System.currentTimeMillis(),                  // ❌ Null to account for an Income
                "5,MOVEMENT,150,AutoAcc1,AutoAcc2,TopUp,Top up both," + System.currentTimeMillis(),         // ✅ Auto-create both
                "6,EXPENSE,120,,AutoAcc1,Bills,Electricity bill," + System.currentTimeMillis(),             // ❌ Null to account for an Expense
                "7,INCOME,999,,AutoAcc3,Bonus,Special bonus," + System.currentTimeMillis(),                 // ✅ Auto-create AutoAcc3
                "8,EXPENSE,-150,MainAcc,,Error,Negative amount," + System.currentTimeMillis(),              // ❌ Invalid value
                "9,INCOME,200,,MainAcc,,No category," + System.currentTimeMillis(),                         // ✅ Missing category added
                "10,MOVEMENT,50,MainAcc,MainAcc,Internal,Self move," + System.currentTimeMillis()           // ✅ Valid self move
        );

        List<String> errors = new ArrayList<>();

        List<AbstractTransaction> parsed = CsvTransactionImporter.importTransactions(
                inlineToTempCsv(csvLines), accountMap, true, true, errors
        );

        for (AccountInterface acc : accountMap.values()) {
            if (!user.getAccountList().contains(acc)) {
                user.addAccount(acc);
            }
        }

        for (AbstractTransaction tx : parsed) {
            InputSanitizer.validate(tx);
            user.addTransaction(tx);
            if (tx.getCategory() != null && !user.isCategoryAllowed(tx.getCategory())) {
                user.addCategory(tx.getCategory());
            }
        }

        assertEquals(7, parsed.size());
        assertTrue(user.getAccountList().stream().anyMatch(a -> a.getName().equals("AutoAcc1")));
        assertTrue(user.getAccountList().stream().anyMatch(a -> a.getName().equals("AutoAcc2")));
        assertTrue(user.getAccountList().stream().anyMatch(a -> a.getName().equals("AutoAcc3")));

        assertEquals(3, errors.size()); // 3 intentionally invalid lines
        assertTrue(errors.stream().anyMatch(e -> e.contains("INCOME")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("EXPENSE")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("NEGATIVE")));

        assertEquals(6, user.getAccountList().size());
    }

    @Test
    void testCsvParsingWithMixedValidLines_falseAccount() throws Exception {
        User user = new User("NoFileUser", 30, Gender.OTHER);
        AccountInterface mainAcc = AccountFactory.createAccount(AccounType.BANK, "MainAcc", 1000);
        user.addAccount(mainAcc);

        // Account map used for resolving names
        Map<String, AccountInterface> accountMap = new HashMap<>();
        accountMap.put(mainAcc.getName(), mainAcc);

        List<String> csvLines = List.of(
                "TransactionId,Type,Amount,From,To,Category,Reason,Date",                          // ✅ 1
                "1,INCOME,1000,,MainAcc,Salary,Monthly pay," + System.currentTimeMillis(),                  // ✅ OK
                "2,EXPENSE,200,MainAcc,,Food,Groceries," + System.currentTimeMillis(),                      // ✅ OK
                "3,MOVEMENT,300,MainAcc,Savings,Transfer,Move to savings," + System.currentTimeMillis(),    // ❌ Account Savings created
                "4,INCOME,500,UnknownAcc,,Gift,Gift income," + System.currentTimeMillis(),                  // ❌ Null to account for an Income
                "5,MOVEMENT,150,AutoAcc1,AutoAcc2,TopUp,Top up both," + System.currentTimeMillis(),         // ❌ Auto-create both
                "6,EXPENSE,120,,AutoAcc1,Bills,Electricity bill," + System.currentTimeMillis(),             // ❌ Null to account for an Expense
                "7,INCOME,999,,AutoAcc3,Bonus,Special bonus," + System.currentTimeMillis(),                 // ❌ Auto-create AutoAcc3
                "8,EXPENSE,-150,MainAcc,,Error,Negative amount," + System.currentTimeMillis(),              // ❌ Invalid value
                "9,INCOME,200,,MainAcc,,No category," + System.currentTimeMillis(),                         // ✅ Missing category added
                "10,MOVEMENT,50,MainAcc,MainAcc,Internal,Self move," + System.currentTimeMillis()           // ✅ Valid self move
        );

        List<String> errors = new ArrayList<>();

        List<AbstractTransaction> parsed = CsvTransactionImporter.importTransactions(
                inlineToTempCsv(csvLines), accountMap, false, true, errors
        );

        for (AccountInterface acc : accountMap.values()) {
            if (!user.getAccountList().contains(acc)) {
                user.addAccount(acc);
            }
        }

        for (AbstractTransaction tx : parsed) {
            InputSanitizer.validate(tx);
            user.addTransaction(tx);
            if (tx.getCategory() != null && !user.isCategoryAllowed(tx.getCategory())) {
                user.addCategory(tx.getCategory());
            }
        }

        assertEquals(4, parsed.size());

        assertEquals(6, errors.size()); // 6 intentionally invalid lines
        assertTrue(errors.stream().anyMatch(e -> e.contains("INCOME")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("EXPENSE")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("MOVEMENT")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("NEGATIVE")));

        assertEquals(1, user.getAccountList().size());
    }

    @Test
    void testExportAndReimportWithCsvFile() throws AccountOperationException, FileIOException, UserLoginException {
        User exportUser = new User("ExportUser", 25, Gender.MALE);
        AccountInterface acc = AccountFactory.createAccount(AccounType.BANK, "MainAcc", 1000);
        exportUser.addAccount(acc);

        for (int i = 0; i < 10; i++) {
            AbstractTransaction tx = new IncomeTransaction(
                    100 + i * 10,
                    "Salary",
                    "Job payment " + i,
                    new Date(),
                    acc
            );
            exportUser.addTransaction(tx);
            exportUser.addCategory(tx.getCategory());
        }
        TransactionService transactionService = new TransactionService();
        transactionService.setCurrentUser(exportUser);
        FileIOService exportService = new FileIOService(transactionService);
        exportService.setCurrentUser(exportUser);
        Path path = Path.of("transactions_export_test.csv");
        exportService.exportTransactions(path);

        // Re-import into new user
        User importUser = new User("ImportUser", 31, Gender.FEMALE);
        importUser.addAccount(acc); // required for resolution
        transactionService.setCurrentUser(importUser);
        FileIOService importService = new FileIOService(transactionService);
        importService.setCurrentUser(importUser);
        importService.importTransactions(path, false, false);

        assertEquals(10, transactionService.getAllTransactionsFlattened().size());
        assertTrue(importUser.isCategoryAllowed("Salary"));
        assertTrue(importUser.getAccountList().contains(acc));
    }

    /**
     * Utility to create a temp file from inline CSV lines (for no-file-based test)
     */
    private Path inlineToTempCsv(List<String> lines) throws Exception {
        Path path = Files.createTempFile("test_transactions", ".csv");
        Files.write(path, lines);
        return path;
    }
}
