package app.controllers.patient;

import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.appointments.Appointment;
import app.models.doctor.ConsultationResult;
import app.models.patient.Patient;
import app.models.permission.AclPermission;
import app.models.pharmacy.medicine.Prescription;
import app.services.admissions.AdmissionsService;
import app.services.appointments.AppointmentService;
import app.services.dentistry.DentalSurgeryService;
import app.services.doctor.ConsultationService;
import app.services.lab.LabService;
import app.services.pharmacy.PharmacyService;
import app.services.theater.TheaterService;
import app.util.LocaleUtil;
import spark.Request;
import spark.Response;

import java.io.File;

@RouteController(path = "/Hms/Patients/:id")
public class SummaryController extends PatientInfoBaseController {

    @Inject
    private AdmissionsService admissionsService;

    @Inject
    private PharmacyService pharmacyService;

    @Inject
    private LabService labService;

    @Inject
    private DentalSurgeryService dentalService;

    @Inject
    private TheaterService theaterService;

    @Inject
    private AppointmentService appointmentService;

    @Inject
    private ConsultationService consultationService;

    @Action(path = "/Admissions", permission = AclPermission.ReadPatientAdmissions)
    public String getPatientAdmissions(Request request, Response response) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        model = createModel(request);
        model.put("patient", patient);
        model.put("admissions", admissionsService.getPatientAdmissions(patient));
        return renderView("patient/admissions/list.html", model);
    }

    @Action(path = "/Prescriptions", permission = AclPermission.ReadPatientPrescriptions)
    public String getPatientPrescriptions(Request request, Response response) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        model = createModel(request);
        model.put("patient", patient);
        model.put("prescriptions", pharmacyService.getDispensedPatientPrescriptions(patient));
        return renderView("patient/prescriptions/list.html", model);
    }

    @Action(path = "/Prescriptions/:prescription-id/Details", permission = AclPermission.ReadPatientPrescriptions)
    public String getPatientPrescriptionDetails(Request request, Response response) {
        Model model;
        Patient patient;
        Prescription prescription;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        prescription = getPatientResourceById(request, "prescription-id", (id) -> pharmacyService.getPatientsDispensedPrescriptionById(id, patient.getId()));

        if (prescription == null) {
            setSessionErrorMessage("Selected prescription does not exist.", request);
            return redirectToPatientSubSection(response, patient, "Prescriptions");
        }

        model = createModel(request);
        model.put("patient", patient);
        model.put("prescription", prescription);
        model.put("drugs", pharmacyService.getPrescriptionDrugs(prescription));
        return renderView("patient/prescriptions/details.html", model);
    }

    @Action(path = "/Dental", permission = AclPermission.ReadPatientDentalResults)
    public String getPatientDentalResults(Request request, Response response) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        model = createModel(request);
        model.put("patient", patient);
        model.put("surgeries", dentalService.getPatientDentalSurgeries(patient.getId()));
        return renderView("patient/dental/list.html", model);
    }

    @Action(path = "/Operations", permission = AclPermission.ReadPatientOperations)
    public String getPatientOperations(Request request, Response response) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        model = createModel(request);
        model.put("patient", patient);
        model.put("surgeries", theaterService.getPatientSurgeries(patient.getId()));
        return renderView("patient/theater/list.html", model);
    }

    private String getPatientAppointments(Request request, Response response, Appointment.Type type) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        model = createModel(request);

        switch (type) {
            case Active:
                model.put("appointments", appointmentService.getActivePatientAppointments(patient));
                model.put("type", type);
                break;
            case Cancelled:
                model.put("appointments", appointmentService.getCancelledPatientAppointments(patient));
                model.put("type", type);
                model.put("cancelled", true);
                break;
            case Completed:
                model.put("appointments", appointmentService.getCompletedPatientAppointments(patient));
                model.put("type", type);
                break;
        }

        model.put("patient", patient);
        return renderView("patient/appointments/list.html", model);
    }

    @Action(path = "/Appointments", permission = AclPermission.ReadPatientAppointments)
    public String getPatientActiveAppointments(Request request, Response response) {
        return getPatientAppointments(request, response, Appointment.Type.Active);
    }

    @Action(path = "/Appointments/Cancelled", permission = AclPermission.ReadPatientAppointments)
    public String getPatientCancelledAppointments(Request request, Response response) {
        return getPatientAppointments(request, response, Appointment.Type.Cancelled);
    }

    @Action(path = "/Appointments/Completed", permission = AclPermission.ReadPatientAppointments)
    public String getPatientCompletedAppointments(Request request, Response response) {
        return getPatientAppointments(request, response, Appointment.Type.Completed);
    }

    @Action(path = "/Consultations", permission = AclPermission.ReadPatientConsultations)
    public String getPatientConsultations(Request request, Response response) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        model = createModel(request);
        model.put("patient", patient);
        model.put("results", consultationService.getPatientConsultationResults(patient.getId()));
        return renderView("patient/consultations/list.html", model);
    }

    private ConsultationResult getSelectedConsultation(Request request, Patient patient) {
        return getPatientResourceById(request, "cid", id -> consultationService.getPatientConsultationById(id, patient.getId()));
    }

    @Action(path = "/Consultations/:cid/Details", permission = AclPermission.ReadPatientConsultations)
    public String getPatientConsultationDetails(Request request, Response response) {
        Model model;
        Patient patient;
        ConsultationResult result;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if ((result = getSelectedConsultation(request, patient)) == null) {
            setSessionErrorMessage("Selected consultation record does not exist.", request);
            return redirectToPatientSubSection(response, patient, "Consultations");
        }

        model = createModel(request);
        model.put("patient", patient);
        model.put("result", result);
        return renderView("patient/consultations/details.html", model);
    }

    @Action(path = "/Consultations/:cid/Attachment", permission = AclPermission.ReadPatientConsultations)
    public Object getConsultationAttachment(Request request, Response response) {
        File file;
        Patient patient;
        ConsultationResult result;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if ((result = getSelectedConsultation(request, patient)) == null) {
            setSessionErrorMessage("Selected consultation record does not exist.", request);
            return redirectToPatientSubSection(response, patient, "Consultations");
        }

        if (result.getAttachment() != null) {
            file = new File(getAttachmentDirectory(), result.getAttachment());
            if (file.exists()) {
                return serveFile(response, file, format("%s_consultation_results%s",
                        result.getPatientName(), LocaleUtil.getFileExtensionWithPeriod(file.getName())));
            }
        }

        setSessionErrorMessage("Attachment does not exist.", request);
        return redirectToPatientSubSection(response, patient, "Consultations");
    }
}
