package it.finance.sb.service;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for AccountService using Mockito to mock TransactionService.
 */
class AccountServiceTest {

    private AccountService accountService;
    private TransactionService transactionService;
    private User user;

    @BeforeEach
    void setUp() {
        transactionService = mock(TransactionService.class);
        accountService = new AccountService(transactionService);
        user = new User("MockedUser", 25, Gender.MALE);
        accountService.setCurrentUser(user);
    }

    @Test
    void testCreateValidAccount_shouldSucceed() throws Exception {
        AccountInterface account = accountService.create(AccounType.BANK, "MockBank", 500.0);

        assertNotNull(account);
        assertEquals("MockBank", account.getName());
        assertEquals(500.0, account.getBalance());
        assertTrue(user.getAccountList().contains(account));
    }

    @Test
    void testCreateAccount_withInvalidBalance_shouldFail() {
        assertThrows(AccountOperationException.class, () ->
                accountService.create(AccounType.BANK, "Invalid", -100.0));
    }

    @Test
    void testModifyAccount_shouldApplyChanges() throws Exception {
        AccountInterface account = accountService.create(AccounType.BANK, "Start", 100.0);
        AccountInterface modified = accountService.modify(account, null, "Updated", 300.0);

        assertEquals("Updated", modified.getName());
        assertEquals(300.0, modified.getBalance());
    }

    @Test
    void testDeleteAccount_shouldRemoveAndCallTransactionCleanup() throws Exception {
        AccountInterface account = accountService.create(AccounType.BANK, "ToDelete", 100.0);
        AccountInterface deleted = accountService.delete(account);

        assertEquals(account, deleted);
        assertFalse(user.getAccountList().contains(account));
        verify(transactionService, times(1)).removeTransactionsForAccount(account);
    }
}
