package it.finance.sb.service;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private AccountService accountService;
    private TransactionService transactionService;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        userService = new UserService();
        user = userService.create("Alice", 30, Gender.FEMALE);
        transactionService = new TransactionService(userService);
        transactionService.setCurrentUser(user);

        accountService = new AccountService(transactionService);
        accountService.setCurrentUser(user);
        AccountInterface acc = accountService.create(AccounType.BANK, "Main", 1000.0);
        transactionService.create(TransactionType.INCOME, 200, "Bonus","Category", new Date(), acc, null);
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
}
