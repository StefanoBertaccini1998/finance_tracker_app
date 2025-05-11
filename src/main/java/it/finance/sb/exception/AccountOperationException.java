package it.finance.sb.exception;

public class AccountOperationException extends RuntimeException {
    public AccountOperationException(String message) {
        super(message);
    }

    public AccountOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
