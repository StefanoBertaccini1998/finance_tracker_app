package it.finance.sb.factory;

import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.Account;
import it.finance.sb.model.account.AccountInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for AccountFactory to validate account creation logic.
 */
class AccountFactoryTest {
    private AccountFactory factory;

    @BeforeEach
    void setup() {
        factory = new AccountFactory();
    }

    @Test
    void testCreateValidAccount_shouldSucceed() {
        AccountInterface account = factory.createAccount(AccounType.BANK, "Main Account", 1000.0);

        assertNotNull(account);
        assertEquals("Main Account", account.getName());
        assertEquals(1000.0, account.getBalance());
        assertEquals(AccounType.BANK, account.getType());
    }

    @Test
    void testCreateAccountWithZeroBalance_shouldSucceed() {
        AccountInterface account = factory.createAccount(AccounType.CASH, "Empty Account", 0.0);
        assertNotNull(account);
        assertEquals(0.0, account.getBalance());
    }

}
