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
 
    public void testLongMap() {
        assertEquals((Long) 12l, Hope.that(12).named("twelve").map(Long::valueOf).value());
    }
}
