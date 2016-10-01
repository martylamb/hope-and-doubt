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

/**
 * A simple optimistic validator that throws <code>UncheckedValidationException</code>s
 * if validation fails.
 * 
 * @author <a href="http://martylamb.com">Marty Lamb</a>
 */
public class Hope<T> extends Validator<T, UncheckedValidationException> {

    private Hope(T t) {
        super(t, UncheckedValidationException::new);
    }
    
    public static <T> Hope<T> that(T t) {
        return new Hope(t);
    }
}
