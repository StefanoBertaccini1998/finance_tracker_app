package it.finance.sb.service;


import it.finance.sb.factory.TransactionFactory;
import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.transaction.*;
import it.finance.sb.model.user.User;

import java.util.Date;

public class TransactionService implements InterfaceService<AbstractTransaction> {
    //TODO The transaction service must expose all the method that can be done with transaction

    private User user;

    public TransactionService(User user) {
        this.user = user;
    }

    //TODO CREATE TRANSACTION
    //@Override
    public AbstractTransaction create(TransactionType type, double amount, String reason, Date date, AbstractAccount toAccount, AbstractAccount fromAccount) throws Exception {
        //Controllare che il valore sia positivo
        if (amount <= 0) {
            //throw new Exception
            System.out.println("Non si possono inserire transazioni con valore <=0");
        }
        if (fromAccount != null && fromAccount.getBalance() < amount) {
            // throw new Exception
            System.out.println("Non si possono inserire transazioni con un valore più alto del balance dell'account scelto");
        }
        //TODO ricordarsi di controllare la riflessione degli account
        //Modifica gli account in base al tipo della transazione
        switch (type) {
            case INCOME:
                toAccount.update(amount);
                break;
            case EXPENSE:
                //Controllare che il valore non sia più grande di from account
                fromAccount.update(-amount);
                break;
            case MOVEMENT:
                //Controllare che il valore non sia più grande di from account
                toAccount.update(amount);
                fromAccount.update(-amount);
                break;
        }

        // Crea transazione
        AbstractTransaction transaction = TransactionFactory.createTransaction(type, amount, reason, date, toAccount, fromAccount);
        //Aggiorna i dati dello user, Account e Transazione
        this.user.addTransaction(transaction);
        //Ritorna la transazione
        return transaction;
    }

    //@Override
    public AbstractTransaction delete(AbstractTransaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Cannot delete a null transaction");
        }

        double amount = transaction.getAmount();
        TransactionType type = transaction.getType();

        switch (type) {
            case INCOME -> {
                AbstractAccount to = ((IncomeTransaction) transaction).getToAccount();
                if (to != null) {
                    to.update(-amount);
                }
            }
            case EXPENSE -> {
                AbstractAccount from = ((ExpenseTransaction) transaction).getFromAccount();
                if (from != null) {
                    from.update(amount);
                }
            }
            case MOVEMENT -> {
                MovementTransaction move = (MovementTransaction) transaction;
                AbstractAccount from = move.getFromAccount();
                AbstractAccount to = move.getToAccount();
                if (from != null && to != null) {
                    from.update(amount);
                    to.update(-amount);
                }
            }
            default -> throw new IllegalArgumentException("Unsupported transaction type: " + type);
        }

        // Remove the transaction from user's transaction list
        user.getTransactionLists().get(type).remove(transaction);

        return transaction;
    }

    //TODO MODIFY TRANSACTION
    //@Override
    public AbstractTransaction modify(AbstractTransaction original,
                                      double newAmount,
                                      String newReason,
                                      Date newDate,
                                      AbstractAccount newTo,
                                      AbstractAccount newFrom) throws Exception {
        if (original == null || newAmount <= 0) {
            throw new IllegalArgumentException("Invalid transaction modification parameters.");
        }

        TransactionType type = original.getType();
        double oldAmount = original.getAmount();

        // Reverse original transaction
        switch (type) {
            case INCOME -> {
                AbstractAccount to = ((IncomeTransaction) original).getToAccount();
                if (to != null) to.update(-oldAmount);
            }
            case EXPENSE -> {
                AbstractAccount from = ((ExpenseTransaction) original).getFromAccount();
                if (from != null) from.update(oldAmount);
            }
            case MOVEMENT -> {
                MovementTransaction move = (MovementTransaction) original;
                move.getFromAccount().update(oldAmount);
                move.getToAccount().update(-oldAmount);
            }
        }

        // Apply new transaction effects
        switch (type) {
            case INCOME -> {
                if (newTo == null) throw new IllegalArgumentException("To Account is required for income");
                newTo.update(newAmount);
            }
            case EXPENSE -> {
                if (newFrom == null || newFrom.getBalance() < newAmount) {
                    throw new IllegalArgumentException("Invalid or insufficient funds in from account.");
                }
                newFrom.update(-newAmount);
            }
            case MOVEMENT -> {
                if (newFrom == null || newTo == null || newFrom.getBalance() < newAmount) {
                    throw new IllegalArgumentException("Invalid or insufficient funds for movement.");
                }
                newFrom.update(-newAmount);
                newTo.update(newAmount);
            }
        }

        // Create a new transaction object
        AbstractTransaction updatedTransaction = TransactionFactory.createTransaction(
                type, newAmount, newReason, newDate, newTo, newFrom);

        // Replace the original in the list
        TransactionList txList = user.getTransactionLists().get(type);
        txList.remove(original);
        txList.addTransaction(updatedTransaction);

        return updatedTransaction;
    }

//TODO SUGGESTION
}
