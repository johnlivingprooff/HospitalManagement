package app.controllers.admissions;


import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.account.Account;
import app.models.admissions.Admission;
import app.models.admissions.AdmissionStatus;
import app.models.patient.TemperatureUnits;
import app.models.patient.Vitals;
import app.models.patient.VitalsType;
import app.models.permission.AclPermission;
import app.services.admissions.AdmissionsService;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.time.LocalDateTime;

@RouteController(path = "/Hms/Admissions/:id/Vitals")
public class AdmissionVitalsController extends Controller {
    @Inject
    private AdmissionsService admissionsService;

    private Admission getSelectedAdmission(Request request) {
        Long id;
        Admission admission;
        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((admission = admissionsService.getAdmissionById(id)) != null) {
                return admission;
            }
        }
        setSessionErrorMessage("Selected admission does not exist.", request);
        return null;
    }

    private String getReturnUrl(Admission admission) {
        return "/Hms/Admissions" + (admission.getStatus() == AdmissionStatus.Discharged ? "/Terminated" : "");
    }

    private String returnToAdmissions(Response response) {
        return temporaryRedirect("/Hms/Admissions", response);
    }

    @Action(path = "/", permission = AclPermission.WriteAdmissions)
    public String getAdmissionPatientVitals(Request request, Response response) {
        Model model;
        Admission admission;

        if ((admission = getSelectedAdmission(request)) == null) {
            return returnToAdmissions(response);
        }

        model = createModel(request);
        model.put("admission", admission);
        model.put("returnUrl", getReturnUrl(admission));
        model.put("vitals", admissionsService.getVitals(admission));
        return renderView("admissions/vitals/list.html", model);
    }

    @Action(path = "/New", permission = AclPermission.WriteAdmissions)
    public String newAdmissionPatientVitals(Request request, Response response) {
        Model model;
        Admission admission;

        if ((admission = getSelectedAdmission(request)) == null) {
            return returnToAdmissions(response);
        }

        if (admission.getStatus() == AdmissionStatus.Discharged) {
            setSessionErrorMessage("Cannot record vitals under a terminated admission.", request);
            return returnToAdmissions(response);
        }

        model = createModel(request);
        model.put("admission", admission);
        model.put("returnUrl", getReturnUrl(admission));
        model.put("vitals", admissionsService.getVitals(admission));
        model.put("units", TemperatureUnits.UNITS);
        return renderView("admissions/vitals/new.html", model);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteAdmissions)
    public String addAdmissionPatientVitals(Request request, Response response) {
        Model model;
        Vitals vitals;
        Account account;
        Admission admission;

        if ((admission = getSelectedAdmission(request)) == null) {
            return returnToAdmissions(response);
        }

        if (admission.getStatus() == AdmissionStatus.Discharged) {
            setSessionErrorMessage("Cannot record vitals under a terminated admission.", request);
            return returnToAdmissions(response);
        }

        if (!validatePostData(request, Vitals.class, ValidationStage.Create)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            model.put("admission", admission);
            model.put("returnUrl", getReturnUrl(admission));
            model.put("units", TemperatureUnits.UNITS);
            return renderView("admissions/vitals/new.html", model);
        }

        account = getCurrentUser(request);

        vitals = new Vitals();
        vitals.setAdmissionId(admission.getId());
        vitals.setPatientId(admission.getPatientId());
        vitals.setType(VitalsType.Admission);
        vitals.setCreatedBy(account.getId());
        vitals.setCreated(LocalDateTime.now());

        copyValidatedData(request, vitals, ValidationStage.Create);

        admissionsService.addVitals(vitals);

        setSessionSuccessMessage("Vital signs added successfully.", request);
        return temporaryRedirect(makePath("/Hms/Admissions", Long.toString(admission.getId()), "Vitals"), response);
    }
}
