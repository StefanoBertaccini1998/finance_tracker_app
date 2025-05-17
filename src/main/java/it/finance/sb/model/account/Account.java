package it.finance.sb.model.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.finance.sb.annotation.Sanitize;

import java.util.Objects;

public class Account implements AccountInterface {
    private static int idCounter = 0;

    @JsonProperty
    private final int accountId;

    @Sanitize(notBlank = true, maxLength = 30)
    private String name;

    @Sanitize(notBlank = true)
    private double balance;

    private AccounType type;

    public Account(String name, double deposit, AccounType type) {
        this.accountId = ++idCounter;
        this.name = name;
        this.balance = deposit;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getBalance() {
        return balance;
    }

    @Override
    public int getAccountId() {
        return accountId;
    }

    @Override
    public AccounType getType() {
        return type;
    }

    @Override
    public void setType(AccounType type) {
        this.type = type;
    }

    @Override
    public void update(double amount) {
        this.balance += amount;
    }

    @Override
    public void setDeposit(double deposit) {
        this.balance = deposit;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Account: name='%s'; balance=%.2f; type=%s", name, balance, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account other)) return false;
        return accountId == other.accountId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }
}