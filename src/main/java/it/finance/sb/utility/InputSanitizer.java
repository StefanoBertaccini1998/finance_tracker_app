package it.finance.sb.utility;

import it.finance.sb.annotation.Sanitize;
import it.finance.sb.exception.DataValidationException;

import java.lang.reflect.Field;
import java.util.Date;

public class InputSanitizer {

    public static void validate(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Sanitize.class)) {
                field.setAccessible(true);
                Sanitize rule = field.getAnnotation(Sanitize.class);
                try {
                    Object value = field.get(obj);

                    // ✅ null check
                    if (rule.nonNull() && value == null) {
                        throw new DataValidationException("Field '" + field.getName() + "' must not be null.");
                    }

                    if (value instanceof String s) {
                        if (rule.notBlank() && s.isBlank()) {
                            throw new DataValidationException("Field '" + field.getName() + "' must not be blank.");
                        }
                        if (s.length() > rule.maxLength()) {
                            throw new DataValidationException("Field '" + field.getName() + "' exceeds max length (" + rule.maxLength() + ")");
                        }
                    }

                    if (value instanceof Number n && rule.positiveNumber()) {
                        if (n.doubleValue() <= 0) {
                            throw new DataValidationException("Field '" + field.getName() + "' must be positive.");
                        }
                    }

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
                    throw new DataValidationException("Sanitization failed accessing field '" + field.getName() + "'.", e);
                }
            }
        }
    }
}
