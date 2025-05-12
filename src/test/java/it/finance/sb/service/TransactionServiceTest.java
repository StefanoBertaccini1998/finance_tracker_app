package it.finance.sb.service;

import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import org.junit.jupiter.api.*;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {

    private TransactionService transactionService;
    private AccountService accountService;
    private User user;

    private AbstractAccount acc1;
    private AbstractAccount acc2;

    @BeforeEach
    void setUp() throws Exception {
        user = new User("TestUser",99, Gender.OTHER);

        transactionService = new TransactionService(user);
        accountService = new AccountService(user,transactionService);

        acc1 = accountService.create(AccounType.BANK, "Main", 1000.0);
        acc2 = accountService.create(AccounType.BANK, "Savings", 500.0);
    }

    @Test
    void testCreateIncomeTransaction() throws Exception {
        AbstractTransaction transaction = transactionService.create(TransactionType.INCOME, 200, "Salary","April work", new Date(), acc1, null);

        assertEquals(1200, acc1.getBalance());
        assertTrue(user.getTransactionLists().get(TransactionType.INCOME).iterator().hasNext());
    }

    @Test
    void testCreateExpenseTransaction() throws Exception {
        AbstractTransaction transaction = transactionService.create(TransactionType.EXPENSE, 150, "Groceries", "Apples and Banans", new Date(), null, acc1);

        assertEquals(850, acc1.getBalance());
        assertTrue(user.getTransactionLists().get(TransactionType.EXPENSE).iterator().hasNext());
    }

    @Test
    void testCreateMovementTransaction() throws Exception {
        AbstractTransaction transaction = transactionService.create(TransactionType.MOVEMENT, 100, "Transfer", "Send money to mum", new Date(), acc2, acc1);

        assertEquals(900, acc1.getBalance());
        assertEquals(600, acc2.getBalance());
    }

    @Test
    void testCreateInvalidAmount() {
        Exception exception = assertThrows(Exception.class, () -> {
            transactionService.create(TransactionType.INCOME, -100, "Invalid", "Invalid", new Date(), acc1, null);
        });
    }

    @Test
    void testCreateExpenseWithInsufficientFunds() {
        Exception exception = assertThrows(Exception.class, () -> {
            transactionService.create(TransactionType.EXPENSE, 9999, "Too much","Too much", new Date(), null, acc2);
        });
    }

    @Test
    void testDeleteIncomeTransaction() throws Exception {
        AbstractTransaction transaction = transactionService.create(TransactionType.INCOME, 100, "Refund","riento pezzo rotto", new Date(), acc1, null);
        assertEquals(1100, acc1.getBalance());

        transactionService.delete(transaction);
        assertEquals(1000, acc1.getBalance());
    }

    @Test
    void testDeleteExpenseTransaction() throws Exception {
        AbstractTransaction transaction = transactionService.create(TransactionType.EXPENSE, 200, "Utilities","Gamepad", new Date(), null, acc1);
        assertEquals(800, acc1.getBalance());

        transactionService.delete(transaction);
        assertEquals(1000, acc1.getBalance());
    }

    @Test
    void testModifyTransaction() throws Exception {
        AbstractTransaction original = transactionService.create(TransactionType.EXPENSE, 100, "Lunch","pranzo di pasqua", new Date(), null, acc1);
        assertEquals(900, acc1.getBalance());

        AbstractTransaction modified = transactionService.modify(
                original,
                200,
                "Dinner",
                "cena di lavoro",
                new Date(),
                null,
                acc1
        );

        assertEquals(800, acc1.getBalance());
        assertNotEquals(original.getTransactionId(), modified.getTransactionId());
    }

    @Test
    void testModifyWithInsufficientFunds() throws Exception {
        AbstractTransaction original = transactionService.create(TransactionType.EXPENSE, 100, "Lunch","Meeting lunch", new Date(), null, acc2);

        Exception exception = assertThrows(Exception.class, () -> {
            transactionService.modify(original, 9999, "Dinner","Meeting Dinner", new Date(), null, acc2);
        });
    }
}
