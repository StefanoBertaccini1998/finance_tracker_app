package it.finance.sb.exception;

/**
 * Custom exception for data validation error
 */
public class DataValidationException extends Exception {
    public DataValidationException(String message) {
        super(message);
    }

    public DataValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}