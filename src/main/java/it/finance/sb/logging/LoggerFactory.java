package it.finance.sb.logging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * The type Logger factory.
 */
public class LoggerFactory {

    private static final Map<Class<?>, Logger> loggers = new HashMap<>();
    private static final String LOG_FILE_PATH = "finance_app.log";

    /**
     * Gets logger.
     *
     * @param clazz the clazz
     * @return the logger
     */
    public static Logger getLogger(Class<?> clazz) {
        Logger logger = loggers.computeIfAbsent(clazz, classObject -> Logger.getLogger(classObject.getName()));
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        if (logger.getHandlers().length == 0) {
            try {
                FileHandler fileHandler = new FileHandler(LOG_FILE_PATH, true);
                fileHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(fileHandler);
            } catch (IOException e) {
                System.err.println("⚠️ Failed to initialize logger file: " + e.getMessage());
            }

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);
            logger.setLevel(Level.ALL);
        }

        return logger;
    }
}

