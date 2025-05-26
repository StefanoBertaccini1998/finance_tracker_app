package it.finance.sb.io;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.factory.AccountFactory;
import it.finance.sb.factory.DefaultFinanceFactory;
import it.finance.sb.factory.FinanceAbstractFactory;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CsvImporterTest  {
    CsvImporter csvTransactionImporter;
    @BeforeEach
    void setUp(){
        FinanceAbstractFactory factory = new DefaultFinanceFactory();
        csvTransactionImporter = new CsvImporter(factory);
    }

    @Test
    void testImport_validLines_shouldReturnTransactions() throws Exception {
        AccountInterface acc = AccountFactory.createAccount(AccounType.BANK, "Main", 1000);
        Map<String, AccountInterface> map = new HashMap<>();
        map.put("Main", acc);

        List<String> lines = List.of(
                "TransactionId,Type,Amount,From,To,Category,Reason,Date",
                "1,INCOME,1000,,Main,Salary,Job," + System.currentTimeMillis()
        );

        Path temp = Files.createTempFile("csv_test_", ".csv");
        Files.write(temp, lines);

        List<AbstractTransaction> result = csvTransactionImporter.importFrom(
                temp, map, false, false, new ArrayList<>()
        );

        assertEquals(1, result.size());
        assertEquals(TransactionType.INCOME, result.get(0).getType());
        Files.deleteIfExists(temp);
    }

    @Test
    void testImport_invalidAmount_shouldThrowOrSkip() throws Exception {
        AccountInterface acc = AccountFactory.createAccount(AccounType.BANK, "Main", 1000);
        Map<String, AccountInterface> map = new HashMap<>();
        map.put("Main", acc);

        List<String> lines = List.of(
                "TransactionId,Type,Amount,From,To,Category,Reason,Date",
                "1,INCOME,-999,,Main,Salary,Job," + System.currentTimeMillis()
        );

        Path temp = Files.createTempFile("csv_test_", ".csv");
        Files.write(temp, lines);

        assertThrows(DataValidationException.class, () -> csvTransactionImporter.importFrom(temp, map, false, false, new ArrayList<>()));

        Files.deleteIfExists(temp);
    }

    @Test
    void testImport_emptyFile_shouldThrow() throws Exception {
        Path emptyFile = Files.createTempFile("empty_test_", ".csv");

        Map<String, AccountInterface> map = new HashMap<>();
        List<String> errorLog = new ArrayList<>();

        assertThrows(IOException.class, () -> csvTransactionImporter.importFrom(emptyFile, map, false, false, errorLog));
        Files.deleteIfExists(emptyFile);
    }

    @Test
    void testImport_headerMismatch_shouldThrow() throws Exception {
        List<String> badHeaderLines = List.of(
                "BadHeader1,BadHeader2,BadHeader3",
                "1,INCOME,1000,,Main,Salary,Job," + System.currentTimeMillis()
        );

        Path file = Files.createTempFile("badheader_test_", ".csv");
        Files.write(file, badHeaderLines);

        Map<String, AccountInterface> map = new HashMap<>();
        List<String> errorLog = new ArrayList<>();

        assertThrows(IOException.class, () -> csvTransactionImporter.importFrom(file, map, false, false, errorLog));
        Files.deleteIfExists(file);
    }

    @Test
    void testImport_autocreateAccount_shouldSucceed() throws Exception {
        Map<String, AccountInterface> map = new HashMap<>(); // no Main account

        List<String> lines = List.of(
                "TransactionId,Type,Amount,From,To,Category,Reason,Date",
                "1,INCOME,1000,,Main,Salary,Job," + System.currentTimeMillis()
        );

        Path file = Files.createTempFile("autocreate_test_", ".csv");
        Files.write(file, lines);

        List<AbstractTransaction> result = csvTransactionImporter.importFrom(file, map, true, false, new ArrayList<>());

        assertEquals(1, result.size());
        assertTrue(map.containsKey("Main"));
        Files.deleteIfExists(file);
    }

}
