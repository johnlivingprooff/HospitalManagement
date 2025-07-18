package app.controllers.role;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.RouteController;
import app.core.validation.DataValidator;
import app.models.permission.AclPermission;
import app.models.permission.Permission;
import app.models.role.Role;
import app.services.audit.AuditService;
import app.services.permission.PermissionService;
import app.types.Bool;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.util.Date;
import java.util.Map;

@RouteController(path = "/Hms/Roles")
@SuppressWarnings("unused")
public class RoleController extends Controller {

    private Role getSelectedRole(Request request) {
        Long id;
        Role role;
        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((role = getService(PermissionService.class).getRoleById(id)) != null) {
                return role;
            }
        }
        setSessionErrorMessage("Selected role does not exist", request);
        return null;
    }

    private Permission getSelectedPermission(Request request) {
        Long id;
        Permission permission;
        if ((id = getNumericQueryParameter(request, "permission-id", Long.class)) != null) {
            if ((permission = getService(PermissionService.class).getPermissionById(id)) != null) {
                return permission;
            }
        }
        setSessionErrorMessage("Selected permission does not exist", request);
        return null;
    }

    @Action(path = "/", permission = AclPermission.ReadRoles)
    public String getRoleList(Request request, Response response) {
        Map<String, Object> model = createModel(request);
        model.put("roles", getService(PermissionService.class).getRoles());
        return renderView("role/list.html", model);
    }

    @Action(path = "/:id/View", permission = AclPermission.ReadRoles)
    public String viewRole(Request request, Response response) {
        final Role role;
        final Map<String, Object> model;
        final PermissionService permissionService;

        permissionService = getService(PermissionService.class);

        if ((role = getSelectedRole(request)) == null) {
            return temporaryRedirect("/Hms/Roles", response);
        }

        if (role.isPrivileged()) {
            setSessionErrorMessage("The selected role does not exist", request);
            return temporaryRedirect("/Hms/Roles", response);
        }

        model = createModel(request);
        model.put("role", role);
        model.put("rolePermissions", permissionService.getRolePermissionList(role.getRoleKey()));
        return renderView("role/details.html", model);
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WriteRoles)
    public String editRole(Request request, Response response) {
        final Role role;

        if ((role = getSelectedRole(request)) == null) {
            return temporaryRedirect("/Hms/Roles", response);
        }

        if (role.isSystemRole() || role.isPrivileged() || role.isAdministrator()) {
            setSessionErrorMessage("This role cannot be edited", request);
            return temporaryRedirect("/Hms/Roles", response);
        }

        Map<String, Object> model = createModel(request);
        model.put("id", role.getId());
        model.put("name", role.getRoleName());
        model.put("active", role.isActive());
        model.put("booleans", Bool.VALUES);
        model.put("description", role.getRoleDescription());
        return renderView("role/edit.html", model);
    }

    @Action(path = "/:id/Permissions", permission = AclPermission.WriteRoles)
    public String getRolePermissions(Request request, Response response) {
        final Role role;
        final PermissionService permissionService;

        if ((role = getSelectedRole(request)) == null) {
            return temporaryRedirect("/Hms/Roles", response);
        }

        if (role.isSystemRole() || role.isPrivileged() || role.isAdministrator()) {
            setSessionErrorMessage("This role cannot be edited", request);
            return temporaryRedirect("/Hms/Roles", response);
        }

        permissionService = getService(PermissionService.class);

        Map<String, Object> model = createModel(request);
        model.put("role", role);
        model.put("permissions", permissionService.getRemainingRolePermissions(role));
        model.put("rolePermissions", permissionService.getRolePermissionList(role.getRoleKey()));
        return renderView("role/permissions.html", model);
    }

    @Action(path = "/:id/AddPermission/:permission-id", permission = AclPermission.WriteRoles)
    public String addRolePermission(Request request, Response response) {
        final Role role;
        final Permission permission;
        final Permission parentPermission;
        final PermissionService permissionService;

        if ((role = getSelectedRole(request)) == null) {
            return temporaryRedirect("/Hms/Roles", response);
        }

        if (role.isSystemRole() || role.isPrivileged() || role.isAdministrator()) {
            setSessionErrorMessage("This role cannot be edited", request);
            return temporaryRedirect("/Hms/Roles", response);
        }

        if ((permission = getSelectedPermission(request)) == null) {
            return temporaryRedirect("/Hms/Roles/" + role.getId() + "/Permissions", response);
        }

        if (permission.isPrivileged()) {
            setSessionErrorMessage("Selected permission cannot be added or removed to and from roles", request);
            return null;
        }

        permissionService = getService(PermissionService.class);

        if (permissionService.doesRoleHavePermission(role, permission)) {
            setSessionErrorMessage("This role already has the selected permission", request);
            return temporaryRedirect("/Hms/Roles/" + role.getId() + "/Permissions", response);
        }

        // Implicitly add parent permission. This parent permission cannot be manually added
        if ((parentPermission = permissionService.getPermissionParent(permission)) != null) {
            if (!permissionService.doesRoleHavePermission(role, parentPermission)) {
                permissionService.addRolePermission(role, parentPermission);
            }
        }

        permissionService.addRolePermission(role, permission);

        getService(AuditService.class)
                .log(
                        getCurrentUser(request).fullname() + " added permission " + permission + " to role " + role,
                        request
                );

        setSessionSuccessMessage("Permission added to role", request);
        return temporaryRedirect("/Hms/Roles/" + role.getId() + "/Permissions", response);
    }

    @Action(path = "/:id/RemovePermission/:permission-id", permission = AclPermission.WriteRoles)
    public String removeRolePermission(Request request, Response response) {
        final Role role;
        final Permission permission;
        final Permission parentPermission;
        final PermissionService permissionService;

        if ((role = getSelectedRole(request)) == null) {
            return temporaryRedirect("/Hms/Roles", response);
        }

        if (role.isSystemRole() || role.isPrivileged() || role.isAdministrator()) {
            setSessionErrorMessage("This role cannot be edited", request);
            return temporaryRedirect("/Hms/Roles", response);
        }

        if ((permission = getSelectedPermission(request)) == null) {
            return temporaryRedirect("/Hms/Roles/" + role.getId() + "/Permissions", response);
        }

        if (permission.isPrivileged()) {
            setSessionErrorMessage("Selected permission cannot be manually removed", request);
            return temporaryRedirect("/Hms/Roles/" + role.getId() + "/Permissions", response);
        }

        permissionService = getService(PermissionService.class);

        if (!permissionService.doesRoleHavePermission(role, permission)) {
            setSessionErrorMessage("The selected permission does not belong to the role", request);
            return temporaryRedirect("/Hms/Roles/" + role.getId() + "/Permissions", response);
        }

        // Remove parent permission if this permission is the last permission that depends on it, under the given role
        if ((parentPermission = permissionService.getPermissionParent(permission)) != null) {
            if(permissionService.getRolePermissionDependenceCount(role, parentPermission, permission) == 0){
                permissionService.removeRolePermission(role, parentPermission);
            }
        }

        permissionService.removeRolePermission(role, permission);

        getService(AuditService.class)
                .log(
                        getCurrentUser(request).fullname() + " removed permission " + permission + " from role " + role,
                        request
                );

        setSessionSuccessMessage("Permission removed from role", request);
        return temporaryRedirect("/Hms/Roles/" + role.getId() + "/Permissions", response);
    }

    @Action(path = "/New", permission = AclPermission.WriteRoles)
    public String createRoleForm(Request request, Response response) {
        final PermissionService permissionService = getService(PermissionService.class);

        Map<String, Object> model = createModel(request);
        model.put("booleans", Bool.VALUES);
        return renderView("role/new.html", model);
    }

    @DataValidator.Schema("role/add")
    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteRoles)
    public String createRole(Request request, Response response) {
        final Role role;
        final String nextRoleName;
        final Map<String, Object> model;
        final PermissionService permissionService;

        permissionService = getService(PermissionService.class);

        if (!validatePostData(request, "add")) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            model.put("booleans", Bool.VALUES);
            return renderView("role/new.html", model);
        }

        role = new Role();
        role.setSystemRole(false);
        role.setPrivileged(false);
        role.setCreated(new Date());
        role.setModified(role.getCreated());
        role.setActive(requestAttribute("active", request));
        role.setRoleName(requestAttribute("name", request));
        role.setRoleKey(permissionService.getNextRoleKey());
        role.setRoleDescription(requestAttribute("description", request));

        permissionService.addRole(role);

        getService(AuditService.class)
                .log(getCurrentUser(request).fullname() + " added role " + role.toString(), request);

        setSessionSuccessMessage("System role added", request);

        // Redirect to edit page to add permissions
        return temporaryRedirect("/Hms/Roles/" + role.getId() + "/Permissions", response);
    }

    @DataValidator.Schema("role/update")
    @Action(path = "/Update", method = HttpMethod.post, permission = AclPermission.WriteRoles)
    public String updateRole(Request request, Response response) {
        final Role role;
        final Map<String, Object> model;
        final PermissionService permissionService;

        permissionService = getService(PermissionService.class);

        if (!validatePostData(request, "update")) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            model.put("booleans", Bool.VALUES);
            return renderView("role/edit.html", model);
        }

        if ((role = permissionService.getRoleById(requestAttribute("id", request))) == null) {
            setSessionErrorMessage("Selected role does not exist", request);
            return temporaryRedirect("/Hms/Roles", response);
        }

        if (role.isSystemRole() || role.isPrivileged() || role.isAdministrator()) {
            setSessionErrorMessage("This role cannot be edited", request);
            return temporaryRedirect("/Hms/Roles", response);
        }

        role.setModified(new Date());
        role.setActive(requestAttribute("active", request));
        role.setRoleName(requestAttribute("name", request));
        role.setRoleDescription(requestAttribute("description", request));

        permissionService.updateRole(role);

        getService(AuditService.class)
                .log(getCurrentUser(request).fullname() + " updated role " + role.toString(), request);

        setSessionSuccessMessage("System role updated", request);

        return temporaryRedirect("/Hms/Roles", response);
    }
}
