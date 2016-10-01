package com.martiansoftware.validation;

import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
abstract class HopeDoubtTester <E extends Exception> {

    protected abstract <T> Validator<T, E> v(T t);
    private Class<E> _eClass;
    
    protected void _setExceptionClass(Class<E> eClass) {
        _eClass = eClass;
    }
    
    private void checkEx(Exception e) throws Exception {
        if (!_eClass.isInstance(e)) throw e;
    }
    
    @Test public void testName() throws Exception {
        try {
            v((String) null).isNotNull();
            fail("non-null check failed");
        } catch (Exception e) {
            checkEx(e);
            assertEquals("value must not be null", e.getMessage());
        }

        try {
            v((String) null).named("test thing").isNotNull();
            fail("non-null check failed");
        } catch (Exception e) {
            checkEx(e);
            assertEquals("test thing must not be null", e.getMessage());
        }

    }
    
    
    
    @Test
    public void testGoodHopes() {
        assertEquals("x", Hope.that("x").isNotNullOrEmpty().value());
    }
 
    public void testLongMap() {
        assertEquals((Long) 12l, Hope.that(12).named("twelve").map(Long::valueOf).value());
    }

}
