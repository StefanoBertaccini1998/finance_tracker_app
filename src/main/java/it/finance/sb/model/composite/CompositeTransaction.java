package it.finance.sb.model.composite;

/**
 * CompositeTransaction represents a component in a composite structure.
 * Leaf nodes are transactions, while composite nodes are lists of transactions.
 */
public interface CompositeTransaction {
    /**
     * Displays a single transaction or recursively displays all nested ones.
     */
    void displayTransaction();

    /**
     * Computes the total amount from this transaction or all nested ones.
     *
     * @return total amount
     */
    double getTotal();
}
