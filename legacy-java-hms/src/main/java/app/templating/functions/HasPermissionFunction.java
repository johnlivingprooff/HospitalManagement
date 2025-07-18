package app.templating.functions;

import app.core.templating.HelperFunction;
import app.core.templating.TemplateFunction;
import app.models.permission.Permission;
import app.util.PermissionUtil;
import org.jtwig.functions.FunctionRequest;

import java.util.List;

@TemplateFunction
@SuppressWarnings("unchecked")
public class HasPermissionFunction implements HelperFunction<FunctionRequest> {
    @Override
    public String name() {
        return "hasPermission";
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        final List<Object> args = functionRequest.getArguments();
        if (args != null && args.size() == 2) {
            final String permission = (String) args.get(0);
            final List<Permission> permissionList = (List<Permission>) args.get(1);
            return PermissionUtil.hasPermission(permission, permissionList);
        }
        throw new RuntimeException("Missing permission and permission list arguments");
    }
}
