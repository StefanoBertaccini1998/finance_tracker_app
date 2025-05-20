package it.finance.sb.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface CsvWriterI<T> {
    void exportToFile(List<T> data, Path path) throws IOException;
}