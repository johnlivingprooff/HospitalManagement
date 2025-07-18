package app.core;

import app.core.factories.ControllerFactory;
import app.core.factories.MiddlewareFactory;
import app.core.factories.ServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationRunner<T extends Application> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationRunner.class);
    final Class<T> applicationClass;
    Context context;
    ServiceFactory serviceFactory;
    MiddlewareFactory middlewareFactory;
    ControllerFactory controllerFactory;
    ExceptionHandlers<T> exceptionHandlers;

    public ApplicationRunner(Class<T> applicationClass) {
        this.applicationClass = applicationClass;
    }

    public ApplicationRunner<T> context(Context context) {
        this.context = context;
        return this;
    }

    public ApplicationRunner<T> factory(MiddlewareFactory middlewareFactory) {
        this.middlewareFactory = middlewareFactory;
        return this;
    }

    public ApplicationRunner<T> factory(ControllerFactory controllerFactory) {
        this.controllerFactory = controllerFactory;
        return this;
    }

    public ApplicationRunner<T> factory(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
        return this;
    }

    public ExceptionHandlers<T> handlers() {
        if (exceptionHandlers == null) {
            exceptionHandlers = new ExceptionHandlers<>(this);
        }
        return exceptionHandlers;
    }

    public void run() {
        Application application;
        try {
            application = applicationClass.getConstructor().newInstance();
            application.create(this);
            application.run();
        } catch (Exception e) {
            logger.error("Error when creating application", e);
            e.printStackTrace();
            System.exit(0);
        }
    }
}
