package it.finance.sb.io;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.model.account.AccountInterface;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface ImporterI<T> {
    List<T> importFrom(Path inputFile,
                       Map<String, AccountInterface> referenceMap,
                       boolean autoCreate,
                       boolean skipErrors,
                       List<String> errorLog) throws IOException, DataValidationException;
}
