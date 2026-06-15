package com.floatty;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;



public class FloattyTest {
    @Test
    public void testFloat() {
        float frac = 1/3;
        float a = 3.0f + frac;
        float b = 3.0f + (1 - frac);

        float result = a + b;
        assertEquals(7, result, 0.001f);
    }

    @Test
    public void testDouble() {
        float frac = 1/127;
        float a = 3.0f + frac;
        float b = 3.0f + (1 - frac);

        float result = a + b;
        assertEquals(7, result, 0.001f);
    }
}