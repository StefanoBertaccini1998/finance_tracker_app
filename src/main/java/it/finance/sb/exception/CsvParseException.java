package it.finance.sb.exception;

public class CsvParseException extends Exception {
    public CsvParseException(String msg, Throwable cause) { super(msg, cause); }
    public CsvParseException(String msg)                  { super(msg); }
}