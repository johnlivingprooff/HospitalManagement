package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.models.permission.Permission;
import app.util.PermissionUtil;
import org.jtwig.functions.FunctionRequest;

import java.util.List;

@TemplateFunction
public class EndProtectedMenu extends HelperFunctionImpl {
    public EndProtectedMenu() {
        super("endProtectedMenu");
    }

    @Override
    public Object execute(FunctionRequest parameters) {
        String permission;
        List<Permission> permissions;
        List<Object> arguments;

        arguments = parameters.minimumNumberOfArguments(2).maximumNumberOfArguments(2).getArguments();

        permission = (String) arguments.get(0);
        // noinspection unchecked
        permissions = (List<Permission>) arguments.get(1);

        if (PermissionUtil.hasPermission(permission, permissions)) {
            return "</div></li>";
        }
        return null;
    }
}
