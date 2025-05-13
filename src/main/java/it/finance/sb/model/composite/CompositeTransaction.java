package it.finance.sb.model.composite;

/**
 * The interface Composite transaction.
 */
public interface CompositeTransaction {
    /**
     * Display transaction.
     */
    void displayTransaction();

    /**
     * Gets total.
     *
     * @return the total
     */
    double getTotal();
}
