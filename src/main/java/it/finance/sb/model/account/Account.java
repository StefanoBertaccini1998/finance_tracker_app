package it.finance.sb.model.account;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import it.finance.sb.annotation.Sanitize;

import java.util.Objects;

@JsonTypeName("Account")
public class Account implements AccountInterface {
    private static int idCounter = 0;

    @JsonProperty
    private int accountId;

    @Sanitize(notBlank = true, maxLength = 30)
    private String name;

    @Sanitize(positiveNumber = true)
    private double balance;

    private AccounType type;

    @JsonCreator
    public Account(@JsonProperty("name") String name,
                   @JsonProperty("balance") double balance,
                   @JsonProperty("type") AccounType type) {
        this.accountId = ++idCounter;
        this.name = name;
        this.balance = balance;
        this.type = type;
    }

    public Account() {
        this.accountId = ++idCounter;
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
        double result = this.balance + amount;
        if (result < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance = result;
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