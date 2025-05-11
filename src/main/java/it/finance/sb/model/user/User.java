package it.finance.sb.model.user;

import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String name;
    private int age;
    private Gender gender;
    private Map<TransactionType, TransactionList> transactionsMap;
    private List<AbstractAccount> accountList;

    public User(String name, int age, Gender gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        createTransactionList();
        accountList = new ArrayList<>();
    }

    private void createTransactionList(){
        Map<TransactionType, TransactionList> transactionMap = new HashMap<>();
        transactionMap.put(TransactionType.EXPENSE,new TransactionList());
        transactionMap.put(TransactionType.INCOME,new TransactionList());
        transactionMap.put(TransactionType.MOVEMENT,new TransactionList());
        this.transactionsMap = transactionMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public  Map<TransactionType, TransactionList> getTransactionLists() {
        return transactionsMap;
    }

    public List<AbstractAccount> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<AbstractAccount> accountList) {
        this.accountList = accountList;
    }

    public void addAccount(AbstractAccount account) {
        this.accountList.add(account);
    }

    public void addTransaction(AbstractTransaction transaction) {
        this.transactionsMap.get(transaction.getType()).addTransaction(transaction);
    }

    public void updateAccount(AbstractAccount account){

    }

    public void getFullBalance(){
        accountList.forEach(x-> System.out.println("Balance of "+x.getName()+": "+x.getBalance()));
    }

    public void getFullTransaction(){
        transactionsMap.forEach((x,y) -> {
            System.out.println("Transaction type:"+x.name());
            y.displayTransaction();
        });
    }
}
