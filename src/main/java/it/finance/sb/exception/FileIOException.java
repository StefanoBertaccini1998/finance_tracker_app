package it.finance.sb.exception;

/**
 * Custom exception for data validation error
 */
public class FileIOException extends Exception {
    public FileIOException(String message) {
        super(message);
    }

    public FileIOException(String message, Throwable cause) {
        super(message, cause);
    }
}