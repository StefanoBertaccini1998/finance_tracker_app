package it.finance.sb.service;

import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.factory.FinanceAbstractFactory;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.ExpenseTransaction;
import it.finance.sb.model.transaction.IncomeTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import it.finance.sb.utility.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService using mock UserService and FinanceAbstractFactory.
 */
class TransactionServiceTest {

    private TransactionService transactionService;
    private UserService userService;
    private FinanceAbstractFactory factory;
    private User mockUser;
    private AccountInterface accFrom;
    private AccountInterface accTo;
    private AbstractTransaction mockTx;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        factory = mock(FinanceAbstractFactory.class);

        transactionService = new TransactionService(userService, factory);
        mockUser = new User("MockUser", 30, Gender.FEMALE, PasswordUtils.hash("Password"));
        transactionService.setCurrentUser(mockUser);

        accFrom = mock(AccountInterface.class);
        accTo = mock(AccountInterface.class);
        mockTx = mock(AbstractTransaction.class);

        when(accFrom.getBalance()).thenReturn(1000.0);
        when(accFrom.getName()).thenReturn("Source");
        when(accTo.getName()).thenReturn("Target");

        when(mockTx.getTransactionId()).thenReturn(1);
        when(mockTx.getType()).thenReturn(TransactionType.EXPENSE);
        when(mockTx.getAmount()).thenReturn(100.0);
        when(mockTx.getCategory()).thenReturn("Lunch");
        when(mockTx.getReason()).thenReturn("Work");
        when(mockTx.getDate()).thenReturn(new Date());
    }

    @Test
    void testCreateIncomeTransaction_shouldIncreaseToAccountBalance() throws Exception {
        when(factory.createIncome(eq(100.0), eq("Salary"), eq("Monthly Pay"), any(Date.class), eq(accTo)))
                .thenReturn(mockTx);

        AbstractTransaction tx = transactionService.create(
                TransactionType.INCOME, 100.0, "Salary", "Monthly Pay", new Date(), accTo, null
        );

        assertNotNull(tx);
        verify(accTo).update(100.0);
        verify(userService).addCategory("Salary");
        verify(factory).createIncome(anyDouble(), anyString(), anyString(), any(), any());
    }

    @Test
    void testCreateExpenseTransaction_shouldDecreaseFromAccountBalance() throws Exception {
        when(factory.createExpense(eq(200.0), eq("Food"), eq("Groceries"), any(Date.class), eq(accFrom)))
                .thenReturn(mockTx);

        AbstractTransaction tx = transactionService.create(
                TransactionType.EXPENSE, 200.0, "Food", "Groceries", new Date(), null, accFrom
        );

        assertNotNull(tx);
        verify(accFrom).update(-200.0);
        verify(userService).addCategory("Food");
        verify(factory).createExpense(anyDouble(), anyString(), anyString(), any(), any());
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
        AbstractTransaction tx = new IncomeTransaction(150.0, "Refund", "Return", new Date(), accTo);
        transactionService.getCurrentUser().addTransaction(tx); // necessario perché delete cerca nell'elenco utente

        transactionService.delete(tx);

        verify(accTo).update(-150.0);
    }

    @Test
    void testModifyTransaction_shouldReplaceAndUpdateAccounts() throws Exception {
        AbstractTransaction original = new ExpenseTransaction(
                100.0, "Lunch", "Work meal", new Date(), accFrom
        );
        transactionService.getCurrentUser().addTransaction(original); // necessario per delete

        AbstractTransaction modifiedTx = new ExpenseTransaction(
                200.0, "Dinner", "Team meal", new Date(), accFrom
        );

        when(factory.createExpense(eq(200.0), eq("Dinner"), eq("Team meal"), any(Date.class), eq(accFrom)))
                .thenReturn(modifiedTx);

        AbstractTransaction modified = transactionService.modify(
                original, 200.0, "Dinner", "Team meal", new Date(), null, accFrom
        );

        assertNotNull(modified);
        assertNotEquals(original.getTransactionId(), modified.getTransactionId());
    }
}
