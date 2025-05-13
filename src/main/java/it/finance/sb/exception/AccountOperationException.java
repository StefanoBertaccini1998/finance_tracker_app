package it.finance.sb.exception;

/**
 * Custom exception for operation in AccountService error
 */
public class AccountOperationException extends RuntimeException {
    public AccountOperationException(String message) {
        super(message);
    }

    public AccountOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
