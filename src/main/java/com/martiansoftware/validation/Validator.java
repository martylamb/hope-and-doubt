package com.martiansoftware.validation;

//   Copyright 2016 Martian Software, Inc.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A validator that is flexible in the type of exceptions it throws.  A new
 * Validator should be created for each object being validated by supplying
 * its constructor with the value to validate and a factory method for creating
 * validation exceptions.
 * 
 * The Hope and Doubt classes provide static factories for creating Validators
 * that throw UncheckedValidationExceptions and CheckedValidationExceptions,
 * respectively.
 * 
 * @author <a href="http://martylamb.com">Marty Lamb</a>
 * @param <T> the type of value to validate
 * @param <E> the type of exception to throw if validation fails
 */
public class Validator <T, E extends Exception> {

    /**
     * the value that is being validated
     */
    private T _value;
    
    /**
     * creates any exceptions that are thrown
     */
    private final Function<String, E> _exceptionFactory;
    
    /**
     * an optional name for the value being validated (only used in exception messages)
     */
    private volatile String _name = "value";
    
    /**
     * Creates a Validator for the specified value that will throw
     * exceptions of type E if any errors occur
     * 
     * @param value the value being validated
     * @param exceptionFactory creates any exceptions that are thrown
     */
    public Validator(T value, Function<String, E> exceptionFactory) {
        _value = value;
        _exceptionFactory = exceptionFactory;
    }

    /**
     * Assigns an (optional) name to the value being validated.  This is useful
     * if validating multiple method parameters (for example) so that any exceptions
     * refer to the parameter by name
     * 
     * @param name the name of the value being validated
     * @return this validator
     */
    public Validator <T, E> named(String name) {
        _name = name;
        return this;
    }
    
    /**
     * Returns the value being validated
     * @return the value being validated
     */
    public T value() { return _value; }
    
    /**
     * Validates that the value is not null.
     * @return this validator
     * @throws E if validation fails
     */
    public Validator <T, E> isNotNull() throws E {
        if (_value == null) invalid(String.format("%s must not be null", _name));
        return this;
    }

    /**
     * Validates that the value is null.  Why is this necessary?
     * It's not, really.  Just included for symmetry.
     * 
     * @return this validator
     * @throws E if validation fails
     */
    public Validator <T, E> isNull() throws E {
        if (_value != null) invalid("%s must be null", _name);
        return this;
    }

    // checks that the value is optional and is present
    private boolean _isPresent() throws E  {
        isNotNull();
        if (_value instanceof Optional) return ((Optional) _value).isPresent();
        else invalid("%s is not an Optional; check for isPresent() is not valid.", _name);
        return false; // not reachable but needed to satisfy compiler
    }
    
    /**
     * Validates that the value is present (must be an Optional)
     * @return this validator
     * @throws E if validation fails
     */
    public Validator <T, E> isPresent() throws E {
        if (!_isPresent()) invalid("%s must be present", _name);
        return this;
    }

    /**
     * Validates that the value is not present (must be an Optional).
     * Why is this necessary?  It's not, really.  Just included for symmetry.
     * @return this validator
     * @throws E if validation fails
     */
    public Validator <T, E> isNotPresent() throws E {
        if (_isPresent()) invalid("%s must not be present", _name);
        return this;
    }    
    
    /**
     * Helper for checking if objects are "empty", used to probe for multiple
     * potential no-arg indicators of emptiness.
     */
    private class EmptinessCheck {
        private final String _methodName;
        private final Class _returnType;
        private final Object _emptinessIndicator;
        
        private EmptinessCheck(String methodName, Class returnType, Object emptinessIndicator) {
            _methodName = methodName;
            _returnType = returnType;
            _emptinessIndicator = emptinessIndicator;
        }
        
        // tries to find and call the named method with the specified return type.
        // if found and invoked, the return value is checked against the emptinessIndicator.
        // if equal, then the value is considered "empty" and the exception is thrown.
        // returns a boolean indicating whether the method was found and emptiness
        // was checked.
        public boolean invoke() throws E {
            try {
                Method m = _value.getClass().getMethod(_methodName);
                if (m.getReturnType().equals(_returnType)) {
                    Object ret = m.invoke(_value);
                    if (_emptinessIndicator.equals(ret)) throwEmpty();
                    return true; // found and invoked a tester so return true regardless of result
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException notAvailable) {}
            return false;
        }
        
        @Override
        public String toString() {
            return String.format("%s %s()", _returnType.getName(), _methodName);
        }
    }
    
    private void throwEmpty() throws E {
        invalid("%s must not be empty", _name);        
    }
    
    /**
     * Validates that the value is not null or empty.  Emptiness
     * is determined as follows:
     * <ul>
     * <li>A zero-length array is empty.</li>
     * <li>If the object has a boolean isEmpty() method that returns true 
     * (e.g., a Collection or a String), it is empty.</li>
     * <li>If the object has a boolean isPresent() method that returns true
     * (e.g., an Optional), it is empty.</li>
     * <li>If the object has an int size() or long size() method that returns 0,
     * it is empty.</li>
     * <li>If the object has an int length() or long length() method that returns 0,
     * it is empty.</li>
     * <li>
     * </ul>
     * 
     * If more than one of the above method signatures is found, only the first
     * one is invoked and its answer is considered authoritative with respect
     * to "emptiness"
     * 
     * @return this validator
     * @throws E if validation fails
     */
    public Validator <T, E> isNotNullOrEmpty() throws E  {
        isNotNull();
        if (_value.getClass().isArray()) {
            if (((Object[]) _value).length == 0) throwEmpty();
        } else {
            List<EmptinessCheck> checks = new java.util.ArrayList<>(6);
            checks.add(new EmptinessCheck("isEmpty", Boolean.TYPE, true));
            checks.add(new EmptinessCheck("isPresent", Boolean.TYPE, true));
            checks.add(new EmptinessCheck("size", Integer.TYPE, 0));
            checks.add(new EmptinessCheck("size", Long.TYPE, 0l));
            checks.add(new EmptinessCheck("length", Integer.TYPE, 0));
            checks.add(new EmptinessCheck("length", Long.TYPE, 0l));
            for (EmptinessCheck ec : checks) {
                if (ec.invoke()) return this;
            }
            invalid("class %s does not provide any emptiness checkers matching any of: %s",
                    _value.getClass().getName(),
                    checks.stream().map(c -> c.toString()).collect(Collectors.joining(", "))
            );
        }
        return this;
    }

    /**
     * Validates that the value is not equal to another Object.
     * @param other the value to which this validator's value should be compared
     * @return this validator
     * @throws E if validation fails
     */
    public Validator <T, E> isNotEqualTo(T other) throws E  {
        if (Objects.equals(_value, other)) invalid("%s must not be equal to '%s'", _name, other);
        return this;
    }
    
    /**
     * Validates that the value is equal to another Object.
     * @param other the value to which this validator's value should be compared
     * @return this validator
     * @throws E if validation fails
     */
    public Validator <T, E> isEqualTo(T other) throws E  {
        if (!Objects.equals(_value, other)) invalid("%s must be equal to '%s'", _name, other);
        return this;
    }

    /**
     * Validates that the specified condition is false
     * @param test the condition to test
     * @param errMessageFormat the exception message format string to use if validation fails
     * @param errMessageArgs the (optional) exception message format arguments to use if validation fails
     * @return validator
     * @throws E if validation fails
     */
    public Validator <T, E> isFalse(Predicate<T> test, String errMessageFormat, Object... errMessageArgs) throws E  {
        if (test.test(_value)) invalid(errMessageFormat, errMessageArgs);
        return this;
    }
    
    /**
     * Validates that the specified condition is false
     * @param test the condition to test
     * @return this validator
     * @throws E if validation fails
     */
    public Validator <T, E> isFalse(Predicate<T> test) throws E {
        return isFalse(test, "custom validation logic must evaluate to false");
    }
    

    /**
     * Validates that the specified Predicate evaluates to true
     * @param test the condition to test
     * @return this validator
     * @throws E if validation fails
     */
    public Validator <T, E> isTrue(Predicate<T> test) throws E {
        return isTrue(test, "custom validation logic must evaluate to true");
    }
    
    /**
     * Validates that the specified Predicate evaluates to true
     * @param test the Predicate to test
     * @param errMessageFormat the exception message format string to use if validation fails
     * @param errMessageArgs the exception message format arguments to use if validation fails
     * @return this validator
     * @throws E if validation fails
     */
    public Validator <T, E> isTrue(Predicate<T> test, String errMessageFormat, Object... errMessageArgs) throws E {
        if (!test.test(_value)) invalid(errMessageFormat, errMessageArgs);
        return this;
    }
    
    /**
     * Validates that the result of the value's toString() method matches
     * the specified regex
     * @param regex the regex to compare to the value's string representation
     * @return this validator
     * @throws E if validation fails
     */
    public Validator <T, E> matches(String regex) throws E {
        return matchesAny(regex);
    }
    
    /**
     * Validates that the result of the value's toString() method matches
     * at least one of the specified regexes
     * @param regexes the regexes to compare to the value's string representation
     * @return this validator
     * @throws E if validation fails
     */
    public Validator <T, E> matchesAny(String... regexes) throws E {
        isNotNull();
        if (regexes.length == 0) invalid("no patterns supplied");
        String s = _value.toString();
        for (String regex : regexes) {
            if (s.matches(regex)) return this;
        }
        invalid("%s must match at least one of the following regular expressions: %s",
                _name,
                Stream.of(regexes).map(r -> '"' + r + '"').collect(Collectors.joining(", ")));
        return this;
    }
    
    /**
     * Replaces this validator's value with the specified default value if this
     * validator's current value is null
     * @param defaultValue the default value to use if the current value is null
     * @return this validator
     */
    public Validator <T, E> orElse(T defaultValue) {
        if (_value == null) _value = defaultValue;
        return this;
    }
    
    /**
     * Transforms this validator into a validator of a different type by
     * transforming the value being validated
     * @param <U> the type to which the value is being mapped
     * @param mapper the mapping function
     * @return a new validator of the mapped value
     */
    public <U> Validator<U, E> map(Function<T, U> mapper) {
        return new Validator<>(mapper.apply(_value), _exceptionFactory).named(_name);
    }
    
    // creates and throws exceptions
    protected void invalid(String fmt, Object... args) throws E {
        throw _exceptionFactory.apply(String.format(fmt, args));
    }
    
}
