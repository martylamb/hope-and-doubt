package com.martiansoftware.validation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A simple optimistic validator that throws <code>UncheckedValidationException</code>s
 * if validation fails.
 * 
 * @author <a href="http://martylamb.com">Marty Lamb</a>
 */
public class Hope<T> {

    /**
     * the value that is being validated
     */
    private final T _value;
    
    /**
     * an optional name for the value being validated (only used in exception messages)
     */
    private volatile String _name = "value";
    
    /**
     * Creates a new optimistic validator for the specified value
     * @param value the value being validated
     */
    private Hope(T value) {
        _value = value;
    }

    /**
     * Creates a new optimistic validator for the specified value
     * @param <T> the type of object being validated
     * @param value the value being validated
     * @return a new optimistic validator for the specified value
     */
    public static <T> Hope<T> that(T value) {
        return new Hope(value);
    }
    
    /**
     * Assigns an (optional) name to the value being validated.  This is useful
     * if validating multiple method parameters (for example) so that any exceptions
     * refer to the parameter by name
     * 
     * @param name the name of the value being validated
     * @return 
     */
    public Hope<T> named(String name) {
        _name = name;
        return this;
    }
    
    /**
     * Returns the value being validated
     * @return the value being validated
     */
    public T value() { return _value; }
    
    /**
     * Optimistically validates that the value is not null.
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isNotNull() throws UncheckedValidationException {
        if (_value == null) invalid(String.format("%s must not be null", _name));
        return this;
    }

    /**
     * Optimistically validates that the value is null.
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isNull() throws UncheckedValidationException {
        if (_value != null) invalid("%s must be null", _name);
        return this;
    }

    // checks that the value is optional and is present
    private boolean _isPresent() throws UncheckedValidationException  {
        isNotNull();
        if (_value instanceof Optional) return ((Optional) _value).isPresent();
        else invalid("%s is not an Optional; check for isPresent() is not valid.", _name);
        return false; // not reachable but needed to satisfy compiler
    }
    
    /**
     * Optimistically validates that the value is not present (must be an Optional)
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isNotPresent() throws UncheckedValidationException {
        if (_isPresent()) invalid("%s must not be present", _name);
        return this;
    }
    
    /**
     * Optimistically validates that the value is present (must be an Optional)
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isPresent() throws UncheckedValidationException {
        if (!_isPresent()) invalid("%s must be present", _name);
        return this;
    }
    
    /**
     * Optimistically validates that the value is not null or empty.  Emptiness
     * is determined as follows:
     * <ul>
     * <li>A zero-length array is empty.</li>
     * <li>If the object has a boolean isEmpty() method that returns true (e.g., a Collection), it is empty.</li>
     * </ul>
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isNotNullOrEmpty() throws UncheckedValidationException  {
        isNotNull();
        boolean oops = false;
        if (_value.getClass().isArray()) {
            oops = ((Object[]) _value).length == 0;
        } else {
            try {
                Method m = _value.getClass().getMethod("isEmpty");
                if (m.getReturnType().equals(Boolean.TYPE)) {
                    oops = (Boolean) m.invoke(_value);
                } else throw new NoSuchMethodException("unexpected return type: " + m.getReturnType());
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new IllegalStateException(String.format("class %s does not provide an accessible boolean isEmpty() method", _value.getClass().getName()), e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(String.format("unable to invoke %s.isEmpty()", _value.getClass().getName()), e);
            }
        }
        if (oops) invalid("%s must not be empty", _name);
        return this;
    }

    /**
     * Optimistically validates that the value is not equal to another Object
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isNotEqualTo(Object other) throws UncheckedValidationException  {
        if (Objects.equals(_value, other)) invalid("%s must not be equal to '%s'", _name, other);
        return this;
    }
    
    /**
     * Optimistically validates that the value is equal to another Object
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isEqualTo(Object other) throws UncheckedValidationException  {
        if (!Objects.equals(_value, other)) invalid("%s must be equal to '%s'", _name, other);
        return this;
    }

    /**
     * Optimistically validates that the specified condition is false
     * @param condition the condition to test
     * @param fmt the exception message format string to use if validation fails
     * @param args the exception message format arguments to use if validation fails
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isFalse(boolean condition, String fmt, Object... args) throws UncheckedValidationException  {
        if (condition) invalid(fmt, args);
        return this;
    }
    
    /**
     * Optimistically validates that the specified condition is false
     * @param condition the condition to test
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isFalse(boolean condition) throws UncheckedValidationException {
        return isFalse(condition, "custom validation logic must evaluate to false");
    }
    
    /**
     * Optimistically validates that the specified Predicate evaluates to false
     * @param p the Predicate to test
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isFalse(Predicate<T> p) throws UncheckedValidationException {
        return isFalse(p.test(_value));
    }
    
    /**
     * Optimistically validates that the specified Predicate evaluates to true
     * @param p the Predicate to test
     * @param fmt the exception message format string to use if validation fails
     * @param args the exception message format arguments to use if validation fails
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isFalse(Predicate<T> p, String fmt, Object... args) throws UncheckedValidationException {
        return isFalse(p.test(_value), fmt, args);
    }

    /**
     * Optimistically validates that the specified condition is true
     * @param condition the condition to test
     * @param fmt the exception message format string to use if validation fails
     * @param args the exception message format arguments to use if validation fails
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isTrue(boolean condition, String fmt, Object... args) throws UncheckedValidationException {
        if (!condition) invalid(fmt, args);
        return this;
    }

    /**
     * Optimistically validates that the specified condition is true
     * @param condition the condition to test
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isTrue(boolean condition) throws UncheckedValidationException {
        return isTrue(condition, "custom validation logic must evaluate to true");
    }

    /**
     * Optimistically validates that the specified Predicate evaluates to true
     * @param p the Predicate to test
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isTrue(Predicate<T> p) throws UncheckedValidationException {
        return isTrue(p.test(_value));
    }
    
    /**
     * Optimistically validates that the specified Predicate evaluates to true
     * @param p the Predicate to test
     * @param fmt the exception message format string to use if validation fails
     * @param args the exception message format arguments to use if validation fails
     * @return this optimistic validator
     * @throws UncheckedValidationException if validation fails
     */
    public Hope<T> isTrue(Predicate<T> p, String fmt, Object... args) throws UncheckedValidationException {
        return isTrue(p.test(_value), fmt, args);
    }
    
    protected void invalid(String fmt, Object... args) throws UncheckedValidationException {
        throw new UncheckedValidationException(String.format(fmt, args));
    }
}
