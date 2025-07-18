package app.templating.functions;

import app.core.annotations.HtmlFieldDisplay;
import app.core.templating.HelperFunction;
import app.util.LocaleUtil;
import app.util.ReflectionUtil;
import org.jtwig.environment.Environment;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.value.Undefined;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class HelperFunctionImpl implements HelperFunction<FunctionRequest> {
    private String name;

    HelperFunctionImpl(String name) {
        this.name = name;
    }

    <T> T getArgument(List<Object> args, int index, Class<T> type, T defaultValue) {
        if (args.size() >= (index + 1)) {
            Object value = args.get(index);
            if (value != null && !(value instanceof Undefined)) {
                return type.cast(value);
            }
        }
        return defaultValue;
    }

    boolean isUndefinedOrNull(Object o) {
        return o == null || o instanceof Undefined;
    }

    boolean itemInList(Object item, Object list) throws Exception {
        if (list != null && !(list instanceof Undefined)) {
            if (list instanceof Map) {
                // use key if map
                for (Object key : ((Map) list).keySet()) {
                    if (valueEquals(item, key)) {
                        return true;
                    }
                }
            } else if (list instanceof Iterable) {
                for (Object listItem : (Iterable) list) {
                    if (valueEquals(item, getItemValue(listItem))) {
                        return true;
                    }
                }
            } else if (ReflectionUtil.isArray(list)) {
                for (int i = 0; i < Array.getLength(list); i++) {
                    final Object arrayItem = Array.get(list, i);
                    if (valueEquals(item, getItemValue(arrayItem))) {
                        return true;
                    }
                }
            } else {
                throw new IllegalArgumentException("Selection list must be iterable or an array");
            }
        }
        return false;
    }

    private static Object getItemValue(Object object) throws Exception {
        final HtmlFieldDisplay fieldDisplay = object.getClass().getAnnotation(HtmlFieldDisplay.class);
        if (fieldDisplay == null) {
            throw new IllegalArgumentException(HtmlFieldDisplay.class + " annotation missing in " + object.getClass());
        }
        return getFieldValue(object, fieldDisplay);
    }

    protected String escape(String value, Environment environment) {
        return environment.getEscapeEnvironment().getInitialEscapeEngine().escape(value);
    }

    static String getFieldExtraValue(Object o, HtmlFieldDisplay display) throws Exception {
        if (!LocaleUtil.isNullOrEmpty(display.extraDataMethod())) {
            return String.valueOf(o.getClass().getMethod(display.extraDataMethod()).invoke(o));
        }
        return "'";
    }

    @Override
    public String name() {
        return name;
    }

    static Object getFieldValue(Object o, HtmlFieldDisplay display) throws Exception {
        if (display.value().endsWith("()")) {
            // call a method to return the value
            String method = display.value().substring(0, display.value().length() - 2);
            return o.getClass().getMethod(method).invoke(o);
        }
        return o.getClass().getField(display.value()).get(o);
    }

    static String getDisplayText(Object object, HtmlFieldDisplay display) throws Exception {
        if (LocaleUtil.isNullOrEmpty(display.label())) {
            // default to .toString()
            return object.toString();
        }
        // Does the display text come from a method call?
        if (display.label().endsWith("()")) {
            String methodName = display.label();
            return String.valueOf(object.getClass()
                    .getMethod(methodName.substring(0, methodName.length() - 2)).invoke(object));
        }
        // Display text comes from a field
        return String.valueOf(getObjectField(object, display.label()).get(object));
    }

    static boolean valueEquals(Object value, Object selected) {
        return value == selected
                || Objects.equals(value, selected)
                || value.toString().equalsIgnoreCase(String.valueOf(selected));
    }

    static Field getObjectField(Object o, String fieldName) throws NoSuchFieldException {
        try {
            return o.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException nsfe) {
            try {
                return o.getClass().getField(fieldName);
            } catch (NoSuchFieldException ignore) {

            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
