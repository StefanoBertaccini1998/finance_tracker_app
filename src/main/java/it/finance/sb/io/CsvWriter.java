package it.finance.sb.io;

import it.finance.sb.logging.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * The type Csv writer.
 *
 * @param <T> the type parameter
 */
public class CsvWriter<T extends CsvSerializable> implements CsvWriterI<T> {

    private static final Logger logger = LoggerFactory.getInstance().getLogger(CsvWriter.class);
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
        logger.info("Exporting to file: " + path.toAbsolutePath() + " - Total items: " + items.size());

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            if (!header.isEmpty()) {
                writer.write(header);
                writer.newLine();
            }

            for (T item : items) {
                writer.write(item.toCsv());
                writer.newLine();
            }
        }catch (IOException e){
            logger.severe("Write failure: " + e.getMessage());
            throw e;
        }
        logger.info("Export completed successfully to: " + path.toAbsolutePath());
    }
}
