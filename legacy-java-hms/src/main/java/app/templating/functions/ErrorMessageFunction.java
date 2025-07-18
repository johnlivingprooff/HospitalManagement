package app.templating.functions;

import app.core.templating.TemplateFunction;

@TemplateFunction
public class ErrorMessageFunction extends MessageFunction {

    public ErrorMessageFunction() {
        super(AlertType.Danger, "showErrorMessage");
    }
}
