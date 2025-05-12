package it.finance.sb.model.user;

import it.finance.sb.annotation.Sanitize;
import it.finance.sb.model.account.AbstractAccount;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;

import java.util.*;

public class User {
    @Sanitize(notBlank = true, maxLength = 50)
    private String name;
    private int age;
    private Gender gender;
    private Map<TransactionType, TransactionList> transactionsMap;
    private List<AbstractAccount> accountList;
    private Set<String> categorySet = new HashSet<>(List.of("Food", "Utilities", "Transport"));

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

    public boolean isCategoryAllowed(String category) {
        return categorySet.contains(category);
    }

    public void addCategory(String category) {
        categorySet.add(category);
    }

    public void addTransaction(AbstractTransaction transaction) {
        this.transactionsMap.get(transaction.getType()).addTransaction(transaction);
    }

    public void updateAccount(AbstractAccount account){

    }
    public Map<String, Double> getAllAccountBalances() {
        Map<String, Double> result = new HashMap<>();
        for (AbstractAccount account : accountList) {
            result.put(account.getName(), account.getBalance());
        }
        return result;
    }

    public Map<TransactionType, List<AbstractTransaction>> getAllTransactions() {
        Map<TransactionType, List<AbstractTransaction>> result = new HashMap<>();
        for (var entry : transactionsMap.entrySet()) {
            List<AbstractTransaction> txs = new ArrayList<>();
            var iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                txs.add(iterator.next());
            }
            result.put(entry.getKey(), txs);
        }
        return result;
    }
}
