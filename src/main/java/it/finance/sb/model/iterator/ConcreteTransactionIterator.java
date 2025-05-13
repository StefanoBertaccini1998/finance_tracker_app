package it.finance.sb.model.iterator;

import it.finance.sb.model.composite.CompositeTransaction;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.transaction.AbstractTransaction;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * The type Concrete transaction iterator.
 */
public class ConcreteTransactionIterator implements TransactionIterator {
    private final List<CompositeTransaction> transactions;
    private int currentIndex = 0;
    private int lastReturnedIndex = -1;

    /**
     * Instantiates a new Concrete transaction iterator.
     *
     * @param transactions the transactions
     */
    public ConcreteTransactionIterator(List<CompositeTransaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < transactions.size();
    }

    @Override
    public AbstractTransaction next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more transactions.");
        }

        CompositeTransaction ct = transactions.get(currentIndex);
        lastReturnedIndex = currentIndex++; // ✅ track the one returned
        if (ct instanceof AbstractTransaction transaction) {
            return transaction;
        } else if (ct instanceof TransactionList nested) {
            TransactionIterator nestedIt = nested.iterator();
            return nestedIt.hasNext() ? nestedIt.next() : null; // TODO ❌ currently unsafe
        } else {
            throw new IllegalStateException("Invalid transaction type at index " + lastReturnedIndex);
        }
    }

    @Override
    public void remove() {
        if (lastReturnedIndex < 0) {
            throw new IllegalStateException("Cannot remove before calling next()");
        }
        transactions.remove(lastReturnedIndex);
        if (lastReturnedIndex < currentIndex) {
            currentIndex--; // adjust if we removed something before where we’re going
        }
        lastReturnedIndex = -1; // reset so user can't call remove twice
    }


}
