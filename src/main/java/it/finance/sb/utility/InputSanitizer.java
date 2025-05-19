package it.finance.sb.utility;

import it.finance.sb.annotation.Sanitize;
import it.finance.sb.exception.DataValidationException;

import java.lang.reflect.Field;
import java.util.Date;

public class InputSanitizer {

    public static void validate(Object obj) throws DataValidationException {
        if (obj == null) throw new DataValidationException("Cannot validate null object.");

        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Sanitize.class)) continue;

            field.setAccessible(true);
            Sanitize rule = field.getAnnotation(Sanitize.class);
            try {
                Object value = field.get(obj);
                String fieldName = field.getName();

                checkNull(value, fieldName, rule);
                checkString(value, fieldName, rule);
                checkNumber(value, fieldName, rule);
                checkDate(value, fieldName, rule);

            } catch (IllegalAccessException e) {
                throw new DataValidationException("Unable to access field: " + field.getName(), e);
            }
        }
    }

    private static void checkNull(Object value, String fieldName, Sanitize rule) throws DataValidationException {
        if (rule.nonNull() && value == null) {
            throw new DataValidationException("Field '" + fieldName + "' must not be null.");
        }
    }

    private static void checkString(Object value, String fieldName, Sanitize rule) throws DataValidationException {
        if (!(value instanceof String s)) return;

        if (rule.notBlank() && s.isBlank()) {
            throw new DataValidationException("Field '" + fieldName + "' must not be blank.");
        }

        if (s.length() > rule.maxLength()) {
            throw new DataValidationException("Field '" + fieldName + "' exceeds max length (" + rule.maxLength() + ").");
        }
    }

    private static void checkNumber(Object value, String fieldName, Sanitize rule) throws DataValidationException {
        if (!(value instanceof Number n)) return;

        if (rule.positiveNumber() && n.doubleValue() <= 0) {
            throw new DataValidationException("Field '" + fieldName + "' must be a positive number.");
        }
    }

    private static void checkDate(Object value, String fieldName, Sanitize rule) throws DataValidationException {
        if (!(value instanceof Date d)) return;

        long now = System.currentTimeMillis();
        if (rule.dateMustBePast() && d.getTime() > now) {
            throw new DataValidationException("Field '" + fieldName + "' must be a date in the past.");
        }

        if (rule.dateMustBeFuture() && d.getTime() < now) {
            throw new DataValidationException("Field '" + fieldName + "' must be a date in the future.");
        }
    }
}
