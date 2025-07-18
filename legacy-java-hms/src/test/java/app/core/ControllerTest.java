package app.core;

import app.core.annotations.Editable;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class ControllerTest {

    static class TestClass {
        @Editable
        int editableInt;
        int readOnlyInt;
        @Editable
        private String editableString;
        String readOnlyString;

        public TestClass(int editableInt, int readOnlyInt, String editableString, String readOnlyString) {
            this.editableInt = editableInt;
            this.readOnlyInt = readOnlyInt;
            this.editableString = editableString;
            this.readOnlyString = readOnlyString;
        }
    }

    @Before
    public void setUp() {
        BasicConfigurator.configure();
    }

    static class MockController extends Controller {

    }

    @Test
    public void getInstanceFieldValues() {
        Map<String, Object> map;
        TestClass test = new TestClass(1, 69, "Editable", "read ony string");
        MockController mockController = new MockController();

        map = mockController.getInstanceFieldValues(test, 2);

        assert 2 == map.size() : "Size not matching";
        assert test.editableInt == (int) map.get("editableInt") : "Fields do not match";
        assert test.editableString.equals(map.get("editableString")) : "Fields do not match";
    }
}