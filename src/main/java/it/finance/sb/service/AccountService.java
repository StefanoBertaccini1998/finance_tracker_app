package it.finance.sb.service;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.factory.AccountFactory;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.user.User;
import it.finance.sb.utility.InputSanitizer;

import java.util.logging.Logger;

/**
 * The type Account service.
 */
public class AccountService {
    private User user;
    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    /**
     * Instantiates a new Account service.
     *
     * @param user               the user
     * @param transactionService the transaction service
     */
    public AccountService(User user, TransactionService transactionService) {
        this.user = user;
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
//TODO CREATE ACCOUNT
    public AbstractAccount create(AccounType type, String name, double balance) throws Exception {
        AbstractAccount accountCreated = AccountFactory.createAccount(type, name, balance);
        InputSanitizer.validate(accountCreated);
        user.addAccount(accountCreated);
        logger.info("[AccountService] Created account '" + name + "' with balance " + balance);
        return accountCreated;
    }

    /**
     * Delete abstract account.
     *
     * @param account the account
     * @return the abstract account
     */
//TODO DELETE ACCOUNT
    public AbstractAccount delete(AbstractAccount account) {
        if (account == null) {
            throw new AccountOperationException("Cannot delete a null account.");
        }

        // Remove all transactions linked to this account
        transactionService.removeTransactionsForAccount(account);

        // Remove account from user
        user.getAccountList().remove(account);
        logger.info("[AccountService] Deleted account ID=" + account.getAccountId());
        return account;
    }

    /**
     * Modify abstract account.
     *
     * @param id         the id
     * @param newName    the new name
     * @param newBalance the new balance
     * @return the abstract account
     */
//TODO MODIFY account its ok for now I likt it. We can further improve the exception handling with exception custom
    public AbstractAccount modify(int id, String newName, Double newBalance) {
        AbstractAccount account = user.getAccountList().stream()
                .filter(a -> a.getAccountId() == id)
                .findFirst()
                .orElseThrow(() -> new AccountOperationException("Account ID not found"));

        if (newName != null && !newName.trim().isEmpty()) {
            account.setName(newName);
        }
        if (newBalance != null && newBalance >= 0) {
            account.setDeposit(newBalance);
        }
        InputSanitizer.validate(account);
        logger.info("[AccountService] Modified account ID=" + id + " -> name: " + newName + ", balance: " + newBalance);
        return account;
    }


    //TODO SUGGESTION
}
