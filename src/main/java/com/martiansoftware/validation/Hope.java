package com.martiansoftware.validation;

/**
 * A simple optimistic validator that throws <code>UncheckedValidationException</code>s
 * if validation fails.
 * 
 * @author <a href="http://martylamb.com">Marty Lamb</a>
 */
public class Hope<T> extends Validator<T, UncheckedValidationException> {

    private Hope(T t) {
        super(t, UncheckedValidationException::new);
    }
    
    public static <T> Hope<T> that(T t) {
        return new Hope(t);
    }
}
