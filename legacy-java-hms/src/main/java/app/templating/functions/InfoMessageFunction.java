package app.templating.functions;

import app.core.templating.TemplateFunction;

@TemplateFunction
public class InfoMessageFunction extends MessageFunction {
    public InfoMessageFunction() {
        super(AlertType.Info, "showInfoMessage");
    }
}
