package com.martiansoftware.validation;

import java.util.List;
import java.util.Optional;
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
    
    @Test
    public void testName() throws Exception {
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
    public void testValue() throws Exception {
        assertEquals("abc", v("abc").value());
        assertEquals(null, v(null).value());
    }
    
    @Test
    public void testDefaultValue() throws Exception {
        assertEquals("abc", v(null).orElse("abc").value());
        assertNotEquals("abc", v(null).orElse("def").value());
    }

    @Test
    public void testIsNotNull() throws Exception {
        v("abc").isNotNull();
        try {
            v(null).isNotNull(); // should throw
            fail("null passed isNotNull() check");
        } catch (Exception e) {
            checkEx(e);
        }
    }
    
    @Test
    public void testIsNull() throws Exception {
        v(null).isNull();
        try {
            v("abc").isNull(); // should throw
            fail("'abc' passed isNull() check");
        } catch (Exception e) {
            checkEx(e);
        }
    }
    
    @Test
    public void testIsPresent() throws Exception {
        Optional<String> p = Optional.of("present");
        Optional<String> n = Optional.empty();
        
        v(p).isPresent();
        try {
            v(n).isPresent();
            fail("Optional.empty passed isPresent() check");            
        } catch (Exception e) {
            checkEx(e);
        }
        
        try {
            v("not an optional!").isPresent();
            fail("non-Optional passed isPresent() check");
        } catch (Exception e) {
            checkEx(e);
            assertTrue(e.getMessage().contains("is not an Optional"));
        }
    }
    
    @Test
    public void testIsNotPresent() throws Exception {
        Optional<String> p = Optional.of("present");
        Optional<String> n = Optional.empty();
        
        v(n).isNotPresent();
        try {
            v(p).isNotPresent();
            fail("non-empty Optional passed isNotPresent() check");            
        } catch (Exception e) {
            checkEx(e);
        }
        
        try {
            v("not an optional!").isNotPresent();
            fail("non-Optional passed isNotPresent() check");
        } catch (Exception e) {
            checkEx(e);
            assertTrue(e.getMessage().contains("is not an Optional"));
        }
    }

    @Test
    public void testIsNotNullOrEmptyArray() throws Exception {
        Object[] nullArray = null;
        Object[] emptyArray = new Object[0];
        Object[] nonEmptyArray = new Object[1];
        
        try {
            v(nullArray).isNotNullOrEmpty(); 
            fail("null array passed isNotNullOrEmpty test");
        } catch (Exception e) {
            checkEx(e);
        }
        
        try {
            v(emptyArray).isNotNullOrEmpty();
            fail("empty array passed isNotNullOrEmpty test");
        } catch (Exception e) {
            checkEx(e);
        }
        
        v(nonEmptyArray).isNotNullOrEmpty();
    }
    
    @Test
    public void testIsNotNullOrEmptyCollection() throws Exception {
        List<Object> nullCollection = null;
        List<Object> emptyCollection = new java.util.LinkedList<>();
        List<Object> nonEmptyCollection = new java.util.LinkedList<>();
        nonEmptyCollection.add(new Object());
        
        try {
            v(nullCollection).isNotNullOrEmpty(); 
            fail("null collection passed isNotNullOrEmpty test");
        } catch (Exception e) {
            checkEx(e);
        }
        
        try {
            v(emptyCollection).isNotNullOrEmpty();
            fail("empty collection passed isNotNullOrEmpty test");
        } catch (Exception e) {
            checkEx(e);
        }
        
        v(nonEmptyCollection).isNotNullOrEmpty();
    }

    @Test
    public void testIsNotNullOrEmptyString() throws Exception {
        String nullString = null;
        String emptyString = "";
        String nonEmptyString = "hello";
        
        try {
            v(nullString).isNotNullOrEmpty(); 
            fail("null string passed isNotNullOrEmpty test");
        } catch (Exception e) {
            checkEx(e);
        }
        
        try {
            v(emptyString).isNotNullOrEmpty();
            fail("empty string passed isNotNullOrEmpty test");
        } catch (Exception e) {
            checkEx(e);
        }
        
        v(nonEmptyString).isNotNullOrEmpty();
    }
    
    @Test
    public void testIsNotNullOrEmptyObject() throws Exception {
        Object nullObject = null;
        Object nonNullObject = new Object();
        
        try {
            v(nullObject).isNotNullOrEmpty(); 
            fail("null Object passed isNotNullOrEmpty test");
        } catch (Exception e) {
            checkEx(e);
        }
        
        try {
            v(nonNullObject).isNotNullOrEmpty();
            fail("Object (which has no isEmpty method) passed isNotNullOrEmpty test");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            checkEx(e);
            assertTrue(e.getMessage().contains("does not provide"));
        }
    }
    
    @Test
    public void testIsEqualTo() throws Exception {
        v(null).isEqualTo(null);
        v("abc").isEqualTo("abc");
        
        try {
            v("abc").isEqualTo("def");
            fail("'abc' passed isEqualTo('def')");
        } catch (Exception e) {
            checkEx(e);
        }

        try {
            v(null).isEqualTo("def");
            fail("null passed isEqualTo('def')");
        } catch (Exception e) {
            checkEx(e);
        }
        
        try {
            v("abc").isEqualTo(null);
            fail("'abc' passed isEqualTo(null)");
        } catch (Exception e) {
            checkEx(e);
        }
    }
    
    @Test
    public void testIsNotEqualTo() throws Exception {
        v("abc").isNotEqualTo("def");
        v(null).isNotEqualTo("def");
        v("abc").isNotEqualTo(null);
                
        try {
            v(null).isNotEqualTo(null);
            fail("null passed isNotEqualTo(null)");
        } catch (Exception e) {
            checkEx(e);
        }

        try {
            v("abc").isNotEqualTo("abc");
            fail("'abc' passed isNotEqualTo('abc')");
        } catch (Exception e) {
            checkEx(e);
        }
    }

    @Test
    public void testIsFalse() throws Exception {
        v("abc").isFalse(s -> s.length() == 1000);
        try {
            v("abc").isFalse(s -> s.length() == 3);
            fail("abc passed isFalse(s -> s.length() == 3)");
        } catch (Exception e) {
            checkEx(e);
        }
    }
    
    @Test
    public void testIsTrue() throws Exception {
        v("abc").isTrue(s -> s.length() == 3);
        try {
            v("abc").isTrue(s -> s.length() == 1000);
            fail("abc passed isTrue(s -> s.length() == 1000)");
        } catch (Exception e) {
            checkEx(e);
        }
    }
    
    @Test
    public void testMatches() throws Exception {
        v("abcdef").matches("^[a-zA-Z]+$");
        try {
            v("abcdef").matches("^[0-9]+$");
            fail("'abcdef' passed matches('^[0-9+$])");
        } catch (Exception e) {
            checkEx(e);
        }
    }
    
    @Test
    public void testMatchesAny() throws Exception {
        v("abcdef").matchesAny("^[0-9]+$", "BLAH", "^[a-zA-Z]+$");
                
        try {
            v("abcdef").matchesAny("^[0-9]+$", "no match");
            fail("'abcdef' passed matchAny with no actual matches");
        } catch (Exception e) {
            checkEx(e);
        }
        
        try {
            v("abcdef").matchesAny();
            fail("'abcdef' passed matchAny with no actual expressions");
        } catch (Exception e) {
            checkEx(e);
        }
    }
    
    @Test
    public void testMap() {
        assertEquals((Long) 12l, Hope.that(12).named("twelve").map(Long::valueOf).value());
    }

}
