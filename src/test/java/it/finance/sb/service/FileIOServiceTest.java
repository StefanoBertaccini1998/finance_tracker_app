package it.finance.sb.service;

import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.account.BankAccount;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.IncomeTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileIOServiceTest {
    @Test
    void testImportAndExportTransactions() throws Exception {
        User user = new User("CSVUser", 28, Gender.OTHER);
        AbstractAccount acc = new BankAccount("ExportAcc", 1000);
        user.addAccount(acc);
        user.addCategory("Job");

        FileIOService fileService = new FileIOService(user);

        // Export dummy transaction
        AbstractTransaction tx = new IncomeTransaction(250,"Job", "TestSalary", new Date(), acc);
        user.addTransaction(tx);

        Path path = Path.of("test_io_transactions.csv");
        fileService.exportTransactions(path);

        // Reset user
        User importedUser = new User("ImportUser", 30, Gender.FEMALE);
        importedUser.addAccount(acc);

        FileIOService importer = new FileIOService(importedUser);
        importer.importTransactions(path);

        assertEquals(1, importedUser.getAllTransactions().get(TransactionType.INCOME).size());
        assertTrue(importedUser.isCategoryAllowed("Job"));
    }

}
