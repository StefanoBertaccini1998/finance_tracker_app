package it.finance.sb.model.composite;

import it.finance.sb.model.iterator.ConcreteTransactionIterator;
import it.finance.sb.model.iterator.TransactionIterator;
import it.finance.sb.model.transaction.AbstractTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * TransactionList is a composite node that can contain individual transactions or nested TransactionLists.
 * Implements Composite pattern.
 */
public class TransactionList implements CompositeTransaction {
    private final List<CompositeTransaction> transactionList;

    public TransactionList() {
        this.transactionList = new ArrayList<>();
    }

    @Override
    public void displayTransaction() {
        transactionList.forEach(CompositeTransaction::displayTransaction);
    }

    @Override
    public double getTotal() {
        return transactionList.stream()
                .mapToDouble(CompositeTransaction::getTotal)
                .sum();
    }

    public void addTransaction(CompositeTransaction transaction) {
        transactionList.add(transaction);
    }

    public void addTransactions(List<? extends CompositeTransaction> transactions) {
        transactionList.addAll(transactions);
    }

    public void remove(CompositeTransaction transaction) {
        transactionList.remove(transaction);
    }

    public ConcreteTransactionIterator iterator() {
        return new ConcreteTransactionIterator(transactionList);
    }

    public List<CompositeTransaction> getInternalList() {
        return transactionList;
    }

    public List<AbstractTransaction> getFlattenedTransactions() {
        List<AbstractTransaction> result = new ArrayList<>();
        TransactionIterator iterator = this.iterator();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    public boolean modifyTransactionById(int id, Consumer<AbstractTransaction> modifier) {
        for (CompositeTransaction ct : transactionList) {
            if (ct instanceof AbstractTransaction tx && tx.getTransactionId() == id) {
                modifier.accept(tx);
                return true;
            } else if (ct instanceof TransactionList nested) {
                if (nested.modifyTransactionById(id, modifier)) return true;
            }
        }
        return false;
    }

    /**
     * Filters transactions by category.
     *
     * @param category the category name
     * @return list of matching transactions
     */
    public List<AbstractTransaction> filterByCategory(String category) {
        return getFlattenedTransactions().stream()
                .filter(tx -> tx.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    public List<AbstractTransaction> filterByMinAmount(double min) {
        return getFlattenedTransactions().stream()
                .filter(tx -> tx.getAmount() >= min)
                .toList();
    }
}
