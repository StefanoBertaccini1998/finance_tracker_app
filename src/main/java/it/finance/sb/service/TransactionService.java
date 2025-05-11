package it.finance.sb.service;

import it.finance.sb.exception.TransactionOperationException;
import it.finance.sb.factory.TransactionFactory;
import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.transaction.*;
import it.finance.sb.model.user.User;

import java.util.Date;

public class TransactionService implements InterfaceService<AbstractTransaction> {

    private final User user;

    public TransactionService(User user) {
        this.user = user;
    }

    /**
     * Creates a transaction and applies its financial effect to accounts.
     * Supports INCOME, EXPENSE, and MOVEMENT types.
     */
    public AbstractTransaction create(TransactionType type, double amount, String reason, Date date,
                                      AbstractAccount toAccount, AbstractAccount fromAccount) throws TransactionOperationException {
        try {
            if (amount <= 0) {
                throw new TransactionOperationException("Transaction amount must be greater than 0.");
            }

            if (fromAccount != null && fromAccount.getBalance() < amount) {
                throw new TransactionOperationException("Insufficient funds in source account.");
            }

            // Apply balance changes based on transaction type
            //TODO add excpetion? for null account?
            switch (type) {
                case INCOME -> {
                    if (toAccount != null) toAccount.update(amount);
                }
                case EXPENSE -> {
                    if (fromAccount != null) fromAccount.update(-amount);
                }
                case MOVEMENT -> {
                    if (toAccount != null && fromAccount != null) {
                        toAccount.update(amount);
                        fromAccount.update(-amount);
                    }
                }
                default -> throw new TransactionOperationException("Unsupported transaction type: " + type);
            }

            // Create transaction and persist
            AbstractTransaction transaction = TransactionFactory.createTransaction(type, amount, reason, date, toAccount, fromAccount);
            user.addTransaction(transaction);
            return transaction;

        } catch (Exception e) {
            throw new TransactionOperationException("Failed to create transaction: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a transaction and reverses its financial effects.
     */
    public AbstractTransaction delete(AbstractTransaction transaction) throws TransactionOperationException {
        if (transaction == null) {
            throw new TransactionOperationException("Cannot delete a null transaction.");
        }

        try {
            double amount = transaction.getAmount();
            TransactionType type = transaction.getType();

            // Reverse transaction impact
            switch (type) {
                case INCOME -> {
                    AbstractAccount to = ((IncomeTransaction) transaction).getToAccount();
                    if (to != null) to.update(-amount);
                }
                case EXPENSE -> {
                    AbstractAccount from = ((ExpenseTransaction) transaction).getFromAccount();
                    if (from != null) from.update(amount);
                }
                case MOVEMENT -> {
                    MovementTransaction move = (MovementTransaction) transaction;
                    move.getFromAccount().update(amount);
                    move.getToAccount().update(-amount);
                }
                default -> throw new TransactionOperationException("Unsupported transaction type: " + type);
            }

            // Remove from transaction list
            TransactionList list = user.getTransactionLists().get(type);
            if (list != null) {
                list.remove(transaction);
            }

            return transaction;

        } catch (Exception e) {
            throw new TransactionOperationException("Failed to delete transaction: " + e.getMessage(), e);
        }
    }

    /**
     * Modifies a transaction by reversing the original and applying a new one.
     */
    public AbstractTransaction modify(AbstractTransaction original,
                                      double newAmount,
                                      String newReason,
                                      Date newDate,
                                      AbstractAccount newTo,
                                      AbstractAccount newFrom) throws TransactionOperationException {
        if (original == null || newAmount <= 0) {
            throw new TransactionOperationException("Invalid input for modifying transaction.");
        }

        try {
            TransactionType type = original.getType();
            double oldAmount = original.getAmount();

            // Reverse original transaction
            switch (type) {
                case INCOME -> ((IncomeTransaction) original).getToAccount().update(-oldAmount);
                case EXPENSE -> ((ExpenseTransaction) original).getFromAccount().update(oldAmount);
                case MOVEMENT -> {
                    MovementTransaction move = (MovementTransaction) original;
                    move.getFromAccount().update(oldAmount);
                    move.getToAccount().update(-oldAmount);
                }
                default -> throw new TransactionOperationException("Unsupported transaction type: " + type);
            }

            // Apply new transaction effect
            switch (type) {
                case INCOME -> {
                    if (newTo == null) throw new TransactionOperationException("ToAccount is required for INCOME.");
                    newTo.update(newAmount);
                }
                case EXPENSE -> {
                    if (newFrom == null || newFrom.getBalance() < newAmount) {
                        throw new TransactionOperationException("Insufficient funds or missing FromAccount for EXPENSE.");
                    }
                    newFrom.update(-newAmount);
                }
                case MOVEMENT -> {
                    if (newFrom == null || newTo == null || newFrom.getBalance() < newAmount) {
                        throw new TransactionOperationException("Invalid accounts or insufficient funds for MOVEMENT.");
                    }
                    newFrom.update(-newAmount);
                    newTo.update(newAmount);
                }
            }

            // Create and replace transaction
            AbstractTransaction updatedTransaction = TransactionFactory.createTransaction(
                    type, newAmount, newReason, newDate, newTo, newFrom);

            TransactionList transactionList = user.getTransactionLists().get(type);
            if (transactionList != null) {
                transactionList.remove(original);
                transactionList.addTransaction(updatedTransaction);
            }

            return updatedTransaction;

        } catch (Exception e) {
            throw new TransactionOperationException("Failed to modify transaction: " + e.getMessage(), e);
        }
    }
}
