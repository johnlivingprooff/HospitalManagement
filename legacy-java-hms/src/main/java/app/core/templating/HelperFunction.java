package app.core.templating;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HelperFunction<T> {
    Logger logger = LoggerFactory.getLogger(HelperFunction.class);

    String name();

    Object execute(T parameters);
}
