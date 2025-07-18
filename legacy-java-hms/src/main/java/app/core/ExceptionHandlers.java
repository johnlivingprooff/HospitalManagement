package app.core;

import spark.ExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ExceptionHandlers<T extends Application> {
    private final ApplicationRunner<T> runner;
    private final Map<Class<? extends Exception>, ExceptionHandler> handlerMap;

    ExceptionHandlers(ApplicationRunner<T> runner) {
        this.runner = runner;
        this.handlerMap = new LinkedHashMap<>();
    }

    public ExceptionHandlers<T> clear() {
        handlerMap.clear();
        return this;
    }

    public <E extends Exception> ExceptionHandlers<T> add(Class<E> clazz, ExceptionHandler handler) {
        handlerMap.put(clazz, handler);
        return this;
    }

    public ApplicationRunner<T> runner() {
        return runner;
    }

    Map<Class<? extends Exception>, ExceptionHandler> getHandlerMap() {
        return handlerMap;
    }
}
