package app.controllers.patient;

import app.core.annotations.Action;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.account.Sex;
import app.models.patient.Birth;
import app.models.patient.Patient;
import app.models.patient.TemperatureUnits;
import app.models.permission.AclPermission;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.time.LocalDateTime;

@RouteController(path = "/Hms/Patients/:id/Births")
public class PatientBirthsController extends PatientInfoBaseController {


    private Birth getSelectedBirth(Request request, Patient patient) {
        Long id;
        Birth birth;

        if ((id = getNumericQueryParameter(request, "birth-id", Long.class)) != null) {
            if ((birth = patientService.getPatientBirth(id, patient.getId())) != null) {
                return birth;
            }
        }

        setSessionErrorMessage("Selected birth record does not exist.", request);
        return null;
    }

    @Action(path = "/", permission = AclPermission.ViewPatientBirths)
    public String getPatientBirths(Request request, Response response) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if (patient.getSex() != Sex.Female) {
            setSessionErrorMessage("This option is only available under female patients only.", request);
            return redirectToPatientList(response);
        }

        model = createModel(request);
        model.put("patient", patient);
        model.put("type", patient.getType());
        model.put("births", patientService.getPatientBirths(patient));
        return renderView("patient/births/list.html", model);
    }

    private String newPatientBirthView(Request request, Patient patient, Model model) {
        if (model == null) {
            model = createModel(request);
        }

        model.put("patient", patient);
        model.put("type", patient.getType());
        model.put("units", TemperatureUnits.UNITS);
        model.put("sexes", Sex.VALUES);
        return renderView("patient/births/new.html", model);
    }

    @Action(path = "/New", permission = AclPermission.WritePatientBirths)
    public String newPatientBirth(Request request, Response response) {
        Patient patient;
        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }
        if (patient.getSex() != Sex.Female) {
            setSessionErrorMessage("This option is only available under female patients only.", request);
            return redirectToPatientList(response);
        }
        return newPatientBirthView(request, patient, null);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WritePatientBirths)
    public String addPatientBirth(Request request, Response response) {
        Model model;
        Birth birth;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if (patient.getSex() != Sex.Female) {
            setSessionErrorMessage("This option is only available under female patients only.", request);
            return redirectToPatientList(response);
        }

        if (!validatePostData(request, Birth.class, ValidationStage.Create)) {
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            copyErrorListToModel(model, request);
            return newPatientBirthView(request, patient, model);
        }

        birth = new Birth();
        copyValidatedData(request, birth, ValidationStage.Create);
        birth.setCreated(LocalDateTime.now());
        birth.setPatientId(patient.getId());
        birth.setDob(LocalDateTime.of(birth.getDate(), birth.getTime()));
        birth.setCreatedBy(getCurrentUser(request).getId());

        patientService.addPatientBirth(birth);

        setSessionSuccessMessage("Birth registered successfully!", request);
        return redirectToPatientSubSection(response, patient, "Births");
    }

    @Action(path = "/:birth-id/Delete", permission = AclPermission.WritePatientBirths)
    public String deleteBirth(Request request, Response response) {
        Birth birth;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if ((birth = getSelectedBirth(request, patient)) != null) {
            patientService.deleteBirth(birth);
            setSessionSuccessMessage("Birth record deleted.", request);
        }
        return redirectToPatientSubSection(response, patient, "Births");
    }
}
