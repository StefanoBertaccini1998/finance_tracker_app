package it.finance.sb.factory;

import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;

import java.util.Date;

/**
 * TransactionCreator is an interface that defines the contract for creating
 * transactions based on dynamic parameters. It is used by the factory to delegate
 * object instantiation to specialized classes, supporting the Strategy and Factory patterns.
 */
public interface TransactionCreator {
    /**
     * Creates a concrete instance of AbstractTransaction.
     *
     * @param amount   transaction amount
     * @param category transaction category
     * @param reason   description or note
     * @param date     transaction date
     * @param to       target account (nullable)
     * @param from     source account (nullable)
     * @return new transaction instance
     */
    AbstractTransaction create(double amount, String category, String reason, Date date, AccountInterface to, AccountInterface from);
}