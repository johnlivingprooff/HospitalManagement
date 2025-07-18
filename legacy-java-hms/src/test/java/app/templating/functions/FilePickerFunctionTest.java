package app.templating.functions;

import org.junit.Test;

import static org.junit.Assert.*;

public class FilePickerFunctionTest {
    @Test
    public void testBuilder() {
        FilePickerFunction fpf = new FilePickerFunction();
        System.out.println(fpf.build(true, "custom-class", "image/png", "upload"));
    }
}