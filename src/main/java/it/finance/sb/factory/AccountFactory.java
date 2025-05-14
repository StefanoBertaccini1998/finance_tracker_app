package it.finance.sb.factory;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.Account;
import it.finance.sb.model.account.AccountInterface;

public class AccountFactory {
    public static AccountInterface createAccount(AccounType type, String name, double balance) throws AccountOperationException {
        if (type == null) {
            throw new AccountOperationException("Account type cannot be null.");
        }
        return new Account(name, balance, type);
    }

}
