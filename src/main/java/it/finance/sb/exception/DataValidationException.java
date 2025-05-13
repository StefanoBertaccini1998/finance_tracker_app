package it.finance.sb.exception;

/**
 * Custom exception for data validation error
 */
public class DataValidationException extends RuntimeException {
    public DataValidationException(String message) {
        super(message);
    }

    public DataValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}