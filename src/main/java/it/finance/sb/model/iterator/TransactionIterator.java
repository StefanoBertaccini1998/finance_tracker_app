package it.finance.sb.model.iterator;

import it.finance.sb.model.transaction.AbstractTransaction;

/**
 * The interface Transaction iterator.
 */
public interface TransactionIterator {
    /**
     * Has next boolean.
     *
     * @return the boolean
     */
    boolean hasNext();

    /**
     * Next abstract transaction.
     *
     * @return the abstract transaction
     */
    AbstractTransaction next();

    /**
     * Remove.
     */
    void remove();
}
