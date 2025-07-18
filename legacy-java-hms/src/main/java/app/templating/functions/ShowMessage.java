package app.templating.functions;

import app.core.templating.TemplateFunction;

@TemplateFunction
public class ShowMessage extends MessageFunction {
    public ShowMessage() {
        super(AlertType.Light, "showMessage");
    }
}
