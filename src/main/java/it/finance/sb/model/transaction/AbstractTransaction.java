package it.finance.sb.model.transaction;


import it.finance.sb.model.composite.CompositeTransaction;

import java.util.Date;


public abstract class AbstractTransaction implements CompositeTransaction {
    protected final int transactionId;  // UNIQUE + final
    private static int idCounter = 0;
    protected double amount;
    protected String reason;
    protected Date date;

    protected TransactionType type;

    protected AbstractTransaction(double amount, String reason, Date date, TransactionType type) {
        this.amount = amount;
        this.reason = reason;
        this.date = date;
        this.type = type;
        this.transactionId = ++idCounter;
    }

    //TODO modify this method
    abstract void modifyTransaction();

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public int getTransactionId() {
        return transactionId;
    }
}
