package app.controllers.patient;

import app.core.annotations.Action;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.patient.Patient;
import app.models.patient.PatientNextOfKin;
import app.models.patient.RelationshipType;
import app.models.permission.AclPermission;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.time.LocalDateTime;

@RouteController(path = "/Hms/Patients/:id/Nok")
public class PatientNokController extends PatientInfoBaseController {

    @Action(path = "/", permission = AclPermission.ReadPatientNok)
    public String getPatientNok(Request request, Response response) {
        Model model;
        Patient patient;
        PatientNextOfKin nok;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        nok = patientService.getNextOfKin(patient);

        model = createModel(request);
        model.put("nok", nok);
        model.put("patient", patient);
        model.put("type", patient.getType());
        return renderView("patient/nok/view.html", model);
    }

    @Action(path = "/Edit", permission = AclPermission.WritePatientNok)
    public String editPatientNok(Request request, Response response) {
        Model model;
        Patient patient;
        PatientNextOfKin nok;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        nok = patientService.getNextOfKin(patient);

        model = createModel(request);
        model.put("relationships", RelationshipType.VALUES);
        model.put("type", patient.getType());
        model.put("patient", patient);

        if (nok != null) {
            copyEditableFieldsToModel(nok, model);
        }
        return renderView("patient/nok/edit.html", model);
    }

    @Action(path = "/Update", permission = AclPermission.WritePatientNok, method = HttpMethod.post)
    public String updatePatientNok(Request request, Response response) {
        Model model;
        Patient patient;
        PatientNextOfKin nok;
        LocalDateTime modified;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if (!validatePostData(request, PatientNextOfKin.class, ValidationStage.Update)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            model.put("type", patient.getType());
            model.put("patient", patient);
            model.put("relationships", RelationshipType.VALUES);
            copyRawPostDataToModel(model, request);
            return renderView("patient/nok/edit.html", model);
        }

        nok = new PatientNextOfKin();
        modified = LocalDateTime.now();
        copyValidatedData(request, nok, ValidationStage.Update);

        nok.setCreated(modified);
        nok.setModified(modified);
        nok.setPatientId(patient.getId());

        patientService.updateNextOfKin(nok);
        setSessionSuccessMessage("Next of kin details updated.", request);
        return temporaryRedirect(makePath("/Hms/Patients", Long.toString(patient.getId()), "Nok"), response);
    }
}
