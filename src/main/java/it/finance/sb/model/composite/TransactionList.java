package it.finance.sb.model.composite;

import it.finance.sb.model.iterator.ConcreteTransactionIterator;
import it.finance.sb.model.iterator.TransactionIterator;
import it.finance.sb.model.transaction.AbstractTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TransactionList implements CompositeTransaction{
    private List<CompositeTransaction> transactionList;

    @Override
    public void displayTransaction() {
        transactionList.forEach(CompositeTransaction::displayTransaction);
    }

    @Override
    public void getTotal() {

    }

    public TransactionList() {
        this.transactionList = new ArrayList<>();
    }

    public void addTransaction(AbstractTransaction transaction){
        transactionList.add(transaction);
    }

    public void addTransactions(List<AbstractTransaction> transactions){
        transactions.forEach(this::addTransaction);
    }

    public void remove(AbstractTransaction transaction){
        transactionList.remove(transaction);
    }

    //get Iterator for the Transaction list
    public TransactionIterator iterator() {
        return new ConcreteTransactionIterator(this.transactionList);
    }

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

}
