package it.finance.sb.logging;

import it.finance.sb.exception.LoggingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

/**
 * LoggerFactory provides centralized logging using Java's built-in logging,
 * applying Singleton, Factory, and security-safe logging to file only.
 */
public class LoggerFactory {

    private static final String LOG_DIR = "log";
    private static final String LOG_FILE_NAME = "finance_app_%s.log";
    private static final Level GLOBAL_LOG_LEVEL = Level.INFO;

    private static LoggerFactory instance;
    private final Handler sharedHandler;
    private final ConcurrentHashMap<String, Logger> loggerCache = new ConcurrentHashMap<>();

    private LoggerFactory() throws LoggingException {
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
        } catch (IOException e) {
            System.err.println("⚠️ Failed to create log directory: " + e.getMessage());
        }

        this.sharedHandler = createFileHandler();
    }

    public static synchronized LoggerFactory getInstance() throws LoggingException {
        if (instance == null) {
            instance = new LoggerFactory();
        }
        return instance;
    }

    /**
     * Returns a logger specific to the given class,
     * sharing the global file handler and format.
     */
    private Logger getLogger(Class<?> clazz) {
        return loggerCache.computeIfAbsent(clazz.getSimpleName(), name -> {
            Logger logger = Logger.getLogger(name);
            logger.setUseParentHandlers(false); // Disable console
            logger.setLevel(GLOBAL_LOG_LEVEL);

            // Prevent duplicate handler attachment
            if (logger.getHandlers().length == 0) {
                logger.addHandler(sharedHandler);
            }
            return logger;
        });
    }

    public static Logger getSafeLogger(Class<?> clazz) {
        try {
            return getInstance().getLogger(clazz);
        } catch (LoggingException e) {
            Logger fallback = Logger.getAnonymousLogger();
            fallback.warning("⚠️ LoggerFactory fallback used for " + clazz.getSimpleName() + ": " + e.getMessage());
            return fallback;
        }
    }

    /**
     * Configures and returns the shared file handler.
     */
    private Handler createFileHandler() throws LoggingException {
        try {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String fileName = String.format(LOG_FILE_NAME, date);
            FileHandler fileHandler = new FileHandler(LOG_DIR + "/" + fileName, true);
            fileHandler.setLevel(GLOBAL_LOG_LEVEL);
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord logRecord) {
                    return String.format("[%1$tF %1$tT] [%2$-7s] [%3$s] %4$s %n",
                            new Date(logRecord.getMillis()),
                            logRecord.getLevel().getName(),
                            logRecord.getLoggerName(),
                            logRecord.getMessage());
                }
            });
            return fileHandler;
        } catch (IOException e) {
            throw new LoggingException("Logger file handler initialization failed", e);
        }
    }
}
