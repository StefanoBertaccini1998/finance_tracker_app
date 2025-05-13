package it.finance.sb.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvWriter<T extends CsvSerializable> {
    public void writeToFile(List<T> data, Path outputFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            for (T item : data) {
                writer.write(item.toCsv());
                writer.newLine();
            }
        }
    }
}
