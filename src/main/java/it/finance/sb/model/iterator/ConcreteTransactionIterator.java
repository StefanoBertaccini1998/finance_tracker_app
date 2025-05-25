package it.finance.sb.model.iterator;

import it.finance.sb.model.composite.CompositeTransaction;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.transaction.AbstractTransaction;

import java.util.*;

/**
 * Iterator over CompositeTransaction structure, supporting flattening.
 * Uses internal stack to support depth-first traversal.
 */
public class ConcreteTransactionIterator implements TransactionIterator {

    private final Deque<Iterator<CompositeTransaction>> stack = new ArrayDeque<>();
    private AbstractTransaction nextItem;
    private final Iterator<CompositeTransaction> currentIterator;

    public ConcreteTransactionIterator(List<CompositeTransaction> rootList) {
        this.currentIterator = rootList.iterator();
        this.stack.push(this.currentIterator);
        advance();
    }

    private void advance() {
        nextItem = null;
        while (!stack.isEmpty()) {
            Iterator<CompositeTransaction> it = stack.peek();
            while (true) {
                assert it != null;
                if (!it.hasNext()) break;
                CompositeTransaction ct = it.next();
                if (ct instanceof AbstractTransaction tx) {
                    nextItem = tx;
                    return;
                } else if (ct instanceof TransactionList nested) {
                    stack.push(nested.getInternalList().iterator());
                    it = stack.peek(); // continue with the newly added iterator
                }
            }
            stack.pop(); // current iterator is exhausted
        }
    }

    @Override
    public boolean hasNext() {
        return nextItem != null;
    }

    @Override
    public AbstractTransaction next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more transactions.");
        }
        AbstractTransaction current = nextItem;
        advance();
        return current;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported in flattened iterator.");
    }

    // Internal hook for recursion
    public Iterator<CompositeTransaction> getInternalIterator() {
        return this.currentIterator;
    }
}
