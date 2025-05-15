package it.finance.sb.model.user;

import it.finance.sb.annotation.Sanitize;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.composite.CompositeTransaction;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * The type User.
 */
public class User {
    @Sanitize(notBlank = true, maxLength = 50)
    private String name;
    private int age;
    private Gender gender;
    private Map<TransactionType, TransactionList> transactionsMap;
    private List<AccountInterface> accountList;
    private Set<String> categorySet;

    /**
     * Instantiates a new User.
     *
     * @param name   the name
     * @param age    the age
     * @param gender the gender
     */
    public User(String name, int age, Gender gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        createTransactionList();
        this.categorySet = new HashSet<>(List.of("Food", "Utilities", "Transport"));
        accountList = new ArrayList<>();
    }

    private void createTransactionList(){
        Map<TransactionType, TransactionList> transactionMap = new HashMap<>();
        transactionMap.put(TransactionType.EXPENSE,new TransactionList());
        transactionMap.put(TransactionType.INCOME,new TransactionList());
        transactionMap.put(TransactionType.MOVEMENT,new TransactionList());
        this.transactionsMap = transactionMap;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets age.
     *
     * @return the age
     */
    public int getAge() {
        return age;
    }

    /**
     * Sets age.
     *
     * @param age the age
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Gets gender.
     *
     * @return the gender
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Sets gender.
     *
     * @param gender the gender
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * Gets transaction lists.
     *
     * @return the transaction lists
     */
    public  Map<TransactionType, TransactionList> getTransactionLists() {
        return transactionsMap;
    }

    /**
     * Gets account list.
     *
     * @return the account list
     */
    public List<AccountInterface> getAccountList() {
        return accountList;
    }

    /**
     * Sets account list.
     *
     * @param accountList the account list
     */
    public void setAccountList(List<AccountInterface> accountList) {
        this.accountList = accountList;
    }

    /**
     * Add account.
     *
     * @param account the account
     */
    public void addAccount(AccountInterface account) {
        this.accountList.add(account);
    }

    /**
     * Is category allowed boolean.
     *
     * @param category the category
     * @return the boolean
     */
    public boolean isCategoryAllowed(String category) {
        return categorySet.contains(category);
    }

    /**
     * Add category.
     *
     * @param category the category
     */
    public void addCategory(String category) {
        categorySet.add(category);
    }

    /**
     * Add transaction.
     *
     * @param transaction the transaction
     */
    public void addTransaction(AbstractTransaction transaction) {
        this.transactionsMap.get(transaction.getType()).addTransaction(transaction);
    }

    /**
     * Gets all account balances.
     *
     * @return the all account balances
     */
    public Map<String, Double> getAllAccountBalances() {
        Map<String, Double> result = new HashMap<>();
        for (AccountInterface account : accountList) {
            result.put(account.getName(), account.getBalance());
        }
        return result;
    }

    /**
     * Gets all transactions.
     *
     * @return the all transactions
     */
    public List<AbstractTransaction> getAllTransactionsFlattened() {
        List<AbstractTransaction> all = new ArrayList<>();
        for (TransactionList txList : transactionsMap.values()) {
            all.addAll(txList.getFlattenedTransactions());
        }
        return all;
    }

    public Set<String> getCategorySet() {
        return categorySet;
    }

    public void setCategorySet(Set<String> categorySet) {
        this.categorySet = categorySet;
    }

    public List<String> getSortedCategories() {
        return categorySet.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList(); // Java 21+ immutable list
    }
}
