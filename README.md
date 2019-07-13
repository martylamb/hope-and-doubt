hope and doubt
=============

*Hope and doubt* is a tiny library aimed at simplifying validation of arguments to methods, especially constructors and setters/getters.  It has no external dependencies and is under 9 KB at the time of this writing.
It provides both *optimistic* and *pessimistic* validators to suit your needs.  The differences are:
  * *Optimistic* validators assume that the input is valid and throw unchecked exceptions if the input is invalid.  Optimistic validators are created via `Hope.that()`
  * *Pessimistic* validators assume that the input is invalid and throw checked exceptions if this turns out to be the case.  Pessimistic validators are created via `Doubt.that()`

get via maven
--------------------
```xml
<dependency>
    <groupId>com.martiansoftware</groupId>
    <artifactId>hope-and-doubt</artifactId>
    <version>0.1.0</version>
</dependency>
```
quick examples
----------------------

### Optimistically ensure that the "name" parameter is neither null nor an empty String:
```java
public void setName(String name) {
    String validatedName = Hope.that(name).isNotNullOrEmpty().value();
    ...
}
```

If `name` is null, this will fail with:
>Exception in thread "main" com.martiansoftware.validation.UncheckedValidationException: value must not be null

If `name` is an empty String, it will fail with
>Exception in thread "main" com.martiansoftware.validation.UncheckedValidationException: value must not be empty

### Pessimistically ensure that the "name" parameter is neither null nor an empty String:
```java
public void setName(String name) throws CheckedValidationException {
    String validatedName = Doubt.that(name).isNotNullOrEmpty().value();
    ...
}
```

If `name` is null, this will fail with:
>Exception in thread "main" com.martiansoftware.validation.CheckedValidationException: value must not be null

If `name` is an empty String, it will fail with
>Exception in thread "main" com.martiansoftware.validation.CheckedValidationException: value must not be empty

### Naming the validated Object for better error messages
```java
public void setName(String name) {
    String validatedName = Hope.that(name).named("a person's name").isNotNullOrEmpty().value();
    ...
}
```

If `name` is null, this will fail with:
>Exception in thread "main" com.martiansoftware.validation.UncheckedValidationException: a person's name must not be null


### Providing a default value
```java
public void setName(String name) {
    String validatedName = Hope.that(name).orElse("Anonymous").isNotNullOrEmpty().value();
    ...
}
```

### Collections, arrays, and other things that can be empty
```java
public void setFoo(List<Foo> fooList, Bar[] barArray) {
    List<Foo> myFoos = Hope.that(fooList).isNotNullOrEmpty().value();
    Bar[] myBars = Hope.that(barArray).isNotNullOrEmpty().value();
    ...
}
```

### Custom validation logic via Predicates
```java
public void setName(String name) {
    String validatedName = Hope.that(name)
                               .isNotNullOrEmpty()
                               .isFalse(n -> n.equalsIgnoreCase("anonymous"))
                               .value();
    ...
}
```

### Custom validation logic via Predicates with custom error messages
```java
public void setAge(Integer age) {
    String validatedAge = Hope.that(name)
                               .isNotNullOrEmpty()
                               .isFalse(n -> n < 0,
                                       "negative ages are not allowed")
                               .value();
    ...
}
```

### Matching regular expressions
```java
public void setName(String name) {
    String validatedName = Hope.that(name)
                               .orElse("Mrs. Jane Doe")
                               .isNotNullOrEmpty()
                               .matchesAny("^Mrs?\\. .*", // accepts "Mr. or "Mrs."
                                           "^Dr\\. .*")   // accepts "Dr."
                               .value();
    ...
}
```

### Mapping inputs to different objects
```java
public void setBaz(String baz) {
    int i = Hope.that(baz).isNotNullOrEmpty().map(Integer::valueOf).value();
    ...
}
```


### What else does it do?

[Take a look at the source code for `Validator.java`](src/main/java/com/martiansoftware/validation/Validator.java)
