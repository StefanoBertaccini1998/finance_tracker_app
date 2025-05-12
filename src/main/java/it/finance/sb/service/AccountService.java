package it.finance.sb.service;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.factory.AccountFactory;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.user.User;
import it.finance.sb.utility.InputSanitizer;

import java.util.logging.Logger;

public class AccountService implements InterfaceService<AbstractAccount> {
    //TODO The ACCOUNT service must expose all the method that can be done with ACCOUNT
    private User user;
    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public AccountService(User user, TransactionService transactionService) {
        this.user = user;
        this.transactionService = transactionService;
    }

    //TODO CREATE ACCOUNT
    public AbstractAccount create(AccounType type, String name, double balance) throws Exception {
        AbstractAccount accountCreated = AccountFactory.createAccount(type, name, balance);
        InputSanitizer.validate(accountCreated);
        user.addAccount(accountCreated);
        logger.info("[AccountService] Created account '" + name + "' with balance " + balance);
        return accountCreated;
    }

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
