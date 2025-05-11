package it.finance.sb.service;

import it.finance.sb.factory.AccountFactory;
import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.user.User;

public class AccountService implements InterfaceService<AbstractAccount> {
    //TODO The ACCOUNT service must expose all the method that can be done with ACCOUNT
    private User user;

    public AccountService(User user) {
        this.user = user;
    }

    //TODO CREATE ACCOUNT
    public AbstractAccount create(AccounType type, String name, double balance) throws Exception {
        //TODO adding validation to the params
        AbstractAccount accountCreated = AccountFactory.createAccount(type, name, balance);
        user.addAccount(accountCreated);
        return accountCreated;
    }

    //TODO DELETE ACCOUNT
    public AbstractAccount delete(AbstractAccount account) {
        //TODO remove all transaction linked
        user.getAccountList().remove(account);
        return account;
    }

    //TODO MODIFY account its ok for now I likt it. We can further improve the exception handling with exception custom
    public AbstractAccount modify(int id, String newName, Double newBalance) {
        AbstractAccount account = user.getAccountList().stream()
                .filter(a -> a.getAccountId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account ID not found"));

        if (newName != null && !newName.trim().isEmpty()) {
            account.setName(newName);
        }
        if (newBalance != null && newBalance >= 0) {
            account.setDeposit(newBalance);
        }
        return account;
    }


    //TODO SUGGESTION
}
