package app.core.validation;

import app.util.LocaleUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PostDataField {
    private String label;
    private final String fieldName;
    private final boolean isArray;
    private final ValidationStage stage;
    private final Set<FilterImpl> filterSet;
    private final Field field;

    PostDataField(DataValidator validator, Field field, ValidationChain chain) throws DataValidator.FilterNotFoundException, DataValidator.ValidationException {
        this.field = field;
        this.fieldName = LocaleUtil.isNullOrEmpty(chain.fieldName()) ? field.getName() : chain.fieldName();
        this.label = LocaleUtil.isNullOrEmpty(chain.label()) ? fieldName : chain.label();
        this.filterSet = new LinkedHashSet<>();
        this.stage = chain.stage();
        this.isArray = chain.array();
        for (FilterNode node : chain.filters()) {
            DataValidator.ValidationFilter filter;
            filter = validator.getValidationFilter(node.value());
            if (filter == null) {
                throw new DataValidator.FilterNotFoundException(getFilterName(node));
            }
            filterSet.add(new FilterImpl(node.parameters(), filter));
        }
        if (filterSet.isEmpty()) {
            throw new DataValidator.ValidationException(fieldName + " did not specify any validation filters");
        }
    }

    public boolean isArray() {
        return isArray;
    }

    public String getFieldName() {
        return fieldName;
    }

    public final ValidationStage getStage() {
        return stage;
    }

    public final Object validate(Object input) throws DataValidator.ValidationException {
        for (FilterImpl filter : filterSet) {
            input = filter.apply(label, input, null);
        }
        return input;
    }

    private String getFilterName(FilterNode filterNode) {
        return filterNode.value().getAnnotation(Validator.class).name();
    }

    private class FilterImpl implements DataValidator.ValidationFilter<Object> {
        private final List<String> arguments;
        private final DataValidator.ValidationFilter delegate;

        FilterImpl(String[] args, DataValidator.ValidationFilter delegate) {
            this.arguments = Arrays.asList(args);
            this.delegate = delegate;
        }

        @Override
        public Object apply(String name, Object input, List<String> args) throws DataValidator.ValidationException {
            // noinspection unchecked
            return delegate.apply(name, input, arguments);
        }
    }

    public boolean stageMatches(ValidationStage stage) {
        if (this.stage == ValidationStage.All) return true;
        return this.stage == stage;
    }

    public void setInstance(Object instance, Object value) throws IllegalAccessException {
        if (this.field.trySetAccessible()) {
            this.field.set(instance, value);
        }
    }
}
