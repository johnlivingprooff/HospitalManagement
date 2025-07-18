package app.controllers.nurse;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.nurse.VitalsFee;
import app.models.patient.*;
import app.models.permission.AclPermission;
import app.services.nurse.NurseService;
import app.services.patient.PatientService;
import app.services.user.AccountService;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.time.LocalDateTime;

@RouteController(path = "/Hms/NurseStation")
public final class NurseStationController extends Controller {

    @Inject
    private AccountService accountService;

    @Inject
    private NurseService nurseService;

    @Inject
    private PatientService patientService;

    @Action(path = "/Vitals/Fees", permission = AclPermission.AccessNursesStation)
    public String getVitalsFeesForm(Request request, Response response) {
        Model model;

        model = createModel(request);
        copyEditableFieldsToModel(nurseService.getVitalsFee(), model);
        return renderView("nurses/vitals/fee.html", model);
    }

    @Action(path = "/Vitals/Fee/Update", method = HttpMethod.post, permission = AclPermission.AccessNursesStation)
    public String saveVitalsFeesForm(Request request, Response response) {
        Model model;
        VitalsFee fee;
        boolean validated;

        validated = validatePostData(request, VitalsFee.class, ValidationStage.Create);

        if (!validated) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return renderView("nurses/vitals/fee.html", model);
        }

        fee = new VitalsFee();
        setSessionSuccessMessage("Fees updated", request);
        copyValidatedData(request, fee, ValidationStage.Create);
        fee.setUpdatedAt(LocalDateTime.now());
        nurseService.updateVitalsFee(fee);
        return temporaryRedirect(withBaseUrl("Vitals/Fees"), response);
    }

    @Action(path = "/Vitals/New", permission = AclPermission.AccessNursesStation)
    public String newVitals(Request request, Response response) {
        return newVitalsView(request, null);
    }

    private String newVitalsView(Request request, Model model) {
        if (model == null) {
            model = createModel(request);
        }
        model.put("units", TemperatureUnits.UNITS);
        return renderView("nurses/vitals/new.html", model);
    }

    @Action(path = "/Vitals/Add", method = HttpMethod.post, permission = AclPermission.AccessNursesStation)
    public String recordVitals(Request request, Response response) {
        Model model;
        PatientVitals vitals;
        PatientInfo patientInfo;

        if (!validatePostData(request, PatientVitals.class, ValidationStage.Create)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newVitalsView(request, model);
        }

        if ((patientInfo = patientService.findPatientInfoByMrn(requestAttribute("mrn", request))) == null) {
            setSessionErrorMessage("Cannot record vitals for this patient at this time.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newVitalsView(request, model);
        }

        if (patientInfo.getStatus() == PatientStatus.Expired) {
            setSessionErrorMessage("Cannot record vitals for this patient at this time.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newVitalsView(request, model);
        }

        vitals = new PatientVitals();
        copyValidatedData(request, vitals, ValidationStage.Create);

        vitals.setCreated(LocalDateTime.now());
        vitals.setPatientId(patientInfo.getId());
        vitals.setCreatedBy(getCurrentUser(request).getId());
        vitals.setType(VitalsType.NursesStation);

        nurseService.addVitals(vitals);

        setSessionSuccessMessage("Patient vitals recorded successfully.", request);
        return temporaryRedirect(withBaseUrl("Vitals/New"), response);
    }
}
