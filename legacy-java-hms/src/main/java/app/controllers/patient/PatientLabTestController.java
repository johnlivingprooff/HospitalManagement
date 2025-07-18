package app.controllers.patient;

import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.lab.LabTest;
import app.models.patient.Patient;
import app.models.permission.AclPermission;
import app.services.lab.LabService;
import app.util.LocaleUtil;
import spark.Request;
import spark.Response;

import java.io.File;

@RouteController(path = "/Hms/Patients/:id/LabTests")
public class PatientLabTestController extends PatientInfoBaseController {

    @Inject
    private LabService labService;

    private LabTest getSelectedLabTest(Request request, Patient patient) {
        Long id;
        LabTest labTest;

        if ((id = getNumericQueryParameter(request, "test-id", Long.class)) != null) {
            if ((labTest = labService.getPatientLabTest(id, patient.getId())) != null) {
                return labTest;
            }
        }
        setSessionErrorMessage("Selected lab test does not exist.", request);
        return null;
    }

    @Action(path = "/", permission = AclPermission.ViewPatientLabTests)
    public String getPatientNok(Request request, Response response) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        model = createModel(request);
        model.put("patient", patient);
        model.put("type", patient.getType());
        model.put("tests", labService.getPatientLabTests(patient.getId()));
        return renderView("patient/lab/list.html", model);
    }

    @Action(path = "/:test-id/Details", permission = AclPermission.ViewPatientLabTests)
    public String getTestDetails(Request request, Response response) {
        Model model;
        Patient patient;
        LabTest labTest;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if ((labTest = getSelectedLabTest(request, patient)) != null) {
            model = createModel(request);
            model.put("test", labTest);
            model.put("type", patient.getType());
            model.put("patient", patient);
            return renderView("patient/lab/details.html", model);
        }

        setSessionErrorMessage("Lab test results not found.", request);
        return redirectToPatientList(response, patient.getType());
    }

    @Action(path = "/:test-id/Attachment", permission = AclPermission.ViewPatientLabTests)
    public Object downloadAttachment(Request request, Response response) {
        File file;
        Patient patient;
        LabTest labTest;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }


        if ((labTest = getSelectedLabTest(request, patient)) != null) {
            if (labTest.getAttachment() != null) {
                file = new File(getAttachmentDirectory(), labTest.getAttachment());
                if (file.exists()) {
                    return serveFile(response, file, format("%s_lab_test%s",
                            labTest.getPatientName(), LocaleUtil.getFileExtensionWithPeriod(file.getName())));
                }
            }
            setSessionErrorMessage("Attachment does not exist.", request);
            return temporaryRedirect(withBaseUrl(Long.toString(labTest.getId()), "Details"), response);
        }
        return temporaryRedirect(makePath("/Hms/Patients", Long.toString(patient.getId()), "LabTests"), response);
    }
}
