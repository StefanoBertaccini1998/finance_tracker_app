package it.finance.sb.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface CsvImporter<T> {
    List<T> importFrom(Path inputFile,
                       Map<String, ?> referenceMap,
                       boolean autoCreate,
                       boolean skipErrors,
                       List<String> errorLog) throws IOException;
}
