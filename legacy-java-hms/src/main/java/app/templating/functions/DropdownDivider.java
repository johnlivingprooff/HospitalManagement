package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.util.ElementBuilder;
import org.jtwig.functions.FunctionRequest;

@TemplateFunction
public class DropdownDivider extends HelperFunctionImpl {
    public DropdownDivider() {
        super("dropdownDivider");
    }

    @Override
    public Object execute(FunctionRequest parameters) {
        return new ElementBuilder("div")
                .classes("dropdown-divider brighter-border")
                .build();
    }
}
