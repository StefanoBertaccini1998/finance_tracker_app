package it.finance.sb.exception;

import java.io.IOException;
import java.util.List;

/**
 * Custom exception for operation in FileIO error
 */
public class FileIOException extends IOException {
    private final List<String> errorLog;

    public FileIOException(String message, Throwable cause) {
        super(message, cause);
        this.errorLog = List.of(); // fallback vuoto
    }

    public FileIOException(String message, Throwable cause, List<String> errorLog) {
        super(message, cause);
        this.errorLog = errorLog == null ? List.of() : List.copyOf(errorLog);
    }

    public List<String> getErrorLog() {
        return errorLog;
    }
}