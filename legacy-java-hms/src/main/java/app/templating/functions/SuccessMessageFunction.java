package app.templating.functions;

import app.core.templating.TemplateFunction;

@TemplateFunction
public class SuccessMessageFunction extends MessageFunction {
    public SuccessMessageFunction() {
        super(AlertType.Success, "showSuccessMessage");
    }
}
