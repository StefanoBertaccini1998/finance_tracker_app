package it.finance.sb.service;

import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private AccountService accountService;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("TestUser",26, Gender.MALE);
        accountService = new AccountService(user);
    }

    @Test
    void testCreateAccount() throws Exception {
        AbstractAccount account = accountService.create(AccounType.BANK, "Savings", 1000.0);
        assertNotNull(account);
        assertEquals("Savings", account.getName());
        assertEquals(1000.0, account.getBalance());
        assertTrue(user.getAccountList().contains(account));
    }

    @Test
    void testModifyAccount() throws Exception {
        AbstractAccount account = accountService.create(AccounType.BANK, "Main", 500);
        AbstractAccount modified = accountService.modify(account.getAccountId(), "Main Updated", 750.0);

        assertEquals("Main Updated", modified.getName());
        assertEquals(750.0, modified.getBalance());
    }

    @Test
    void testDeleteAccount() throws Exception {
        AbstractAccount account = accountService.create(AccounType.BANK, "ToDelete", 250);
        AbstractAccount deleted = accountService.delete(account);

        assertEquals(account, deleted);
        assertFalse(user.getAccountList().contains(account));
    }
}
