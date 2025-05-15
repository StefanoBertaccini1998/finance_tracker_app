package it.finance.sb.service;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import it.finance.sb.utility.InputSanitizer;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The type User service.
 */
public class UserService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    /**
     * Create user.
     *
     * @param name   the name
     * @param age    the age
     * @param gender the gender
     * @return the user
     */
    public User create(String name, int age, Gender gender) throws DataValidationException {
        logger.info("[UserService] Created user '" + name + "' age=" + age + ", gender=" + gender);
        User user = new User(name, age, gender);
        InputSanitizer.validate(user);
        setCurrentUser(user);
        return user;
    }

    /**
     * Delete user.
     *
     * @param user the user
     * @return the user
     */
    public User delete(User user) {
        logger.info("[UserService] Deleted user '" + user.getName() + "'");
        return user;
    }

    /**
     * Modify user.
     *
     * @param user      the user
     * @param newName   the new name
     * @param newAge    the new age
     * @param newGender the new gender
     * @return the user
     */
    public User modify(User user, String newName, Integer newAge, Gender newGender) throws DataValidationException {
        if (newName != null && !newName.trim().isEmpty()) user.setName(newName);
        if (newAge != null && newAge > 0) user.setAge(newAge);
        if (newGender != null) user.setGender(newGender);
        logger.info("[UserService] Modified user info: name=" + user.getName() + ", age=" + user.getAge() + ", gender=" + user.getGender());
        InputSanitizer.validate(user);
        return user;
    }

    /**
     * Display all account balances without the account used for movement
     */
    public void displayAllAccount(AccountInterface accountInterfaceAvoid) {
        logger.info("[UserService] Showing all balances for user '" + getCurrentUser().getName() + "'");
        List<AccountInterface> accountList = getCurrentUser().getAccountList();
        for (int i = 0; i < accountList.size(); i++) {
            if (accountInterfaceAvoid == null || accountInterfaceAvoid != accountList.get(i)) {
                AccountInterface accountInterface = accountList.get(i);
                System.out.println(i + 1 + ") " + accountInterface);
            }
        }
    }

    /**
     * Display all account balances.
     */
    public void displayAllAccount() {
        displayAllAccount(null);
    }

    /**
     * Add category.
     *
     * @param user     the user
     * @param category the category
     */
    public void addCategory(User user, String category) {
        if (category == null || category.isBlank()) {
            logger.warning("[UserService] Rejected blank category");
            return;
        }
        user.addCategory(category);
        logger.info("[UserService] Added category '" + category + "' for user " + user.getName());
    }

    public List<AbstractTransaction> getTransactionsByCategory(String category) {
        return getCurrentUser().getAllTransactionsFlattened().stream()
                .filter(tx -> tx.getCategory().equalsIgnoreCase(category))
                .toList();
    }

}
