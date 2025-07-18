package app.controllers.pharmacy.medicine;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.Contexts;
import app.models.permission.AclPermission;
import app.models.pharmacy.medicine.Medicine;
import app.models.pharmacy.medicine.MedicineCategory;
import app.models.pharmacy.medicine.StockAlertConfiguration;
import app.services.audit.AuditService;
import app.services.pharmacy.PharmacyService;
import app.types.Bool;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.time.LocalDateTime;
import java.util.List;

@RouteController(path = "/Hms/Medicines")
public class MedicineController extends Controller {
    @Inject
    private PharmacyService pharmacyService;

    @Inject
    private AuditService auditService;

    private String showList(Request request, List<Medicine> medicineList, String title) {
        Model model;

        model = createModel(request);
        model.put("listTitle", title);
        model.put("medicines", medicineList);
        return renderView("pharmacy/medicine/list.html", model);
    }

    @Action(path = "/", permission = AclPermission.ReadMedicines)
    public String getMedicines(Request request, Response response) {
        return showList(request, pharmacyService.getMedicines(), "Medicines");
    }

    @Action(path = "/LowStock", permission = AclPermission.ReadMedicines)
    public String getLowStockMedicines(Request request, Response response) {
        return showList(request, pharmacyService.getLowStockMedicines(), "Low Stock Medicines");
    }

    @Action(path = "/Expiring", permission = AclPermission.ReadMedicines)
    public String getExpiringMedicines(Request request, Response response) {
        return showList(request, pharmacyService.getExpiringMedicines(), "Expiring Medicines");
    }

    private Medicine getSelectedMedicine(Request request) {
        String message;
        ValidationResults results;
        Medicine medicine;

        results = validate(Medicine.class,
                Options.defaults().map(false).context(Contexts.FIND), request);

        if (results.success()) {
            if ((medicine = pharmacyService.getMedicineById((long) results.getResults().get("id"))) != null) {
                return medicine;
            } else {
                message = "Selected medicine does not exist.";
            }
        } else {
            message = results.getViolations().get(0);
        }

        setSessionErrorMessage(message, request);
        return null;
    }

    private MedicineCategory getSelectedCategory(long id, Request request) {
        MedicineCategory category;
        if ((category = pharmacyService.getMedicineCategoryById(id)) != null) {
            return category;
        }
        setSessionErrorMessage("Selected category does not exist.", request);
        return null;
    }

    @Action(path = "/Alerts", permission = AclPermission.ReadMedicines)
    public String alertsAndNotifications(Request request, Response response) {
        Model model;
        StockAlertConfiguration config;

        config = pharmacyService.getStockAlertConfiguration();

        model = createModel(request);
        model.put("days", config.getDays());
        model.put("notificationEmail", config.getNotificationEmail());
        model.put("notifyExpiration", config.isNotifyExpiration());
        model.put("notifyStockLevel", config.isNotifyStockLevel());
        model.put("booleans", Bool.VALUES);
        return renderView("pharmacy/medicine/alerts.html", model);
    }

    @Action(path = "/Alerts/Update", method = HttpMethod.post, permission = AclPermission.WriteMedicines)
    public String updateAlertsAndNotifications(Request request, Response response) {
        Model model;
        ValidationResults results;
        StockAlertConfiguration config;

        results = validate(StockAlertConfiguration.class, Options.defaults().sticky(true).map(false), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            model.put("booleans", Bool.VALUES);
            return renderView("pharmacy/medicine/alerts.html", model);
        }

        results.updateBean(config = new StockAlertConfiguration());

        pharmacyService.updateSocketAlertConfiguration(config);

        setSessionSuccessMessage("Settings saved!", request);
        return temporaryRedirect(getBaseUrl() + "/Alerts", response);
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WriteMedicines)
    public String getMedicine(Request request, Response response) {
        Model model;
        Medicine medicine;

        if ((medicine = getSelectedMedicine(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        model = createModel(request);
        model.put("id", medicine.getId());
        model.put("name", medicine.getName());
        model.put("expires", medicine.getExpires());
        model.put("quantity", medicine.getQuantity());
        model.put("category", medicine.getCategory());
        model.put("location", medicine.getLocation());
        model.put("threshold", medicine.getThreshold());
        model.put("genericName", medicine.getGenericName());
        model.put("sellingPrice", medicine.getSellingPrice());
        model.put("purchasePrice", medicine.getPurchasePrice());
        model.put("locations", pharmacyService.getMedicineLocations());
        model.put("categories", pharmacyService.getMedicineCategories());
        return renderView("pharmacy/medicine/edit.html", model);
    }

    @Action(path = "/:id/Delete", permission = AclPermission.WriteMedicines)
    public String deleteMedicine(Request request, Response response) {
        Medicine medicine;

        if ((medicine = getSelectedMedicine(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        medicine.setDeleted(true);
        pharmacyService.updateMedicine(medicine);
        auditService.log(format("%s deleted medicine %s.", getCurrentUser(request), medicine), request);
        setSessionSuccessMessage("Medicine deleted successfully!", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/New", permission = AclPermission.WriteMedicines)
    public String newMedicine(Request request, Response response) {
        Model model;

        model = createModel(request);
        model.put("categories", pharmacyService.getMedicineCategories());
        model.put("locations", pharmacyService.getMedicineLocations());
        return renderView("pharmacy/medicine/new.html", model);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteMedicines)
    public String addMedicine(Request request, Response response) {
        Model model;
        Medicine medicine;
        ValidationResults results;
        MedicineCategory category;

        results = validate(Medicine.class, Options.defaults().context(Contexts.CREATE), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            model.put("categories", pharmacyService.getMedicineCategories());
            model.put("locations", pharmacyService.getMedicineLocations());
            return renderView("pharmacy/medicine/new.html", model);
        }

        medicine = results.getBean();

        if ((category = getSelectedCategory(medicine.getCategory(), request)) == null) {
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            model.put("locations", pharmacyService.getMedicineLocations());
            model.put("categories", pharmacyService.getMedicineCategories());
            return renderView("pharmacy/medicine/new.html", model);
        }

        medicine.setUpdated(LocalDateTime.now());
        pharmacyService.addMedicine(medicine);

        auditService.log(format("%s added medicine %s.", getCurrentUser(request), medicine), request);
        setSessionSuccessMessage("Medicine added successfully!", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/Update", method = HttpMethod.post, permission = AclPermission.WriteMedicines)
    public String updateMedicine(Request request, Response response) {
        Model model;
        Medicine medicine;
        ValidationResults results;

        results = validate(Medicine.class, Options.defaults().map(false).context(Contexts.UPDATE), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            model.put("locations", pharmacyService.getMedicineLocations());
            model.put("categories", pharmacyService.getMedicineCategories());
            return renderView("pharmacy/medicine/edit.html", model);
        }

        medicine = pharmacyService.getMedicineById((long) results.getResults().get("id"));

        if (medicine == null) {
            setSessionErrorMessage("Selected medicine does not exist.", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        medicine.setUpdated(LocalDateTime.now());
        results.updateBean(medicine);

        if (getSelectedCategory(medicine.getCategory(), request) == null) {
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            model.put("locations", pharmacyService.getMedicineLocations());
            model.put("categories", pharmacyService.getMedicineCategories());
            return renderView("pharmacy/medicine/new.html", model);
        }

        pharmacyService.updateMedicine(medicine);
        auditService.log(format("%s updated medicine %s.", getCurrentUser(request), medicine), request);

        setSessionSuccessMessage("Medicine updated successfully!", request);
        return temporaryRedirect(getBaseUrl(), response);
    }
}
