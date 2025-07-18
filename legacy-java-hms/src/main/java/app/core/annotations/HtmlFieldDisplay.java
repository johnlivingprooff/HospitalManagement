package app.core.annotations;

import java.lang.annotation.*;

/**
 * <p>This annotation specifies which field or method will be used to render the text and value of
 * the annotated object</p>
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HtmlFieldDisplay {
    /**
     * <p>The name of the field or method whose value should be rendered</p>
     * <p></p>
     * <p>If method, append parentheses to the name. For instance, if the name of the object is
     * "toString", the variable name will be "toString" and its method counterpart "toString()" </p>
     *
     * @return .
     */
    String value();

    /**
     * <p>The name of the field or method (ending with '()') whose (return) value shall be used as the display text</p>
     *
     * @return .
     */
    String label() default "toString()";

    /**
     * <p>Name of method to get extra data that will be injected inside the attribute "data-extra"</p>
     * <p>Make sure HTML and quote marks are escaped</p>
     *
     * @return .
     */
    String extraDataMethod() default "";
}