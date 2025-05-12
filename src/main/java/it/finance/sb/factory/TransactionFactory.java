package it.finance.sb.factory;

import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.transaction.*;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

public class TransactionFactory {
    private static final Map<TransactionType, TransactionCreator> creators = new EnumMap<>(TransactionType.class);

    static {
        creators.put(TransactionType.INCOME, new IncomeTransactionCreator());
        creators.put(TransactionType.EXPENSE, new ExpenseTransactionCreator());
        creators.put(TransactionType.MOVEMENT, new MovementTransactionCreator());
    }

    public static AbstractTransaction createTransaction(TransactionType type, double amount, String category, String reason, Date date, AbstractAccount to, AbstractAccount from) {
        TransactionCreator creator = creators.get(type);
        if (creator == null) throw new TransactionOperationException("Unsupported transaction type: " + type);
        return creator.create(amount, category, reason, date, to, from);
    }
}