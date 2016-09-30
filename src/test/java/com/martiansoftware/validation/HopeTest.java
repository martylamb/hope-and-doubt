package com.martiansoftware.validation;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class HopeTest {
    
    @Test
    public void testGoodHopes() {
        assertEquals("x", Hope.that("x").isNotNullOrEmpty().value());
    }
    
}
