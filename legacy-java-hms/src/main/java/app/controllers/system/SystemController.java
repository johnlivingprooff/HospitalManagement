package app.controllers.system;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.account.Account;
import app.models.account.AdministratorDetails;
import app.models.permission.AclPermission;
import app.models.system.SystemSettings;
import app.services.audit.AuditService;
import app.services.auth.AuthService;
import app.services.location.LocationService;
import app.services.system.SystemService;
import app.services.user.AccountService;
import app.util.LocaleUtil;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@RouteController
@SuppressWarnings("unused")
public final class SystemController extends Controller {

    @Inject
    private SystemService systemService;

    @Action(path = "/Hms/SystemSettings", permission = AclPermission.WriteSystemSettings)
    public String editSystemSettings(Request request, Response response) {
        Model model;
        SystemSettings systemSettings;

        systemSettings = systemService.getSettings();

        model = createModel(request);
        model.put("banner", systemSettings.getBanner());
        return renderView("system/settings.html", model);
    }

    @Action(path = "/Hms/SystemSettings/Update", method = HttpMethod.post, permission = AclPermission.WriteSystemSettings)
    public String updateSystemSettings(Request request, Response response) {
        File file;
        Model model;
        boolean success;
        SystemSettings systemSettings;

        systemSettings = systemService.getSettings();

        if (success = validatePostData(request, SystemSettings.class, ValidationStage.Create)) {
            if (success = handleUploadedFile("picture", "logo", getAttachmentDirectory(), false, request, IMAGE_TYPES)) {
                if ((file = getUploadedFile("picture", request)) != null) {
                    deleteAttachmentFile(systemSettings.getLogo());
                    systemSettings.setLogo(file.getName());
                }
                copyValidatedData(request, systemSettings, ValidationStage.Create);
                systemService.updateSystemSettings(systemSettings);
                setSessionSuccessMessage("Settings updated successfully.", request);
            }
        }
        if (!success) {
            model = createModel(request);
            copyErrorListToModel(model, request);
        } else {
            model = createModel(request);
        }
        copyRawPostDataToModel(model, request);
        return renderView("system/settings.html", model);
    }

    @Action(path = "/GetWebsiteLogo", checkPermission = false)
    public Object getWebsiteLogo(Request request, Response response) {
        SystemService systemService;
        SystemSettings systemSettings;

        systemService = getService(SystemService.class);
        systemSettings = systemService.getSettings();

        if (LocaleUtil.isNullOrEmpty(systemSettings.getLogo())) {
            // Serve default image
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("public/assets/static/hms-default-logo.png")) {
                return serveFile(response, inputStream, "hms-default-logo.png", "image/png");
            } catch (IOException e) {
                getLogger().error("Error serving file from resources", e);
                return serverError(response);
            }
        } else {
            return serveFile(response, new File(getAttachmentDirectory(), systemSettings.getLogo()));
        }
    }

    @Action(path = "/Hms/About", permission = AclPermission.ReadVersion)
    public String getSystemVersion(Request request, Response response) {
        SystemService systemService;

        systemService = getService(SystemService.class);

        Map<String, Object> model = createModel(request);
        model.put("version", systemService.getVersion());
        model.put("timestamp", systemService.getTimestamp());
        model.put("artifactId", systemService.getArtifactId());
        return renderView("system/about.html", model);
    }

    @Action(path = "/Hms/Administrator", permission = AclPermission.WriteAdminSettings)
    public String editAdministratorInformation(Request request, Response response) {
        return updateAdminDetails(request, response, null);
    }

    @Action(path = "/Hms/System/Administrator/Update", permission = AclPermission.WriteAdminSettings, method = HttpMethod.post)
    public String updateAccount(Request request, Response response) {
        final Model model;
        final Account account;
        final AuthService authService;
        final AccountService accountService;
        final LocationService locationService;

        if (!validatePostData(request, AdministratorDetails.class, ValidationStage.Update)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);

            return updateAdminDetails(request, response, model);
        }

        accountService = getService(AccountService.class);
        account = accountService.getAccountById(getCurrentUser(request).getId());

        if (!account.getEmail().equalsIgnoreCase(requestAttribute("email", request))) {
            if (accountService.userEmailAddressExists(requestAttribute("email", request))) {
                setSessionErrorMessage("Email address is already in use", request);
                model = createModel(request);
                copyRawPostDataToModel(model, request);

                return updateAdminDetails(request, response, model);
            }

            account.setEmail(requestAttribute("email", request));
        }

        account.setFirstName(requestAttribute("firstName", request));
        account.setLastName(requestAttribute("lastName", request));
        account.setEmail(requestAttribute("email", request));

        accountService.updateAccount(account, AuditService.createLogEntry(request));

        account.setPassword(null);
        setSessionObject("account", account, request);

        setSessionSuccessMessage("Account updated successfully", request);

        return temporaryRedirect("/Hms/Administrator", response);
    }

    private String updateAdminDetails(Request request, Response response, Model model) {
        Account account;

        model = model != null ? model : createModel(request);
        account = getCurrentUser(request);

        model.put("email", account.getEmail());
        model.put("firstName", account.getFirstName());
        model.put("lastName", account.getLastName());

        return renderView("system/administrator.html", model);
    }
}
