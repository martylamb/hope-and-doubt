package com.martiansoftware.validation;

/**
 * A simple pessimistic validator that throws <code>CheckedValidationException</code>s
 * if validation fails.
 * 
 * @author <a href="http://martylamb.com">Marty Lamb</a>
 */
public class Doubt<T> extends Validator<T, CheckedValidationException> {

    private Doubt(T t) {
        super(t, CheckedValidationException::new);
    }
    
    public static <T> Doubt<T> that(T t) {
        return new Doubt(t);
    }
}
