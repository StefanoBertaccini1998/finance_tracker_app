package it.finance.sb.factory;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.Account;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DefaultFinanceFactoryTest {

    private FinanceAbstractFactory factory;
    private AccountInterface to;
    private AccountInterface from;
    private final Date now = new Date();

    @BeforeEach
    void setup() {
        factory = new DefaultFinanceFactory();
        to = new Account("To", 1000.0, AccounType.BANK);
        from = new Account("From", 1000.0, AccounType.CASH);
    }

    // --- ACCOUNT ---

    @Test
    void testCreateValidAccount_shouldSucceed() throws Exception {
        AccountInterface acc = factory.createAccount(AccounType.BANK, "MyBank", 500.0);
        assertNotNull(acc);
        assertEquals("MyBank", acc.getName());
        assertEquals(500.0, acc.getBalance());
    }

    @Test
    void testCreateAccount_withNegativeBalance_shouldFail() {
        assertThrows(DataValidationException.class, () ->
                factory.createAccount(AccounType.CASH, "Neg", -100.0));
    }

    // --- TRANSACTIONS ---

    @Test
    void testCreateIncome_shouldSucceed() throws Exception {
        AbstractTransaction tx = factory.createIncome(100.0, "Salary", "Monthly", now, to);
        assertEquals(TransactionType.INCOME, tx.getType());
    }

    @Test
    void testCreateExpense_shouldFail_onNegativeAmount() {
        assertThrows(DataValidationException.class, () ->
                factory.createExpense(-20.0, "Food", "Bad", now, from));
    }

    @Test
    void testCreateMovement_shouldFail_ifMissingAccount() {
        assertThrows(DataValidationException.class, () ->
                factory.createMovement(100.0, "Move", "Error", now, null, from));
    }

    @Test
    void testCreateMovement_shouldSucceed() throws Exception {
        AbstractTransaction tx = factory.createMovement(200.0, "Transfer", "Saving", now, to, from);
        assertEquals(TransactionType.MOVEMENT, tx.getType());
    }
}
