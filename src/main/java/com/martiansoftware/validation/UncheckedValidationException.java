package com.martiansoftware.validation;

/**
 *
 * @author mlamb
 */
public class UncheckedValidationException extends RuntimeException {
    
    public UncheckedValidationException(String msg) {
        super(msg);
    }
    
    public UncheckedValidationException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }
}
