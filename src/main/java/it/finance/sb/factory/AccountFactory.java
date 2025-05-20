package it.finance.sb.factory;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.Account;
import it.finance.sb.model.account.AccountInterface;

import java.util.logging.Logger;

public class AccountFactory {
    private static final Logger logger = LoggerFactory.getInstance().getLogger(AccountFactory.class);

    public static AccountInterface createAccount(AccounType type, String name, double balance) throws AccountOperationException {
        if (type == null) {
            throw new AccountOperationException("Account type cannot be null.");
        }
        Account account = new Account(name, balance, type);
        logger.info("Creating account: " + name + " of type " + type);
        return account;
    }

}
