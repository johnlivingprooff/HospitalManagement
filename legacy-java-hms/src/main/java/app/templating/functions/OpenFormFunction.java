package app.templating.functions;

import app.core.templating.HelperFunction;
import app.core.templating.TemplateFunction;
import app.util.ElementBuilder;
import org.jtwig.functions.FunctionRequest;

import java.util.List;

@TemplateFunction
public class OpenFormFunction implements HelperFunction<FunctionRequest> {
    @Override
    public String name() {
        return "openForm";
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        // openForm(url, class, true, id)
        final FunctionRequest request = functionRequest.minimumNumberOfArguments(1).maximumNumberOfArguments(3);
        final List<Object> args = request.getArguments();
        final String url = String.valueOf(args.get(0).toString());
        final String classList = args.size() >= 2 ? String.valueOf(args.get(1)) : "";
        final boolean isMultipart = args.size() == 3 ? Boolean.valueOf(String.valueOf(args.get(2))) : false;

        ElementBuilder.FormBuilder builder = new ElementBuilder.FormBuilder();

        builder.target(url)
                .method("POST")
                .enctype(isMultipart ? "multipart/form-data" : "application/x-www-form-urlencoded")
                .charset("UTF-8")
                .classes(classList);
        return builder.build();
    }
}
