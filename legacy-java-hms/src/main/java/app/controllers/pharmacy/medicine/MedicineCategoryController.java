package app.controllers.pharmacy.medicine;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.permission.AclPermission;
import app.models.pharmacy.medicine.MedicineCategory;
import app.services.audit.AuditService;
import app.services.pharmacy.PharmacyService;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

@RouteController(path = "/Hms/MedicineCategories")
public final class MedicineCategoryController extends Controller {

    @Inject
    private PharmacyService pharmacyService;

    @Inject
    private AuditService auditService;

    @Action(path = "/", permission = AclPermission.ReadMedicineCategories)
    public String getCategories(Request request, Response response) {
        Model model;

        model = createModel(request);
        model.put("categories", pharmacyService.getMedicineCategories());
        return renderView("pharmacy/medicine/categories/list.html", model);
    }

    private MedicineCategory getSelectedCategory(Request request) {
        String message;
        ValidationResults results;
        MedicineCategory category;

        results = validate(MedicineCategory.class,
                Options.defaults().map(false).context(MedicineCategory.CONTEXT_FIND), request);

        if (results.success()) {
            if ((category = pharmacyService.getMedicineCategoryById((long) results.getResults().get("id"))) != null) {
                return category;
            } else {
                message = "Selected category does not exist.";
            }
        } else {
            message = results.getViolations().get(0);
        }

        setSessionErrorMessage(message, request);
        return null;
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WriteMedicineCategories)
    public String getCategory(Request request, Response response) {
        Model model;
        MedicineCategory category;

        if ((category = getSelectedCategory(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        model = createModel(request);
        model.put("id", category.getId());
        model.put("name", category.getName());
        return renderView("pharmacy/medicine/categories/edit.html", model);
    }

    @Action(path = "/:id/Delete", permission = AclPermission.WriteMedicineCategories)
    public String deleteCategory(Request request, Response response) {
        MedicineCategory category;

        if ((category = getSelectedCategory(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        category.setDeleted(true);
        pharmacyService.updateMedicineCategory(category);
        auditService.log(format("%s deleted medicine category %s.", getCurrentUser(request), category), request);
        setSessionSuccessMessage("Category deleted successfully!", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/New", permission = AclPermission.WriteMedicineCategories)
    public String newCategory(Request request, Response response) {
        Model model;
        model = createModel(request);
        return renderView("pharmacy/medicine/categories/new.html", model);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteMedicineCategories)
    public String addCategory(Request request, Response response) {
        Model model;
        MedicineCategory category;
        ValidationResults results;

        results = validate(MedicineCategory.class, Options.defaults(), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderView("pharmacy/medicine/categories/new.html", model);
        }

        category = results.getBean();

        pharmacyService.addMedicineCategory(category);

        auditService.log(format("%s added medicine category %s.", getCurrentUser(request), category), request);

        setSessionSuccessMessage("Category added successfully!", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/Update", method = HttpMethod.post, permission = AclPermission.WriteMedicineCategories)
    public String updateCategory(Request request, Response response) {
        Model model;
        MedicineCategory category;
        ValidationResults results;

        results = validate(MedicineCategory.class, Options.defaults().map(false).context(1), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderView("pharmacy/medicine/categories/edit.html", model);
        }

        category = pharmacyService.getMedicineCategoryById((long) results.getResults().get("id"));

        if (category == null) {
            setSessionErrorMessage("Selected category does not exist.", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        category.setName((String) results.getResults().get("name"));

        pharmacyService.updateMedicineCategory(category);

        auditService.log(format("%s updated medicine category %s.", getCurrentUser(request), category), request);

        setSessionSuccessMessage("Category updated successfully!", request);
        return temporaryRedirect(getBaseUrl(), response);
    }
}
