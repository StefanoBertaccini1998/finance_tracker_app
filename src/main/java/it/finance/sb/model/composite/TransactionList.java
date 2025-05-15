package it.finance.sb.model.composite;

import it.finance.sb.model.iterator.ConcreteTransactionIterator;
import it.finance.sb.model.iterator.TransactionIterator;
import it.finance.sb.model.transaction.AbstractTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The type Transaction list.
 */
public class TransactionList implements CompositeTransaction {
    private List<CompositeTransaction> transactionList;

    @Override
    public void displayTransaction() {
        transactionList.forEach(CompositeTransaction::displayTransaction);
    }

    @Override
    public double getTotal() {
        return transactionList.stream()
                .filter(t -> t instanceof AbstractTransaction)
                .map(t -> (AbstractTransaction) t)
                .mapToDouble(AbstractTransaction::getAmount)
                .sum();
    }

    /**
     * Instantiates a new Transaction list.
     */
    public TransactionList() {
        this.transactionList = new ArrayList<>();
    }

    /**
     * Add transaction.
     *
     * @param transaction the transaction
     */
    public void addTransaction(AbstractTransaction transaction) {
        transactionList.add(transaction);
    }

    /**
     * Add transactions.
     *
     * @param transactions the transactions
     */
    public void addTransactions(List<AbstractTransaction> transactions) {
        transactions.forEach(this::addTransaction);
    }

    /**
     * Remove.
     *
     * @param transaction the transaction
     */
    public void remove(AbstractTransaction transaction) {
        transactionList.remove(transaction);
    }

    /**
     * Iterator transaction iterator.
     *
     * @return the transaction iterator
     */
    public TransactionIterator iterator() {
        return new ConcreteTransactionIterator(this.transactionList);
    }

    /**
     * Get all the transaction in the map.
     *
     * @return the transaction iterator
     */
    public List<AbstractTransaction> getFlattenedTransactions() {
        List<AbstractTransaction> result = new ArrayList<>();
        for (CompositeTransaction ct : transactionList) {
            if (ct instanceof AbstractTransaction tx) {
                result.add(tx);
            } else if (ct instanceof TransactionList nested) {
                result.addAll(nested.getFlattenedTransactions());
            }
        }
        return result;
    }

    /**
     * Modify transaction by id boolean.
     *
     * @param id       the id
     * @param modifier the modifier
     * @return the boolean
     */
    //TODO un po' complesso pure per le mie capacità
    //TODO Valutare la complessità della possibile istanza singola transaction come compoiste transaction e se deve essere aggiunto alla interface
    public boolean modifyTransactionById(int id, Consumer<AbstractTransaction> modifier) {
        for (CompositeTransaction ct : transactionList) {
            if (ct instanceof AbstractTransaction tx && tx.getTransactionId() == id) {
                modifier.accept(tx); // apply external mutation securely
                return true;
            } else if (ct instanceof TransactionList nested) {
                if (nested.modifyTransactionById(id, modifier)) return true;
            }
        }
        return false;
    }

    /**
     * Filter by reason list.
     *
     * @param reason the reason
     * @return the list
     */
    public List<AbstractTransaction> filterByReason(String reason) {
        return transactionList.stream()
                .filter(t -> t instanceof AbstractTransaction tx && tx.getReason().equalsIgnoreCase(reason))
                .map(t -> (AbstractTransaction) t)
                .toList();
    }

    /**
     * Filter by min amount list.
     *
     * @param min the min
     * @return the list
     */
    public List<AbstractTransaction> filterByMinAmount(double min) {
        return transactionList.stream()
                .filter(t -> t instanceof AbstractTransaction tx && tx.getAmount() >= min)
                .map(t -> (AbstractTransaction) t)
                .toList();
    }
}
