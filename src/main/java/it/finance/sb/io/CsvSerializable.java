package it.finance.sb.io;

import it.finance.sb.model.transaction.AbstractTransaction;

public interface CsvSerializable {
    String toCsv();          // serialize
    static AbstractTransaction fromCsv(String csvLine) throws Exception { return null; } // implement in subclasses
}