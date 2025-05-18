package it.finance.sb.service;

import it.finance.sb.exception.FileIOException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.io.CsvImporter;
import it.finance.sb.io.CsvWriter;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.IncomeTransaction;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FileIOServiceTest {

    private TransactionService transactionService;
    private CsvImporter<AbstractTransaction> mockImporter;
    private CsvWriter<AbstractTransaction> mockWriter;
    private FileIOService fileIOService;

    private User user;
    private AccountInterface account;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService();
        mockImporter = Mockito.mock(CsvImporter.class);
        mockWriter = Mockito.mock(CsvWriter.class);

        fileIOService = new FileIOService(transactionService, mockImporter, mockWriter);

        user = new User("TestUser", 30, Gender.OTHER);
        account = new it.finance.sb.model.account.Account("TestAccount", 1000, AccounType.BANK);
        user.addAccount(account);

        fileIOService.setCurrentUser(user);
        transactionService.setCurrentUser(user);
    }

    @Test
    void testExportTransactions_success() throws Exception {
        AbstractTransaction tx = new IncomeTransaction(100.0, "Salary", "Monthly", new Date(), account);
        user.addTransaction(tx);

        Path outputPath = Path.of("dummy_output.csv");

        doNothing().when(mockWriter).exportToFile(any(), eq(outputPath));

        assertDoesNotThrow(() -> fileIOService.exportTransactions(outputPath));

        verify(mockWriter, times(1)).exportToFile(anyList(), eq(outputPath));
    }

    @Test
    void testExportTransactions_fail() throws Exception {
        Path outputPath = Path.of("dummy_output.csv");

        doThrow(new RuntimeException("Write error")).when(mockWriter).exportToFile(any(), eq(outputPath));

        assertThrows(FileIOException.class, () -> fileIOService.exportTransactions(outputPath));
    }

    @Test
    void testImportTransactions_success() throws Exception {
        AbstractTransaction tx = new IncomeTransaction(100.0, "Bonus", "Performance", new Date(), account);

        when(mockImporter.importFrom(any(), any(), anyBoolean(), anyBoolean(), any()))
                .thenReturn(List.of(tx));

        Path inputPath = Path.of("dummy_input.csv");

        assertDoesNotThrow(() -> fileIOService.importTransactions(inputPath, false, false));

        assertEquals(1, user.getTransactionLists().values().stream()
                .mapToInt(l -> l.getFlattenedTransactions().size()).sum());

        assertTrue(user.isCategoryAllowed("Bonus"));
    }

    @Test
    void testImportTransactions_fail() throws Exception {
        Path inputPath = Path.of("bad_input.csv");

        when(mockImporter.importFrom(any(), any(), anyBoolean(), anyBoolean(), any()))
                .thenThrow(new RuntimeException("Import failed"));

        assertThrows(FileIOException.class, () -> fileIOService.importTransactions(inputPath, false, false));
    }

    @Test
    void testImportTransactions_skipBadTx() throws Exception {
        AbstractTransaction tx1 = new IncomeTransaction(100.0, "Salary", "Ok", new Date(), account);
        AbstractTransaction tx2 = mock(AbstractTransaction.class);
        when(tx2.getCategory()).thenReturn(null); // Simulate broken

        when(mockImporter.importFrom(any(), any(), anyBoolean(), anyBoolean(), any()))
                .thenReturn(List.of(tx1, tx2));

        // tx2 is invalid -> skipped
        assertDoesNotThrow(() -> fileIOService.importTransactions(Path.of("dummy.csv"), false, true));

        assertEquals(1, transactionService.getAllTransactionsFlattened().size());
    }
}
