package it.finance.sb.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Sanitize {
    boolean notBlank() default true;
    int maxLength() default 100;
}
