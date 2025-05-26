package it.finance.sb.mapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;

import java.util.List;
import java.util.Map;

public record UserSnapshot(
        String name,
        int age,
        Gender gender,
        List<String> categories,
        List<AccountInterface> accounts,
        Map<TransactionType, List<AbstractTransaction>> transactions
) {

    @JsonCreator
    public UserSnapshot(
            @JsonProperty("name") String name,
            @JsonProperty("age") int age,
            @JsonProperty("gender") Gender gender,
            @JsonProperty("categories") List<String> categories,
            @JsonProperty("accounts") List<AccountInterface> accounts,
            @JsonProperty("transactions") Map<TransactionType, List<AbstractTransaction>> transactions
    ) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.categories = categories;
        this.accounts = accounts;
        this.transactions = transactions;
    }
}

