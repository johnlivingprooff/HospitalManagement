package app.templating.functions;

import app.core.templating.TemplateFunction;
import org.jtwig.functions.FunctionRequest;

import java.util.List;

@TemplateFunction
public class ErrorListFunction extends HelperFunctionImpl {

    public ErrorListFunction() {
        super("printErrorList");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(FunctionRequest functionRequest) {
        List<Object> args = functionRequest.minimumNumberOfArguments(1).maximumNumberOfArguments(2).getArguments();
        if (args != null && args.size() >= 1) {
            Object o = args.get(0);
            if (!isUndefinedOrNull(o)) {
                List<String> errorList = (List<String>) o;
                if (!errorList.isEmpty()) {
                    StringBuilder sb = new StringBuilder("<div class=");

                    // append extra classes
                    sb.append('"').append("alert alert-danger");

                    if (args.size() >= 2) {
                        sb.append(' ').append(args.get(1));
                    }
                    sb.append('"')
                            .append(" role=")
                            .append('"')
                            .append("alert")
                            .append('"')
                            .append("><ul>");


                    for (String error : errorList) {
                        sb.append("<li>").append(error).append("</li>");
                    }
                    sb.append("</ul></div>");
                    return sb.toString();
                }
            }
        }
        return "";
    }
}
