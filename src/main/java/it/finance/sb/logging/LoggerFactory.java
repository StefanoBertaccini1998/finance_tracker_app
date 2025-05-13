package it.finance.sb.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The type Logger factory.
 */
public class LoggerFactory {
    private static final Map<Class<?>, Logger> loggers = new HashMap<>();

    /**
     * Gets logger.
     *
     * @param clazz the clazz
     * @return the logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return loggers.computeIfAbsent(clazz, classObject -> Logger.getLogger(classObject.getName()));
    }
}
