package it.finance.sb.utility;

import it.finance.sb.annotation.Sanitize;
import it.finance.sb.exception.DataValidationException;
import it.finance.sb.logging.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility for validating fields annotated with @Sanitize.
 */
public class InputSanitizer {
    private static final Logger logger = LoggerFactory.getInstance().getLogger(InputSanitizer.class);

    /**
     * Validates the given object by applying the constraints defined in @Sanitize annotations.
     * @param obj the object to validate
     * @throws DataValidationException if one or more constraints are violated
     */
    public static void validate(Object obj) throws DataValidationException {
        for (Field field : getAllFields(obj.getClass())) {
            if (field.isAnnotationPresent(Sanitize.class)) {
                if (!field.canAccess(obj)) {
                    try {
                        field.setAccessible(true);
                    } catch (SecurityException se) {
                        logger.warning("Security manager prevented access to field '" + field.getName() + "'");
                        throw new DataValidationException("Security exception accessing field '" + field.getName() + "'", se);
                    }
                }
                Sanitize rule = field.getAnnotation(Sanitize.class);
                try {
                    Object value = getObject(obj, field, rule);

                    // Positive number constraint
                    if (value instanceof Number n && rule.positiveNumber()) {
                        if (n.doubleValue() < 0) {
                            logAndThrow(field, value, "must be positive.");
                        }
                    }

                    // Date constraints
                    if (value instanceof Date d) {
                        long now = System.currentTimeMillis();
                        if (rule.dateMustBePast() && d.getTime() > now) {
                            logAndThrow(field, value, "must be a date in the past.");
                        }
                        if (rule.dateMustBeFuture() && d.getTime() < now) {
                            logAndThrow(field, value, "must be a date in the future.");
                        }
                    }

                } catch (IllegalAccessException e) {
                    logger.warning("Access error for field '" + field.getName() + "'");
                    throw new DataValidationException("Access error for field '" + field.getName() + "'", e);
                }
            }
        }
    }

    private static Object getObject(Object obj, Field field, Sanitize rule) throws IllegalAccessException, DataValidationException {
        Object value = field.get(obj);

        // Non-null constraint
        if (rule.nonNull() && value == null) {
            logAndThrow(field, value, "must not be null.");
        }

        // String constraints
        if (value instanceof String s) {
            if (rule.notBlank() && s.isBlank()) {
                logAndThrow(field, value, "must not be blank.");
            }
            if (rule.maxLength() > 0 && s.length() > rule.maxLength()) {
                logAndThrow(field, value, "exceeds max length (" + rule.maxLength() + ")");
            }
        }

        return value;
    }

    private static void logAndThrow(Field field, Object value, String message) throws DataValidationException {
        logger.warning("Validation failed: Field '" + field.getName() + "' = " + value + " did not meet constraint: " + message);
        throw new DataValidationException("Field '" + field.getName() + "' " + message);
    }

    /**
     * Recursively collects all declared fields including superclass fields.
     */
    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null && type != Object.class) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        return fields;
    }
}
