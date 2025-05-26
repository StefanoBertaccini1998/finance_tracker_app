package it.finance.sb.factory;

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
 * Unit tests for TransactionFactory using its Abstract Factory interface.
 */
class TransactionFactoryTest {

    private AccountInterface toAccount;
    private AccountInterface fromAccount;
    private TransactionAbstractFactory factory;
    private final Date now = new Date();

    @BeforeEach
    void setup() {
        toAccount = new Account("To", 1000.0, null);
        fromAccount = new Account("From", 1000.0, null);
        factory = new TransactionFactory();
    }

    @Test
    void testCreateIncome_shouldSucceed() throws TransactionOperationException {
        AbstractTransaction tx = factory.createIncome(500.0, "Salary", "Monthly pay", now, toAccount);
        assertNotNull(tx);
        assertEquals(TransactionType.INCOME, tx.getType());
        assertEquals(500.0, tx.getAmount());
    }

    @Test
    void testCreateExpense_shouldSucceed() throws TransactionOperationException {
        AbstractTransaction tx = factory.createExpense(200.0, "Food", "Dinner", now, fromAccount);
        assertNotNull(tx);
        assertEquals(TransactionType.EXPENSE, tx.getType());
        assertEquals(200.0, tx.getAmount());
    }

    @Test
    void testCreateMovement_shouldSucceed() throws TransactionOperationException {
        AbstractTransaction tx = factory.createMovement(300.0, "Transfer", "Savings", now, toAccount, fromAccount);
        assertNotNull(tx);
        assertEquals(TransactionType.MOVEMENT, tx.getType());
        assertEquals(300.0, tx.getAmount());
    }


    @Test
    void testRegisterCustomCreator_shouldOverrideIncome() throws TransactionOperationException {
        TransactionFactory realFactory = new TransactionFactory();

        TransactionFactory.registerCreator(TransactionType.INCOME, (amount, category, reason, date, to, from) ->
                new AbstractTransaction(amount, category, reason, date, TransactionType.INCOME) {
                    @Override public void displayTransaction() {/*Test method*/}
                    @Override public double getTotal() { return amount; }
                    @Override public String toCsv() { return "MOCKED"; }
                });

        AbstractTransaction tx = realFactory.createIncome(100.0, "Mocked", "Custom", now, toAccount);
        assertEquals("MOCKED", tx.toCsv());
    }
}
