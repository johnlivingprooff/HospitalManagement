package app.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public interface ReflectionUtil {


    /**
     * Perform an inclusive size range check
     *
     * @param arr Array object
     * @param min Inclusive lower bound value
     * @param max Inclusive upper bound value
     * @return true if the array's length is min <= n <= max
     */
    static boolean isArrayLengthWithinRange(Object arr, int min, int max) {
        return LocaleUtil.withinBounds(Array.getLength(arr), min, max);
    }

    static boolean isNullOrEmpty(Object input) throws IllegalArgumentException {
        if (input == null) return true;
        if (Collection.class.isAssignableFrom(input.getClass())) return ((Collection) input).isEmpty();
        if (input.getClass().isArray()) return Array.getLength(input) == 0;
        if (input.getClass() == String.class) return LocaleUtil.isNullOrEmpty(input.toString());
        // No null
        return false;
        //throw new IllegalArgumentException("Unsupported type " + input.getClass());
    }

    static boolean isCollection(Object input) {
        return input != null && Collection.class.isAssignableFrom(input.getClass());
    }

    static boolean isCollectionSizeWithinRange(Collection c, int min, int max) {
        return LocaleUtil.withinBounds(c.size(), min, max);
    }

    static String getClassname(Class c) {
        String name = c.getName();
        if (name.startsWith("class ")) {
            name = name.substring(7);
        }
        return name.replaceAll(";", "");
    }

    static boolean isArray(Object o) {
        return o != null && o.getClass().isArray();
    }

    static boolean isNonStaticPublicMethod(Method method) {
        final int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers);
    }

    static Object getPropertyValue(Object bean, String property, int searchDepth) {
        int height;
        Class<?> tmp;
        Field field;

        height = 0;
        tmp = bean.getClass();

        while (height < searchDepth && tmp != null && tmp != Object.class) {
            try {
                field = tmp.getDeclaredField(property);
                field.trySetAccessible();
                return field.get(bean);
            } catch (NoSuchFieldException | IllegalAccessException ignore) {
            }
            height++;
            tmp = tmp.getSuperclass();
        }

        throw new RuntimeException("Property " + property + " not found.");
    }
}
