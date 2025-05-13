package it.finance.sb.utility;

import it.finance.sb.annotation.Sanitize;
import it.finance.sb.exception.DataValidationException;

import java.lang.reflect.Field;

/**
 * The type Input sanitizer.
 */
public class InputSanitizer {

    /**
     * Validate.
     *
     * @param obj the obj
     */
    public static void validate(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Sanitize.class)) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    Sanitize rule = field.getAnnotation(Sanitize.class);

                    if (value instanceof String s) {
                        if (rule.notBlank() && s.isBlank()) {
                            throw new DataValidationException("Field '" + field.getName() + "' must not be blank.");
                        }
                        if (s.length() > rule.maxLength()) {
                            throw new DataValidationException("Field '" + field.getName() + "' exceeds max length.");
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new DataValidationException("Access error during sanitization.");
                }
            }
        }
    }
}
