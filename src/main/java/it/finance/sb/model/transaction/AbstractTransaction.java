package it.finance.sb.model.transaction;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.finance.sb.annotation.Sanitize;
import it.finance.sb.model.composite.CompositeTransaction;
import it.finance.sb.io.CsvSerializable;

import java.util.Date;


/**
 * The type Abstract transaction.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "objectType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = IncomeTransaction.class, name = "IncomeTransaction"),
        @JsonSubTypes.Type(value = ExpenseTransaction.class, name = "ExpenseTransaction"),
        @JsonSubTypes.Type(value = MovementTransaction.class, name = "MovementTransaction")
})
public abstract class AbstractTransaction implements CompositeTransaction, CsvSerializable {
    /**
     * The Transaction id.
     */
    @JsonProperty
    protected int transactionId;
    private static int idCounter = 0;
    /**
     * The Amount.
     */
    @Sanitize(positiveNumber = true)
    protected double amount;
    /**
     * The Reason.
     */
    @Sanitize(maxLength = 50)
    protected String reason;
    /**
     * The Date.
     */
    @Sanitize(nonNull = true)
    protected Date date;
    /**
     * The Category.
     */
    @Sanitize(maxLength = 30)
    protected String category;

    /**
     * The Type.
     */
    protected TransactionType type;

    /**
     * Instantiates a new Abstract transaction.
     *
     * @param amount   the amount
     * @param category the category
     * @param reason   the reason
     * @param date     the date
     * @param type     the type
     */
    protected AbstractTransaction(double amount, String category, String reason, Date date, TransactionType type) {
        this.amount = amount;
        this.reason = reason;
        this.date = date;
        this.type = type;
        this.category = category;
        this.transactionId = ++idCounter;
    }

    public AbstractTransaction() {
        this.transactionId = ++idCounter;
    }

    /**
     * Gets amount.
     *
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets amount.
     *
     * @param amount the amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Gets reason.
     *
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets reason.
     *
     * @param reason the reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Gets date.
     *
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets date.
     *
     * @param date the date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(TransactionType type) {
        this.type = type;
    }

    /**
     * Gets transaction id.
     *
     * @return the transaction id
     */
    public int getTransactionId() {
        return transactionId;
    }

    /**
     * Gets category.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets category.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    public String formatCsvLine(String from, String to, String category, String reason, long dateMillis) {
        return String.join(",",
                String.valueOf(transactionId),
                type.name(),
                String.valueOf(amount),
                from == null ? "" : from,
                to == null ? "" : to,
                category == null ? "" : category,
                reason == null ? "" : reason,
                String.valueOf(dateMillis)
        );
    }
}


