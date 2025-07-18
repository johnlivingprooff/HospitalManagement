package app.core.templating;

import org.atteo.classindex.ClassIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.TemplateEngine;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class ViewRenderer {
    private TemplateEngine templateEngine;
    private final OnFunctionListLoadedListener listener;
    private static final Logger logger = LoggerFactory.getLogger(ViewRenderer.class);

    public ViewRenderer(TemplateEngine engine, OnFunctionListLoadedListener listener) {
        this.listener = listener;
        this.loadHelperFunctions();
        this.templateEngine = Objects.requireNonNull(engine, "Template engine must not be null");
    }

    public String render(Object object) {
        return templateEngine.render(object);
    }

    public String render(String viewName) {
        return templateEngine.render(templateEngine.modelAndView(null, viewName));
    }

    public String render(ModelAndView modelAndView) {
        return templateEngine.render(modelAndView);
    }

    private void loadHelperFunctions() {
        Iterable<Class<?>> classes;
        List<? super Object> functions;


        if (listener != null) {
            functions = new LinkedList<>();
            classes = ClassIndex.getAnnotated(TemplateFunction.class);
            try {

                for (Class<?> clazz : classes) {
                    if (!HelperFunction.class.isAssignableFrom(clazz)) {
                        throw new IllegalArgumentException(clazz + " must implement " + Function.class);
                    }
                    functions.add(clazz.getConstructor().newInstance());
                }
                listener.onFunctionListLoaded(functions);
            } catch (Exception e) {
                logger.error("Error when loading template helper functions for engine " + templateEngine, e);
                throw new RuntimeException("Error when loading template helper functions", e);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Skipping function loading because listener is null");
            }
        }
    }

    public interface OnFunctionListLoadedListener {
        void onFunctionListLoaded(List functions);
    }
}
