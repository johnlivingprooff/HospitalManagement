package app.core.validation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
public @interface FilterNode {
    Class<? extends DataValidator.ValidationFilter> value();

    String[] parameters() default {};
}
