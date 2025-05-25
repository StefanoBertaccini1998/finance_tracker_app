package it.finance.sb.factory;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.model.account.Account;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionFactory to verify correct creation,
 * validation, and error handling of transaction instances.
 */
public class TransactionFactoryTest {

    private AccountInterface mockToAccount;
    private AccountInterface mockFromAccount;
    private final Date now = new Date();

    @BeforeEach
    void setup() {
        mockToAccount = new Account("ToAccount", 1000.0, null);
        mockFromAccount = new Account("FromAccount", 1000.0, null);
    }

    @Test
    void testCreateIncomeTransaction_shouldSucceed() throws Exception {
        AbstractTransaction tx = TransactionFactory.createTransaction(
                TransactionType.INCOME, 500.0, "Salary", "Monthly salary", now,
                mockToAccount, null);

        assertNotNull(tx);
        assertEquals(TransactionType.INCOME, tx.getType());
        assertEquals(500.0, tx.getAmount());
    }

    @Test
    void testCreateExpenseTransaction_withNegativeAmount_shouldThrow() {
        assertThrows(DataValidationException.class, () ->
                TransactionFactory.createTransaction(
                        TransactionType.EXPENSE, -100.0, "Food", "Groceries", now,
                        null, mockFromAccount)
        );
    }

    @Test
    void testCreateMovementTransaction_withMissingAccount_shouldThrow() {
        assertThrows(DataValidationException.class, () ->
                TransactionFactory.createTransaction(
                        TransactionType.MOVEMENT, 200.0, "Transfer", "Bank transfer", now,
                        null, null)
        );
    }

    @Test
    void testCreateWithUnsupportedType_shouldThrow() {
        assertThrows(TransactionOperationException.class, () ->
                TransactionFactory.createTransaction(null, 100.0, "Misc", "None", now,
                        mockToAccount, mockFromAccount)
        );
    }

    @Test
    void testRegisterCustomCreator_shouldOverrideDefault() throws Exception {
        TransactionFactory.registerCreator(TransactionType.INCOME, (amount, category, reason, date, to, from) ->
                new AbstractTransaction(amount, category, reason, date, TransactionType.INCOME) {
                    @Override public void displayTransaction() {}
                    @Override public double getTotal() { return amount; }
                    @Override public String toCsv() { return "mock-csv"; }
                });

        AbstractTransaction tx = TransactionFactory.createTransaction(
                TransactionType.INCOME, 100.0, "Mock", "Custom", now, mockToAccount, null);

        assertNotNull(tx);
        assertEquals("mock-csv", tx.toCsv());
    }
}
