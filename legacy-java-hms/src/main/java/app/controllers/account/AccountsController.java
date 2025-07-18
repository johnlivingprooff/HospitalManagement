package app.controllers.account;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.account.Account;
import app.models.account.AccountType;
import app.models.account.Sex;
import app.models.location.Department;
import app.models.permission.AclPermission;
import app.models.role.Role;
import app.services.UploadService;
import app.services.audit.AuditService;
import app.services.auth.AuthService;
import app.services.location.LocationService;
import app.services.messaging.MessagingService;
import app.services.permission.PermissionService;
import app.services.user.AccountService;
import app.types.Bool;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.io.File;
import java.util.Date;


@RouteController(path = "/Hms/Accounts")
@SuppressWarnings("unused")
public final class AccountsController extends Controller {

    private Account getSelectedAccount(Request request) {
        Long id;
        Account account;
        AccountService accountService;

        accountService = getService(AccountService.class);

        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((account = accountService.getAccountById(id)) != null && (!account.isHidden() || !account.isSystem())) {
                return account;
            }
            return null;
        }

        setSessionErrorMessage("Selected account does not exist", request);
        return null;
    }

    private String getAccounts(Request request, boolean active) {
        final Model model;
        final AccountService accountService;

        accountService = getService(AccountService.class);

        model = createModel(request);
        if (active) {
            model.put("pageTitle", "Active Accounts");
            model.put("accounts", accountService.getActiveAccounts());
        } else {
            model.put("accounts", accountService.getInactiveAccounts());
            model.put("pageTitle", "Inactive Accounts");
        }
        return renderView("accounts/list.html", model);
    }

    @Action(path = "/", permission = AclPermission.ReadAccounts)
    public String getActiveAccounts(Request request, Response response) {
        return getAccounts(request, true);
    }

    @Action(path = "/Inactive", permission = AclPermission.ReadAccounts)
    public String getInactiveAccounts(Request request, Response response) {
        return getAccounts(request, false);
    }

    @Action(path = "/New", permission = AclPermission.WriteAccounts)
    public String newAccount(Request request, Response response) {
        final Model model;
        model = createModel(request);
        return newAccountModel(model);
    }

    private String newAccountModel(Model model) {
        model.put("roles", getService(PermissionService.class).getActiveRoles());
        model.put("sexes", Sex.VALUES);
        model.put("booleans", Bool.VALUES);
        model.put("accountTypes", AccountType.TYPES);
        model.put("departments", getService(LocationService.class).getDepartments(true));
        return renderView("accounts/new.html", model);
    }

    private Department getDepartment(long departmentId, LocationService locationService, Request request) {
        Department department;
        if ((department = locationService.getDepartmentById(departmentId)) == null || department.isHidden()) {
            setSessionErrorMessage("Selected department does not exist", request);
            return null;
        }
        if (!department.isActive()) {
            setSessionErrorMessage("Selected department is inactive and cannot be used", request);
            return null;
        }
        return department;
    }

    private Role getRole(long roleId, Request request) {
        Role role;
        PermissionService permissionService;

        permissionService = getService(PermissionService.class);

        if ((role = permissionService.getRoleById(roleId)) == null || role.isPrivileged()) {
            setSessionErrorMessage("Selected role does not exist", request);
            return null;
        }
        if (!role.isActive()) {
            setSessionErrorMessage("Selected role is inactive and cannot be used", request);
            return null;
        }
        return role;
    }

    private String editAccountModel(Model model, Account account) {
        model.put("roles", getService(PermissionService.class).getActiveRoles());
        model.put("sexes", Sex.VALUES);
        model.put("booleans", Bool.VALUES);
        model.put("departments", getService(LocationService.class).getDepartments(true));
        model.put("accountTypes", AccountType.TYPES);

        model.put("id", account.getId());
        model.put("dob", account.getDob());
        model.put("sex", account.getSex());
        model.put("email", account.getEmail());
        model.put("active", account.isActive());
        model.put("phone", account.getPhone());
        model.put("roleId", account.getRoleId());
        model.put("lastName", account.getLastName());
        model.put("roleName", account.getRoleName());
        model.put("firstName", account.getFirstName());
        model.put("accountType", account.getAccountType());
        model.put("departmentId", account.getDepartmentId());
        model.put("role_modifiable", account.isRole_modifiable());
        return renderView("accounts/edit.html", model);
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WriteAccounts)
    public String editAccount(Request request, Response response) {
        final Model model;
        final Account account;

        if ((account = getSelectedAccount(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }
        model = createModel(request);
        return editAccountModel(model, account);
    }

    @Action(path = "/Update", permission = AclPermission.WriteAccounts, method = HttpMethod.post)
    public String updateAccount(Request request, Response response) {
        final Date date;
        final Role role;
        final Model model;
        final Account account;
        final Department department;
        final AuthService authService;
        final AccountService accountService;
        final LocationService locationService;

        if (!validatePostData(request, Account.class, ValidationStage.Update)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newAccountModel(model);
        }

        accountService = getService(AccountService.class);
        locationService = getService(LocationService.class);

        if ((account = accountService.getAccountById(requestAttribute("id", request))) == null) {
            setSessionErrorMessage("Selected account does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newAccountModel(model);
        }

        if (account.isSystem() || account.isHidden()) {
            setSessionErrorMessage("Selected account does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newAccountModel(model);
        }

        if ((department = getDepartment(requestAttribute("departmentId", request), locationService, request)) == null) {
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newAccountModel(model);
        }

        if (account.isRole_modifiable()) {
            if ((role = getRole(requestAttribute("roleId", request), request)) == null) {
                model = createModel(request);
                copyRawPostDataToModel(model, request);
                return newAccountModel(model);
            }
        } else {
            role = null;
        }

        if (!account.getEmail().equalsIgnoreCase(requestAttribute("email", request))) {
            if (accountService.userEmailAddressExists(requestAttribute("email", request))) {
                setSessionErrorMessage("Email address is already in use", request);
                model = createModel(request);
                copyRawPostDataToModel(model, request);
                return newAccountModel(model);
            }
            account.setEmail(requestAttribute("email", request));
        }

        if (!handleUploadedFile("picture", "Profile image", getService(UploadService.class).getProfileImageDirectory(),
                false, request, IMAGE_TYPES)) {
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newAccountModel(model);
        }

        final File file = getUploadedFile("picture", request);
        if (file != null) {
            getService(UploadService.class).deleteImageFile(account.getPicture());
            account.setPicture(file.getName());
        }

        date = new Date();
        account.setFirstName(requestAttribute("firstName", request));
        account.setLastName(requestAttribute("lastName", request));
        account.setDob(requestAttribute("dob", request));
        account.setSex(requestAttribute("sex", request));
        account.setAccountType(requestAttribute("accountType", request));
        account.setPhone(requestAttribute("phone", request));
        if (account.isRole_modifiable() && role != null) {
            account.setRoleKey(role.getRoleKey());
        }
        account.setDepartmentId(department.getId());
        account.setActive(requestAttribute("active", request));
        account.setEmail(requestAttribute("email", request));
        account.setModified(date);

        accountService.updateAccount(account, AuditService.createLogEntry(request));

        setSessionSuccessMessage("Account updated successfully", request);

        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/Add", permission = AclPermission.WriteAccounts, method = HttpMethod.post)
    public String addAccount(Request request, Response response) {
        final Date date;
        final Role role;
        final Model model;
        final Account account;
        final Department department;
        final AuthService authService;
        final AccountService accountService;
        final LocationService locationService;

        if (!validatePostData(request, Account.class, ValidationStage.Create)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newAccountModel(model);
        }

        authService = getService(AuthService.class);
        accountService = getService(AccountService.class);
        locationService = getService(LocationService.class);

        if ((department = getDepartment(requestAttribute("departmentId", request), locationService, request)) == null) {
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newAccountModel(model);
        }

        if ((role = getRole(requestAttribute("roleId", request), request)) == null) {
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newAccountModel(model);
        }

        if (accountService.userEmailAddressExists(requestAttribute("email", request))) {
            setSessionErrorMessage("Email address is already in use", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newAccountModel(model);
        }

        if (!handleUploadedFile("picture", "Profile image", getService(UploadService.class).getProfileImageDirectory(),
                false, request, IMAGE_TYPES)) {
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newAccountModel(model);
        }

        date = new Date();
        account = new Account();
        account.setRole_modifiable(true);
        account.setFirstName(requestAttribute("firstName", request));
        account.setLastName(requestAttribute("lastName", request));
        account.setDob(requestAttribute("dob", request));
        account.setSex(requestAttribute("sex", request));
        account.setRoleKey(role.getRoleKey());
        account.setDepartmentId(department.getId());
        account.setActive(requestAttribute("active", request));
        account.setEmail(requestAttribute("email", request));
        account.setAccountType(requestAttribute("accountType", request));
        account.setPhone(requestAttribute("phone", request));
        account.setModified(date);
        account.setCreated(date);
        account.setPassword(authService.generatePassword());

        File imageFile = getUploadedFile("picture", request);

        if (imageFile != null) {
            account.setPicture(imageFile.getName());
        }

        accountService.addAccount(account, AuditService.createLogEntry(request));

        sendAccountSetupInstructions(account);

        setSessionSuccessMessage("Account added successfully", request);

        return temporaryRedirect(getBaseUrl(), response);
    }

    private void sendAccountSetupInstructions(Account account) {
        getService(MessagingService.class).sendAccountSetupInstructions(account);
    }

    private String getCandidatesByDepartment(Request request, Response response) {
        Long id;
        Department department;
        LocationService locationService;

        response.type("application/json");

        if ((id = getNumericQueryParameter(request, "department-id", Long.class)) != null) {
            locationService = getService(LocationService.class);
            if ((department = locationService.getDepartmentById(id)) != null) {
                if (department.isGoodForRegularUse()) {
                    return getGson().toJson(
                            getService(AccountService.class).getCandidatesByDepartment(department)
                    );
                }
            }
        }
        return "[]";
    }
}