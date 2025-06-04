package it.finance.sb.factory;

import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.Account;
import it.finance.sb.model.account.AccountInterface;

import java.util.logging.Logger;

/**
 * AccountFactory is responsible only for constructing account instances.
 * Validation must be performed by DefaultFinanceFactory or higher layers.
 */
public class AccountFactory {
    private static final Logger logger = LoggerFactory.getSafeLogger(AccountFactory.class);

    public AccountInterface createAccount(AccounType type, String name, double balance) {
        Account account = new Account(name, balance, type);
        logger.info(() -> "Creating account: " + name + " of type " + type);
        return account;
    }
}
