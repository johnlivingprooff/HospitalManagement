package app.controllers.pharmacy.medicine;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.Contexts;
import app.models.permission.AclPermission;
import app.models.pharmacy.medicine.MedicineLocation;
import app.services.audit.AuditService;
import app.services.pharmacy.PharmacyService;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

@RouteController(path = "/Hms/MedicineLocations")
public final class MedicineLocationController extends Controller {

    @Inject
    private PharmacyService pharmacyService;

    @Inject
    private AuditService auditService;

    @Action(path = "/", permission = AclPermission.ReadMedicineLocations)
    public String getLocations(Request request, Response response) {
        Model model;

        model = createModel(request);
        model.put("locations", pharmacyService.getMedicineLocations());
        return renderView("pharmacy/medicine/locations/list.html", model);
    }

    private MedicineLocation getSelectedLocation(Request request) {
        String message;
        ValidationResults results;
        MedicineLocation location;

        results = validate(MedicineLocation.class,
                Options.defaults().map(false).context(Contexts.FIND), request);

        if (results.success()) {
            if ((location = pharmacyService.getMedicineLocationById((long) results.getResults().get("id"))) != null) {
                return location;
            } else {
                message = "Selected location does not exist.";
            }
        } else {
            message = results.getViolations().get(0);
        }

        setSessionErrorMessage(message, request);
        return null;
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WriteMedicineLocations)
    public String getLocation(Request request, Response response) {
        Model model;
        MedicineLocation location;

        if ((location = getSelectedLocation(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        model = createModel(request);
        model.put("id", location.getId());
        model.put("name", location.getName());
        return renderView("pharmacy/medicine/locations/edit.html", model);
    }

    @Action(path = "/:id/Delete", permission = AclPermission.WriteMedicineLocations)
    public String deleteLocation(Request request, Response response) {
        MedicineLocation location;

        if ((location = getSelectedLocation(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        location.setDeleted(true);
        pharmacyService.updateMedicineLocation(location);
        auditService.log(format("%s deleted medicine location %s.", getCurrentUser(request), location), request);
        setSessionSuccessMessage("Location deleted successfully!", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/New", permission = AclPermission.WriteMedicineLocations)
    public String newLocation(Request request, Response response) {
        Model model;
        model = createModel(request);
        return renderView("pharmacy/medicine/locations/new.html", model);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteMedicineLocations)
    public String addLocation(Request request, Response response) {
        Model model;
        MedicineLocation location;
        ValidationResults results;

        results = validate(MedicineLocation.class, Options.defaults(), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderView("pharmacy/medicine/locations/new.html", model);
        }

        location = results.getBean();

        pharmacyService.addMedicineLocation(location);

        auditService.log(format("%s added medicine location %s.", getCurrentUser(request), location), request);

        setSessionSuccessMessage("Location added successfully!", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/Update", method = HttpMethod.post, permission = AclPermission.WriteMedicineLocations)
    public String updateLocation(Request request, Response response) {
        Model model;
        MedicineLocation location;
        ValidationResults results;

        results = validate(MedicineLocation.class, Options.defaults().map(false).context(Contexts.UPDATE), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderView("pharmacy/medicine/locations/edit.html", model);
        }

        location = pharmacyService.getMedicineLocationById((long) results.getResults().get("id"));

        if (location == null) {
            setSessionErrorMessage("Selected location does not exist.", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        results.updateBean(location);

        pharmacyService.updateMedicineLocation(location);

        auditService.log(format("%s updated medicine location %s.", getCurrentUser(request), location), request);

        setSessionSuccessMessage("Location updated successfully!", request);
        return temporaryRedirect(getBaseUrl(), response);
    }
}
