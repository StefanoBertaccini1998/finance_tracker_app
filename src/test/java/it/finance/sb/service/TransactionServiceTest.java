package it.finance.sb.service;

import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService using mock UserService.
 */
class TransactionServiceTest {

    private TransactionService transactionService;
    private UserService userService;
    private User mockUser;
    private AccountInterface accFrom;
    private AccountInterface accTo;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        transactionService = new TransactionService(userService);
        mockUser = new User("MockUser", 30, Gender.FEMALE);
        transactionService.setCurrentUser(mockUser);

        accFrom = mock(AccountInterface.class);
        accTo = mock(AccountInterface.class);

        when(accFrom.getBalance()).thenReturn(1000.0);
        when(accFrom.getName()).thenReturn("Source");
        when(accTo.getName()).thenReturn("Target");
    }

    @Test
    void testCreateIncomeTransaction_shouldIncreaseToAccountBalance() throws Exception {
        AbstractTransaction tx = transactionService.create(
                TransactionType.INCOME, 100.0, "Salary", "Monthly Pay", new Date(), accTo, null
        );

        assertNotNull(tx);
        verify(accTo, times(1)).update(100.0);
        verify(userService, times(1)).addCategory("Salary");
    }

    @Test
    void testCreateExpenseTransaction_shouldDecreaseFromAccountBalance() throws Exception {
        AbstractTransaction tx = transactionService.create(
                TransactionType.EXPENSE, 200.0, "Food", "Groceries", new Date(), null, accFrom
        );

        assertNotNull(tx);
        verify(accFrom, times(1)).update(-200.0);
        verify(userService, times(1)).addCategory("Food");
    }

    @Test
    void testCreateTransaction_withInsufficientFunds_shouldThrow() {
        when(accFrom.getBalance()).thenReturn(50.0);

        assertThrows(TransactionOperationException.class, () -> {
            transactionService.create(TransactionType.EXPENSE, 100.0, "Food", "Dinner", new Date(), null, accFrom);
        });
    }

    @Test
    void testDeleteTransaction_shouldReverseAccountUpdate() throws Exception {
        AbstractTransaction tx = transactionService.create(
                TransactionType.INCOME, 150.0, "Refund", "Return", new Date(), accTo, null
        );

        transactionService.delete(tx);
        verify(accTo, times(1)).update(-150.0);
    }

    @Test
    void testModifyTransaction_shouldReplaceAndUpdateAccounts() throws Exception {
        AbstractTransaction original = transactionService.create(
                TransactionType.EXPENSE, 100.0, "Lunch", "Work meal", new Date(), null, accFrom
        );

        AbstractTransaction modified = transactionService.modify(
                original, 200.0, "Dinner", "Team meal", new Date(), null, accFrom
        );

        assertNotNull(modified);
        assertNotEquals(original.getTransactionId(), modified.getTransactionId());
    }
}
