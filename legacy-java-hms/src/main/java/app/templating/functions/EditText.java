package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.util.ElementBuilder;
import org.jtwig.functions.FunctionRequest;

import java.util.List;
import java.util.Map;

@TemplateFunction
public class EditText extends HelperFunctionImpl {
    public EditText() {
        super("editText");
    }

    // editText(true, "id", value, [extra options])

    @Override
    public Object execute(FunctionRequest functionRequest) {
        final List<Object> args = functionRequest.minimumNumberOfArguments(3)
                .maximumNumberOfArguments(4)
                .getArguments();
        final boolean required = getArgument(args, 0, Boolean.class, true);
        final String identifier = getArgument(args, 1, String.class, Long.toHexString(System.currentTimeMillis()));
        final String value = getArgument(args, 2, String.class, "");
        String classes = "form-control ";
        final Map<String, Object> options = getArgument(args, 3, Map.class, Map.of());

        final ElementBuilder builder = new ElementBuilder("textarea", true)
                .id(identifier)
                .autofocus()
                .text(escape(value, functionRequest.getEnvironment()))
                .name(identifier);

        if (options.containsKey("readonly")) {
            builder.attribute("readonly", "readonly");
        }

        if (options.containsKey("class")) {
            classes += " " + escape(String.valueOf(options.get("class")), functionRequest.getEnvironment());
        }

        if (options.containsKey("autocomplete")) {
            builder.attribute("autocomplete", escape(String.valueOf(options.get("autocomplete")), functionRequest.getEnvironment()));
        }

        if (options.containsKey("hint")) {
            builder.attribute("placeholder", escape(String.valueOf(options.get("hint")), functionRequest.getEnvironment()));
        }

        if (options.containsKey("min")) {
            builder.attribute("minlength", escape(String.valueOf(options.get("min")), functionRequest.getEnvironment()));
        }

        if (options.containsKey("max")) {
            builder.attribute("maxlength", escape(String.valueOf(options.get("max")), functionRequest.getEnvironment()));
        }

        if (options.containsKey("rows")) {
            builder.attribute("rows", escape(String.valueOf(options.get("rows")), functionRequest.getEnvironment()));
        }

        if (required) {
            builder.attribute("required", "required");
        }

        return builder.classes(classes).build();
    }
}
