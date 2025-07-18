package app.core.validation;

import app.util.LocaleUtil;
import org.atteo.classindex.ClassIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for validating POST data
 */
public final class DataValidator {
    /**
     * <p>Filter lookup table</p>
     * <p>This table contains instances of filters that can be used to validate POST data</p>
     */
    private final Map<String, ValidationFilter> filterMap;
    private static final Logger logger = LoggerFactory.getLogger(DataValidator.class);
    private final Map<String, PostDataSchema> dataSchemaMap;

    /**
     * Regex pattern for extracting filter name and parameters
     */
    private static final Pattern EXTRACTION_PATTERN = Pattern.compile("^([aA-zZ]+)(\\[([^\\[\\]]+)])?$");

    /**
     * <p>Validation schema. Contains lookup name and filter list</p>
     *
     * @see #filter
     */
    public static class ValidationSchema {

        /**
         * Name of field.
         */
        public String fieldName;

        /**
         * <p>Contains filter names separated by pipe symbol ('|'). Each filter name references a filter instance
         * and filters are applied in order of occurrence rather precedence</p>
         * <p>For example:</p>
         * <pre>
         *     String filter = "trim|required|length[1,23]";
         * </pre>
         * <p>Translates to</p>
         * <pre>
         *     String input = "some input";
         *
         *     // trim
         *     input = trim(input);
         *
         *     // required
         *     if(isNullOrEmpty(input)){
         *         throw new ValidationException("Field '$foo' is required");
         *     }
         *
         *     // length
         *     if(lengthWithinRage(input, 1, 23)){
         *         throw new ValidationException("Length of field '$foo' must be between X and Y");
         *     }
         * </pre>
         */
        public String filter;

        /**
         * Human readable text to display instead of {@link #fieldName}
         */
        public String label;

        /**
         * If the expected field is an array
         */
        public boolean isArray;
    }

    /**
     * Validation context contains validation schema and a reference table to all predefined filters
     */
    public final static class ValidationContext {
        private final String fieldName;
        private final boolean isArray;
        private final Set<ValidationFilterCallInfo> filterMap;
        private final String label;

        public ValidationContext(String name, String label, boolean isArray, Set<ValidationFilterCallInfo> filterMap) {
            this.fieldName = name;
            this.isArray = isArray;
            this.filterMap = filterMap;
            this.label = label;
        }

        public Set<ValidationFilterCallInfo> getFilterMap() {
            return filterMap;
        }

        /**
         * Returns the name of the field
         *
         * @return
         */
        public String getFieldName() {
            return fieldName;
        }

        public String getLabel() {
            return label;
        }

        public boolean isArray() {
            return isArray;
        }

        @Override
        public String toString() {
            return fieldName;
        }
    }

    public DataValidator() throws Exception {
        Iterable<Class<?>> classes;

        classes = ClassIndex.getAnnotated(Validator.class);

        filterMap = new LinkedHashMap<>();
        dataSchemaMap = new ConcurrentHashMap<>();

        for (final Class<?> clazz : classes) {
            if (!ValidationFilter.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(clazz + " must implement " + ValidationFilter.class);
            }
            ValidationFilter<?> validationFilter = createValidator(clazz);
            Validator validator = validationFilter.getClass().getAnnotation(Validator.class);
            if (logger.isInfoEnabled()) {
                logger.info("Found validation filter {}({})", validator.name(), clazz.getCanonicalName());
            }
            filterMap.put(validator.name(), validationFilter);
        }
    }

    public <T extends Validatable> PostDataSchema getPostDataSchema(Class<T> tClass)
            throws FilterNotFoundException, ValidationException {
        PostDataSchema schema;
        schema = dataSchemaMap.get(tClass.getCanonicalName());
        if (schema == null) {
            schema = new PostDataSchema(this, tClass);
            dataSchemaMap.put(schema.getName(), schema);
        }
        return schema;
    }

    ValidationFilter getValidationFilter(Class<? extends ValidationFilter> clazz) {
        return filterMap.get(clazz.getAnnotation(Validator.class).name());
    }

    private ValidationFilter<?> createValidator(Class<?> clazz) throws Exception {
        return (ValidationFilter) clazz.getConstructor().newInstance();
    }

    /**
     * <p>Apply filters on the given input</p>
     *
     * @param name      Name of field. Required for error message generation
     * @param input     Input value to be filtered/transformed
     * @param filterSet Filter function set.
     * @return Transformed value.
     * @throws ValidationException .
     */
    public Object applyFilters(String name, Object input, Set<ValidationFilterCallInfo> filterSet) throws ValidationException {
        Object value = input;
        for (ValidationFilterCallInfo callInfo : filterSet) {
            value = callInfo.validationFilter.apply(name, value, callInfo.arguments);
        }
        return value;
    }

    /**
     * <p>Create validation context based on given schema</p>
     *
     * @param schemas List of schemas to create the contexts from
     * @return Set of of validation contexts for each schema
     */
    public Set<ValidationContext> createValidationContexts(ValidationSchema[] schemas) throws IllegalArgumentException, FilterNotFoundException {
        final Set<ValidationContext> contextSet = new LinkedHashSet<>();
        for (ValidationSchema schema : schemas) {
            contextSet.add(createValidationContext(schema));
        }
        return contextSet;
    }

    private ValidationContext createValidationContext(ValidationSchema dataSchema)
            throws IllegalArgumentException, FilterNotFoundException {
        Set<String> filters = new LinkedHashSet<>(Arrays.asList(dataSchema.filter.split("\\|")));
        if (filters.isEmpty()) {
            throw new IllegalArgumentException(dataSchema.fieldName + ": Filters cannot be empty");
        }

        // Is required?
        final Set<ValidationFilterCallInfo> validationFilterSet = new TreeSet<>(Comparator.comparingInt(o -> o.precedence));
        int precedence = 0;

        for (String filter : filters) {

            // Check if filter takes extra arguments
            final Matcher matcher = EXTRACTION_PATTERN.matcher(filter);
            final ArrayList<String> parameters = new ArrayList<>();

            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid filter format/name: " + filter
                        + ". Must be <filter-name>[optional,parameters]. If no parameters, do not include brackets");
            }

            // actual filter name
            filter = matcher.group(1);

            final String args = matcher.group(3);

            // parameters
            if (!LocaleUtil.isNullOrEmpty(args)) {
                parameters.addAll(Arrays.asList(args.split(",")));
            }

            if (!filterMap.containsKey(filter)) {
                throw new FilterNotFoundException(filter);
            }

            // point to the filter
            validationFilterSet.add(new ValidationFilterCallInfo(filterMap.get(filter), parameters, precedence++));
        }

        return new ValidationContext(dataSchema.fieldName, dataSchema.label, dataSchema.isArray, validationFilterSet);
    }

    final class ValidationFilterCallInfo {
        final ValidationFilter validationFilter;
        final ArrayList<String> arguments;
        final int precedence;

        ValidationFilterCallInfo(ValidationFilter<?> validationFilter, ArrayList<String> arguments, int precedence) {
            this.validationFilter = validationFilter;
            this.arguments = arguments;
            this.precedence = precedence;
        }

        @Override
        public String toString() {
            return validationFilter.getClass().getCanonicalName();
        }
    }

    /**
     * <p>Validation filters validate and apply transformations to input data</p>
     */
    public interface ValidationFilter<T> {
        /**
         * <p>Apply the filter's transformation</p>
         *
         * @param name  Name of the field being filtered
         * @param input The input data
         * @param args  Extra arguments
         * @return The transformed data/validated data. Some filters will transform input data after validation.
         * @throws ValidationException If the filter could not validate or transform the data
         */
        Object apply(String name, T input, List<String> args) throws ValidationException;
    }

    /**
     * <p>Use this annotation to load a validation schema file</p>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Schema {
        /**
         * @return The location of the schema file to use for validating POST data
         */
        String value();
    }

    /**
     * <p>This exception is thrown when input validation fails</p>
     *
     * @see #applyFilters(String, Object, Set)
     */
    public final static class ValidationException extends Exception {
        public ValidationException(String msg) {
            super(msg);
        }

        public ValidationException(String msg, Exception e) {
            super(msg, e);
        }
    }

    /**
     * <p>This exception is thrown during context creation when a referenced filter cannot be found</p>
     *
     * @see #createValidationContexts(ValidationSchema[])
     */
    public final static class FilterNotFoundException extends Exception {
        FilterNotFoundException(String filter) {
            super("Unknown filter function: " + filter + ". Did you forget to register it?");
        }
    }
}
