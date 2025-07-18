package app.controllers.admissions;


import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.Contexts;
import app.models.ListOption;
import app.models.admissions.Admission;
import app.models.admissions.AdmissionStatus;
import app.models.admissions.AdmissionType;
import app.models.bed.Bed;
import app.models.patient.PatientInfo;
import app.models.permission.AclPermission;
import app.services.admissions.AdmissionsService;
import app.services.audit.AuditService;
import app.services.bed.BedService;
import app.services.patient.PatientService;
import app.services.ward.WardService;
import app.util.LocaleUtil;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.io.File;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.List;

@RouteController(path = "/Hms/Admissions")
public class AdmissionsController extends Controller {
    @Inject
    private AdmissionsService admissionsService;

    @Inject
    private WardService wardService;

    @Inject
    private BedService bedService;

    @Inject
    private PatientService patientService;

    @Inject
    private AuditService auditService;

    @Action(path = "/GetVacantBeds/:ward-id", permission = AclPermission.WriteAdmissions)
    public String getVacantBeds(Request request, Response response) {
        Long wardId;
        List<ListOption> beds;

        if ((wardId = getNumericQueryParameter(request, "ward-id", Long.class)) != null) {
            if (wardService.activeWardExists(wardId)) {
                beds = bedService.getVacantBedsByWardId(wardId);
            } else {
                beds = List.of();
            }
        } else {
            beds = List.of();
        }
        response.status(HttpURLConnection.HTTP_OK);
        response.type("application/json");
        return getGson().toJson(beds);
    }

    private String getAdmissions(Request request, AdmissionStatus status) {
        Model model;
        model = createModel(request);
        model.put("admissions", admissionsService.getAdmissionsByState(status));
        model.put("dischargedAdmissions", status == AdmissionStatus.Discharged);
        switch (status) {
            case Active:
                model.put("title", "Active Admissions");
                break;
            case Discharged:
                model.put("title", "Terminated Admissions");
                break;
        }
        return renderView("admissions/list.html", model);
    }

    @Action(path = "/", permission = AclPermission.ReadAdmissions)
    public String getActiveAdmissions(Request request, Response response) {
        return getAdmissions(request, AdmissionStatus.Active);
    }

    @Action(path = "/Terminated", permission = AclPermission.ReadAdmissions)
    public String getDischargedAdmissions(Request request, Response response) {
        return getAdmissions(request, AdmissionStatus.Discharged);
    }

    private String renderNewAdmissionView(Request request, Model model, Admission admission) {
        if (model == null) {
            model = createModel(request);
            model.put("beds", List.of());
        } else {
            if (admission != null) {
                model.put("beds", bedService.getVacantBedsByWardId(admission.getWardId()));
            } else {
                model.put("beds", List.of());
            }
        }
        model.put("wards", wardService.getActiveWards());
        model.put("admissionTypes", AdmissionType.TYPES);
        return renderView("admissions/new.html", model);
    }

    @Action(path = "/New", permission = AclPermission.WriteAdmissions)
    public String newAdmission(Request request, Response response) {
        return renderNewAdmissionView(request, null, null);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteAdmissions)
    public String createAdmission(Request request, Response response) {
        Bed bed;
        File file;
        Model model;
        Admission overlap;
        Admission admission;
        PatientInfo patientInfo;
        ValidationResults results;

        results = validate(Admission.class, Options.defaults().sticky(true), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderNewAdmissionView(request, model, null);
        }

        admission = results.getBean();

        // get patient
        if ((patientInfo = patientService.findPatientInfoByMrn(admission.getPatientMrn())) == null) {
            setSessionErrorMessage("Selected patient does not exist in the system.", request);
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderNewAdmissionView(request, model, admission);
        }
        if (!patientInfo.isEligibleForAdmission()) {
            setSessionErrorMessage(patientInfo.getAdmissionIneligibilityReason(), request);
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderNewAdmissionView(request, model, admission);
        }

        // Make sure there are no other current admissions
        if (admissionsService.isPatientUnderAdmission(patientInfo.getId())) {
            setSessionErrorMessage("This patient already seems to be under admission.", request);
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderNewAdmissionView(request, model, admission);
        }

        admission.setCreatedAt(LocalDateTime.of(admission.getAdmissionDate(), admission.getAdmissionTime()));
        if (admission.getCreatedAt().isAfter(LocalDateTime.now())) {
            setSessionErrorMessage("Admission date and time cannot be after today.", request);
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderNewAdmissionView(request, model, admission);
        }

        if ((overlap = admissionsService.findOverlappingPatientAdmission(patientInfo.getId(), admission.getCreatedAt())) != null) {
            setSessionErrorMessage(
                    format("Admission time %s overlaps with a previous admission time at %s. This is because the " +
                                    "patient could not have been under two different admissions at the same point time. " +
                                    "Please fix your time input.",
                            LocaleUtil.formatDate(admission.getCreatedAt()),
                            LocaleUtil.formatDate(overlap.getCreatedAt())
                    ),
                    request
            );
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderNewAdmissionView(request, model, admission);
        }

        if ((bed = bedService.getBedById(admission.getBedId())) == null) {
            setSessionErrorMessage("Selected bed does not exist.", request);
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderNewAdmissionView(request, model, admission);
        }

        if (!bed.isVacant()) {
            setSessionErrorMessage("Selected bed is currently occupied.", request);
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderNewAdmissionView(request, model, admission);
        }

        admission.setStatus(AdmissionStatus.Active);
        admission.setPatientId(patientInfo.getId());
        admission.setUpdatedAt(LocalDateTime.now());
        admission.setAdmittedBy(getCurrentUser(request).getId());

        if (!handleUploadedFile("attachment", "Additional attachment", getAttachmentDirectory(), false, request)) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderNewAdmissionView(request, model, admission);
        }

        if ((file = getUploadedFile("attachment", request)) != null) {
            admission.setAttachment(file.getName());
        }

        admissionsService.addAdmission(admission);

        auditService.log(format("%s created admission for patient with id %s.",
                getCurrentUser(request), admission.getPatientMrn()), request);

        setSessionSuccessMessage("Patient admission created!", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

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

    @Action(path = "/:id/Details", permission = AclPermission.ReadAdmissionDetails)
    public String getAdmissionDetails(Request request, Response response) {
        Model model;
        Admission admission;

        model = createModel(request);

        if ((admission = getSelectedAdmission(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }
        switch (admission.getStatus()) {
            case Discharged:
                model.put("returnUrl", getBaseUrl());
                break;
            case Active:
                model.put("returnUrl", withBaseUrl("Terminated"));
                break;
        }
        model.put("admission", admission);
        return renderView("admissions/details.html", model);
    }

    private Object serveAttachment(Request request, Response response, boolean termination) {
        File file;
        String attachment;
        Admission admission;

        if ((admission = getSelectedAdmission(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (termination) {
            attachment = admission.getTerminationAttachment();
        } else {
            attachment = admission.getAttachment();
        }

        if (attachment != null) {
            file = new File(getAttachmentDirectory(), attachment);
            if (file.exists()) {
                return serveFile(response, file, format("%s_%s%s", admission.getPatientName(),
                        (termination ? "Admission_Termination" : "Admission"),
                        LocaleUtil.getFileExtensionWithPeriod(file.getName())));
            }
        }
        setSessionErrorMessage("This attachment does not exist.", request);
        return temporaryRedirect(withBaseUrl(Long.toString(admission.getId()), "Details"), response);
    }

    @Action(path = "/:id/Attachment", permission = AclPermission.ReadAdmissionDetails)
    public Object getAdmissionAttachment(Request request, Response response) {
        return serveAttachment(request, response, false);
    }

    @Action(path = "/:id/TerminationAttachment", permission = AclPermission.ReadAdmissionDetails)
    public Object getTerminationAttachment(Request request, Response response) {
        return serveAttachment(request, response, true);
    }

    @Action(path = "/:id/Terminate", permission = AclPermission.TerminateAdmissions)
    public String getAdmissionForTermination(Request request, Response response) {
        Model model;
        Admission admission;

        model = createModel(request);

        if ((admission = getSelectedAdmission(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        model.put("id", admission.getId());
        return renderView("admissions/terminate.html", model);
    }

    @Action(path = "/Terminate", method = HttpMethod.post, permission = AclPermission.TerminateAdmissions)
    public String terminateAdmission(Request request, Response response) {
        File file;
        Model model;
        LocalDateTime now;
        Admission admission;
        ValidationResults results;

        results = validate(Admission.class,
                Options.defaults().sticky(true).context(Contexts.UPDATE).map(false), request);

        if (!results.success()) {
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            copyErrorListToModel(model, results);
            return renderView("admissions/terminate.html", model);
        }

        if ((admission = admissionsService.getAdmissionById((long) results.getResults().get("id"))) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (admission.getStatus() != AdmissionStatus.Active) {
            setSessionErrorMessage("Cannot terminate this admission at the moment.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return renderView("admissions/terminate.html", model);
        }

        // check times
        results.updateBean(admission);
        admission.setDischargedAt(LocalDateTime.of(admission.getTerminationDate(), admission.getTerminationTime()));

        now = LocalDateTime.now();

        if (admission.getDischargedAt().isAfter(now) || admission.getDischargedAt().isBefore(admission.getCreatedAt())) {
            setSessionErrorMessage(
                    format(
                            "Termination date and time must fall between today and the admission date %s.",
                            LocaleUtil.formatDate(admission.getCreatedAt())
                    ),
                    request
            );
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return renderView("admissions/terminate.html", model);
        }

        admission.setTerminatedBy(getCurrentUser(request).getId());
        admission.setTerminationReason((String) results.getResults().get("terminationReason"));

        if (!handleUploadedFile("attachment", "Additional attachment", getAttachmentDirectory(), false, request)) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderView("admissions/terminate.html", model);
        }

        if ((file = getUploadedFile("attachment", request)) != null) {
            admission.setTerminationAttachment(file.getName());
        }

        admission.setStatus(AdmissionStatus.Discharged);
        admissionsService.terminateAdmission(admission);

        auditService.log(format("%s termination admission (%d) for patient with id %s.",
                getCurrentUser(request), admission.getId(), admission.getPatientMrn()), request);

        setSessionSuccessMessage("Admission terminate successfully!", request);
        return temporaryRedirect(withBaseUrl("Terminated"), response);
    }
}
