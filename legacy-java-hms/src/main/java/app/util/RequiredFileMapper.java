package app.util;

import lib.gintec_rdl.jini.extenstion.MapException;
import lib.gintec_rdl.jini.extenstion.TypeMapper;

import java.io.File;
import java.lang.reflect.Field;

public class RequiredFileMapper implements TypeMapper {
    @Override
    public void map(Object instance, Field field, String value) throws MapException {
        try {
            final String className = ReflectionUtil.getClassname(field.getType());
            if ("java.io.File".equalsIgnoreCase(className)) {
                final File file = new File(value);
                if (!file.exists()) {
                    throw new Exception("Failed to find file: " + file.getAbsolutePath());
                }
                field.set(instance, file);
            }
        } catch (Exception e) {
            throw new MapException(e);
        }
    }
}
