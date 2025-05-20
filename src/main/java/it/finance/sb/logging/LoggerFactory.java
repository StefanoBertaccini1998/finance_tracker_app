package it.finance.sb.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 * Singleton Logger Factory for centralized, file-only logging across the app.
 */
public class LoggerFactory {

    private static final String LOG_DIR = "log";
    private static final String LOG_FILE_NAME = "finance_app_%s.log";
    private static final Level GLOBAL_LOG_LEVEL = Level.INFO;

    private static LoggerFactory instance;
    private final Logger sharedLogger;

    private LoggerFactory() {
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
        } catch (IOException e) {
            System.err.println("⚠️ Failed to create log directory: " + e.getMessage());
        }

        sharedLogger = Logger.getLogger("GlobalAppLogger");
        sharedLogger.setUseParentHandlers(false);
        sharedLogger.setLevel(GLOBAL_LOG_LEVEL);

        if (sharedLogger.getHandlers().length == 0) {
            try {
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                String fileName = String.format(LOG_FILE_NAME, date);
                FileHandler fileHandler = new FileHandler(LOG_DIR + "/" + fileName, true);

                fileHandler.setLevel(GLOBAL_LOG_LEVEL);
                fileHandler.setFormatter(new Formatter() {
                    @Override
                    public String format(LogRecord record) {
                        return String.format("[%1$tF %1$tT] [%2$-7s] [%3$s] %4$s %n",
                                new Date(record.getMillis()),
                                record.getLevel().getName(),
                                record.getLoggerName(),
                                record.getMessage());
                    }
                });

                sharedLogger.addHandler(fileHandler);
                // 🔕 No console handler

            } catch (IOException e) {
                System.err.println("⚠️ Logger initialization failed: " + e.getMessage());
            }
        }
    }

    /**
     * Get the singleton instance of LoggerFactory.
     */
    public static synchronized LoggerFactory getInstance() {
        if (instance == null) {
            instance = new LoggerFactory();
        }
        return instance;
    }

    /**
     * Get the centralized shared logger with the caller’s class name tagged.
     */
    public Logger getLogger(Class<?> clazz) {
        // This just tags the logger name with the class for log readability
        sharedLogger.setFilter(record -> {
            record.setLoggerName(clazz.getSimpleName());
            return true;
        });
        return sharedLogger;
    }
}
