package app.services.scheduling;

import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IndexAnnotated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Task {
    /**
     * @return Tame of this task
     */
    String name();

    /**
     * @return The task group this task belongs to
     */
    String group();

    /**
     * <p>Cron pattern. Visit link below to get more information</p>
     * <a href="https://www.freeformatter.com/cron-expression-generator-quartz.html">
     * https://www.freeformatter.com/cron-expression-generator-quartz.html</a>
     *
     * @return .
     */
    String cron();

    String description() default "";
}