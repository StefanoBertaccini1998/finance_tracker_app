package it.finance.sb.exception;

public class UserCancelledException extends Exception {
    public UserCancelledException(String message) {
        super(message);
    }
    public UserCancelledException() {
        super();
    }

    public UserCancelledException(String message, Throwable cause) {
        super(message, cause);
    }
}
