package app.controllers.permission;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.RouteController;
import app.models.permission.AclPermission;
import app.services.permission.PermissionService;
import spark.Request;
import spark.Response;

import java.util.Map;

@RouteController(path = "/Hms/Permissions")
@SuppressWarnings({"unused", "WeakerAccess"})
public final class PermissionController extends Controller {

    @Action(path = "/", permission = AclPermission.ReadPermissions)
    public String getPermissionList(Request request, Response response) {
        Map<String, Object> model = createModel(request);
        model.put("permissions", getService(PermissionService.class).getPermissions());
        return renderView("permission/list.html", model);
    }
}
