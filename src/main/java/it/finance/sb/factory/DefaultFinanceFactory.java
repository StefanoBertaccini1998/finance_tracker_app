package it.finance.sb.factory;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.utility.InputSanitizer;

import java.util.Date;

public class DefaultFinanceFactory implements FinanceAbstractFactory {

    private final TransactionAbstractFactory transactionFactory;
    private final AccountFactory accountFactory;        // will convert to instance factory

    public DefaultFinanceFactory(TransactionAbstractFactory txFactory,
                                 AccountFactory accFactory) {
        this.transactionFactory = txFactory;
        this.accountFactory = accFactory;
    }

    @Override
    public AccountInterface createAccount(AccounType type, String name, double balance) throws DataValidationException {
        AccountInterface account = accountFactory.createAccount(type, name, balance);
        InputSanitizer.validate(account);
        return account;
    }

    @Override
    public AbstractTransaction createIncome(double amount, String category, String reason, Date date, AccountInterface to) throws DataValidationException, TransactionOperationException {
        AbstractTransaction income = transactionFactory.createIncome(amount, category, reason, date, to);
        InputSanitizer.validate(income);
        return income;
    }

    @Override
    public AbstractTransaction createExpense(double amount, String category, String reason, Date date, AccountInterface from) throws DataValidationException, TransactionOperationException {
        AbstractTransaction expense = transactionFactory.createExpense(amount, category, reason, date, from);
        InputSanitizer.validate(expense);
        return expense;

    }

    @Override
    public AbstractTransaction createMovement(double amount, String category, String reason, Date date, AccountInterface to, AccountInterface from) throws DataValidationException, TransactionOperationException {
        AbstractTransaction movement = transactionFactory.createMovement(amount, category, reason, date, to, from);
        InputSanitizer.validate(movement);
        return movement;
    }
}
