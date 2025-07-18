package app.templating;

import app.core.templating.HelperFunction;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.JtwigFunction;
import org.jtwig.functions.SimpleJtwigFunction;
import spark.ModelAndView;
import spark.template.jtwig.JtwigTemplateEngine;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class AppTemplateEngine extends JtwigTemplateEngine {
    private final String templatesDirectory;
    private EnvironmentConfiguration configuration;

    public AppTemplateEngine() {
        this("templates");
    }

    public AppTemplateEngine(String templatesDirectory) {
        super(templatesDirectory);
        this.templatesDirectory = templatesDirectory;
    }

    public String escape(String input) {
        return configuration.getEscapeConfiguration()
                .getEscapeEngineMap()
                .get(configuration.getEscapeConfiguration().getDefaultEngine())
                .escape(input);
    }

    public void initialize(List<? extends HelperFunction> functions) {
        EnvironmentConfigurationBuilder builder = EnvironmentConfigurationBuilder
                .configuration()
                .escape()
                .and();

        for (final HelperFunction function : functions) {
            if (function instanceof SimpleJtwigFunction) {
                builder.functions().add((JtwigFunction) function);
            } else {
                builder.functions().add(new SimpleJtwigFunction() {
                    @Override
                    public String name() {
                        return function.name();
                    }

                    @Override
                    public Object execute(FunctionRequest functionRequest) {
                        // noinspection unchecked
                        try {
                            return function.execute(functionRequest);
                        } catch (Exception e) {
                            throw new RuntimeException("Error in template function " + name(), e);
                        }
                    }
                });
            }
        }
        configuration = builder.build();
    }


    @SuppressWarnings("unchecked")
    @Override
    public String render(ModelAndView modelAndView) {
        String viewName = this.templatesDirectory + "/" + modelAndView.getViewName();
        JtwigTemplate template = JtwigTemplate.classpathTemplate(viewName, configuration);
        JtwigModel model = JtwigModel.newModel((Map) modelAndView.getModel());
        return template.render(model);
    }
}