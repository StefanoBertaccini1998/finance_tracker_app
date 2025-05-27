package it.finance.sb.io;

import it.finance.sb.exception.FileIOException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface WriterI<T> {
    void exportToFile(List<T> data, Path path) throws IOException;
}