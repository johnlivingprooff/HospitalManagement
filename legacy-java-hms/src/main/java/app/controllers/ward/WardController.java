package app.controllers.ward;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.permission.AclPermission;
import app.models.ward.Ward;
import app.services.audit.AuditService;
import app.services.ward.WardService;
import app.types.Bool;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

@RouteController(path = "/Hms/Wards")
public class WardController extends Controller {

    @Inject
    private WardService wardService;

    @Action(path = "/", permission = AclPermission.ReadWards)
    public String getWards(Request request, Response response) {
        Model model;

        model = createModel(request);
        model.put("wards", wardService.getWards());
        return renderView("wards/list.html", model);
    }

    private String newWardRequest(Request request, Model model) {
        if (model == null) {
            model = createModel(request);
        }
        model.put("booleans", Bool.VALUES);
        return renderView("wards/new.html", model);
    }

    @Action(path = "/New", permission = AclPermission.WriteWards)
    public String newWard(Request request, Response response) {
        return newWardRequest(request, null);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteWards)
    public String addWard(Request request, Response response) {
        final Ward ward;
        final Model model;

        if (!validatePostData(request, Ward.class, ValidationStage.Create)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newWardRequest(request, model);
        }

        if (wardService.isWardCodeInUse(requestAttribute("code", request))) {
            setSessionErrorMessage("Selected code already exists", request);
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newWardRequest(request, model);
        }

        ward = new Ward();
        copyValidatedData(request, ward, ValidationStage.Create);

        wardService.addWard(ward);
        getService(AuditService.class).log(getCurrentUser(request) + " added ward " + ward, request);

        setSessionSuccessMessage("Ward added successfully", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    private Ward getSelectedWard(Request request, boolean isPostRequest) {
        final Long id;
        final Ward ward;

        if (isPostRequest) {
            id = requestAttribute("id", request);
        } else {
            id = getNumericQueryParameter(request, "id", Long.class);
        }

        if (id != null) {
            if ((ward = wardService.findWardById(id)) != null) {
                return ward;
            }
        }

        setSessionErrorMessage("Selected ward does not exist", request);
        return null;
    }

    private String editWardRequest(Request request, Ward ward, Model model) {
        if (model == null) {
            model = createModel(request);
        }
        model.put("id", ward.getId());
        model.put("name", ward.getName());
        model.put("code", ward.getCode());
        model.put("active", ward.isActive());
        model.put("booleans", Bool.VALUES);
        return renderView("wards/edit.html", model);
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WriteWards)
    public String editWard(Request request, Response response) {
        final Ward ward;
        if ((ward = getSelectedWard(request, false)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }
        return editWardRequest(request, ward, null);
    }

    @Action(path = "/Update", method = HttpMethod.post, permission = AclPermission.WriteWards)
    public String updateWard(Request request, Response response) {
        final Model model;
        final Ward ward;

        if (!validatePostData(request, Ward.class, ValidationStage.Update)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            model.put("booleans", Bool.VALUES);
            return renderView("wards/edit.html", model);
        }

        if ((ward = getSelectedWard(request, true)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (!ward.getCode().equalsIgnoreCase(requestAttribute("code", request))) {
            if (wardService.isWardCodeInUse(requestAttribute("code", request))) {
                setSessionErrorMessage("Selected code is already in use", request);
                model = createModel(request);
                copyErrorListToModel(model, request);
                copyRawPostDataToModel(model, request);
                model.put("booleans", Bool.VALUES);
                return renderView("wards/edit.html", model);
            }
            ward.setCode(requestAttribute("code", request));
        }

        ward.setActive(requestAttribute("active", request));
        ward.setName(requestAttribute("name", request));

        wardService.updateWard(ward);
        getService(AuditService.class).log(getCurrentUser(request) + " updated ward " + ward, request);

        setSessionSuccessMessage("Ward updated successfully", request);
        return temporaryRedirect(getBaseUrl(), response);
    }
}