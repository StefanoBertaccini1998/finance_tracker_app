package it.finance.sb.decorator;

import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;

public class LoggingAccountDecorator implements AccountInterface {
    private final AccountInterface wrapped;

    public LoggingAccountDecorator(AccountInterface wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void update(double amount) {
        System.out.println("[LOG] " + wrapped.getName() + " balance change: " + amount);
        wrapped.update(amount);
    }

    @Override
    public double getBalance() {
        return wrapped.getBalance();
    }

    @Override
    public int getAccountId() {
        return wrapped.getAccountId();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public AccounType getType() {
        return wrapped.getType();
    }

    @Override
    public void setType(AccounType type) {

    }

    @Override
    public void setDeposit(double deposit) {
        wrapped.setDeposit(deposit);
    }

    @Override
    public void setName(String name) {
        wrapped.setName(name);
    }
}
