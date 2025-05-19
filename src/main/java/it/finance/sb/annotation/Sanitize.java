package it.finance.sb.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Sanitize {
    boolean notBlank() default false;

    int maxLength() default 255;

    boolean positiveNumber() default false;

    boolean nonNull() default false;

    boolean dateMustBePast() default false;

    boolean dateMustBeFuture() default false;

    double min() default Double.NEGATIVE_INFINITY;

    double max() default Double.POSITIVE_INFINITY;
}
