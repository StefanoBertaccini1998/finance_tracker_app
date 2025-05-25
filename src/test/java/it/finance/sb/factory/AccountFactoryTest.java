package it.finance.sb.factory;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.exception.DataValidationException;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.account.AccounType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountFactory to validate account creation logic.
 */
public class AccountFactoryTest {

    @Test
    void testCreateValidAccount_shouldSucceed() throws Exception {
        AccountInterface account = AccountFactory.createAccount(AccounType.BANK, "Main Account", 1000.0);

        assertNotNull(account);
        assertEquals("Main Account", account.getName());
        assertEquals(1000.0, account.getBalance());
        assertEquals(AccounType.BANK, account.getType());
    }

    @Test
    void testCreateAccountWithNullType_shouldThrow() {
        assertThrows(AccountOperationException.class, () ->
                AccountFactory.createAccount(null, "Error Account", 100.0)
        );
    }

    @Test
    void testCreateAccountWithZeroBalance_shouldSucceed() throws Exception {
        AccountInterface account = AccountFactory.createAccount(AccounType.CASH, "Empty Account", 0.0);
        assertNotNull(account);
        assertEquals(0.0, account.getBalance());
    }

    @Test
    void testCreateAccountWithNegativeBalance_shouldThrow() {
        assertThrows(DataValidationException.class, () ->
                AccountFactory.createAccount(AccounType.CASH, "Invalid Account", -50.0)
        );
    }
}
