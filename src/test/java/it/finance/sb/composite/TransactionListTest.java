package it.finance.sb.composite;

import it.finance.sb.model.account.Account;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.transaction.ExpenseTransaction;
import it.finance.sb.model.transaction.IncomeTransaction;
import it.finance.sb.model.transaction.AbstractTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionList and ConcreteTransactionIterator.
 */
class TransactionListTest {
    private AbstractTransaction tx1;
    private AbstractTransaction tx2;
    private AbstractTransaction tx3;
    private TransactionList mainList;
    private Account account1;
    private Account account2;

    @BeforeEach
    void setup() {
        account1 = new Account("Main Account", 1000.0, AccounType.BANK);
        account2 = new Account("Second Account", 2000.0, AccounType.CASH);

        tx1 = new IncomeTransaction(100.0, "Salary", "August Salary", new Date(), account1);
        tx2 = new ExpenseTransaction(50.0, "Groceries", "Weekly shopping", new Date(), account2);
        tx3 = new ExpenseTransaction(30.0, "Transport", "Bus card", new Date(), account2);

        mainList = new TransactionList();
        mainList.addTransaction(tx1);
        mainList.addTransaction(tx2);
    }

    @Test
    void testGetTotal_shouldSumAllTransactions() {
        mainList.addTransaction(tx3);
        assertEquals(180.0, mainList.getTotal(), 0.01);
    }

    @Test
    void testFilterByCategory_shouldReturnCorrectTransaction() {
        List<AbstractTransaction> result = mainList.filterByCategory("groceries");
        assertEquals(1, result.size());
        assertEquals("Groceries", result.get(0).getCategory());
    }

    @Test
    void testFilterByMinAmount_shouldReturnOnlyLargerTransactions() {
        mainList.addTransaction(tx3);
        List<AbstractTransaction> result = mainList.filterByMinAmount(60.0);
        assertEquals(1, result.size());
        assertEquals(tx1, result.get(0));
    }

    @Test
    void testModifyTransactionById_shouldApplyChange() {
        boolean modified = mainList.modifyTransactionById(tx2.getTransactionId(), tx -> tx.setReason("Changed Reason"));
        assertTrue(modified);
        assertEquals("Changed Reason", tx2.getReason());
    }

    @Test
    void testIterator_shouldReturnAllTransactionsIncludingNested() {
        TransactionList nested = new TransactionList();
        nested.addTransaction(tx3);
        mainList.addTransaction(nested);

        List<AbstractTransaction> flattened = mainList.getFlattenedTransactions();
        assertEquals(3, flattened.size());
        assertTrue(flattened.contains(tx1));
        assertTrue(flattened.contains(tx2));
        assertTrue(flattened.contains(tx3));
    }
}
