package it.finance.sb.mapper;

import it.finance.sb.model.composite.TransactionList;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type User mapper.
 */
public class UserMapper {

    /**
     * To snapshot user snapshot.
     *
     * @param user the user
     * @return the user snapshot
     */
    public static UserSnapshot toSnapshot(User user) {
        Map<TransactionType, List<AbstractTransaction>> flattened = new HashMap<>();
        for (Map.Entry<TransactionType, TransactionList> entry : user.getTransactionLists().entrySet()) {
            flattened.put(entry.getKey(), entry.getValue().getFlattenedTransactions());
        }
        return new UserSnapshot(
                user.getName(),
                user.getAge(),
                user.getGender(),
                List.copyOf(user.getCategorySet()),
                List.copyOf(user.getAccountList()),
                flattened
        );
    }

    /**
     * From snapshot user.
     *
     * @param snapshot the snapshot
     * @return the user
     */
    public static User fromSnapshot(UserSnapshot snapshot) {
        User user = new User(snapshot.name(), snapshot.age(), snapshot.gender());

        snapshot.categories().forEach(user::addCategory);
        snapshot.accounts().forEach(user::addAccount);

        // Rebuild TransactionList composites
        snapshot.transactions().forEach((type, flatList) -> {
            TransactionList composite = user.getTransactionLists().get(type);
            flatList.forEach(composite::addTransaction);
        });

        return user;
    }
}
