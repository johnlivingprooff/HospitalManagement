package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.util.ElementBuilder;
import org.jtwig.functions.FunctionRequest;

import java.util.List;
import java.util.Map;

@TemplateFunction
public class InputText extends HelperFunctionImpl {
    public InputText() {
        super("inputText");
    }

    // inputText(true, "id", value, [extra options])

    @Override
    public Object execute(FunctionRequest functionRequest) {
        final List<Object> args = functionRequest.minimumNumberOfArguments(3)
                .maximumNumberOfArguments(4)
                .getArguments();
        final boolean required = getArgument(args, 0, Boolean.class, true);
        final String identifier = getArgument(args, 1, String.class, Long.toHexString(System.currentTimeMillis()));
        final Object value = getArgument(args, 2, Object.class, "");
        String classes = "form-control ";
        final Map<String, Object> options = getArgument(args, 3, Map.class, Map.of());

        final ElementBuilder.Input input = new ElementBuilder
                .Input()
                .type("text")
                .id(identifier)
                .autofocus()
                .attribute("value", escape(value.toString(), functionRequest.getEnvironment()))
                .name(identifier);

        if (options.containsKey("readonly")) {
            input.attribute("readonly", "readonly");
        }

        if (options.containsKey("class")) {
            classes += " " + escape(String.valueOf(options.get("class")), functionRequest.getEnvironment());
        }

        if (options.containsKey("autocomplete")) {
            input.attribute("autocomplete", escape(String.valueOf(options.get("autocomplete")), functionRequest.getEnvironment()));
        }

        if (options.containsKey("hint")) {
            input.attribute("placeholder", escape(String.valueOf(options.get("hint")), functionRequest.getEnvironment()));
        }

        if (options.containsKey("min")) {
            input.attribute("minlength", escape(String.valueOf(options.get("min")), functionRequest.getEnvironment()));
        }

        if (options.containsKey("max")) {
            input.attribute("maxlength", escape(String.valueOf(options.get("max")), functionRequest.getEnvironment()));
        }

        if (required) {
            input.attribute("required", "required");
        }

        return input.classes(classes).build();
    }
}
