package it.finance.sb.utility;

import it.finance.sb.annotation.Sanitize;
import it.finance.sb.exception.DataValidationException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class InputSanitizer {

    public static void validate(Object obj) throws DataValidationException {
        for (Field field : getAllFields(obj.getClass())) {
            if (field.isAnnotationPresent(Sanitize.class)) {
                field.setAccessible(true);
                Sanitize rule = field.getAnnotation(Sanitize.class);
                try {
                    Object value = getObject(obj, field, rule);

                    // Positive number
                    if (value instanceof Number n && rule.positiveNumber()) {
                        if (n.doubleValue() <= 0) {
                            throw new DataValidationException("Field '" + field.getName() + "' must be positive.");
                        }
                    }

                    // Date rules
                    if (value instanceof Date d) {
                        long now = System.currentTimeMillis();
                        if (rule.dateMustBePast() && d.getTime() > now) {
                            throw new DataValidationException("Field '" + field.getName() + "' must be a date in the past.");
                        }
                        if (rule.dateMustBeFuture() && d.getTime() < now) {
                            throw new DataValidationException("Field '" + field.getName() + "' must be a date in the future.");
                        }
                    }

                } catch (IllegalAccessException e) {
                    throw new DataValidationException("Access error for field '" + field.getName() + "'", e);
                }
            }
        }
    }

    private static Object getObject(Object obj, Field field, Sanitize rule) throws IllegalAccessException, DataValidationException {
        Object value = field.get(obj);

        // Non-null
        if (rule.nonNull() && value == null) {
            throw new DataValidationException("Field '" + field.getName() + "' must not be null.");
        }

        // String checks
        if (value instanceof String s) {
            if (rule.notBlank() && s.isBlank()) {
                throw new DataValidationException("Field '" + field.getName() + "' must not be blank.");
            }
            if (s.length() > rule.maxLength()) {
                throw new DataValidationException("Field '" + field.getName() + "' exceeds max length (" + rule.maxLength() + ")");
            }
        }
        return value;
    }

    // Recursively collect all declared fields including superclass fields
    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null && type != Object.class) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        return fields;
    }
}
