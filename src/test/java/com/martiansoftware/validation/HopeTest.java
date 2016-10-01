package com.martiansoftware.validation;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author mlamb
 */
public class HopeTest extends HopeDoubtTester {

    @Before
    public void setupExceptionClass() {
        _setExceptionClass(UncheckedValidationException.class);
    }
    
    @Override
    protected Hope v(Object o) {
        return Hope.that(o);
    }
    
}
