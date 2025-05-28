package it.finance.sb.service;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.factory.FinanceAbstractFactory;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.Account;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.IncomeTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import it.finance.sb.utility.PasswordUtils;
import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserService userService;
    private AccountService accountService;
    private FinanceAbstractFactory factory;
    private TransactionService transactionService;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        userService = new UserService();
        user = userService.create("Alice", 30, Gender.FEMALE, PasswordUtils.hash("Password"));

        factory = mock(FinanceAbstractFactory.class);
        transactionService = new TransactionService(userService, factory);
        transactionService.setCurrentUser(user);

        accountService = new AccountService(transactionService, factory);
        accountService.setCurrentUser(user);

        AccountInterface acc = new Account("Main", 1000.0, AccounType.BANK);

        when(factory.createAccount(eq(AccounType.BANK), eq("Main"), eq(1000.0)))
                .thenReturn(acc);

        accountService.create(AccounType.BANK, "Main", 1000.0);

        when(factory.createIncome(anyDouble(), anyString(), anyString(), any(Date.class), eq(acc)))
                .thenReturn(new IncomeTransaction(200, "Category", "Bonus", new Date(), acc));

        transactionService.create(TransactionType.INCOME, 200, "Category", "Bonus", new Date(), acc, null);
    }

    @Test
    void testCreateUser() {
        assertEquals("Alice", user.getName());
        assertEquals(30, user.getAge());
        assertEquals(Gender.FEMALE, user.getGender());
    }

    @Test
    void testModifyUser() throws DataValidationException {
        userService.modify(user, "Alice Smith", 31, Gender.FEMALE);
        assertEquals("Alice Smith", user.getName());
        assertEquals(31, user.getAge());
    }

    @Test
    void testDisplayAllAccountBalances() {
        Map<String, Double> balances = user.getAllAccountBalances();
        assertTrue(balances.containsKey("Main"));
        assertEquals(1200.0, balances.get("Main")); // 1000 + 200 income
    }

    @Test
    void testDisplayAllTransactions_flattenedList() {
        List<AbstractTransaction> flatList = transactionService.getAllTransactionsFlattened();

        assertEquals(1, flatList.size());
        AbstractTransaction retrieved = flatList.get(0);
        assertEquals(200.0, retrieved.getAmount(), 0.001);
        assertEquals("Category", retrieved.getCategory());
        assertEquals("Bonus", retrieved.getReason());
        assertEquals(TransactionType.INCOME, retrieved.getType());
    }

    @Test
    void testAddCategoryShouldIgnoreDuplicates() throws Exception {
        userService.addCategory("Custom");
        userService.addCategory("custom");
        assertEquals(1, user.getCategorySet().stream().filter(c -> c.equalsIgnoreCase("Custom")).count());
    }
}
