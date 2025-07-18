package app.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReflectionUtilTest {

    @org.junit.Test
    public void isNullOrEmpty() {
        String[] stringArray = {"1"};
        Object[] emptyArray = {};
        Object[] nullArray = null;

        String testString = "Hello world";
        String nullObject = null;

        assertFalse(ReflectionUtil.isNullOrEmpty(stringArray));
        assertFalse(ReflectionUtil.isNullOrEmpty(testString));

        assertTrue(ReflectionUtil.isNullOrEmpty(emptyArray));
        assertTrue(ReflectionUtil.isNullOrEmpty(nullArray));
        assertTrue(ReflectionUtil.isNullOrEmpty(nullObject));
    }

    @Test
    public void isArrayLengthWithingRange() {
        int min = 1, max = 5;

        char[] chars = {1, 2, 3, 4, 5};
        int[] ints = {1, 2};

        assertTrue(ReflectionUtil.isArrayLengthWithinRange(chars, min, max));
        assertTrue(ReflectionUtil.isArrayLengthWithinRange(ints, min, max));
    }
}