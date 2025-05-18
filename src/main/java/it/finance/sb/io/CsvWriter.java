package it.finance.sb.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvWriter<T extends CsvSerializable> implements CsvExporter<T> {


    private final String header;

    public CsvWriter(String header) {
        this.header = header;
    }


    @Override
    public void exportToFile(List<T> items, Path path) throws IOException {
        if (items == null || path == null) {
            throw new IllegalArgumentException("CsvWriter: items or path cannot be null.");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            if (!header.isEmpty()) {
                writer.write(header);
                writer.newLine();
            }

            for (T item : items) {
                writer.write(item.toCsv());
                writer.newLine();
            }
        }
    }
}
