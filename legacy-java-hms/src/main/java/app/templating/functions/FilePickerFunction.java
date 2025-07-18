package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.util.ElementBuilder;
import org.jtwig.functions.FunctionRequest;

import java.util.List;

@TemplateFunction
public class FilePickerFunction extends HelperFunctionImpl {
    public FilePickerFunction() {
        super("filePicker");
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        final List<Object> args = functionRequest.minimumNumberOfArguments(3)
                .maximumNumberOfArguments(4)
                .getArguments();
        final boolean required = getArgument(args, 0, Boolean.class, true);
        final String identifier = getArgument(args, 1, String.class, "id");
        final String accept = getArgument(args, 2, String.class, "*/*");
        final String classes = "form-control " + getArgument(args, 3, String.class, "");
        return build(required, classes, accept, identifier);
    }

    String build(boolean required, String classes, String accept, String identifier) {
        ElementBuilder builder = new ElementBuilder("input")
                .single()
                .attribute("type", "file")
                .classes(classes)
                .autofocus()
                .attribute("accept", accept)
                .attribute("id", identifier)
                .attribute("name", identifier);
        if (required) {
            builder.attribute("required", "required");
        }
        return builder.build();
    }
}
