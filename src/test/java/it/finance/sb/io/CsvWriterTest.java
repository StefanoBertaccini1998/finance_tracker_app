package it.finance.sb.io;

import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.IncomeTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvWriterTest {

    private Path tempFile;
    private CsvWriter<AbstractTransaction> csvWriter;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("transaction_test", ".csv");
        csvWriter = new CsvWriter<>("TransactionId,Type,Amount,From,To,Category,Reason,Date");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempFile != null && Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
    }

    @Test
    void exportToFile_writesValidCsv() throws IOException {
        AccountInterface acc = new it.finance.sb.model.account.Account("Main", 1000.0, AccounType.BANK);
        AbstractTransaction tx = new IncomeTransaction(100.0, "Salary", "Monthly", new Date(0), acc);

        csvWriter.exportToFile(List.of(tx), tempFile);

        List<String> lines = Files.readAllLines(tempFile);

        assertEquals(2, lines.size()); // header + 1 line
        assertTrue(lines.get(0).contains("TransactionId"));
        assertTrue(lines.get(1).contains("Salary"));
        assertTrue(lines.get(1).contains("Monthly"));
    }

    @Test
    void exportToFile_emptyList_writesHeaderOnly() throws IOException {
        csvWriter.exportToFile(List.of(), tempFile);

        List<String> lines = Files.readAllLines(tempFile);
        assertEquals(1, lines.size());
        assertEquals("TransactionId,Type,Amount,From,To,Category,Reason,Date", lines.get(0));
    }

    @Test
    void exportToFile_nullPath_throwsException() {
        AccountInterface acc = new it.finance.sb.model.account.Account("Main", 1000.0, AccounType.BANK);
        AbstractTransaction tx = new IncomeTransaction(100.0, "Salary", "Monthly", new Date(), acc);

        Assertions.assertThrows(IllegalArgumentException.class, () -> csvWriter.exportToFile(List.of(tx), null));
    }

    @Test
    void exportToFile_nullList_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> csvWriter.exportToFile(null, tempFile));
    }

    @Test
    void exportToFile_multipleTransactions_shouldWriteAll() throws IOException {
        AccountInterface acc = new it.finance.sb.model.account.Account("Main", 1000.0, AccounType.BANK);
        AbstractTransaction tx1 = new IncomeTransaction(100.0, "Salary", "Jan", new Date(0), acc);
        AbstractTransaction tx2 = new IncomeTransaction(200.0, "Bonus", "Feb", new Date(0), acc);

        csvWriter.exportToFile(List.of(tx1, tx2), tempFile);

        List<String> lines = Files.readAllLines(tempFile);
        assertEquals(3, lines.size()); // header + 2 tx
        assertTrue(lines.get(2).contains("Bonus"));
    }
}
