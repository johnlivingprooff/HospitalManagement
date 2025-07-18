package app.controllers.patient;

import app.core.annotations.Action;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.patient.Insurance;
import app.models.patient.Patient;
import app.models.permission.AclPermission;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.time.LocalDateTime;

@RouteController(path = "/Hms/Patients/:id/Insurance")
public class InsuranceController extends PatientInfoBaseController {

    @Action(path = "/", permission = AclPermission.ReadPatientInsurance)
    public String getPatientInsurance(Request request, Response response) {
        Model model;
        Patient patient;
        Insurance insurance;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        insurance = patientService.getInsurance(patient);

        model = createModel(request);
        model.put("insurance", insurance);
        model.put("patient", patient);
        model.put("type", patient.getType());
        return renderView("patient/insurance/view.html", model);
    }

    @Action(path = "/Edit", permission = AclPermission.WritePatientInsurance)
    public String editPatientInsurance(Request request, Response response) {
        Model model;
        Patient patient;
        Insurance insurance;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        insurance = patientService.getInsurance(patient);

        model = createModel(request);
        model.put("type", patient.getType());
        model.put("patient", patient);

        if (insurance != null) {
            copyEditableFieldsToModel(insurance, model);
        }
        return renderView("patient/insurance/edit.html", model);
    }

    @Action(path = "/Update", permission = AclPermission.WritePatientInsurance, method = HttpMethod.post)
    public String updatePatientNok(Request request, Response response) {
        Model model;
        Patient patient;
        Insurance insurance;
        LocalDateTime modified;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if (!validatePostData(request, Insurance.class, ValidationStage.Update)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            model.put("type", patient.getType());
            model.put("patient", patient);
            copyRawPostDataToModel(model, request);
            return renderView("patient/insurance/edit.html", model);
        }

        insurance = new Insurance();
        modified = LocalDateTime.now();
        copyValidatedData(request, insurance, ValidationStage.Update);

        insurance.setCreated(modified);
        insurance.setModified(modified);
        insurance.setPatientId(patient.getId());

        patientService.updateInsurance(insurance);
        setSessionSuccessMessage("Insurance details updated.", request);
        return temporaryRedirect(makePath("/Hms/Patients", Long.toString(patient.getId()), "Insurance"), response);
    }
}
