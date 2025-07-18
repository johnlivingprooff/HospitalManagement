package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.models.permission.Permission;
import app.util.ElementBuilder;
import app.util.PermissionUtil;
import org.jtwig.functions.FunctionRequest;

import java.util.List;

@TemplateFunction
public class ProtectedNavLink extends HelperFunctionImpl {
    public ProtectedNavLink() {
        super("protectedNavLink");
    }

    @Override
    public Object execute(FunctionRequest parameters) {
        String link;
        String label;
        String permission;
        String extraClass;
        List<Permission> permissions;
        List<Object> arguments;

        arguments = parameters.minimumNumberOfArguments(4).maximumNumberOfArguments(5).getArguments();

        link = (String) arguments.get(0);
        label = (String) arguments.get(1);
        permission = (String) arguments.get(2);
        // noinspection unchecked
        permissions = (List<Permission>) arguments.get(3);

        extraClass = arguments.size() >= 5 ? (" " + arguments.get(4)) : "";

        if (PermissionUtil.hasPermission(permission, permissions)) {
            return new ElementBuilder("li")
                    .classes("nav-item" + extraClass)
                    .addChild(
                            new ElementBuilder("a")
                                    .attribute("href", link)
                                    .classes("nav-link")
                                    .text(label)
                    ).build();
        }
        return null;
    }
}
