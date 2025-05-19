package it.finance.sb.model.transaction;

import it.finance.sb.model.account.AccountInterface;

import java.util.Date;

public class TransactionBuilder {

    private TransactionType type;
    private double amount;
    private String category;
    private String reason;
    private Date date;
    private AccountInterface to;
    private AccountInterface from;

    public TransactionBuilder type(TransactionType type) {
        this.type = type;
        return this;
    }

    public TransactionBuilder amount(double amount) {
        this.amount = amount;
        return this;
    }

    public TransactionBuilder category(String category) {
        this.category = category;
        return this;
    }

    public TransactionBuilder reason(String reason) {
        this.reason = reason;
        return this;
    }

    public TransactionBuilder date(Date date) {
        this.date = date;
        return this;
    }

    public TransactionBuilder from(AccountInterface from) {
        this.from = from;
        return this;
    }

    public TransactionBuilder to(AccountInterface to) {
        this.to = to;
        return this;
    }

    public AbstractTransaction build() {
        return switch (type) {
            case INCOME -> new IncomeTransaction(amount, category, reason, date, to);
            case EXPENSE -> new ExpenseTransaction(amount, category, reason, date, from);
            case MOVEMENT -> new MovementTransaction(amount, category, reason, date, to, from);
        };
    }
}
