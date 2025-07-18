package app.models.permission;

// TODO Convert into an inheritable/extensible class
public interface AclPermission {

    String ReadAuditLogs = "ReadAuditLogs";
    String ArchiveAuditLogs = "ArchiveAuditLogs";
    String ReadPermissions = "ReadPermissions";
    String ReadRoles = "ReadRoles";
    String ReadUsers = "ReadUsers";
    String WriteUsers = "WriteUsers";
    String WriteRoles = "WriteRoles";
    String ReadSections = "ReadSections";
    String WriteSections = "WriteSections";
    String ReadReception = "ReadReception";
    String WriteBilling = "WriteBilling";
    String ReadPatients = "ReadPatients";
    String WritePatients = "WritePatients";
    String ReadSettings = "ReadSettings";
    String WriteSettings = "WriteSettings";
    String ReadVersion = "ReadVersion";
    String ReadAccounts = "ReadAccounts";
    String WriteAccounts = "WriteAccounts";

    // Location permissions
    String ReadRegions = "ReadRegions";
    String WriteRegions = "WriteRegions";
    String ReadWorkstations = "ReadWorkstations";
    String WriteWorkstations = "WriteWorkstations";
    String ReadDistricts = "ReadDistricts";
    String WriteDistricts = "WriteDistricts";
    String ReadDepartments = "ReadDepartments";
    String WriteDepartments = "WriteDepartments";

    String ViewInPatients = "ViewInPatients";
    String ViewOutPatients = "ViewOutPatients";
    String AddPatients = "AddPatients";

    String AccessDoctors = "AccessDoctors";
    String ReadDoctors = "ReadDoctors";
    String WriteDoctors = "WriteDoctors";

    String ReadWards = "ReadWards";
    String WriteWards = "WriteWards";

    String ReadBeds = "ReadBeds";
    String WriteBeds = "WriteBeds";

    String ReadNurses = "ReadNurses";

    String ReadMedicineCategories = "ReadMedicineCategories";
    String WriteMedicineCategories = "WriteMedicineCategories";

    String WriteMedicines = "WriteMedicines";
    String ReadMedicines = "ReadMedicines";

    String ReadMedicineLocations = "ReadMedicineLocations";
    String WriteMedicineLocations = "WriteMedicineLocations";

    String ReadPrescriptions = "ReadPrescriptions";
    String WritePrescriptions = "WritePrescriptions";
    String DispenseMedicine = "DispenseMedicine";

    String WriteAdmissionRates = "WriteAdmissionRates";

    String ReadAdmissions = "ReadAdmissions";
    String WriteAdmissions = "WriteAdmissions";
    String ReadAdmissionDetails = "ReadAdmissionDetails";
    String TerminateAdmissions = "TerminateAdmissions";

    String ReadLabProcedures = "ReadLabProcedures";
    String WriteLabProcedures = "WriteLabProcedures";
    String WriteLabTests = "WriteLabTests";
    String ReadLabTests = "WriteLabTests";

    String AccessDentalProcedures = "AccessDentalProcedures";
    String WriteDentalProcedures = "WriteDentalProcedures";
    String ReadDentalSurgeries = "ReadDentalSurgeries";
    String WriteDentalSurgeries = "WriteDentalSurgeries";

    String WriteTheaterProcedures = "WriteTheaterProcedures";
    String ReadTheaterProcedures = "ReadTheaterProcedures";
    String ReadTheaterSurgeries = "ReadTheaterSurgeries";
    String WriteTheaterSurgeries = "WriteTheaterSurgeries";

    String WriteConsultations = "WriteConsultations";
    String ReadConsultations = "ReadConsultations";
    String ReadConsultationResults = "ReadConsultationResults";
    String WriteConsultationResults = "WriteConsultationResults";

    String WriteAdminSettings = "WriteAdminSettings";

    String AccessReception = "AccessReception";

    String ReadPatientNok = "ReadPatientNok";
    String WritePatientNok = "WritePatientNok";

    String ReadPatientInsurance = "ReadPatientInsurance";
    String WritePatientInsurance = "WritePatientInsurance";
    String ViewPatientLabTests = "ViewPatientLabTests";

    String ViewPatientAttachments = "ViewPatientAttachments";
    String AddPatientAttachments = "AddPatientAttachments";
    String EditPatientAttachments = "EditPatientAttachments";

    String WriteSystemSettings = "WriteSystemSettings";

    String AccessScheduleModule = "AccessScheduleModule";

    String AccessBilling = "AccessBilling";
    String AccessNursesStation = "AccessNursesStation";

    String ViewPatientVitals = "ViewPatientVitals";
    String ViewPatientBills = "ViewPatientBills";

    String ViewPatientBirths = "ViewPatientBirths";
    String WritePatientBirths = "WritePatientBirths";
    String ReadPatientDeathReport = "ReadPatientDeathReport";
    String WritePatientDeathReport = "WritePatientDeathReport";
    String ReadDeathReports = "ReadDeathReports";
    String ReadPatientAdmissions = "ReadPatientAdmissions";
    String ReadPatientPrescriptions = "ReadPatientPrescriptions";
    String ReadPatientLabResults = "ReadPatientLabResults";
    String ReadPatientDentalResults = "ReadPatientDentalResults";
    String ReadPatientOperations = "ReadPatientOperations";
    String ReadPatientAppointments = "ReadPatientAppointments";
    String ReadPatientConsultations = "ReadPatientConsultations";

    String PerformPatientActivities = "PerformPatientActivities";
}