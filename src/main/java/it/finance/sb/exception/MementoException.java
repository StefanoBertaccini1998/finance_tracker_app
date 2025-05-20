package it.finance.sb.exception;

/**
 * Custom exception for operation in MementoOperation error
 */
public class MementoException extends Exception {
    public MementoException(String message) {
        super(message);
    }

    public MementoException(String message, Throwable cause) {
        super(message, cause);
    }
}
