package it.finance.sb.factory;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;

import java.util.Date;

public interface FinanceAbstractFactory {
    AccountInterface createAccount(AccounType type, String name, double balance) throws DataValidationException;
    AbstractTransaction createIncome(double amount, String category, String reason, Date date, AccountInterface to) throws DataValidationException, TransactionOperationException;
    AbstractTransaction createExpense(double amount, String category, String reason, Date date, AccountInterface from) throws DataValidationException, TransactionOperationException;
    AbstractTransaction createMovement(double amount, String category, String reason, Date date, AccountInterface to, AccountInterface from) throws DataValidationException, TransactionOperationException;
}
