package it.finance.sb.factory;

import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;

import java.util.Date;

public interface TransactionAbstractFactory {
    AbstractTransaction createIncome(double amount, String category, String reason, Date date, AccountInterface to);

    AbstractTransaction createExpense(double amount, String category, String reason, Date date, AccountInterface from);

    AbstractTransaction createMovement(double amount, String category, String reason, Date date, AccountInterface to, AccountInterface from);
}