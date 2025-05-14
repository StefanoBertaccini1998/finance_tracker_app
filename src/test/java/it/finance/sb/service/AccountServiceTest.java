package it.finance.sb.service;

import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import org.junit.jupiter.api.*;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private AccountService accountService;
    private TransactionService transactionService;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("TestUser", 26, Gender.MALE);
        transactionService = new TransactionService();
        transactionService.setCurrentUser(user);
        accountService = new AccountService(transactionService);
        accountService.setCurrentUser(user);
    }

    @Test
    void testCreateAccount() throws Exception {
        AccountInterface account = accountService.create(AccounType.BANK, "Savings", 1000.0);
        assertNotNull(account);
        assertEquals("Savings", account.getName());
        assertEquals(1000.0, account.getBalance());
        assertTrue(user.getAccountList().contains(account));
    }

    @Test
    void testModifyAccount() throws Exception {
        AccountInterface account = accountService.create(AccounType.BANK, "Main", 500.0);
        AccountInterface modified = accountService.modify(account, null, "Main Updated", 750.0);

        assertEquals("Main Updated", modified.getName());
        assertEquals(750.0, modified.getBalance());
    }

    @Test
    void testDeleteAccountAlsoRemovesTransactions() throws Exception {
        // Create account and another to use in movement
        AccountInterface toDelete = accountService.create(AccounType.BANK, "Victim", 500.0);
        AccountInterface other = accountService.create(AccounType.BANK, "Other", 500.0);

        // Create a transaction that uses the account
        AbstractTransaction tx = transactionService.create(TransactionType.EXPENSE, 100, "TestExpense", "DeleteExpense", new Date(), null, toDelete);

        assertEquals(400, toDelete.getBalance());

        // Now delete the account
        AccountInterface deleted = accountService.delete(toDelete);

        assertEquals(toDelete, deleted);
        assertFalse(user.getAccountList().contains(toDelete));

        assertFalse(user.getTransactionLists().get(TransactionType.EXPENSE).iterator().hasNext());
    }
}
