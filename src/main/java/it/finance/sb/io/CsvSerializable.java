package it.finance.sb.io;

public interface CsvSerializable {
    String[] toCsv();         // Object → CSV row
}