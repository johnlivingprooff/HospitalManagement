package app.services.templating;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.core.templating.ViewRenderer;
import app.templating.AppTemplateEngine;
import spark.ModelAndView;

import java.util.Collections;
import java.util.Map;

@ServiceDescriptor
public final class TemplateService extends ServiceImpl {
    private ViewRenderer viewRenderer;
    private final AppTemplateEngine templateEngine;

    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public TemplateService(Configuration configuration) {
        super(configuration);
        templateEngine = new AppTemplateEngine();
        viewRenderer = new ViewRenderer(templateEngine, templateEngine::initialize);
    }

    public String render(String view, Map<String, Object> model) {
        return viewRenderer.render(new ModelAndView(model, view));
    }

    public String render(String view) {
        return render(view, Collections.emptyMap());
    }

    public String escapeString(String input) {
        return templateEngine.escape(input);
    }
}
