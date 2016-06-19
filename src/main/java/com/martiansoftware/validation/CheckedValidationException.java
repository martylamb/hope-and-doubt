package com.martiansoftware.validation;

/**
 *
 * @author mlamb
 */
public class CheckedValidationException extends Exception {
  
    public CheckedValidationException(String msg) {
        super(msg);
    }
    
    public CheckedValidationException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }
    
}
