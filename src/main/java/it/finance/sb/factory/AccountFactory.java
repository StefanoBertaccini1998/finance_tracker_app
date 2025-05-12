package it.finance.sb.factory;


import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.model.account.*;

public class AccountFactory {
    public static AbstractAccount createAccount(AccounType type, String name, double balance) throws Exception {
        return switch (type) {
            case BANK -> new BankAccount(name, balance);
            case CASH -> new CashAccount(name, balance);
            case INVESTMENTS -> new InvestementAccount(name, balance);
            case null -> throw new AccountOperationException("Non è stato passato nessun tipo di account");
        };
    }
}
