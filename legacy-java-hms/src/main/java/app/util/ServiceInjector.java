package app.util;

import app.core.Service;
import app.core.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

public class ServiceInjector {
    private static final Logger log = LoggerFactory.getLogger(ServiceInjector.class);

    public interface ServiceLookup {
        <T extends Service> T getService(Class<T> tClass);
    }

    /**
     * Collects all fields in a class' hierarchy
     *
     * @param clazz Class to inspect
     * @return .
     */
    private static Set<Field> collectFields(Class<?> clazz) {
        Class<?> root;
        Field[] fields;
        Set<Field> fieldSet;

        fieldSet = new LinkedHashSet<>();
        root = clazz;
        do {
            fields = root.getDeclaredFields();
            if (fields.length > 0) {
                for (Field field : fields) {
                    if (app.core.Service.class.isAssignableFrom(field.getType()) && field.isAnnotationPresent(Inject.class)) {
                        fieldSet.add(field);
                    }
                }
            }
        } while ((root = root.getSuperclass()) != Object.class);
        return fieldSet;
    }

    public static void inject(Object object, ServiceLookup serviceLookup) throws Exception {
        Set<Field> fieldSet;
        fieldSet = collectFields(object.getClass());

        if (!fieldSet.isEmpty()) {
            for (Field field : fieldSet) {
                if (field.trySetAccessible()) {
                    app.core.Service service;

                    service = serviceLookup.getService(field.getType().asSubclass(app.core.Service.class));
                    if (service != null) {
                        field.set(object, service);
                    } else {
                        log.warn("Failure injecting service {} into field {}: Service not found.",
                                field.getType(), field.getName());
                    }
                } else {
                    log.warn("Failure injecting service {} into field {}.", field.getType(), field.getName());
                }
            }
        }
    }
}
