package app.core.annotations;

import spark.route.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action {
    String path();

    HttpMethod method() default HttpMethod.get;

    String acceptTypes() default "*/*";

    String permission() default "";

    boolean checkPermission() default true;
}
