package it.finance.sb.service;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.exception.DataValidationException;
import it.finance.sb.factory.FinanceAbstractFactory;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.Account;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for AccountService using Mockito to mock TransactionService and FinanceAbstractFactory.
 */
class AccountServiceTest {

    private AccountService accountService;
    private TransactionService transactionService;
    private FinanceAbstractFactory factory;
    private User user;
    private AccountInterface mockAccount;

    @BeforeEach
    void setUp() {
        transactionService = mock(TransactionService.class);
        factory = mock(FinanceAbstractFactory.class);
        accountService = new AccountService(transactionService, factory);
        user = new User("MockedUser", 25, Gender.MALE, "Password");
        accountService.setCurrentUser(user);

        // crea un account reale da usare come ritorno della factory
        mockAccount = new Account("MockBank", 500.0, AccounType.BANK);
    }

    @Test
    void testCreateValidAccount_shouldSucceed() throws Exception {
        when(factory.createAccount(AccounType.BANK, "MockBank", 500.0)).thenReturn(mockAccount);

        AccountInterface account = accountService.create(AccounType.BANK, "MockBank", 500.0);

        assertNotNull(account);
        assertEquals("MockBank", account.getName());
        assertEquals(500.0, account.getBalance());
        assertTrue(user.getAccountList().contains(account));

        verify(factory).createAccount(AccounType.BANK, "MockBank", 500.0);
    }

    @Test
    void testCreateAccount_withInvalidBalance_shouldFail() throws DataValidationException {
        assertThrows(AccountOperationException.class, () ->
                accountService.create(AccounType.BANK, "Invalid", -100.0));

        verify(factory, never()).createAccount(any(), any(), anyDouble());
    }

    @Test
    void testModifyAccount_shouldApplyChanges() throws Exception {
        when(factory.createAccount(any(), any(), anyDouble())).thenReturn(mockAccount);

        AccountInterface account = accountService.create(AccounType.BANK, "Start", 100.0);
        AccountInterface modified = accountService.modify(account, null, "Updated", 300.0);

        assertEquals("Updated", modified.getName());
        assertEquals(300.0, modified.getBalance());
    }

    @Test
    void testDeleteAccount_shouldRemoveAndCallTransactionCleanup() throws Exception {
        when(factory.createAccount(any(), any(), anyDouble())).thenReturn(mockAccount);

        AccountInterface account = accountService.create(AccounType.BANK, "ToDelete", 100.0);
        AccountInterface deleted = accountService.delete(account);

        assertEquals(account, deleted);
        assertFalse(user.getAccountList().contains(account));
        verify(transactionService).removeTransactionsForAccount(account);
    }
}
