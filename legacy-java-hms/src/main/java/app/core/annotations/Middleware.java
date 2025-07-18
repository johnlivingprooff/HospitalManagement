package app.core.annotations;

import org.atteo.classindex.IndexAnnotated;
import spark.route.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IndexAnnotated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Middleware {
    String path();

    String acceptTypes() default "*/*";

    HttpMethod method() default HttpMethod.get;
}

