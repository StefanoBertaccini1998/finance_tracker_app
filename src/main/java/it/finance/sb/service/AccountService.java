package it.finance.sb.service;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.factory.AccountFactory;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.utility.InputSanitizer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The type Account service.
 */
public class AccountService extends BaseService {

    private final Map<Integer, AccountInterface> accounts = new HashMap<>();
    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    /**
     * Instantiates a new Account service.
     *
     * @param transactionService the transaction service
     */
    public AccountService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Create abstract account.
     *
     * @param type    the type
     * @param name    the name
     * @param balance the balance
     * @return the abstract account
     * @throws Exception the exception
     */
    public AccountInterface create(AccounType type, String name, Double balance) throws Exception {
        requireLoggedInUser();

        //Create account using factory
        AccountInterface accountCreated = AccountFactory.createAccount(type, name, balance);
        //Sanitize the object
        InputSanitizer.validate(accountCreated);
        //Add to the current user context the account created
        currentUser.getAccountList().add(accountCreated);
        logger.info("[AccountService] Created account '" + name + "' with balance " + balance);
        return accountCreated;
    }

    /**
     * Delete abstract account.
     *
     * @param account the account
     * @return the abstract account
     */
    public AccountInterface delete(AccountInterface account) throws AccountOperationException {
        requireLoggedInUser();
        if (account == null) {
            throw new AccountOperationException("Cannot delete a null account.");
        }

        // Remove all transactions linked to this account
        transactionService.removeTransactionsForAccount(account);

        // Remove account from user
        currentUser.getAccountList().remove(account);
        logger.info("[AccountService] Deleted account ID=" + account.getAccountId());
        return account;
    }

    /**
     * Modify abstract account.
     *
     * @param account    the account
     * @param type       the account type
     * @param newName    the new name
     * @param newBalance the new balance
     * @return the abstract account
     */
    public AccountInterface modify(AccountInterface account, AccounType type, String newName, Double newBalance) throws Exception {
        requireLoggedInUser();

        if (newName != null && !newName.trim().isEmpty()) {
            account.setName(newName);
        }
        if (type != null) {
            account.setType(type);
        }
        if (newBalance != null && newBalance >= 0) {
            account.setDeposit(newBalance);
        }
        InputSanitizer.validate(account);
        logger.info("[AccountService] Modified account " + account + " = -> name: " + newName + ", balance: " + newBalance);
        return account;
    }

    //TODO SUGGESTION
}
