package app.util;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class DateUtilsTest {

    @Test
    public void duration() {
        LocalDateTime start, end;

        start = LocalDateTime.of(1970, 1, 1, 0, 1, 0);
        end = LocalDateTime.now();

        System.out.println(DateUtils.duration(start, end));
    }
}