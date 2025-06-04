package it.finance.sb.io;

import it.finance.sb.exception.FileIOException;
import it.finance.sb.logging.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The type Csv writer.
 *
 * @param <T> the type parameter
 */
public class CsvWriter<T extends CsvSerializable> implements WriterI<T> {

    private static final Logger logger = LoggerFactory.getSafeLogger(CsvWriter.class);
    private final String header;

    /**
     * Instantiates a new Csv writer.
     *
     * @param header the header
     */
    public CsvWriter(String header) {
        this.header = header;
    }


    @Override
    public void exportToFile(List<T> items, Path path) throws IOException {
        if (items == null || path == null) {
            logger.severe("Export failed: null list or path.");
            throw new IllegalArgumentException("CsvWriter: items or path cannot be null.");
        }
        logger.info(()->"Exporting to file: " + path.toAbsolutePath() + " - Total items: " + items.size());
        Path parent = path.toAbsolutePath().getParent();

        if (parent != null && !Files.exists(parent))
            Files.createDirectories(parent);        // create missing folders

        try (BufferedWriter writer = Files.newBufferedWriter(
                path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            if (!header.isEmpty()) {
                writer.write(header);
                writer.newLine();
            }

            for (T item : items) {
                String line = Arrays.stream(item.toCsv())
                        .map(CsvWriter::escape)      // the helper we added
                        .collect(Collectors.joining(","));
                writer.write(line);
                writer.newLine();
            }
        }catch (IOException e){
            throw new FileIOException("Write failure: "+e.getMessage(),e);
        }
        logger.info(()->"Export completed successfully to: " + path.toAbsolutePath());
    }

    private static String escape(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
