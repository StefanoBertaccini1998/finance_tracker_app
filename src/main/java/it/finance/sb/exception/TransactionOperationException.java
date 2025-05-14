package it.finance.sb.exception;

/**
 * Custom exception for operation in TransactionService error
 */
public class TransactionOperationException extends Exception {
    public TransactionOperationException(String message) {
        super(message);
    }

    public TransactionOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
