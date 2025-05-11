package it.finance.sb.factory;

import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.transaction.*;

import java.util.Date;

public class TransactionFactory {

    public static AbstractTransaction createTransaction(TransactionType type, double amount, String reason, Date date, AbstractAccount toAccount, AbstractAccount fromAccount) throws Exception {
        return switch (type) {
            case EXPENSE -> new ExpenseTransaction(amount, reason, date, fromAccount);
            case INCOME -> new IncomeTransaction(amount, reason, date, toAccount);
            case MOVEMENT -> new MovementTransaction(amount, reason, date, toAccount, fromAccount);
            case null -> throw new Exception("Non è stato passato nessun tipo alla transazione");
        };
    }
}
