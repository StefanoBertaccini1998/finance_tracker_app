package it.finance.sb.service;

import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.account.AccounType;
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
        transactionService = new TransactionService(user);
        accountService = new AccountService(user,transactionService);

        AbstractAccount acc = accountService.create(AccounType.BANK, "Main", 1000);
        transactionService.create(TransactionType.INCOME, 200, "Bonus","Category", new Date(), acc, null);
    }

    @Test
    void testCreateUser() {
        assertEquals("Alice", user.getName());
        assertEquals(30, user.getAge());
        assertEquals(Gender.FEMALE, user.getGender());
    }

    @Test
    void testModifyUser() {
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
    void testDisplayAllTransactions() {
        Map<TransactionType, List<AbstractTransaction>> txMap = user.getAllTransactions();
        assertTrue(txMap.containsKey(TransactionType.INCOME));
        assertEquals(1, txMap.get(TransactionType.INCOME).size());
    }
}
