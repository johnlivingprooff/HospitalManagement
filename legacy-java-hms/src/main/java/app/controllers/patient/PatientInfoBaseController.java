package app.controllers.patient;

import app.core.Controller;
import app.core.annotations.Inject;
import app.models.patient.Patient;
import app.models.patient.PatientType;
import app.services.patient.PatientService;
import spark.Request;
import spark.Response;

public abstract class PatientInfoBaseController extends Controller {

    interface ResourceResolver<T> {
        T get(long id);
    }

    protected
    @Inject
    PatientService patientService;

    protected String redirectToPatientList(Response response) {
        return redirectToPatientList(response, PatientType.Inpatient);
    }

    protected final <T> T getPatientResourceById(Request request, String id, ResourceResolver<T> resolver) {
        T resource;
        Long itemId;
        resource = null;
        if ((itemId = getNumericQueryParameter(request, id, Long.class)) != null) {
            resource = resolver.get(itemId);
        }
        return resource;
    }

    protected String redirectToPatientList(Response response, PatientType type) {
        String path;
        if (type == PatientType.Inpatient) {
            path = "Inpatient";
        } else {
            path = "Outpatient";
        }
        return temporaryRedirect(makePath("/Hms/Patients", path), response);
    }

    protected String redirectToPatientSubSection(Response response, Patient patient, String path) {
        return temporaryRedirect(makePath("/Hms/Patients", Long.toString(patient.getId()), path), response);
    }

    protected Patient getSelectedPatient(Request request) {
        Long id;
        Patient patient;
        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((patient = patientService.getPatientByAccountId(id)) != null) {
                return patient;
            }
        }
        setSessionErrorMessage("Selected patient does not exist.", request);
        return null;
    }
}
