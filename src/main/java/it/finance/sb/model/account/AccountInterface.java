package it.finance.sb.model.account;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "objectType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Account.class, name = "Account")
})
public interface AccountInterface {
    String getName();
    double getBalance();
    int getAccountId();
    AccounType getType();
    void setType(AccounType type);
    void update(double amount);
    void setDeposit(double deposit);
    void setName(String name);
}
