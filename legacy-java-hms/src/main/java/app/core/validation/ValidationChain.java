package app.core.validation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValidationChain {
    /**
     * @return List of validation filters to be used for validation. Validators are called in order of occurrence
     */
    FilterNode[] filters();

    /**
     * @return Field name. If empty, will be derived from field name
     */
    String fieldName() default "";

    /**
     * @return Human friendly name used when displaying validation errors.
     * If empty, will be derived from {@link #fieldName()}
     */
    String label() default "";

    /**
     * @return Whether or not this post field is an array (i,e checkbox values)
     */
    boolean array() default false;

    /**
     * @return Validation stage to trigger the validation of this field
     */
    ValidationStage stage() default ValidationStage.All;
}
