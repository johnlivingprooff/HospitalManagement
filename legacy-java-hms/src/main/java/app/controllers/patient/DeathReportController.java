package app.controllers.patient;

import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.patient.DeathReport;
import app.models.patient.Patient;
import app.models.patient.PatientStatus;
import app.models.permission.AclPermission;
import app.services.patient.PatientService;
import app.util.LocaleUtil;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.io.File;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@RouteController(path = "/Hms/Patients/:id/Death")
public class DeathReportController extends PatientInfoBaseController {

    private static final List<String> types = new LinkedList<>() {{
        add("application/pdf");
    }};

    @Inject
    private PatientService patientService;

    @Action(path = "/", permission = AclPermission.ReadPatientDeathReport)
    public String getPatientDeathReport(Request request, Response response) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        model = createModel(request);
        model.put("patient", patient);
        model.put("type", patient.getType());
        model.put("report", patientService.getPatientDeathReport(patient));
        return renderView("patient/death/report.html", model);
    }

    private String newPatientDeathReportView(Request request, Patient patient, Model model) {
        if (model == null) {
            model = createModel(request);
        }
        model.put("patient", patient);
        model.put("type", patient.getType());
        return renderView("patient/death/new.html", model);
    }

    @Action(path = "/Record", permission = AclPermission.WritePatientDeathReport)
    public String newPatientDeathReport(Request request, Response response) {
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if (patient.getStatus() == PatientStatus.Expired) {
            setSessionErrorMessage("Cannot add report because this patient is already expired.", request);
            return redirectToPatientList(response);
        }

        return newPatientDeathReportView(request, patient, null);
    }

    @Action(path = "/Report", method = HttpMethod.post, permission = AclPermission.WritePatientDeathReport)
    public String addPatientDeathReport(Request request, Response response) {
        Model model;
        Patient patient;
        DeathReport report;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if (patient.getStatus() == PatientStatus.Expired) {
            setSessionErrorMessage("Cannot add report because this patient is already expired.", request);
            return redirectToPatientList(response);
        }

        if (!validatePostData(request, DeathReport.class, ValidationStage.Create)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newPatientDeathReportView(request, patient, model);
        }


        if (!handleUploadedFile("attachment", "Document", getAttachmentDirectory(), true, request, types)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newPatientDeathReportView(request, patient, model);
        }

        report = new DeathReport();
        copyValidatedData(request, report, ValidationStage.Create);

        report.setDod(LocalDateTime.of(report.getDate(), report.getTime()));
        report.setCreatedBy(getCurrentUser(request).getId());
        report.setAttachment(getUploadedFile("attachment", request).getName());
        report.setCreatedAt(LocalDateTime.now());
        report.setPatientId(patient.getId());

        patientService.addPatientDeathReport(report);

        setSessionSuccessMessage("Death report added.", request);
        return redirectToPatientSubSection(response, patient, "Death");
    }

    @Action(path = "/Download", permission = AclPermission.ReadPatientDeathReport)
    public Object downloadAttachment(Request request, Response response) {
        File file;
        Patient patient;
        DeathReport report;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if (patient.getStatus() != PatientStatus.Expired) {
            setSessionErrorMessage("This patient does not have a death report.", request);
            return redirectToPatientSubSection(response, patient, "Death");
        }

        if ((report = patientService.getPatientDeathReport(patient)) != null) {
            if (report.getAttachment() != null) {
                file = new File(getAttachmentDirectory(), report.getAttachment());
                if (file.exists()) {
                    return serveFile(response, file, format("%s_death-report%s",
                            patient.fullname(), LocaleUtil.getFileExtensionWithPeriod(file.getName()))
                    );
                }
            }
        }
        setSessionErrorMessage("Attachment does not exist.", request);
        return redirectToPatientSubSection(response, patient, "Death");
    }
}
