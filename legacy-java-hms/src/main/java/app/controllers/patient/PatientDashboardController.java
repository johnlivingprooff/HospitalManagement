package app.controllers.patient;

import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.account.Sex;
import app.models.appointments.Appointment;
import app.models.patient.Insurance;
import app.models.patient.Patient;
import app.models.permission.AclPermission;
import app.services.admissions.AdmissionsService;
import app.services.appointments.AppointmentService;
import app.services.dentistry.DentalSurgeryService;
import app.services.doctor.ConsultationService;
import app.services.lab.LabService;
import app.services.pharmacy.PharmacyService;
import app.services.theater.TheaterService;
import spark.Request;
import spark.Response;

@RouteController(path = "/Hms/Patient")
public class PatientDashboardController extends PatientInfoBaseController {

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

    @Action(path = "/Nok", permission = AclPermission.PerformPatientActivities)
    public String getPatientNok(Request request, Response response) {
        Model model;
        Patient patient;

        patient = getCurrentPatient(request);

        model = createModel(request);
        model.put("patient", patient);
        model.put("nok", patientService.getNextOfKin(patient));
        return renderView("patient/nok/view.html", model);
    }

    @Action(path = "/Insurance", permission = AclPermission.PerformPatientActivities)
    public String getPatientInsurance(Request request, Response response) {
        Model model;
        Patient patient;
        Insurance insurance;

        patient = getCurrentPatient(request);

        insurance = patientService.getInsurance(patient);

        model = createModel(request);
        model.put("insurance", insurance);
        model.put("patient", patient);
        model.put("type", patient.getType());
        return renderView("patient/insurance/view.html", model);
    }

    @Action(path = "/Bills", permission = AclPermission.PerformPatientActivities)
    public String getPatientBills(Request request, Response response) {
        Model model;
        Patient patient;

        patient = getCurrentPatient(request);

        model = createModel(request);
        model.put("patient", patient);
        model.put("bills", patientService.getPatientBills(patient));
        return renderView("patient/bills/list.html", model);
    }

    @Action(path = "/Births", permission = AclPermission.PerformPatientActivities)
    public String getPatientBirths(Request request, Response response) {
        Model model;
        Patient patient;

        patient = getCurrentPatient(request);

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

    private String getPatientAppointments(Request request, Response response, Appointment.Type type) {
        Model model;
        Patient patient;

        patient = getCurrentPatient(request);

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

    @Action(path = "/Appointments", permission = AclPermission.PerformPatientActivities)
    public String getPatientActiveAppointments(Request request, Response response) {
        return getPatientAppointments(request, response, Appointment.Type.Active);
    }

    @Action(path = "/Appointments/Cancelled", permission = AclPermission.PerformPatientActivities)
    public String getPatientCancelledAppointments(Request request, Response response) {
        return getPatientAppointments(request, response, Appointment.Type.Cancelled);
    }

    @Action(path = "/Appointments/Completed", permission = AclPermission.PerformPatientActivities)
    public String getPatientCompletedAppointments(Request request, Response response) {
        return getPatientAppointments(request, response, Appointment.Type.Completed);
    }
}
