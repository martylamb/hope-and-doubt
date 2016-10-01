package com.martiansoftware.validation;

import org.junit.Before;

/**
 *
 * @author mlamb
 */
public class DoubtTest extends HopeDoubtTester {

    @Before
    public void setupExceptionClass() {
        _setExceptionClass(CheckedValidationException.class);
    }
    
    @Override
    protected Doubt v(Object o) {
        return Doubt.that(o);
    }
    
}
