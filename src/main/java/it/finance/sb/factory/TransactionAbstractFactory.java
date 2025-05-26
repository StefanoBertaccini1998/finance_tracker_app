package it.finance.sb.factory;

import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;

import java.util.Date;

public interface TransactionAbstractFactory {
    AbstractTransaction createIncome(double amount, String category, String reason, Date date, AccountInterface to) throws TransactionOperationException;

    AbstractTransaction createExpense(double amount, String category, String reason, Date date, AccountInterface from) throws TransactionOperationException;

    AbstractTransaction createMovement(double amount, String category, String reason, Date date, AccountInterface to, AccountInterface from) throws TransactionOperationException;
}