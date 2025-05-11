package it.finance.sb.model.iterator;

import it.finance.sb.model.composite.CompositeTransaction;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.transaction.AbstractTransaction;

import java.util.List;

public class ConcreteTransactionIterator implements TransactionIterator {
    private final List<CompositeTransaction> transactions;
    private int position = 0;

    public ConcreteTransactionIterator(List<CompositeTransaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public boolean hasNext() {
        return position < transactions.size();
    }

    @Override
    public AbstractTransaction next() {
        CompositeTransaction compositeTransaction = transactions.get(position++);
        if (compositeTransaction instanceof AbstractTransaction transaction) {
            return transaction;
            //TODO check this branch of next if it ha sense, I would not accept inside of inside.
        } else if (compositeTransaction instanceof TransactionList transaction) {
            // Recursive iterator logic for nested composite
            return transaction.iterator().next();
        } else {
            throw new IllegalStateException("Invalid transaction type");
        }
    }
}
