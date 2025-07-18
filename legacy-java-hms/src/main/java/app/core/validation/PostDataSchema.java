package app.core.validation;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public final class PostDataSchema {
    private final String name;
    private final Set<PostDataField> dataFields;

    PostDataSchema(DataValidator validator, Class<? extends Validatable> klazz)
            throws app.core.validation.DataValidator.FilterNotFoundException, DataValidator.ValidationException {
        this.name = klazz.getCanonicalName();
        this.dataFields = new LinkedHashSet<>();
        this.loadPostDataFields(validator, klazz);
    }

    private void loadPostDataFields(DataValidator validator, Class<? extends Validatable> klazz)
            throws app.core.validation.DataValidator.FilterNotFoundException, DataValidator.ValidationException {
        Set<Field> fieldSet = new LinkedHashSet<>();

        populateFields(fieldSet, klazz);

        for (Field field : fieldSet) {
            ValidationChain chain = field.getAnnotation(ValidationChain.class);
            if (chain != null) {
                dataFields.add(new PostDataField(validator, field, chain));
            }
        }

        if (dataFields.isEmpty()) {
            throw new DataValidator.ValidationException(name + " did not specify fields to be validated. " +
                    "Annotate fields to be validated with " + ValidationChain.class);
        }
    }

    public String getName() {
        return name;
    }

    public Set<PostDataField> getDataFields() {
        return dataFields;
    }

    private void populateFields(Set<Field> fieldSet, Class klazz) {
        if (Validatable.class.isAssignableFrom(klazz.getSuperclass())) {
            populateFields(fieldSet, klazz.getSuperclass());
        }
        fieldSet.addAll(Arrays.asList(klazz.getDeclaredFields()));
    }
}
