package app.util;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class NetUtilsTest {

    @Test
    public void testLoopback() {
        String message = "Address not local";

        assertTrue(message, NetUtils.isLoopbackAddress("localhost"));
        assertTrue(message, NetUtils.isLoopbackAddress("127.0.0.1"));
        assertTrue(message, NetUtils.isLoopbackAddress("0:0:0:0:0:0:0:1"));
    }
}