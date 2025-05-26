package it.finance.sb.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.finance.sb.annotation.Sanitize;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;

import java.util.*;

/**
 * The type User.
 */
public class User {
    private static int idCounter = 0;
    @JsonProperty
    private final int userId;
    @Sanitize(notBlank = true, maxLength = 50)
    private String name;
    @Sanitize(positiveNumber = true)
    private int age;
    private Gender gender;
    private final Map<TransactionType, TransactionList> transactionLists = new EnumMap<>(TransactionType.class);
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
        this.userId = ++idCounter;
        this.name = name;
        this.age = age;
        this.gender = gender;
        for (TransactionType type : TransactionType.values()) {
            transactionLists.put(type, new TransactionList());
        }
        this.categorySet = new HashSet<>(List.of("Food", "Utilities", "Transport"));
        accountList = new ArrayList<>();
    }

    /**
     * Gets UserID.
     *
     * @return the ID
     */
    public int getUserId() {
        return userId;
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
        return Collections.unmodifiableMap(transactionLists);
    }

    /**
     * Gets account list.
     *
     * @return the account list
     */
    public List<AccountInterface> getAccountList() {
        return Collections.unmodifiableList(accountList);
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
     * Remove account.
     *
     * @param account the account
     */
    public void removeAccount(AccountInterface account) {
        this.accountList.remove(account);
    }

    /**
     * Is category allowed boolean.
     *
     * @param category the category
     * @return the boolean
     */
    public boolean isCategoryAllowed(String category) {
        return categorySet.contains(category.toLowerCase());
    }

    /**
     * Add category.
     *
     * @param category the category
     */
    public void addCategory(String category) {
        categorySet.add(category.toLowerCase());
    }

    /**
     * Add transaction.
     *
     * @param transaction the transaction
     */
    public void addTransaction(AbstractTransaction transaction) {
        this.transactionLists.get(transaction.getType()).addTransaction(transaction);
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
        return Map.copyOf(result);
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
