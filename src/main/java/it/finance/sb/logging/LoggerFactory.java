package it.finance.sb.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class LoggerFactory {
    private static final Map<Class<?>, Logger> loggers = new HashMap<>();

    public static Logger getLogger(Class<?> clazz) {
        return loggers.computeIfAbsent(clazz, classObject -> Logger.getLogger(classObject.getName()));
    }
}
