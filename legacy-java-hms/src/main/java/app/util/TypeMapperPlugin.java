package app.util;

import lib.gintec_rdl.jini.extenstion.MapException;
import lib.gintec_rdl.jini.extenstion.TypeMapper;
import org.simplejavamail.api.mailer.config.TransportStrategy;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class TypeMapperPlugin implements TypeMapper {
    @Override
    public void map(Object instance, Field field, String value) throws MapException {
        try {
            final String className = ReflectionUtil.getClassname(field.getType());
            switch (className) {
                case "org.simplejavamail.api.mailer.config.TransportStrategy":
                    field.set(instance, TransportStrategy.valueOf(value));
                    break;
                case "java.io.File":
                    field.set(instance, new File(value));
                    break;
                case "[Ljava.lang.String":
                    String[] parts = value.split(";");
                    Set<String> set = new HashSet<>();
                    for (String part : parts) {
                        String tmp = part.trim();
                        if (!LocaleUtil.isNullOrEmpty(tmp)) {
                            set.add(tmp);
                        }
                    }
                    field.set(instance, set.toArray(new String[0]));
                    break;
                default:
                    throw new MapException(field.getType());

            }
        } catch (Exception e) {
            throw new MapException(e);
        }
    }
}
