package app.controllers.bed;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.bed.Bed;
import app.models.permission.AclPermission;
import app.models.ward.Ward;
import app.services.audit.AuditService;
import app.services.bed.BedService;
import app.services.ward.WardService;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;


@RouteController(path = "/Hms/Beds")
public class BedController extends Controller {
    @Inject
    private BedService bedService;

    @Inject
    private WardService wardService;

    private String getBeds(Request request, boolean all, boolean vacant) {
        Model model;
        model = createModel(request);
        if (all) {
            model.put("beds", bedService.getAllBeds());
        } else {
            if (vacant) {
                model.put("beds", bedService.getVacantBeds());
            } else {
                model.put("beds", bedService.getOccupiedBeds());
            }
        }
        return renderView("beds/list.html", model);
    }

    @Action(path = "/", permission = AclPermission.ReadBeds)
    public String getAllBeds(Request request, Response response) {
        return getBeds(request, true, false);
    }

    private String newBedRequest(Request request, Model model) {
        if (model == null) {
            model = createModel(request);
        }
        model.put("wards", wardService.getActiveWards());
        return renderView("beds/new.html", model);
    }

    @Action(path = "/New", permission = AclPermission.WriteBeds)
    public String newBed(Request request, Response response) {
        return newBedRequest(request, null);
    }

    private Ward getSelectedWard(Request request) {
        final Long id;
        final Ward ward;

        if ((id = requestAttribute("wardId", request)) != null) {
            if ((ward = wardService.findWardById(id)) != null && ward.isActive()) {
                return ward;
            }
        }

        setSessionErrorMessage("Selected ward does not exist", request);
        return null;
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteBeds)
    public String addBed(Request request, Response response) {
        final Bed bed;
        final Ward ward;
        final Model model;

        if (!validatePostData(request, Bed.class, ValidationStage.Create)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newBedRequest(request, model);
        }

        if ((ward = getSelectedWard(request)) == null) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newBedRequest(request, model);
        }

        if (bedService.isBedCodeInUse(requestAttribute("code", request))) {
            setSessionErrorMessage("Selected code is already in use", request);
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newBedRequest(request, model);
        }

        bed = new Bed();
        copyValidatedData(request, bed, ValidationStage.Create);

        bedService.addBed(bed);
        getService(AuditService.class).log(getCurrentUser(request) + " added bed " + bed + " to ward " + ward, request);

        setSessionSuccessMessage("Bed successfully added", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    private Bed getSelectedBed(Request request, boolean isPostRequest) {
        final Long id;
        final Bed bed;

        if (isPostRequest) {
            id = requestAttribute("id", request);
        } else {
            id = getNumericQueryParameter(request, "id", Long.class);
        }

        if (id != null) {
            if ((bed = bedService.getBedById(id)) != null) {
                return bed;
            }
        }

        setSessionErrorMessage("Selected bed does not exist", request);
        return null;
    }

    private String editBedRequest(Request request, Bed bed) {
        Model model;
        model = createModel(request);
        model.put("id", bed.getId());
        model.put("code", bed.getCode());
        model.put("wardId", bed.getWardId());
        model.put("wards", wardService.getActiveWards());
        return renderView("beds/edit.html", model);
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WriteBeds)
    public String editBed(Request request, Response response) {
        final Bed bed;
        if ((bed = getSelectedBed(request, false)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }
        return editBedRequest(request, bed);
    }

    @Action(path = "/:id/Delete", permission = AclPermission.WriteBeds)
    public String deleteBed(Request request, Response response) {
        final Bed bed;

        if ((bed = getSelectedBed(request, false)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }
        if (bed.isVacant()) {
            bedService.deleteBed(bed);
            setSessionSuccessMessage("Bed deleted", request);
            getService(AuditService.class).log(getCurrentUser(request)
                    + " deleted bed " + bed + " from ward " + bed.getWard(), request);
        } else {
            setSessionErrorMessage("Cannot delete this bed because it is currently occupied", request);
        }
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/Update", method = HttpMethod.post, permission = AclPermission.WriteBeds)
    public String updateBed(Request request, Response response) {
        final Bed bed;
        final Ward ward;
        final Model model;

        if (!validatePostData(request, Bed.class, ValidationStage.Update)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            model.put("wards", wardService.getActiveWards());
            return renderView("beds/edit.html");
        }

        if ((bed = getSelectedBed(request, true)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if ((ward = getSelectedWard(request)) == null) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            model.put("wards", wardService.getActiveWards());
            return renderView("beds/edit.html");
        }

        if (!bed.getCode().equalsIgnoreCase(requestAttribute("code", request))) {
            if (bedService.isBedCodeInUse(requestAttribute("code", request))) {
                setSessionErrorMessage("Selected code is already in use", request);
                model = createModel(request);
                copyErrorListToModel(model, request);
                copyRawPostDataToModel(model, request);
                return newBedRequest(request, model);
            }
            bed.setCode(requestAttribute("code", request));
        }

        bed.setWardId(ward.getId());
        bedService.updateBed(bed);
        getService(AuditService.class).log(getCurrentUser(request) + " updated bed " + bed, request);

        setSessionSuccessMessage("Updated successfully", request);
        return temporaryRedirect(getBaseUrl(), response);
    }
}