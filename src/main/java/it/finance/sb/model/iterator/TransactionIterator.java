package it.finance.sb.model.iterator;

import it.finance.sb.model.transaction.AbstractTransaction;

/**
 * Custom iterator for composite transaction traversal.
 */
public interface TransactionIterator {
    boolean hasNext();

    AbstractTransaction next();
    void remove();
}