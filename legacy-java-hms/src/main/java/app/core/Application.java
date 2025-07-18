package app.core;

import app.core.annotations.Action;
import app.core.annotations.Middleware;
import app.core.annotations.RouteController;
import app.core.annotations.ServiceDescriptor;
import app.core.factories.*;
import app.core.validation.DataValidator;
import app.util.ReflectionUtil;
import app.util.ServiceInjector;
import org.atteo.classindex.ClassIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;
import spark.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;

public abstract class Application {
    private Logger logger;
    private Service service;
    private Context context;
    private DataValidator dataValidator;
    private ServiceFactory serviceFactory;
    private ServiceContainer serviceContainer;
    private MiddlewareFactory middlewareFactory;
    private ControllerFactory controllerFactory;
    private InitializationParameters initParams;
    private Class<? extends Application> applicationClass;

    @SuppressWarnings("unused")
    final void create(ApplicationRunner<? extends Application> applicationRunner) throws Exception {
        this.applicationClass = applicationRunner.applicationClass;
        this.logger = LoggerFactory.getLogger(applicationRunner.applicationClass);
        this.middlewareFactory = Optional.ofNullable(applicationRunner.middlewareFactory).orElse(new SimpleMiddlewareFactory(true));
        this.controllerFactory = Optional.ofNullable(applicationRunner.controllerFactory).orElse(new SimpleControllerFactory(true));
        this.serviceFactory = Optional.ofNullable(applicationRunner.serviceFactory).orElse(new SimpleServiceFactory(true));
        this.service = Service.ignite();
        this.context = applicationRunner.context;
        this.serviceContainer = new ServiceContainer();
        this.dataValidator = new DataValidator();
        this.initParams = new InitializationParameters(this.dataValidator, this.serviceContainer);
        this.initialize(applicationRunner);
    }

    protected final Service getSparkService() {
        return service;
    }

    protected final Context getContext() {
        return context;
    }

    protected final MiddlewareFactory getMiddlewareFactory() {
        return middlewareFactory;
    }

    protected final ServiceFactory getServiceFactory() {
        return serviceFactory;
    }

    protected final ControllerFactory getControllerFactory() {
        return controllerFactory;
    }

    protected final ServiceContainer getServiceContainer() {
        return serviceContainer;
    }

    protected final <T extends app.core.Service> T getService(Class<T> service) {
        return serviceContainer.getService(service);
    }

    private void initialize(ApplicationRunner<? extends Application> runner) {
        service.initExceptionHandler(e -> {
            logger.error("Error during application initialization", e);
            System.exit(0);
        });
        if (runner.exceptionHandlers != null) {
            for (Class<? extends Exception> exception : runner.exceptionHandlers.getHandlerMap().keySet()) {
                // noinspection unchecked
                service.exception(exception, runner.exceptionHandlers.getHandlerMap().get(exception));
            }
        }
    }

    void run() {
        try {
            onCreate();
            setupApplication();
            onPostCreate();
        } catch (Exception e) {
            logger.error("Error during application setup. Application will now exit", e);
            service.stop();
            System.exit(-1);
        }
    }

    private void setupApplication() throws Exception {
        initServices();
        initMiddleware();
        initRoutes();
    }

    private void initServices() throws Exception {

        class ServiceProperty {
            private final ServiceDescriptor descriptor;
            private final Class serviceClass;

            private ServiceProperty(Class serviceClass, ServiceDescriptor descriptor) {
                this.serviceClass = serviceClass;
                this.descriptor = descriptor;
            }

            int getPriority() {
                return descriptor.priority();
            }
        }

        Iterable<Class<?>> classes;
        PriorityQueue<ServiceProperty> propertyQueue;

        propertyQueue = new PriorityQueue<>(Comparator.comparingInt(ServiceProperty::getPriority));
        classes = getAnnotatedClasses(ServiceDescriptor.class);
        for (final Class serviceClass : classes) {
            if (app.core.Service.class.isAssignableFrom(serviceClass)) {
                propertyQueue.add(
                        new ServiceProperty(serviceClass,
                                getAnnotationFromClass(serviceClass, ServiceDescriptor.class))
                );
            } else {
                throw new IllegalArgumentException(serviceClass.getCanonicalName()
                        + " must implement " + app.core.Service.class);
            }
        }
        for (final ServiceProperty serviceProperty : propertyQueue) {
            if (logger.isInfoEnabled()) {
                logger.info("Adding service {}, priority {}",
                        serviceProperty.serviceClass, serviceProperty.getPriority());
            }
            app.core.Service service = createService(serviceProperty.serviceClass, serviceProperty.descriptor);
            serviceContainer.addService(service);
            new Thread(() -> {
                // initialize the service in another thread (sandbox) to prevent modification of the container
                service.serviceContainer = this.serviceContainer;
                service.onCreate();
            }).start();
        }
    }

    private void initMiddleware() throws Exception {
        Iterable<Class<?>> iterable;

        iterable = getAnnotatedClasses(Middleware.class);

        for (final Class<?> clazz : iterable) {
            if (Filter.class.isAssignableFrom(clazz)) {
                final Middleware middleware = getAnnotationFromClass(clazz, Middleware.class);
                final Filter filter = createMiddleware(clazz, middleware);
                service.addFilter(middleware.method(), new FilterImpl(middleware.path(), middleware.acceptTypes()) {
                    @Override
                    public void handle(Request request, Response response) throws Exception {
                        filter.handle(request, response);
                    }

                    @Override
                    public String toString() {
                        return clazz.getCanonicalName();
                    }
                });
            } else {
                throw new IllegalArgumentException(clazz.getCanonicalName() + " must implement " + Filter.class);
            }
        }
    }

    private void initRoutes() throws Exception {
        Iterable<Class<?>> routeClasses;

        routeClasses = getAnnotatedClasses(RouteController.class);
        for (Class<?> clazz : routeClasses) {
            if (Controller.class.isAssignableFrom(clazz)) {
                final RouteController meta = getAnnotationFromClass(clazz, RouteController.class);
                final Controller controller = createController(clazz, meta);

                final Method[] actionMethods = clazz.getMethods();
                int methodCount = 0;

                controller.initialize(initParams, meta.path());

                if (actionMethods.length > 0) {
                    for (Method method : actionMethods) {
                        if (ReflectionUtil.isNonStaticPublicMethod(method)) {
                            final Action action = method.getAnnotation(Action.class);
                            if (action != null) {
                                final ActionMethodWrapper wrapper = new ActionMethodWrapper(
                                        controller,
                                        method,
                                        action.permission(),
                                        action.checkPermission()
                                );
                                service.addRoute(
                                        action.method(),
                                        new RouteImpl(createActionRoute(meta, action),
                                                action.acceptTypes(), wrapper) {
                                            @Override
                                            public Object handle(Request request, Response response) throws Exception {
                                                return ((ActionMethodWrapper) super.delegate()).handle(request, response);
                                            }

                                            @Override
                                            public String toString() {
                                                return super.delegate().toString();
                                            }
                                        }
                                );
                                methodCount++;
                            }
                        }
                    }
                    if (methodCount == 0) {
                        throw new IllegalArgumentException(clazz.getCanonicalName()
                                + " does not have any actions. Actions must be public methods annotated with the "
                                + Action.class);
                    }
                } else {
                    throw new IllegalArgumentException(clazz.getCanonicalName() + " does not have any actions");
                }
            } else {
                throw new IllegalArgumentException(clazz.getCanonicalName() + " must extend " + Controller.class);
            }
        }
    }

    private String createActionRoute(RouteController routeController, Action action) {
        String path = (routeController.path() + "/" + action.path()).replaceAll("//", "/");
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private app.core.Service createService(Class type, ServiceDescriptor meta) throws Exception {
        // noinspection unchecked
        return getServiceFactory().getInstance(type, meta, getContext());
    }

    private Filter createMiddleware(Class type, Middleware meta) throws Exception {
        // noinspection unchecked
        return getMiddlewareFactory().getInstance(type, meta, getContext());
    }

    private Controller createController(Class type, RouteController meta) throws Exception {
        Controller controller;
        // noinspection unchecked
        controller = getControllerFactory().getInstance(type, meta, getContext());
        injectServices(controller);
        return controller;
    }

    private void injectServices(Controller controller) throws Exception {
        ServiceInjector.inject(controller, Application.this::getService);
    }

    private <A extends Annotation> A getAnnotationFromClass(Class clazz, Class<A> type) {
        return type.cast(clazz.getDeclaredAnnotation(type));
    }

    private Iterable<Class<?>> getAnnotatedClasses(Class<? extends Annotation> c) {
        return ClassIndex.getAnnotated(c);
    }

    /**
     * Called before services, routes, and filters have been initialized
     *
     * @throws Exception .
     */
    protected void onCreate() throws Exception {
    }

    /**
     * Called when service, routes, and filters have been initialized
     *
     * @throws Exception .
     */
    protected void onPostCreate() throws Exception {
    }

    protected final Logger getLogger() {
        return logger;
    }
}
