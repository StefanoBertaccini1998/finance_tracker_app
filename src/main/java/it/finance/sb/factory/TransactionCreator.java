package it.finance.sb.factory;

import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.transaction.AbstractTransaction;

import java.util.Date;

public interface TransactionCreator {
    AbstractTransaction create(double amount,String category, String reason, Date date, AbstractAccount to, AbstractAccount from);
}