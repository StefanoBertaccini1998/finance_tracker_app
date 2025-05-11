package it.finance.sb.model.iterator;

import it.finance.sb.model.transaction.AbstractTransaction;

public interface TransactionIterator {
    boolean hasNext();
    AbstractTransaction next();
}
