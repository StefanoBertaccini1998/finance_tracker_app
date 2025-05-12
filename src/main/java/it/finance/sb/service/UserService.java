package it.finance.sb.service;

import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import it.finance.sb.utility.InputSanitizer;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UserService implements InterfaceService<User> {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public User create(String name, int age, Gender gender) {
        logger.info("[UserService] Created user '" + name + "' age=" + age + ", gender=" + gender);
        User user = new User(name, age, gender);
        InputSanitizer.validate(user);
        return user;
    }

    public User delete(User user) {
        logger.info("[UserService] Deleted user '" + user.getName() + "'");
        return user;
    }

    public User modify(User user, String newName, Integer newAge, Gender newGender) {
        if (newName != null && !newName.trim().isEmpty()) user.setName(newName);
        if (newAge != null && newAge > 0) user.setAge(newAge);
        if (newGender != null) user.setGender(newGender);
        logger.info("[UserService] Modified user info: name=" + user.getName() + ", age=" + user.getAge() + ", gender=" + user.getGender());
        InputSanitizer.validate(user);
        return user;
    }

    public void displayAllAccountBalances(User user) {
        logger.info("[UserService] Showing all balances for user '" + user.getName() + "'");
        Map<String, Double> balances = user.getAllAccountBalances();
        balances.forEach((name, balance) -> System.out.println("Account: " + name + " -> Balance: " + balance));
    }

    /**
     * @param user
     */
    public void displayAllTransactions(User user) {
        logger.info("[UserService] Showing all transactions for user '" + user.getName() + "'");
        Map<TransactionType, List<AbstractTransaction>> txMap = user.getAllTransactions();
        txMap.forEach((type, list) -> {
            System.out.println("\nTransaction Type: " + type.name());
            for (AbstractTransaction tx : list) {
                System.out.println("  ID: " + tx.getTransactionId() + " | Amount: " + tx.getAmount() + " | Reason: " + tx.getReason() + " | Date: " + tx.getDate());
            }
        });
    }

    public void addCategory(User user, String category) {
        if (category == null || category.isBlank()) {
            logger.warning("[UserService] Rejected blank category");
            return;
        }
        user.addCategory(category);
        logger.info("[UserService] Added category '" + category + "' for user " + user.getName());
    }

}
