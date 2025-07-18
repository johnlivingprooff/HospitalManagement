package app.util;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class LocaleUtilTest {

    @Test
    public void ucString() {
        String input = " hello world ";
        String input1 = " Hello World ";
        String input2 = "good bye cruel world ";
        String input3 = "HelloWorld ";

        String expected = "HelloWorld";
        String expected2 = "GoodByeCruelWorld";

        assertEquals(expected, LocaleUtil.ucString(input));
        assertEquals(expected, LocaleUtil.ucString(input1));
        assertEquals(expected2, LocaleUtil.ucString(input2));
        assertEquals(expected, LocaleUtil.ucString(input3));
    }

    @Test
    public void isAlphaNumericText() {
        assertTrue(LocaleUtil.isAlphaNumericText("Hell0 W0RD7"));
    }

    @Test
    public void convert() throws Exception {
        assertEquals(123.43f, LocaleUtil.convert("123.43", Float.class), 2);
        assertEquals(1023, LocaleUtil.convert("1023", Integer.class).intValue());
        assertEquals((byte) 123, LocaleUtil.convert("123", Byte.class).byteValue());
    }

    @Test
    public void isDateAfter() {
        Date today = new Date();
        Date end = new Date(69, 1, 1);

        assertTrue(LocaleUtil.isDateAfter(today, end));
        assertFalse(LocaleUtil.isDateAfter(today, today));
    }
}